package com.juzistar.m.page.main;


import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewTreeObserver;
import android.widget.Toast;
import com.juzistar.m.R;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.biz.msg.MessageCenter;
import com.ssn.framework.foundation.App;
import com.ssn.framework.foundation.BroadcastCenter;
import com.ssn.framework.uikit.BaseTabActivity;
import com.ssn.framework.uikit.Navigator;
import com.ssn.framework.uikit.UITabBar;

/**
 * Created by lingminjun on 15/9/26.
 */
public class MainTabActivity extends BaseTabActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setBackgroundDrawable(R.drawable.page_background_bitmap);

        tabBar().setBackgroundResource(R.color.black_0_5_alpha);
        tabBar().setSeparateLineHide(true);


        if (UserCenter.shareInstance().isLogin()) {
            MessageCenter.shareInstance().start();//开启
        }

        BroadcastCenter.shareInstance().addObserver(this, UserCenter.USER_LOGIN_NOTIFICATION, observerMethod);
        BroadcastCenter.shareInstance().addObserver(this, UserCenter.USER_LOGOUT_NOTIFICATION, observerMethod);
        BroadcastCenter.shareInstance().addObserver(this, MessageCenter.RECEIVED_MSG_NOTIFICATION, observerMethod);
    }

    BroadcastCenter.Method<MainTabActivity> observerMethod = new BroadcastCenter.Method<MainTabActivity>() {
        @Override
        public void onReceive(MainTabActivity observer, Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(UserCenter.USER_LOGIN_NOTIFICATION)) {
                MessageCenter.shareInstance().start();//开启
            } else if (action.equals(UserCenter.USER_LOGOUT_NOTIFICATION)) {
                MessageCenter.shareInstance().stop();//关闭
            } else if (action.equals(MessageCenter.RECEIVED_MSG_NOTIFICATION)) {
                tabBar().tabItemAtIndex(1).setBadgeValue(UITabBar.TabItem.BADGE_VALUE_DOT_VALUE);
            }
        }
    };

    private long mLastBackKeyTime;

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - mLastBackKeyTime) > 2000) {
                App.toast(getString(R.string.press_again_exit));
                mLastBackKeyTime = System.currentTimeMillis();
            } else {
                Navigator.shareInstance().finishApplication();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }


    @Override
    protected void onSelectedViewControllerAtIndex(int index) {
        super.onSelectedViewControllerAtIndex(index);

        if (index == 0) {
            tabBar().setBackgroundResource(R.color.black_0_5_alpha);
            tabBar().setSeparateLineHide(true);
        } else {
            tabBar().setBackgroundResource(R.color.white);
            tabBar().setSeparateLineHide(false);
        }

//        if (index == 1) {//清空未读数
//            tabBar().tabItemAtIndex(1).setBadgeValue("");
//        }
    }
}
