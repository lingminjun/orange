package com.orange.m.page.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.orange.m.R;
import com.orange.m.constants.Constants;
import com.orange.m.page.PageCenter;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.Navigator;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/9/26.
 */
public class LoginViewController extends UIViewController {

    Button loginBtn;
    Button registerBtn;
    TextView forgetPswdBtn;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.string.origin_star));
        navigationItem().setBottomLineHidden(true);
        navigationItem().setTitleColor(Res.color(R.color.white));
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.login_layout, null);
        loginBtn = (Button)view.findViewById(R.id.login_btn);
        registerBtn = (Button)view.findViewById(R.id.register_btn);
        forgetPswdBtn = (TextView)view.findViewById(R.id.forget_psw_tv);
        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        setBackgroundDrawable(R.drawable.page_background);

        loginBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                Navigator.shareInstance().openURL("http://m.orangestar.com/checkmobile.html");
            }
        }));
    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

    }
}
