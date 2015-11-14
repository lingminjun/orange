package com.juzistar.m.view.common;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.juzistar.m.R;
import com.juzistar.m.Utils.Utils;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIEvent;


/*
 * 文件名：CommentInputViewManager.java
 * 版权：(C)版权所有2015-2015顺丰国际电商
 * 描述：评论输入
 * 修改人：Administrator
 * 修改时间：202015/5/11 11:55
 */
public final class Keyboard {

    private static final int COMMENT_MAX_LENGTH = 300;//最多输入字数
    private static final int COMMENT_EDIT_MAX_LINES = 3;//内容最多输入行数
    private static final int COMMENT_EDIT_HINT_MAX_LINES = 1;//hint最多输入行数

    private EditText _input;
    private View _keyboard;
    private View _sendButton;
    private TextView _maxLengthTip;

    private Keyboard() {}
    private static Keyboard _instance = null;
    /**
     * 用户中心
     * @return 唯一实例
     */
    static public Keyboard shareInstance() {
        if (_instance != null) return _instance;
        synchronized(Keyboard.class){
            if (_instance != null) return _instance;
            _instance = newInstance();
            return _instance;
        }
    }

    private static Keyboard newInstance() {
        return new Keyboard();
    }


    /**
     * 高度变化回调
     */
    public static interface KeyboardHeightChanged {
        public void onChanged(int newHeight,int oldHeight);
    }

    private KeyboardHeightChanged _heightChanged;
    public void setKeyboardHeightChanged(KeyboardHeightChanged changed) {
        _heightChanged = changed;
    }

    /**
     * 展示到某个view上面
     * @param group
     */
    private ViewGroup _group;
    public void showInView(ViewGroup group) {
        if (_group == group) {
            focus();
            return;
        }

        if (_group != null && _group != group) {
            _group.removeView(inputView());
        }
        _group = group;

        _group.addView(inputView());

        focus();
    }

    private void focus() {
        if (_keyboard != null) {
            _keyboard.setVisibility(View.VISIBLE);
        }

        if (_input != null) {

            //高度需要调整
            if (!Utils.isEdtEmpty(_input)) {
                setEditMaxLines(COMMENT_EDIT_MAX_LINES);
            }

            if (!_input.isFocused()) {
                _input.requestFocus();
                InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(_input, InputMethodManager.SHOW_IMPLICIT);
                Editable editable = _input.getText();
                if (editable != null) {
                    _input.setSelection(editable.length());
                }
                else {
                    _input.setSelection(0);
                }
            }
        }
    }

    public void dismiss(boolean isDock) {
        if (_keyboard == null) {return;}

        _input.setSingleLine(true);
        _input.clearFocus();

        InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(_input.getWindowToken(), 0);

        if (!isDock) {
            _keyboard.setVisibility(View.GONE);
        }
    }


    private String _placeholder;
    public void setPlaceholder(String placeholder) {
        if (_keyboard == null) { _placeholder = placeholder; return;}
        _input.setHint(placeholder);
    }


    private String _text;
    public String text() {
        if (_keyboard == null) {return _text;}

        if (_input != null) {
            Editable editable = _input.getText();
            if (editable != null) {
                return editable.toString().trim();
            }
        }
        return null;
    }

