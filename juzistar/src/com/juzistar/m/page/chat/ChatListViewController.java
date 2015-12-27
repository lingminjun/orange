package com.juzistar.m.page.chat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import com.juzistar.m.R;
import com.juzistar.m.biz.MessageBiz;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.biz.lbs.LBService;
import com.juzistar.m.biz.msg.MessageCenter;
import com.juzistar.m.constants.Constants;
import com.juzistar.m.entity.MapMarkPoint;
import com.juzistar.m.page.PageURLs;
import com.juzistar.m.page.base.BaseTableViewController;
import com.juzistar.m.view.chat.SessionCell;
import com.juzistar.m.view.chat.SessionCellModel;
import com.ssn.framework.foundation.BroadcastCenter;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 15/11/28.
 */
public class ChatListViewController extends BaseTableViewController {

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.string.encounters));
        tabItem().setTabName(Res.localized(R.string.encounters));
        tabItem().setTabImage(R.drawable.tab_selector_chat);
        tabItem().setTabNameColor(Res.colorState(R.color.tab_selector_color));

        navigationItem().setBottomLineHidden(false);
        navigationItem().setTitleColor(Res.color(R.color.ssn_normal_text));
        navigationItem().backItem().setHidden(true);

        if (MessageCenter.shareInstance().unreadCount() > 0) {
            tabItem().setBadgeValue(UITabBar.TabItem.BADGE_VALUE_DOT_VALUE);
        }

        BroadcastCenter.shareInstance().addObserver(this, MessageCenter.RECEIVED_MSG_NOTIFICATION, observerMethod);
    }

    BroadcastCenter.Method<ChatListViewController> observerMethod = new BroadcastCenter.Method<ChatListViewController>() {
        @Override
        public void onReceive(ChatListViewController observer, Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(MessageCenter.RECEIVED_MSG_NOTIFICATION)) {
                if (MessageCenter.shareInstance().unreadCount() > 0) {
                    observer.tabItem().setBadgeValue(UITabBar.TabItem.BADGE_VALUE_DOT_VALUE);
                }

                if (observer.tableViewAdapter() != null) {
                    observer.tableViewAdapter().refresh();
                }
            }
        }
    };


    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.chat_list_layout, null);
        UITableView tableView = (UITableView)view.findViewById(R.id.table_view);
        UITableView.TableViewAdapter adapter = new UITableView.TableViewAdapter(tableView);
        setTableView(tableView,adapter);
        return view;
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        //获取最近的聊天列表
    }

    @Override
    public void onViewDidAppear() {
        super.onViewDidAppear();

        tableViewAdapter().reload();

        if (MessageCenter.shareInstance().unreadCount() > 0) {
            tabItem().setBadgeValue(UITabBar.TabItem.BADGE_VALUE_DOT_VALUE);
        } else {
            tabItem().setBadgeValue("");
        }
    }

    UITableViewCell.CellModelLongClick itemLongClick = new UITableViewCell.CellModelLongClick() {
        @Override
        public boolean onLongClick(View var, final UITableViewCell.CellModel model) {
            final SessionCellModel m = (SessionCellModel)model;

            UIAlert.showAlert(getActivity(),
                    Res.localized(R.string.whether_delete_session),
                    Res.localized(R.string.ok),
                    Res.localized(R.string.cancel), new UIAlertButtonClick() {
                        @Override
                        public void onClick(Dialog dialog, String btnTitle) {
                            if (btnTitle.equals(Res.localized(R.string.ok))) {
                                tableViewAdapter().removeCell(model);
                                MessageCenter.shareInstance().removeSession(m.session.sid);
                            }
                        }
                    });

            return true;
        }
    };

    @Override
    public List<? extends UITableViewCell.CellModel> tableViewLoadCells(UITableView.TableViewAdapter adapter) {
        List<UITableViewCell.CellModel> list = new ArrayList<>();

        List<MessageCenter.Session> sessions = MessageCenter.shareInstance().getSessions();
        for (MessageCenter.Session session : sessions) {
            SessionCellModel model = new SessionCellModel();
            model.longClick = itemLongClick;
            model.session = session;
            list.add(model);
        }

        return list;
    }

    @Override
    public void onTableViewCellClick(UITableView.TableViewAdapter adapter, UITableViewCell.CellModel cellModel, int row) {
        super.onTableViewCellClick(adapter, cellModel, row);

        SessionCellModel model = (SessionCellModel)cellModel;
        //取私聊页面
        Bundle bundle = new Bundle();

        bundle.putLong(Constants.PAGE_ARG_OTHER_ID,model.session.other);


        if (!TextUtils.isEmpty(model.session.lastRcvMsg)) {
            MapMarkPoint point = new MapMarkPoint();
            point.uid = model.session.other;
            point.nick = model.session.otherName;
            point.longitude = model.session.lastLng;
            point.latitude = model.session.lastLat;
            point.message = model.session.lastRcvMsg;
            bundle.putSerializable(Constants.PAGE_ARG_LATEST_RECEIVE_MESSAGE, point);
        }

        if (!TextUtils.isEmpty(model.session.lastSndMsg)) {
            MapMarkPoint point = new MapMarkPoint();
            point.uid = UserCenter.shareInstance().UID();
            point.nick = UserCenter.shareInstance().user().nick;
            point.longitude = LBService.shareInstance().getLatestLongitude();
            point.latitude = LBService.shareInstance().getLatestLatitude();
            point.message = model.session.lastSndMsg;
            bundle.putSerializable(Constants.PAGE_ARG_LATEST_SEND_MESSAGE, point);
        }

        Navigator.shareInstance().openURL(PageURLs.MAP_CHAT_URL,bundle);
    }


}
