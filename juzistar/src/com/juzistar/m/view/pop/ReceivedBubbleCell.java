package com.juzistar.m.view.pop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.juzistar.m.R;
import com.juzistar.m.view.com.UIDic;
import com.ssn.framework.foundation.TR;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class ReceivedBubbleCell extends UITableViewCell {
    private View view;

    private View panel;
    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mRightIcon;


    public ReceivedBubbleCell(Context context) {
        super(context);
    }

    private OnClickListener click = new OnClickListener() {
        @Override
        public void onClick(View view) {
            ReceivedBubbleCellModel model = (ReceivedBubbleCellModel)cellModel();
            if (model != null && model.cellListener != null) {
                model.cellListener.onMessageClick(model, model.notice);
            }
        }
    };

    private OnClickListener headerClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            ReceivedBubbleCellModel model = (ReceivedBubbleCellModel)cellModel();
            if (model != null && model.cellListener != null) {
                model.cellListener.onHeaderClick(model, model.notice);
            }
        }
    };

    @Override
    public void onPrepareForReuse() {
        mTitleView.setText("");
        mRightIcon.setVisibility(GONE);
    }

    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate,ViewGroup containerView) {
        view = inflate(inflate,R.layout.received_bubble_cell, containerView);
        panel = view.findViewById(R.id.message_content_panel);
        panel.setOnClickListener(UIEvent.click(click));
        mIconView = (ImageView) view.findViewById(R.id.icon_image);
        mIconView.setOnClickListener(UIEvent.click(headerClick));
        mTitleView = (TextView) view.findViewById(R.id.title_label);
        mRightIcon = (TextView) view.findViewById(R.id.right_icon);
        return view;
    }


    @Override
    protected void onDisplay(CellModel cellModel, int row) {
        super.onDisplay(cellModel, row);

        ReceivedBubbleCellModel mEntity = (ReceivedBubbleCellModel)cellModel;

        if (mEntity.notice == null) {return;}

        //设置内容
        mTitleView.setText(TR.string(mEntity.notice.content));

        //头像
        mIconView.setBackgroundResource(UIDic.avatarResourceId(mEntity.notice.creatorId));

        int resId = UIDic.bubbleResourceId(mEntity.notice.category,false);
        if (resId != 0) {
            mRightIcon.setVisibility(VISIBLE);
            mRightIcon.setBackgroundResource(resId);

            //文案
            mRightIcon.setText(UIDic.bubbleTagResourceId(mEntity.notice.category));
        }

        //透明度设置
        view.setAlpha(mEntity.getAlpha());

    }
}
