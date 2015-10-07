package com.orange.m.biz;

import android.app.Application;
import com.ssn.framework.foundation.APPLog;

/**
 * Created by lingminjun on 15/10/7.
 */
public final class UserCenter {
    private static UserCenter _instance = null;

    /**
     * 用户中心
     * @return 唯一实例
     */
    static public UserCenter shareInstance() {
        if (_instance != null) return _instance;
        synchronized(UserCenter.class){
            if (_instance != null) return _instance;
            _instance = newInstance();
            return _instance;
        }
    }

    private static UserCenter newInstance() {
        return new UserCenter();
    }

    /**
     * 防止构造实例
     */
    private UserCenter() {
        super();
    }


    /**
     * 应用程序启动,务必在Application onCreate方法中调用，调用一次后失效
     */
    public boolean applicationDidLaunch(Application application) {
        if (_application != null) {
            APPLog.error("UserCenter didLauch 方法 务必在Application onCreate方法中调用，调用一次后失效！！！");
            return false;
        }

        if (application == null) {
            APPLog.error("UserCenter didLauch 方法 务必在Application onCreate方法中调用，调用一次后失效！！！");
            return false;
        }

        _application = application;

        return true;
    }


    /**
     * 当前应用是否有用户登录
     * @return
     */
    public boolean isLogin() {
        synchronized (this) {
            return false;
        }
    }

    private Application _application;
    private static final String TOKEN_INFO_DIR  = "/users/";
    private static final String UID_MD5         = "sfht.user.uid.md5";
}
