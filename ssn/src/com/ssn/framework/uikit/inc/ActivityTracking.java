package com.ssn.framework.uikit.inc;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by lingminjun on 15/9/20.
 */
public interface ActivityTracking {
    public void onActivityCreate(Activity activity,Bundle savedInstanceState);
    public void onActivitySaveInstanceState(Activity activity,Bundle outState);
    public void onActivityResume(Activity activity);
    public void onActivityPause(Activity activity);
    public void onActivityDestroy(Activity activity);
}
