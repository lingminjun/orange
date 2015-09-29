package com.ssn.framework.uikit;

import android.view.LayoutInflater;
import android.view.View;
import com.ssn.framework.R;

import java.util.List;

/**
 * Created by lingminjun on 15/9/27.
 */
public class UITableViewController extends UIViewController implements UITableView.TableViewAdapter.TableViewDelegate {

    UITableView _tableView;

    UITableView.TableViewAdapter _adapter;

    protected UITableView tableView() {return _tableView;}
    protected UITableView.TableViewAdapter tableViewAdapter() {return _adapter;}

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.ssn_table_view_controller, null);
        setTableView((UITableView)view);
        return view;
    }

    /**
     * 务必在load view中调用
     * @param tableView
     */
    protected void setTableView(UITableView tableView) {
        _tableView = tableView;
        _adapter = new UITableView.TableViewAdapter(_tableView);
        _adapter.setDelegate(this);
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        //加载数据
        _adapter.reload();
    }

    @Override
    public List<UITableViewCell.CellModel> tableViewAdapterLoadData(UITableView.TableViewAdapter adapter) {
        return null;
    }

    @Override
    public void tableViewAdapterItemClick(UITableView.TableViewAdapter adapter, UITableViewCell.CellModel cellModel, int row) {

    }

    @Override
    public void tableViewAdapterPullDownRefresh(UITableView.TableViewAdapter adapter) {adapter.completedLoad();}

    @Override
    public void tableViewAdapterPullUpRefresh(UITableView.TableViewAdapter adapter) {adapter.completedLoad();}
}