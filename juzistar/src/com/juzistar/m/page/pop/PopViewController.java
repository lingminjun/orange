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
import com.juzistar.m.biz.Convert;
import com.juzistar.m.biz.NoticeBiz;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.biz.lbs.LBService;
import com.juzistar.m.biz.pop.BarrageCenter;
import com.juzistar.m.constants.Constants;
import com.juzistar.m.entity.MapMarkPoint;
import com.juzistar.m.page.PageCenter;
import com.juzistar.m.page.PageURLs;
import com.juzistar.m.page.base.BaseTableViewController;
import com.juzistar.m.view.com.Keyboard;
import com.juzistar.m.view.com.KeyboardButton;
import com.juzistar.m.view.com.UIDic;
import com.juzistar.m.view.pop.BubbleCellModel;
import com.juzistar.m.view.pop.PPBlankCellModel;
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
    static String TAG_NOTICE_LIMIT_CLOCK = "TAG_NOTICE_LIMIT_CLOCK";
    static String TAG_NOTICE_POP_CLOCK = "TAG_NOTICE_POP_CLOCK";//不断推

    LinearLayout bottom;
    LinearLayout topHintBar;

    LinearLayout switchBtnPanel;
    TextView switchBtn;
    LinearLayout sendBtnPanel;
    TextView sendBtn;

    String UIDISPLAYLINK_PREFIX;

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        String title = Res.localized(R.string.bubble_title);
        navigationItem().setTitle(title);
        tabItem().setTabName(title);
        tabItem().setTabImage(R.drawable.tab_selector_pop);
        tabItem().setTabNameColor(Res.colorState(R.color.tab_selector_color));

        navigationItem().setBottomLineHidden(true);
        navigationItem().backItem().setHidden(true);
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

//        tableView.setOnClickListener(UIEvent.click(disKeyboard));

        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        UIDISPLAYLINK_PREFIX = ""+getActivity().hashCode();

//        Keyboard.barrageKeyboard().setKeyboardListener(keyboardListener);

        navigationItem().rightItem().setOnClick(UIEvent.click(rightClick));
        navigationItem().rightItem().setImage(R.drawable.refresh_icon);
        navigationItem().setTitleImage(R.drawable.location_icon);
        navigationItem().setTitleClick(titleClick);

        String addr = BarrageCenter.shareInstance().getLocation().getSimpleAddress();//
        if (!TextUtils.isEmpty(addr)) {
            navigationItem().setTitle(addr);
        } else {
            navigationItem().setTitle(Res.localized(R.string.please_refresh_location));
        }

        switchBtnPanel.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (BarrageCenter.shareInstance().isOpenService()) {
                    BarrageCenter.shareInstance().stopService();
                    switchBtn.setSelected(true);
                    sendBtnPanel.setVisibility(View.INVISIBLE);

                    //清空table
                    tableViewAdapter().removeAll();

                    Clock.shareInstance().removeListener(TAG_NOTICE_POP_CLOCK);
                } else {
                    BarrageCenter.shareInstance().startService();
                    switchBtn.setSelected(false);
                    sendBtnPanel.setVisibility(View.VISIBLE);
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
//                    notice.longitude = Double.toString(121.4596178033);
//                    notice.latitude = Double.toString(31.2117411154);
//                    notice.id = "" + Utils.getServerTime();
//
//                    appendNoticeCellModel(notice);
//                    test_count++;
//
//                    if (test_count > 20) {
//                        Clock.shareInstance().removeListener("dd");
//                    }
//                }
//            }
//        },"dd");
    }


    PPBlankCellModel blankCellModel = new PPBlankCellModel();

    Clock.Listener popClock = new Clock.Listener() {
        @Override
        public void fire(String flag) {

            boolean hasPop = false;
            for (int row = 0; row < tableViewAdapter().getCount(); row++) {
                UITableViewCell.CellModel cellModel = tableViewAdapter().getItem(row);
                if (cellModel instanceof BubbleCellModel) {
                    hasPop = true;
                    break;
                }
            }

            if (hasPop) {
                tableViewAdapter().appendCell(blankCellModel);
            } else {
                Clock.shareInstance().removeListener(TAG_NOTICE_POP_CLOCK);
            }
        }
    };

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

        String addr = BarrageCenter.shareInstance().getLocation().getSimpleAddress();//
        if (!TextUtils.isEmpty(addr)) {
            navigationItem().setTitle(addr);
        } else {
            navigationItem().setTitle(Res.localized(R.string.please_refresh_location));
        }

        //进入时不展示键盘
