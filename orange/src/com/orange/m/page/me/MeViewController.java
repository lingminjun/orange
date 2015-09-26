package com.orange.m.page.me;

import android.os.Bundle;
import com.orange.m.R;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/9/26.
 */
public class MeViewController extends UIViewController {

    private int count = 0;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle("我");
        tabItem().setTabName("我");
        tabItem().setTabImage(R.drawable.tab_selector_me);
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();


    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

        tabItem().setBadgeValue(Integer.toString(count++));
    }
}
