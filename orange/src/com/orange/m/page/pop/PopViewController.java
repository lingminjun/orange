package com.orange.m.page.pop;

import android.os.Bundle;
import com.orange.m.R;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/9/26.
 */
public class PopViewController extends UIViewController {

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle("泡泡");
        tabItem().setTabName("泡泡");
        tabItem().setTabImage(R.drawable.tab_selector_pop);
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();


    }
}
