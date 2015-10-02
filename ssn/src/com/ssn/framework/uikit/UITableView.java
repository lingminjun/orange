package com.ssn.framework.uikit;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.ssn.framework.R;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.pullview.PullToRefreshBase;
import com.ssn.framework.uikit.pullview.PullToRefreshListView;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lingminjun on 15/9/27.
 */
public class UITableView extends PullToRefreshListView {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static class TableViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener,AbsListView.OnScrollListener,PullToRefreshBase.OnRefreshListener2<ListView> {

        public static interface TableViewDelegate {
            public List<UITableViewCell.CellModel> tableViewLoadCells(TableViewAdapter adapter);
            public void onTableViewCellClick(TableViewAdapter adapter, UITableViewCell.CellModel cellModel, int row);
            public void onTableViewPullDownRefresh(TableViewAdapter adapter);
            public void onTableViewPullUpRefresh(TableViewAdapter adapter);
        }

        protected List<UITableViewCell.CellModel> _items = new ArrayList<UITableViewCell.CellModel>();

        private Map<String,Integer> _types = new HashMap<>();
        private UITableView _tableView;
        private TableViewDelegate _delegate;
        private int _viewTypeCount;

        public TableViewDelegate delegate() {return _delegate;}
        public void setDelegate(TableViewDelegate delegate) {_delegate = delegate;}

        public int cellTypeCount(){return _viewTypeCount;}

        public TableViewAdapter(UITableView tableView){
            _tableView = tableView;
            _viewTypeCount = 8;//默认支持八种
            _tableView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);//默认只开启下拉模式
            _tableView.setAdapter(this);
            updateRefreshLabel();
            setListeners();
        }

        public TableViewAdapter(UITableView tableView,int cellTypeCount){
            _tableView = tableView;
            _viewTypeCount = cellTypeCount < 8 ? 8 : cellTypeCount;
            _tableView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);//默认只开启下拉模式
            _tableView.setAdapter(this);
            updateRefreshLabel();
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

