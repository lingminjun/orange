package com.orange.m.biz;

import com.orange.m.net.BaseModel;
import com.orange.m.net.BaseRequest;
import com.ssn.framework.foundation.RPC;

import java.util.HashMap;

/**
 * Created by lingminjun on 15/10/25.
 */
public final class UserBiz {

    public static class TokenModel extends BaseModel {

    }

    public static class SMSCodeModel extends BaseModel {

    }

    /*
    private Long id;
	private String mobile;
	private String nickname;
	private String password;
	private String smsAuthCode;
    */

    /**
     * 注册调用
     * @param mobile
     * @param smsCode
     * @param password
     * @param nick
     * @param response
     * @return
     */
    public static RPC.Cancelable register(final String mobile, final String smsCode, final String password, final String nick, final RPC.Response<TokenModel> response){

        BaseRequest<TokenModel> request = new BaseRequest<TokenModel>() {
            @Override
            public String path() {
                return "register";
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("mobile",mobile);
                params.put("smsAuthCode",smsCode);
                params.put("password",password);
                params.put("nickname",nick);
            }
        };

        return RPC.call(request,response);
    }

    public static RPC.Cancelable login(final String mobile, final String password, final RPC.Response<TokenModel> response){

        BaseRequest<TokenModel> request = new BaseRequest<TokenModel>() {
            @Override
            public String path() {
                return "login";
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("mobile",mobile);
                params.put("password",password);
            }
        };

        return RPC.call(request,response);
    }

    public static RPC.Cancelable refreshToken(final String mobile, final String token, final String refreshToken, final RPC.Response<TokenModel> response){

        BaseRequest<TokenModel> request = new BaseRequest<TokenModel>() {
            @Override
            public String path() {
                return "refreshToken";
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("mobile",mobile);
                params.put("token",token);
                params.put("refreshToken",refreshToken);
            }

            @Override
            public AUTH_LEVEL authLevel() {
                return AUTH_LEVEL.TOKEN;
            }
        };

        return RPC.call(request,response);
    }

    public static RPC.Cancelable updateUser(final String nick, final RPC.Response<TokenModel> response){

        BaseRequest<TokenModel> request = new BaseRequest<TokenModel>() {
            @Override
            public String path() {
                return "updateUser";
            }

            @Override
            public AUTH_LEVEL authLevel() {
                return AUTH_LEVEL.TOKEN;
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("nickname",nick);
            }
        };

        return RPC.call(request,response);
    }

    /**
     *
     * @param mobile
     * @param type 取值范围 “register”
     * @param response
     * @return
     */
    public static RPC.Cancelable requestSMSCode(final String mobile, final String type, final RPC.Response<SMSCodeModel> response){

        BaseRequest<SMSCodeModel> request = new BaseRequest<SMSCodeModel>() {
            @Override
            public String path() {
                return "smsAuthCode";
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("mobile",mobile);
                params.put("type",type);
            }
        };

        return RPC.call(request,response);
    }
}
