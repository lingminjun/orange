package com.orange.m.page.chat;

import android.os.Bundle;
import com.orange.m.R;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/9/26.
 */
public class ChatViewController extends UIViewController {

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle("邂逅");
        tabItem().setTabName("邂逅");
        tabItem().setTabImage(R.drawable.tab_selector_chat);
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();


        tabItem().setBadgeValue(".");
    }
}
