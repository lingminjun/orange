package com.juzistar.m.page.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.juzistar.m.R;
import com.juzistar.m.Utils.Utils;
import com.juzistar.m.biz.UserBiz;
import com.juzistar.m.constants.Constants;
import com.juzistar.m.page.PageCenter;
import com.juzistar.m.page.base.BaseViewController;
import com.ssn.framework.foundation.App;
import com.ssn.framework.foundation.RPC;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UILoading;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/9/26.
 */
public class ResetPassworViewController extends BaseViewController {

    Button doneBtn;
    EditText pswdEdit;
    EditText confirmPswdEdit;

    String mobile;
    String smsCode;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.string.origin_star));
        navigationItem().setBottomLineHidden(true);
        navigationItem().backItem().setImage(R.drawable.nav_left_white_icon);

        //获取参数
        Bundle bundle = getArguments();
        mobile = bundle.getString(Constants.PAGE_ARG_MOBILE);
        smsCode = bundle.getString(Constants.PAGE_ARG_SMS_CODE);
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.reset_password_layout, null);
        doneBtn = (Button)view.findViewById(R.id.done_btn);
        pswdEdit = (EditText)view.findViewById(R.id.psw_edt);
        confirmPswdEdit = (EditText)view.findViewById(R.id.confirm_psw_edt);
        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        setBackgroundDrawable(R.drawable.page_background);

        doneBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String password = Utils.getInputString(pswdEdit);
                String confirm = Utils.getInputString(confirmPswdEdit);

                if (TextUtils.isEmpty(password) || password.length() < 3 || password.length() > 18) {
                    App.toast(Res.localized(R.string.please_input_password));
                    return;
                }

                if (!password.equals(confirm)) {
                    App.toast(Res.localized(R.string.confirm_password_error));
                    return;
                }

                UserBiz.resetPassword(mobile, smsCode, password, new RPC.Response<UserBiz.TokenModel>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                        UILoading.show(getActivity());
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                        UILoading.dismiss(getActivity());
                    }

                    @Override
                    public void onSuccess(UserBiz.TokenModel tokenModel) {
                        super.onSuccess(tokenModel);

                        //直接登录
                        App.toast(Res.localized(R.string.login_success));

                        PageCenter.goHome();

                        PageCenter.authComplete(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        super.onFailure(e);
                        Utils.toastException(e,Res.localized(R.string.reset_failed));
                    }
                });
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
}
