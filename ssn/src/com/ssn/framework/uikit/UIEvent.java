package com.ssn.framework.uikit;

import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import com.ssn.framework.foundation.APPLog;

/**
 * Created by lingminjun on 15/7/9.
 */
public class UIEvent {

    /**
     * 点击事件工厂方法
     */
    public static Click click(View.OnClickListener listener) {
        return new UIEvent.Click(listener);
    }
    public static Click click(View.OnClickListener listener, @Nullable UTEvent ut, @Nullable String utid) {
        return new UIEvent.Click(listener,utid,ut);
    }


    /**
     * 焦点事件工厂方法
     */
    public static FocusChange focus(View.OnFocusChangeListener listener) {
        return new UIEvent.FocusChange(listener);
    }
    public static FocusChange focus(View.OnFocusChangeListener listener, @Nullable UTEvent ut, @Nullable String utid) {
        return new UIEvent.FocusChange(listener,utid,ut);
    }

    /**
     * 长按事件工厂方法
     */
    public static LongClick longClick(View.OnLongClickListener listener) {
        return new UIEvent.LongClick(listener);
    }
    public static LongClick longClick(View.OnLongClickListener listener, @Nullable UTEvent ut, @Nullable String utid) {
        return new UIEvent.LongClick(listener,utid,ut);
    }

    /**
     * touch事件工厂方法
     */
    public static Touch touch(View.OnTouchListener listener) {
        return new UIEvent.Touch(listener);
    }
    public static Touch touch(View.OnTouchListener listener, @Nullable UTEvent ut, @Nullable String utid) {
        return new UIEvent.Touch(listener,utid,ut);
    }

    /**
     * Watcher工厂方法
     */
    public static Watcher watcher(TextWatcher watcher) {
        return new UIEvent.Watcher(watcher);
    }
    public static Watcher watcher(TextWatcher watcher, @Nullable UTEvent ut, @Nullable String utid) {
        return new UIEvent.Watcher(watcher,utid,ut);
    }

    /**
     * Selected工厂方法
     */
    public static Selected select(AdapterView.OnItemSelectedListener select) {
        return new UIEvent.Selected(select);
    }
    public static Selected select(AdapterView.OnItemSelectedListener select, @Nullable UTEvent ut, @Nullable String utid) {
        return new UIEvent.Selected(select,utid,ut);
    }

    /**
     * Item Long Click工厂方法
     */
    public static ItemLongClick itemLongClick(AdapterView.OnItemLongClickListener click) {
        return new UIEvent.ItemLongClick(click);
    }
    public static ItemLongClick itemLongClick(AdapterView.OnItemLongClickListener click, @Nullable UTEvent ut, @Nullable String utid) {
        return new UIEvent.ItemLongClick(click,utid,ut);
    }

    /**
     * Item Click工厂方法
     */
    public static ItemClick itemClick(AdapterView.OnItemClickListener click) {
        return new UIEvent.ItemClick(click);
    }
    public static ItemClick itemClick(AdapterView.OnItemClickListener click, @Nullable UTEvent ut, @Nullable String utid) {
        return new UIEvent.ItemClick(click,utid,ut);
    }

    /**
     * 点击事件默认实现
     */
    public final static class Click extends UT implements View.OnClickListener {
        private View.OnClickListener _click;

        private void onInnerClick(View var1) {
            try {
                if (_click != null) _click.onClick(var1);
                track(var1);
            }
            catch (Throwable e) {
                APPLog.error(e);
            }
        }

        public void onClick(View var1) {
            if (_click == null) {return;}

            if (tryLock()) {
                lock();
                onInnerClick(var1);
                unlock();
            }
        }

        public Click(View.OnClickListener click) {
            super();
            _click = click;
        }

        public Click(View.OnClickListener click, @Nullable UIEvent.UTEvent ut) {
            super(ut);
            _click = click;
        }

        public Click(View.OnClickListener click, @Nullable String utid, @Nullable UIEvent.UTEvent ut) {
            super(ut,utid);
            _click = click;
        }
    }

    /**
     * 获取焦点事件默认实现
     */
    public final static class FocusChange extends UT implements View.OnFocusChangeListener {
        private View.OnFocusChangeListener _listener;
        public void onFocusChange(View var1, boolean var2) {
            try {
                if (_listener != null) _listener.onFocusChange(var1, var2);
                track(var1);
            }
            catch (Throwable e) {
                APPLog.error(e);
            }
        }
        public FocusChange(View.OnFocusChangeListener click) {
            super();
            _listener = click;
        }

