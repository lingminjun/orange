package com.orange.m.view.me;

import android.content.Context;
import android.widget.TextView;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class IconTitleCellModel extends UITableViewCell.CellModel {
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
    public String mPrompt;

    /**
     * 隐藏右边箭头
     */
    public boolean mHiddenRightArrow;

    //新功能提示
    public boolean isNewFunc;

    public IconTitleCellModel() {
        mHiddenRightArrow = true;
        height = 60;
    }

    @Override
    protected UITableViewCell createCell(Context context) {
        return new IconTitleCell(context);
    }

    public void paseCusTextView(TextView prompt_tv){}
}
