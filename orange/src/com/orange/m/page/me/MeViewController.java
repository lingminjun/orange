package com.orange.m.page.me;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import com.orange.m.R;
import com.orange.m.view.me.IconTitleCellModel;
import com.orange.m.view.me.TestCell;
import com.orange.m.view.me.TestCellModel;
import com.ssn.framework.foundation.TaskQueue;
import com.ssn.framework.uikit.*;

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
        navigationItem().setHidden(true);
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
    public List<UITableViewCell.CellModel> tableViewLoadCells(UITableView.TableViewAdapter adapter) {
        List<UITableViewCell.CellModel> list = new ArrayList<>();

        for (int i = 0; i< 100;i++) {
            {
                IconTitleCellModel model = new IconTitleCellModel();
                model.mIconId = R.drawable.icon_me_normal;
                model.mTitle = "这仅仅只为测试"+i;
//                model.disabled = true;
                model.click = ViewEvent.click(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
//                        UIAlert.showAlert(getActivity(),"这是标题","随便写点啥，看看换行的效果，可能还不够长，必须凑个字数","确定","取消",null);
                        UIActionSheet.showActionSheet(getActivity(), new String[]{"随便写写", "看着办", "还写一个"}, null, new UIAlertButtonClick() {
                            @Override
                            public void onClick(Dialog dialog, String btnTitle) {
                                Log.e("action sheet","action="+btnTitle);
                            }
                        });
                    }
                });



                list.add(model);
            }
            {
                TestCellModel model = new TestCellModel();
                list.add(model);
            }
        }
//        {
//            IconTitleCellModel model = new IconTitleCellModel();
//            model.mIconId = R.drawable.icon_me_normal;
//            model.mTitle = "这仅仅只为测试";
//            list.add(model);
//        }
//
//        {
//            TestCellModel model = new TestCellModel();
//            list.add(model);
//        }
//
//        {
//            IconTitleCellModel model = new IconTitleCellModel();
//            model.mIconId = R.drawable.icon_me_normal;
//            model.mTitle = "这仅仅只为测试";
//            list.add(model);
//        }
//        {
//            TestCellModel model = new TestCellModel();
//            list.add(model);
//        }
//
//        {
//            IconTitleCellModel model = new IconTitleCellModel();
//            model.mIconId = R.drawable.icon_me_normal;
//            model.mTitle = "这仅仅只为测试";
//            list.add(model);
//        }
//        {
//            TestCellModel model = new TestCellModel();
//            list.add(model);
//        }
//
//        {
//            IconTitleCellModel model = new IconTitleCellModel();
//            model.mIconId = R.drawable.icon_me_normal;
//            model.mTitle = "这仅仅只为测试";
//            list.add(model);
//        }
//        {
//            TestCellModel model = new TestCellModel();
//            list.add(model);
//        }
//
//        {
//            IconTitleCellModel model = new IconTitleCellModel();
//            model.mIconId = R.drawable.icon_me_normal;
//            model.mTitle = "这仅仅只为测试";
//            list.add(model);
//        }

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
