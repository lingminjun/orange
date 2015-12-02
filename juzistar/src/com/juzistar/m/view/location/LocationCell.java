package com.juzistar.m.view.location;

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
public class LocationCell extends UITableViewCell {
    public LocationCell(Context context) {
        super(context);
    }

    public LocationCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LocationCell(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate, ViewGroup containerView) {
        return inflate(inflate, R.layout.location_cell, containerView);
    }

    @Override
    protected void onPrepareForReuse() {
    }
}
