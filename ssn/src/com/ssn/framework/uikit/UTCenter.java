package com.ssn.framework.uikit;

import android.app.Activity;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.ssn.framework.foundation.Density;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 * Created by lingminjun on 16/3/31.
 */
public final class UTCenter {
    private static final int MAX_SIZE = 50;//最大缓存数
    private static final float PERCENTAGE_RAT = 640.0f;//屏幕比例

//    private Object _cl;
    private UTCenter () {
        _stack = new EventInfo[MAX_SIZE];
//        _cacheStack = new LruCache<Long,EventInfo>(MAX_SIZE);

//        _cl = Proxy.newProxyInstance(UTCenter.class.getClassLoader(),
//                new Class[]{View.OnClickListener.class}, onClickHandler);
//        InvocationHandler
    }

    private static class Singleton {
        private static final UTCenter instance = new UTCenter();
    }

    /**
     * 页面uri依赖
     */
    public interface Page {
        public String ut_ID();
        public String ut_getPageURI();
        public String ut_getUID();
    }

    /**
     * 获取唯一实例
     * @return
     */
    public static UTCenter getInstance() {
        return Singleton.instance;
    }

    /**
     * 当事件产生时记录时间所依赖的数据，
     * 请在 public boolean dispatchTouchEvent(MotionEvent ev); 中调用此方法
     * @param ac
     * @param ev
     */
    public void pushEvent(Activity ac, MotionEvent ev) {

        //只需要记住点击事件
        if (ev.getAction() != MotionEvent.ACTION_DOWN) {
            return;
        }

        //事件坐标信息
        final float x = ev.getX();//点击屏幕横坐标位置
        final float y = ev.getY();//点击屏幕纵坐标位置
        final long downAt = ev.getDownTime();//点击屏幕时间点 （ms）

        //事件页面信息
        String uri = ac.getClass().getSimpleName();
        String uid = "-";//默认值替代
        if (ac instanceof Page) {
            String t_uri = ((Page) ac).ut_getPageURI();
            if (!TextUtils.isEmpty(t_uri)) {
                uri = t_uri;
            }

            //获取事件发生的用户信息
            String t_uid = ((Page) ac).ut_getUID();
            if (!TextUtils.isEmpty(t_uid)) {
                uid = t_uid;
            }
        }

        //将时间
        long code = ev.hashCode();

        EventInfo info = new EventInfo();
        info._id = ""+code;
        info._x = x;
        info._y = y;
        info._at = downAt;
        info._percentageX = x / Density.screenWidthpx() * PERCENTAGE_RAT;
        info._percentageY = y / Density.screenHeightpx() * PERCENTAGE_RAT;
        info._uri = uri;
        info._uid = uid;

        _stack[_idx%MAX_SIZE] = info;//替换栈顶元素
        _idx = ((_idx + 1)%MAX_SIZE);
//        _cacheStack.put(code,info);
        Log.e("UT","push "+info.toString());
    }

    public void onEvent(View sender, String actionName, String actionId) {
        if (sender == null) {return;}

        if (sender instanceof EditText) {return;}

        if (TextUtils.isEmpty(actionName)) {return;}

        pop(sender,actionName,actionId);
    }

//    InvocationHandler onClickHandler = new InvocationHandler() {
//        @Override
//        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//            Log.e("UTC","method:"+method);
//            return null;
//        }
//    };

    private static boolean isPointInView(View view, int rx, int ry, int diff) {
        int[] l = new int[2];
        view.getLocationOnScreen(l);
        int x = l[0] - diff;
        int y = l[1] - diff;
        int w = view.getWidth() + diff;
        int h = view.getHeight() + diff;
        if (rx < x || rx > x + w || ry < y || ry > y + h) {
            return false;
        }
        return true;
    }

    private void pop(View sender, String actionName, String actionId) {

        //取栈顶元素
        _idx = (_idx + MAX_SIZE - 1)%MAX_SIZE;
        EventInfo info = _stack[_idx%MAX_SIZE];//取出栈顶元素
        _stack[_idx%MAX_SIZE] = null;//

        if (info == null) {
            Log.e("UT","empty pop action:"+actionName);
            return;
        }

        if (!isPointInView(sender,(int)info._x,(int)info._y,2)) {
            Log.e("UT","misplace pop action:"+actionName);
            return;
        }

        String name = actionName;
        if (TextUtils.isEmpty(actionName) && sender != null) {
            if (sender instanceof Button) {
                name = ""+((Button) sender).getText();
            } else if (sender instanceof TextView) {
                name = ""+((TextView) sender).getText();
            }
        }

        info._name = name;
        info._biz = actionId;

        Log.e("UT", "pop " + info.toString());
    }

    private static class EventInfo {
        String _id;
        String _uri;
        String _uid;
        String _name;
        String _biz;
        long _at;
        float _x;
        float _y;
        float _percentageX;
        float _percentageY;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("id:" + _id);
            builder.append(" at:" + _at);
            builder.append(" in("+_x+","+_y+")");
            builder.append("=("+_percentageX+","+_percentageY+")");
            builder.append(" name:" + _name);
            builder.append(" biz:" + _biz);
            builder.append(" uid:" + _uid);
            builder.append(" ref:"+ _uri);
            return builder.toString();
        }
    }

    private EventInfo[] _stack;
    private int _idx = 0;
//    private LruCache<Long,EventInfo> _cacheStack;
}
