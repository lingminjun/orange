package com.orange.m.page.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.orange.m.R;
import com.orange.m.Utils.Utils;
import com.orange.m.biz.UserBiz;
import com.orange.m.constants.Constants;
import com.orange.m.page.PageCenter;
import com.ssn.framework.foundation.RPC;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.Navigator;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/9/26.
 */
public class RegisterViewController extends UIViewController {

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
        navigationItem().setTitleColor(Res.color(R.color.white));

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

                UserBiz.register(mobile,smsCode,password,nick, new RPC.Response<UserBiz.TokenModel>() {
                    @Override
                    public void onSuccess(UserBiz.TokenModel tokenModel) {
                        super.onSuccess(tokenModel);

                        PageCenter.goHome();
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
