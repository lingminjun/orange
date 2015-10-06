package com.ssn.framework.foundation.inc;

import android.app.Application;
import com.ssn.framework.foundation.App;
import com.ssn.framework.foundation.BroadcastCenter;
import com.ssn.framework.foundation.Res;

/**
 * Created by lingminjun on 15/9/13.
 */
public class AApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //提前初始化res
        Res.initialization(this);

        //启动通知中心
        BroadcastCenter.shareInstance().applicationDidLaunch(this);

        //仅仅防止app状态判断没有被设置
        App.isFirstInstall();
    }
}
