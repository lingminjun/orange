package com.juzistar.m.page.splash;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import com.juzistar.m.R;
import com.juzistar.m.page.PageCenter;
import com.ssn.framework.foundation.App;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.foundation.TaskQueue;
import com.ssn.framework.uikit.Navigator;
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

        //是否需要展示引导图（必须排除应用仅仅进入后台，并没有被杀死过，再次打开界面，此时还是原来界面）
        final boolean isUpgraded = App.isUpgraded() && !App.isStayedBackground();

        //展示三秒后消失
        TaskQueue.mainQueue().executeDelayed(new Runnable() {
            @Override
            public void run() {
                if (isUpgraded) {
                    Navigator.shareInstance().openURL("http://m.juzistar.com/welcome.html",null);
                }
                else {
                    goHome();
                }
            }
        },2000);
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
