package com.juzistar.m.view;

import android.view.LayoutInflater;
import android.view.View;
import com.juzistar.m.R;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIKeyboard;

/**
 * Created by lingminjun on 15/11/25.
 */
public final class Keyboard {
    public static View barrageCustomView = null;
    public static UIKeyboard barrageKeyboard() {
        if (barrageCustomView == null) {
            LayoutInflater inflater = LayoutInflater.from(Res.context());
            barrageCustomView = inflater.inflate(R.layout.sub_keyboard_layout, null);
            UIKeyboard.shareInstance().setKeyboardBody(barrageCustomView);
            UIKeyboard.shareInstance().setRightButtonResourceId(R.drawable.button_keyboard_switch_icon);
        }

        return UIKeyboard.shareInstance();
    }
}
