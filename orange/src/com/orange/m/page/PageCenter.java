package com.orange.m.page;

import android.os.Bundle;
import com.ssn.framework.uikit.BaseTabActivity;
import com.ssn.framework.uikit.Navigator;

import java.util.ArrayList;

/**
 * Created by lingminjun on 15/9/26.
 */
public final class PageCenter {

    public static void goHome() {
        Bundle args = new Bundle();

        ArrayList<BaseTabActivity.IntentTabItem> list = new ArrayList<BaseTabActivity.IntentTabItem>();
        {
            BaseTabActivity.IntentTabItem tabItem = new BaseTabActivity.IntentTabItem();
            tabItem.url = "http://m.orangestar.com/pop.html";
            tabItem.viewControllerClass = Navigator.shareInstance().fragmentClassForURL(tabItem.url);
            list.add(tabItem);
        }

        {
            BaseTabActivity.IntentTabItem tabItem = new BaseTabActivity.IntentTabItem();
            tabItem.url = "http://m.orangestar.com/chat.html";
            tabItem.viewControllerClass = Navigator.shareInstance().fragmentClassForURL(tabItem.url);
            list.add(tabItem);
        }

        {
            BaseTabActivity.IntentTabItem tabItem = new BaseTabActivity.IntentTabItem();
            tabItem.url = "http://m.orangestar.com/me.html";
            tabItem.viewControllerClass = Navigator.shareInstance().fragmentClassForURL(tabItem.url);
            list.add(tabItem);
        }

        args.putParcelableArrayList(BaseTabActivity.TAB_FRAGMENT_CLASS_LIST_KEY, list);

        Navigator.shareInstance().openURL("http://m.orangestar.com/main.html",args);
    }
}
