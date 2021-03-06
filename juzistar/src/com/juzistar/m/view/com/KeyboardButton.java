package com.juzistar.m.view.com;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
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

    private View _buttonPanel;
    private TextView _icon;
    private TextView _title;
    private int _selected_background;

    private void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.setOrientation(LinearLayout.HORIZONTAL);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.sub_keyboard_button, this);

        _buttonPanel = findViewById(R.id.button_panel);
        _icon = (TextView)findViewById(R.id.icon_image);
        _title = (TextView)findViewById(R.id.tag_name);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.keyboard_button, defStyleAttr, defStyleRes);
        int iconId = a.getResourceId(R.styleable.keyboard_button_icon, 0);
        _selected_background = a.getResourceId(R.styleable.keyboard_button_select_background,R.drawable.green_strock_white_bg);
        if (iconId != 0) {
            _icon.setBackgroundResource(iconId);
        }

        String title = a.getString(R.styleable.keyboard_button_title);
        if (!TextUtils.isEmpty(title)) {
            _title.setText(title);
        }

        boolean selected = super.isSelected();
        _buttonPanel.setBackgroundResource(selected ? _selected_background : R.drawable.grey_strock_white_bg);
    }

    private int key;
    public void setKey(int key) {
        this.key = key;
    }
    public int key() {return key;}

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (_buttonPanel != null) {
            _buttonPanel.setBackgroundResource(selected ? _selected_background : R.drawable.grey_strock_white_bg);
        }
    }
}
