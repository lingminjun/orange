package com.orange.m.page.user;

import com.orange.m.R;
import com.orange.m.page.base.BaseViewController;
import com.ssn.framework.foundation.Res;

/**
 * Created by lingminjun on 15/11/8.
 */
public class EditHeaderViewController extends BaseViewController {
    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        containerView().setBackgroundColor(Res.color(R.color.red_text));
    }
}
