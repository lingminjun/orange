package com.juzistar.m.view.pop;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.juzistar.m.R;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class PPBlankCell extends UITableViewCell {


    public PPBlankCell(Context context) {
        super(context);
    }

    public PPBlankCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PPBlankCell(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate, ViewGroup containerView) {
        return inflate(inflate, R.layout.pp_blank_cell, containerView);
    }

    @Override
    protected void onPrepareForReuse() {

    }

    @Override
    protected void onDisplay(CellModel cellModel, int row) {
        super.onDisplay(cellModel, row);
        displayView().setBackgroundResource(android.R.color.transparent);
    }
}
