package com.juzistar.m.view.me;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.juzistar.m.R;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/11/28.
 */
public class BlankCell extends UITableViewCell {
    public BlankCell(Context context) {
        super(context);
    }

    public BlankCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BlankCell(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate, ViewGroup containerView) {
        return inflate(inflate, R.layout.blank_cell, containerView);
    }

    @Override
    protected void onPrepareForReuse() {

    }
}
