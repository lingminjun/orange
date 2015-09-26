package com.ssn.framework.uikit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import com.ssn.framework.foundation.APPLog;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.foundation.URLHelper;
import com.ssn.framework.uikit.inc.ActivityTracking;

import java.util.*;

/**
 * Created by lingminjun on 15/9/13.
 */
public class Navigator implements ActivityTracking {

    public static final String NAVIGATOR_URL_KEY = "navigator_router_url_key";
    public static final String NAVIGATOR_FRAGMENT_CLASS_KEY = "navigator_router_fragment_class_key";
    public static final String NAVIGATOR_FRAGMENT_CLASS_LIST_KEY = "navigator_router_fragment_class_list_key";

    /**
     * 页面委托协议，暂时还没用到
     */
    public static interface Page {
        /**
         * 页面被打开回调，仅仅第一次打开时将收到回调
         * @param url
         * @param intent
         */
        public void onOpenPage(String url,Intent intent);

        /**
         * 询问是否需要继续打开，若继续打开，将会再次调用onOpenPage
         * @param url
         * @param intent
         * @return
         */
        public boolean canOpenPage(String url,Intent intent);

        /**
         * 页面收到通知回调
         * @param url
         * @param intent
         */
        public void onNoticePage(String url,Intent intent);
    }




    ////////////////////////////////////////////////////////////
    private static Navigator _instance = null;
    /**
     * 用户中心
     * @return 唯一实例
     */
    static public Navigator shareInstance() {
        if (_instance != null) return _instance;
        synchronized(Navigator.class){
            if (_instance != null) return _instance;
            _instance = newInstance();
            return _instance;
        }
    }

    private static Navigator newInstance() {
        return new Navigator();
    }

    /**
     * 防止构造实例
     */
    private Navigator() {
        super();
    }




    ////////////////////////////////////////////////////////////
    private Set<String> _schemes = new HashSet<String>();;//支持的schame跳转，不设置将默认

    /**
     * 支持的scheme跳转
     * @param scheme
     */
    public void addScheme(String scheme) {
        if (TextUtils.isEmpty(scheme)) {return;}

        String tem_scheme = scheme.toLowerCase();
        synchronized (_schemes) {
            this._schemes.add(tem_scheme);
        }
    }

    public void addSchemes(Set<String> schemes) {
        if (schemes == null || schemes.size() == 0) {return;}

        Set<String> tem_schemes = new HashSet<String>();
        for (String str : schemes) {
            tem_schemes.add(str.toLowerCase());
        }

        synchronized (_schemes) {
            this._schemes.addAll(tem_schemes);
        }
    }

    /**
     * 满足要求的url，首先是查看url是否正确，再是scheme是否被包含
     * @param url
     * @return
     */
    public boolean isValidURL(String url) {
        if (TextUtils.isEmpty(url)) {return false;}

        Uri uri = Uri.parse(url);
        if (uri == null) {return false;}

        String host = uri.getHost();
        //非法url
        if (TextUtils.isEmpty(host)) {return false;}

        String scheme = uri.getScheme();
        if (TextUtils.isEmpty(scheme)) {return false;}

        scheme = scheme.toLowerCase();

        synchronized (_schemes) {
            return _schemes.contains(scheme);
        }
    }

    //////////////////////////////////////////////////
    private Stack<Activity> _stack = new Stack<Activity>();

    public void onActivityCreate(Activity activity,Bundle savedInstanceState) {
        _stack.add(activity);
        APPLog.info("Activity:" + activity.hashCode() + "被创建");
    }
    public void onActivityResume(Activity activity) {APPLog.info("Activity:" + activity.hashCode() + " Resume");}
    public void onActivityPause(Activity activity) {APPLog.info("Activity:" + activity.hashCode() + " Pause");}
    public void onActivityDestroy(Activity activity) {
        _stack.remove(activity);
        APPLog.info("Activity:"+activity.hashCode()+"被销毁");
    }
    public void onActivitySaveInstanceState(Activity activity,Bundle outState) {}

    public void finishTopActivity() {
        Activity last = _stack.lastElement();
        if (last != null) {
            last.finish();
        }
    }

    public void finishToActivity(Activity toActivity) {
        if (toActivity == null) {
            return;
        }
        if (!_stack.contains(toActivity)) {
            return;
        }
        do {
            Activity last = _stack.lastElement();
            if (last == toActivity) {
                return;
            }

            last.finish();
        } while (true);
    }

