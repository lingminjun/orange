package com.juzistar.m.page.setting;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import com.juzistar.m.R;
import com.juzistar.m.Utils.Utils;
import com.juzistar.m.biz.UserBiz;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.page.base.BaseTableViewController;
import com.juzistar.m.view.me.BlankCellModel;
import com.juzistar.m.view.setting.EditTextCellModel;
import com.ssn.framework.foundation.App;
import com.ssn.framework.foundation.RPC;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UILoading;
import com.ssn.framework.uikit.UITableView;
import com.ssn.framework.uikit.UITableViewCell;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by lingminjun on 15/5/4.
 */
public class EditNickViewController extends BaseTableViewController {

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.string.modify_nick));
        navigationItem().setTitleColor(Res.color(R.color.ssn_normal_text));
        navigationItem().setBottomLineHidden(false);

        navigationItem().rightItem().setTitle(Res.localized(R.string.save));
        navigationItem().rightItem().setTitleColor(Res.colorState(R.color.green_to_grey));
        navigationItem().rightItem().setOnClick(UIEvent.click(rightClick));
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        tableView().setBackgroundColor(Res.color(R.color.page_bg));
    }


    View.OnClickListener rightClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (editTextCellModel == null) {return;}

            String string = editTextCellModel.text;

            if (TextUtils.isEmpty(string)) {
                App.toast(Res.localized(R.string.input_new_nick_tip));
                return;
            }

            if (string.length() < 1 || string.length() > 10) {
                App.toast(Res.localized(R.string.input_nick_length_tip));
                return;
            }

            UserCenter.User user = UserCenter.shareInstance().user();
            if (string.equals(user.nick)) {
                finish();
                return;
            }

            UILoading.show(getActivity());
            UserBiz.updateUser(string,new RPC.Response<UserBiz.TokenModel>(){
                @Override
                public void onSuccess(UserBiz.TokenModel tokenModel) {
                    super.onSuccess(tokenModel);

//                    user.nick = string;
                    finish();

                    App.toast(Res.localized(R.string.modify_success));
                }

                @Override
                public void onFailure(Exception e) {
                    super.onFailure(e);
                    Utils.toastException(e,Res.localized(R.string.modify_failed));
                }

                @Override
                public void onFinish() {
                    super.onFinish();
                    UILoading.dismiss(getActivity());
                }
            });
        }
    };

    private EditTextCellModel editTextCellModel;

    @Override
    public List<? extends UITableViewCell.CellModel> tableViewLoadCells(UITableView.TableViewAdapter adapter) {
        List<UITableViewCell.CellModel> list = new ArrayList<>();

        list.add(new BlankCellModel());

        if (editTextCellModel == null) {
            editTextCellModel = new EditTextCellModel();
            UserCenter.User user = UserCenter.shareInstance().user();
            editTextCellModel.text = user.nick;
            editTextCellModel.placeholder = Res.localized(R.string.please_input_nick);
        }
        list.add(editTextCellModel);

        return list;
    }
}
