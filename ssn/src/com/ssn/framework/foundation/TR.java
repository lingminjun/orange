package com.ssn.framework.foundation;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

/**
 * Created by lingminjun on 15/7/15.
 * 用于一些常用对象变量因为空指针而异常处理
 */
public class TR {
    private static final String EMPTY_STRING = "";

    /**
     * 取字符串
     * @param str
     * @return 返回非空字符串
     */
    public final static String string(String str) {return str != null ? str : EMPTY_STRING;}

    /**
     * 取字符串
     * @param str
     * @param def 默认值
     * @return 返回非空字符串
     */
    public final static String string(String str,String def) {return str != null ? str : (def != null ? def : EMPTY_STRING);}


    /**
     * 取数字
     * @param v
     * @return 返回数字
     */
    public final static int integer(Integer v) {return v != null ? v : 0;}

    /**
     * 取数字
     * @param v
     * @param def 默认值
     * @return 返回数字
     */
    public final static int integer(Integer v, int def) {return v != null ? v : def;}

    /**
     * 取数字
     * @param v
     * @return 返回数字
     */
    public final static long longInteger(Long v) {return v != null ? v : 0;}

    /**
     * 取数字
     * @param v
     * @param def 默认值
     * @return 返回数字
     */
    public final static long longInteger(Long v, long def) {return v != null ? v : def;}


    /**
     * 取字数字
     * @param v
     * @return 返回数字
     */
    public final static boolean bool(Boolean v) {return v != null ? v : false;}

    /**
     * 取字符串中的bool值
     * @param v
     * @return
     */
    public final static boolean bool(String v) {
        return bool(v,false);
    }

    /**
     * 取字符串中的bool值，取不到时返回默认值
     * @param v
     * @param defaultValue
     * @return
     */
    public final static boolean bool(String v, boolean defaultValue) {
        if (TextUtils.isEmpty(v)) {return defaultValue;}
        if ("1".equalsIgnoreCase(v)
                || "yes".equalsIgnoreCase(v)
                || "true".equalsIgnoreCase(v)
                || "on".equalsIgnoreCase(v)) {
            return true;
        }
        return defaultValue;
    }

    /**
     * 转int
     * @param v
     * @param def
     * @return
     */
    public final static int intString(String v, int def) {
        try {
            return Integer.parseInt(v);
        } catch (Throwable e) {}
        return def;
    }

    /**
     * 转long
     * @param v
     * @param def
     * @return
     */
    public final static long longString(String v, long def) {
        try {
            return Long.parseLong(v);
        } catch (Throwable e) {}
        return def;
    }

    /**
     * 转bool
     * @param v
     * @param def
     * @return
     */
    public final static boolean boolString(String v, boolean def) {
        try {
            return Boolean.parseBoolean(v);
        } catch (Throwable e) {}
        return def;
    }

//    public final static Value<? extends Object> value(Object value) {return }
//    private class Value<T extends Object>{
//        private T v(T v) {return v != null ? v : new T();}
//    }

    /**
     * 从intent中取字数字，兼容字符串方式，主要兼容h5传参
     */
    public final static int intExtra(Intent intent, String name, int defaultValue) {
        if (intent == null || TextUtils.isEmpty(name) || !intent.hasExtra(name)) {return defaultValue;}

        try {
            int v = intent.getIntExtra(name, defaultValue);
            if (v != defaultValue) { return v; }

            String str = intent.getStringExtra(name);
            if (!TextUtils.isEmpty(str)) {
                try {
                    return Integer.parseInt(str);
                } catch (Throwable eee) {}
            }
        } catch (Throwable e) {}
        return defaultValue;
    }

    /**
     * 从intent中取字数字，兼容字符串方式，主要兼容h5传参
     * @param intent
     * @param name
     * @param defaultValue
     * @return
     */
    public final static long longExtra(Intent intent, String name, long defaultValue) {
        if (intent == null || TextUtils.isEmpty(name) || !intent.hasExtra(name)) {return defaultValue;}

        try {
            long num = intent.getLongExtra(name, defaultValue);
            if (num != defaultValue) {return num;}

            int v = intent.getIntExtra(name,(int)defaultValue);
            if (v != defaultValue) {return v;}

            String obj  = intent.getStringExtra(name);//兼容h5
            if (!TextUtils.isEmpty(obj)) {
                try {
                    return Long.parseLong(obj);
                } catch (Throwable eee) {}
            }
        } catch (Throwable e) {}
        return defaultValue;
    }

    /**
     * 从intent中取字数字，兼容字符串方式，主要兼容h5传参
     * @param intent
     * @param name
     * @param defaultValue
     * @return
     */
    public final static boolean boolExtra(Intent intent, String name, boolean defaultValue) {
        if (intent == null || TextUtils.isEmpty(name) || !intent.hasExtra(name)) {return defaultValue;}

        try {
            boolean v = intent.getBooleanExtra(name, defaultValue);
            if (v != defaultValue) {
                return v;
            }

            String str = intent.getStringExtra(name);
            if (!TextUtils.isEmpty(str)) {
                try {
                    return bool(str, defaultValue);//做字符串兼容
                } catch (Throwable ee) {}
            }
        } catch (Throwable e) {}
        return defaultValue;
    }


    /**
     * 从intent中取字数字，兼容字符串方式，主要兼容h5传参
     */
    public final static int intExtra(Bundle bundle, String name, int defaultValue) {
        if (bundle == null || TextUtils.isEmpty(name) || !bundle.containsKey(name)) {return defaultValue;}
        Object obj = bundle.get(name);
        try {
            return Integer.parseInt(obj.toString());
        }catch (Exception e){
            return defaultValue;
        }
    }

    /**
     * 从intent中取字数字，兼容字符串方式，主要兼容h5传参
     * @param bundle
     * @param name
     * @param defaultValue
     * @return
     */
    public final static long longExtra(Bundle bundle, String name, long defaultValue) {
        if (bundle == null || TextUtils.isEmpty(name) || !bundle.containsKey(name)) {return defaultValue;}
        Object obj = bundle.get(name);
        try {
            return Long.parseLong(obj.toString());
        }catch (Exception e){
            return defaultValue;
        }
    }

    /**
     * 从intent中取字数字，兼容字符串方式，主要兼容h5传参
     * @param bundle
     * @param name
     * @param defaultValue
     * @return
     */
    public final static boolean boolExtra(Bundle bundle, String name, boolean defaultValue) {
        if (bundle == null || TextUtils.isEmpty(name) || !bundle.containsKey(name)) {return defaultValue;}
        Object obj = bundle.get(name);
        try {
            return bool(obj.toString(),defaultValue);
        }catch (Exception e){
            return defaultValue;
        }
    }

}
