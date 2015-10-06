package com.orange.m.page.main;


import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.Toast;
import com.orange.m.R;
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

//        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_UNSPECIFIED);
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
}
