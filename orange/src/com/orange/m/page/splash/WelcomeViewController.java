package com.orange.m.page.splash;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.orange.m.R;
import com.orange.m.page.PageCenter;
import com.ssn.framework.foundation.App;
import com.ssn.framework.foundation.TaskQueue;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/9/26.
 */
public class WelcomeViewController extends UIViewController {

    TextView openBtn;

    @Override
    public View loadView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.welcome_layout, null);
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        openBtn = (TextView)findViewById(R.id.open_btn);
        openBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goHome();
            }
        }));
    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

    }

    @Override
    protected boolean onBackEvent() {
        goHome();
        return true;
    }

    private void goHome() {
        PageCenter.goHome();
        finish();
    }
}
