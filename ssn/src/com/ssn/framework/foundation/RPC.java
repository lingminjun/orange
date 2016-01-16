package com.ssn.framework.foundation;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by lingminjun on 15/10/20.
 */
public final class RPC {

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
        private Request<? extends Object> _nextReq;
        private Request<?> _prevReq;

        /**
         * 是否为链式请求
         */
        public boolean isChained;//

        /**
         * 忽略失败，主要用于链式响应参数，默认前一个失败
         */
        public boolean ignoreError;//

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
        public Request<? extends Object> nextRequest(Request<? extends Object> req) {
            _nextReq = req;
            return req;
        }


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

        /**
         * 重置请求状态，复用请求体，若一个请求cancel后需要重新被发起，需要调用此方法重置
         */
        public void reset() {
            _cancel = true;
        }
    }

    /**
     * 响应回调
     */
    public static class Response<T> {

        /**简单响应**/
        public void onStart(){};
        public void onCache(T t){};
        public void onSuccess(T t){};
        public void onFailure(Exception e){};
        public void onFinish(){};

        /**链式响应回调接口，没有特定返回值类型**/
        public void onStart(final Request<? extends Object> req){};
        public void onCache(final Request<? extends Object> req,T t, int i){};
        public void onSuccess(final Request<? extends Object> req,T t, int i){};
        public void onFailure(final Request<? extends Object> req,Exception e, int i){};
        public void onFinish(final Request<? extends Object> req){};

        /**
         * 获取响应持有者
         * @return
         */
        public Object getHostObject() {
            //访问私有属性
            Class<?> type = this.getClass();
            Field field = null;
//        Field[] fields = null;
            try {
//            fields = type.getDeclaredFields();
                field = type.getDeclaredField("this$0");
                field.setAccessible(true);
                return field.get(this);
            } catch (Throwable e) {}

            return null;
        }
    }

    /**
     * 中断器，注意中断器使用，不要引用activity以及其他ui
     */
    public static interface Interceptor {
        /**
         * 是否中断，返回true表示中断，返回false表示不中断，若intercept产生异常，则直接中断。
         * 注意此方法将在线程跨越和网络返回时都会进行检查。
         * @param req
         * @param res
         * @return
         */
        public boolean intercept(Request<?> req, Response<?> res);
    }

    /**
     * 设置中断器，注意，此方法请在Application初始化时调用，效率考虑，暂时没有加锁处理，请谨慎使用
     * @param interceptor
     */
    public static void setInterceptor(Interceptor interceptor) {
//        synchronized (RPC.class) {
            itpt = interceptor;
//        }
    }

    /**
     * 移除拦截器
     */
