package com.ssn.framework.uikit;

import com.ssn.framework.foundation.inc.AApplication;

/**
 * Created by lingminjun on 15/9/13.
 */
public class UIApplication extends AApplication {

    /**
     * 应用启动回调函数
     */
    protected void applicationDidLaunch() {}

    @Override
    public final void onCreate() {
        super.onCreate();//保证框架组件初始化

        this.applicationDidLaunch();
    }
}
