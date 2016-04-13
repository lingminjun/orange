package com.juzistar.m.view.chat;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.juzistar.m.R;
import com.juzistar.m.view.com.UIDic;
import com.ssn.framework.foundation.Density;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/11/28.
 */
public class SessionCell extends UITableViewCell {
    public SessionCell(Context context) {
        super(context);
    }

    public SessionCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SessionCell(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    ImageView avatar;
    TextView badge;

    TextView name;
    TextView message;

    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate, ViewGroup containerView) {
        View view = inflate(inflate, R.layout.session_cell, containerView);

        avatar = (ImageView)view.findViewById(R.id.icon_image);
        badge = (TextView)view.findViewById(R.id.badge_label);
        name = (TextView)view.findViewById(R.id.name_label);
        message = (TextView)view.findViewById(R.id.message_label);

        return view;
    }

    @Override
    protected void onPrepareForReuse() {
        badge.setText("");
        badge.setVisibility(GONE);
        name.setText("");
        message.setText("");
    }

    @Override
    protected void onDisplay(CellModel cellModel, int row) {
        super.onDisplay(cellModel, row);

        SessionCellModel model = (SessionCellModel)cellModel;

        if (model.session == null) {return;}

        if (model.session.unreadCount > 0) {
            badge.setVisibility(VISIBLE);
            if (model.isHint) {
                setBadgeRedDot(badge);
            }
            else {
                setBadgeContent(badge,Integer.toString(model.session.unreadCount));
            }
        }

        name.setText(model.session.otherName);
        message.setText(model.session.msg);

        avatar.setBackgroundResource(UIDic.avatarResourceId(model.session.other));
    }

    private static void setBadgeRedDot(TextView tv) {
        tv.setBackgroundResource(com.ssn.framework.R.drawable.white_stroke_red_bg);
        ViewGroup.LayoutParams params = tv.getLayoutParams();
        tv.setText("");
        int size = Density.dip2px(10);
        params.height = size;
        params.width = size;
        if (params instanceof ViewGroup.MarginLayoutParams) {
            ((MarginLayoutParams) params).topMargin = 0;
        }
        tv.setLayoutParams(params);
    }

    private static String badgeValueTidy(String value) {
        if (TextUtils.isEmpty(value)) {return "";}
        int num = -1;
        try {
            num = Integer.parseInt(value);
        }catch (Throwable e) {
            num = -1;
        }

        if (num < 0) {
            return value;
        }
        else {
            if (num >= 100) {//显示太多很丑
                return "99+";
            }
            else {
                return value;
            }
        }
    }

    private static void setBadgeContent(TextView tv,String content){
        tv.setBackgroundResource(com.ssn.framework.R.drawable.white_stroke_red_bg);
        ViewGroup.LayoutParams params = tv.getLayoutParams();
        tv.setText(badgeValueTidy(content));
        params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
//        if (params instanceof ViewGroup.MarginLayoutParams) {
//            ((MarginLayoutParams) params).topMargin = Density.dip2px(-2);
//        }
        tv.setLayoutParams(params);
    }
}
