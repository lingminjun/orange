package com.juzistar.m.biz;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Base64;
import com.alibaba.fastjson.JSON;
import com.juzistar.m.net.BaseRequest;
import com.ssn.framework.foundation.APPLog;
import com.ssn.framework.foundation.BroadcastCenter;
import com.ssn.framework.foundation.HTTPAccessor;
import com.ssn.framework.foundation.UserDefaults;

/**
 * Created by lingminjun on 15/10/7.
 */
public final class UserCenter {

    public static final String USER_LOGIN_NOTIFICATION = "userLoginNotice";
    public static final String USER_LOGOUT_NOTIFICATION = "userLogoutNotice";

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

        String json = UserDefaults.getInstance().getJSONString(USER_TOKEN_KEY);
        if (!TextUtils.isEmpty(json)) {
            try {
                _token = (UserBiz.TokenModel)JSON.parseObject(json, UserBiz.TokenModel.class);
                if (_token != null) {
                    _uid = _token.id;
                }
            } catch (Throwable e) {}
        }

        if (_token != null && !TextUtils.isEmpty(_token.token)) {
            HTTPAccessor.setAuthToken(_token.token);
        }

        return true;
    }


    /**
     * 当前应用是否有用户登录
     * @return
     */
    public boolean isLogin() {
        synchronized (this) {
            return _token != null && _token.token != null && _token.token.length() > 0 && _token.id > 0;
        }
    }

    /**
     * 返回用户id
     * @return
     */
    public long UID() {
        return _uid;
    }

    /**
     * 返回当前用户的昵称
     * @return
     */
    public User user() {
        User us = new User();
        synchronized (this) {
            if (_token != null) {
                us.uid = _token.id;
                us.nick = _token.nickname;
                us.mobile = _token.mobile;
            }
        }
        return us;
    }

    private long _uid;
    private Application _application;
    private static final String TOKEN_INFO_DIR  = "/users/";
    private static final String UID_MD5         = "sfht.user.uid.md5";

    //将数据存储下来
    public void saveToken(BaseRequest.Token tokenModel) {

        refreshToken(tokenModel);

        //抛出成功通知
        if (tokenModel != null && tokenModel.token != null && tokenModel.token.length() > 0) {
            BroadcastCenter.shareInstance().postBroadcast(new Intent(USER_LOGIN_NOTIFICATION));
        }
    }

    public void refreshToken(BaseRequest.Token tokenModel) {
        if (tokenModel == null) {return;}

        UserBiz.TokenModel token = null;
        synchronized (this) {
            token = new UserBiz.TokenModel();

            token.refreshToken = tokenModel.refreshToken;
            token.token = tokenModel.token;

            if (tokenModel instanceof UserBiz.TokenModel) {//更新其他值
                UserBiz.TokenModel userToken = (UserBiz.TokenModel)tokenModel;
                token.id = userToken.id;
                token.mobile = userToken.mobile;
                token.nickname = userToken.nickname;
            }

            _token = token;//采用copy的方式
            if (tokenModel != null) {
                _uid = token.id;
                String json = JSON.toJSONString(tokenModel);
                UserDefaults.getInstance().putJSONString(USER_TOKEN_KEY, json);
            }
        }

        if (tokenModel != null && !TextUtils.isEmpty(tokenModel.token)) {
            HTTPAccessor.setAuthToken(tokenModel.token);
        }
    }

    public BaseRequest.Token getToken() {
        if (!isLogin()) {
            return null;
        }

        UserBiz.TokenModel token = new UserBiz.TokenModel();
        token.id = _token.id;
        token.mobile = _token.mobile;
        token.token = _token.token;
        token.refreshToken = _token.refreshToken;

        return token;
    }

    private static final String USER_TOKEN_KEY = "_user_token";
    private UserBiz.TokenModel _token;


    public static class User {
        public long uid;
        public String nick;
        public String mobile;
    }
}
