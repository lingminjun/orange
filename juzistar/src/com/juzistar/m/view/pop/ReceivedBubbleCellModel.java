package com.juzistar.m.view.pop;

import android.content.Context;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class ReceivedBubbleCellModel extends BubbleCellModel {

    public ReceivedBubbleCellModel() {
        hiddenRightArrow = true;
        hiddenSeparateLine = true;
    }

    @Override
    protected UITableViewCell createCell(Context context) {
        return new ReceivedBubbleCell(context);
    }

}
