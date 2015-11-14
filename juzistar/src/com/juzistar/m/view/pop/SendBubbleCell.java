package com.juzistar.m.view.pop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.juzistar.m.R;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class SendBubbleCell extends UITableViewCell {
    private View view;

    private View panel;
    private ImageView mIconView;
    private TextView mTitleView;
    private ImageView mRightIcon;


    public SendBubbleCell(Context context) {
        super(context);
    }

    private OnClickListener click = new OnClickListener() {
        @Override
        public void onClick(View view) {

        }
    };

    @Override
    public void onPrepareForReuse() {
        mTitleView.setText("");;
    }

    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate,ViewGroup containerView) {
        view = inflate(inflate,R.layout.send_bubble_cell, containerView);
        panel = view.findViewById(R.id.message_content_panel);
        panel.setOnClickListener(UIEvent.click(click));
        mIconView = (ImageView) view.findViewById(R.id.icon_image);
        mTitleView = (TextView) view.findViewById(R.id.title_label);
        mRightIcon = (ImageView) view.findViewById(R.id.right_icon);
        return view;
    }


    @Override
    protected void onDisplay(CellModel cellModel, int row) {
        super.onDisplay(cellModel, row);

        cellModel.disabled = true;

        SendBubbleCellModel mEntity = (SendBubbleCellModel)cellModel;

        if (mEntity.message != null) {
            mTitleView.setText(mEntity.message);
        } else {
            mTitleView.setText("");
        }

    }
}