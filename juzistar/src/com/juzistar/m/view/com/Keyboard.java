package com.juzistar.m.view.com;

import android.view.LayoutInflater;
import android.view.View;
import com.juzistar.m.R;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UILockScreenKeyboard;

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

        View.OnClickListener click = UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_01);
            btn.setKey(KEY.BOOK);
            btn.setOnClickListener(click);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_02);
            btn.setKey(KEY.EAT);
            btn.setOnClickListener(click);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_03);
            btn.setKey(KEY.TEST);
            btn.setOnClickListener(click);
        }

        //10~13
        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_10);
            btn.setKey(KEY.HOME);
            btn.setOnClickListener(click);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_11);
            btn.setKey(KEY.FLOWER);
            btn.setOnClickListener(click);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_12);
            btn.setKey(KEY.HELP);
            btn.setOnClickListener(click);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_13);
            btn.setKey(KEY.DATING);
            btn.setOnClickListener(click);
        }

        //20~23
        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_20);
            btn.setKey(KEY.CAR);
            btn.setOnClickListener(click);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_21);
            btn.setKey(KEY.WEAR);
            btn.setOnClickListener(click);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_22);
            btn.setKey(KEY.SPORT);
            btn.setOnClickListener(click);
        }

        {
            KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_23);
            btn.setKey(KEY.MOVIE);
            btn.setOnClickListener(click);
        }

        return barrageCustomView;
    }

    /**
     * 弹幕输入键盘
     * @return
     */
//    public static UILockScreenKeyboard barrageKeyboard() {
//        if (barrageKeyboard == null) {
//            barrageKeyboard = newKeyboard();
//
//            LayoutInflater inflater = LayoutInflater.from(Res.context());
//            View barrageCustomView = inflater.inflate(R.layout.sub_keyboard_layout, null);
//
//            barrageKeyboard.setKeyboardBody(barrageCustomView);
//            barrageKeyboard.setRightButtonResourceId(R.drawable.button_keyboard_switch_icon);
//            barrageKeyboard.setEnableScopeView(true);
//
//            View.OnClickListener click = UIEvent.click(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (view instanceof KeyboardButton) {
//                        UIKeyboard.KeyboardListener keyboardListener = barrageKeyboard.keyboardListener();
//                        if (keyboardListener != null) {
//                            keyboardListener.onCustomButtonClick(barrageKeyboard,view,((KeyboardButton) view).key());
//                        }
//                    }
//
//                    //弹起键盘
//                    barrageKeyboard.showSystemKeyboard();
//                }
//            });
//
//            //添加事件
//            //00~03
//            {
//                KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_00);
//                btn.setKey(KEY.LOVE);
//                btn.setOnClickListener(click);
//            }
//
//            {
//                KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_01);
//                btn.setKey(KEY.BOOK);
//                btn.setOnClickListener(click);
//            }
//
//            {
//                KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_02);
//                btn.setKey(KEY.EAT);
//                btn.setOnClickListener(click);
//            }
//
//            {
//                KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_03);
//                btn.setKey(KEY.TEST);
//                btn.setOnClickListener(click);
//            }
//
//            //10~13
//            {
//                KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_10);
//                btn.setKey(KEY.HOME);
//                btn.setOnClickListener(click);
//            }
//
//            {
//                KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_11);
//                btn.setKey(KEY.FLOWER);
//                btn.setOnClickListener(click);
//            }
//
//            {
//                KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_12);
//                btn.setKey(KEY.HELP);
//                btn.setOnClickListener(click);
//            }
//
//            {
//                KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_13);
//                btn.setKey(KEY.DATING);
//                btn.setOnClickListener(click);
//            }
//
//            //20~23
//            {
//                KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_20);
//                btn.setKey(KEY.CAR);
//                btn.setOnClickListener(click);
//            }
//
//            {
//                KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_21);
//                btn.setKey(KEY.WEAR);
//                btn.setOnClickListener(click);
//            }
//
//            {
//                KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_22);
//                btn.setKey(KEY.SPORT);
//                btn.setOnClickListener(click);
//            }
//
//            {
//                KeyboardButton btn = (KeyboardButton) barrageCustomView.findViewById(R.id.button_23);
//                btn.setKey(KEY.MOVIE);
//                btn.setOnClickListener(click);
//            }
//        }
//
//        return barrageKeyboard;
//    }
}
