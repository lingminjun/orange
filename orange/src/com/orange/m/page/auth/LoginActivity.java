package com.orange.m.page.auth;

import android.os.Bundle;
import com.orange.m.R;
import com.ssn.framework.uikit.BaseNavActivity;

/**
 * Created by lingminjun on 15/9/26.
 */
public class LoginActivity extends BaseNavActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.base_stay_orig, R.anim.activity_push_bottom_out);
    }
}
