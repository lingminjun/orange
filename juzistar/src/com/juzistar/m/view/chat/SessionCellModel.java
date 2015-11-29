package com.juzistar.m.view.chat;

import android.content.Context;
import com.juzistar.m.biz.msg.MessageCenter;
import com.juzistar.m.view.me.BlankCell;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class SessionCellModel extends UITableViewCell.CellModel {

    public MessageCenter.Session session;
    public boolean isHint;//若提示

    public SessionCellModel() {
        hiddenRightArrow = true;
        separateLineLeftPadding = 15;
        isHint = true;
    }

    @Override
    protected UITableViewCell createCell(Context context) {
        return new SessionCell(context);
    }

}
