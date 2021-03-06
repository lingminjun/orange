/*
 * 文件名：SharedPreferenceService.java
 * 描述：SharedPreference管理类
 * 修改人：admin
 * 修改时间：2013-6-4 上午9:36:46
 */
package com.ssn.framework.foundation;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Base64;

/**
 * 引用默认值设置
 */
public final class UserDefaults {

    SharedPreferences sp;
    SharedPreferences.Editor editor;
    private Context mContext = null;

    private UserDefaults(Context context) {
        mContext = context.getApplicationContext();
        sp = mContext.getSharedPreferences("app_defaults", 0);
        editor = sp.edit();
    }

    private static class Singleton {
        private static final UserDefaults INSTANCE = new UserDefaults(Res.context());
    }

    /**
     * 唯一实例
     * @return
     */
    public static final UserDefaults getInstance() {
        return Singleton.INSTANCE;
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

    public boolean put(String key, float value) {
        editor.putFloat(key, value);
        return editor.commit();
    }

    public void putAsync(String key, float value) {
        editor.putFloat(key, value);
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

    public float get(String key, float defValue) {
        return sp.getFloat(key, defValue);
    }

    //高级接口
    public String getJSONString(String key) {
        if (TextUtils.isEmpty(key)) {return null;}

        String tokenCry = get(key, "");
        if (!TextUtils.isEmpty(tokenCry)) {
            try {
                byte[] bytes = Base64.decode(tokenCry.getBytes(), Base64.DEFAULT);
                if (bytes != null) {
                    return new String(bytes);
                }
            } catch (Throwable e) { APPLog.error(e); }
        }
        return null;
    }

    public void putJSONString(String key,String json) {
        if (TextUtils.isEmpty(key)) {return;}

        if (TextUtils.isEmpty(json)) {//清除
            putAsync(key, "");//清除
        }
        else {
            try {
                byte[] bytes = Base64.encode(json.getBytes(), Base64.DEFAULT);
                if (bytes != null) {
                    String value = new String(bytes);
                    putAsync(key, value);//清除
                }
            } catch (Throwable e) {APPLog.error(e);}
        }
    }

}
