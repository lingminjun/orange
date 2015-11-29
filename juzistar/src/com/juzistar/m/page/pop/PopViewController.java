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
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.juzistar.m.R;
import com.juzistar.m.Utils.Utils;
import com.juzistar.m.biz.Convert;
import com.juzistar.m.biz.NoticeBiz;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.biz.lbs.LBService;
import com.juzistar.m.biz.pop.BarrageCenter;
import com.juzistar.m.page.PageCenter;
import com.juzistar.m.page.PageURLs;
import com.juzistar.m.page.base.BaseTableViewController;
import com.juzistar.m.view.com.Keyboard;
import com.juzistar.m.view.com.UIDic;
import com.juzistar.m.view.pop.BubbleCellModel;
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
    LinearLayout topHintBar;

    LinearLayout switchBtnPanel;
    TextView switchBtn;
    LinearLayout sendBtnPanel;
    TextView sendBtn;

    static final String CHECK_POP_TIMER_KEY = "check_pop_timer_key";

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
        tableView.setScrollEnable(false);
        setTableView(tableView);

        topHintBar = (LinearLayout)view.findViewById(R.id.top_hint_bar);
        topHintBar.setOnClickListener(UIEvent.click(lookOverTagMsg));
        topHintBar.setVisibility(View.INVISIBLE);

        //底部发送panel
        bottom = (LinearLayout)inflater.inflate(R.layout.pop_bottom_layout, null);
        switchBtn = (TextView)bottom.findViewById(R.id.close_pop_btn);
        switchBtnPanel = (LinearLayout)bottom.findViewById(R.id.close_pop_btn_panel);
        sendBtn = (TextView)bottom.findViewById(R.id.send_pop_btn);
        sendBtnPanel = (LinearLayout)bottom.findViewById(R.id.send_pop_btn_panel);
        tableView.setFooterView(bottom);

        tableView.setOnClickListener(UIEvent.click(disKeyboard));

        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        Keyboard.barrageKeyboard().setKeyboardListener(keyboardListener);
        Clock.shareInstance().addListener(timerListener,CHECK_POP_TIMER_KEY);

        navigationItem().rightItem().setOnClick(UIEvent.click(rightClick));
        navigationItem().rightItem().setImage(R.drawable.refresh_icon);
        navigationItem().setTitleImage(R.drawable.location_icon);
        navigationItem().setTitleClick(titleClick);

        String addr = LBService.shareInstance().getLatestSimpleAddress();
        if (!TextUtils.isEmpty(addr)) {
            navigationItem().setTitle(addr);
        }

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


        BroadcastCenter.shareInstance().addObserver(this, BarrageCenter.RECEIVED_BARRAGE_NOTIFICATION, receivedMethod);

        //默认打开
        BarrageCenter.shareInstance().startService();

