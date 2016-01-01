package com.ssn.framework.foundation;

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
                        res.onCache(o);
                    }
                });
            }
        } catch (Throwable e) {//缓存实现失败不做处理
            e.printStackTrace();
        }

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

            TaskQueue.mainQueue().execute(new Runnable() {
                @Override
                public void run() {
                    res.onSuccess((T2)(req._result));
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
                res.onStart(mainReq);
            }
        });

        //读取文件缓存
        int idx = 0;
        Request<? extends Object> req = mainReq;
        while (req != null) {

            //cancel连续性考虑
            if (req._cancel) {
                req.reset();
                break;
            }

            //检查是否取消请求
            if (mainReq._cancel) {
                mainReq.reset();
                break;
            }

            /**
             * 请求失败时，若标明不忽略错误，则停止继续请求
             */
            if (!callIMP(mainReq,req,idx,res) && !req.ignoreError) {
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
                    res.onFinish();
                }
            });
    }

    /**
     * 链式响应请求体
     * @param mainReq
     * @param req
     * @param idx
     * @param res
     * @param <T1,T2>
     * @return 请求是否成功
     */
    private static <T1,T2> boolean callIMP(final Request<? extends Object> mainReq, final Request<T1> req, final int idx, final Response<T2> res) {

        //读取文件缓存
        try {
            final T2 o = (T2)(req.cache());
            if (o != null) {//确实取到缓存数据
                TaskQueue.mainQueue().execute(new Runnable() {
                    @Override
                    public void run() {
                        res.onCache(req, o, idx);
                    }
                });
            }
        } catch (Throwable e) {//缓存实现失败不做处理
            e.printStackTrace();
        }

        boolean result = true;
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

            //成功回调
            TaskQueue.mainQueue().execute(new Runnable() {
                @Override
                public void run() {
                    res.onSuccess(req,(T2)(req._result),idx);
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

            result = false;
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

            result = false;
        }

        return result;
    }
}
