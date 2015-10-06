package com.orange.m.page.about;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import com.orange.m.R;
import com.orange.m.view.me.IconTitleCellModel;
import com.orange.m.view.me.TestCellModel;
import com.ssn.framework.foundation.BroadcastCenter;
import com.ssn.framework.foundation.TaskQueue;
import com.ssn.framework.uikit.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 15/10/5.
 */
public class AboutViewController extends UITableViewController {

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        navigationItem().setTitle("关于");

        //禁用下来刷新
        tableViewAdapter().setPullRefreshEnabled(false);
    }

    @Override
    public List<UITableViewCell.CellModel> tableViewLoadCells(UITableView.TableViewAdapter adapter) {
        List<UITableViewCell.CellModel> list = new ArrayList<>();

        {
            IconTitleCellModel model = new IconTitleCellModel();
            model.mIconId = R.drawable.icon_me_normal;
            model.mTitle = "抛通知";
            model.click = ViewEvent.click(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BroadcastCenter.shareInstance().postBroadcast(new Intent("TestTTTT"));
                }
            });
            list.add(model);
        }


        {
            IconTitleCellModel model = new IconTitleCellModel();
            model.mIconId = R.drawable.icon_me_normal;
            model.mTitle = "通知注册";
            model.click = ViewEvent.click(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BroadcastCenter.shareInstance().addObserver(AboutViewController.this, "TestTTTT", new BroadcastCenter.Method<AboutViewController>() {
                        @Override
                        public void onReceive(AboutViewController observer, Context context, Intent intent) {
                            Log.e("收到通知", "xxxxx");
                        }
                    });
                }
            });
            list.add(model);
        }

        {
            IconTitleCellModel model = new IconTitleCellModel();
            model.mIconId = R.drawable.icon_me_normal;
            model.mTitle = "弱通知注册";
            model.click = ViewEvent.click(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    BroadcastCenter.shareInstance().softAddObserver(AboutViewController.this, "TestTTTT", new BroadcastCenter.Method<AboutViewController>() {
                        @Override
                        public void onReceive(AboutViewController observer, Context context, Intent intent) {
                            Log.e("弱收到通知", "xxxxx");
                        }
                    });
                }
            });
            list.add(model);
        }

        {
            IconTitleCellModel model = new IconTitleCellModel();
            model.mIconId = R.drawable.icon_me_normal;
            model.mTitle = "菊花";
            model.click = ViewEvent.click(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UILoading.show(AboutViewController.this.getActivity());
                    TaskQueue.mainQueue().executeDelayed(new Runnable() {
                        @Override
                        public void run() {
                            UILoading.dismiss(AboutViewController.this.getActivity());
                        }
                    },5000);
                }
            });
            list.add(model);
        }

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
                                Log.e("action sheet", "action=" + btnTitle);
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

        return list;
    }

}
