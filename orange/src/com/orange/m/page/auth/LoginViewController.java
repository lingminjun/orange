package com.orange.m.page.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.orange.m.R;
import com.orange.m.page.PageCenter;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/9/26.
 */
public class LoginViewController extends UIViewController {

    Button loginBtn;
    Button registerBtn;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.id.login));
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.login_layout, null);
        loginBtn = (Button)view.findViewById(R.id.login_btn);
        registerBtn = (Button)view.findViewById(R.id.register_btn);
        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        loginBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        }));
    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

    }
}
