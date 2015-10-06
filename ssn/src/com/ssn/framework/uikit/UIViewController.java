package com.ssn.framework.uikit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.*;
import android.widget.LinearLayout;
import com.ssn.framework.R;
import com.ssn.framework.foundation.APPLog;
import com.ssn.framework.foundation.BroadcastCenter;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.inc.ViewController;

import java.io.Serializable;

/**
 * Created by lingminjun on 15/7/9.
 */
public class UIViewController extends Fragment implements ViewController {

    /**
     * 导航 bar 支持
     */
    private View.OnClickListener _backListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            onBackEvent();
        }
    };
    private UINavigationBar.NavigationItem _navigationItem;
    public final UINavigationBar.NavigationItem navigationItem() {
        if (_navigationItem == null) {
            _navigationItem = new UINavigationBar.NavigationItem();
        }
        return _navigationItem;
    }

    /**
     * tab bar支持
     */
    private UITabBar.TabItem _tabItem;
    public final UITabBar.TabItem tabItem() {
        if (_tabItem == null) {
            _tabItem = new UITabBar.TabItem();
        }
        return _tabItem;
    }

//    protected Bundle _attributes;
//
//    private void onInitAttributes(Bundle args) {
//        //首先初始化_package
//    }
    private void onCheckInit(Bundle args) {
        if (!_isInit) {
            _isInit = true;
            try {
                onInit(args);
            }catch (Throwable e) {APPLog.error(e);}
        }
    }
//
    @Override
    public final void setArguments(Bundle args) {
        super.setArguments(args);
        onCheckInit(args);
    }

    /**
     * 返回事件
     * return 若处理返回事件则返回yes
     */
    protected boolean onBackEvent() {
        finish();
        return true;
    }

    public final void finish() {
        Activity activity = getActivity();
        if (activity == null) {return;}
        if (activity instanceof ContainerViewController) {
            ((ContainerViewController) activity).dismissViewController(this);
        }
        else {
            activity.finish();
        }
    }

    /**
     * 键盘触发事件
     * @param ev
     * @return
     */
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return false;
    }

    //////////////////////////ViewController实现/////////////////////////////////////
    /**
     * 初始化方法
     */
    public void onInit(Bundle args) {}

    /**
     * 主view，可能返回未空
     * @return
     */
    protected View containerView() {
        if (_containerView == null) {
            try {
                LayoutInflater inflater = LayoutInflater.from(Res.context());
                _containerView = loadView(inflater);
            }
            catch (Throwable e) {}
        }
        return _containerView;
    }



    /**
     * 加载当前页面，此界面的主view
     * @param inflater
     */
    public View loadView(LayoutInflater inflater) {
        return _loadView(inflater);
    }

    private View _loadView(LayoutInflater inflater) {
        if (_loadViewStackFlag) {
            APPLog.error("务必在loadview中返回当前页面的view实例，不要循环调用loadview方法");
            System.exit(-1);
        }
        _loadViewStackFlag = true;

        Context acontext = inflater.getContext();

        if (acontext == null) {acontext = getActivity();}
        if (acontext == null) {acontext = Res.context();}

        _containerView = new View(acontext);
        return _containerView;
    }

    /**
     * 当此页面的containerView被成功加载后回调，方法只会调用一次
     * @return 返回当前此视图主要的view
     */
    public void onViewDidLoad() {

        //返回按钮设置
        String back = getArguments().getString(Navigator.NAVIGATOR_VC_BACK_KEY);
        if (!TextUtils.isEmpty(back)) {
            navigationItem().backItem().setOnClick(ViewEvent.click(_backListener));
            navigationItem().backItem().setTitle("    ");
            navigationItem().backItem().setHidden(false);
            navigationItem().backItem().setView(UINavigationBar.NavigationItem.ButtonItem.createButtonItemView(Res.context()));
        }
        else {
            String close = getArguments().getString(Navigator.NAVIGATOR_VC_CLOSE_KEY);
            if (!TextUtils.isEmpty(close)) {
                navigationItem().backItem().setOnClick(ViewEvent.click(_backListener));
                navigationItem().backItem().setTitle("取消");
                navigationItem().backItem().setImage(0);
                navigationItem().backItem().setHidden(false);
                navigationItem().backItem().setView(UINavigationBar.NavigationItem.ButtonItem.createButtonItemView(Res.context()));
            }
        }
    }

    /**
     * 页面将要被展示
     * //@param animated 展示是否启动动画
     */
    @Deprecated
    public void onViewWillAppear(/*boolean animated*/) {}

    /**
     * 页面已经被展示，展示subView，可以得到所有subView尺寸与位置
     * //@param animated 展示是否启动动画
     */
    public void onViewDidAppear(/*boolean animated*/) {}

    /**
     * Activit已经被消失
     * //@param animated 展示是否启动动画
     */
    public void onViewDidDisappear(/*boolean animated*/) {}

    /**
     * Activit进入后台
     */
    public void onDidEnterBackgroud() {}

    /**
     * 与onViewDidLoad对应，此时可以释放view以及subView和其他资源
     */
    @Deprecated
    public void onViewDidUnload() {}

    /**
     * 页面退出
     */
    public void onDestroyController() {}

    /**
     * 系统内存资源不足是调用，释放掉不用的资源，4.0以前版本level == TRIM_MEMORY_UI_HIDDEN
     */
    public void onReceiveMemoryWarning(int level) {}

    /**
     * view是否显示
     * @return
     */
    public boolean isViewVisible() {return _isViewVisible;}

    /**
     * 获取父容器，如果没有返回null
     * @return
     */
    public ViewController.ContainerViewController containerController() {
        Activity activity = getActivity();
        if (activity != null && activity instanceof ViewController.ContainerViewController) {
            return (ViewController.ContainerViewController)activity;
        }
        return null;
    }

    @Override
    public void onControllerActivityResult(int requestCode, int resultCode, Intent data) {

    }

    //////////////////////////系统生命周期函数实现/////////////////////////////////////
    @Override
    public final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        onCheckInit(savedInstanceState);
    }

    @Override
    public final View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

