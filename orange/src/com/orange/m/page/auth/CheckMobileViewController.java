package com.orange.m.page.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.orange.m.R;
import com.orange.m.Utils.Utils;
import com.orange.m.constants.Constants;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.Navigator;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UIViewController;

import java.util.HashMap;

/**
 * Created by lingminjun on 15/9/26.
 */
public class CheckMobileViewController extends UIViewController {

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
        navigationItem().setTitleColor(Res.color(R.color.white));

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

                Bundle bundle = new Bundle();
                bundle.putString(Constants.PAGE_ARG_MOBILE, mobile);
                bundle.putString(Constants.PAGE_ARG_SMS_CODE, smsCode);
                Navigator.shareInstance().openURL(nextURL,bundle);
            }
        }));
    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

    }
}
