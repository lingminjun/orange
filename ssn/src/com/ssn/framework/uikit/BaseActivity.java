package com.ssn.framework.uikit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.FrameLayout;
import com.ssn.framework.R;
import com.ssn.framework.foundation.APPLog;
import com.ssn.framework.foundation.BroadcastCenter;
import com.ssn.framework.foundation.TR;
import com.ssn.framework.foundation.URLHelper;
import com.ssn.framework.uikit.inc.ViewController;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * 基本activity框架
 */
public class BaseActivity extends FragmentActivity implements ViewController.ContainerViewController {

    /**
     * 加载的fragmentkey
     */
    public static final String FRAGMENT_CLASS_KEY = "__fragment_class_key";

    private static final String VCS_TAGS_KEY = "_fragment_tags";

    private ArrayList<Integer> _vcs_tags = new ArrayList<Integer>();
    private List<Fragment> _vcs = new ArrayList<Fragment>();
    private Fragment _tvc;

    //activity状态
    private boolean _isPause;

    private String _navigator_uri;

    public String getURI() {
        return TR.string(_navigator_uri);
    }

    /**
     * 获取xml
     * @return
     */
    protected int getContentViewlayoutID() {
        return R.layout.ssn_base_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //记录active的弱引用，方便finish，分配流水id，若回复就复位
        Navigator.shareInstance().onActivityCreate(this,savedInstanceState);

        //清楚所有回复的fragment
        if (savedInstanceState != null) {
            ArrayList<Integer> tags = savedInstanceState.getIntegerArrayList(VCS_TAGS_KEY);
            if (tags.size() > 0) {
                FragmentManager fm = getSupportFragmentManager();
                FragmentTransaction transaction = fm.beginTransaction();
                for (Integer tag : tags) {
                    Fragment fg = fm.findFragmentByTag(tag.toString());
                    if (fg != null) {
                        transaction.remove(fg);
                    }
                }
                transaction.commit();
            }
        }

        super.onCreate(savedInstanceState);
        _isPause = false;

        Intent intent = getIntent();
        if (intent != null) {//重要参数先记录下来
            String url = intent.getStringExtra(Navigator.NAVIGATOR_URL_KEY);
            if (url != null) {
                _navigator_uri = URLHelper.getURLFinderPath(url);
            }
        }

        //设置view布局
        setContentView(getContentViewlayoutID());

        //加载第一个fragment
        if (intent != null) {
            Serializable fragmentClass = intent.getSerializableExtra(this.FRAGMENT_CLASS_KEY);
            if (fragmentClass != null) {
                startFragment((Class)fragmentClass,intent.getExtras());
            }
        }
    }