    public void setText(String text) {
        if (_keyboard == null) {_text = text;}
        else {
            if (_input != null) {
                _input.setText(text);
            }
        }
    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            if (editable != null) {
                String content = String.format("%d/%d", editable.length(), COMMENT_MAX_LENGTH);
                _maxLengthTip.setText(content);
            }
            boolean empty = (editable == null) || TextUtils.isEmpty(editable.toString().trim());
            int maxLines = empty ? COMMENT_EDIT_HINT_MAX_LINES : COMMENT_EDIT_MAX_LINES;
            setEditMaxLines(maxLines);
        }
    };

    private View.OnLayoutChangeListener changeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            int newHeight = bottom - top;
            int oldHeight = oldBottom - oldTop;
            if (newHeight != oldHeight) {

                if (_heightChanged != null) {
                    try {
                        _heightChanged.onChanged(_keyboard.getHeight(), oldHeight);
                    } catch (Throwable e) {}

                }
            }
        }
    };

    private String getTextString() {
        Editable editable = _input.getText();
        if (editable != null) {
            return editable.toString().trim();
        }
        return "";
    }

    private TextView.OnEditorActionListener actionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            final String string = getTextString();
            if (actionId == EditorInfo.IME_ACTION_SEND
                    || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                //do something;
                if (TextUtils.isEmpty(string)) {
//                    Utils.toast(getActivity(),Res.localized(R.string.));
                    return false;
                } else {//触发发送
                    if (sendClick != null) {
                        try {
                            sendClick.onClick(textView);
                        } catch (Throwable e) {}
                    }
                }
                return true;
            }
            return false;
        }
    };

    /**
     * 实例化输入框layout
     *
     * @return
     */
    private View inputView() {
        if (_keyboard != null) {
            return _keyboard;
        }

        Context context = Res.context();
        _keyboard = LayoutInflater.from(context).inflate(R.layout.keyboard_layout, null);
        ((ViewGroup)_keyboard).setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
        _sendButton = _keyboard.findViewById(R.id.send_btn);
        if (sendClick != null) {
            _sendButton.setOnClickListener(UIEvent.click(sendClick));
        }
        _input = (EditText) _keyboard.findViewById(R.id.input_edt);
        _input.setImeOptions(EditorInfo.IME_ACTION_SEND);
        _input.setOnEditorActionListener(actionListener);
        _input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(COMMENT_MAX_LENGTH)});
        _maxLengthTip = (TextView) _keyboard.findViewById(R.id.count_tv);
        _input.addTextChangedListener(UIEvent.watcher(watcher));

        //高度随之改变
        _input.addOnLayoutChangeListener(changeListener);

        if (_text != null) {
            _input.setText(_text);
            _text = null;
        }

        if (_placeholder != null) {
            _input.setHint(_placeholder);
            _placeholder = null;
        }

        return _keyboard;
    }

    private void setEditMaxLines(int maxLines) {
        _input.setSingleLine(false);
        int curLineCount = _input.getLineCount();
        curLineCount = curLineCount <= 0 ? 1 : curLineCount;
        int setLineCount = curLineCount < maxLines ? curLineCount : maxLines;
        _input.setMaxLines(setLineCount);
    }


    private View.OnClickListener sendClick;
    public void setSendClick(View.OnClickListener click) {
        sendClick = click;
        if (_sendButton != null) {
            if (click != null) {
                _sendButton.setOnClickListener(UIEvent.click(click));
            }
            else {
                _sendButton.setOnClickListener(null);
            }
        }
    }

//    private void closeInputComment(boolean keepInputViewVisible) {
//        _input.setSingleLine(true);
//        _input.setEllipsize(TextUtils.TruncateAt.END);
//        _input.clearFocus();
//        _keyboard.setVisibility(keepInputViewVisible ? View.VISIBLE : View.GONE);
//        _input.setFocusable(false);
//
//        InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.hideSoftInputFromWindow(_input.getWindowToken(), 0);
//    }
//
//
//    public void showInputComment(String hint, final View.OnClickListener sendBtnListener) {
//        _keyboard.setVisibility(View.VISIBLE);
//        if (!Utils.isEdtEmpty(_input)) {
//            setEditMaxLines(COMMENT_EDIT_MAX_LINES);
//        }
//        _input.setOnClickListener(null);
//        _input.setFocusableInTouchMode(true);
//        _input.requestFocus();
//        if (!TextUtils.isEmpty(hint)) {
//            _input.setHint(hint);
//        }
//
//        InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(_input, InputMethodManager.SHOW_IMPLICIT);
//    }

}
