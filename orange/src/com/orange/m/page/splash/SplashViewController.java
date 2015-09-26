package com.orange.m.page.splash;

import android.view.LayoutInflater;
import android.view.View;
import com.orange.m.R;
import com.orange.m.page.PageCenter;
import com.ssn.framework.foundation.TaskQueue;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/9/26.
 */
public class SplashViewController extends UIViewController {

    @Override
    public View loadView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.splash_layout, null);
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        //展示三秒后消失
        TaskQueue.mainQueue().executeDelayed(new Runnable() {
            @Override
            public void run() {
                PageCenter.goHome();
                finish();
            }
        },3000);
    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();


    }
}
