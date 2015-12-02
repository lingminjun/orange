package com.juzistar.m.view.com;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.juzistar.m.R;
import com.ssn.framework.foundation.Density;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/11/28.
 */
public class TitleCell extends UITableViewCell {
    public TitleCell(Context context) {
        super(context);
    }

    public TitleCell(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TitleCell(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }


    private ImageView imageView;
    private TextView titleLabel;
    private TextView subTitleLabel;


    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate, ViewGroup containerView) {
        View view = inflate(inflate, R.layout.title_cell, containerView);
        imageView = (ImageView)view.findViewById(R.id.image_view);
        titleLabel = (TextView)view.findViewById(R.id.title_label);
        subTitleLabel = (TextView)view.findViewById(R.id.sub_label);
        return view;
    }

    @Override
    protected void onPrepareForReuse() {
        imageView.setVisibility(GONE);
        titleLabel.setText("");
        subTitleLabel.setText("");
    }

    @Override
    protected void onDisplay(CellModel cellModel, int row) {
        super.onDisplay(cellModel, row);

        TitleCellModel model = (TitleCellModel)cellModel;

        if (TextUtils.isEmpty(model.title)) {
            titleLabel.setText(model.title);
        }

        if (TextUtils.isEmpty(model.subTitle)) {
            subTitleLabel.setText(model.subTitle);
        }

        if (model.imageId != 0) {
            imageView.setImageResource(model.imageId);

            model.separateLineLeftPadding = Density.dipTopx(52);
        } else {
            model.separateLineLeftPadding = Density.dipTopx(12);
        }

    }
}
