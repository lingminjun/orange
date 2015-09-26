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
public class ViewEvent {

    /**
     * 点击事件工厂方法
     */
    public static Click click(View.OnClickListener listener) {
        return new ViewEvent.Click(listener);
    }
    public static Click click(View.OnClickListener listener, @Nullable UTEvent ut, @Nullable String utid) {
        return new ViewEvent.Click(listener,utid,ut);
    }


    /**
     * 焦点事件工厂方法
     */
    public static FocusChange focus(View.OnFocusChangeListener listener) {
        return new ViewEvent.FocusChange(listener);
    }
    public static FocusChange focus(View.OnFocusChangeListener listener, @Nullable UTEvent ut, @Nullable String utid) {
        return new ViewEvent.FocusChange(listener,utid,ut);
    }

    /**
     * 长按事件工厂方法
     */
    public static LongClick longClick(View.OnLongClickListener listener) {
        return new ViewEvent.LongClick(listener);
    }
    public static LongClick longClick(View.OnLongClickListener listener, @Nullable UTEvent ut, @Nullable String utid) {
        return new ViewEvent.LongClick(listener,utid,ut);
    }

    /**
     * touch事件工厂方法
     */
    public static Touch touch(View.OnTouchListener listener) {
        return new ViewEvent.Touch(listener);
    }
    public static Touch touch(View.OnTouchListener listener, @Nullable UTEvent ut, @Nullable String utid) {
        return new ViewEvent.Touch(listener,utid,ut);
    }

    /**
     * Watcher工厂方法
     */
    public static Watcher watcher(TextWatcher watcher) {
        return new ViewEvent.Watcher(watcher);
    }
    public static Watcher watcher(TextWatcher watcher, @Nullable UTEvent ut, @Nullable String utid) {
        return new ViewEvent.Watcher(watcher,utid,ut);
    }

    /**
     * Selected工厂方法
     */
    public static Selected select(AdapterView.OnItemSelectedListener select) {
        return new ViewEvent.Selected(select);
    }
    public static Selected select(AdapterView.OnItemSelectedListener select, @Nullable UTEvent ut, @Nullable String utid) {
        return new ViewEvent.Selected(select,utid,ut);
    }

    /**
     * Item Long Click工厂方法
     */
    public static ItemLongClick itemLongClick(AdapterView.OnItemLongClickListener click) {
        return new ViewEvent.ItemLongClick(click);
    }
    public static ItemLongClick itemLongClick(AdapterView.OnItemLongClickListener click, @Nullable UTEvent ut, @Nullable String utid) {
        return new ViewEvent.ItemLongClick(click,utid,ut);
    }

    /**
     * Item Click工厂方法
     */
    public static ItemClick itemClick(AdapterView.OnItemClickListener click) {
        return new ViewEvent.ItemClick(click);
    }
    public static ItemClick itemClick(AdapterView.OnItemClickListener click, @Nullable UTEvent ut, @Nullable String utid) {
        return new ViewEvent.ItemClick(click,utid,ut);
    }

    /**
     * 点击事件默认实现
     */
    public final static class Click extends UT implements View.OnClickListener {
        private View.OnClickListener _click;

        //exclusive 实现
        private final static long MIN_CLICK_SPACE   = 300;
        private static long _last_click_at = 0;

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

            if (_click instanceof Click) {
                ((Click) _click).onInnerClick(var1);
            }
            else {
                long at = System.currentTimeMillis();
                if (_last_click_at == 0 || at > _last_click_at + MIN_CLICK_SPACE || _last_click_at > at + MIN_CLICK_SPACE) {
                    _last_click_at = at;
                    onInnerClick(var1);
                }
            }
        }

        public Click(View.OnClickListener click) {
            super();
            _click = click;
        }

        public Click(View.OnClickListener click, @Nullable ViewEvent.UTEvent ut) {
            super(ut);
            _click = click;
        }

        public Click(View.OnClickListener click, @Nullable String utid, @Nullable ViewEvent.UTEvent ut) {
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

        public FocusChange(View.OnFocusChangeListener click, @Nullable ViewEvent.UTEvent ut) {
            super(ut);
            _listener = click;
        }

        public FocusChange(View.OnFocusChangeListener click, @Nullable String utid, @Nullable ViewEvent.UTEvent ut) {
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

        public LongClick(View.OnLongClickListener click, @Nullable ViewEvent.UTEvent ut) {
            super(ut);
            _click = click;
        }

        public LongClick(View.OnLongClickListener click, @Nullable String utid, @Nullable ViewEvent.UTEvent ut) {
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

        public Touch(View.OnTouchListener touch, @Nullable ViewEvent.UTEvent ut) {
            super(ut);
            _touch = touch;
        }

        public Touch(View.OnTouchListener touch, @Nullable String utid, @Nullable ViewEvent.UTEvent ut) {
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

        public Watcher(TextWatcher watcher, @Nullable ViewEvent.UTEvent ut) {
            super(ut);
            _watcher = watcher;
        }

        public Watcher(TextWatcher watcher, @Nullable String utid, @Nullable ViewEvent.UTEvent ut) {
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

        public Selected(AdapterView.OnItemSelectedListener selected, @Nullable ViewEvent.UTEvent ut) {
            super(ut);
            _selected = selected;
        }

        public Selected(AdapterView.OnItemSelectedListener selected, @Nullable String utid, @Nullable ViewEvent.UTEvent ut) {
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

        public ItemLongClick(AdapterView.OnItemLongClickListener click, @Nullable ViewEvent.UTEvent ut) {
            super(ut);
            _click = click;
        }

        public ItemLongClick(AdapterView.OnItemLongClickListener click, @Nullable String utid, @Nullable ViewEvent.UTEvent ut) {
            super(ut,utid);
            _click = click;
        }
    }

    public final static class ItemClick extends UT implements AdapterView.OnItemClickListener {
        private AdapterView.OnItemClickListener _click;

        public void onItemClick(AdapterView<?> var1, View var2, int var3, long var4) {
            try {
                if (_click != null) _click.onItemClick(var1, var2, var3, var4);
                track(var2);//打点
            }
            catch (Throwable e) {
                APPLog.error(e);
            }
        }

        public ItemClick(AdapterView.OnItemClickListener click) {
            super();
            _click = click;
        }

        public ItemClick(AdapterView.OnItemClickListener click, @Nullable ViewEvent.UTEvent ut) {
            super(ut);
            _click = click;
        }

        public ItemClick(AdapterView.OnItemClickListener click, @Nullable String utid, @Nullable ViewEvent.UTEvent ut) {
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
        private ViewEvent.UTEvent _ut;
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
}
