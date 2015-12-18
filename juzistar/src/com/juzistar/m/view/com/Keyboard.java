package com.juzistar.m.view.com;

import android.view.LayoutInflater;
import android.view.View;
import com.juzistar.m.R;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UILockScreenKeyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 15/11/25.
 */
public final class Keyboard {

    /**
     * 自定义key定义
     */
    public static class KEY {
        public final static int NAN =  0;
        public final static int LOVE = 1;
        public final static int BOOK = 2;
        public final static int EAT  = 3;
        public final static int TEST = 4;
        public final static int HOME = 5;
        public final static int FLOWER = 6;
        public final static int HELP = 7;
        public final static int DATING = 8;
        public final static int CAR = 9;
        public final static int WEAR = 10;
        public final static int SPORT = 11;
        public final static int MOVIE = 12;
    }


//    private static UIKeyboard newKeyboard() {
//        return new UIKeyboard(Res.context());
//    }
//    private static UILockScreenKeyboard barrageKeyboard = null;

    public static View barrageCustomView() {
        LayoutInflater inflater = LayoutInflater.from(Res.context());
        View barrageCustomView = inflater.inflate(R.layout.sub_keyboard_layout, null);

        final List<KeyboardButton> btns = new ArrayList<>();
        View.OnClickListener click = UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                for (KeyboardButton btn : btns) {
                    btn.setSelected(false);
                }

                if (view instanceof KeyboardButton) {
                    UILockScreenKeyboard.KeyboardListener keyboardListener = UILockScreenKeyboard.keyboardListener();
                    if (keyboardListener != null) {
                        keyboardListener.onCustomButtonClick(UILockScreenKeyboard.keyboard(),view,((KeyboardButton) view).key());
                    }
                }

                //弹起键盘
                UILockScreenKeyboard.keyboard().showSystemKeyboard();
            }
        });

        //添加事件
        //00~03
        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_00);
            btn.setKey(KEY.LOVE);
            btn.setOnClickListener(click);
            btns.add(btn);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_01);
            btn.setKey(KEY.BOOK);
            btn.setOnClickListener(click);
            btns.add(btn);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_02);
            btn.setKey(KEY.EAT);
            btn.setOnClickListener(click);
            btns.add(btn);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_03);
            btn.setKey(KEY.TEST);
            btn.setOnClickListener(click);
            btns.add(btn);
        }

        //10~13
        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_10);
            btn.setKey(KEY.HOME);
            btn.setOnClickListener(click);
            btns.add(btn);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_11);
            btn.setKey(KEY.FLOWER);
            btn.setOnClickListener(click);
            btns.add(btn);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_12);
            btn.setKey(KEY.HELP);
            btn.setOnClickListener(click);
            btns.add(btn);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_13);
            btn.setKey(KEY.DATING);
            btn.setOnClickListener(click);
            btns.add(btn);
        }

        //20~23
        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_20);
            btn.setKey(KEY.CAR);
            btn.setOnClickListener(click);
            btns.add(btn);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_21);
            btn.setKey(KEY.WEAR);
            btn.setOnClickListener(click);
            btns.add(btn);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_22);
            btn.setKey(KEY.SPORT);
            btn.setOnClickListener(click);
            btns.add(btn);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_23);
            btn.setKey(KEY.MOVIE);
            btn.setOnClickListener(click);
            btns.add(btn);
        }

        return barrageCustomView;
    }

}
