package com.ssn.framework.uikit;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.ssn.framework.R;
import com.ssn.framework.foundation.APPLog;
import com.ssn.framework.foundation.Density;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.foundation.TaskQueue;

/**
 * Created by lingminjun on 15/9/27.
 */
public abstract class UITableViewCell extends RelativeLayout {

    public static int TABLE_VIEW_CELL_DEFAULT_HEIGHT = 44;
    public static int TABLE_VIEW_CELL_MIN_HEIGHT = 10;

    public abstract static class CellModel {

        public int height;//高度

        public boolean disabled;//不可点击的

        public int separateLineLeftPadding;//分割线左边距，(dp)
        public boolean hiddenSeparateLine;//分割线是否隐藏
        public int separateLineColor;//分割线颜色
        public boolean hiddenRightArrow;//隐藏右边箭头

        public CellModel() {}

        /**
         * 若cell中存在EditText时，务必设置成yes，否则无法获取光标
         * 在派生类构造函数中调用super(true);
         * @param needInput 是否需要输入内容，需要获取键盘和光标
         */
        public CellModel(boolean needInput){this.needInput=needInput;}

        /**
         * 一些事件支持
         */
        public UIEvent.LongClick longClick;
        public UIEvent.Click     click;

        /**
         * 子类需要实现此方法，返回其对应的实例方法
         * @param context
         * @return
         */
        protected abstract UITableViewCell createCell(Context context);

        /**
         * 返回当前cell type
         * @return
         */
        public final String cellIdentifier() {
            if (_id != null) {
                return _id;
            }
            _id = getClass().getName();
//            Log.e("xxx","code="+code+",class:"+getClass().getSimpleName());
            return _id;
        }
        private String _id;

        public int getModelID() {
            return this.hashCode();
        }

        /**
         * 此cell需要输入，此处指包含EditText控件
         */
        private boolean needInput;

        /**
         * EditText支持
         */
        private int _editID;
//        private EditText _edit;
        private boolean _showKeyboard;
    }

    private ViewGroup _container;
    private View _customView;
    private ImageView _rightArrow;
    private TextView _separateLine;

    private CellModel _cellModel;

    public UITableViewCell(Context context) {
        super(context);
        init(context,null);
    }

    public UITableViewCell(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public UITableViewCell(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context,attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.ssn_table_view_cell, this);
        _container = (ViewGroup)this.findViewById(R.id.ssn_view_container);
        _rightArrow = (ImageView)this.findViewById(R.id.ssn_right_arrow_icon);
        _separateLine = (TextView)this.findViewById(R.id.ssn_separate_line);
    }

