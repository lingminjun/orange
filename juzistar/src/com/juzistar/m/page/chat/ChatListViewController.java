package com.juzistar.m.page.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import com.juzistar.m.R;
import com.juzistar.m.biz.msg.MessageCenter;
import com.juzistar.m.page.base.BaseTableViewController;
import com.juzistar.m.view.chat.SessionCell;
import com.juzistar.m.view.chat.SessionCellModel;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UITableView;
import com.ssn.framework.uikit.UITableViewCell;

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

        navigationItem().setBottomLineHidden(false);
        navigationItem().setTitleColor(Res.color(R.color.ssn_normal_text));
    }

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
    public List<? extends UITableViewCell.CellModel> tableViewLoadCells(UITableView.TableViewAdapter adapter) {
        List<UITableViewCell.CellModel> list = new ArrayList<>();

        List<MessageCenter.Session> sessions = MessageCenter.shareInstance().getSessions();
        for (MessageCenter.Session session : sessions) {
            SessionCellModel model = new SessionCellModel();
            model.session = session;
            list.add(model);
        }

        return list;
    }

    @Override
    public void onTableViewCellClick(UITableView.TableViewAdapter adapter, UITableViewCell.CellModel cellModel, int row) {
        super.onTableViewCellClick(adapter, cellModel, row);

        //取私聊页面
    }
}