    @Override
    public final void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }

    @Override @Deprecated
    public final void setContentView(View view) {
        APPLog.error("废弃");
        super.setContentView(view);
    }

    @Override @Deprecated
    public final void setContentView(View view, ViewGroup.LayoutParams params) {
        APPLog.error("废弃");
        super.setContentView(view, params);
    }

    @Override @Deprecated
    public void addContentView(View view, ViewGroup.LayoutParams params) {
        APPLog.error("废弃");
        super.addContentView(view, params);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        _isPause = false;
        super.onResume();
        Fragment fragment = topFragment();
        if (fragment != null && fragment instanceof UIViewController) {
            ((UIViewController) fragment).onViewDidAppear();
        }
        Navigator.shareInstance().onActivityResume(this);
    }

    @Override
    protected void onPause() {
        _isPause = true;
        Navigator.shareInstance().onActivityPause(this);
        super.onPause();
        Fragment fragment = topFragment();
        if (fragment != null && fragment instanceof UIViewController) {
            ((UIViewController) fragment).onViewDidDisappear();
        }
    }

    @Override
    protected void onDestroy() {
        _isPause = true;
        Navigator.shareInstance().onActivityDestroy(this);
        BroadcastCenter.shareInstance().removeObserver(this);
        UILoading.dismiss(this,false);//防止泄露
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        _isPause = true;
        super.onStop();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
//        if (fragmentStack != null && fragmentStack.size() > 0) {
//            BaseFragment fragment = fragmentStack.lastElement();
//            fragment.onWindowFocusChanged(hasFocus);
//        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        Navigator.shareInstance().onActivitySaveInstanceState(this,outState);

        //将fragment tag保存下来
        outState.putIntegerArrayList(VCS_TAGS_KEY,_vcs_tags);

        super.onSaveInstanceState(outState);
    }

    protected final boolean isActive() {
        return !_isPause;
    }

    /**
     * 切换到对应的fragment
     * @param fragment
     */
    protected void transitionToFragment(Fragment fragment,boolean removeOld) {
        if (fragment == null) {return;}

        //当前已经是现实的fragment
        if (_tvc == fragment) {
            return;
        }

        //先将fragment 加入到数组中，并且调整位置
        boolean exist = _vcs.contains(fragment);

        //调整顺序
        _vcs.remove(fragment);
        _vcs.add(fragment);

        if (!exist) {//将tag记录下来
            _vcs_tags.add(fragment.hashCode());
        }

        Fragment old = _tvc;
        _tvc = fragment;

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
//        Log.e("test", "transaction=" + transaction.hashCode());
        if (!exist) {//理论上FragmentManager应该不包含其实例
            transaction.add(R.id.ssn_view_container, fragment, Integer.toString(fragment.hashCode()));
        }

        if (removeOld) {
            transaction.remove(old);
        }
        else if (old != null) {
            transaction.hide(old);
        }

        transaction.show(fragment);
        transaction.commit();

        //消失时机通知
        if (old instanceof UIViewController) {
            try {((UIViewController) old).onViewDidDisappear();} catch (Throwable e) {APPLog.error(e);}
        }

        //出现时机通知，此方法多次调用，没有什么坏处
        if (/*exist && */fragment instanceof UIViewController) {
            try {((UIViewController) fragment).onViewDidAppear();} catch (Throwable e) {APPLog.error(e);}
        }
    }

    protected final Fragment topFragment() {
        return _tvc;
    }

    /**
     * 显示fragment，默认实现是不断加入到队列
     * @param fragment
     */
    protected final void displayFragment(Fragment fragment) {
        transitionToFragment(fragment,false);
    }


    /**
     * 结束页面
     * @param vc
     */
    @Override
    public void dismissViewController(ViewController vc) {
        if (vc == null) {finish();return;}

        if (!_vcs.contains(vc)) {
            return;
        }

        if (_vcs.size() <= 1) {finish();return;}

        removeFragment((Fragment)vc);
    }

    /**
     * 删除fragment，移除现实
     * @param fragment
     */
    protected final void removeFragment(Fragment fragment) {
        if (fragment != null) {return;}

        if (!_vcs.contains(fragment)) {
            return;
        }

        _vcs.remove(fragment);

        Fragment sec = null;
        int size = _vcs.size();
        if (size > 0) {
            sec = _vcs.get(size - 1);
        }

        //切换展示
        transitionToFragment(sec,true);
    }

    protected final void dismissFragment(Fragment fragment) {
        if (fragment != null) {return;}

        if (_tvc != fragment) {
            return;
        }

        Fragment sec = null;
        int size = _vcs.size();
        if (size > 1) {//
            sec = _vcs.get(size - 2);
        }

        transitionToFragment(sec,false);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN && _vcs.size() > 0) {
            Fragment fragment = _tvc;
            if (fragment instanceof UIViewController) {
                boolean result = false;
                try {result = ((UIViewController) fragment).onBackEvent();} catch (Throwable e) {APPLog.error(e);}
                return result;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean result = false;
        if (_tvc != null && _tvc instanceof UIViewController) {
            try {
                result = ((UIViewController) _tvc).dispatchTouchEvent(ev);
            }catch (Throwable e){APPLog.error(e);}
        }
        return result ? true : super.dispatchTouchEvent(ev);
    }

    @Override
    public ViewController topViewController() {
        if (_tvc != null && _tvc instanceof UIViewController) {
            return (UIViewController)_tvc;
        }
        return null;
    }

    @Override
    public List<ViewController> childrenViewControllers() {
        ArrayList<ViewController> list = new ArrayList<ViewController>();
        for (Fragment fragment : _vcs) {
            if (fragment instanceof UIViewController) {
                list.add((UIViewController)fragment);
            }
        }
        return list;
    }

    /**
     * 创建一个fragment
     * @param fragmentClass
     * @param args
     * @return
     */
    public static final Fragment createFragment(Class fragmentClass,Bundle args) {
        if (fragmentClass == null) {
            return null;
        }

        Class fragmentCl = fragmentClass;
        Constructor c1;
        Fragment fragment = null;
        try {
//            fragment = (Fragment)fragmentCl.newInstance();
            c1 = fragmentCl.getDeclaredConstructor();//私有构造方法
            if (c1 == null) {
                c1 = fragmentCl.getConstructor();//public构造
            }
            c1.setAccessible(true);
            fragment = (Fragment) c1.newInstance();
        } catch (NoSuchMethodException e) {
            APPLog.error(e);
        } catch (InvocationTargetException e) {
            APPLog.error(e);
        } catch (InstantiationException e) {
            APPLog.error(e);
        } catch (IllegalAccessException e) {
            APPLog.error(e);
        }

        if (fragment != null) {
            if (args != null) {
                fragment.setArguments(args);
            }
            else {
                fragment.setArguments(new Bundle());
            }
        }
        return fragment;
    }

    /**
     * 打开一个新的fragment
     * @param fragmentClass
     * @param args
     */
    public final void startFragment(Class fragmentClass,Bundle args) {
        Fragment fragment = createFragment(fragmentClass,args);

        if (fragment != null) {
            displayFragment(fragment);
        }
    }

    public void setBackgroundDrawable(int sourceID) {
        View root = findViewById(R.id.ssn_root_wrap_view);
        if (root != null) {
            root.setBackgroundResource(sourceID);
        }
    }

    protected FrameLayout getRootWrapView() {
        return (FrameLayout)findViewById(R.id.ssn_root_wrap_view);
    }
}