//    public static void removeInterceptor() {
//        synchronized (RPC.class) {
//            itpt = null;
//        }
//    }

    /**
     * 是否中断
     * @param req
     * @param res
     * @return
     */
    private static boolean checkInterceptor(final Request<?> req, final Response<?> res) {
        if (itpt != null) {
            boolean intercept = false;
            try {
                intercept = itpt.intercept(req, res);
            } catch (Throwable e) {
                intercept = true;
            }
            return intercept;
        }
        return false;
    }

    /**
     * 调用过程，调用发生在异步线程，回调在ui线程
     * @param req
     * @param res
     * @param <T1>
     * @param <T2>
     * @return
     */
    public static <T1,T2 extends Object> Cancelable call(final Request<T1> req, final Response<T2> res) {
        return call(req,res,false);
    }

    /**
     * 调用过程，调用发生在异步线程，回调在ui线程
     * @param req
     * @param res
     * @param <T1>
     * @param <T2>
     * @return
     */
    public static <T1,T2 extends Object> Cancelable call(final Request<T1> req, final Response<T2> res, final boolean block) {

        if (req == null || res == null) {
            return null;
        }

        if (block) {
            if (req.isChained) {
                chainCallIMP(req,res);
            } else {
                callIMP(req, res);
            }
        } else {

            Runnable reqRun = new Runnable() {
                @Override
                public void run() {

                    if (checkInterceptor(req,res)) {
                        return;
                    }

                    if (req.isChained) {
                        chainCallIMP(req,res);
                    } else {
                        callIMP(req, res);
                    }
                }
            };

            TaskQueue.commonQueue().execute(reqRun);
        }

        return req;
    }

    /**
     * 记录Interceptor
     */
    private static Interceptor itpt = null;

    private static <T1,T2 extends Object> void callIMP(final Request<T1> req, final Response<T2> res) {
        //检查是否取消请求
        if (req._cancel) {
            req.reset();
            return;
        }

        //开始请求
        TaskQueue.mainQueue().execute(new Runnable() {
            @Override
            public void run() {
                if (checkInterceptor(req,res)) {
                    return;
                }
                res.onStart();
            }
        });

        //读取文件缓存
        try {
            final T2 o = (T2)(req.cache());
            if (o != null) {//确实取到缓存数据
                TaskQueue.mainQueue().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (checkInterceptor(req,res)) {
                            return;
                        }
                        res.onCache(o);
                    }
                });
            }
        } catch (Throwable e) {//缓存实现失败不做处理
            e.printStackTrace();
        }

        boolean intercept = false;
        try {

            Retry retry = new Retry();
            req._result = req.call(retry);
            int times = retry.retryTimes;

            //开始重试
            while (times > 0 && req._result == null) {
                APPLog.error("rpc retry", req.toString());
                times--;
                retry.isLast = times == 0;
                req._result = req.call(retry);
            }

            intercept = checkInterceptor(req,res);
            if (!intercept) {
                TaskQueue.mainQueue().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (checkInterceptor(req,res)) {
                            return;
                        }
                        res.onSuccess((T2)(req._result));
                    }
                });
            }
        } catch (final Exception e) {
            APPLog.error("rpc error", e.toString());
            //非直接中断
            intercept = checkInterceptor(req,res);
            if (!intercept) {
                //异常回调
                TaskQueue.mainQueue().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (checkInterceptor(req, res)) {
                            return;
                        }
                        res.onFailure(e);
                    }
                });
            }
        } catch (Throwable throwable){
            APPLog.error("rpc error", throwable.toString());

            intercept = checkInterceptor(req,res);
            //非直接中断
            if (!intercept) {
                final RuntimeException e = new RuntimeException(throwable);
                //异常回调
                TaskQueue.mainQueue().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (checkInterceptor(req, res)) {
                            return;
                        }
                        res.onFailure(e);
                    }
                });
            }
        }finally {
            //最终回调
            if (!intercept) {
                TaskQueue.mainQueue().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (checkInterceptor(req, res)) {
                            return;
                        }
                        res.onFinish();
                    }
                });
            }

            //重置请求
            req.reset();
        }
    }

    private static void chainCallIMP(final Request<? extends Object> mainReq, final Response<? extends Object> res) {
        //检查是否取消请求
        if (mainReq._cancel) {
            mainReq.reset();
            return;
        }

        //开始请求
        TaskQueue.mainQueue().execute(new Runnable() {
            @Override
            public void run() {
                if (checkInterceptor(mainReq,res)) {
                    return;
                }
                res.onStart(mainReq);
            }
        });

        //读取文件缓存
        int idx = 0;
        Request<? extends Object> req = mainReq;
        while (req != null) {

            //请求已经取消，cancel连续性考虑
            if (req._cancel) {
                mainReq.reset();
                req.reset();
                return;
            }

            //请求已经取消，检查是否取消请求
            if (mainReq._cancel) {
                mainReq.reset();
                req.reset();
                return;
            }


            int st = callIMP(mainReq,req,idx,res);
            if (st < 0) {//表明已经中断
                mainReq.reset();
                req.reset();
                return;
            }

            //请求失败时，若标明不忽略错误，则停止继续请求
            if (st == 0 && !req.ignoreError) {
                break;
            }

            //设置其前一个请求体
            if (req._nextReq != null) {
                req._nextReq._prevReq = req;
            }

            //继续下一个响应
            req = req._nextReq;
        }

        //最终回调
        TaskQueue.mainQueue().execute(new Runnable() {
                @Override
                public void run() {
                    if (checkInterceptor(mainReq,res)) {
                        return;
                    }
                    res.onFinish();
                }
            });

        mainReq.reset();
    }

    /**
     * 链式响应请求体
     * @param mainReq
     * @param req
     * @param idx
     * @param res
     * @param <T1,T2>
     * @return 请求状态，0：失败；1：成功；-1：中断
     */
    private static <T1,T2> int callIMP(final Request<? extends Object> mainReq, final Request<T1> req, final int idx, final Response<T2> res) {

        //读取文件缓存
        try {
            final T2 o = (T2)(req.cache());
            if (o != null) {//确实取到缓存数据
                TaskQueue.mainQueue().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (checkInterceptor(req,res)) {
                            return;
                        }
                        res.onCache(req, o, idx);
                    }
                });
            }
        } catch (Throwable e) {//缓存实现失败不做处理
            e.printStackTrace();
        }

        int result = 1;
        try {
            Retry retry = new Retry();
            req._result = req.call(retry);
            int times = retry.retryTimes;

            //开始重试
            while (times > 0 && req._result == null) {
                APPLog.error("rpc retry", req.toString());
                times--;
                retry.isLast = times == 0;
                req._result = req.call(retry);
            }

            //直接中断
            if (checkInterceptor(req,res)) {
                return -1;
            }

            //成功回调
            TaskQueue.mainQueue().execute(new Runnable() {
                @Override
                public void run() {
                    if (checkInterceptor(req,res)) {
                        return;
                    }
                    res.onSuccess(req,(T2)(req._result),idx);
                }
            });
        } catch (final Exception e) {
            APPLog.error("rpc error", e.toString());

            //直接中断
            if (checkInterceptor(req,res)) {
                result = -1;
            } else {
                //异常回调
                TaskQueue.mainQueue().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (checkInterceptor(req, res)) {
                            return;
                        }
                        res.onFailure(req, e, idx);
                    }
                });
                result = 0;
            }
        } catch (Throwable throwable){
            APPLog.error("rpc error", throwable.toString());
            //直接中断
            if (checkInterceptor(req,res)) {
                result = -1;
            } else {
                final RuntimeException e = new RuntimeException(throwable);
                //异常回调
                TaskQueue.mainQueue().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (checkInterceptor(req, res)) {
                            return;
                        }
                        res.onFailure(req, e, idx);
                    }
                });

                result = 0;
            }
        }

        return result;
    }
}
