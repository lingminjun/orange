package com.juzistar.m.net;

import android.text.TextUtils;
import com.juzistar.m.R;
import com.juzistar.m.Utils.Utils;
import com.ssn.framework.foundation.Res;
import org.apache.http.conn.ConnectTimeoutException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Created by lingminjun on 15/11/8.
 */
public final class APIErrorMessage {

    /**
     * 从异常中对应错误文案
     * @param e
     * @return
     */
    public static String message(Exception exe) {

        if (exe instanceof BaseRequest.APIException) {
            String msg = exe.getMessage();
            if (Utils.isContainChinese(msg)) {
                return msg;
            }
            return apiMessage(((BaseRequest.APIException) exe).code);
        } else if (exe instanceof ConnectTimeoutException) {
            return Res.localized(R.string.request_time_out);
        } else if (exe instanceof ConnectException || exe instanceof SocketException
                || exe instanceof SocketTimeoutException || exe instanceof UnknownHostException) {
            return Res.localized(R.string.network_error_message);
        } else if (exe instanceof IOException) {
            return Res.localized(R.string.request_data_error);
        }

        return null;
    }


    private static String apiMessage(int code) {

        switch (code) {
            case 999:
            case -1:return Res.localized(R.string.unknown_exception);
            case 100:return Res.localized(R.string.no_user_token);
            case 101:return Res.localized(R.string.user_token_invalid);
            case 102:return Res.localized(R.string.user_token_invalid);
            case 105:return Res.localized(R.string.account_or_password_error);
            case 200:return Res.localized(R.string.register_info_lose);
            case 103:return Res.localized(R.string.user_token_expired);
            default:return Res.localized(R.string.unknown_exception);
        }

/*
        NO_SOLR_COLLECTION(1, "no solr collection found"), SOLR_EXCEPTION(2, "solr process exception"),
		NEED_AUTHORIZION(100, "需要token验证"),
		INVALID_TOKEN(101, "无效的user token"), INVALID_REFRESH_TOKEN(102, "无效的refresh token"),

		USER_INFO_LACK(200, "用户注册信息不完整"), SMS_CODE_EXCEPTION(103, "验证码发送异常"), INVALID_SMS_CODE(104, "验证码验证失败"),

		EXPIRED_TOKEN(103, "过期token"), STARTUP_EXCEPTION(999, "startup exception"),
		UNKNOW(-1, "unmapped exception");
        */
    }

}
