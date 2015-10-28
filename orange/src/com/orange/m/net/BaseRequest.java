package com.orange.m.net;

import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.orange.m.R;
import com.ssn.framework.foundation.*;
import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;


/**
 * Created by lingminjun on 15/10/21.
 */
public abstract class BaseRequest<T extends BaseModel> extends RPC.Request<T> implements HTTPAccessor.HTTPRequest {
    private static String _device_token          = "";
    private static String _user_token            = "";

    /**
     * 异常处理
     */
    public static class APIException extends RuntimeException {
        public int code;
        public String domain;
        public String name;

        public APIException() {
            super();
        }

        public APIException(String detailMessage) {
            super(detailMessage);
        }
    }

    private static class HTTPError extends BaseModel {
        private int errorCode;
        private String name;
        private String message;

        private APIException exception() {
            APIException exception = new APIException(message == null ? name : message);
            exception.code = errorCode;
            exception.name = name;
            exception.domain = "API";
            return exception;
        }
    }

    private Class<T> _modelClass;

    public BaseRequest() {
        _modelClass =(Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
    }

    private T getModel() {
        try {
            return _modelClass.newInstance();
        } catch (Exception e) {
            Log.e("BaseRequest",_modelClass.getName() + "必须包含一个不带参数的构造函数");
        }
        return null;
    }

    /**
     * 认证权限
     */
    public static enum AUTH_LEVEL {
        NONE,DEVICE,TOKEN
    }


    @Override
    public T call() throws Exception {


//        System.out.println("\ntestGetByJSON\n-----------------------------");

        /*
        ServiceResponse response = null;
        RequestParams params = getRequestParams();

        //请求
        if (restMethod() == HTTPAccessor.REST_METHOD.GET) {
            response = Resting.get(methodURI(), 8080, params);
        } else if (restMethod() == HTTPAccessor.REST_METHOD.POST) {
            response = Resting.post(methodURI(), 8080, params);
        } else if (restMethod() == HTTPAccessor.REST_METHOD.PUT) {
            response = Resting.put(methodURI(), 8080, params);
        } else if (restMethod() == HTTPAccessor.REST_METHOD.DELETE) {
            response = Resting.delete(methodURI(), 8080, params);
        }
        //解析数据
        if (response.getStatusCode() == HttpStatus.SC_OK) {
            String content = response.getResponseString();
            Log.e("res:",content);
        }
        */

        HTTPAccessor.ServerResponse response = HTTPAccessor.access(this);

        //获取返回值，开始解析
        T object = null;
        if (response.getStatusCode() == HttpStatus.SC_OK) {

            JSONObject json = new JSONObject(response.getResponseString());

            //转换成对象
            object = getModel();

            boolean fill = false;
            if (object != null) {//先看是否能自己fill，不行再采用fastjson
                fill = object.fillFromJSON(json);
            }

            if (!fill) {
                //先用fastjson处理
                object = (T) com.alibaba.fastjson.JSON.parseObject(response.getResponseString(), object.getClass());
            }
        } else {
            HTTPError error = (HTTPError)com.alibaba.fastjson.JSON.parseObject(response.getResponseString(), HTTPError.class);
            if (error != null) {
                throw error.exception();
            } else {
                error = new HTTPError();
                error.errorCode = -1;
                error.message = Res.localized(R.string.unknown_exception);
                error.name = "UNKNOWN_EXCEPTION";
                throw error.exception();
            }
        }

        //数据转换
        return object;
    }

    public static <T> T createInstance(Class<T> cls) {
        T obj=null;
        try {
            obj=cls.newInstance();
        } catch (Exception e) {
            obj=null;
        }
        return obj;
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

        //添加其他公共参数添加
//
//        if (authLevel() == AUTH_LEVEL.DEVICE) {
//            ps.put("deviceToken",_device_token);
//        }
//
//        if (authLevel() == AUTH_LEVEL.TOKEN) {
//            ps.put("token",_user_token);
//        }

        return ps;
    }


    @Override
    public abstract HTTPAccessor.REST_METHOD method();

    @Override
    public HTTPAccessor.PARAM_ENCODE_TYPE encode() {
        //更具服务器需要配置
        return HTTPAccessor.PARAM_ENCODE_TYPE.JSON;
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

//    private RequestParams getRequestParams() {
//        HashMap<String, String> ps = new HashMap<>();
//        params(ps);
//
//        RequestParams params = new BasicRequestParams();
//        Set<String> keys = ps.keySet();
//        for (String key : keys) {
//            String value = ps.get(key);
//            params.add(key,value);
//        }
//
//        return params;
//    }
}
