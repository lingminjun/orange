package com.juzistar.m.view.pop;

import android.content.Context;
import android.text.TextUtils;
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
public class SendBubbleCell extends UITableViewCell {
    private View view;

    private View panel;
    private ImageView mIconView;
    private TextView mTitleView;
    private TextView mLeftIcon;
    private TextView mStatusIcon;


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
        mTitleView.setText("");
        mLeftIcon.setVisibility(GONE);
        mStatusIcon.setVisibility(GONE);
    }

    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate,ViewGroup containerView) {
        view = inflate(inflate,R.layout.send_bubble_cell, containerView);
        panel = view.findViewById(R.id.message_content_panel);
        panel.setOnClickListener(UIEvent.click(click));
        mIconView = (ImageView) view.findViewById(R.id.icon_image);
        mTitleView = (TextView) view.findViewById(R.id.title_label);
        mLeftIcon = (TextView) view.findViewById(R.id.left_icon);
        mStatusIcon = (TextView) view.findViewById(R.id.send_status_icon);
        return view;
    }


    @Override
    protected void onDisplay(CellModel cellModel, int row) {
        super.onDisplay(cellModel, row);

        SendBubbleCellModel mEntity = (SendBubbleCellModel)cellModel;

        if (mEntity.notice == null) {return;}

        //设置内容
        mTitleView.setText(TR.string(mEntity.notice.content));

        //头像
        mIconView.setBackgroundResource(UIDic.avatarResourceId(mEntity.notice.creatorId));

        int resId = UIDic.bubbleResourceId(mEntity.notice.type,true);
        if (resId != 0) {
            mLeftIcon.setVisibility(VISIBLE);
            mLeftIcon.setBackgroundResource(resId);

            //文案
            mLeftIcon.setText(UIDic.bubbleTagResourceId(mEntity.notice.type));
        }

        //发送失败
        if (TextUtils.isEmpty(mEntity.notice.id)) {
            mStatusIcon.setVisibility(VISIBLE);
            mStatusIcon.setBackgroundResource(R.drawable.error_icon);
        }
//        else if (mEntity.notice.id.startsWith("sending:")) {
//            mStatusIcon.setVisibility(VISIBLE);
//            mStatusIcon.setBackgroundResource(R.drawable.error_icon);//发送中
//        }
    }
}
