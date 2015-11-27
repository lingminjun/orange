package com.juzistar.m.view.com;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.juzistar.m.R;

/**
 * Created by lingminjun on 15/11/27.
 */
public class KeyboardButton extends LinearLayout {
    public KeyboardButton(Context context) {
        super(context);
        init(context, null, 0, 0);
    }

    public KeyboardButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0, 0);
    }

    public KeyboardButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public KeyboardButton(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    private TextView _icon;
    private TextView _title;
    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.setOrientation(LinearLayout.HORIZONTAL);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.sub_keyboard_button, this);

        _icon = (TextView)findViewById(R.id.icon_image);
        _title = (TextView)findViewById(R.id.tag_name);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.keyboard_button, defStyleAttr, defStyleRes);
        int iconId = a.getResourceId(R.styleable.keyboard_button_icon, 0);
        if (iconId != 0) {
            _icon.setBackgroundResource(iconId);
        }

        String title = a.getString(R.styleable.keyboard_button_title);
        if (!TextUtils.isEmpty(title)) {
            _title.setText(title);
        }
    }

    private int key;
    public void setKey(int key) {
        this.key = key;
    }
    public int key() {return key;}
}
