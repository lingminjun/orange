package com.juzistar.m.page.location;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.juzistar.m.R;
import com.juzistar.m.biz.msg.MessageCenter;
import com.juzistar.m.page.base.BaseTableViewController;
import com.juzistar.m.page.base.BaseViewController;
import com.juzistar.m.view.chat.SessionCellModel;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UITableView;
import com.ssn.framework.uikit.UITableViewCell;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lingminjun on 15/11/29.
 */
public class LocationViewController extends BaseTableViewController {

    private ViewGroup mCancelPanel;
    EditText searchText;

    //pio功能实现
    private PoiSearch mPoiSearch;
    private SuggestionSearch mSuggestionSearch;

    private void loadPoiSevice() {
        if (mPoiSearch == null) {
            // 初始化搜索模块，注册搜索事件监听
            mPoiSearch = PoiSearch.newInstance();

            mPoiSearch.setOnGetPoiSearchResultListener(onGetPoiSearchResultListener);
            mSuggestionSearch = SuggestionSearch.newInstance();
            mSuggestionSearch.setOnGetSuggestionResultListener(onGetSuggestionResultListener);

        }
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.manul_location_layout, null);
        UITableView tableView = (UITableView)view.findViewById(R.id.table_view);
        UITableView.TableViewAdapter adapter = new UITableView.TableViewAdapter(tableView);
        setTableView(tableView,adapter);
        return view;
    }

    private void loadSearchBar() {
        RelativeLayout layout = (RelativeLayout) findViewById(R.id.top_search_bar);

        //返回按钮
        View left = layout.findViewById(R.id.left_btn_layout);
        left.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        }));

        searchText = (EditText) layout.findViewById(R.id.search_edit);
        searchText.setOnEditorActionListener(actionListener);
        watcher = UIEvent.watcher(searchWatcher);
        searchText.addTextChangedListener(watcher);
        searchText.setOnFocusChangeListener(searchFocus);

        //取消按钮
        mCancelPanel = (ViewGroup) layout.findViewById(R.id.cancel_btn_panel);
        mCancelPanel.setVisibility(View.GONE);
        mCancelPanel.setOnClickListener(UIEvent.click(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clearResults();
            }
        }));
    }


    @Override
    public void onViewDidLoad() {
        super.onViewDidLoad();

        loadSearchBar();//加载search bar

        loadPoiSevice();//搜索组件加载
    }

    @Override
    public void onDestroyController() {
        super.onDestroyController();

        mPoiSearch.destroy();
        mSuggestionSearch.destroy();
    }

    @Override
    public List<? extends UITableViewCell.CellModel> tableViewLoadCells(UITableView.TableViewAdapter adapter) {
        List<UITableViewCell.CellModel> list = new ArrayList<>();

//        List<MessageCenter.Session> sessions = MessageCenter.shareInstance().getSessions();
//        for (MessageCenter.Session session : sessions) {
//            SessionCellModel model = new SessionCellModel();
//            model.session = session;
//            list.add(model);
//        }

        return list;
    }

    private String getSearchString() {
        Editable editable = searchText.getText();
        if (editable != null) {
            return editable.toString().trim();
        }
        return "";
    }

    private View.OnFocusChangeListener searchFocus = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (b) {
                showCancelButton();
            } else {
                hideCancelButton();
            }
        }
    };

    private TextView.OnEditorActionListener actionListener = new TextView.OnEditorActionListener() {
        @Override
        public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            final String searchStr = getSearchString();
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                //do something;
                if (TextUtils.isEmpty(searchStr)) {
//                    Utils.toast(getActivity(),Res.localized(R.string.));
                    return false;
                } else {
                    searchLocation(searchStr);
                    cancelSearch(false);
                }
                return true;
            }
            return false;
        }
    };

    UIEvent.Watcher watcher;
    private TextWatcher searchWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s != null && !TextUtils.isEmpty(s.toString())) {
                suggestionSearch(s.toString());
            } else {
                clearResults();
            }
        }
    };

    private void suggestionSearch(String string) {

    }

    //开始搜索
    private void searchLocation(String string) {

    }

    //清除结果
    private void clearResults() {
        cancelSearch(true);

        //删除搜索结果
        tableViewAdapter().removeAll();
    }


    private void showCancelButton() {
        mCancelPanel.setVisibility(View.VISIBLE);
    }

    private void hideCancelButton() {
        mCancelPanel.setVisibility(View.GONE);
    }

    private void cancelSearch(boolean clear) {

        if (watcher != null) {
            searchText.removeTextChangedListener(watcher);
        }

        //清空输入
        InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && imm.isAcceptingText()) {
            imm.hideSoftInputFromWindow(searchText.getWindowToken(), 0);
        }
        searchText.clearFocus();

        //在输入时点取消则清空输入
        if (clear) {
            searchText.setText("");
        }

        if (watcher == null) {
            watcher = UIEvent.watcher(searchWatcher);
        }
        searchText.addTextChangedListener(watcher);
    }

    /**
     * 输入时推荐结果
     */
    OnGetSuggestionResultListener onGetSuggestionResultListener = new OnGetSuggestionResultListener() {
        @Override
        public void onGetSuggestionResult(SuggestionResult suggestionResult) {

        }
    };

    /**
     * 点击搜索后出来结果
     */
    private OnGetPoiSearchResultListener onGetPoiSearchResultListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult poiResult) {

        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }
    };
}
