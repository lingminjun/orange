package com.juzistar.m.view.pop;

import android.content.Context;
import com.juzistar.m.biz.NoticeBiz;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/11/27.
 */
public class PPBlankCellModel extends UITableViewCell.CellModel {

    public PPBlankCellModel() {
        hiddenSeparateLine = true;
        hiddenRightArrow = true;
        height = 30;
    }

    @Override
    protected UITableViewCell createCell(Context context) {
        return new PPBlankCell(context);
    }

}