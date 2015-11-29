package com.juzistar.m.view.me;

import android.content.Context;
import com.juzistar.m.R;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class SettingCellModel extends UITableViewCell.CellModel {

    public static interface SettingCellListener {
        /**
         * 开关点击事件
         * @param model
         * @param switchValue 点击后改变的值
         */
        public void onSwitchChanged(SettingCellModel model, boolean switchValue);
    }

    /**
     * icon id
     */
    public int mIconId;

    /**
     * 标题
     */
    public String mTitle;
    /**
     * 右边内容
     */
    public String mDes;


    /**
     * 开关的值
     */
    public boolean switchValue;

    /**
     * 是开关
     */
    public boolean isSwitch;


    /**
     * 事件
     */
    public SettingCellListener listener;

    @Override
    protected UITableViewCell createCell(Context context) {
        return new SettingCell(context);
    }

    public SettingCellModel() {
        backgroundColor = Res.color(R.color.white);
    }
}
