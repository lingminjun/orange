package com.juzistar.m.page.chat;

import android.os.Bundle;
import com.juzistar.m.R;
import com.juzistar.m.page.base.BaseTableViewController;
import com.ssn.framework.foundation.Res;

/**
 * Created by lingminjun on 15/11/28.
 */
public class ChatListViewController extends BaseTableViewController {

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.string.encounters));
        tabItem().setTabName(Res.localized(R.string.encounters));
        tabItem().setTabImage(R.drawable.tab_selector_chat);
    }

}
