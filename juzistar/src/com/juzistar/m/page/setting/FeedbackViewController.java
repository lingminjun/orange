package com.juzistar.m.page.setting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.juzistar.m.R;
import com.juzistar.m.Utils.Utils;
import com.juzistar.m.biz.UserBiz;
import com.juzistar.m.net.BoolModel;
import com.juzistar.m.page.PageCenter;
import com.juzistar.m.page.base.BaseViewController;
import com.ssn.framework.foundation.App;
import com.ssn.framework.foundation.RPC;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UILoading;

/**
 * Created by BlackDev on 6/15/15.
 */
public class FeedbackViewController extends BaseViewController {

    private EditText mContentText;
    private TextView mWordLimitText;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.string.setting_advice_title));
        navigationItem().setTitleColor(Res.color(R.color.ssn_normal_text));
        navigationItem().setBottomLineHidden(false);

        navigationItem().rightItem().setTitle(Res.localized(R.string.submit));
        navigationItem().rightItem().setTitleColor(Res.colorState(R.color.green_to_grey));
        navigationItem().rightItem().setOnClick(UIEvent.click(rightClick));
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.feedback_layout, null);
        mContentText = (EditText)view.findViewById(R.id.feedback_content);
        mContentText.addTextChangedListener(UIEvent.watcher(watcher));
        mWordLimitText = (TextView)view.findViewById(com.ssn.framework.R.id.ssn_word_limit_label);
        mWordLimitText.setText(String.format("%d/%d", 0, 150));
        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();
    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable != null) {
                String content = String.format("%d/%d", editable.length(), 150);
                mWordLimitText.setText(content);
            }
        }
    };

    View.OnClickListener rightClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            final String string = Utils.getEditText(mContentText);

            if (string == null || string.length() <= 3) {
                App.toast(Res.localized(R.string.comment_too_short));
                return;
            }

            //需要登录权限
            PageCenter.checkAuth(new PageCenter.AuthCallBack() {
                @Override
                public void auth(String account) {
                    submit(string);
                }
            });

        }
    };


    private void submit(String string) {
        RPC.Response<BoolModel> cb = new RPC.Response<BoolModel>() {
            @Override
            public void onStart() {
                super.onStart();
                UILoading.show(getActivity());
            }

            @Override
            public void onSuccess(BoolModel o) {
                super.onSuccess(o);
                App.toast(Res.localized(R.string.feedback_success));
                finish();
            }

            @Override
            public void onFailure(Exception e) {
                super.onFailure(e);
                Utils.toastException(e,Res.localized(R.string.feedback_error));
            }

            @Override
            public void onFinish() {
                super.onFinish();
                UILoading.dismiss(getActivity());
            }
        };

        UserBiz.feedback(string,cb);
    }
}
