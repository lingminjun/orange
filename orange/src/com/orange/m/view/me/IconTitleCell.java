package com.orange.m.view.me;

import android.content.Context;
import android.text.TextUtils;
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
public class IconTitleCell extends UITableViewCell {
    private View view;

    private ImageView mIconView;
    private TextView mTitleView;
    private TextView prompt_tv;
    private TextView newFuncIcon;
    private ImageView mArrowIcon;


    public IconTitleCell(Context context) {
        super(context);
    }

    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate,ViewGroup containerView) {
        view = inflate.inflate(R.layout.icon_title_cell, containerView);
        mIconView = (ImageView) view.findViewById(R.id.icon_image);
        mTitleView = (TextView) view.findViewById(R.id.title_label);
        prompt_tv = (TextView) view.findViewById(R.id.prompt_tv);
        mArrowIcon = (ImageView) view.findViewById(R.id.right_arrow_icon);
        newFuncIcon = (TextView)view.findViewById(R.id.new_func_tip_icon);
        return view;
    }

    @Override
    public void onPrepareForReuse() {
        newFuncIcon.setVisibility(GONE);
    }

    @Override
    protected void onDisplay(CellModel cellModel, int row) {
        super.onDisplay(cellModel, row);

        IconTitleCellModel mEntity = (IconTitleCellModel)cellModel;

        if (mEntity.mIconId > 0) {
            mIconView.setVisibility(View.VISIBLE);
            mIconView.setImageResource(mEntity.mIconId);
        } else {
            mIconView.setVisibility(View.GONE);
        }

        if (mEntity.mTitle != null) {
            mTitleView.setText(mEntity.mTitle);
        } else {
            mTitleView.setText("");
        }

        if (mEntity.mHiddenRightArrow) {
            mArrowIcon.setVisibility(View.GONE);
        } else {
            mArrowIcon.setVisibility(View.VISIBLE);
        }
        int promptTvVisible = TextUtils.isEmpty(mEntity.mPrompt) ? View.GONE : View.VISIBLE;
        prompt_tv.setVisibility(promptTvVisible);
        prompt_tv.setText(mEntity.mPrompt);
        mEntity.paseCusTextView(prompt_tv);

        if (mEntity.isNewFunc) {
            newFuncIcon.setVisibility(VISIBLE);
        }
    }
}
