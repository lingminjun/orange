package com.juzistar.m.page.pop;

import android.content.Context;
import android.content.Intent;
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
import com.juzistar.m.biz.MessageBiz;
import com.juzistar.m.biz.NoticeBiz;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.net.BoolModel;
import com.juzistar.m.page.PageCenter;
import com.juzistar.m.page.base.BaseTableViewController;
import com.juzistar.m.view.common.Keyboard;
import com.juzistar.m.view.me.IconTitleCellModel;
import com.juzistar.m.view.me.TestCellModel;
import com.juzistar.m.view.pop.BubbleCellModel;
import com.juzistar.m.view.pop.ReceivedBubbleCell;
import com.juzistar.m.view.pop.SendBubbleCellModel;
import com.ssn.framework.foundation.*;
import com.ssn.framework.uikit.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 15/9/26.
 */
public class PopViewController extends BaseTableViewController {

    LinearLayout bottom;

    LinearLayout buttonBar;
    LinearLayout bottomBar;

    LinearLayout switchBtnPanel;
    TextView switchBtn;
    LinearLayout sendBtnPanel;
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
        tableView.setStackFromBottom(true);
        setTableView(tableView);

        //底部发送panel
        bottom = (LinearLayout)inflater.inflate(R.layout.pop_bottom_layout, null);
        switchBtn = (TextView)bottom.findViewById(R.id.close_pop_btn);
        switchBtnPanel = (LinearLayout)bottom.findViewById(R.id.close_pop_btn_panel);
        sendBtn = (TextView)bottom.findViewById(R.id.send_pop_btn);
        sendBtnPanel = (LinearLayout)bottom.findViewById(R.id.send_pop_btn_panel);
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

            }
        });

        Keyboard.shareInstance().setSendClick(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendAction(Keyboard.shareInstance().text());
            }
        });

        switchBtnPanel.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PageCenter.checkAuth(null);
            }
        }));

        sendBtnPanel.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Keyboard.shareInstance().showInView(bottomBar);
            }
        }));

        Clock.shareInstance().addListener(new Clock.Listener() {
            @Override
            public void fire(String flag) {
                {
                    BubbleCellModel model = new BubbleCellModel();
                    model.message = "这仅仅只为测试"+test_count;
                    test_count++;

                    tableViewAdapter().appendCell(model);

                    if (test_count > 20) {
                        Clock.shareInstance().removeListener("dd");
                    }
                }
            }
        },"dd");
    }

    private int test_count;

    private void addObserver() {

        BroadcastCenter.Method<PopViewController> method = new BroadcastCenter.Method<PopViewController>() {
            @Override
            public void onReceive(PopViewController observer, Context context, Intent intent) {
                String action = intent.getAction();
                if (action.equals(UIEvent.UIKeyboardWillShowNotification)) {
                    buttonBar.setVisibility(View.GONE);
                }
                else if (action.equals(UIEvent.UIKeyboardWillHideNotification)) {
                    buttonBar.setVisibility(View.VISIBLE);
                    Keyboard.shareInstance().dismiss(false);
                }

            }
        };

        BroadcastCenter.shareInstance().addObserver(this, UIEvent.UIKeyboardWillShowNotification, method);
        BroadcastCenter.shareInstance().addObserver(this, UIEvent.UIKeyboardWillHideNotification, method);
    }

    public void sendAction(final String msg) {
        if (TextUtils.isEmpty(msg)) {
            App.toast(Res.localized(R.string.please_input_content));
            return;
        }

        PageCenter.checkAuth(new PageCenter.AuthCallBack() {
            @Override
            public void auth(String account) {
                sendNotice(msg);
            }
        });
    }

    private void sendNotice(final String msg) {
        Keyboard.shareInstance().dismiss(false);

        UserCenter.User user = UserCenter.shareInstance().user();

        NoticeBiz.Notice notice = new NoticeBiz.Notice();
        notice.content = msg;
        notice.creator = user.nick;
        notice.creatorId = user.uid;
        notice.longitude = Double.toString(31.2117411154);
        notice.latitude = Double.toString(121.4596178033);

        NoticeBiz.create(notice,new RPC.Response<BoolModel>(){
            @Override
            public void onSuccess(BoolModel boolModel) {
                super.onSuccess(boolModel);

                //清除输入
                Keyboard.shareInstance().setText("");

                SendBubbleCellModel model = new SendBubbleCellModel();
                model.message = msg;
                tableViewAdapter().appendCell(model);
            }

            @Override
            public void onFailure(Exception e) {
                super.onFailure(e);
                Utils.toastException(e,Res.localized(R.string.send_failed));
            }
        });
    }

    private void sendMessage(final String msg) {

        MessageBiz.send(msg,3, new RPC.Response<MessageBiz.Message>() {
            @Override
            public void onSuccess(MessageBiz.Message message) {
                super.onSuccess(message);
            }
        });

    }

    @Override
    public List<UITableViewCell.CellModel> tableViewLoadCells(UITableView.TableViewAdapter adapter) {
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

//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        return super.dispatchTouchEvent(ev);
//    }
//
}
