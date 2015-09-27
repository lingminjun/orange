package com.ssn.framework.uikit;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import com.ssn.framework.uikit.pullview.PullToRefreshBase;
import com.ssn.framework.uikit.pullview.PullToRefreshListView;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 15/9/27.
 */
public class UITableView extends PullToRefreshListView {

    public static class TableViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener,AbsListView.OnScrollListener,PullToRefreshBase.OnRefreshListener2<ListView> {

        public static interface TableViewDelegate {
            public List<UITableViewCell.CellModel> tableViewAdapterLoadData(TableViewAdapter adapter);
            public void tableViewAdapterItemClick(TableViewAdapter adapter,UITableViewCell.CellModel cellModel,int row);
            public void tableViewAdapterPullDownRefresh(TableViewAdapter adapter);
            public void tableViewAdapterPullUpRefresh(TableViewAdapter adapter);
        }

        protected List<UITableViewCell.CellModel> _items = new ArrayList<UITableViewCell.CellModel>();
        private UITableView _tableView;
        private TableViewDelegate _delegate;

        public TableViewDelegate delegate() {return _delegate;}
        public void setDelegate(TableViewDelegate delegate) {_delegate = delegate;}

        public TableViewAdapter(UITableView tableView){
            this._tableView = tableView;
            setListeners();
        }

        private void setListeners() {
            _tableView.setOnItemLongClickListener(ViewEvent.itemLongClick(this));
            _tableView.setOnItemClickListener(ViewEvent.itemClick(this));
            _tableView.setOnScrollListener(this);
            _tableView.setOnRefreshListener(this);
        }

        /**
         * AdapterView.OnItemClickListener
         */
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            int row = i-1;//header view

            UITableViewCell.CellModel cellModel = this.getItem(row);

            if (cellModel == null) {
                return ;
            }

            if (cellModel.click != null) {
                cellModel.click.onClick(view);
            }

            if (_delegate != null) {
                _delegate.tableViewAdapterItemClick(this, cellModel,row);
            }
        }

        /**
         * AdapterView.OnItemLongClickListener
         */
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {

            int row = i-1;//header view

            UITableViewCell.CellModel cellModel = this.getItem(row);

            if (cellModel == null) {
                return false;
            }

            if (cellModel.longClick != null) {
                return cellModel.longClick.onLongClick(view);
            }

            return false;
        }

        /**
         * AbsListView.OnScrollListener()
         */
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {}

        @Override
        public void onScroll(AbsListView absListView, int i, int i1, int i2) {}

        /**
         * PullToRefreshBase.OnRefreshListener2<ListView> 实现
         */
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
            if (_delegate != null) {
                _delegate.tableViewAdapterPullDownRefresh(this);
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            if (_delegate != null) {
                _delegate.tableViewAdapterPullUpRefresh(this);
            }
        }

        public UITableView tableView() {return _tableView;}

        @Deprecated
        public void setItems(List<UITableViewCell.CellModel> items) {
            this._items.clear();
            if (items != null) {
                this._items.addAll(items);
            }
        }

        @Deprecated
        public void addItem(UITableViewCell.CellModel entity) {
            this._items.add(entity);
        }

        @Deprecated
        public void addItems(List<UITableViewCell.CellModel> entities) {
            this._items.addAll(entities);
        }

        @Deprecated
        public void clearAll() {
            this._items.clear();
        }

        @Override
        public int getCount() {
            return _items.size();
        }

        @Override
        public UITableViewCell.CellModel getItem(int i) {
            if (i < 0 && i >= _items.size()) {return null;}
            return _items.get(i);
        }

        @Override
        public long getItemId(int i) {
            UITableViewCell.CellModel cellModel = getItem(i);
            if (cellModel != null) {
                return cellModel.getModelID();
            }
            return -1 + i;//防止与原有 数据id一致
        }

        @Override
        public int getItemViewType(int position) {
            UITableViewCell.CellModel cellModel = getItem(position);
            if (cellModel != null) {
                return cellModel.getCellType();
            }
            return super.getItemViewType(position);
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            UITableViewCell.CellModel cellModel = this.getItem(i);

            if (cellModel == null) {
                return view;
            }

            UITableViewCell cell = (UITableViewCell)view;
            if (cell != null) {
                cell = UITableViewCell.newInstance(cellModel,_tableView.getContext());
            }

            return cell;
        }

        /**
         * 刷新数据
         */
        public void reload() {
            List<UITableViewCell.CellModel> items = null;
            if (_delegate != null) {
                items = _delegate.tableViewAdapterLoadData(this);
            }
            _items.clear();
            if (items != null && items.size() > 0) {
                _items.addAll(items);
            }

            //防止不必要的刷新
            Context context = _tableView.getContext();
            if (context == null) {return;}
            if (context instanceof Activity && ((Activity) context).isFinishing()) {return;}

            //刷新界面
            notifyDataSetChanged();
        }
    }

    public UITableView(Context context) {
        super(context);
    }

    public UITableView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public UITableView(Context context, Mode mode) {
        super(context, mode);
    }

    public UITableView(Context context, Mode mode, AnimationStyle style) {
        super(context, mode, style);
    }

}