//        if (_package != null && savedInstanceState != null) {
//            _package.initialize(savedInstanceState);//当界面回复时数据初始化
//        }

        if (_containerView == null) {
            try {
                _containerView = loadView(inflater);
            }catch (Throwable e) {
                APPLog.error(e);
            }

            //不能block此页面，否则无法防止系统crash
            if (_containerView == null) {
                _containerView = _loadView(inflater);
            }
        }

        //防止重复添加view
        if (_containerView != null) {
            try {
                ViewParent parent = _containerView.getParent();
                if (parent != null) {
                    ((ViewGroup) parent).removeView(_containerView);
                }
            }catch (Throwable e) {APPLog.error(e);}
        }

        //调用viewDidLoad方法
        if (!_isViewDidLoad) {
            _isViewDidLoad = true;
            try {
                //寻找导航栏
                UINavigationBar navigationBar = (UINavigationBar) _containerView.findViewById(R.id.ssn_navigation_bar);
                if (navigationBar != null) {
                    navigationBar.pushItem(navigationItem());
                }

                onViewDidLoad();//此方法只被调用一次，后面不再触发
            }catch (Throwable e) {APPLog.error(e);}
        }

        return _containerView;
    }

    @Override
    public final void onStart() {
        super.onStart();
        try {
            onViewWillAppear();
        }
        catch (Throwable e) {
            APPLog.error(e);
        }
    }

    @Override
    public final void onResume() {
        super.onResume();
        _isViewVisible = true;
        try {
            onViewDidAppear();
        }
        catch (Throwable e) {
            APPLog.error(e);
        }
    }

    @Override
    public final void onPause() {
        try {
            onViewDidDisappear();
        }
        catch (Throwable e) {
            APPLog.error(e);
        }
        _isViewVisible = false;
        super.onPause();
    }

    @Override
    public final void onStop() {
        try {
            onDidEnterBackgroud();
        }
        catch (Throwable e) {
            APPLog.error(e);
        }
        super.onStop();
    }

    @Override
    public final void onLowMemory() {
        super.onLowMemory();
        try {
            onReceiveMemoryWarning(20);
        }
        catch (Throwable e) {
            APPLog.error(e);
        }
    }

    /*
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        try {
            onReceiveMemoryWarning(level);
        }
        catch (Throwable e) {
            APPLog.error(e);
        }
    }
    */

    @Override
    public final void onDestroyView() {
        try {
            onViewDidUnload();
        }
        catch (Throwable e) {
            APPLog.error(e);
        }
        super.onDestroyView();
    }


    @Override
    public final void onDestroy() {
        try {
            onDestroyController();
        }
        catch (Throwable e) {
            APPLog.error(e);
        }
        BroadcastCenter.shareInstance().removeObserver(this);
        super.onDestroy();
    }

    @Override
    public final void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            super.onActivityResult(requestCode, resultCode, data);
            onControllerActivityResult(requestCode, resultCode, data);
        }
        catch (Throwable e) {
            APPLog.error(e);
        }
    }

    @Override
    public final void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    //
//    @Override
//    public void onDetach() {
//        throw new RuntimeException("Stub!");
//    }

    private boolean _isInit;

    private View _containerView;
    private boolean _loadViewStackFlag;

    private boolean _isViewDidLoad;
//    private View _layoutView;
    private boolean _isViewVisible;
}