    /**
     * 子类初始化
     * @param context
     */
    private void derivedInit(Context context,CellModel cellModel) {
        if (_customView == null) {
            try {
                ViewGroup viewGroup = _container;
                if (_container == null) {
                    viewGroup = this;
                }
                _customView = loadCustomDisplayView(LayoutInflater.from(context),viewGroup);
            } catch (Throwable e) {APPLog.error(e);}
            if (_customView != null && _container != _customView && _container != null) {
                int index = _container.indexOfChild(_customView);
                if (index < 0 || index >= _container.getChildCount()) {
                    _container.addView(_customView);
                }
            }

            //检查是否存在出入框
            if (cellModel.needInput && checkContainedEditText(_container)) {
                _container.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);//还原到默认值
            }
        }
    }

    public static boolean checkContainedEditText(ViewGroup group) {
        int count = group.getChildCount();
        for (int i = 0; i < count; i++) {
            View v = group.getChildAt(i);
            if (v instanceof EditText) {
                return true;
            }
            else if (v instanceof ViewGroup) {
                if (checkContainedEditText((ViewGroup)v)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static UITableViewCell newInstance(CellModel cellModel,Context context) {
        UITableViewCell cell = null;
        try {
            cell = cellModel.createCell(context);
        }catch (Throwable e){APPLog.error(e);}

        if (cell != null) {
            cell.derivedInit(context,cellModel);
        }
        return cell;
    }

    /**
     * 获取焦点时处理
     * @param cell
     * @param editText
     */
    public static void prepareFocus(final UITableViewCell cell, EditText editText) {
        UITableViewCell.CellModel cellModel = cell.cellModel();
        cellModel._editID = editText.getId();
//        cellModel._edit = editText;
        cellModel._showKeyboard = true;
    }

    private static void afreshFocus(final UITableViewCell cell, final CellModel cellModel) {
        TaskQueue.mainQueue().executeDelayed(new Runnable() {
            @Override
            public void run() {
                cellModel._showKeyboard = false;

                if (cell._cellModel != cellModel) {
//                    cellModel._edit = null;
                    return;
                }

                View parentView = cell._customView;
                if (parentView == null) {
                    parentView = cell._container;
                }

                if (cellModel._editID <= 0 || parentView == null) {
//                    cellModel._edit = null;
                    return;
                }

                EditText editText = (EditText)parentView.findViewById(cellModel._editID);
                if (editText != null) {

//                    if (cellModel._edit != null && editText != cellModel._edit) {
//                        cellModel._edit.clearFocus();
//                    }
//                    cellModel._edit = null;

                    InputMethodManager imm = (InputMethodManager) Res.context().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(editText, 0);
                    editText.requestFocus();
                    Editable editable = editText.getText();
                    if (editable != null) {
                        editText.setSelection(editable.length());
                    }
                    else {
                        editText.setSelection(0);
                    }
//                    Log.e("reshow","txt"+editText.hashCode());
                }
            }
        },100);
    }

    public static void displayCell(final UITableViewCell cell, final CellModel cellModel, int row) {
        cell._cellModel = null;
        try {
            cell.onPrepareForReuse();
        } catch (Throwable e) {APPLog.error(e);}

        try {
            cell._cellModel = cellModel;
            cell.onDisplay(cellModel, row);
        } catch (Throwable e) {APPLog.error(e);}

        cell._cellModel = cellModel;//防止子类替换
        cell._onDisplay(cellModel,row);

        if (cellModel.needInput && cellModel._showKeyboard) {
            afreshFocus(cell,cellModel);
        }
    }

    /**
     * 展示的custom display view
     * @return
     */
    protected final View displayView() {return _container;}

    /**
     * 你需要展示的view，仅仅在此view第一次创建时调用，务必将所有subview都记录下来，提高复用效率
     * @param inflate
     * @param containerView
     * @return inflate(inflate, R.layout.custom_cell, containerView);
     *         或者 inflate.inflate(R.layout.custom_cell, containerView);
     */
    protected abstract View loadCustomDisplayView(LayoutInflater inflate,ViewGroup containerView);

    /**
     * 仅仅用于loadCustomDisplayView实现
     * @param inflate
     * @param layout
     * @param root
     * @return
     */
    protected static View inflate(LayoutInflater inflate,int layout, ViewGroup root) {
        View view = inflate.inflate(layout, root);
        if (view != root) {
            return view;
        }
        if (view == null) {
            return null;
        }
        return root.getChildAt(root.getChildCount() - 1);
    }

    /**
     * 所适配的数据
     * @return
     */
    protected CellModel cellModel() {return _cellModel;}

    /**
     * 复用cell前回调，必须实现
     */
    protected abstract void onPrepareForReuse();

    /**
     * 展示view
     * @param cellModel
     * @param row
     */
    protected void onDisplay(CellModel cellModel, int row) {}

    //防止子类实现不调用父类方法造成错误
    private void _onDisplay(CellModel cellModel, int row) {
        if (_container != null) {
            LayoutParams params = (LayoutParams) _container.getLayoutParams();
            if (cellModel.height > 0 && cellModel.height < TABLE_VIEW_CELL_MIN_HEIGHT) {
                params.height = Density.dipTopx(TABLE_VIEW_CELL_DEFAULT_HEIGHT);
            } else if (cellModel.height >= TABLE_VIEW_CELL_MIN_HEIGHT) {
                params.height = Density.dipTopx(cellModel.height);
            }
            else {
                params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            }
        }

        if (_separateLine != null) {
            if (!cellModel.hiddenSeparateLine) {
                _separateLine.setVisibility(VISIBLE);

                //设置边距
                LayoutParams separate_params = (LayoutParams) _separateLine.getLayoutParams();
                if (cellModel.separateLineLeftPadding > 0) {
                    separate_params.leftMargin = Density.dipTopx(cellModel.separateLineLeftPadding);
                } else {
                    separate_params.leftMargin = 0;
                }

                //设置颜色
                if (cellModel.separateLineColor != 0) {
                    _separateLine.setBackgroundColor(cellModel.separateLineColor);
                } else {
                    _separateLine.setBackgroundColor(Res.color(android.R.color.darker_gray));
                }
            } else {
                _separateLine.setVisibility(GONE);
            }
        }

        if (_rightArrow != null) {
            if (cellModel.hiddenRightArrow) {
                _rightArrow.setVisibility(GONE);
            } else {
                _rightArrow.setVisibility(VISIBLE);
            }
        }
    }


    /**
     * 设置分割线的左右padding
     *
     * @param leftPadding  左边(dp)
     */
    protected void setSeparateLeftPadding(int leftPadding) {
        LayoutParams params = (LayoutParams) _separateLine.getLayoutParams();
        params.leftMargin = leftPadding;
        _separateLine.setLayoutParams(params);
    }

    /**
     * 影藏分割线
     * @param hidden
     */
    protected void hiddenSeparate(boolean hidden) {
        if (hidden) {
            _separateLine.setVisibility(GONE);
        }
        else {
            _separateLine.setVisibility(VISIBLE);
        }
    }


    protected void hiddenRightArrow(boolean hidden) {
        if (hidden) {
            _rightArrow.setVisibility(GONE);
        }
        else {
            _rightArrow.setVisibility(VISIBLE);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (_cellModel != null && _cellModel.disabled) {
            return true;
        }
        return super.dispatchTouchEvent(ev);
    }

    /*//touch 事件分发，下面是点击非edit隐藏键盘实现思路，
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideInput(v, ev)) {

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    public  boolean isShouldHideInput(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] leftTop = { 0, 0 };
            //获取输入框当前的location位置
            v.getLocationInWindow(leftTop);
            int left = leftTop[0];
            int top = leftTop[1];
            int bottom = top + v.getHeight();
            int right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击的是输入框区域，保留点击EditText的事件
                return false;
            } else {
                return true;
            }
        }
        return false;
    }
    */
}
