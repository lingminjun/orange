package com.juzistar.m.page;

import android.app.Activity;
import android.os.Bundle;
import com.juzistar.m.biz.UserCenter;
import com.ssn.framework.uikit.BaseTabActivity;
import com.ssn.framework.uikit.Navigator;

import java.util.ArrayList;

/**
 * Created by lingminjun on 15/9/26.
 */
public final class PageCenter {

    public static void goHome() {

        Activity mainActivity = Navigator.shareInstance().getExistActivity(PageURLs.MAIN_URL);
        if (mainActivity != null) {
            Navigator.shareInstance().finishToActivity(mainActivity);
            return;
        } else {
            Bundle args = new Bundle();

            ArrayList<BaseTabActivity.IntentTabItem> list = new ArrayList<BaseTabActivity.IntentTabItem>();
            {
                BaseTabActivity.IntentTabItem tabItem = new BaseTabActivity.IntentTabItem();
                tabItem.url = PageURLs.POP_URL;
                tabItem.viewControllerClass = Navigator.shareInstance().fragmentClassForURL(tabItem.url);
                list.add(tabItem);
            }

            {
                BaseTabActivity.IntentTabItem tabItem = new BaseTabActivity.IntentTabItem();
                tabItem.url = PageURLs.CHAT_LIST_URL;
                tabItem.viewControllerClass = Navigator.shareInstance().fragmentClassForURL(tabItem.url);
                list.add(tabItem);
            }

            {
                BaseTabActivity.IntentTabItem tabItem = new BaseTabActivity.IntentTabItem();
                tabItem.url = PageURLs.ME_URL;
                tabItem.viewControllerClass = Navigator.shareInstance().fragmentClassForURL(tabItem.url);
                list.add(tabItem);
            }

            args.putParcelableArrayList(BaseTabActivity.TAB_FRAGMENT_CLASS_LIST_KEY, list);

            Navigator.shareInstance().openURL(PageURLs.MAIN_URL, args);
        }
    }

    public static interface AuthCallBack {
        public void auth(String account);//
    }

    /**
     * 检查认证，如果没有登录，则触发登录界面
     * @param callBack
     */
    public static void checkAuth(AuthCallBack callBack) {
        _authCallBack = callBack;
        if (UserCenter.shareInstance().isLogin()) {
            if (callBack != null) {
                callBack.auth(null);
            }
            _authCallBack = null;
        }
        else {
            Navigator.shareInstance().openURL(PageURLs.LOGIN_URL,null,true);
        }
    }

    public static void authComplete(String account) {
        if (_authCallBack != null) {
            if (UserCenter.shareInstance().isLogin()) {
                try {
                    _authCallBack.auth(account);
                } catch (Throwable e) {
                }
            }
            _authCallBack = null;
        }
    }
    private static AuthCallBack _authCallBack;
}
