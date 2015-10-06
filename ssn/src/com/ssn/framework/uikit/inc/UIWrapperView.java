package com.ssn.framework.uikit.inc;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import com.ssn.framework.foundation.BroadcastCenter;
import com.ssn.framework.foundation.Density;
import com.ssn.framework.foundation.TaskQueue;
import com.ssn.framework.uikit.UIEvent;

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
        if (changed >= getKeyboardMinHeight() && changed < oldh) {
            _showingKeyboard = true;

            if (_bottomView != null) {//键盘弹出将tool bar弹出问题修改
                TaskQueue.mainQueue().executeDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (_showingKeyboard) {
                            _bottomView.setVisibility(GONE);
                        }
                    }
                }, 100);
            }

            Intent intent = new Intent(UIEvent.UIKeyboardWillShowNotification);
            intent.putExtra(UIEvent.UIKeyboardHeightKey,changed);
            BroadcastCenter.shareInstance().postBroadcast(intent);
        }
        else if (changed <= getKeyboardMinHeight() && -changed < h) {
            _showingKeyboard = false;

            if (_bottomView != null) {
                _bottomView.setVisibility(VISIBLE);
            }

            Intent intent = new Intent(UIEvent.UIKeyboardWillHideNotification);
            intent.putExtra(UIEvent.UIKeyboardHeightKey,-changed);
            BroadcastCenter.shareInstance().postBroadcast(intent);
        }

//        1860-1080=780
//        Log.e("tabbar", "yyyyyyy"+h+"old"+oldh);
    }

    private static int KEYBOARD_MIN_HEIGHT = 100;//纯粹是经验值
    private boolean _showingKeyboard;
    private int _keyboardMinHeight;
    private int getKeyboardMinHeight() {
        if (_keyboardMinHeight > 0) {return _keyboardMinHeight;}
        _keyboardMinHeight = Density.dipTopx(KEYBOARD_MIN_HEIGHT);
        return _keyboardMinHeight;
    }
    public boolean isShowingKeyboard() {return _showingKeyboard;}

    private View _bottomView;
    public void setBottomDockView(View view) {
        _bottomView = view;
    }
}
