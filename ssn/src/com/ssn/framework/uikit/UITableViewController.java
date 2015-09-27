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
        _tableView =  (UITableView)inflater.inflate(R.layout.ssn_table_view_controller, null);
        _adapter = new UITableView.TableViewAdapter(_tableView);
        _adapter.setDelegate(this);
        return _tableView;
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
    public void tableViewAdapterPullDownRefresh(UITableView.TableViewAdapter adapter) {

    }

    @Override
    public void tableViewAdapterPullUpRefresh(UITableView.TableViewAdapter adapter) {

    }
}
