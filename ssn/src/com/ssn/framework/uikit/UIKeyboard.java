package com.ssn.framework.uikit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.ssn.framework.R;
import com.ssn.framework.foundation.Density;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.foundation.TR;
import org.w3c.dom.Text;

/**
 * Created by lingminjun on 15/11/21.
 */
public final class UIKeyboard extends LinearLayout {
    private static final int COMMENT_MAX_LENGTH = 300;//最多输入字数
    private static final int COMMENT_EDIT_MAX_LINES = 3;//内容最多输入行数
    private static final int COMMENT_EDIT_HINT_MAX_LINES = 1;//hint最多输入行数

    public static interface KeyboardListener {
        public void onSendButtonClick(UIKeyboard keyboard, View sender);//发送按钮被触发

        /**
         * 右按钮事件
         * @param keyboard
         * @param sender
         * @return 返回yes表示事件已经处理，返回false表示采用控件默认
         */
        public boolean onRightButtonClick(UIKeyboard keyboard, View sender);

        /**
         * 键盘高度变化
         * @param newHeight
         * @param oldHeight
         */
        public void onKeyboardChanged(int newHeight,int oldHeight);
    }

    public UIKeyboard(Context context) {
        super(context);
        init();
    }

    public UIKeyboard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UIKeyboard(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private static UIKeyboard _instance = null;
    /**
     * 用户中心
     * @return 唯一实例
     */
    static public UIKeyboard shareInstance() {
        if (_instance != null) return _instance;
        synchronized(UIKeyboard.class){
            if (_instance != null) return _instance;
            _instance = newInstance();
            return _instance;
        }
    }

    private static UIKeyboard newInstance() {
        return new UIKeyboard(Res.context());
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UIKeyboard(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private FrameLayout _keyboardPanel;
    private ViewGroup _inputPanel;
    private TextView _rightButton;
    private TextView _wordLimitText;
    private EditText _input;

    private boolean _showSystemKeyboard;

    private OnClickListener _rightBtnAction = new OnClickListener() {
        @Override
        public void onClick(View view) {

            boolean done = false;
            if (listener != null) {
                done = listener.onRightButtonClick(UIKeyboard.this, view);
            }

            if (!done) {

                InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);

                if (_showSystemKeyboard) {
                    boolean ret = imm.hideSoftInputFromWindow(_input.getWindowToken(), 0);
                    if (ret) {
                        hideSystemKeyboard(true);
                    } else {
                        _showSystemKeyboard = false;
                        showSystemKeyboard();
                    }
                    Log.e("UIKeyboard","hide="+ret);
                } else {
                    boolean ret = imm.showSoftInput(_input, InputMethodManager.SHOW_IMPLICIT);
                    if (ret) {
                        showSystemKeyboard();
                    } else {
                        _showSystemKeyboard = true;
                        hideSystemKeyboard(true);
                    }
                    Log.e("UIKeyboard","show="+ret);
                }

                _showSystemKeyboard = !_showSystemKeyboard;
                _rightButton.setSelected(!_showSystemKeyboard);
            }
        }
    };

    private KeyboardListener listener;
    private View _customView;

    private int _rightButtonResourceId;

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
                            listener.onSendButtonClick(UIKeyboard.this, textView);
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
                String content = String.format("%d/%d", editable.length(), COMMENT_MAX_LENGTH);
                _wordLimitText.setText(content);
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

                if (listener != null) {
                    try {
                        listener.onKeyboardChanged(UIKeyboard.this.getHeight(), oldHeight);
                    } catch (Throwable e) {}

                }
            }
        }
    };

    private void init() {
        this.setId(R.id.ssn_keyboard);
        this.setOrientation(LinearLayout.VERTICAL);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.ssn_keyboard, this);

        _keyboardPanel = (FrameLayout)findViewById(R.id.ssn_keyboard_panel);
        _inputPanel = (ViewGroup)findViewById(R.id.ssn_input_panel);
        _wordLimitText = (TextView)findViewById(R.id.ssn_word_limit_label);
        _rightButton = (TextView)findViewById(R.id.ssn_keyboard_right_button);
        _input = (EditText)findViewById(R.id.ssn_input_text);

        _input.setImeOptions(EditorInfo.IME_ACTION_SEND);
        _input.setOnEditorActionListener(editorListener);
        _input.addTextChangedListener(UIEvent.watcher(watcher));
        _input.addOnLayoutChangeListener(changeListener);//高度随之改变
        _input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(COMMENT_MAX_LENGTH)});

        _rightButton.setOnClickListener(UIEvent.click(_rightBtnAction));

    }

    public void setKeyboardBody(View customView) {
        if (_customView == customView) {
            return;
        }

        if (_customView != null) {
            _keyboardPanel.removeView(_customView);
        }

        _customView = customView;
        if (customView != null) {
            _keyboardPanel.addView(customView);
        }
    }

    public void setRightButtonResourceId(int resId) {
        _rightButtonResourceId = resId;
        if (_rightButton != null) {
            _rightButton.setBackgroundResource(resId);
        }
    }

    public void setKeyboardListener(KeyboardListener listener) {
        this.listener = listener;
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

    /**
     * 展示键盘
     * @param viewController
     */
    private FrameLayout showV;
    public void show(UIViewController viewController) {
        if (viewController == null) {return;}

        Activity activity = viewController.getActivity();
        if (activity == null || activity.isFinishing()) {return;}

        View view = activity.findViewById(R.id.ssn_root_wrap_view);
        if (view == null || !(view instanceof FrameLayout)) {return;}

        FrameLayout frameLayout = (FrameLayout)view;

        if (showV != frameLayout) {
            if (showV != null) {
                showV.removeView(this);
            }
            showV = frameLayout;
            frameLayout.addView(this);

            //在frameLayout底部
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) this.getLayoutParams();
            params.height = FrameLayout.LayoutParams.WRAP_CONTENT;
            params.gravity = Gravity.BOTTOM;
            this.setLayoutParams(params);

//            this.getRootView().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    getSystemKeyboardHeight();
//                }
//            });
        }

        _keyboardPanel.setVisibility(VISIBLE);//键盘的body展示出来
        _showSystemKeyboard = true;

        showSystemKeyboard();

        if (listener != null) {
            try {
                listener.onKeyboardChanged(this.getHeight(),0);
            } catch (Throwable e){e.printStackTrace();}
        }
    }

    public void dismiss(boolean isDock) {
        if (showV == null) {return;}

        if (isDock) {
            _showSystemKeyboard = false;
        }
        hideSystemKeyboard(isDock);

        int keyboard_height = 0;
        if (!isDock) {
            showV.removeView(this);
            showV = null;
        } else {
            _keyboardPanel.setVisibility(GONE);
            keyboard_height = this.getHeight();
        }

        if (listener != null) {
            try {
                listener.onKeyboardChanged(keyboard_height,0);
            } catch (Throwable e){e.printStackTrace();}
        }
    }

    public boolean isShow() {
        return showV != null;
    }

    public boolean isSystemKeyboardShow() {
        return _showSystemKeyboard;
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

    private static int _keyboard_height = 274;
    private void adjustKeyboardHeight() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) _keyboardPanel.getLayoutParams();

        //展示系统键盘
        if (_showSystemKeyboard) {
            params.height = Density.dipTopx(_keyboard_height);
        } else {
            params.height = LinearLayout.LayoutParams.WRAP_CONTENT;
        }
    }

    private void showSystemKeyboard() {
        //弹起键盘
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

        adjustKeyboardHeight();
    }

    private void hideSystemKeyboard(boolean keepFocus) {
        //隐藏键盘
        InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(_input.getWindowToken(), 0);

        if (!keepFocus) {
            _input.clearFocus();
        }

        adjustKeyboardHeight();
    }

