package com.ssn.framework.uikit;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.ssn.framework.R;
import com.ssn.framework.foundation.Density;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.pullview.*;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lingminjun on 15/9/27.
 */
public class UITableView extends RelativeLayout /*PullToRefreshListView*/ {

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    public static class TableViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener,AdapterView.OnItemLongClickListener,AbsListView.OnScrollListener,PullToRefreshBase.OnRefreshListener2<ListView> {

        public static interface TableViewDelegate {
            public List<? extends UITableViewCell.CellModel> tableViewLoadCells(TableViewAdapter adapter);
            public void onTableViewCellClick(TableViewAdapter adapter, UITableViewCell.CellModel cellModel, int row);
            public void onTableViewPullDownRefresh(TableViewAdapter adapter);
            public void onTableViewPullUpRefresh(TableViewAdapter adapter);
        }

        public static final int DEFAULT_CELL_TYPE_COUNT = 8;

        protected List<UITableViewCell.CellModel> _items = new ArrayList<UITableViewCell.CellModel>();

        private Map<String,Integer> _types = new HashMap<>();
        private UITableView _tableView;
        private TableViewDelegate _delegate;
        private int _viewTypeCount;//cell类型个数
        private boolean _nonReusable;//cell可复用
        private boolean _disableScroll;//不能滑动

        public TableViewDelegate delegate() {return _delegate;}
        public void setDelegate(TableViewDelegate delegate) {_delegate = delegate;}

        public int cellTypeCount(){return _viewTypeCount;}

        public boolean isReusable() { return !_nonReusable; }

        public TableViewAdapter(UITableView tableView){
            init(tableView, 0, true);
        }

        public TableViewAdapter(UITableView tableView,int cellTypeCount){
            init(tableView,cellTypeCount,true);
        }

        public TableViewAdapter(UITableView tableView,boolean reusable){
            init(tableView,0,reusable);
        }

        private void init(UITableView tableView,int cellTypeCount,boolean reusable) {
            _tableView = tableView;
            _nonReusable = !reusable;
            if (reusable) {
                _viewTypeCount = cellTypeCount < DEFAULT_CELL_TYPE_COUNT ? DEFAULT_CELL_TYPE_COUNT : cellTypeCount;
            } else {
                _viewTypeCount = 0;
            }
            _tableView._tableView.setMode(PullToRefreshBase.Mode.DISABLED);//默认只开启下拉模式
            _tableView._tableView.setAdapter(this);
            _tableView._tableView.getRefreshableView().setCacheColorHint(Color.TRANSPARENT);
            updateRefreshLabel();
            setListeners();
        }

        private void setListeners() {
            _tableView._tableView.setOnItemLongClickListener(UIEvent.itemLongClick(this));
            _tableView._tableView.setOnItemClickListener(UIEvent.itemClick(this));
            _tableView._tableView.setOnScrollListener(this);
            _tableView._tableView.setOnRefreshListener(this);
        }

        public void setPullRefreshEnabled(boolean enable) {
            this.completedLoad();
            _tableView._tableView.setPullDownEnable(enable);
        }

        public void setPullLoadMoreEnabled(boolean enable) {
            this.completedLoad();
            _tableView._tableView.setPullUpEnable(enable);
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
                try {
                    cellModel.click.onClick(view,cellModel);
                } catch (Throwable e){e.printStackTrace();}
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
                try {
                    return cellModel.longClick.onLongClick(view,cellModel);
                } catch (Throwable e){e.printStackTrace();}
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

        /**
         * 返回cellModel对应的行
         * @param cellModel
         * @return
         */
        public int row(UITableViewCell.CellModel cellModel) {
            if (cellModel == null) {return -1;}

            return this._items.indexOf(cellModel);
        }

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
         * 删除从begin row开始到后range位数，越界忽略
         * @param beginRow
         * @param range
         */
        public void removeCells(int beginRow, int range) {
            if (beginRow < 0 || beginRow >= this._items.size()) {return;}

            if (range <= 0) {return;}

            for (int i = 0; i < range; i++) {

                if (beginRow>= this._items.size()) {break;}

                this._items.remove(beginRow);
            }

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
            _tableView._tableView.onRefreshComplete();
            updateRefreshLabel();
        }

        private void updateRefreshLabel() {
            String dateStr = dateFormat.format(new Date());
            _tableView._tableView.getLoadingLayoutProxy().setLastUpdatedLabel(Res.localized(R.string.header_last_time) + dateStr);
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
            if (_nonReusable) {return 1;}
            return _viewTypeCount;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public int getItemViewType(int position) {
            if (_nonReusable) {return 0;}
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
                if (_nonReusable || view == null) {//防止cell构建异常
                    return new TextView(viewGroup.getContext());
                }
                return view;
            }

            UITableViewCell cell = null;
            if (!_nonReusable && view != null && view instanceof UITableViewCell) {
                cell = (UITableViewCell)view;
            }
            else {
                cell = UITableViewCell.newInstance(cellModel,viewGroup.getContext());
            }

            //展示内容
            if (cell != null) {
                UITableViewCell.displayCell(cell, cellModel, i);
            }

            if (cell == null) {//防止cell构建异常
                return new TextView(viewGroup.getContext());
            } else {
                return cell;
            }
        }

        /**
         * 刷新数据
         */
        public void reload() {
            _items.clear();

            List<? extends UITableViewCell.CellModel> items = null;
            if (_delegate != null) {
                items = _delegate.tableViewLoadCells(this);
            }
            if (items != null && items.size() > 0) {
                _items.addAll(items);
            }

            //强制刷新界面
            synchronizeToUI();
        }

        /**
         * 通知界面刷新，不重建数据源
         */
        public void refresh() {
            //强制刷新界面
            synchronizeToUI();
        }
    }

    public UITableView(Context context) {
        super(context);
        init();
    }

    public UITableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public UITableView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public UITableView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.ssn_table_view_layout, this);
        _tableView = (PullToRefreshListView)findViewById(R.id.ssn_list_view);
        _headerLayout = (FrameLayout)findViewById(R.id.ssn_header_layout);
        _footerLayout = (FrameLayout)findViewById(R.id.ssn_footer_layout);
    }


    private UITableViewCell targetCellWithEditText(ViewGroup group,EditText editText) {
        int id = editText.getId();

        int count = group.getChildCount();

        UITableViewCell cell = null;

        for (int i = 0; i < count; i++) {
            View v = group.getChildAt(i);
            if (v instanceof UITableViewCell) {
                View target = v.findViewById(id);
                if (target == editText) {
                    cell = (UITableViewCell)v;
                    break;
                }
            }
            else if (v instanceof ViewGroup) {
                cell = targetCellWithEditText((ViewGroup)v,editText);
                if (cell != null) {
                    break;
                }
            }
        }

        return cell;
    }

    private void findCellFromEditText(EditText editText) {
        if (editText.getId() <= 0) {
            Log.e("UITableViewCell","请务必保证cell上的EditText的id被设置");
            return;
        }

        UITableViewCell cell = targetCellWithEditText(this,editText);

        if (cell != null && cell.cellModel() != null) {
            UITableViewCell.prepareFocus(cell,editText);
        }
    }

    @Override
    protected final void onSizeChanged(int w, int h, int oldw, int oldh) {

        /*
        当触发键盘出现，造成光标无法聚焦问题修改
         */
        try {
            int changed = oldh - h;
            if (changed >= Density.dip2px(100) && changed < oldh) {
                InputMethodManager imm = (InputMethodManager)Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
                View txt = this.findFocus();
                if (imm != null && imm.isAcceptingText() && txt != null) {
//                Log.e("show","txt"+txt.hashCode());
                    findCellFromEditText((EditText)txt);
                }
            }
        }catch (Throwable e){}


        super.onSizeChanged(w, h, oldw, oldh);
    }

    private View _headerView;
    private View _footerView;

    /**
     * listview添加底部view
     *
     * @param view
     */
    public void setFooterView(View view) {
        if (_footerView != null) {
            _footerLayout.removeView(_footerView);
        }
        _footerView = view;
        if (view != null) {
            _footerLayout.addView(view);
        }
    }

    /**
     * listview添加顶部view
     *
     * @param view
     */
    public void setHeaderView(View view) {
        if (_headerView != null) {
            _headerLayout.removeView(_headerView);
        }
        _headerView = view;
        if (view != null) {
            _headerLayout.addView(view);
        }
    }

    /**
     * 设置滑动颜色
     * @param color
     */
    public void setCacheColorHint(int color) {
        if (_tableView != null) {
            _tableView.getRefreshableView().setCacheColorHint(color);
        }
    }

    /**
     * 设置是否可以滑动，默认可以滑动
     * @param enable
     */
    public void setScrollEnable(boolean enable) {
        disabledScroll = !enable;
    }

    private boolean disabledScroll;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (disabledScroll) {
            if (ev.getAction() == MotionEvent.ACTION_HOVER_MOVE
                    || ev.getAction() == MotionEvent.ACTION_MOVE) {
                return false;
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 不满屏幕时，显示在最底下
     * @param bottom
     */
    public void setStackFromBottom(boolean bottom) {
        if (_tableView != null) {
            if (bottom) {
                _tableView.getRefreshableView().setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
            }
            _tableView.getRefreshableView().setStackFromBottom(true);
        }
    }

    /**
     * 滚到最顶部
     */
    public void scrollToTop() {
        if (_tableView != null) {
            ListView listView = _tableView.getRefreshableView();
            int idx = listView.getTop();
            if (idx >= 0) {
                listView.smoothScrollToPosition(idx);
            }
        }
    }

    /**
     * 滚到最底部
     */
    public void scrollToBottom() {
        if (_tableView != null) {
            ListView listView = _tableView.getRefreshableView();
            int idx = listView.getBottom();
            if (idx >= 0) {
                listView.smoothScrollToPosition(idx);
            }
        }
    }

    /**
     * 设置底部高度
     * @param height(px) 小于等于零时自动设置成wrap_content
     */
    public void setFooterHeight(int height) {
        if (_footerLayout != null) {
            ViewGroup.LayoutParams params = _footerLayout.getLayoutParams();
            if (height > 0) {
                params.height = height;
            } else {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        }
    }

    private PullToRefreshListView _tableView;
    private FrameLayout _headerLayout;
    private FrameLayout _footerLayout;

}
