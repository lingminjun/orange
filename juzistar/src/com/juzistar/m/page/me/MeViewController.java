package com.juzistar.m.page.me;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.juzistar.m.R;
import com.juzistar.m.constants.Constants;
import com.juzistar.m.page.PageCenter;
import com.juzistar.m.page.PageURLs;
import com.juzistar.m.page.base.BaseTableViewController;
import com.juzistar.m.view.chat.SessionCellModel;
import com.juzistar.m.view.me.*;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.foundation.TaskQueue;
import com.ssn.framework.foundation.UserDefaults;
import com.ssn.framework.uikit.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 15/9/26.
 */
public class MeViewController extends BaseTableViewController {

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.string.me));
        tabItem().setTabName(Res.localized(R.string.me));
        navigationItem().setHidden(true);
        tabItem().setTabImage(R.drawable.tab_selector_me);
        tabItem().setTabNameColor(Res.colorState(R.color.tab_selector_color));

        navigationItem().backItem().setHidden(true);
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.me_layout, null);
        UITableView tableView = (UITableView)view.findViewById(R.id.table_view);
        UITableView.TableViewAdapter adapter = new UITableView.TableViewAdapter(tableView);
        setTableView(tableView,adapter);
        tableView.setBackgroundColor(Res.color(R.color.page_bg));
        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();


    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

        if (headerCellModel != null) {
            tableViewAdapter().updateCell(headerCellModel,0);
        }
    }


    UserHeaderCellModel headerCellModel;

    @Override
    public List<? extends UITableViewCell.CellModel> tableViewLoadCells(UITableView.TableViewAdapter adapter) {
        List<UITableViewCell.CellModel> list = new ArrayList<>();

        if (headerCellModel == null) {
            headerCellModel = new UserHeaderCellModel();
        }
        list.add(headerCellModel);

        list.add(new BlankCellModel());

        {
            SettingCellModel model = new SettingCellModel();
            model.mIconId = R.drawable.setting_message_icon;
            model.mTitle = Res.localized(R.string.setting_message_title);
            model.isSwitch = true;
            model.switchValue = UserDefaults.getInstance().get(Constants.USER_DEFAULTS_MESSAGE_NOTICE_SOUND,true);
            model.listener = switchListener;
            model.separateLineLeftPadding = 52;
            list.add(model);
        }

        {
            SettingCellModel model = new SettingCellModel();
            model.mIconId = R.drawable.setting_location_icon;
            model.mTitle = Res.localized(R.string.setting_location_title);
            model.isSwitch = true;
            model.switchValue = UserDefaults.getInstance().get(Constants.USER_DEFAULTS_LOCATION_ALLOW,true);
            model.listener = switchListener;
            list.add(model);
        }

        list.add(new BlankCellModel());

        {
            SettingCellModel model = new SettingCellModel();
            model.mIconId = R.drawable.setting_advice_icon;
            model.mTitle = Res.localized(R.string.setting_advice_title);
            list.add(model);
        }

        return list;
    }

    @Override
    public void onTableViewCellClick(UITableView.TableViewAdapter adapter, UITableViewCell.CellModel cellModel, int row) {
        super.onTableViewCellClick(adapter, cellModel, row);

        if (cellModel instanceof SettingCellModel) {
            SettingCellModel model = (SettingCellModel)cellModel;


            if (model.mTitle.equals(Res.localized(R.string.setting_advice_title))) {//意见反馈
                Navigator.shareInstance().openURL(PageURLs.FEEDBACK_URL);
            }

        } else if (cellModel instanceof UserHeaderCellModel) {
            PageCenter.checkAuth(new PageCenter.AuthCallBack() {
                @Override
                public void auth(String account) {
                    Navigator.shareInstance().openURL(PageURLs.SET_NICK_URL);
                }
            });
        }
    }

    public SettingCellModel.SettingCellListener switchListener = new SettingCellModel.SettingCellListener() {
        @Override
        public void onSwitchChanged(SettingCellModel model, boolean switchValue) {
            int row = tableViewAdapter().row(model);
            tableViewAdapter().updateCell(model,row);

            if (model.mTitle.equals(Res.localized(R.string.setting_message_title))) {//消息提示音
                UserDefaults.getInstance().put(Constants.USER_DEFAULTS_MESSAGE_NOTICE_SOUND, switchValue);
            } else if (model.mTitle.equals(Res.localized(R.string.setting_location_title))) {//允许获取地理位置
                UserDefaults.getInstance().put(Constants.USER_DEFAULTS_LOCATION_ALLOW, switchValue);
            }
        }
    };
}
