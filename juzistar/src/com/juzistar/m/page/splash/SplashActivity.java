package com.juzistar.m.page.splash;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.ssn.framework.uikit.BaseActivity;

/**
 * Created by lingminjun on 15/9/26.
 */
public class SplashActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //展示启动页
        Fragment splash = createFragment(SplashViewController.class,null);
        displayFragment(splash);
    }
}
