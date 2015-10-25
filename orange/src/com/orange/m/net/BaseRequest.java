package com.orange.m.net;

import com.ssn.framework.foundation.HTTPAccessor;
import com.ssn.framework.foundation.RPC;
import com.ssn.framework.foundation.TR;
import org.apache.http.HttpResponse;

import java.util.HashMap;

/**
 * Created by lingminjun on 15/10/21.
 */
public abstract class BaseRequest<T extends BaseModel> extends RPC.Request<T> implements HTTPAccessor.HTTPRequest {
    private static String _device_token          = "";
    private static String _user_token            = "";

    /**
     * 认证权限
     */
    public static enum AUTH_LEVEL {
        NONE,DEVICE,TOKEN
    }

    @Override
    public T call() throws Exception {

        //请求
        HttpResponse response = HTTPAccessor.access(this);

        //获取返回值

        //数据转换
        return null;
    }

    public abstract String path();
    public abstract void params(HashMap<String, Object> params);

    public AUTH_LEVEL authLevel() {
        return AUTH_LEVEL.NONE;
    }

    @Override
    public String methodURI() {
        return "http://182.92.158.25:8080/orange/" + path();
    }

    @Override
    public HashMap<String, Object> parameter() {
        HashMap<String, Object> ps = new HashMap<>();
        params(ps);

        if (authLevel() == AUTH_LEVEL.DEVICE) {
            ps.put("deviceToken",_device_token);
        }

        if (authLevel() == AUTH_LEVEL.TOKEN) {
            ps.put("token",_user_token);
        }

        //其他公共参数添加

        return ps;
    }

    @Override
    public boolean postToJson() {
        return true;
    }

    @Override
    public boolean forcePost() {
        return false;
    }

    @Override
    public boolean auth() {
        if (authLevel() == AUTH_LEVEL.TOKEN) {
            return true;
        }
        return false;
    }

    /**
     * 设备权限认证
     * @param token
     */
    public static void setDeviceToken(String token) {
        _device_token = TR.string(token);
    }

    /**
     * 用户权限认证
     * @param token
     */
    public static void setUserToken(String token) {
        _user_token = TR.string(token);
        HTTPAccessor.setAuthToken(_user_token);
    }
}
