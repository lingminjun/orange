package com.ssn.framework.uikit;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ssn.framework.R;
import com.ssn.framework.foundation.APPLog;
import com.ssn.framework.foundation.Density;
import com.ssn.framework.foundation.Res;

/**
 * Created by lingminjun on 15/9/27.
 */
public abstract class UITableViewCell extends RelativeLayout {

    public static int TABLE_VIEW_CELL_DEFAULT_HEIGHT = 44;
    public static int TABLE_VIEW_CELL_MIN_HEIGHT = 10;

    public abstract static class CellModel {

        public int height;//高度

        public int separateLineLeftPadding;//分割线左边距，(dp)
        public boolean hiddenSeparateLine;//分割线是否隐藏
        public int separateLineColor;//分割线颜色
        public boolean hiddenRightArrow;//隐藏右边箭头

        /**
         * 一些事件支持
         */
        public ViewEvent.LongClick longClick;
        public ViewEvent.Click     click;

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
        public final int getCellType() {
            int code = getClass().hashCode();
//            Log.e("xxx","code="+code+",class:"+getClass().getSimpleName());
            return code;
        }

        public int getModelID() {
            return this.hashCode();
        }
    }

    private LinearLayout _container;
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
        _container = (LinearLayout)this.findViewById(R.id.ssn_view_container);
        _rightArrow = (ImageView)this.findViewById(R.id.ssn_right_arrow_icon);
        _separateLine = (TextView)this.findViewById(R.id.ssn_separate_line);
    }

    /**
     * 子类初始化
     * @param context
     */
    private void derivedInit(Context context) {
        if (_customView == null) {
            try {
                ViewGroup viewGroup = _container;
                if (_container == null) {
                    viewGroup = this;
                }
                _customView = loadCustomDisplayView(LayoutInflater.from(context),viewGroup);
            } catch (Throwable e) {APPLog.error(e);}
            if (_customView != null && _container != _customView && _container != null) {
                _container.addView(_customView);
            }
        }
    }

    public static UITableViewCell newInstance(CellModel cellModel,Context context) {
        UITableViewCell cell = null;
        try {
            cell = cellModel.createCell(context);
        }catch (Throwable e){APPLog.error(e);}

        if (cell != null) {
            cell.derivedInit(context);
        }
        return cell;
    }

    public static void displayCell(UITableViewCell cell, CellModel cellModel, int row) {
        cell._cellModel = null;
        try {
            cell.onPrepareForReuse();
        } catch (Throwable e) {APPLog.error(e);}

        try {
            cell.onDisplay(cellModel, row);
        } catch (Throwable e) {APPLog.error(e);}

        cell._cellModel = cellModel;//防止子类替换
    }

    /**
     * 你需要展示的view，仅仅在此view第一次创建时调用，务必将所有subview都记录下来，提高复用效率
     * @param inflate
     * @param containerView
     * @return inflate.inflate(R.layout.custom_cell, containerView);
     */
    protected abstract View loadCustomDisplayView(LayoutInflater inflate,ViewGroup containerView);

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
    protected void onDisplay(CellModel cellModel, int row) {
        this._cellModel = cellModel;

//        LayoutParams params = (LayoutParams)this.getLayoutParams();
//        if (cellModel.height < TABLE_VIEW_CELL_MIN_HEIGHT) {
//            params.height = Density.dipTopx(TABLE_VIEW_CELL_DEFAULT_HEIGHT);
//        }
//        else {
//            params.height = Density.dipTopx(cellModel.height);
//        }

        if (!cellModel.hiddenSeparateLine) {
            _separateLine.setVisibility(VISIBLE);

            //设置边距
            LayoutParams separate_params = (LayoutParams) _separateLine.getLayoutParams();
            if (cellModel.separateLineLeftPadding > 0) {
                separate_params.leftMargin = Density.dipTopx(cellModel.separateLineLeftPadding);
            }
            else {
                separate_params.leftMargin = 0;
            }

            //设置颜色
            if (cellModel.separateLineColor > 0) {
                _separateLine.setBackgroundColor(Res.color(cellModel.separateLineColor));
            }
            else {
                _separateLine.setBackgroundColor(Res.color(android.R.color.darker_gray));
            }
        }
        else {
            _separateLine.setVisibility(GONE);
        }

        if (cellModel.hiddenRightArrow) {
            _rightArrow.setVisibility(GONE);
        }
        else {
            _rightArrow.setVisibility(VISIBLE);
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
}