    public void finishToRoot() {
        while (_stack.size() <= 1) {
            Activity last = _stack.lastElement();
            last.finish();
        }
    }

    public Activity topActivity() {
        return _stack.lastElement();
    }

    ///////////////////////////////////////////////////////
    /*open url 模块*/
    public static class VCNode {
        public Class fragmentClass;
        public Class activityClass;//限定特定的activity打开

        public String url;//对应的url

        private String uri;//统一资源标识符，主要取url中path

    }
    private Map<String,VCNode> _map = new HashMap<String, VCNode>();

    /**
     * 添加view map
     * @param url
     * @param fragmentClass
     * @param activityClass
     */
    public void addPageRouter(String url, Class fragmentClass, Class activityClass) {
        if (TextUtils.isEmpty(url) || (fragmentClass == null && activityClass == null)) {
            return;
        }

        if (!isValidURL(url)) {
            return;
        }

        String uri = URLHelper.getURLFinderPath(url);
        if (TextUtils.isEmpty(uri)) {
            return;
        }

        VCNode node = new VCNode();
        node.fragmentClass = fragmentClass;
        node.activityClass = activityClass;
        node.uri = uri;
        node.url = url;


        synchronized (_map) {
            _map.put(uri,node);
        }
    }

    public void addPageRouter(VCNode node) {
        addPageRouter(node.url, node.fragmentClass, node.activityClass);
    }

    public void addPageRouters(List<VCNode> nodes) {
        if (nodes == null || nodes.size() == 0) {return;}
        for (VCNode node : nodes) {
            addPageRouter(node);
        }
    }

    /**
     * 返回其router配置
     * @param url
     * @return
     */
    public VCNode pageRouter(String url) {
        String uri = URLHelper.getURLFinderPath(url);
        if (TextUtils.isEmpty(uri)) {
            return null;
        }

        synchronized (_map) {
            return _map.get(uri);
        }
    }

    /**
     * 是否配置其
     * @param url
     * @return
     */
    public boolean existPageRouter(String url) {
        return pageRouter(url) != null;
    }



    public boolean openURL(String url) {
        return openURL(url,null);
    }

    /**
     * 打开某个界面
     * @param url url所对应的界面（包含参数）
     * @param args 参数
     * @return
     */
    public boolean openURL(String url, Bundle args) {
        return openURL(url,args,false);
    }

    /**
     * 打开某个界面
     * @param url url所对应的界面（包含参数）
     * @param args 参数
     * @return
     */
    public boolean openURL(String url, Bundle args, boolean isModal) {

        //遍历所有activity，是否可以打开此界面


        Context context = getReasonableContext();

        Intent intent = getOpenURLIntent(context,url,args);
        if (intent == null) {
            return false;
        }

        //打开页面，动画是否需要控制
        context.startActivity(intent);

        //动画设置
        if (context instanceof Activity) {
            if (isModal) {
                ((Activity)context).overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            } else {
                ((Activity)context).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        }

        return true;
    }

    public Intent getOpenURLIntent(Context context,String url, Bundle args) {
        VCNode node = pageRouter(url);
        //无法打开
        if (node == null) {
            return null;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setClass(context,node.activityClass != null ? node.activityClass : BaseActivity.class);

        intent.putExtra(NAVIGATOR_URL_KEY,url);
        if (node.fragmentClass != null) {
            intent.putExtra(NAVIGATOR_FRAGMENT_CLASS_KEY, node.fragmentClass);
        }

        //防止 service 直接 open url，其他Activity都不需要重新开启一个栈
        if (context != null && !(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        //参数带上
        if (args.size() > 0) {
            intent.putExtras(args);
        }

        //url中的参数
        HashMap<String,Object> query = URLHelper.URLQuery(url);
        Set<String> keys = query.keySet();
        for (String key : keys) {
            Object value = query.get(key);
            if (value instanceof String) {
                intent.putExtra(key, (String)value);
            }
            else if (value instanceof ArrayList) {
                intent.putStringArrayListExtra(key, (ArrayList<String>) value);
            }
            else {
                APPLog.error("不支持的类型"+value);
            }
        }

        return intent;
    }

    private Context getReasonableContext() {
        Activity activity = topActivity();
        if (activity == null) {
            return Res.context();
        }
        return activity;
    }


//    public void startActivity()
}
