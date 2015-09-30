package com.orange.m.page.me;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.orange.m.R;
import com.orange.m.view.me.IconTitleCellModel;
import com.ssn.framework.foundation.TaskQueue;
import com.ssn.framework.uikit.UITableView;
import com.ssn.framework.uikit.UITableViewCell;
import com.ssn.framework.uikit.UITableViewController;
import com.ssn.framework.uikit.UIViewController;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 15/9/26.
 */
public class MeViewController extends UITableViewController {

    private int count = 0;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle("我");
        tabItem().setTabName("我");
        tabItem().setTabImage(R.drawable.tab_selector_me);
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.me_layout, null);
        setTableView((UITableView)view.findViewById(R.id.table_view));
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
    public List<UITableViewCell.CellModel> tableViewAdapterLoadData(UITableView.TableViewAdapter adapter) {
        List<UITableViewCell.CellModel> list = new ArrayList<>();

        {
            IconTitleCellModel model = new IconTitleCellModel();
            model.mIconId = R.drawable.icon_me_normal;
            model.mTitle = "这仅仅只为测试";
            list.add(model);
        }

        {
            IconTitleCellModel model = new IconTitleCellModel();
            model.mIconId = R.drawable.icon_me_normal;
            model.mTitle = "这仅仅只为测试";
            model.height = 44;
            list.add(model);
        }

        {
            IconTitleCellModel model = new IconTitleCellModel();
            model.mIconId = R.drawable.icon_me_normal;
            model.mTitle = "这仅仅只为测试";
            list.add(model);
        }

        return list;
    }

    @Override
    public void tableViewAdapterPullDownRefresh(final UITableView.TableViewAdapter adapter) {
        TaskQueue.mainQueue().executeDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.completedLoad();
            }
        },3000);
    }
}
