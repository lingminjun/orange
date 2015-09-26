package com.ssn.framework.uikit;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import com.ssn.framework.R;
import com.ssn.framework.uikit.inc.ViewController;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by lingminjun on 15/9/26.
 */
public class BaseNavActivity extends BaseActivity {

    /**
     * 加载的fragmentkey
     */
    public static final String ROOT_FRAGMENT_CLASS_KEY = "__root_fragment_class_key";

    private UINavigationBar _navigationBar;

    private Stack<UIViewController> _stack = new Stack<UIViewController>();

    @Override
    protected int getContentViewlayoutID() {
        return R.layout.base_nav_activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //导航
        _navigationBar = (UINavigationBar) findViewById(R.id.navigation_bar);

        //加载root fregment
        Intent intent = getIntent();
        if (intent != null) {
            Serializable fragmentClass = intent.getSerializableExtra(this.ROOT_FRAGMENT_CLASS_KEY);
            if (fragmentClass != null) {
                startFragment((Class)fragmentClass,intent.getExtras());
            }
        }
    }

    @Override
    protected void transitionToFragment(Fragment fragment, boolean removeOld) {
        if (!(fragment instanceof UIViewController)) {
            Log.e("NavActivity","不支持的Fragment被push到NavActivity中了");
            return;
        }

        if (!_stack.contains(fragment)) {//防止直接调用startFragment加入到栈中
            _stack.push((UIViewController)fragment);
        }

        super.transitionToFragment(fragment, removeOld);

        if (fragment instanceof UIViewController) {
            if (_navigationBar != null) {//暂时不做压站处理，以后再想想，先让其重置栈方式呈现即可
                _navigationBar.resetItemStack(((UIViewController) fragment).navigationItem());
            }
        }
    }

    /**
     * push一个view到activity中
     * @param vc
     */
    public final void pushViewController(UIViewController vc) {
        if (vc == null) {return;}
        if (_stack.contains(vc)) {
            Log.e("NavActivity","不能重复push一个VC到栈中");
            return;
        }

        _stack.push(vc);
        transitionToFragment(vc,false);
    }

    @Override
    public final ViewController topViewController() {
        if (_stack.size() <=  0) {return null;}
        return _stack.lastElement();
    }


    /**
     * 推出一个栈
     */
    public void popViewController() {
        if (_stack.size() <=  0) {return;}

        UIViewController old = (UIViewController)topViewController();
        if (old == null) {return;}
        _stack.pop();
        UIViewController next = (UIViewController)topViewController();
        if (next != null) {
            transitionToFragment(next, true);
        }
        else {
            removeFragment(old);
        }
    }


    /**
     * 推出到指定栈
     * @param vc
     */
    public void popToViewController(UIViewController vc) {
        if (vc == null) {return;}

        if (_stack.size() <=  0) {return;}

        if (!_stack.contains(vc)) {return;}

        UIViewController old = (UIViewController)topViewController();
        if (vc == old) {return;}

        UIViewController tem = _stack.lastElement();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        Log.e("test","transaction="+transaction.hashCode());
        while (tem != null && vc != tem) {
            _stack.pop();
            tem = _stack.lastElement();
            transaction.remove(tem);
        }

        transitionToFragment(vc, true);
    }

    @Override
    public final List<ViewController> childrenViewControllers() {
        return new ArrayList<ViewController>(_stack);
    }
}
