/*
 * 文件名：SharedPreferenceService.java
 * 描述：SharedPreference管理类
 * 修改人：admin
 * 修改时间：2013-6-4 上午9:36:46
 */
package com.ssn.framework.foundation;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 引用默认值设置
 */
public final class UserDefaults {

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private Context mContext = null;

    private static UserDefaults manager = null;

    private UserDefaults(Context context) {
        mContext = context.getApplicationContext();
        sp = mContext.getSharedPreferences("app_defaults", 0);
        editor = sp.edit();
    }

    public static UserDefaults getInstance() {
        if (manager != null) {
            return manager;
        }
        synchronized (UserDefaults.class) {
            if (manager != null) {
                return manager;
            }
            manager = newInstance();
            return manager;
        }
    }

    private static UserDefaults newInstance() {
        return new UserDefaults(Res.context());
    }

    /**
     * 同步的操作，如果不需要返回值建议使用putAsync
     *
     * @param key
     * @param value
     * @return
     */
    public boolean put(String key, String value) {
        editor.putString(key, value);
        return editor.commit();
    }

    public void putAsync(String key, String value) {
        editor.putString(key, value);
        editor.apply();
    }

    public boolean put(String key, int value) {
        editor.putInt(key, value);
        return editor.commit();
    }

    public void putAsync(String key, int value) {
        editor.putInt(key, value);
        editor.apply();
    }

    public boolean put(String key, long value) {
        editor.putLong(key, value);
        return editor.commit();
    }

    public void putAsync(String key, long value) {
        editor.putLong(key, value);
        editor.apply();
    }

    public boolean put(String key, boolean value) {
        editor.putBoolean(key, value);
        return editor.commit();
    }

    public void putAsync(String key, boolean value) {
        editor.putBoolean(key, value);
        editor.apply();
    }

    public String get(String key, String defValue) {
        return sp.getString(key, defValue);
    }

    public int get(String key, int defValue) {
        return sp.getInt(key, defValue);
    }

    public long get(String key, long defValue) {
        return sp.getLong(key, defValue);
    }

    public boolean get(String key, boolean defValue) {
        return sp.getBoolean(key, defValue);
    }

}