        public FocusChange(View.OnFocusChangeListener click, @Nullable UIEvent.UTEvent ut) {
            super(ut);
            _listener = click;
        }

        public FocusChange(View.OnFocusChangeListener click, @Nullable String utid, @Nullable UIEvent.UTEvent ut) {
            super(ut,utid);
            _listener = click;
        }
    }

    /*
    public interface OnDragListener {
        boolean onDrag(View var1, DragEvent var2);
    }
    */

    /**
     * 长按事件默认实现
     */
    public final static class LongClick extends UT implements View.OnLongClickListener {
        private View.OnLongClickListener _click;
        public boolean onLongClick(View var1) {
            boolean result = false;
            try {
                if (_click != null) result = _click.onLongClick(var1);
                track(var1);//打点
            }
            catch (Throwable e) {
                APPLog.error(e);
            }
            return result;
        }

        public LongClick(View.OnLongClickListener click) {
            super();
            _click = click;
        }

        public LongClick(View.OnLongClickListener click, @Nullable UIEvent.UTEvent ut) {
            super(ut);
            _click = click;
        }

        public LongClick(View.OnLongClickListener click, @Nullable String utid, @Nullable UIEvent.UTEvent ut) {
            super(ut,utid);
            _click = click;
        }
    }

    /*
    public interface OnGenericMotionListener {
        boolean onGenericMotion(View var1, MotionEvent var2);
    }

    public interface OnHoverListener {
        boolean onHover(View var1, MotionEvent var2);
    }
    */

    /**
     * touch事件默认实现
     */
    public final static class Touch extends UT implements View.OnTouchListener {
        private View.OnTouchListener _touch;
        public boolean onTouch(View var1, MotionEvent var2) {
            boolean result = false;
            try {
                if (_touch != null) result = _touch.onTouch(var1,var2);
                track(var1);//打点
            }
            catch (Throwable e) {
                APPLog.error(e);
            }
            return result;
        }

        public Touch(View.OnTouchListener touch) {
            super();
            _touch = touch;
        }

        public Touch(View.OnTouchListener touch, @Nullable UIEvent.UTEvent ut) {
            super(ut);
            _touch = touch;
        }

        public Touch(View.OnTouchListener touch, @Nullable String utid, @Nullable UIEvent.UTEvent ut) {
            super(ut,utid);
            _touch = touch;
        }
    }


    public final static class Watcher extends UT implements TextWatcher {
        private TextWatcher _watcher;

        public void beforeTextChanged(CharSequence var1, int var2, int var3, int var4) {
            try {
                if (_watcher != null) _watcher.beforeTextChanged(var1, var2, var3, var4);
                track(null);//打点
            }
            catch (Throwable e) {
                APPLog.error(e);
            }
        }

        public void onTextChanged(CharSequence var1, int var2, int var3, int var4) {
            try {
                if (_watcher != null) _watcher.onTextChanged(var1, var2, var3, var4);
                track(null);//打点
            }
            catch (Throwable e) {
                APPLog.error(e);
            }
        }

        public void afterTextChanged(Editable var1) {
            try {
                if (_watcher != null) _watcher.afterTextChanged(var1);
                track(null);//打点
            }
            catch (Throwable e) {
                APPLog.error(e);
            }
        }

        public Watcher(TextWatcher watcher) {
            super();
            _watcher = watcher;
        }

        public Watcher(TextWatcher watcher, @Nullable UIEvent.UTEvent ut) {
            super(ut);
            _watcher = watcher;
        }

        public Watcher(TextWatcher watcher, @Nullable String utid, @Nullable UIEvent.UTEvent ut) {
            super(ut,utid);
            _watcher = watcher;
        }
    }


    /**
     *  AdapterView cell选择 事件实现
     */
    public final static class Selected extends UT implements AdapterView.OnItemSelectedListener {
        private AdapterView.OnItemSelectedListener _selected;

        public void onItemSelected(AdapterView<?> var1, View var2, int var3, long var4) {
            try {
                if (_selected != null) _selected.onItemSelected(var1, var2, var3, var4);
                track(var2);//打点
            }
            catch (Throwable e) {
                APPLog.error(e);
            }
        }

        public void onNothingSelected(AdapterView<?> var1) {
            try {
                if (_selected != null) _selected.onNothingSelected(var1);
                track(var1);//打点
            }
            catch (Throwable e) {
                APPLog.error(e);
            }
        }

        public Selected(AdapterView.OnItemSelectedListener selected) {
            super();
            _selected = selected;
        }

