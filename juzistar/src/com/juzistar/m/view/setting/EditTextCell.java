package com.juzistar.m.view.setting;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.juzistar.m.R;
import com.juzistar.m.Utils.Utils;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/10/1.
 */
public class EditTextCell extends UITableViewCell {

    EditText input;

    public EditTextCell(Context context) {
        super(context);
    }

    @Override
    protected View loadCustomDisplayView(LayoutInflater inflate, ViewGroup containerView) {
        View view = inflate(inflate, R.layout.edit_text_cell, containerView);
        input = (EditText)view.findViewById(R.id.input_id);
        input.addTextChangedListener(UIEvent.watcher(watcher));
        return null;
    }

    private TextWatcher watcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

        @Override
        public void afterTextChanged(Editable editable) {
            CellModel cellModel = cellModel();
            if (cellModel != null && cellModel instanceof EditTextCellModel) {
                ((EditTextCellModel) cellModel).text = Utils.getEditText(input);
            }
        }
    };


    @Override
    protected void onPrepareForReuse() {

    }

    @Override
    protected void onDisplay(CellModel cellModel, int row) {
        super.onDisplay(cellModel, row);

        EditTextCellModel model = (EditTextCellModel)cellModel;

        input.setHint(model.placeholder);
        input.setText(model.text);
    }
}
