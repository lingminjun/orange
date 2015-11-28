package com.juzistar.m.view.me;

import android.content.Context;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class BlankCellModel extends UITableViewCell.CellModel {

    @Override
    protected UITableViewCell createCell(Context context) {
        return new BlankCell(context);
    }

}
