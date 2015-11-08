package com.orange.m.page.auth;

import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.orange.m.R;
import com.orange.m.Utils.Utils;
import com.orange.m.biz.UserBiz;
import com.orange.m.constants.Constants;
import com.orange.m.net.APIErrorMessage;
import com.orange.m.page.PageCenter;
import com.orange.m.page.base.BaseViewController;
import com.ssn.framework.foundation.App;
import com.ssn.framework.foundation.RPC;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.Navigator;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/9/26.
 */
public class LoginViewController extends BaseViewController {

    Button loginBtn;
    Button registerBtn;
    TextView forgetPswdBtn;

    EditText accountEdit;
    EditText passwordEdit;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.string.origin_star));
        navigationItem().setBottomLineHidden(true);
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.login_layout, null);
        loginBtn = (Button)view.findViewById(R.id.login_btn);
        registerBtn = (Button)view.findViewById(R.id.register_btn);
        forgetPswdBtn = (TextView)view.findViewById(R.id.forget_psw_tv);

        accountEdit = (EditText)view.findViewById(R.id.user_name_edt);
        passwordEdit = (EditText)view.findViewById(R.id.psw_edt);
        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        setBackgroundDrawable(R.drawable.page_background);

        loginBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String account = Utils.getInputString(accountEdit);
                String password = Utils.getInputString(passwordEdit);

                RPC.Response<UserBiz.TokenModel> response = new RPC.Response<UserBiz.TokenModel>() {
                    @Override
                    public void onStart() {
                        super.onStart();
                    }

                    @Override
                    public void onSuccess(UserBiz.TokenModel tokenModel) {
                        super.onSuccess(tokenModel);

                        //进入主页
                        finish();

                        App.toast(Res.localized(R.string.login_success));
                    }

                    @Override
                    public void onFailure(Exception e) {
                        super.onFailure(e);
                        Utils.toastException(e,Res.localized(R.string.login_failed));
                    }

                    @Override
                    public void onFinish() {
                        super.onFinish();
                    }
                };
                UserBiz.login(account,password,response);
            }
        }));

        registerBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.PAGE_ARG_NEXT_URL,"http://m.orangestar.com/register.html");
                Navigator.shareInstance().openURL("http://m.orangestar.com/checkmobile.html",bundle);
            }
        }));

        forgetPswdBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = new Bundle();
                bundle.putString(Constants.PAGE_ARG_NEXT_URL,"http://m.orangestar.com/resetpassword.html");
                Navigator.shareInstance().openURL("http://m.orangestar.com/checkmobile.html",bundle);
            }
        }));
    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

    }
}
