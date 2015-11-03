package com.orange.m.page.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.orange.m.R;
import com.orange.m.Utils.Utils;
import com.orange.m.biz.UserBiz;
import com.orange.m.constants.Constants;
import com.orange.m.page.PageCenter;
import com.orange.m.page.base.BaseViewController;
import com.ssn.framework.foundation.App;
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

    private void sendSMSCode(String mobile) {
        UserBiz.requestSMSCode(mobile, UserBiz.SMS_CODE_TYPE_FORGET, new RPC.Response<UserBiz.SMSCodeModel>() {
            @Override
            public void onSuccess(UserBiz.SMSCodeModel model) {
                super.onSuccess(model);
            }
        });
    }
}
