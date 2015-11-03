package com.orange.m.page.pop;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.orange.m.R;
import com.orange.m.page.PageCenter;
import com.orange.m.view.common.Keyboard;
import com.orange.m.view.me.IconTitleCellModel;
import com.orange.m.view.me.TestCellModel;
import com.orange.m.view.pop.BubbleCellModel;
import com.orange.m.view.pop.ReceivedBubbleCell;
import com.ssn.framework.foundation.BroadcastCenter;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.foundation.TaskQueue;
import com.ssn.framework.uikit.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 15/9/26.
 */
public class PopViewController extends UITableViewController {

    LinearLayout bottom;

    LinearLayout buttonBar;
    LinearLayout bottomBar;

    TextView switchBtn;
    TextView sendBtn;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        String title = Res.localized(R.string.bubble_title);
        navigationItem().setTitle(title);
        tabItem().setTabName(title);
        tabItem().setTabImage(R.drawable.tab_selector_pop);
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.pop_layout, null);
        UITableView tableView = (UITableView)view.findViewById(R.id.table_view);
        tableView.setCacheColorHint(Color.TRANSPARENT);
        setTableView(tableView);
        bottom = (LinearLayout)inflater.inflate(R.layout.pop_bottom_layout, null);
        switchBtn = (TextView)bottom.findViewById(R.id.close_pop_btn);
        sendBtn = (TextView)bottom.findViewById(R.id.send_pop_btn);
        buttonBar = (LinearLayout)bottom.findViewById(R.id.button_bar);
        bottomBar = (LinearLayout)bottom.findViewById(R.id.bottom_input_bar);
        tableView.setFooterView(bottom);
        return view;
    }


    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

//        tableViewAdapter().setPullRefreshEnabled(false);

        addObserver();

        Keyboard.shareInstance().setKeyboardHeightChanged(new Keyboard.KeyboardHeightChanged() {
            @Override
            public void onChanged(int newHeight, int oldHeight) {
//                tableView().resizeFooterViewHeight();
            }
        });

        Keyboard.shareInstance().setSendClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAction(Keyboard.shareInstance().text());
            }
        });

        switchBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PageCenter.checkAuth(null);
            }
        }));

        sendBtn.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Keyboard.shareInstance().showInView(bottomBar);
            }
        }));
    }

    private void addObserver() {

        BroadcastCenter.Method<PopViewController> method = new BroadcastCenter.Method<PopViewController>() {
            @Override
            public void onReceive(PopViewController observer, Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(UIEvent.UIKeyboardWillShowNotification)) {
                    buttonBar.setVisibility(View.GONE);
                    tableViewAdapter().setPullRefreshEnabled(false);
                }
                else if (action.equals(UIEvent.UIKeyboardWillHideNotification)) {
                    buttonBar.setVisibility(View.VISIBLE);
                    Keyboard.shareInstance().dismiss(false);
                    tableViewAdapter().setPullRefreshEnabled(true);
                }
            }
        };

        BroadcastCenter.shareInstance().addObserver(this, UIEvent.UIKeyboardWillShowNotification, method);
        BroadcastCenter.shareInstance().addObserver(this, UIEvent.UIKeyboardWillHideNotification, method);
    }

    public void sendAction(String msg) {

    }

    @Override
    public List<UITableViewCell.CellModel> tableViewLoadCells(UITableView.TableViewAdapter adapter) {
        List<UITableViewCell.CellModel> list = new ArrayList<>();

        for (int i = 0; i< 10;i++) {
            {
                BubbleCellModel model = new BubbleCellModel();
                model.message = "这仅仅只为测试"+i;
                list.add(model);
            }
        }

        return list;
    }

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        return super.dispatchTouchEvent(ev);
//    }
//
}
