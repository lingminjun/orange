package com.juzistar.m.page.me;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.juzistar.m.R;
import com.juzistar.m.page.PageURLs;
import com.juzistar.m.page.base.BaseTableViewController;
import com.juzistar.m.view.me.*;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.foundation.TaskQueue;
import com.ssn.framework.uikit.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 15/9/26.
 */
public class MeViewController extends BaseTableViewController {

    private int count = 0;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle("我");
        tabItem().setTabName("我");
        navigationItem().setHidden(true);
        tabItem().setTabImage(R.drawable.tab_selector_me);
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.me_layout, null);
        UITableView tableView = (UITableView)view.findViewById(R.id.table_view);
        UITableView.TableViewAdapter adapter = new UITableView.TableViewAdapter(tableView);
        setTableView(tableView,adapter);
        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();


    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

        tabItem().setBadgeValue(Integer.toString(count++));
    }

    @Override
    public List<? extends UITableViewCell.CellModel> tableViewLoadCells(UITableView.TableViewAdapter adapter) {
        List<UITableViewCell.CellModel> list = new ArrayList<>();

        list.add(new UserHeaderCellModel());

        list.add(new BlankCellModel());

        {
            SettingCellModel model = new SettingCellModel();
            model.mIconId = R.drawable.setting_message_icon;
            model.mTitle = Res.localized(R.string.setting_message_title);
            model.isSwitch = true;
            model.switchValue = true;
        }

        {
            SettingCellModel model = new SettingCellModel();
            model.mIconId = R.drawable.setting_location_icon;
            model.mTitle = Res.localized(R.string.setting_location_title);
            model.isSwitch = true;
            model.switchValue = true;
        }

        list.add(new BlankCellModel());

        {
            SettingCellModel model = new SettingCellModel();
            model.mIconId = R.drawable.setting_advice_icon;
            model.mTitle = Res.localized(R.string.setting_advice_title);
        }

        return list;
    }

    @Override
    public void onTableViewPullDownRefresh(final UITableView.TableViewAdapter adapter) {
        TaskQueue.mainQueue().executeDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.completedLoad();
            }
        },3000);
    }
}
