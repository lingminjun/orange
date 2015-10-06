package com.orange.m.view.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import com.orange.m.R;
import com.orange.m.Utils.Utils;
import com.ssn.framework.foundation.App;
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

    private EditText inputCommentEdit;
    private View inputLayout;
    private View sendCommentBtn;
    private TextView input_count_tv;
//    private Context context;
//    private ShowKeyBoardByItSelfDeal showKeyBoardByItSelfDeal;
//    private AccountBiz accountBiz;
    private boolean keepInputEditVisibleWhenShowEditNick = false;

    private Keyboard() {
//        this.context = context;
//        accountBiz = new AccountBiz(context);
    }

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

//    public ShowKeyBoardByItSelfDeal getShowKeyBoardByItSelfDeal() {
//        return showKeyBoardByItSelfDeal;
//    }

//    public void setShowKeyBoardByItSelfDeal(ShowKeyBoardByItSelfDeal showKeyBoardByItSelfDeal) {
//        this.showKeyBoardByItSelfDeal = showKeyBoardByItSelfDeal;
//    }

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
        if (inputLayout != null) {
            inputLayout.setVisibility(View.VISIBLE);
        }

        if (inputCommentEdit != null) {

            //高度需要调整
            if (!Utils.isEdtEmpty(inputCommentEdit)) {
//            inputCommentEdit.setMaxLines(COMMENT_EDIT_MAX_LINES);
                setEditMaxLines(COMMENT_EDIT_MAX_LINES);
            }

            if (!inputCommentEdit.isFocused()) {
                inputCommentEdit.requestFocus();
                InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(inputCommentEdit, InputMethodManager.SHOW_IMPLICIT);
                Editable editable = inputCommentEdit.getText();
                if (editable != null) {
                    inputCommentEdit.setSelection(editable.length());
                }
                else {
                    inputCommentEdit.setSelection(0);
                }
            }
        }
    }

    public void dismiss(boolean isDock) {
        inputCommentEdit.setSingleLine(true);
//        inputCommentEdit.setEllipsize(TextUtils.TruncateAt.END);
        inputCommentEdit.clearFocus();
//        inputLayout.setVisibility(keepInputViewVisible ? View.VISIBLE : View.GONE);
//        inputCommentEdit.setFocusable(false);
//        if (keepInputViewVisible) {
//            inputCommentEdit.setOnClickListener(UIEvent.click(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (showKeyBoardByItSelfDeal != null) {
//                        showInputComment(showKeyBoardByItSelfDeal.hint, showKeyBoardByItSelfDeal.sendBtnClickListener);
//                    } else {
//                        App.toast("inner error!");
//                    }
//                }
//            }));
//        }
        InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputCommentEdit.getWindowToken(), 0);

        if (!isDock) {
            inputLayout.setVisibility(View.GONE);
        }
//        closeInputComment(isDock);
    }


    public void setPlaceholder(String placeholder) {
        inputCommentEdit.setHint(placeholder);
    }

    public String text() {
        if (inputCommentEdit != null) {
            Editable editable = inputCommentEdit.getText();
            if (editable != null) {
                return editable.toString().trim();
            }
        }
        return null;
    }

    public void setText(String text) {
        if (inputCommentEdit != null) {
            inputCommentEdit.setText(text);
        }
    }

    /**
     * 实例化输入框layout
     *
     * @return
     */
    private View inputView() {
        if (inputLayout != null) {
            return inputLayout;
        }

        Context context = Res.context();
        inputLayout = LayoutInflater.from(context).inflate(R.layout.keyboard_layout, null);
        sendCommentBtn = inputLayout.findViewById(R.id.send_btn);
        if (sendClick != null) {
            sendCommentBtn.setOnClickListener(UIEvent.click(sendClick));
        }
        inputCommentEdit = (EditText) inputLayout.findViewById(R.id.input_edt);
        inputCommentEdit.setFilters(new InputFilter[]{new InputFilter.LengthFilter(COMMENT_MAX_LENGTH)});
        input_count_tv = (TextView) inputLayout.findViewById(R.id.count_tv);
        inputCommentEdit.addTextChangedListener(UIEvent.watcher(new TextWatcher() {
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
                    input_count_tv.setText(content);
                }
                boolean empty = (editable == null) || TextUtils.isEmpty(editable.toString().trim());
                int maxLines = empty ? COMMENT_EDIT_HINT_MAX_LINES : COMMENT_EDIT_MAX_LINES;
                setEditMaxLines(maxLines);
            }
        }));

        //高度随之改变
        inputCommentEdit.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View view, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                int newHeight = bottom - top;
                int oldHeight = oldBottom - oldTop;
                if (newHeight != oldHeight) {

                    if (_heightChanged != null) {
                        _heightChanged.onChanged(inputLayout.getHeight(),oldHeight);
                    }
                }
            }
        });

        return inputLayout;
    }

    private void setEditMaxLines(int maxLines) {
        inputCommentEdit.setSingleLine(false);
        int curLineCount = inputCommentEdit.getLineCount();
        curLineCount = curLineCount <= 0 ? 1 : curLineCount;
        int setLineCount = curLineCount < maxLines ? curLineCount : maxLines;
        inputCommentEdit.setMaxLines(setLineCount);
    }

