package com.juzistar.m.page.auth;

import android.os.Bundle;
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
import com.ssn.framework.uikit.Navigator;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UILoading;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/9/26.
 */
public class RegisterViewController extends BaseViewController {

    Button doneBtn;
    EditText nickEdit;
    EditText pswdEdit;

    String mobile;
    String smsCode;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.string.origin_star));
        navigationItem().setBottomLineHidden(true);

        //获取参数
        Bundle bundle = getArguments();
        mobile = bundle.getString(Constants.PAGE_ARG_MOBILE);
        smsCode = bundle.getString(Constants.PAGE_ARG_SMS_CODE);
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.register_layout, null);
        doneBtn = (Button)view.findViewById(R.id.done_btn);
        nickEdit = (EditText)view.findViewById(R.id.user_name_edt);
        pswdEdit = (EditText)view.findViewById(R.id.psw_edt);
        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        setBackgroundDrawable(R.drawable.page_background);

        doneBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String nick = Utils.getInputString(nickEdit);
                String password = Utils.getInputString(pswdEdit);

                String sms = "3721";

                UserBiz.register(mobile,sms,password,nick, new RPC.Response<UserBiz.TokenModel>() {
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
                        Utils.toastException(e,Res.localized(R.string.register_failed));
                    }
                });

            }
        }));

    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

    }
}