        public Selected(AdapterView.OnItemSelectedListener selected, @Nullable UIEvent.UTEvent ut) {
            super(ut);
            _selected = selected;
        }

        public Selected(AdapterView.OnItemSelectedListener selected, @Nullable String utid, @Nullable UIEvent.UTEvent ut) {
            super(ut,utid);
            _selected = selected;
        }
    }

    public final static class ItemLongClick extends UT implements AdapterView.OnItemLongClickListener {
        private AdapterView.OnItemLongClickListener _click;

        public boolean onItemLongClick(AdapterView<?> var1, View var2, int var3, long var4) {
            boolean result = false;
            try {
                if (_click != null) result = _click.onItemLongClick(var1, var2, var3, var4);
                track(var2);//打点
            }
            catch (Throwable e) {
                APPLog.error(e);
            }
            return result;
        }

        public ItemLongClick(AdapterView.OnItemLongClickListener click) {
            super();
            _click = click;
        }

        public ItemLongClick(AdapterView.OnItemLongClickListener click, @Nullable UIEvent.UTEvent ut) {
            super(ut);
            _click = click;
        }

        public ItemLongClick(AdapterView.OnItemLongClickListener click, @Nullable String utid, @Nullable UIEvent.UTEvent ut) {
            super(ut,utid);
            _click = click;
        }
    }

    public final static class ItemClick extends UT implements AdapterView.OnItemClickListener {
        private AdapterView.OnItemClickListener _click;

        private void onInnerItemClick(AdapterView<?> var1, View var2, int var3, long var4) {
            try {
                if (_click != null) _click.onItemClick(var1, var2, var3, var4);
                track(var2);//打点
            }
            catch (Throwable e) {
                APPLog.error(e);
            }
        }

        public void onItemClick(AdapterView<?> var1, View var2, int var3, long var4) {
            if (_click == null) {return;}

            if (tryLock()) {//是否要排他
                lock();
                onInnerItemClick(var1, var2, var3, var4);
                unlock();
            }
        }

        public ItemClick(AdapterView.OnItemClickListener click) {
            super();
            _click = click;
        }

        public ItemClick(AdapterView.OnItemClickListener click, @Nullable UIEvent.UTEvent ut) {
            super(ut);
            _click = click;
        }

        public ItemClick(AdapterView.OnItemClickListener click, @Nullable String utid, @Nullable UIEvent.UTEvent ut) {
            super(ut,utid);
            _click = click;
        }
    }

    /*
    public interface OnKeyListener {
        boolean onKey(View var1, int var2, KeyEvent var3);
    }
    */


    /**
     * 打点基类
     */
    public static class UT {

        /**
         * 排他属性
         */
        private boolean _nonExclusive;
        public boolean isExclusive() {return !_nonExclusive;}
        public void setExclusive(boolean exclusive) {_nonExclusive = !exclusive;}

        //exclusive 实现
        private final static long MIN_CLICK_SPACE   = 300;
        private static long _last_click_at = 0;
        private static boolean _event_lock_recursion = false;

        /**
         * 获得event lock
         * @return 返回yes表示有效，返回no表示无效
         */
        protected final boolean tryLock() {
            //非排他属性时，立即响应
            if (_nonExclusive) {return true;}

            //说明递归调用
            if (_event_lock_recursion) {return true;}

            long at = System.currentTimeMillis();
            if (_last_click_at == 0 || at > _last_click_at + MIN_CLICK_SPACE || _last_click_at > at + MIN_CLICK_SPACE) {
                _last_click_at = at;
                return true;
            }
            return false;
        }

        protected final void lock() {_event_lock_recursion = true;}
        protected final void unlock() {_event_lock_recursion = false;}

        private UIEvent.UTEvent _ut;
        private String _utid;
        public UT(){}
        public UT(@Nullable UTEvent ut) {_ut = ut;}
        public UT(@Nullable UTEvent ut,@Nullable String utid) {_ut = ut;_utid = utid;}
        protected void track(View var1) {if (_ut != null) _ut.onTrack(var1,_utid);}
    }

    /**
     * 用户跟踪事件，打点需要
     */
    public static interface UTEvent {
        void onTrack(View var1, String var2);
    }

    /**
     * 系统事件通知定义
     */
    public static final String UIKeyboardWillShowNotification = "_keyboard_will_show_notification";
    public static final String UIKeyboardWillHideNotification = "_keyboard_will_hide_notification";
    public static final String UIKeyboardHeightKey            = "_keyboard_height_key";//高度像素
}
