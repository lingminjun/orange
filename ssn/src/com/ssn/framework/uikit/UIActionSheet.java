package com.ssn.framework.uikit;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import com.ssn.framework.R;
import com.ssn.framework.foundation.Res;

/**
 * Created by lingminjun on 15/7/17.
 */
public final class UIActionSheet {
    /**
     * 取消默认显示
     * @param context
     * @param action  操作
     * @param click   按钮回调，可为空
     * @return
     */
    public static Dialog showActionSheet(@NonNull Context context, @NonNull final String action, @Nullable final UIAlertButtonClick click) {
        return showActionSheet(context,new String[]{action},null,click);
    }

    /**
     * @param context
     * @param actions 操作列表，从上往下
     * @param cancel  可为空，为空采用默认值
     * @param click   按钮回调，可为空
     * @return
     */
    public static Dialog showActionSheet(@NonNull Context context, @NonNull final String[] actions, @Nullable final String cancel, @Nullable final UIAlertButtonClick click) {

        final Dialog dialog = new Dialog(context, R.style.ui_action_sheet_style);
        dialog.setCanceledOnTouchOutside(true);

        LayoutInflater inflater = LayoutInflater.from(context);
        View layout = inflater.inflate(R.layout.ssn_action_sheet_layout, null);

        layout.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        }));

        LinearLayout container = (LinearLayout)layout.findViewById(R.id.ssn_view_container);
        Button cancelBtn = (Button)layout.findViewById(R.id.cancel_btn);

        if (!TextUtils.isEmpty(cancel)) {
            cancelBtn.setText(cancel);
        }


        cancelBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
                if (click != null) {
                    if (!TextUtils.isEmpty(cancel)) {
                        click.onClick(dialog, cancel);
                    } else {
                        click.onClick(dialog, Res.localized(R.string.cancel));
                    }
                }
            }
        }));


        boolean isFirst = true;
        for (final String btnTitle : actions) {
            if (isFirst) {
                isFirst = false;
            }
            else {
                inflater.inflate(R.layout.ssn_horizontal_line,container);
            }

            inflater.inflate(R.layout.ssn_action_sheet_btn,container);

            Button btn = (Button)container.getChildAt(container.getChildCount() - 1);
            btn.setText(btnTitle);
            btn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dialog.dismiss();
                    if (click != null) {
                        click.onClick(dialog, btnTitle);
                    }
                }
            }));
        }

        dialog.setContentView(layout);
        dialog.show();

        return dialog;
    }

}