//    public EditText getInputCommentEdit() {
//        return inputCommentEdit;
//    }

//    public static class ShowKeyBoardByItSelfDeal {
//        public String hint;
//        public View.OnClickListener sendBtnClickListener;
//    }

    private View.OnClickListener sendClick;
    public void setSendClick(View.OnClickListener click) {
        sendClick = click;
        if (sendCommentBtn != null) {
            if (click != null) {
                sendCommentBtn.setOnClickListener(UIEvent.click(click));
            }
            else {
                sendCommentBtn.setOnClickListener(null);
            }
        }
    }

    private void closeInputComment(boolean keepInputViewVisible) {
        inputCommentEdit.setSingleLine(true);
        inputCommentEdit.setEllipsize(TextUtils.TruncateAt.END);
        inputCommentEdit.clearFocus();
        inputLayout.setVisibility(keepInputViewVisible ? View.VISIBLE : View.GONE);
        inputCommentEdit.setFocusable(false);
//        if (keepInputViewVisible) {
//            inputCommentEdit.setOnClickListener(UIEvent.click(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    if (showKeyBoardByItSelfDeal != null) {
//                        showInputComment(showKeyBoardByItSelfDeal.hint, showKeyBoardByItSelfDeal.sendBtnClickListener);
//                    } else {
//                        App.toast("inner error!");
//                    }
//                }
//            }));
//        }
        InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(inputCommentEdit.getWindowToken(), 0);
    }

//    public void setInputLayoutVisible(boolean visible) {
//        inputLayout.setVisibility(visible ? View.VISIBLE : View.GONE);
//    }

    public void showInputComment(String hint, final View.OnClickListener sendBtnListener) {
        inputLayout.setVisibility(View.VISIBLE);
        if (!Utils.isEdtEmpty(inputCommentEdit)) {
//            inputCommentEdit.setMaxLines(COMMENT_EDIT_MAX_LINES);
            setEditMaxLines(COMMENT_EDIT_MAX_LINES);
        }
        inputCommentEdit.setOnClickListener(null);
        inputCommentEdit.setFocusableInTouchMode(true);
        inputCommentEdit.requestFocus();
        if (!TextUtils.isEmpty(hint)) {
            inputCommentEdit.setHint(hint);
        }

//        sendCommentBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
//            @Override
//            public void onClick(final View view) {
//                if (TextUtils.isEmpty(getInputEditContent())) {
//                    App.toast(Res.localized(R.string.please_input_content));
//                    return;
//                }
//                sendBtnListener.onClick(view);
//            }
//        }));
        InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(inputCommentEdit, InputMethodManager.SHOW_IMPLICIT);
    }

//    public boolean isKeepInputEditVisibleWhenShowEditNick() {
//        return keepInputEditVisibleWhenShowEditNick;
//    }
//
//    public void setKeepInputEditVisibleWhenShowEditNick(boolean keepInputEditVisibleWhenShowEditNick) {
//        this.keepInputEditVisibleWhenShowEditNick = keepInputEditVisibleWhenShowEditNick;
//    }


//    public boolean isInputEditEmpty() {
//        return Utils.isEdtEmpty(inputCommentEdit);
//    }
//
//    public String getInputEditContent() {
//        String content = "";
//        if (!isInputEditEmpty()) {
//            content = inputCommentEdit.getText().toString().trim();
//        }
//        return content;
//    }
//
//    public void setInputEditEmpty() {
//        if (inputCommentEdit != null) {
//            inputCommentEdit.setText("");
//        }
//    }

}