//        Clock.shareInstance().addListener(new Clock.Listener() {
//            @Override
//            public void fire(String flag) {
//                {
//                    final NoticeBiz.Notice notice = new NoticeBiz.Notice();
//                    notice.type = NoticeBiz.NoticeType.NORMAL;
//                    notice.category = Convert.noticeCategory(Keyboard.KEY.LOVE);
//                    notice.content = "这仅仅只为测试"+test_count;
//                    notice.creator = "xxxx";
//                    notice.creatorId = test_count+101;
//                    notice.longitude = Double.toString(31.2117411154);
//                    notice.latitude = Double.toString(121.4596178033);
//
//                    ReceivedBubbleCellModel model = new ReceivedBubbleCellModel();
//                    model.notice = notice;
//                    test_count++;
//
//                    tableViewAdapter().appendCell(model);
//
//                    if (test_count > 20) {
//                        Clock.shareInstance().removeListener("dd");
//                    }
//                }
//            }
//        },"dd");
    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

        //进入时不展示键盘
        Keyboard.barrageKeyboard().dismiss(false);
    }

    Clock.Listener timerListener = new Clock.Listener() {
        @Override
        public void fire(String flag) {
            UITableView.TableViewAdapter adapter = tableViewAdapter();
            int count = adapter.getCount();
            if (count <= 0) {return;}

            //从最后一个开始判断
            adapter.beginUpdate();
            for (int i = count-1; i >= 0; i--) {
                BubbleCellModel model = (BubbleCellModel)adapter.getItem(i);
                model.expireTime = model.expireTime - 1;
                if (model.expireTime <= 0) {
                    adapter.removeCell(model);
                } else {
                    adapter.updateCell(model,i);
                }
            }
            adapter.endUpdate();
        }
    };

    View.OnClickListener rightClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            UILoading.show(getActivity());

                LBService.shareInstance().asyncLocation(new BDLocationListener() {
                    @Override
                    public void onReceiveLocation(BDLocation bdLocation) {

                        if (!TextUtils.isEmpty(bdLocation.getAddrStr())) {
                            navigationItem().setTitle(LBService.shareInstance().getLatestSimpleAddress());
                        }

                        UILoading.dismiss(getActivity());
                    }
                });
        }
    };

    View.OnClickListener titleClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            //去手动定位页
            Navigator.shareInstance().openURL(PageURLs.LOCATION_URL);
        }
    };

    //接收pop消息
    BroadcastCenter.Method<PopViewController> receivedMethod = new BroadcastCenter.Method<PopViewController>() {
        @Override
        public void onReceive(PopViewController observer, Context context, Intent intent) {

            NoticeBiz.Notice notice = (NoticeBiz.Notice)intent.getSerializableExtra(BarrageCenter.BARRAGE_KEY);

            //刷新界面
            ReceivedBubbleCellModel model = new ReceivedBubbleCellModel();
            model.notice = notice;
            tableViewAdapter().appendCell(model);
        }
    };


    private int move_y;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean bool = super.dispatchTouchEvent(ev);
        float y = ev.getY();
        if (y - move_y > Density.dipTopx(100) && move_y != 0) {//往下滑动一个阀值
            topHintBar.setVisibility(View.VISIBLE);
            TaskQueue.mainQueue().cancel(hideTopHintBar);
            TaskQueue.mainQueue().executeDelayed(hideTopHintBar,3000);
        }

        int action = ev.getAction();
        if (action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL
                || action == MotionEvent.ACTION_HOVER_EXIT
                || action == MotionEvent.ACTION_OUTSIDE) {
            move_y = 0;
        } else if (move_y == 0 && (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_HOVER_ENTER)) {
            move_y = (int)y;
        }
        return bool;
    }

    private Runnable hideTopHintBar = new Runnable() {
        @Override
        public void run() {
            topHintBar.setVisibility(View.INVISIBLE);
            move_y = 0;
        }
    };

    /**
     * 查看标签消息
     */
    View.OnClickListener lookOverTagMsg = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Navigator.shareInstance().openURL(PageURLs.TAG_POP_URL);
        }
    };

    View.OnClickListener disKeyboard = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Keyboard.barrageKeyboard().dismiss(false);
        }
    };



    private int test_count;

    PageCenter.AuthCallBack sendAuthCallback = new PageCenter.AuthCallBack() {
        @Override
        public void auth(String account) {
            Keyboard.barrageKeyboard().show(PopViewController.this);
        }
    };

    private int customButtonKey;
    UIKeyboard.KeyboardListener keyboardListener = new UIKeyboard.KeyboardListener() {
        @Override
        public void onSendButtonClick(UIKeyboard keyboard, View sender) {
            sendAction(keyboard.text(),customButtonKey);//发送消息
        }

        @Override
        public boolean onRightButtonClick(UIKeyboard keyboard, View sender) {

            return false;
        }

        @Override
        public void onCustomButtonClick(UIKeyboard keyboard, View sender, int buttonKey) {

            if (buttonKey == customButtonKey) {
                customButtonKey = 0;
                Keyboard.barrageKeyboard().setRightButtonTitle("");
                Keyboard.barrageKeyboard().setRightButtonResourceId(R.drawable.button_keyboard_switch_icon);
            } else {
                customButtonKey = buttonKey;

                //设置键盘文案
                String keyName = UIDic.bubbleTagResourceId(Convert.noticeCategory(buttonKey));
                Keyboard.barrageKeyboard().setRightButtonTitle(keyName);

                //设置键盘右按钮背景图标
                Keyboard.barrageKeyboard().setRightButtonResourceId(R.drawable.tag_icon_bg);
            }
        }

        @Override
        public void onKeyboardChanged(int newHeight, int oldHeight) {}
    };


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

        UserCenter.User user = UserCenter.shareInstance().user();

        /**
         * 构建消息
         */
        final NoticeBiz.Notice notice = new NoticeBiz.Notice();
        notice.type = NoticeBiz.NoticeType.NORMAL;
        notice.category = Convert.noticeCategory(tag);
        notice.content = msg;
        notice.creator = user.nick;
        notice.creatorId = user.uid;
        notice.longitude = Float.toString((float)(LBService.shareInstance().getLatestLongitude()));
        notice.latitude = Float.toString((float)(LBService.shareInstance().getLatestLatitude()));
        notice.id = "sending:" + Utils.getServerTime();

        final SendBubbleCellModel model = new SendBubbleCellModel();
        model.notice = notice;
        tableViewAdapter().appendCell(model);

        NoticeBiz.create(notice,new RPC.Response<NoticeBiz.Notice>(){
            @Override
            public void onStart() {
                super.onStart();
//                UILoading.show(getActivity());
            }

            @Override
            public void onFinish() {
                super.onFinish();
//                UILoading.dismiss(getActivity());
            }

            @Override
            public void onSuccess(NoticeBiz.Notice notice1) {
                super.onSuccess(notice1);

                //发送成功则将键盘收起来，清除输入
                Keyboard.barrageKeyboard().dismiss(false);
                Keyboard.barrageKeyboard().setText("");
                Keyboard.barrageKeyboard().setRightButtonTitle("");
                Keyboard.barrageKeyboard().setRightButtonResourceId(R.drawable.button_keyboard_switch_icon);
                customButtonKey = 0;

                model.disabled = true;
                notice.id = notice1.id;//获取新的id

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
