package com.ssn.framework.foundation;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.*;

/**
 * Created by lingminjun on 15/7/5.
 */
public class BroadcastCenter {
    private static class Singleton {
        private static final BroadcastCenter INSTANCE = new BroadcastCenter();
    }

    /**
     * 唯一实例
     * @return
     */
    public static final BroadcastCenter shareInstance() {
        return Singleton.INSTANCE;
    }


    /**
     * 防止构造实例
     */
    private BroadcastCenter() {
        super();
    }

    /**
     * 应用程序启动,务必在Application onCreate方法中调用，调用一次后失效
     */
    public boolean applicationDidLaunch(Application application) {
        if (_application != null) {
            APPLog.info("BroadcastCenter didLauch 方法 务必在Application onCreate方法中调用，调用一次后失效！！！");
            return false;
        }

        if (application == null) {
            APPLog.info("BroadcastCenter didLauch 方法 务必在Application onCreate方法中调用，调用一次后失效！！！");
            return false;
        }

        _application = application;
        _observers = new HashMap<Integer, WeakObserver>();
        _gcQueue = new ReferenceQueue<Object>();

        _allNotices = new HashMap<String, Integer>();
        _innerReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                distributeBroadcast(context, intent);
            }
        };
        IntentFilter filter = new IntentFilter();
        _application.registerReceiver(_innerReceiver, filter);

        return true;
    }

    /////////////////////////////

    /**
     * 监听广播方法
     * @param observer 监听者，弱引用
     * @param notificationName 广播名称，一个通知名只能注册一个方法，若当前监听者尝试监听同一广播多次只有第一次有效，后面都将忽略
     * @param method 监听回调方法
     */
    public void addObserver(@NonNull Object observer,@NonNull String notificationName,@NonNull Method<?> method) {
        paddObserver(observer,notificationName,method);
    }

    /**
     * 监听广播方法，可以不显示移除通知，当observer释放后，通知将自动解除
     * 使用此方法特别要注意的是，其method必须由observer的直接或间接引用，否则此注册将会失效
     * @param observer 监听者，弱引用
     * @param notificationName 广播名称，一个通知名只能注册一个方法，若当前监听者尝试监听同一广播多次只有第一次有效，后面都将忽略
     * @param weakMethod 必须被监听者所引用，否则被释放 TODO : method必须被observer所引用，否则被释放 (仅仅警告提示)
     */
    public void softAddObserver(@NonNull Object observer,@NonNull String notificationName,@NonNull Method<?> weakMethod) {
        if (observer == null || TextUtils.isEmpty(notificationName) || weakMethod == null){
            return ;
        }
        paddObserver(observer,notificationName,new WeakReference<Method<?>>(weakMethod));
    }

    private void paddObserver(@NonNull Object observer,@NonNull String notificationName,@NonNull Object method) {
        if (observer == null || TextUtils.isEmpty(notificationName) || method == null){
            return ;
        }

        cleanCache();

        int key = observer.hashCode();

        synchronized (this) {
            WeakObserver weakObserver = _observers.get(key);

            if (weakObserver != null) {//注册过其他方法

                if (weakObserver.add(notificationName, method)) {
                    addNotice(notificationName,1);
                    APPLog.info("监听者" + Integer.toString(key) + "添加注册" + notificationName + "通知");
                } else {
                    APPLog.info("监听者" + Integer.toString(key) + "重复添加注册" + notificationName + "通知！忽略");
                }

                return;
            }

            //监听注册者是否释放，这里存在问题，实际不会释放，被receiver引用
            weakObserver = new WeakObserver(observer,_gcQueue);
            weakObserver.add(notificationName,method);

            _observers.put(key,weakObserver);

            addNotice(notificationName,1);
            APPLog.info("监听者" + Integer.toString(key) + "添加注册" + notificationName + "通知");
        }
    }

    /**
     * 移除通知
     * @param observer
     */
    public void removeObserver(Object observer) {
        removeObserver(observer,null);
    }

    /**
     * 移除特定通知
     * @param observer
     * @param notificationName
     */
    public void removeObserver(Object observer,String notificationName) {
        if (observer == null) {
            return;
        }

        int key = observer.hashCode();

        synchronized (this) {
            WeakObserver weakObserver = _observers.get(key);
            if (weakObserver == null) {
                return;
            }

            if (TextUtils.isEmpty(notificationName)) {//remove all
                Set<String> actions = weakObserver.actions();//遍历删除么？？？暂时不处理，后面处理吧

                for (String action : actions) {
                    removeNotice(action,1);
                }

                _observers.remove(key);
                APPLog.info("监听者" + Integer.toString(key) + "移除所有注册通知");
            }
            else {
                weakObserver.remove(notificationName);
                if (weakObserver.methodCount() == 0) {
                    _observers.remove(key);
                    APPLog.info("监听者" + Integer.toString(key) + "移除所有注册通知");
                }
                else {
                    APPLog.info("监听者" + Integer.toString(key) + "移除通知" + notificationName);
                }
                removeNotice(notificationName,1);
            }
        }
    }

    /**
     * 播发广播，异步
     * @param intent
     */
    public void postBroadcast(Intent intent) {
        if (intent == null) {return;}
        _application.sendBroadcast(intent);
    }

    /**
     * 总监听者个数
     * @return
     */
    public int getSize() {
        synchronized (this) {
            return _observers.size();
        }
    }

    /**
     * 清理缓存
     */
    public void gc() {
        cleanCache();
    }

    /**
     * 注意，为了防止循环引用，在onReceive中不要持有外部observer实例
     * @param <T>
     */
    public static abstract class Method<T> {
        public abstract void onReceive(T observer, Context context, Intent intent);
    }


    
    //这个类所引用的目标对象会在JVM内存不足时自动回收
    private class WeakObserver extends WeakReference<Object> {

        private int _key;                       //key
//        private Map<String, WeakReference<Method<?> > > _methods;//注册的方法
        private Map<String, Object> _methods;//注册的方法，兼容两套

        public int getKey() {
            return _key;
        }

        public WeakObserver(Object observer, ReferenceQueue<Object> queue) {
            super(observer, queue);
            this._key = observer.hashCode();
//            this._methods = new HashMap<String, WeakReference<Method<?> > >();
            this._methods = new HashMap<String, Object>();
        }

        public long methodCount() {return _methods.size();}

        public Method<?> method(String notificationName) {
            Object obj = _methods.get(notificationName);
            if (obj != null && obj instanceof WeakReference) {
                WeakReference<Method<?>> weak = (WeakReference<Method<?>>)obj;
                return weak.get();
            }
            else {
                return (Method<?>)obj;
            }
        }

        public boolean add(String notificationName, Object methodBox) {
            if (_methods.containsKey(notificationName)) {//防止重复注册，
                return false;
            }

            _methods.put(notificationName,methodBox);
            return true;
        }

        public boolean remove(String notificationName) {
            if (!_methods.containsKey(notificationName)) {//
                return false;
            }
            Object obj = _methods.remove(notificationName);
            if (obj != null) {
                if (obj instanceof WeakReference) {
                    WeakReference<Method<?>> weak = (WeakReference<Method<?>>) obj;
                    weak.clear();
                }
                return true;
            }
            return false;
        }

        public Set<String> actions() {
            return _methods.keySet();
        }
    }

    //添加注册
    private void addNotice(String notice,int add) {

        Set<String> noticeKeys = _allNotices.keySet();
        boolean changed = false;
        if (!noticeKeys.contains(notice)) {
            changed = true;
        }

        Integer num = _allNotices.get(notice);
        int count = 0;
        if (num != null) {
            count = num;
        }

        count += add;
        _allNotices.put(notice, count);

        if (changed) {
            _application.unregisterReceiver(_innerReceiver);

            IntentFilter filter = new IntentFilter();

            noticeKeys = _allNotices.keySet();
            for (String action : noticeKeys) {
                filter.addAction(action);
            }

            _application.registerReceiver(_innerReceiver, filter);
        }
    }

    //移除通知
    private void removeNotice(String notice,int reduce) {
        boolean changed = false;

        Integer num = _allNotices.get(notice);
        int count = 0;
        if (num != null) {
            count = num;
        }

        count -= reduce;

        if (count <= 0) {
            changed = true;
            _allNotices.remove(notice);
        }
        else {//修改计数
            _allNotices.put(notice, count);
        }

        if (changed) {
            _application.unregisterReceiver(_innerReceiver);

            IntentFilter filter = new IntentFilter();

            Set<String> noticeKeys = _allNotices.keySet();
            for (String action : noticeKeys) {
                filter.addAction(action);
            }

            _application.registerReceiver(_innerReceiver, filter);
        }
    }

    private void distributeBroadcast(Context context, Intent intent) {
        String action = intent.getAction();

        List<WeakObserver> observers = null;
        //分发
        synchronized (this) {
            observers = new ArrayList<WeakObserver>(_observers.values());
        }

        for (WeakObserver observer : observers) {
            Object obj = observer.get();
            if (obj == null) {
                synchronized (this) {
                    clearObserver(observer.getKey());
                }
                continue;
            }

            Method<Object> method = (Method<Object>)observer.method(action);
            if (method == null) {
                continue;
            }

            try {
                method.onReceive(obj,context,intent);
            }
            catch (Throwable e) {
                APPLog.error(e);
            }
        }
    }


    /**
     * 清除所有已经释放对象
     */
    private void cleanCache() {
//        System.gc();

        WeakObserver se = null;
        synchronized (this) {
            while ((se = (WeakObserver) _gcQueue.poll()) != null) {
                int key = se.getKey();
                clearObserver(key);
                se.clear();
            }
        }
    }

    private void clearObserver(int key) {
        WeakObserver observer = _observers.get(key);

        if (observer == null) {
            APPLog.info("监听者" + Integer.toString(key) + "已经在前一次被JVM回收");
            return;
        }

        Set<String> actions = observer.actions();//遍历删除么？？？暂时不处理，后面处理吧
        for (String action : actions) {
            removeNotice(action, 1);
        }

        _observers.remove(key);

        observer.clear();
        APPLog.info("监听者" + Integer.toString(key) + "被JVM回收");
    }


    private Application _application;
    private Map<String, Integer> _allNotices;//所有通知名统计
    private BroadcastReceiver _innerReceiver;

    //容器
    private Map<Integer, WeakObserver> _observers;//所有注册者

    //监听对象释放
    private ReferenceQueue<Object> _gcQueue;
}
