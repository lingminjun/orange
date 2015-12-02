package com.juzistar.m.page.location;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.juzistar.m.R;
import com.juzistar.m.biz.lbs.LBService;
import com.juzistar.m.biz.msg.MessageCenter;
import com.juzistar.m.page.base.BaseTableViewController;
import com.juzistar.m.page.base.BaseViewController;
import com.juzistar.m.view.chat.SessionCellModel;
import com.juzistar.m.view.com.TitleCellModel;
import com.juzistar.m.view.location.LocationCellModel;
import com.juzistar.m.view.me.BlankCellModel;
import com.ssn.framework.foundation.App;
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
    private SuggestionSearchOption option;

    private void loadPoiSevice() {
        if (mPoiSearch == null) {
            // 初始化搜索模块，注册搜索事件监听
            mPoiSearch = PoiSearch.newInstance();

            mPoiSearch.setOnGetPoiSearchResultListener(onGetPoiSearchResultListener);
            mSuggestionSearch = SuggestionSearch.newInstance();
            mSuggestionSearch.setOnGetSuggestionResultListener(onGetSuggestionResultListener);

            option = new SuggestionSearchOption();
            option.city(LBService.shareInstance().getLatestCity());

        }
    }

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.manul_location_layout, null);
        UITableView tableView = (UITableView)view.findViewById(R.id.table_view);
        UITableView.TableViewAdapter adapter = new UITableView.TableViewAdapter(tableView);
        setTableView(tableView,adapter);
        tableView.setBackgroundColor(Res.color(R.color.page_bg));
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

        searchText.clearFocus();

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

        Log.e("dddd","dddddddddddddd"+adapter.getCount());

        return list;
    }

    @Override
    public void onTableViewCellClick(UITableView.TableViewAdapter adapter, UITableViewCell.CellModel cellModel, int row) {
        super.onTableViewCellClick(adapter, cellModel, row);

        if (cellModel instanceof LocationCellModel) {
            LBService.shareInstance().asyncLocation(new BDLocationListener() {
                @Override
                public void onReceiveLocation(BDLocation bdLocation) {
                    App.toast(Res.localized(R.string.geolocation_complete));
                    finish();
                }
            });
        }
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

        //结果在 onGetSuggestionResultListener 中
        mSuggestionSearch.requestSuggestion(option.keyword(string));
    }

    //开始搜索
    private void searchLocation(String string) {

    }

    //清除结果
    private void clearResults() {
        cancelSearch(true);

        //删除搜索结果
        showNoResults();
    }

    private void showNoResults() {
        //删除搜索结果
        if (tableViewAdapter().row(locationCellModel) < 0) {
            tableViewAdapter().beginUpdate();
            tableViewAdapter().removeAll();
            tableViewAdapter().appendCells(noResults());
            tableViewAdapter().endUpdate();
        }
    }

    List<UITableViewCell.CellModel> noResults;
    LocationCellModel locationCellModel;
    private List<UITableViewCell.CellModel> noResults() {
        if (noResults != null) {
            return noResults;
        }
        noResults = new ArrayList<>();

        noResults.add(new BlankCellModel());

        locationCellModel = new LocationCellModel();
        noResults.add(locationCellModel);

        return noResults;
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

            if (suggestionResult == null) {return;}

            List<SuggestionResult.SuggestionInfo> list = suggestionResult.getAllSuggestions();
            if (list == null || list.size() == 0) {return;}

            tableViewAdapter().beginUpdate();
            tableViewAdapter().removeAll();

            for (SuggestionResult.SuggestionInfo info : list) {
                TitleCellModel model = new TitleCellModel();
                model.title = info.key;
                model.data = info;
                tableViewAdapter().appendCell(model);
            }

            tableViewAdapter().endUpdate();
        }
    };

    /**
     * 点击搜索后出来结果
     */
    private OnGetPoiSearchResultListener onGetPoiSearchResultListener = new OnGetPoiSearchResultListener() {
        @Override
        public void onGetPoiResult(PoiResult result) {
            if (result == null
                    || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
                App.toast(Res.localized(R.string.no_search_result));
                showNoResults();
                return;
            }
            if (result.error == SearchResult.ERRORNO.NO_ERROR) {

                List< PoiInfo > list =  result.getAllPoi();

                if (list.size() > 0) {
                    tableViewAdapter().beginUpdate();
                    tableViewAdapter().removeAll();

                    for (PoiInfo poiInfo : list) {
                        TitleCellModel model = new TitleCellModel();
                        model.title = poiInfo.name;
                        model.data = poiInfo;
                        tableViewAdapter().appendCell(model);
                    }

                    tableViewAdapter().endUpdate();
                }


//                mBaiduMap.clear();
//                PoiOverlay overlay = new MyPoiOverlay(mBaiduMap);
//                mBaiduMap.setOnMarkerClickListener(overlay);
//                overlay.setData(result);
//                overlay.addToMap();
//                overlay.zoomToSpan();

                return;
            } else {
                App.toast(Res.localized(R.string.no_search_result));
                showNoResults();
            }
//            if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
//
//                // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
//                String strInfo = "在";
//                for (CityInfo cityInfo : result.getSuggestCityList()) {
//                    strInfo += cityInfo.city;
//                    strInfo += ",";
//                }
//                strInfo += "找到结果";
//                Toast.makeText(PoiSearchDemo.this, strInfo, Toast.LENGTH_LONG)
//                        .show();
//            }
        }

        @Override
        public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {

        }
    };
}
