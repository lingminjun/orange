package com.juzistar.m.view.me;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.juzistar.m.R;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class SettingCell extends UITableViewCell {

    private TextView mHeaderIcon;
    private TextView mTextLabel;
    private TextView mSubLabel;
    private TextView switchButton;


    public SettingCell(Context context) {
        super(context);
    }

    public OnClickListener switchClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            SettingCellModel model = (SettingCellModel)cellModel();
            if (model != null && model.listener != null) {
                model.switchValue = !model.switchValue;
                model.listener.onSwitchChanged(model,model.switchValue);
            }
        }
    };

    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate,ViewGroup containerView) {
        View view = inflate(inflate,R.layout.setting_cell, containerView);
        mHeaderIcon = (TextView) view.findViewById(R.id.header_icon);
        mTextLabel = (TextView) view.findViewById(R.id.text_label);
        mSubLabel = (TextView) view.findViewById(R.id.sub_label);
        switchButton = (TextView)view.findViewById(R.id.switch_button);
        switchButton.setOnClickListener(UIEvent.click(switchClick));
        return view;
    }

    @Override
    public void onPrepareForReuse() {
        mHeaderIcon.setBackgroundResource(R.color.grey_alpha_40);
        mTextLabel.setText("");
        mSubLabel.setText("");
        switchButton.setVisibility(GONE);
    }


    @Override
    protected void onDisplay(CellModel cellModel, int row) {
        super.onDisplay(cellModel, row);

        SettingCellModel model = (SettingCellModel)cellModel;

        if (model.mIconId != 0) {
            mHeaderIcon.setBackgroundResource(model.mIconId);
        }

        mTextLabel.setText(model.mTitle);

        if (model.isSwitch) {
            model.hiddenRightArrow = true;
            switchButton.setVisibility(VISIBLE);
            switchButton.setBackgroundResource(model.switchValue ? R.drawable.switch_on : R.drawable.switch_off);
        } else {
            model.hiddenRightArrow = false;

            mSubLabel.setText(model.mDes);
        }
    }
}
