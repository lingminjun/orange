package com.ssn.framework.uikit;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.util.Log;
import com.ssn.framework.R;
import com.ssn.framework.uikit.inc.UIWrapperView;
import com.ssn.framework.uikit.inc.ViewController;

import java.util.ArrayList;
import java.util.List;

/**
 * Tab activity 基类
 */
public class BaseTabActivity extends BaseActivity {

    public static final String TAB_FRAGMENT_CLASS_LIST_KEY = "__tab_fragment_class_list_key";

    /**
     * 用于生产 tab vc 的intent
     */
    public static class IntentTabItem implements Parcelable {
        public Class viewControllerClass;
        public String url;
        public Bundle args;

        public IntentTabItem() {
        }

        public IntentTabItem(Parcel parcel) {
            // 反序列化 顺序要与序列化时相同
            viewControllerClass = (Class)parcel.readSerializable();
            url = parcel.readString();
            args = parcel.readBundle();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            // 序列化
            dest.writeSerializable(viewControllerClass);
            dest.writeString(url);
            dest.writeBundle(args);
        }

        public static final Creator<IntentTabItem> CREATOR = new Creator<IntentTabItem>() {

            @Override
            public IntentTabItem createFromParcel(Parcel source) {
                // 反序列化 顺序要与序列化时相同
                return new IntentTabItem(source);
            }

            @Override
            public IntentTabItem[] newArray(int i) {
                return new IntentTabItem[i];
            }
        };
    }


    private List<UIViewController> _vcs = new ArrayList<UIViewController>();
    private UITabBar _tabbar;
    private UIViewController _selectedVC;
    private int _selectedIndex;

    private UITabBar.OnTabSelectedListener changeListener = new UITabBar.OnTabSelectedListener() {
        @Override
        public void onSelected(UITabBar tabbar, int index) {
            selectViewControllerAtIndex(index);
        }
    };

    private UITabBar getTabbar() {
        if (_tabbar != null) {return _tabbar;}
        _tabbar = (UITabBar) findViewById(R.id.ssn_tab_bar);
        return _tabbar;
    }

    protected UITabBar tabBar() {
        return getTabbar();
    }

    protected final int getContentViewlayoutID() {
        return R.layout.ssn_base_tab_activity;
    }

//    private int __pid;//记录进程id

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();

//        __pid = android.os.Process.myPid();//获取当前进程
//        if (savedInstanceState != null) {
//            int _pid = savedInstanceState.getInt("__pid", 0);
//            if (_pid != 0 && _pid != __pid) {
//                APPLog.error("上一次应用异常退出！");
//                System.exit(-1);
//            }
//        }

        UITabBar tabBar = getTabbar();
        tabBar.setTabSelectedListener(changeListener);
        UIWrapperView wrapperView = (UIWrapperView)findViewById(R.id.ssn_wrap_view);
        if (wrapperView != null) {
            wrapperView.setBottomDockView(tabBar);
        }

        ArrayList<IntentTabItem> list = intent.getParcelableArrayListExtra(this.TAB_FRAGMENT_CLASS_LIST_KEY);
        if (list != null) {
            List<UITabBar.TabItem> tabs = new ArrayList<UITabBar.TabItem>();
            for (IntentTabItem item : list) {

                //将url添加到参数中
                if (item.args == null) {
                    item.args = new Bundle();
                }
                if (item.url != null) {
                    item.args.putString(Navigator.NAVIGATOR_URL_KEY,item.url);
                }

                Fragment frag = createFragment(item.viewControllerClass,item.args);
                if (frag == null) {
                    continue;
                }

                if (!(frag instanceof UIViewController)) {
                    Log.e("TabActivity","不支持普通的fragment");
                    continue;
                }

                UIViewController vc = (UIViewController)frag;

                _vcs.add(vc);
                tabs.add(vc.tabItem());//取vc的tab
            }

            tabBar.setTabItems(tabs);

            //默认选第一个
            if (tabs.size() > 0) {
                selectViewControllerAtIndex(0);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (_selectedVC != null) {
            int idx = _vcs.indexOf(_selectedVC);
            if (idx != _selectedIndex) {//说明外部选择了其他index
                selectViewControllerAtIndex(_selectedIndex);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
//        outState.putInt("__pid",__pid);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void dismissViewController(ViewController vc) {
        finish();
    }

    protected final int viewControllerCount() {
        return _vcs.size();
    }

    /**
     * 对外提供切换tab方法
     * @param index
     */
    public final void selectViewControllerAtIndex(int index) {
        if (index < 0 || index >= _vcs.size()) {
            return;
        }

        _selectedIndex = index;
        if (isActive()) {
            _selectedVC = _vcs.get(index);
            displayFragment(_selectedVC);
            tabBar().checkIndex(_selectedIndex);
        }
    }

    @Override
    public UIViewController topViewController() {
        return (UIViewController) _vcs.get(_selectedIndex);
    }

    @Override
    public List<ViewController> childrenViewControllers() {
        return new ArrayList<ViewController>(_vcs);
    }
}
