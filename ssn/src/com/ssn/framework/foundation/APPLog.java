package com.ssn.framework.foundation;

import android.text.TextUtils;
import android.util.Log;

/**
 * 一句话功能简述<br>
 * 功能详细描述
 *
 * @version 1.0
 * @author： RWJ
 */
public class APPLog {
    private static boolean DEBUG = true;
    private final static String TAG = "ssn";

    /**
     *配置APPLog 运行 环境
     * @param debug
     */
    public static void configAPPLog(boolean debug) {
        DEBUG = debug;
    }

    /**
     * 基本日志接口
     *
     * @param tag
     * @param msg
     * @param level
     */
    public static void log(String tag, String msg, int level) {
        if (isDebug()) {
            String msgs = msg;
            if (TextUtils.isEmpty(msg)) {
                msgs = "Null Point Exception";
            }
            String tags = tag;
            Exception e = new Exception();
            StackTraceElement[] els = e.getStackTrace();

            String logDetails = Res.packageName();
            for (int i = 0, l = els.length; i < l; i++) {
                if (els[i].getClassName().startsWith(logDetails)
                        && !els[i].getClassName().equals(APPLog.class.getName())) {
                    logDetails = els[i].getFileName() + "->" + els[i].getMethodName() + ":" + els[i].getLineNumber()
                            + " ";
                    msgs = logDetails + msgs;
                    if ("".equals(tags)) {
                        tags = els[i].getFileName().substring(0, els[i].getFileName().indexOf("."));
                    }
                    break;
                }
            }
            switch (level) {
                case Log.DEBUG:
                    Log.d(tags, msgs);
                    break;
                case Log.INFO:
                    Log.i(tags, msgs);
                    break;
                case Log.WARN:
                    Log.w(tags, msgs);
                    break;
                case Log.ERROR:
                    Log.e(tags, msgs);
                    break;
                default:
                    Log.d(tag, msgs);
                    break;
            }
        }
    }

    /**
     * Simple log
     *
     * @param tag
     * @param msg
     */
    public static void log(String tag, String msg) {
        log(tag, msg, Log.WARN);
    }

    public static void log(String msg) {
        log(TAG, msg);
    }

    public static void info(String msg) {
        log(TAG, msg, Log.INFO);
    }

    public static void error(String msg) {
        log(TAG, msg, Log.ERROR);
    }

    public static void error(Throwable e) {
        log(TAG, e.toString(), Log.ERROR);
        if (APPLog.DEBUG) {
            e.printStackTrace();
            throw (RuntimeException)e;
        }
    }

    public static void debug(String msg) {
        log(TAG, msg, Log.DEBUG);
    }

    public static void error(String tag, String msg) {
        log(tag, msg, Log.ERROR);
    }

    public static void d(String tag, String msg, Throwable tr) {
        if (isDebug()) {
            Log.d(tag, msg, tr);
        }
    }

    public static void v(String tag, String msg, Throwable tr) {
        if (isDebug()) {
            Log.v(tag, msg, tr);
        }
    }

    public static void i(String tag, String msg, Throwable tr) {
        if (isDebug()) {
            Log.i(tag, msg, tr);
        }
    }

    public static void w(String tag, String msg, Throwable tr) {
        if (isDebug()) {
            Log.w(tag, msg, tr);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {
        if (isDebug()) {
            Log.e(tag, msg, tr);
        }
    }

    private static boolean inited = false;

    public static boolean isDebug() {
//        if (inited)
//            return DEBUG;
//        inited = true;
        return DEBUG;
    }
}
