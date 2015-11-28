package com.juzistar.m.view.me;

import android.content.Context;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class SettingCellModel extends UITableViewCell.CellModel {
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


    @Override
    protected UITableViewCell createCell(Context context) {
        return new SettingCell(context);
    }

}
