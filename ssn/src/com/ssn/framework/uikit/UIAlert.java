package com.ssn.framework.uikit;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ssn.framework.R;

/**
 * Created by lingminjun on 15/7/17.
 */
public final class UIAlert {
    /**
     * @param context
     * @param msg     提示消息
     * @param ok      不可为空
     * @param click   按钮回调，可为空
     * @return
     */
    public static Dialog showAlert(@NonNull Context context, @NonNull final String msg, @NonNull final String ok, @Nullable final UIAlertButtonClick click) {
        return showAlert(context,null,msg,ok,null,click);
    }

    /**
     * @param context
     * @param msg     提示消息
     * @param ok      不可为空
     * @param cancel  可为空
     * @param click   按钮回调，可为空
     * @return
     */
    public static Dialog showAlert(@NonNull Context context, @NonNull final String msg, @NonNull final String ok, @Nullable final String cancel, @Nullable final UIAlertButtonClick click) {
        return showAlert(context,null,msg,ok,cancel,click);
    }

    /**
     * @param context
     * @param title   标题
     * @param msg     提示消息
     * @param ok      不可为空
     * @param cancel  可为空
     * @param click   按钮回调，可为空
     * @return
     */
    public static Dialog showAlert(@NonNull Context context, @Nullable final String title, @NonNull final String msg, @NonNull final String ok, @Nullable final String cancel, @Nullable final UIAlertButtonClick click) {
        final Dialog dialog = new Dialog(context, R.style.ssn_ui_alert_style);
        dialog.setCanceledOnTouchOutside(false);

        View layout = null;
        TextView leftTv = null;
        TextView rightTv = null;
        TextView titleTv = null;
        TextView messageTv = null;

        if (!TextUtils.isEmpty(cancel)) {
            layout = LayoutInflater.from(context).inflate(R.layout.ssn_2btn_alert_layout, null);
            leftTv = (TextView) layout.findViewById(R.id.left_btn);
            rightTv = (TextView) layout.findViewById(R.id.right_btn);
            titleTv = (TextView) layout.findViewById(R.id.title_label);
            messageTv = (TextView) layout.findViewById(R.id.message_label);
        } else {
            layout = LayoutInflater.from(context).inflate(R.layout.ssn_1btn_alert_layout, null);
            rightTv = (TextView) layout.findViewById(R.id.right_btn);
            titleTv = (TextView) layout.findViewById(R.id.title_label);
            messageTv = (TextView) layout.findViewById(R.id.message_label);
        }

        //title
        if (TextUtils.isEmpty(title)) {
            titleTv.setVisibility(View.GONE);
        }
        else {
            titleTv.setVisibility(View.VISIBLE);
            titleTv.setText(title);
        }

        //message
        messageTv.setText(msg);

        //right button
        rightTv.setText(ok);
        rightTv.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (click != null) {
                    click.onClick(dialog, ok);
                }
            }
        }));


        //left button
        if (!TextUtils.isEmpty(cancel)) {
            leftTv.setText(cancel);
            leftTv.setOnClickListener(UIEvent.click(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (click != null) {
                        click.onClick(dialog, cancel);
                    }
                }
            }));
        }

        dialog.setContentView(layout);
        dialog.show();

        return dialog;
    }

    /**
     * show一个自定义弹出框，显示内容自定义，但是按钮遵循dialog设定
     * @param context
     * @param contentView
     * @param ok
     * @param cancel
     * @param click
     * @return
     */
    public static Dialog showCustomAlert(@NonNull Context context, @NonNull View contentView, @NonNull final String ok, @Nullable final String cancel, @Nullable final UIAlertButtonClick click) {
        final Dialog dialog = new Dialog(context, R.style.ssn_ui_alert_style);
        dialog.setCanceledOnTouchOutside(false);

        View layout = null;
        LinearLayout container = null;
        TextView leftTv = null;
        TextView rightTv = null;

        if (!TextUtils.isEmpty(cancel)) {
            layout = LayoutInflater.from(context).inflate(R.layout.ssn_custom2_alert_layout, null);
            leftTv = (TextView) layout.findViewById(R.id.left_btn);
            rightTv = (TextView) layout.findViewById(R.id.right_btn);
            container = (LinearLayout) layout.findViewById(R.id.ssn_view_container);
        } else {
            layout = LayoutInflater.from(context).inflate(R.layout.ssn_custom1_alert_layout, null);
            rightTv = (TextView) layout.findViewById(R.id.right_btn);
            container = (LinearLayout) layout.findViewById(R.id.ssn_view_container);
        }

        //添加内容
        container.addView(contentView);

        //right button
        rightTv.setText(ok);
        rightTv.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                if (click != null) {
                    click.onClick(dialog, ok);
                }
            }
        }));


        //left button
        if (!TextUtils.isEmpty(cancel)) {
            leftTv.setText(cancel);
            leftTv.setOnClickListener(UIEvent.click(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    if (click != null) {
                        click.onClick(dialog, cancel);
                    }
                }
            }));
        }

        dialog.setContentView(layout);
        dialog.show();

        return dialog;
    }
}
