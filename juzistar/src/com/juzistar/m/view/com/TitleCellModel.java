package com.juzistar.m.view.com;

import android.content.Context;
import com.juzistar.m.R;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class TitleCellModel extends UITableViewCell.CellModel {

    public String title;
    public int imageId;
    public String subTitle;

    public Object data;//具体数据

    public TitleCellModel() {
        super();
        backgroundColor = Res.color(R.color.white);
    }

    @Override
    protected UITableViewCell createCell(Context context) {
        return new TitleCell(context);
    }

}
