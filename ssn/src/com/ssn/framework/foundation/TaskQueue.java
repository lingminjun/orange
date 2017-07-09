package com.ssn.framework.foundation;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by lingminjun on 15/9/14.
 */
public final class TaskQueue {
    private static final int CONCURRENCE_COUNT = 4;//并发数
    private static final int THREAD_MIN_SIZE = 3;//默认保留线程数，尽量反复利用已有空闲线程，而不是创建线程
    private static final int TASK_SIZE = 127;
    private static final int KEEP_ALIVE_TIME = 20;//20秒

    /**
     * 构造方法
     */
    public TaskQueue() {
        this(null);
    }
    public TaskQueue(String name) {
        this(CONCURRENCE_COUNT,name);
    }
    public TaskQueue(int concurrenceCount,String name) {
        this(concurrenceCount,TASK_SIZE,name);
    }
    public TaskQueue(int concurrenceCount, int taskSize,String name) {
        int con_count = concurrenceCount <= 0? CONCURRENCE_COUNT : concurrenceCount;
        int task_size = taskSize <= 0 ? TASK_SIZE : taskSize;
        int thread_size = con_count < THREAD_MIN_SIZE ? con_count : THREAD_MIN_SIZE;

        queue = new ArrayBlockingQueue(task_size);
        rejectedHanlder = new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                Log.e("task.queue","A task is rejected. " + r.toString(), null);
            }
        };
        pool = new ThreadPoolExecutor(thread_size, con_count, KEEP_ALIVE_TIME, TimeUnit.SECONDS, queue, new CoreThreadFactory(name), rejectedHanlder);
    }

    /**
     * 通用并发队列
     * @return
     */
    private static TaskQueue common_queue = null;
    public static TaskQueue commonQueue() {
        if (common_queue != null) return common_queue;
        synchronized (TaskQueue.class) {
            if (common_queue == null) {
                common_queue = newTaskQueue(8,127,"common");
            }
        }
        return common_queue;
    }

    /**
     * 主线程队列
     * @return
     */
    private static TaskQueue main_queue = null;
    public static TaskQueue mainQueue() {
        if (main_queue != null) return main_queue;
        synchronized (TaskQueue.class) {
            if (main_queue == null) {
                main_queue = newMainQueue();
            }
        }
        return main_queue;
    }


    /**
     * 异步执行事件方法
     * @param r
     */
    public void execute(Runnable r) {
        if (r != null) {
            _execute(r);
        }
    }

    /**
     * 异步执行事件方法
     * @param r
     * @param delayMillis 小于等于零时不做延迟
     */
    public void executeDelayed(final Runnable r, final long delayMillis) {
        if (r != null) {
            _executeDelayed(r,delayMillis);
        }
    }

    /**
     * 只有延迟的runnable可以被cancel
     * @param r
     */
    public void cancel(final Runnable r) {
        if (r != null) {
            _cancel(r);
        }
    }

    /*****************一下为私有实现*******************/
    static class SaftyRunnable implements Runnable {
        public SaftyRunnable(Runnable runnable) {
            if (runnable == null) {throw new RuntimeException("Please make sure that the incoming effective Runnable");}
            _run = runnable;
        }

        private Runnable _run;

        @Override
        public void run() {
            try {
                _run.run();
            } catch (Throwable e) {}
        }
    }

    private void _execute(Runnable r) {
        SaftyRunnable rr = new SaftyRunnable(r);
        if (pool != null) {
            pool.execute(rr);
        }
        else {
            mainHandler.post(rr);
        }
    }

    private void _executeDelayed(final Runnable r, final long delayMillis) {
        final Integer code = r.hashCode();
        if (pool != null) {
            if (delayMillis <= 0) {
                pool.execute(new SaftyRunnable(r));
            }
            else {
                rrs.put(code,r);
                pool.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(delayMillis);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        Runnable rr = rrs.remove(code);

                        if (rr != null) {
                            try {
                                rr.run();
                            } catch (Throwable e) {}
                        }
                    }
                });
            }
        }
        else {
//            Log.i("RR","s what="+code);
            mainHandler.sendMessageDelayed(Message.obtain(mainHandler,code,r),(delayMillis < 0 ? 0 : delayMillis));
        }
    }

    private void _cancel(final Runnable r) {
        final Integer code = r.hashCode();
        if (pool != null) {
            rrs.remove(code);
        }
        else {
//            Log.i("RR","c what="+code);
            mainHandler.removeMessages(code);
        }
    }

    private static TaskQueue newTaskQueue(int concurrenceCount, int taskSize,String name) {
        return new TaskQueue(concurrenceCount,taskSize,name);
    }

    private TaskQueue(final boolean main) {
        mainHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                Runnable r = (Runnable)msg.obj;
//                Log.i("RR","e what="+msg.what);
                if (r != null) {
                    try {
                        r.run();
                    } catch (Throwable e) {}
                }
            }
        };
    }

    private static TaskQueue newMainQueue() {
        return new TaskQueue(true);
    }

    private static class CoreThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        CoreThreadFactory(String name) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            String a_name = name != null ? name : "thread";
            namePrefix = "pool-" + poolNumber.getAndIncrement() + "-" + a_name + "-";
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) t.setDaemon(false);
            if (t.getPriority() != Thread.NORM_PRIORITY) t.setPriority(Thread.NORM_PRIORITY);

            t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

                @Override
                public void uncaughtException(Thread thread, Throwable ex) {
                    Log.e("task.queue", thread.getName() + " thrown an exception : ", ex);
                }
            });

            return t;
        }
    }

    private BlockingQueue<Runnable> queue;
    private RejectedExecutionHandler rejectedHanlder;
    private Map<Integer,Runnable> rrs = new ConcurrentHashMap<>();
    private ThreadPoolExecutor pool;

    private Handler mainHandler;
}
