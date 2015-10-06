package com.ssn.framework.foundation;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

/**
 * 应用相关特性，可以在这里找到
 * Created by lingminjun on 15/10/6.
 */
public final class App {

    /**
     * 简单的toast提示
     * @param content
     */
    public static void toast(String content) {
        if (TextUtils.isEmpty(content)) {return;}
        Toast.makeText(Res.context(), content, Toast.LENGTH_SHORT).show();
    }

    /**
     * 当前进程id
     * @return
     */
    public static int pid() {
        return android.os.Process.myPid();//获取当前进程
    }

    /**
     * 是否为首次安装
     * @return
     */
    public static boolean isFirstInstall() {
        checkUpgraded();
        return _upgraded == 2;
    }

    /**
     * 是否为升级版本，首次安装同样被视为升级版本
     * @return
     */
    public static boolean isUpgraded() {
        checkUpgraded();
        return _upgraded > 0;
    }

    private static volatile boolean _checked = false;
    private static int _upgraded = 0;//0表示非更新，1表示升级，2表示首次安装
    private static final String _upgraded_version_key = "_app_upgraded_version_key";
    private static void checkUpgraded() {
        if (_checked) {return;}

        String v1 = UserDefaults.getInstance().get(_upgraded_version_key,null);
        String v2 = Res.appVersion();
        UserDefaults.getInstance().put(_upgraded_version_key,v2);

        if (v1 == null) {
            _upgraded = 2;
        }
        else if (!v1.equals(v2)) {
            _upgraded = 1;
        }

        _checked = true;
    }

    public static boolean isBackground() {
        return _background;
    }

    private static boolean _background = false;
    public static void terminate() {
        _background = true;
        _checked = false;
    }

    private static Runnable _runnable = null;
    public static void checkEnterBackground(final boolean terminate) {
        //三秒后，进入后台，如果没有新的activity出现，将直接进入后台
        _runnable = new Runnable() {
            @Override
            public void run() {
                if (terminate) {
                    terminate();
                }
                else {
                    _background = true;
                }
            }
        };

        TaskQueue.mainQueue().executeDelayed(_runnable,3000);
    }

    public static void checkEnterFront() {
        TaskQueue.mainQueue().cancel(_runnable);
        _background = false;
        _runnable = null;
    }
}
