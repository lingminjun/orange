package com.juzistar.m.page.base;

import android.os.Bundle;
import com.juzistar.m.R;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UITableViewController;

/**
 * Created by lingminjun on 15/11/3.
 * 一些统一的风格在这里定义
 */
public class BaseTableViewController extends UITableViewController {
    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        String title = Res.localized(R.string.app_name);
        navigationItem().setTitle(title);
        navigationItem().setTitleColor(Res.color(R.color.white));
        navigationItem().backItem().setImage(R.drawable.nav_left_icon);
    }


}
