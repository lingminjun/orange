package com.juzistar.m.page.pop;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
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
import com.juzistar.m.view.pop.ReceivedBubbleCellModel;
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
        tableView.setFooterView(bottom);

        return view;
    }


    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        Keyboard.barrageKeyboard().setKeyboardListener(keyboardListener);

        switchBtnPanel.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BarrageCenter.shareInstance().isOpenService()) {
                    BarrageCenter.shareInstance().stopService();
                    switchBtn.setSelected(true);
                } else {
                    BarrageCenter.shareInstance().startService();
                    switchBtn.setSelected(false);
                }
            }
        }));

        sendBtnPanel.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PageCenter.checkAuth(sendAuthCallback);
            }
        }));

        Clock.shareInstance().addListener(new Clock.Listener() {
            @Override
            public void fire(String flag) {
                {
                    final NoticeBiz.Notice notice = new NoticeBiz.Notice();
                    notice.type = Convert.noticeType(Keyboard.KEY.LOVE);
                    notice.content = "这仅仅只为测试"+test_count;
                    notice.creator = "xxxx";
                    notice.creatorId = test_count+101;
                    notice.longitude = Double.toString(31.2117411154);
                    notice.latitude = Double.toString(121.4596178033);

                    ReceivedBubbleCellModel model = new ReceivedBubbleCellModel();
                    model.notice = notice;
                    test_count++;

                    tableViewAdapter().appendCell(model);

                    if (test_count > 20) {
                        Clock.shareInstance().removeListener("dd");
                    }
                }
            }
        },"dd");
    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

        //进入时不展示键盘
        Keyboard.barrageKeyboard().dismiss(false);
    }

    private int test_count;

    PageCenter.AuthCallBack sendAuthCallback = new PageCenter.AuthCallBack() {
        @Override
        public void auth(String account) {
            Keyboard.barrageKeyboard().show(PopViewController.this);
        }
    };

    UIKeyboard.KeyboardListener keyboardListener = new UIKeyboard.KeyboardListener() {
        @Override
        public void onSendButtonClick(UIKeyboard keyboard, View sender) {
            sendAction(keyboard.text(),Keyboard.KEY.NAN);//发送消息
        }

        @Override
        public boolean onRightButtonClick(UIKeyboard keyboard, View sender) {

            return false;
        }

        @Override
        public void onCustomButtonClick(UIKeyboard keyboard, View sender, int buttonKey) {
            sendAction(keyboard.text(),buttonKey);//发送消息
        }

        @Override
        public void onKeyboardChanged(int newHeight, int oldHeight) {
            int tab_height = Density.dipTopx(50);
            if (newHeight > tab_height) {
                tableView().setFooterHeight(newHeight - tab_height);
            } else {
                tableView().setFooterHeight(0);
            }
        }
    };

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
                }

            }
        };

        BroadcastCenter.shareInstance().addObserver(this, UIEvent.UIKeyboardWillShowNotification, method);
        BroadcastCenter.shareInstance().addObserver(this, UIEvent.UIKeyboardWillHideNotification, method);
    }

    public void sendAction(final String msg, final int tag) {
        if (TextUtils.isEmpty(msg)) {
            App.toast(Res.localized(R.string.please_input_content));
            return;
        }

        PageCenter.checkAuth(new PageCenter.AuthCallBack() {
            @Override
            public void auth(String account) {
                sendNotice(msg,tag);
            }
        });
    }

    private void sendNotice(final String msg, final int tag) {
        Keyboard.barrageKeyboard().dismiss(false);

        UserCenter.User user = UserCenter.shareInstance().user();

        /**
         * 构建消息
         */
        final NoticeBiz.Notice notice = new NoticeBiz.Notice();
        notice.type = NoticeBiz.NoticeType.TEMP;//Convert.noticeType(tag);
        notice.content = msg;
        notice.creator = user.nick;
        notice.creatorId = user.uid;
        notice.longitude = Double.toString(31.2117411154);
        notice.latitude = Double.toString(121.4596178033);
        notice.id = "sending:" + Utils.getServerTime();

        //清除输入
        Keyboard.barrageKeyboard().setText("");

        final SendBubbleCellModel model = new SendBubbleCellModel();
        model.notice = notice;
        tableViewAdapter().appendCell(model);

        NoticeBiz.create(notice,new RPC.Response<BoolModel>(){
            @Override
            public void onStart() {
                super.onStart();
                UILoading.show(getActivity());
            }

            @Override
            public void onFinish() {
                super.onFinish();
                UILoading.dismiss(getActivity());
            }

            @Override
            public void onSuccess(BoolModel boolModel) {
                super.onSuccess(boolModel);

                model.disabled = true;
                notice.id = "" + Utils.getServerTime();

                int row = tableViewAdapter().row(model);
                if (row >= 0) {
                    tableViewAdapter().updateCell(model,row);
                }
            }

            @Override
            public void onFailure(Exception e) {
                super.onFailure(e);
                Utils.toastException(e,Res.localized(R.string.send_failed));

                notice.id = "";
                model.disabled = false;

                int row = tableViewAdapter().row(model);
                if (row >= 0) {
                    tableViewAdapter().updateCell(model,row);
                }
            }
        });
    }

//    private void sendMessage(final String msg) {
//
//        MessageBiz.send(msg,3, new RPC.Response<MessageBiz.Message>() {
//            @Override
//            public void onSuccess(MessageBiz.Message message) {
//                super.onSuccess(message);
//            }
//        });
//
//    }

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
        Keyboard.barrageKeyboard().dismiss(false);
    }

}
