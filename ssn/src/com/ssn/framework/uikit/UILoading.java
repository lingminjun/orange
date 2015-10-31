package com.ssn.framework.uikit;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import com.ssn.framework.R;
import com.ssn.framework.foundation.APPLog;

import java.util.HashMap;
import java.util.Set;

/**
 * 用于控制显示loading，每个context只有一个菊花
 * Created by lingminjun on 15/6/25.
 */
public class UILoading {

    /**
     * 对activity支持
     * @param activity
     */
    public static void show(Activity activity) {
        show(activity, true);
    }
    public static void show(Activity activity, boolean cancelAble) {
        show(activity, cancelAble, "loading...");
    }
    public static void show(Activity activity, boolean cancelAble, String msg) {
        if (activity == null) {
            return;
        }
        pshow(activity,cancelAble,msg);
    }

    private static void pshow(Context context, boolean cancelAble, String msg) {
        /**
         * check是否在主线程
         */
        if (APPLog.isDebug() && Thread.currentThread() != Looper.getMainLooper().getThread()) {
            Thread thread = Thread.currentThread();
            APPLog.error("不能在子线程中调用progress"+thread.toString());
            System.exit(-1);
        }

        int key = context.hashCode();
        HashMap<Integer,ProgressDialog> dialogs = getDialogs();

        ProgressDialog progress = dialogs.get(key);
        if (progress == null) {
            progress = new ProgressDialog();
        }

        progress.show(context,cancelAble,key);
    }

    /**
     * 消失loading
     * @param activity
     */
    public static void dismiss(Activity activity) {
        dismiss(activity, true);
    }
    public static void dismiss(Activity activity, boolean animated) {

        if (activity == null) {
            return;
        }

        pdismiss(activity,animated);
    }

    private static void pdismiss(Activity activity,boolean animated) {
        /**
         * check是否在主线程
         */
        if (APPLog.isDebug() && Thread.currentThread() != Looper.getMainLooper().getThread()) {
            Thread thread = Thread.currentThread();
            APPLog.error("不能在子线程中调用progress"+thread.toString());
            System.exit(-1);
        }

        final int key = activity.hashCode();
        final HashMap<Integer,ProgressDialog> dialogs = getDialogs();

        final ProgressDialog progress = dialogs.get(key);
        if (progress != null) {
            progress.dismiss(key,animated);
        }
    }

    /**
     * 释放说有的loading
     */
    public static void dismissAll() {
        /**
         * check是否在主线程
         */
        if (APPLog.isDebug() && Thread.currentThread() != Looper.getMainLooper().getThread()) {
            Thread thread = Thread.currentThread();
            APPLog.error("不能在子线程中调用progress"+thread.toString());
            System.exit(-1);
        }

        final HashMap<Integer,ProgressDialog> dialogs = getDialogs();
        Set<Integer> keys = dialogs.keySet();

        for (final Integer key : keys) {
            ProgressDialog progress = dialogs.get(key);
            progress.dismiss(key,true);
        }
    }


    /**
     * 私有dialog实现
     */
    private static class ProgressDialog {
        private Dialog dialog;
        private boolean shouldShow;
        private Handler handler;

        private ProgressDialog() {
            handler = new Handler(Looper.getMainLooper());
            shouldShow = true;
        }

        protected void show(Context context,boolean cancelAble, int key) {
            if (this.dialog == null) {
                Dialog dialog = new Dialog(context, R.style.ssn_ui_loading_style);
                this.dialog = dialog;

//                APPLog.error("开启菊花"+key);

                getDialogs().put(key, this);

                View loading = LayoutInflater.from(context).inflate(R.layout.ssn_loading_layout, null);
                dialog.setCancelable(cancelAble);
                dialog.setCanceledOnTouchOutside(false);
                Animation animation = AnimationUtils.loadAnimation(context, R.anim.ssn_loading_rotate);
                LinearInterpolator lir = new LinearInterpolator();
                animation.setInterpolator(lir);

                if (!((Activity) context).isFinishing()){
                    dialog.show();
                }

                //动画
                View img = loading.findViewById(R.id.icon_loading);
                img.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                img.startAnimation(animation);
                dialog.setContentView(loading);
            }
            else {
                if (((Activity) context).isFinishing()){
                    this.dialog.dismiss();

                    getDialogs().remove(key);
                }
                else {

                    this.shouldShow = true;
                    this.handler.removeMessages(key);

//                    APPLog.error("开启菊花"+key);

                    if (!this.dialog.isShowing()) {
                        this.dialog.show();
                    }
                }
            }
        }

        protected void dismiss(final int key,boolean animated) {

            //防止重复调用dismiss
            this.handler.removeMessages(key);

            this.shouldShow = false;
//            APPLog.error("消失菊花"+key);

            if (animated) {
                Message msg = Message.obtain(this.handler, new Runnable() {
                    @Override
                    public void run() {

                        if (!ProgressDialog.this.shouldShow) {
                            if (dialog != null && dialog.getContext() != null) {
                                try {
                                    dialog.dismiss();
                                    dialog = null;
//                                    System.gc();
                                } catch (Exception e) {
                                    ProgressDialog.this.shouldShow = false;
                                    APPLog.error(e.toString());
                                }
//                                APPLog.error("最终消失菊花" + key);
                            }
                            dialogs.remove(key);
                        }
                    }
                });
                msg.what = key;

                this.handler.sendMessageDelayed(msg, 500);
            }
            else  {
                if (dialog != null && dialog.getContext() != null) {
                    try {
                        dialog.dismiss();
                        dialog = null;
//                        System.gc();
                    } catch (Exception e) {
                        ProgressDialog.this.shouldShow = false;
                        APPLog.error(e);
                    }
//                    APPLog.error("最终消失菊花" + key);
                }
                dialogs.remove(key);
            }
        }
    }

    private static HashMap<Integer,ProgressDialog> dialogs;

    protected static HashMap<Integer,ProgressDialog> getDialogs() {
        if (dialogs != null) return dialogs;//主线程执行，不需要任何锁处理
        dialogs = new HashMap<Integer, ProgressDialog>();
        return dialogs;
    }

}
