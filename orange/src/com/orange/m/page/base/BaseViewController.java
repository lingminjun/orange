package com.orange.m.page.base;

import android.os.Bundle;
import com.orange.m.R;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/11/3.
 */
public class BaseViewController extends UIViewController {

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        String title = Res.localized(R.string.app_name);
        navigationItem().setTitle(title);
        navigationItem().setTitleColor(Res.color(R.color.white));
    }
}