//        Keyboard.barrageKeyboard().dismiss(false);
    }

    @Override
    public void onViewDidDisappear() {
        super.onViewDidDisappear();
    }

    @Override
    public void onDestroyController() {
        super.onDestroyController();
        Clock.shareInstance().removeListener(TAG_NOTICE_LIMIT_CLOCK);
        Clock.shareInstance().removeListener(TAG_NOTICE_POP_CLOCK);
        UIDisplayLink.shareInstance().removeListeners(UIDISPLAYLINK_PREFIX);
    }

    View.OnClickListener rightClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {

            UILoading.show(getActivity());

            BarrageCenter.shareInstance().refreshCurrentLocation(new Runnable() {
                @Override
                public void run() {
                    String addr = BarrageCenter.shareInstance().getLocation().getSimpleAddress();//
                    navigationItem().setTitle(addr);
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

            if (notice != null) {
                appendNoticeCellModel(notice);
            }
        }
    };


    private int move_y;
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean bool = super.dispatchTouchEvent(ev);
        float y = ev.getY();
        if (y - move_y > Density.dip2px(100) && move_y != 0) {//往下滑动一个阀值
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

    /*
    View.OnClickListener disKeyboard = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Keyboard.barrageKeyboard().dismiss(false);
        }
    };
    */



    private int test_count;

    PageCenter.AuthCallBack sendAuthCallback = new PageCenter.AuthCallBack() {
        @Override
        public void auth(String account) {
//            Keyboard.barrageKeyboard().show(PopViewController.this);
            UILockScreenKeyboard.show(getActivity(),keyboardListener,Keyboard.barrageCustomView());
        }
    };

    //限制发送时效
    private long limit_time;
    private Clock.Listener limitClock = new Clock.Listener() {
        @Override
        public void fire(String flag) {

            UILockScreenKeyboard keyboard = UILockScreenKeyboard.keyboard();
            if (keyboard == null) {
                Clock.shareInstance().removeListener(TAG_NOTICE_LIMIT_CLOCK);
                return;
            }

            limit_time -= 1000;
            if (limit_time <= 0) {

                keyboard.setRightButtonTitle("");
                keyboard.setRightButtonResourceId(R.drawable.button_keyboard_switch_icon);

                Clock.shareInstance().removeListener(TAG_NOTICE_LIMIT_CLOCK);
                return;
            }

            //设置键盘文案
            int sec = (int)(limit_time / 1000);
//            int min = sec / 60;
//            sec = sec % 60;

            String str = String.format("%ds",sec);
            keyboard.setRightButtonTitle(str);
            //设置键盘右按钮背景图标
            keyboard.setRightButtonResourceId(R.drawable.tag_icon_bg);
        }
    };

    private int customButtonKey;
    UILockScreenKeyboard.KeyboardListener keyboardListener = new UILockScreenKeyboard.KeyboardListener() {
        @Override
        public void onKeyboardDidLoad(UILockScreenKeyboard keyboard) {
            keyboard.setRightButtonResourceId(R.drawable.button_keyboard_switch_icon);

            //需要计时读秒
            if (BarrageCenter.shareInstance().isLimitSendingTagNotice()) {
                limit_time = BarrageCenter.shareInstance().limitSendingTagNoticeTime();
                Clock.shareInstance().addListener(limitClock,TAG_NOTICE_LIMIT_CLOCK);
            }
        }

        @Override
        public void onSendButtonClick(UILockScreenKeyboard keyboard, View sender) {
            sendAction(keyboard.text(),customButtonKey);//发送消息
        }

        @Override
        public boolean onRightButtonClick(UILockScreenKeyboard keyboard, View sender) {
            if (BarrageCenter.shareInstance().isLimitSendingTagNotice()) {
                return true;
            }
            return false;
        }

        @Override
        public void onScopeViewClick(UILockScreenKeyboard keyboard, View sender) {
            UILockScreenKeyboard.dismiss();
        }

        @Override
        public void onCustomButtonClick(UILockScreenKeyboard keyboard, View sender, int buttonKey) {
            if (buttonKey == customButtonKey) {
                customButtonKey = 0;
                keyboard.setRightButtonTitle("");
                keyboard.setRightButtonResourceId(R.drawable.button_keyboard_switch_icon);
            } else {
                customButtonKey = buttonKey;

                if (sender instanceof KeyboardButton) {
                    sender.setSelected(true);
                }

                //设置键盘文案
                String keyName = UIDic.bubbleTagResourceId(Convert.noticeCategory(buttonKey));
                keyboard.setRightButtonTitle(keyName);

                //设置键盘右按钮背景图标
                keyboard.setRightButtonResourceId(R.drawable.tag_icon_bg);
            }
        }

        @Override
        public void onKeyboardChanged(UILockScreenKeyboard keyboard, int newHeight, int oldHeight) {

        }

        @Override
        public void onKeyboardStatusChanged(UILockScreenKeyboard keyboard, boolean isShow) {
            if (!isShow && !keyboard.isCustomKeyboardShow()) {
                keyboard.dismiss();//直接隐藏
                LBService.shareInstance().stop();
            }
        }
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

    private BubbleCellModel.BubbleCellListener bubbleCellListener = new BubbleCellModel.BubbleCellListener() {
        @Override
        public void onMessageClick(BubbleCellModel cellModel, NoticeBiz.Notice notice1) {

        }

        @Override
        public void onHeaderClick(BubbleCellModel cellModel,final NoticeBiz.Notice notice1) {
            PageCenter.checkAuth(new PageCenter.AuthCallBack() {
                @Override
                public void auth(String account) {
                    Bundle bundle = new Bundle();

                    if (notice1.creatorId == UserCenter.shareInstance().UID()) {
                        return;
                    }

                    //若不返回经纬度，则不让聊天
                    if (TextUtils.isEmpty(notice1.longitude) || TextUtils.isEmpty(notice1.latitude)) {
                        return;
                    }

                    bundle.putLong(Constants.PAGE_ARG_OTHER_ID,notice1.creatorId);

                    MapMarkPoint point = new MapMarkPoint();
                    point.uid = notice1.creatorId;
                    point.nick = notice1.creator;
                    point.longitude = Double.parseDouble(notice1.longitude);
                    point.latitude = Double.parseDouble(notice1.latitude);
                    point.message = notice1.content;
                    bundle.putSerializable(Constants.PAGE_ARG_LATEST_RECEIVE_MESSAGE,point);

                    Navigator.shareInstance().openURL(PageURLs.MAP_CHAT_URL,bundle);
                }
            });
        }

        @Override
        public void onErrorTagClick(BubbleCellModel cellModel, NoticeBiz.Notice notice1) {
            //重新发送
            resendNotice(cellModel);
        }

        @Override
        public void onDisappear(BubbleCellModel cellModel, NoticeBiz.Notice notice1) {
            tableViewAdapter().removeCell(cellModel);//移除即可
        }
    };

    private BubbleCellModel appendNoticeCellModel(NoticeBiz.Notice notice) {
        BubbleCellModel model = null;
        if (notice.creatorId == UserCenter.shareInstance().UID()) {
            model = new SendBubbleCellModel();
        } else {
            model = new ReceivedBubbleCellModel();
        }
        model.notice = notice;
        model.cellListener = bubbleCellListener;

        tableViewAdapter().beginUpdate();
        tableViewAdapter().appendCell(model);

        //添加ui刷新器
        model.autoDisappear = true;
        if (tableViewAdapter().getCount() > 40) {
            tableViewAdapter().removeCells(0,2);//太多时删除一部分
        }

        tableViewAdapter().endUpdate();

        Clock.shareInstance().addListener(popClock,TAG_NOTICE_POP_CLOCK);

        return model;
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
        notice.id = "sending:" + Utils.getServerTime();

        final BubbleCellModel model = appendNoticeCellModel(notice);

        //先收起键盘，还原键盘状态
        UILockScreenKeyboard.dismiss();
//        Keyboard.barrageKeyboard().dismiss(false);
//        Keyboard.barrageKeyboard().setText("");
//        Keyboard.barrageKeyboard().setRightButtonTitle("");
//        Keyboard.barrageKeyboard().setRightButtonResourceId(R.drawable.button_keyboard_switch_icon);
        customButtonKey = 0;

        BarrageCenter.shareInstance().publishNotice(notice, new RPC.Response<NoticeBiz.Notice>() {
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }

            @Override
            public void onSuccess(NoticeBiz.Notice notice1) {
                super.onSuccess(notice1);

                model.disabled = true;
                notice.id = notice1.id;//获取新的id
                model.autoDisappear = true;


                int row = tableViewAdapter().row(model);
                if (row >= 0) {
                    tableViewAdapter().updateCell(model, row);
                }

                //如果键盘再次弹起，开始计时
                if (notice.category == NoticeBiz.NoticeCategory.NAN) {
                    //需要计时读秒
                    if (BarrageCenter.shareInstance().isLimitSendingTagNotice()) {
                        limit_time = BarrageCenter.shareInstance().limitSendingTagNoticeTime();
                        Clock.shareInstance().addListener(limitClock,TAG_NOTICE_LIMIT_CLOCK);
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                super.onFailure(e);
                Utils.toastException(e, Res.localized(R.string.send_failed));

                notice.id = "";
                model.disabled = false;

                int row = tableViewAdapter().row(model);
                if (row >= 0) {
                    tableViewAdapter().updateCell(model, row);
                }
            }
        });
    }

    public void resendNotice(final BubbleCellModel model) {

        final  NoticeBiz.Notice notice = model.notice;
        notice.id = "sending:" + Utils.getServerTime();

        NoticeBiz.create(notice,new RPC.Response<NoticeBiz.Notice>(){
            @Override
            public void onStart() {
                super.onStart();
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }

            @Override
            public void onSuccess(NoticeBiz.Notice notice1) {
                super.onSuccess(notice1);

                model.disabled = true;
                notice.id = notice1.id;//获取新的id
                if (notice.category == NoticeBiz.NoticeCategory.NAN) {
                    model.autoDisappear = true;
                }

                int row = tableViewAdapter().row(model);
                if (row >= 0) {
                    tableViewAdapter().updateCell(model, row);
                }
            }

            @Override
            public void onFailure(Exception e) {
                super.onFailure(e);
                Utils.toastException(e, Res.localized(R.string.send_failed));

                notice.id = "";
                model.disabled = false;

                int row = tableViewAdapter().row(model);
                if (row >= 0) {
                    tableViewAdapter().updateCell(model, row);
                }
            }
        });
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
//        Keyboard.barrageKeyboard().dismiss(false);
        UILockScreenKeyboard.dismiss();
    }

}
