package com.orange.m.view.pop;

import android.content.Context;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class BubbleCellModel extends UITableViewCell.CellModel {

    public String message;

    public BubbleCellModel() {
        hiddenRightArrow = true;
        hiddenSeparateLine = true;
    }

    @Override
    protected UITableViewCell createCell(Context context) {
        return new ReceivedBubbleCell(context);
    }

}
