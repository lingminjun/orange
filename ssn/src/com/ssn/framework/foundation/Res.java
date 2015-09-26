package com.ssn.framework.foundation;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;

/**
 * Created by lingminjun on 15/7/11.
 */
public class Res {

    private static Application _application;
    public static boolean initialization(Application application) {
        if (_application != null) {
            Log.e("", "Res 初始化方法 务必在Application onCreate方法中调用，调用一次后失效！！！");
            return false;
        }

        if (application == null) {
            Log.e("", "Res 初始化方法 务必在Application onCreate方法中调用，调用一次后失效！！！");
            return false;
        }

        _application = application;

        return true;
    }

    /**
     * 获取文案（本地化文案）
     * @param id
     * @return
     */
    public static String localized(int id) {
        return application().getString(id);
    }

    public static String localized(int resId, Object... formatArgs){
        return application().getString(resId, formatArgs);
    }

    /**
     * 获取颜色，主要用于控件颜色赋值
     * @param id
     * @return
     */
    public static int color(int id) {
        return application().getResources().getColor(id);
    }

    /**
     * 获取颜色，主要用于控件颜色赋值
     * @param id
     * @return
     */
    public static ColorStateList colorState(int id) {
        return application().getResources().getColorStateList(id);
    }

    /**
     * 获取图片
     * @param id
     * @return
     */
    public static Drawable image(int id) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            return application().getResources().getDrawable(id, null);
        } else {
            return application().getResources().getDrawable(id);
        }
    }

    /**
     * 获取资源包
     * @return
     */
    public static Resources resources() {
        return application().getResources();
    }

    /**
     * 获取上下文
     * @return
     */
    public static Context context() {
        return application();
    }


    /**
     * 当前进程id
     * @return
     */
    public static int pid() {
       return android.os.Process.myPid();//获取当前进程
    }

    /**
     * 取 version name
     * @return
     */
    public static final String appVersion() {
        PackageManager pm = application().getPackageManager();
        PackageInfo pi = null;
        String version = null;
        try {
            pi = pm.getPackageInfo(application().getPackageName(), PackageManager.GET_INSTRUMENTATION);
            version = pi.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return TR.string(version);
    }

    /**
     * 取 build 号，等同于version code
     * @return
     */
    public static final long buildNumer() {
        PackageManager pm = application().getPackageManager();
        PackageInfo pi = null;
        int number = 0;
        try {
            pi = pm.getPackageInfo(application().getPackageName(), PackageManager.GET_INSTRUMENTATION);
            number = pi.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return number;
    }

    /**
     * 取包名
     * @return
     */
    public static final String packageName() {
        return application().getPackageName();
    }

    /**
     * 获取预信息获取，配置在Manifest的Application meta-data中的数据
     * @param key
     * @return
     */
    public static final String metaData(final String key) {
        if (TextUtils.isEmpty(key)) {
            return TR.string(null);
        }
        try {
            ApplicationInfo appInfo = application().getPackageManager().getApplicationInfo(packageName(), PackageManager.GET_META_DATA);

            Object obj = appInfo.metaData.get(key);
            if (obj == null) {
                return TR.string(null);
            }
            if (obj instanceof String) {
                return (String)obj;
            }
            else {
                return obj.toString();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return TR.string(null);
    }

    private static Application application() {
        if (_application == null) {
            Log.e("","必须在 application 启动中初始化Res");
            System.exit(-1);
        }
        return _application;
    }


    /**
     * 删除此时间点以前的缓存文件
     * @param outdated (毫秒)
     */
    public static void clearApplicationCache(long outdated) {
        File dir = Res.context().getCacheDir();
        clearFolder(dir, outdated);
    }

    // clear the cache before time numDays
    private static void clearFolder(File dir, long numDays) {
        if (dir!= null && dir.isDirectory()) {
            try {
                for (File child:dir.listFiles()) {
                    if (child.isDirectory()) {
                        clearFolder(child, numDays);
                    }
                    if (child.lastModified() < numDays) {
                        child.delete();
                    }
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 清除WebView缓存
     */
    public static void clearWebViewCache(){

        //清理Webview缓存数据库
        Context context = Res.context();
        try {
            context.deleteDatabase("webview.db");
            context.deleteDatabase("webviewCache.db");
        } catch (Exception e) {
            e.printStackTrace();
        }

        //WebView 缓存文件
        File appCacheDir = new File(context.getFilesDir().getAbsolutePath()+"/webcache");
        Log.e("res", "appCacheDir path=" + appCacheDir.getAbsolutePath());

        File webviewCacheDir = new File(context.getCacheDir().getAbsolutePath()+"/webviewCache");
        Log.e("res", "webviewCacheDir path="+webviewCacheDir.getAbsolutePath());

        //删除webview 缓存目录
        if(webviewCacheDir.exists()){
            context.deleteFile(webviewCacheDir.getAbsolutePath());
        }

        //删除webview 缓存 缓存目录
        if(appCacheDir.exists()){
            context.deleteFile(appCacheDir.getAbsolutePath());
        }
    }
}
