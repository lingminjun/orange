package com.orange.m.view.me;

import android.content.Context;
import android.widget.TextView;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class TestCellModel extends UITableViewCell.CellModel {

    public TestCellModel() {
        hiddenRightArrow = true;
    }

    @Override
    protected UITableViewCell createCell(Context context) {
        return new TestCell(context);
    }
}
