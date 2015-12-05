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
import com.juzistar.m.page.base.BaseViewController;
import com.ssn.framework.foundation.App;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIEvent;

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
            if (mContentText == null || mContentText.length() <= 3) {
                App.toast(Res.localized(R.string.comment_too_short));
                return;
            }

            String string = Utils.getEditText(mContentText);

//            HtAsyncWorkViewCB<Boolean> cb = new HtAsyncWorkViewCB<Boolean>() {
//                @Override
//                public void onStart() {
//                    Progress.show(FeedbackFragment.this, true);
//                }
//
//                @Override
//                public void onFinish() {
//                    Progress.dismiss(FeedbackFragment.this);
//                }
//
//                @Override
//                public void onSuccess(Boolean success) {
//                    Utils.toast(getActivity(), Res.localized(R.string.feedback_status_submited));
//                    FeedbackFragment.this.finish();
//                }
//
//                @Override
//                public void onFailure(Exception e) {
//                    Utils.toastException(getActivity(), e, Res.localized(R.string.feedback_status_failed));
//                }
//            };
//
//            AccountBiz.asyncFeedback(cb, mContentText.getText().toString(), mContactText.getText().toString());
        }
    };

}
