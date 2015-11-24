package com.ssn.framework.uikit;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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

    /**
     * 务必在load view中调用
     * @param tableView
     * @param adapter
     */
    protected void setTableView(UITableView tableView,UITableView.TableViewAdapter adapter) {
        _tableView = tableView;
        if (adapter != null && adapter.tableView() == tableView) {
            _adapter = adapter;
        } else {
            _adapter = new UITableView.TableViewAdapter(_tableView);
        }
        _adapter.setDelegate(this);
    }

    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        //加载数据
        _adapter.reload();
    }

    @Override
    public List<? extends UITableViewCell.CellModel> tableViewLoadCells(UITableView.TableViewAdapter adapter) {
        return null;
    }

    @Override
    public void onTableViewCellClick(UITableView.TableViewAdapter adapter, UITableViewCell.CellModel cellModel, int row) {

    }

    @Override
    public void onTableViewPullDownRefresh(UITableView.TableViewAdapter adapter) {adapter.completedLoad();}

    @Override
    public void onTableViewPullUpRefresh(UITableView.TableViewAdapter adapter) {adapter.completedLoad();}
}
