package com.orange.m.view.pop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.orange.m.R;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class ReceivedBubbleCell extends UITableViewCell {
    private View view;

    private ImageView mIconView;
    private TextView mTitleView;
    private ImageView mRightIcon;


    public ReceivedBubbleCell(Context context) {
        super(context);
    }

    @Override
    public void onPrepareForReuse() {
        mTitleView.setText("");;
    }

    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate,ViewGroup containerView) {
        view = inflate(inflate,R.layout.received_bubble_cell, containerView);
        mIconView = (ImageView) view.findViewById(R.id.icon_image);
        mTitleView = (TextView) view.findViewById(R.id.title_label);
        mRightIcon = (ImageView) view.findViewById(R.id.right_icon);
        return view;
    }


    @Override
    protected void onDisplay(CellModel cellModel, int row) {
        super.onDisplay(cellModel, row);

        BubbleCellModel mEntity = (BubbleCellModel)cellModel;

        if (mEntity.message != null) {
            mTitleView.setText(mEntity.message);
        } else {
            mTitleView.setText("");
        }

    }
}
