package com.juzistar.m.biz;

import com.juzistar.m.net.BaseModel;
import com.juzistar.m.net.BaseRequest;
import com.juzistar.m.net.BoolModel;
import com.ssn.framework.foundation.HTTPAccessor;
import com.ssn.framework.foundation.RPC;

import java.util.HashMap;

/**
 * Created by lingminjun on 15/10/25.
 */
public final class UserBiz {

    /*
    {"id":3,
    "refreshToken":"MTM3NjEwNjczODZ8NjkxMGRkMTJhOTg4OTQ2N2M1MTNmNjI4MDkxZDM1NTA=",
    "token":"M3wxMzc2MTA2NzM4NnwxNDQ1OTE4MDYyMTI0",
    "nickname":"?? ???nickname???",
    "mobile":"13761067386"}
    */
    public static class TokenModel extends BaseRequest.Token {
        public long id;
        public String mobile;
        public String nickname;

        @Override
        public boolean fillFromOther(Object obj) {
            if (obj != null && obj instanceof BaseRequest.Token) {//仅仅修改这两个值
                this.refreshToken = ((BaseRequest.Token) obj).refreshToken;
                this.token = ((BaseRequest.Token) obj).token;
                return true;
            } else {
                return super.fillFromOther(obj);
            }
        }
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
                return "user";
            }

            @Override
            public HTTPAccessor.REST_METHOD method() {
                return HTTPAccessor.REST_METHOD.POST;
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("mobile",mobile);
                params.put("smsAuthCode",smsCode);
                params.put("password",password);
                params.put("nickname",nick);
            }

            @Override
            public TokenModel call(RPC.Retry retry) throws Exception {
                TokenModel token = super.call(retry);
                UserCenter.shareInstance().saveToken(token);
                return token;
            }
        };

        return RPC.call(request,response);
    }

    public static RPC.Cancelable login(final String mobile, final String password, final RPC.Response<TokenModel> response){

        BaseRequest<TokenModel> request = new BaseRequest<TokenModel>() {
            @Override
            public String path() {
                return "user";
            }

            @Override
            public HTTPAccessor.REST_METHOD method() {
                return HTTPAccessor.REST_METHOD.GET;
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("mobile",mobile);
                params.put("password",password);
            }

            @Override
            public TokenModel call(RPC.Retry retry) throws Exception {
                TokenModel token = super.call(retry);
                UserCenter.shareInstance().saveToken(token);
                return token;
            }
        };

        return RPC.call(request,response);
    }

    public static RPC.Cancelable resetPassword(final String mobile, final String smsCode, final String password, final RPC.Response<TokenModel> response){

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
            public HTTPAccessor.REST_METHOD method() {
                return HTTPAccessor.REST_METHOD.PUT;
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("mobile",mobile);
                params.put("smsAuthCode",smsCode);
                params.put("password",password);
            }
        };

        return RPC.call(request,response);
    }

    /**
     * 修改nick
     * @param nick
     * @param response
     * @return
     */
    public static RPC.Cancelable updateUser(final String nick, final RPC.Response<TokenModel> response){

        BaseRequest<TokenModel> request = new BaseRequest<TokenModel>() {
            @Override
            public String path() {
                return "user";
            }

            @Override
            public AUTH_LEVEL authLevel() {
                return AUTH_LEVEL.TOKEN;
            }

            @Override
            public HTTPAccessor.REST_METHOD method() {
                return HTTPAccessor.REST_METHOD.PUT;
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("nickname",nick);
            }

            @Override
            public TokenModel call(RPC.Retry retry) throws Exception {
                TokenModel token = super.call(retry);
                UserCenter.shareInstance().modifyNick(token.nickname);
                return token;
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
    public static final String SMS_CODE_TYPE_FORGET = "forget_pw";
    public static final String SMS_CODE_TYPE_REGISTER = "register";
    public static RPC.Cancelable requestSMSCode(final String mobile, final String type, final RPC.Response<SMSCodeModel> response){

        BaseRequest<SMSCodeModel> request = new BaseRequest<SMSCodeModel>() {
            @Override
            public String path() {
                return "smsAuthCode";
            }

            @Override
            public HTTPAccessor.REST_METHOD method() {
                return HTTPAccessor.REST_METHOD.GET;
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("mobile",mobile);
                params.put("type",type);
            }
        };

        return RPC.call(request,response);
    }

    public static RPC.Cancelable feedback(final String content, final RPC.Response<BoolModel> response){

        BaseRequest<BoolModel> request = new BaseRequest<BoolModel>() {
            @Override
            public String path() {
                return "user/feedback";
            }

            @Override
            public AUTH_LEVEL authLevel() {
                return AUTH_LEVEL.TOKEN;
            }

            @Override
            public HTTPAccessor.REST_METHOD method() {
                return HTTPAccessor.REST_METHOD.POST;
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("text",content);
            }
        };

        return RPC.call(request,response);
    }


    public static RPC.Cancelable mobileExist(final String mobile, final RPC.Response<BoolModel> response){

        BaseRequest<BoolModel> request = new BaseRequest<BoolModel>() {
            @Override
            public String path() {
                return "user/mobileExist";
            }

            @Override
            public HTTPAccessor.REST_METHOD method() {
                return HTTPAccessor.REST_METHOD.GET;
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("mobile",mobile);
            }
        };

        return RPC.call(request,response);
    }

    /**
     * 链式请求实例，请求注册验证码
     * @param mobile
     * @param response
     * @return
     */
    public static RPC.Cancelable sendRegisterSMSCode(final String mobile, final String type,  final RPC.Response<BoolModel> response){

        BaseRequest<BoolModel> request = new BaseRequest<BoolModel>() {
            @Override
            public String path() {
                return "user/mobileExist";
            }

            @Override
            public HTTPAccessor.REST_METHOD method() {
                return HTTPAccessor.REST_METHOD.GET;
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("mobile",mobile);
            }
        };

        request.nextRequest = new BaseRequest<SMSCodeModel>() {
            @Override
            public String path() {
                return "smsAuthCode";
            }

            @Override
            public HTTPAccessor.REST_METHOD method() {
                return HTTPAccessor.REST_METHOD.GET;
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
