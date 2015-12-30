package com.ssn.framework.uikit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ssn.framework.R;
import com.ssn.framework.foundation.*;

/**
 * Created by lingminjun on 15/12/12.
 */
public class UILockScreenKeyboard extends Activity {
    private static final int INPUT_MAX_LENGTH = 30;//最多输入字数
    private static final int INPUT_EDIT_MAX_LINES = 2;//内容最多输入行数
    private static final int INPUT_EDIT_HINT_MAX_LINES = 1;//hint最多输入行数

    private static final String HIDE_RIGHT_BTN_KEY = "hideRightButton";
    private static final String HIDE_LIMIT_WORD_KEY = "hideLimitWord";
    private static final String LIMIT_WORD_COUNT_KEY = "limitWordCount";
    private static final String DISABLED_SCOPE_KEY = "disabledScope";

    private LinearLayout _wrapperView;
    private FrameLayout _keyboardPanel;
    private View _scopePanel;
    private ViewGroup _boardPanel;

    private TextView _rightButton;
    private TextView _rightMargin;
    private TextView _wordLimitText;
    private EditText _input;

    private View _customView;

    private boolean _showSystemKeyboard;
    private boolean _showCustomKeyboard;

    public static interface KeyboardListener {
        /**
         * 当键盘加载完回调
         * @param keyboard
         */
        public void onKeyboardDidLoad(UILockScreenKeyboard keyboard);

        /**
         * 发送按钮
         * @param keyboard
         * @param sender
         */
        public void onSendButtonClick(UILockScreenKeyboard keyboard, View sender);//发送按钮被触发

        /**
         * 右按钮事件
         * @param keyboard
         * @param sender
         * @return 返回yes表示事件已经处理，返回false表示采用控件默认
         */
        public boolean onRightButtonClick(UILockScreenKeyboard keyboard, View sender);


        /**
         * 键盘上面区域点击事件
         * @param keyboard
         * @param sender
         * @return
         */
        public void onScopeViewClick(UILockScreenKeyboard keyboard, View sender);

        /**
         /**
         * 自定义按钮事件
         * @param keyboard
         * @param sender 自定义按钮
         * @param buttonKey 自定义按钮的key
         */
        public void onCustomButtonClick(UILockScreenKeyboard keyboard, View sender, int buttonKey);

        /**
         * 键盘高度变化
         * @param keyboard
         * @param newHeight
         * @param oldHeight
         */
        public void onKeyboardChanged(UILockScreenKeyboard keyboard, int newHeight,int oldHeight);

        /**
         * 键盘状态改变
         * @param keyboard
         * @param isShow
         */
        public void onKeyboardStatusChanged(UILockScreenKeyboard keyboard, boolean isShow);
    }

    private static KeyboardListener listener;
    private static UILockScreenKeyboard keyboard;
    private static View customKeyboard;
    public static void show(Activity activity,KeyboardListener listener) {
        show(activity,listener,false);
    }


    public static void show(Activity activity,KeyboardListener alistener,boolean hideRightButton) {
        show(activity,alistener,null,INPUT_MAX_LENGTH,hideRightButton,true,false);
    }

    public static void show(Activity activity,KeyboardListener alistener,View customView) {
        show(activity,alistener,customView,INPUT_MAX_LENGTH,false,true,false);
    }

    public static void show(Activity activity,KeyboardListener alistener, View customView, int wordLimit, boolean hideRightButton, boolean hideLimit, boolean disabledScope) {
        listener = alistener;
        customKeyboard = customView;
        Intent intent = new Intent(activity,UILockScreenKeyboard.class);
        intent.putExtra(HIDE_RIGHT_BTN_KEY,hideRightButton);
        intent.putExtra(HIDE_LIMIT_WORD_KEY,hideLimit);
        intent.putExtra(LIMIT_WORD_COUNT_KEY,wordLimit);
        intent.putExtra(DISABLED_SCOPE_KEY,disabledScope);
        activity.startActivity(intent);
        activity.overridePendingTransition(R.anim.ssn_push_bottom_in, R.anim.ssn_stay_put);
    }

    public static void dismiss() {
        if (keyboard != null) {
            keyboard.finish();
        }
    }

    public static KeyboardListener keyboardListener() {
        return listener;
    }

