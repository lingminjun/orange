package com.juzistar.m.view.me;

import android.content.Context;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/11/28.
 */
public class UserHeaderCellModel extends UITableViewCell.CellModel {

    public UserHeaderCellModel() {
        hiddenRightArrow = true;
    }

    @Override
    protected UITableViewCell createCell(Context context) {
        return new UserHeaderCell(context);
    }
}
