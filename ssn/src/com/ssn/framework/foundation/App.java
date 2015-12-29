package com.ssn.framework.foundation;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
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
     * 是否为首次安装，在app启动之后，此状态一直不变
     * @return
     */
    public static boolean isFirstInstall() {
        checkUpgraded();
        return _upgraded == 2;
    }

    /**
     * 是否为升级版本，首次安装同样被视为升级版本，在app启动之后，此状态一直不变
     * @return
     */
    public static boolean isUpgraded() {
        checkUpgraded();
        return _upgraded > 0;
    }


    public static boolean isStayedBackground() {
        return _stayed_background;
    }

    private static boolean _stayed_background = false;
    private static volatile boolean _checked = false;
    private static int _upgraded = 0;//0表示非更新，1表示升级，2表示首次安装
    private static final String _upgraded_version_key = "_app_upgraded_version_key";
    private static void checkUpgraded() {
        if (_checked) {return;}

        String v1 = UserDefaults.getInstance().get(_upgraded_version_key,(String)null);
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

    /**
     * 当前应用是否在后台
     * @return
     */
    public static boolean isBackground() {
        return _background;
    }

    private static boolean _background = false;
    public static void terminate() {
        _terminate = true;
        _background = true;
        _stayed_background = true;
        _checked = false;

        //是否需要真的退出引用，最好是启动推送服务的service
        try {
            android.os.Process.killProcess(android.os.Process.myPid());
        } catch (Throwable e) {System.exit(0);}

//        ActivityManager activityMgr = (ActivityManager) Res.context().getSystemService(Context.ACTIVITY_SERVICE);
//        /**
//         * 需要此协议，因为只杀掉自己进程，可以不设置，android.permission.KILL_BACKGROUND_PROCESSES
//         * activityMgr.restartPackage(Res.context().getPackageName());
//         */
//        activityMgr.killBackgroundProcesses(Res.packageName());
    }

    private static Runnable _runnable = new Runnable() {
        @Override
        public void run() {
            if (_runnable == null || _runnable != this) {return;}

            if (_terminate) {
                terminate();
            }
            else {
                _background = true;
                _stayed_background = true;
            }
        }
    };

    private static boolean _terminate = false;
    public static void checkEnterBackground(final boolean terminate) {
        //三秒后，进入后台，如果没有新的activity出现，将直接进入后台
        if (terminate && !_terminate) {
            _terminate = terminate;
        }

        TaskQueue.mainQueue().cancel(_runnable);
        TaskQueue.mainQueue().executeDelayed(_runnable,3000);
    }

    public static void checkEnterFront() {
        TaskQueue.mainQueue().cancel(_runnable);
        _background = false;
    }

    /**
     * 消息提示音
     */
    public static void ringtone() {
        //Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        //alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        //alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(Res.context(), notification);
        r.play();
    }
}
