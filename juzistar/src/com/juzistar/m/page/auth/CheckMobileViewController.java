package com.juzistar.m.page.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.juzistar.m.R;
import com.juzistar.m.Utils.Utils;
import com.juzistar.m.biz.UserBiz;
import com.juzistar.m.constants.Constants;
import com.juzistar.m.page.PageCenter;
import com.juzistar.m.page.base.BaseViewController;
import com.ssn.framework.foundation.App;
import com.ssn.framework.foundation.Clock;
import com.ssn.framework.foundation.RPC;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.Navigator;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UIViewController;

import java.util.HashMap;

/**
 * Created by lingminjun on 15/9/26.
 */
public class CheckMobileViewController extends BaseViewController {

    Button nexBtn;
    EditText mobileEdit;
    EditText smsCodeEdit;
    TextView sendSMSCode;

    String nextURL;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.string.origin_star));
        navigationItem().setBottomLineHidden(true);

        nextURL = args.getString(Constants.PAGE_ARG_NEXT_URL);
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.check_mobile_layout, null);
        nexBtn = (Button)view.findViewById(R.id.next_btn);
        sendSMSCode = (TextView)view.findViewById(R.id.send_sms_code_tv);
        mobileEdit = (EditText)view.findViewById(R.id.mobile_edt);
        smsCodeEdit = (EditText)view.findViewById(R.id.code_edt);
        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        setBackgroundDrawable(R.drawable.page_background);

        nexBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String mobile = Utils.getInputString(mobileEdit);
                String smsCode = Utils.getInputString(smsCodeEdit);

                if (TextUtils.isEmpty(mobile)) {
                    App.toast(Res.localized(R.string.please_input_mobile));
                    return;
                }

                if (TextUtils.isEmpty(smsCode)) {
                    App.toast(Res.localized(R.string.please_input_sms_code));
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putString(Constants.PAGE_ARG_MOBILE, mobile);
                bundle.putString(Constants.PAGE_ARG_SMS_CODE, smsCode);
                Navigator.shareInstance().openURL(nextURL,bundle);
            }
        }));

        sendSMSCode.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mobile = Utils.getInputString(mobileEdit);
                if (TextUtils.isEmpty(mobile)) {
                    App.toast(Res.localized(R.string.please_input_mobile));
                    return;
                }

                sendSMSCode(mobile);
            }
        }));
    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

    }

    @Override
    public void onDestroyController() {
        super.onDestroyController();
    }

    private void sendSMSCode(String mobile) {
        startTiming();

        UserBiz.requestSMSCode(mobile, UserBiz.SMS_CODE_TYPE_FORGET, new RPC.Response<UserBiz.SMSCodeModel>() {
            @Override
            public void onSuccess(UserBiz.SMSCodeModel model) {
                super.onSuccess(model);
            }

            @Override
            public void onFailure(Exception e) {
                super.onFailure(e);
                stopTiming();
            }
        });
    }

    private static final int CLOCK_MAX_COUNT = 60;
    private int count;
    private Clock.Listener timing = new Clock.Listener() {
        @Override
        public void fire(String flag) {
            if (count >= CLOCK_MAX_COUNT) {//可以继续点击
                stopTiming();
            } else {
                sendSMSCode.setEnabled(false);
                sendSMSCode.setText(Res.localized(R.string.x_second_can_send,CLOCK_MAX_COUNT - count));
                count++;
            }
        }
    };

    private void startTiming() {
        count = 0;
        Clock.shareInstance().addListener(timing,CLOCK_KEY);
    }

    private void stopTiming() {
        Clock.shareInstance().removeListener(CLOCK_KEY);
        sendSMSCode.setEnabled(true);
        count = 0;
        sendSMSCode.setText(Res.localized(R.string.send_sms_code));
    }

    private static final String CLOCK_KEY = "check_mobile";
}