            //去掉tableview所获取的键盘
            InputMethodManager imm = (InputMethodManager)Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && imm.isAcceptingText()) {
                View txt = _tableView.findFocus();
                if (txt != null && txt instanceof EditText) {
//                    Log.e("hidden","txt");
                    imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);
                    txt.clearFocus();
                }
            }

            int row = i-1;//header view

            UITableViewCell.CellModel cellModel = this.getItem(row);
            Log.e("onClick","idx="+i+"model="+cellModel.getClass().getSimpleName());

            if (cellModel == null) {
                return ;
            }

            if (cellModel.click != null) {
                cellModel.click.onClick(view);
            }

            if (_delegate != null) {
                _delegate.onTableViewCellClick(this, cellModel, row);
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
                _delegate.onTableViewPullDownRefresh(this);
            }
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            if (_delegate != null) {
                _delegate.onTableViewPullUpRefresh(this);
            }
        }

        public UITableView tableView() {return _tableView;}

        @Deprecated
        public void setItems(List<UITableViewCell.CellModel> items) {
            this._items.clear();
            if (items != null) {
                this._items.addAll(items);
                checkSynchronize();
            }
        }

        /**
         * 追加，界面自动更新
         * @param cellModel
         */
        public void appendCell(UITableViewCell.CellModel cellModel) {
            if (cellModel == null) {return;}

            this._items.add(cellModel);

            //刷新界面，以后再优化仅仅刷新单行数据
            checkSynchronize();
        }

        /**
         * 插入，界面自动更新
         * @param cellModel
         * @param row 越界将忽略
         */
        public void insertCell(UITableViewCell.CellModel cellModel,int row) {
            if (cellModel == null) {return;}

            if (row < 0 || row > this._items.size()) {return;}

            this._items.add(row,cellModel);

            //刷新界面，以后再优化仅仅刷新单行数据
            checkSynchronize();
        }

        /**
         * 更新，自动刷新界面
         * @param cellModel 更新新的内容，若传入null，原数据发生改变，也会被理解更新
         * @param row 越界将忽略
         */
        public void updateCell(UITableViewCell.CellModel cellModel,int row) {
            if (row < 0 || row > this._items.size()) {return;}

            //刷新界面，以后再优化仅仅刷新单行数据
            checkSynchronize();
        }

        /**
         * 追加，界面自动更新，如loadmore场景
         * @param cellModels
         */
        public void appendCells(List<UITableViewCell.CellModel> cellModels) {
            if (cellModels == null || cellModels.size() == 0) {return;}

            this._items.addAll(cellModels);

            //刷新界面
            checkSynchronize();
        }

        public void insertCells(List<UITableViewCell.CellModel> cellModels, int row) {
            if (cellModels == null || cellModels.size() == 0) {return;}

            if (row < 0 || row > this._items.size()) {return;}

            this._items.addAll(row,cellModels);

            //刷新界面，以后再优化仅仅刷新单行数据
            checkSynchronize();
        }

        /**
         * 删除单行数据，
         * @param row 越界将忽略
         */
        public void removeCell(int row) {

            if (row < 0 || row >= this._items.size()) {return;}

            this._items.remove(row);

            //刷新界面
            checkSynchronize();
        }

        /**
         * 删除单行数据
         * @param cellModel
         */
        public void removeCell(UITableViewCell.CellModel cellModel) {
            if (cellModel == null) {return;}

            if (!_items.contains(cellModel)) {return;}

            this._items.remove(cellModel);

            //刷新界面
            checkSynchronize();
        }

        /**
         * 删除所有，界面自动更新
         */
        public void removeAll() {

            this._items.clear();
            //刷新界面
            checkSynchronize();
        }

        private boolean _modifying;//数据修改中

        /**
         * 集中更新机制，此方法必须成对出现，否则将产出异常
         */
        public void beginUpdate() {_modifying = true;}
        public void endUpdate(){
            if (_modifying) {
                _modifying = false;
                notifyDataSetChanged();
            }
        }

        private void checkSynchronize() {
            if (!_modifying) {
                synchronizeToUI();
            }
        }

        private void synchronizeToUI() {
            _modifying = false;

            //防止不必要的刷新
            Context context = _tableView.getContext();
            if (context == null) {return;}
            if (context instanceof Activity && ((Activity) context).isFinishing()) {return;}

            //刷新界面
            notifyDataSetChanged();
        }

        /**
         * 停止加载
         */
        public void completedLoad() {
            _tableView.onRefreshComplete();
            updateRefreshLabel();
        }

        private void updateRefreshLabel() {
            String dateStr = dateFormat.format(new Date());
            _tableView.getLoadingLayoutProxy().setLastUpdatedLabel(Res.localized(R.string.header_last_time) + dateStr);
        }

        @Override
        public int getCount() {
//            Log.e("size",""+_items.size());
            return _items.size();
        }

        @Override
        public UITableViewCell.CellModel getItem(int i) {
            if (i < 0 || i >= _items.size()) {return null;}
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
        public int getViewTypeCount() {//默认最大为4个
            return _viewTypeCount;
//            int i = super.getViewTypeCount();
//            Log.e("getViewTypeCount","count="+0);
//            if (_types.size() > 10) {
//                return _types.size() + 1;
//            }
//            return 10;//设置一个非常大的数，使得后面cellModel的hashCode都小于此值
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public int getItemViewType(int position) {
            //此方法调用非常频繁，
            UITableViewCell.CellModel cellModel = getItem(position);
//            Log.e("getType","idx="+position+"model="+cellModel.getClass().getName());

            if (cellModel != null) {
                String id = cellModel.cellIdentifier();
                Integer type = _types.get(id);
                if (type != null) {
                    return type;
                }

                int size = _types.size();
                _types.put(id,size+1);
                return size+1;
            }

            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            UITableViewCell.CellModel cellModel = this.getItem(i);
//            Log.e("getView","idx="+i+"model="+cellModel.getClass().getSimpleName());
            if (cellModel == null) {
                if (view == null) {
                    return new View(viewGroup.getContext());
                }
                return view;
            }

            UITableViewCell cell = null;
            if (view != null && view instanceof UITableViewCell) {
                cell = (UITableViewCell)view;
            }
            else {
                cell = UITableViewCell.newInstance(cellModel,_tableView.getContext());
            }

            //展示内容
            UITableViewCell.displayCell(cell,cellModel,i);

            return cell;
        }

        /**
         * 刷新数据
         */
        public void reload() {
            _items.clear();

            List<UITableViewCell.CellModel> items = null;
            if (_delegate != null) {
                items = _delegate.tableViewLoadCells(this);
            }
            if (items != null && items.size() > 0) {
                _items.addAll(items);
            }

            //强制刷新界面
            synchronizeToUI();
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
