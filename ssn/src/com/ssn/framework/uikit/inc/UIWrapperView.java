package com.ssn.framework.uikit.inc;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

/**
 * Created by lingminjun on 15/10/4.
 */
public class UIWrapperView extends LinearLayout {
    public UIWrapperView(Context context) {
        super(context);
    }

    public UIWrapperView(Context context, AttributeSet attrs) {
        super(context,attrs);
    }

    public UIWrapperView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UIWrapperView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        int changed = oldh - h;
        if (changed >= KEYBOARD_MIN_HEIGHT && changed < oldh) {
            _showingKeyboard = true;

            if (_bottomView != null) {
                _bottomView.setVisibility(GONE);
            }
        }
        else if (changed <= KEYBOARD_MIN_HEIGHT && -changed < h) {
            _showingKeyboard = false;

            if (_bottomView != null) {
                _bottomView.setVisibility(VISIBLE);
            }
        }

//        1860-1080=780
//        Log.e("tabbar", "yyyyyyy"+h+"old"+oldh);
    }

    private static int KEYBOARD_MIN_HEIGHT = 100;
    private boolean _showingKeyboard;

    public boolean isShowingKeyboard() {return _showingKeyboard;}

    private View _bottomView;
    public void setBottomDockView(View view) {
        _bottomView = view;
    }
}
