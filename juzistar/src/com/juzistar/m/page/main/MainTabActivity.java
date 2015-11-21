package com.juzistar.m.page.main;


import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.ViewTreeObserver;
import android.widget.Toast;
import com.juzistar.m.R;
import com.ssn.framework.foundation.App;
import com.ssn.framework.uikit.BaseTabActivity;
import com.ssn.framework.uikit.Navigator;

/**
 * Created by lingminjun on 15/9/26.
 */
public class MainTabActivity extends BaseTabActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setBackgroundDrawable(R.drawable.page_bd);

        tabBar().setBackgroundResource(R.color.black_0_5_alpha);


//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);


//        getRootWrapView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                getKeyboardHeight();
//            }
//        });
    }

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

//    private Rect r = new Rect();
//    private int mVisibleHeight = 0;
//    private boolean mIsKeyboardShow = false;
//    private void getKeyboardHeight() {
//        getWindow().getDecorView().getWindowVisibleDisplayFrame(r);
//        int visibleHeight = r.height();
//        if (mVisibleHeight == 0) {
//            mVisibleHeight = visibleHeight;
//            return;
//        }
//        if (mVisibleHeight == visibleHeight) {
//            return;
//        }
//
//        int diff = visibleHeight - mVisibleHeight;
//
//        if (diff < 0) {
//            diff = -diff;
//
//            mIsKeyboardShow = true;
//        } else {
//            mIsKeyboardShow = false;
//        }
//
//        mVisibleHeight = visibleHeight;
//
//        Log.e("keyboard:","keybord show="+mIsKeyboardShow+";height="+diff);
//    }
}