//
//    public boolean isViewCovered(final View view)
//    {
//        View currentView = view;
//
////        Rect currentViewRect = new Rect();
////        boolean partVisible =currentView.getGlobalVisibleRect(currentViewRect);
////        boolean totalHeightVisible = (currentViewRect.bottom - currentViewRect.top) >= adView.getMeasuredHeight();
////        boolean totalWidthVisible = (currentViewRect.right - currentViewRect.left) >= adView.getMeasuredWidth();
////        boolean totalViewVisible = partVisible && totalHeightVisible && totalWidthVisible;
////        if (!totalViewVisible)//if any part of the view is clipped by any of its parents,return true
////            return true;
//
////        while (currentView.getParent() instanceof ViewGroup)
////        {
////            ViewGroup currentParent = (ViewGroup) currentView.getParent();
////
////            Log.e("UIKeyboard","" + currentParent);
////
////            if (currentParent.getVisibility() != View.VISIBLE)//if the parent of view is not visible,return true
////                return true;
////
////            int start = indexOfViewInParent(currentView, currentParent);
////            for (int i = start + 1; i < currentParent.getChildCount(); i++)
////            {
////                Rect viewRect = new Rect();
////                view.getGlobalVisibleRect(viewRect);
////                View otherView = currentParent.getChildAt(i);
////                Rect otherViewRect = new Rect();
////                otherView.getGlobalVisibleRect(otherViewRect);
////                if (Rect.intersects(viewRect, otherViewRect))//if view intersects its older brother(covered),return true
////                    return true;
////            }
////            currentView = currentParent;
////        }
////        return false;
//
////        WindowManager wmManager=(WindowManager) Res.context().getSystemService(Context.WINDOW_SERVICE);
////        Activity activity = Navigator.shareInstance().topActivity();
////        Window win = activity.getWindow();
////        allView((ViewGroup)win.getDecorView());
//
//        InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
//        return imm.isActive();
////        return false;
//    }
//
//    private void allView(ViewGroup viewGroup) {
//        int c = viewGroup.getChildCount();
//        for (int i = 0;i < c;i++) {
//            View view = viewGroup.getChildAt(i);
//            Log.e("v",""+view);
//            if (view instanceof ViewGroup) {
//                allView((ViewGroup)view);
//            }
//        }
//    }
//
//    private int indexOfViewInParent(View view, ViewGroup parent)
//    {
//        int index;
//        for (index = 0; index < parent.getChildCount(); index++)
//        {
//            if (parent.getChildAt(index) == view)
//                break;
//        }
//        return index;
//    }
//
//    private int getSystemKeyboardHeight() {
//        Rect r = new Rect();
//        this.getWindowVisibleDisplayFrame(r);
//        int screenHeight = this.getRootView().getHeight();
//        Log.e("d","" + screenHeight);
//        return 0;
//    }
}
