package com.juzistar.m.view.me;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.juzistar.m.R;
import com.juzistar.m.Utils.Utils;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.page.PageCenter;
import com.juzistar.m.page.PageURLs;
import com.juzistar.m.view.com.RoundAngleImageView;
import com.ssn.framework.foundation.Density;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.foundation.TaskQueue;
import com.ssn.framework.uikit.Navigator;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/11/28.
 */
public class UserHeaderCell extends UITableViewCell {
    public UserHeaderCell(Context context) {
        super(context);
    }

    public UserHeaderCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UserHeaderCell(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private static final String AVATAR_SAVED_NAME = "temp_ava.png";

    private ImageView mBackgroudImage;
    private ImageView mBackgroudImageAlpha;

    private RoundAngleImageView mAvatarImage;
    private TextView mNickLabel;
    private TextView mLoginButton;

    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate, ViewGroup containerView) {
        View view = inflate(inflate, R.layout.user_header_cell, containerView);
        mBackgroudImage = (ImageView) view.findViewById(R.id.header_backgroud_view);
        mBackgroudImageAlpha = (ImageView) view.findViewById(R.id.header_backgroud_view_alpha);
        mAvatarImage = (RoundAngleImageView) view.findViewById(R.id.avatar_image);
        mNickLabel = (TextView) view.findViewById(R.id.nick_label);
        mLoginButton = (TextView)view.findViewById(R.id.login_btn);
        mLoginButton.setOnClickListener(UIEvent.click(loginClick));
        return view;
    }

    OnClickListener loginClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            Navigator.shareInstance().openURL(PageURLs.LOGIN_URL, null, true);
        }
    };

    @Override
    protected void onPrepareForReuse() {
        mNickLabel.setText("");
        mNickLabel.setVisibility(VISIBLE);
        mLoginButton.setVisibility(GONE);
    }

    private boolean isInit;

    @Override
    protected void onDisplay(CellModel cellModel, int row) {
        super.onDisplay(cellModel, row);

        //设置用户名
        UserCenter.User user = UserCenter.shareInstance().user();
        if (user.nick != null) {
            mNickLabel.setText(user.nick);
        }

        if (UserCenter.shareInstance().isLogin()) {
            mNickLabel.setVisibility(VISIBLE);
            mLoginButton.setVisibility(GONE);
        } else {
            mNickLabel.setVisibility(GONE);
            mLoginButton.setVisibility(VISIBLE);
        }

        if (isInit) {return;}
        isInit = true;

        //设置默认头像
        mAvatarImage.setImageResource(R.drawable.default_avatar);
        mBackgroudImage.setImageResource(R.drawable.header_background);
        mBackgroudImageAlpha.setVisibility(View.GONE);

//        TaskQueue.commonQueue().execute(new Runnable() {
//            @Override
//            public void run() {
//                Bitmap bitmap = Res.bitmap(R.drawable.default_avatar);
//                final Bitmap gaussian = Utils.fastblur(bitmap, 50);
//
//                if (gaussian != null) {
//                    TaskQueue.mainQueue().execute(new Runnable() {
//                        @Override
//                        public void run() {
//                            mBackgroudImage.setImageDrawable(new BitmapDrawable(getResources(), gaussian));
//                            mBackgroudImageAlpha.setVisibility(View.VISIBLE);
//                        }
//                    });
//                }
//            }
//        });
    }
}
