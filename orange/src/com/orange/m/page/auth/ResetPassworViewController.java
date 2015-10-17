package com.orange.m.page.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import com.orange.m.R;
import com.orange.m.page.PageCenter;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UIViewController;

/**
 * Created by lingminjun on 15/9/26.
 */
public class ResetPassworViewController extends UIViewController {

    Button doneBtn;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.string.origin_star));
        navigationItem().setBottomLineHidden(true);
        navigationItem().setTitleColor(Res.color(R.color.white));
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.reset_password_layout, null);
        doneBtn = (Button)view.findViewById(R.id.done_btn);
        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        setBackgroundDrawable(R.drawable.page_background);

        doneBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PageCenter.goHome();
            }
        }));

    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

    }
}