    public static UILockScreenKeyboard keyboard() {
        return keyboard;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        keyboard = this;
        super.onCreate(savedInstanceState);

        setContentView(R.layout.ssn_lock_screen_keyboard_layout);

        _wrapperView = (LinearLayout)findViewById(R.id.ssn_keyboard_wrapper);
        _wrapperView.addOnLayoutChangeListener(layoutChangeListener);

        _keyboardPanel = (FrameLayout)findViewById(R.id.ssn_keyboard_panel);

        _scopePanel = findViewById(R.id.ssn_scope_panel);
        _boardPanel = (ViewGroup)findViewById(R.id.ssn_board_panel);
        _wordLimitText = (TextView)findViewById(R.id.ssn_word_limit_label);
        _rightButton = (TextView)findViewById(R.id.ssn_keyboard_right_button);
        _rightMargin = (TextView)findViewById(R.id.ssn_margin_label);
        _input = (EditText)findViewById(R.id.ssn_input_text);

        _input.setImeOptions(EditorInfo.IME_ACTION_SEND);
        _input.setOnEditorActionListener(editorListener);
        _input.addTextChangedListener(UIEvent.watcher(watcher));
        _input.addOnLayoutChangeListener(changeListener);//高度随之改变

        _rightButton.setOnClickListener(UIEvent.click(_rightBtnAction));
        _scopePanel.setOnClickListener(UIEvent.click(_scopeAction));

        _showSystemKeyboard = true;//默认显示

        Intent intent = getIntent();
        boolean hideRightButton = intent.getBooleanExtra(HIDE_RIGHT_BTN_KEY,false);
        if (hideRightButton) {
            hideRightButton();
        }

        boolean hideLimit = intent.getBooleanExtra(HIDE_LIMIT_WORD_KEY,false);
        if (hideLimit) {
            _wordLimitText.setVisibility(View.GONE);
        }

        int limitWord = intent.getIntExtra(LIMIT_WORD_COUNT_KEY, INPUT_MAX_LENGTH);
        _wordLimitText.setText(String.format("%d/%d", 0, limitWord));
        _input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(limitWord)});

        boolean disabledScope = intent.getBooleanExtra(DISABLED_SCOPE_KEY, false);
        if (disabledScope) {
            _scopePanel.setVisibility(View.GONE);
        } else {
            _scopePanel.setVisibility(View.VISIBLE);
        }

        if (customKeyboard != null) {
            setKeyboardBody(customKeyboard);
        }

        if (listener != null) {
            listener.onKeyboardDidLoad(this);
        }
    }

    private View.OnClickListener onClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };

    @Override
    public void finish() {
//        innerHideSystemKeyboard(false);
        listener = null;
        keyboard = null;
        customKeyboard = null;
        super.finish();
        overridePendingTransition(R.anim.ssn_stay_put, R.anim.ssn_push_bottom_out);
    }

    private static int keyboard_min_height = Density.dipTopx(118);//353;//经验值
    private int sys_keyboard_height = 827;//274
    private View.OnLayoutChangeListener layoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (listener != null && oldBottom != 0) {
                if (oldBottom - bottom > keyboard_min_height) {
                    _showCustomKeyboard = false;
                    _showSystemKeyboard = true;
                    sys_keyboard_height = oldBottom - bottom;
                    adjustKeyboardHeight();//马上隐藏custom键盘
                    listener.onKeyboardStatusChanged(UILockScreenKeyboard.this, true);
                } else if (bottom - oldBottom > keyboard_min_height) {
                    _showSystemKeyboard = false;
                    listener.onKeyboardStatusChanged(UILockScreenKeyboard.this, false);
                }
            }
        }
    };

    private View.OnClickListener _rightBtnAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            boolean done = false;
            if (listener != null) {
                done = listener.onRightButtonClick(UILockScreenKeyboard.this, view);
            }

            if (!done) {
                if (_customView != null) {
                    _showCustomKeyboard = !_showCustomKeyboard;
                    switchKeyboardStatus();
                }
            }
        }
    };

    private View.OnClickListener _scopeAction = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (listener != null) {
                listener.onScopeViewClick(UILockScreenKeyboard.this, view);
            }
        }
    };

    private TextView.OnEditorActionListener editorListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            final String string = getTextString();
            if (actionId == EditorInfo.IME_ACTION_SEND
                    || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                //do something;
                if (TextUtils.isEmpty(string)) {
//                    Utils.toast(getActivity(),Res.localized(R.string.));
                    return false;
                } else {//点击键盘发送按钮
                    if (listener != null) {
                        try {
                            listener.onSendButtonClick(UILockScreenKeyboard.this, textView);
                        } catch (Throwable e) {}
                    }
                }
                return true;
            }
            return false;
        }
    };

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {_showSystemKeyboard = true;}

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable != null) {
                String content = String.format("%d/%d", editable.length(), INPUT_MAX_LENGTH);
                _wordLimitText.setText(content);
            }
            boolean empty = (editable == null) || TextUtils.isEmpty(editable.toString().trim());
            int maxLines = empty ? INPUT_EDIT_HINT_MAX_LINES : INPUT_EDIT_MAX_LINES;
            setEditMaxLines(maxLines);
        }
    };

    private View.OnLayoutChangeListener changeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            int newHeight = bottom - top;
            int oldHeight = oldBottom - oldTop;
            if (newHeight != oldHeight) {

                if (listener != null) {
                    try {
                        listener.onKeyboardChanged(UILockScreenKeyboard.this, _boardPanel.getHeight(), oldHeight);
                    } catch (Throwable e) {}

                }
            }
        }
    };

    private void hideRightButton() {
        _rightButton.setVisibility(View.GONE);
        _rightMargin.setVisibility(View.INVISIBLE);
    }

    private void innerShowSystemKeyboard() {
        //弹起键盘
        _input.requestFocus();
        InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(_input, InputMethodManager.SHOW_IMPLICIT);
        _showSystemKeyboard = true;
        Editable editable = _input.getText();
        if (editable != null) {
            _input.setSelection(editable.length());
        }
        else {
            _input.setSelection(0);
        }

        adjustKeyboardHeight();
    }

    private void innerHideSystemKeyboard(boolean keepFocus) {
        //隐藏键盘
        InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(_input.getWindowToken(), 0);
        _showSystemKeyboard = false;

        if (!keepFocus) {
            _input.clearFocus();
        }

        adjustKeyboardHeight();
    }

    private void setKeyboardBody(View customView) {
        if (_customView == customView) {
            return;
        }

        if (_customView != null) {
            _keyboardPanel.removeView(_customView);
        }

        _customView = customView;
        if (customView != null) {
            _keyboardPanel.addView(customView);
            _keyboardPanel.setVisibility(View.GONE);
        }
    }

    public void setRightButtonResourceId(int resId) {
        if (_rightButton != null) {
            _rightButton.setBackgroundResource(resId);
        }
    }

    public void setRightButtonTitle(String title) {
        if (_rightButton != null) {
            _rightButton.setText(title);
        }
    }

    public String text() {
        if (_input != null) {
            Editable editable = _input.getText();
            if (editable != null) {
                return editable.toString().trim();
            }
        }
        return null;
    }

    public void setText(String text) {
        if (_input != null) {
            _input.setText(text);
        }
    }

    public boolean isSystemKeyboardShow() {
        return _showSystemKeyboard;
    }

    public boolean isCustomKeyboardShow() {
        return _showCustomKeyboard;
    }

    public void showSystemKeyboard() {
        //切换键盘状态
        switchKeyboardStatus();
    }

    private void switchKeyboardStatus() {
        InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);

        if (_showSystemKeyboard) {
            _showSystemKeyboard = !_showSystemKeyboard;
            boolean ret = imm.hideSoftInputFromWindow(_input.getWindowToken(), 0);
            if (ret) {
                innerHideSystemKeyboard(true);
            } else {
                innerShowSystemKeyboard();
            }
            Log.e("UIKeyboard", "hide=" + ret);
        } else {
            _showSystemKeyboard = !_showSystemKeyboard;
            boolean ret = imm.showSoftInput(_input, InputMethodManager.SHOW_IMPLICIT);
            if (ret) {
                innerShowSystemKeyboard();
            } else {
                innerHideSystemKeyboard(true);
            }
            Log.e("UIKeyboard","show="+ret);
        }
        _rightButton.setSelected(!_showSystemKeyboard);
    }

    private String getTextString() {
        Editable editable = _input.getText();
        if (editable != null) {
            return editable.toString().trim();
        }
        return TR.string(null);
    }

    private void setEditMaxLines(int maxLines) {
        _input.setSingleLine(false);
        int curLineCount = _input.getLineCount();
        curLineCount = curLineCount <= 0 ? 1 : curLineCount;
        int setLineCount = curLineCount < maxLines ? curLineCount : maxLines;
        _input.setMaxLines(setLineCount);
    }

    Runnable showSystemRunnable = new Runnable() {
        @Override
        public void run() {
            _keyboardPanel.setVisibility(View.GONE);
        }
    };

    Runnable hideSystemRunnable = new Runnable() {
        @Override
        public void run() {
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) _keyboardPanel.getLayoutParams();
            params.height = sys_keyboard_height;
            _keyboardPanel.setVisibility(View.VISIBLE);
        }
    };

    private void adjustKeyboardHeight() {
        if (_customView == null) {return;}

        if (_showSystemKeyboard) {
            TaskQueue.mainQueue().executeDelayed(showSystemRunnable,10);
        } else {
            TaskQueue.mainQueue().executeDelayed(hideSystemRunnable,50);
        }
    }
}
