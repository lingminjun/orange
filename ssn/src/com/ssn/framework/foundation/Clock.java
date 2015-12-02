package com.ssn.framework.foundation;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by lingminjun on 15/7/16.
 */
public final class Clock {
    private Clock() {}

    private static Clock newInstance() {
        return new Clock();
    }

    private static Clock _instance = null;

    /**
     * 唯一实例
     * @return
     */
    public static Clock shareInstance() {
        if (_instance != null) {return _instance;}
        synchronized (Clock.class) {
            if (_instance == null) {
                _instance = newInstance();
            }
        }
        return _instance;
    }

    /**
     * 开启定时器
     * @return 操作是否成功
     */
    public synchronized boolean start() {
        if (_timer != null) {
            return false;
        }
        _start();
        return true;
    }

    /**
     * 停止定时器，并不会移除监听者
     * @return 操作是否成功
     */
    public synchronized boolean pause() {
        if (_timer == null) {
            return false;
        }
        _stop();
        return true;
    }

    /**
     * 添加监听者，添加监听者后会启动timer
     * @param listener 监听者
     * @param flag  标记
     */
    public void addListener(@NonNull Listener listener, @NonNull String flag) {
        if (listener == null || flag == null) {return;}
        _addListener(listener,flag);
    }

    /**
     * 移除对应的监听者，当最后一个对象被移除后，将结束timer
     * @param flag
     */
    public void removeListener(String flag){
        if (flag == null) {return;}
        _removeListener(flag);
    }

    /**
     * 移除所有监听者，timer会结束
     */
    public void removeAllListeners() {_removeAllListeners();}

    /**
     * 监听者回调
     * @param
     */
    public interface Listener {
        public void fire(String flag);
    }

    ////////////////一下私有方法实现/////////////////////////////

    private HashMap<String,Listener> _fires = new HashMap<String, Listener>();
    private ScheduledExecutorService _timer;

    private void dispatch() {
        Map<String,Listener> map = listeners();

        Set<String> keys = map.keySet();
        for (String flag : keys) {
            Listener listener = map.get(flag);
            try {
                listener.fire(flag);
            } catch (Throwable e) {
                APPLog.error(e);
            }
        }
    }

    private Handler _handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            dispatch();
        }
    };

    private void _start() {
        if (_timer != null) return;
        _timer = Executors.newScheduledThreadPool(1);
        TimerTask task = new TimerTask() {
            public void run() {
                Message msg = _handler.obtainMessage();
                _handler.sendMessage(msg);
            }
        };
        //一秒触发一次
        _timer.scheduleAtFixedRate(task, 0, 1000, TimeUnit.MILLISECONDS);
    }

    private void _stop() {
        if (_timer != null){
            _timer.shutdown();
            _timer = null;
        }
    }

    private synchronized void _addListener(@NonNull Listener listener, @NonNull String flag) {
        _fires.put(flag,listener);
        _start();
    }

    private synchronized void _removeListener(String flag){
        _fires.remove(flag);
    }

    private synchronized void _removeListeners(String hasPrefix){
        Iterator<Map.Entry<String,Listener>> iter = _fires.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,Listener> entry = (Map.Entry<String,Listener>) iter.next();
            String key = entry.getKey();
            if (key.startsWith(hasPrefix)) {
                _fires.remove(key);
            }
        }
    }


    private synchronized void _removeAllListeners(){
        _fires.clear();
        _stop();
    }

    private synchronized final Map<String,Listener> listeners() {
        if (_fires.size() == 0) {_stop();}
        return new HashMap<String, Listener>(_fires);
    }
}
