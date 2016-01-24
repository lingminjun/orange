package com.juzistar.m.page.setting;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.juzistar.m.R;
import com.juzistar.m.Utils.Utils;
import com.juzistar.m.biz.UserBiz;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.page.base.BaseTableViewController;
import com.juzistar.m.view.me.BlankCellModel;
import com.juzistar.m.view.setting.EditTextCellModel;
import com.ssn.framework.foundation.App;
import com.ssn.framework.foundation.Density;
import com.ssn.framework.foundation.RPC;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by lingminjun on 15/5/4.
 */
public class XEditNickViewController extends UIViewController implements UISimpleTableView.TableViewAdapter.TableViewDelegate {

    UISimpleTableView tableView;
    UISimpleTableView.TableViewAdapter adapter;
    List<? extends UITableViewCell.CellModel> items = new ArrayList<>();

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
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.x_test_layout, null);
        tableView = (UISimpleTableView)view.findViewById(R.id.x_list_view);
        adapter = new UISimpleTableView.TableViewAdapter(tableView);
        adapter.setDelegate(this);
        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();
        adapter.reload();
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
    public List<? extends UITableViewCell.CellModel> tableViewLoadCells(UISimpleTableView.TableViewAdapter adapter) {
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

    @Override
    public void onTableViewCellClick(UISimpleTableView.TableViewAdapter adapter, UITableViewCell.CellModel cellModel, int row) {

    }
}
