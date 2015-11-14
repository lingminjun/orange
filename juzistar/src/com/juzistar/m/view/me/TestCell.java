package com.juzistar.m.view.me;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.juzistar.m.R;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/10/1.
 */
public class TestCell extends UITableViewCell {

    TextView btn;
    Button btn2;
    EditText input;

    public TestCell(Context context) {
        super(context);
    }

    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate, ViewGroup containerView) {
        View view = inflate(inflate, R.layout.my_test_cell, containerView);
        btn = (TextView)view.findViewById(R.id.custom_btn);
        btn2 = (Button)view.findViewById(R.id.custom_btn1);
        input = (EditText)view.findViewById(R.id.input_id);

        return null;
    }

    @Override
    protected void onPrepareForReuse() {

    }

    @Override
    protected void onDisplay(CellModel cellModel, int row) {
        super.onDisplay(cellModel, row);

        btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("error","click");
            }
        });

        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("error","button click");
            }
        });
    }
}
