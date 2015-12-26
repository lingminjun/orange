package com.ssn.framework.foundation;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

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
     * 构造颜色，主要用于控件颜色赋值
     * @param normal
     * @param pressed
     * @param unable
     * @return
     */
    public static ColorStateList colorState(int normal, int pressed, int unable) {
        return colorState(normal, pressed,pressed,unable);
    }

    /**
     * 构造颜色，主要用于控件颜色赋值
     * @param normal
     * @param pressed
     * @param focused
     * @param unable
     * @return
     */
    public static ColorStateList colorState(int normal, int pressed, int focused, int unable) {
        int[] colors = new int[] { pressed, focused, normal, focused, unable, normal };
        int[][] states = new int[6][];
        states[0] = new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled };
        states[1] = new int[] { android.R.attr.state_enabled, android.R.attr.state_focused };
        states[2] = new int[] { android.R.attr.state_enabled };
        states[3] = new int[] { android.R.attr.state_focused };
        states[4] = new int[] { android.R.attr.state_window_focused };
        states[5] = new int[] {};
        ColorStateList colorList = new ColorStateList(states, colors);
        return colorList;
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
     * 取图片
     * @param id
     * @return
     */
    public static Bitmap bitmap(int id) {
        Drawable drawable = image(id);
        if (drawable == null) {return null;}

        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                        : Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0,0,width,height);
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     *
     * @param view
     * @return
     */
    public static Bitmap bitmap(View view) {
        view.destroyDrawingCache();
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        view.setDrawingCacheEnabled(true);
        Bitmap bitmap = view.getDrawingCache(true);
        return bitmap;
    }

    /**
     * 按钮状态背景列表
     * btn.setBackgroundDrawable(imageState(R.drawable.btn_normal, R.drawable.btn_selected));
     * @param idNormal
     * @param idPressed
     * @param idUnable
     * @return
     */
    public static StateListDrawable imageState(int idNormal, int idPressed,int idUnable) {
        return imageState(idNormal,idPressed,idPressed,idUnable);
    }

    /**
     * 按钮状态背景列表
     * btn.setBackgroundDrawable(imageState(R.drawable.btn_normal, R.drawable.btn_selected));
     * @param idNormal
     * @param idPressed
     * @param idFocused
     * @param idUnable
     * @return
     */
    public static StateListDrawable imageState(int idNormal, int idPressed, int idFocused, int idUnable) {
        Context context = Res.context();
        StateListDrawable bg = new StateListDrawable();
        Drawable normal = idNormal == -1 ? null : context.getResources().getDrawable(idNormal);
        Drawable pressed = idPressed == -1 ? null : context.getResources().getDrawable(idPressed);
        Drawable focused = idFocused == -1 ? null : context.getResources().getDrawable(idFocused);
        Drawable unable = idUnable == -1 ? null : context.getResources().getDrawable(idUnable);

        // View.PRESSED_ENABLED_STATE_SET
        bg.addState(new int[] { android.R.attr.state_pressed, android.R.attr.state_enabled }, pressed);
        // View.ENABLED_FOCUSED_STATE_SET
        bg.addState(new int[] { android.R.attr.state_enabled, android.R.attr.state_focused }, focused);
        // View.ENABLED_STATE_SET
        bg.addState(new int[] { android.R.attr.state_enabled }, normal);
        // View.FOCUSED_STATE_SET
        bg.addState(new int[] { android.R.attr.state_focused }, focused);
        // View.WINDOW_FOCUSED_STATE_SET
        bg.addState(new int[] { android.R.attr.state_window_focused }, unable);
        // View.EMPTY_STATE_SET
        bg.addState(new int[] {}, normal);
        return bg;
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

    /**
     * 取meta boolean值
     * @param key
     * @return
     */
    public static final boolean metaBoolean(final String key) {
        String string = metaData(key);
        try {
            return Boolean.parseBoolean(string);
        } catch (Throwable e){e.printStackTrace();}
        return false;
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
