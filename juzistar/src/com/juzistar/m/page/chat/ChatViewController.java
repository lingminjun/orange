package com.juzistar.m.page.chat;

import android.os.Bundle;
import com.juzistar.m.R;
import com.juzistar.m.page.base.BaseViewController;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/9/26.
 */
public class ChatViewController extends BaseViewController {

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.string.encounters));
        tabItem().setTabName(Res.localized(R.string.encounters));
        tabItem().setTabImage(R.drawable.tab_selector_chat);
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();


        tabItem().setBadgeValue(".");
    }
}
