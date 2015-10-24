package com.ssn.framework.foundation;

import android.app.Activity;
import android.content.Context;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;

import java.lang.reflect.Field;

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
     * 请求接口
     * @param <T>
     */
    public static abstract class Request<T> implements Cancelable {
        private boolean _cancel;

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
         * @return
         */
        public abstract T call();
    }

    /**
     * 响应回调
     * @param <T>
     */
    public static class Response<T> {

        public void onStart(){};
//        public void onCache(T t){};
        public void onSuccess(T t){};
        public void onFailure(Exception e){};
        public void onFinish(){};

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

        if (req == null || res == null) {
            return null;
        }

        Runnable reqRun = new Runnable() {
            @Override
            public void run() {

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
                    final T t = req.call();
                    //成功回调
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
        };

        TaskQueue.commonQueue().execute(reqRun);

        return req;
    }

}
