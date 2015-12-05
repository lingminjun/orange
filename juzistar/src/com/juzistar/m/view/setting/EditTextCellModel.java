package com.juzistar.m.view.setting;

import android.content.Context;
import com.juzistar.m.view.me.TestCell;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class EditTextCellModel extends UITableViewCell.CellModel {

    public String placeholder;
    public String text;

    public EditTextCellModel() {
        super(true);
        hiddenRightArrow = true;
    }

    @Override
    protected UITableViewCell createCell(Context context) {
        return new EditTextCell(context);
    }
}
