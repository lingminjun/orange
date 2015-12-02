package com.juzistar.m.view.location;

import android.content.Context;
import com.juzistar.m.view.me.BlankCell;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public class LocationCellModel extends UITableViewCell.CellModel {

    public LocationCellModel() {
        hiddenRightArrow = true;
    }

    @Override
    protected UITableViewCell createCell(Context context) {
        return new LocationCell(context);
    }

}
