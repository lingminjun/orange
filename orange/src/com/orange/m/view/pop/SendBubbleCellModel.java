package com.orange.m.view.pop;

import android.content.Context;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class SendBubbleCellModel extends UITableViewCell.CellModel {

    public String message;

    public SendBubbleCellModel() {
        hiddenRightArrow = true;
        hiddenSeparateLine = true;
        disabled = true;
    }

    @Override
    protected UITableViewCell createCell(Context context) {
        return new SendBubbleCell(context);
    }

}
