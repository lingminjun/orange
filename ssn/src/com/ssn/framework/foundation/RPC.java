package com.ssn.framework.foundation;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;

import java.lang.reflect.Field;
import java.util.Objects;

/**
 * Created by lingminjun on 15/10/20.
 */
public class RPC {

    /**
     * 取消请求
     */
    public static interface Cancelable {
        public boolean cancel();
    }

    /**
     * 重试次数告知，如果修改retryTimes,表示不再重试，重试只在第一次有效
     */
    public static final class Retry {
        public int retryTimes;

        /**
         * 是否可以重复试，若不能重试时，因改返回错误
         * @return
         */
        public boolean canRetry() {
            return !isLast;
        }
        private boolean isLast;
    }

    /**
     * 请求接口
     * @param <T>
     */
    public static abstract class Request<T> implements Cancelable {
        private boolean _cancel;
        private T _result;
        private Request<?> _prevReq;

        /**
         * 取消请求
         * @return
         */
        @Override
        public final boolean cancel() {
            _cancel = true;
            return _cancel;
        }

        /**
         * 请求调用
         * @param retry 返回值不为null时retry被忽略
         * @return
         * @throws Exception
         */
        public abstract T call(Retry retry) throws Exception;

        /**
         * 文件缓存
         * @return
         * @throws Exception
         */
        public abstract T cache() throws Exception;

        /**
         * 链式请求支持
         */
        public Request<? extends Object> nextRequest;


        /**
         * 获取请求响应值，只有请求完成后才有值
         * @return
         */
        public T getResult() {
            return _result;
        }

        /**
         * 获取前一个请求体，可以获取请求的值，前一个必须成功才能走到当前响应
         * @return
         */
        public Request<?> getPrevRequest() {
            return _prevReq;
        }
    }

    /**
     * 响应回调
     * @param <Object>
     */
    public static class Response<Object> {

        public void onStart(){};
//        public void onCache(T t){};
        public void onSuccess(Object t){};
        public void onFailure(Exception e){};
        public void onFinish(){};

        /**链式响应回调接口**/
        public void onStart(final Request<? extends Object> req){};
        public void onCache(final Request<? extends Object> req,Object t, int i){};
        public void onSuccess(final Request<? extends Object> req,Object t, int i){};
        public void onFailure(final Request<? extends Object> req,Exception e, int i){};
        public void onFinish(final Request<? extends Object> req){};

        /*
        private Object getHostObject() {
            //访问私有属性
            Class<?> type = this.getClass();
            Field field = null;
            try {
                field = type.getDeclaredField("this$0");
                field.setAccessible(true);
                return field.get(this);
            } catch (Throwable e) {}

            return null;
        }
        */
    }

    /**
     * 调用过程，调用发生在异步线程，回调在ui线程
     * @param req
     * @param res
     * @param <T>
     * @return
     */
    public static <T> Cancelable call(final Request<T> req, final Response<T> res) {
        return call(req,res,false);
    }

    /**
     * 调用过程，调用发生在异步线程，回调在ui线程
     * @param req
     * @param res
     * @param <T>
     * @return
     */
    public static <T> Cancelable call(final Request<T> req, final Response<T> res, final boolean block) {

        if (req == null || res == null) {
            return null;
        }

        if (block) {

            callIMP(req, res);

        } else {

            Runnable reqRun = new Runnable() {
                @Override
                public void run() {
//                    callIMP(req,res);
                    chainCallIMP(req,res);
                }
            };

            TaskQueue.commonQueue().execute(reqRun);
        }

        return req;
    }

    public static <T> void callIMP(final Request<T> req, final Response<T> res) {
        //检查是否取消请求
        if (req._cancel) {
            return;
        }

        //开始请求
        TaskQueue.mainQueue().execute(new Runnable() {
            @Override
            public void run() {
                res.onStart();
            }
        });

        try {
            Retry retry = new Retry();
            T o = req.call(retry);
            int times = retry.retryTimes;

            //开始重试
            while (times > 0 && o == null) {
                APPLog.error("rpc retry", req.toString());
                times--;
                retry.isLast = times == 0;
                o = req.call(retry);
            }

            //成功回调
            final T t = o;
            TaskQueue.mainQueue().execute(new Runnable() {
                @Override
                public void run() {
                    res.onSuccess(t);
                }
            });
        } catch (final Exception e) {
            APPLog.error("rpc error", e.toString());

            //异常回调
            TaskQueue.mainQueue().execute(new Runnable() {
                @Override
                public void run() {
                    res.onFailure(e);
                }
            });
        } catch (Throwable throwable){
            APPLog.error("rpc error", throwable.toString());
            final RuntimeException e = new RuntimeException(throwable);

            //异常回调
            TaskQueue.mainQueue().execute(new Runnable() {
                @Override
                public void run() {
                    res.onFailure(e);
                }
            });
        }finally {
            //最终回调
            TaskQueue.mainQueue().execute(new Runnable() {
                @Override
                public void run() {
                    res.onFinish();
                }
            });
        }
    }

    public static  void chainCallIMP(final Request<Object> mainReq, final Response<Object> res) {
        //检查是否取消请求
        if (mainReq._cancel) {
            return;
        }

        //开始请求
        TaskQueue.mainQueue().execute(new Runnable() {
            @Override
            public void run() {
                res.onStart(mainReq);
            }
        });

        //读取文件缓存
        int idx = 0;
        Request<? extends Object> req = mainReq;
        while (req != null) {

            callIMP(mainReq,req,idx,res);

            //继续下一个响应
            req = req.nextRequest;
        }

        //最终回调
        TaskQueue.mainQueue().execute(new Runnable() {
                @Override
                public void run() {
                    res.onFinish();
                }
            });
    }


    private static void callIMP(final Request<Object> mainReq, final Request<? extends Object> req, final int idx, final Response<Object> res) {
        //检查是否取消请求
        if (mainReq._cancel) {
            return;
        }

        //读取文件缓存
        try {
            final Object o = req.cache();
            TaskQueue.mainQueue().execute(new Runnable() {
                @Override
                public void run() {
                    res.onCache(req,o,idx);
                }
            });
        } catch (Throwable e) {//缓存实现失败不做处理
            e.printStackTrace();
        }

        try {
            Retry retry = new Retry();
            Object o = req.call(retry);
            int times = retry.retryTimes;

            //开始重试
            while (times > 0 && o == null) {
                APPLog.error("rpc retry", req.toString());
                times--;
                retry.isLast = times == 0;
                o = req.call(retry);
            }

            //成功回调
            final Object t = o;
            TaskQueue.mainQueue().execute(new Runnable() {
                @Override
                public void run() {
                    res.onSuccess(req,t,idx);
                }
            });
        } catch (final Exception e) {
            APPLog.error("rpc error", e.toString());

            //异常回调
            TaskQueue.mainQueue().execute(new Runnable() {
                @Override
                public void run() {
                    res.onFailure(req,e,idx);
                }
            });
        } catch (Throwable throwable){
            APPLog.error("rpc error", throwable.toString());
            final RuntimeException e = new RuntimeException(throwable);

            //异常回调
            TaskQueue.mainQueue().execute(new Runnable() {
                @Override
                public void run() {
                    res.onFailure(req,e,idx);
                }
            });
        }
    }
}
