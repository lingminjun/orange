package com.juzistar.m.page.pop;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.juzistar.m.R;
import com.juzistar.m.Utils.Utils;
import com.juzistar.m.biz.Convert;
import com.juzistar.m.biz.NoticeBiz;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.biz.pop.BarrageCenter;
import com.juzistar.m.net.BoolModel;
import com.juzistar.m.page.PageCenter;
import com.juzistar.m.page.base.BaseTableViewController;
import com.juzistar.m.view.com.Keyboard;
import com.juzistar.m.view.com.UIDic;
import com.juzistar.m.view.pop.ReceivedBubbleCellModel;
import com.juzistar.m.view.pop.SendBubbleCellModel;
import com.ssn.framework.foundation.*;
import com.ssn.framework.uikit.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 15/9/26.
 * 泡泡列表页
 */
public class TagPopViewController extends BaseTableViewController {

//    LinearLayout bottom;
//    LinearLayout topHintBar;
//
//    LinearLayout switchBtnPanel;
//    TextView switchBtn;
//    LinearLayout sendBtnPanel;
//    TextView sendBtn;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        String title = Res.localized(R.string.look_tag_msg_title);
        navigationItem().setTitle(title);
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.tag_pop_layout, null);
        UITableView tableView = (UITableView)view.findViewById(R.id.table_view);
        tableView.setCacheColorHint(Color.TRANSPARENT);
        tableView.setStackFromBottom(true);
//        tableView.setScrollEnable(false);
        setTableView(tableView);

//        topHintBar = (LinearLayout)view.findViewById(R.id.top_hint_bar);
//        topHintBar.setOnClickListener(UIEvent.click(lookOverTagMsg));
//        topHintBar.setVisibility(View.INVISIBLE);

        //底部发送panel
//        bottom = (LinearLayout)inflater.inflate(R.layout.pop_bottom_layout, null);
//        switchBtn = (TextView)bottom.findViewById(R.id.close_pop_btn);
//        switchBtnPanel = (LinearLayout)bottom.findViewById(R.id.close_pop_btn_panel);
//        sendBtn = (TextView)bottom.findViewById(R.id.send_pop_btn);
//        sendBtnPanel = (LinearLayout)bottom.findViewById(R.id.send_pop_btn_panel);
//        tableView.setFooterView(bottom);

//        tableView.setOnClickListener(UIEvent.click(disKeyboard));

        return view;
    }


    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        Activity activity = getActivity();
        if (activity instanceof BaseActivity) {
            ((BaseActivity) activity).setBackgroundDrawable(R.drawable.page_bd);
        }
    }

    @Override
    public List<? extends UITableViewCell.CellModel> tableViewLoadCells(UITableView.TableViewAdapter adapter) {
        List<UITableViewCell.CellModel> list = new ArrayList<>();

//        for (int i = 0; i< 10;i++) {
//            {
//                BubbleCellModel model = new BubbleCellModel();
//                model.message = "这仅仅只为测试"+i;
//                list.add(model);
//            }
//        }

        return list;
    }

    @Override
    public void onTableViewCellClick(UITableView.TableViewAdapter adapter, UITableViewCell.CellModel cellModel, int row) {
        super.onTableViewCellClick(adapter, cellModel, row);
    }

}
