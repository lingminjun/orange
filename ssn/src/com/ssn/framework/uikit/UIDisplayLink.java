package com.ssn.framework.uikit;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.Log;
import com.ssn.framework.foundation.APPLog;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by lingminjun on 15/7/16.
 */
public final class UIDisplayLink {

    public static final int FRAME_INTERVAL_MS = 17;//帧间隔

    private int what;
    private UIDisplayLink() {
        what = this.hashCode();
    }

    private static UIDisplayLink newInstance() {
        return new UIDisplayLink();
    }

    private static UIDisplayLink _instance = null;

    /**
     * 唯一实例
     * @return
     */
    public static UIDisplayLink shareInstance() {
        if (_instance != null) {return _instance;}
        synchronized (UIDisplayLink.class) {
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
    public boolean start() {
        if (_timer) {
            return false;
        }
        _start();
        return true;
    }

    /**
     * 停止定时器，并不会移除监听者
     * @return 操作是否成功
     */
    public boolean pause() {
        if (!_timer) {
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
     * 删除以什么开头的注册器
     * @param hasPrefix
     */
    public void removeListeners(String hasPrefix) {
        if (hasPrefix == null) {return;}
        _removeListeners(hasPrefix);
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
    private boolean _timer;

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
            if (!_timer) {
                return;
            }

            dispatch();

            //保持每秒60帧
            _handler.sendMessageDelayed(_handler.obtainMessage(what), FRAME_INTERVAL_MS);
        }
    };

    private void _start() {
        if (_timer) return;
        _timer = true;
        _handler.sendMessage(_handler.obtainMessage(what));
    }

    private void _stop() {
        if (_timer){
            _timer = false;
            _handler.removeMessages(what);
            Log.i("UIDisplayLink","is stop:");
        }
    }

    private void _addListener(@NonNull Listener listener, @NonNull String flag) {
        _fires.put(flag,listener);
        _start();
    }

    private void _removeListener(String flag){
        Object o = _fires.remove(flag);
        Log.i("UIDisplayLink","remove:" + o);
    }

    private void _removeListeners(String hasPrefix){
        Iterator<Map.Entry<String,Listener>> iter = _fires.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<String,Listener> entry = (Map.Entry<String,Listener>) iter.next();
            String key = entry.getKey();
            if (key.startsWith(hasPrefix)) {
                Log.i("UIDisplayLink","remove:" + entry.getValue());
                iter.remove();
            }
        }
    }


    private void _removeAllListeners(){
        _fires.clear();
        Log.i("UIDisplayLink", "remove all");
        _stop();
    }

    private final Map<String,Listener> listeners() {
        if (_fires.size() == 0) {_stop();}
        return new HashMap<String, Listener>(_fires);
    }
}
