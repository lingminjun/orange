package com.ssn.framework.uikit.pullview.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import com.ssn.framework.uikit.pullview.ILoadingLayout;

/*
 * 文件名：BaseLoadingLayout.java
 * 版权：(C)版权所有2015-2015顺丰国际电商
 * 描述：
 * 修改人：guankaiqiang
 * 修改时间：202015/7/24 18:25
 */
public abstract class BaseLoadingLayout extends FrameLayout implements ILoadingLayout {
    public BaseLoadingLayout(Context context) {
        super(context);
    }

    public BaseLoadingLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseLoadingLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BaseLoadingLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setLabels(String pullLabel,String refreshingLabel,String releaseLabel){
    }

    public abstract void hideAllViews();

    public abstract void showInvisibleViews();

    public abstract void onPull(float scaleOfLayout);

    public abstract void onScroll(int moveDistance);

    public abstract int getContentSize();

    public abstract void refreshing();

    public abstract void reset();

    public abstract void setWidth(int width);

    public abstract void setHeight(int height);

    public abstract void pullToRefresh();

    public abstract void releaseToRefresh();

    protected abstract int getDefaultDrawableResId();

    protected abstract void onLoadingDrawableSet(Drawable imageDrawable);

    protected abstract void onPullImpl(float scaleOfLayout);

    protected abstract void pullToRefreshImpl();

    protected abstract void refreshingImpl();

    protected abstract void releaseToRefreshImpl();

    protected abstract void resetImpl();
}
