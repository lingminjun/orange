package com.ssn.framework.uikit.pullview.internal;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.ssn.framework.R;
import com.ssn.framework.uikit.pullview.PullToRefreshBase;

/*
 * 文件名：FishLoadingLayout.java
 * 版权：(C)版权所有2015-2015顺丰国际电商
 * 描述：下拉为动画小章鱼风格，上拉为默认的文字风格
 * 修改时间：202015/7/24 14:31
 */

public class FishLoadingLayout extends BaseLoadingLayout {
    private static int MAX_FISH_SIZE;

    private int layoutId;//layoutId of this view
    protected final PullToRefreshBase.Mode mMode;
    protected final PullToRefreshBase.Orientation mScrollDirection;
    private Context context;

    private ViewGroup mInnerLayout;
    //下拉模式（章鱼模式）
    protected ImageView fish_icon;//小章鱼icon
    protected ImageView refresh_tip_icon;//只卖正品，顺丰直营
    //上拉模式（文字模式）
    static final int FLIP_ANIMATION_DURATION = 150;
    protected TextView pull_to_refresh_text;
    protected TextView pull_to_refresh_sub_text;
    protected ImageView pull_to_refresh_image;//下拉或上拉的箭头
    protected ProgressBar mHeaderProgress;
    private boolean mUseIntrinsicAnimation;
    private CharSequence mPullLabel;
    private CharSequence mRefreshingLabel;
    private CharSequence mReleaseLabel;
    private Animation mRotateAnimation, mResetRotateAnimation;
    static final Interpolator ANIMATION_INTERPOLATOR = new LinearInterpolator();

    public FishLoadingLayout(Context context, final PullToRefreshBase.Mode mode, final PullToRefreshBase.Orientation scrollDirection, TypedArray attrs) {
        super(context);
        this.context = context;
        mMode = mode;
        mScrollDirection = scrollDirection;
        MAX_FISH_SIZE = context.getResources().getDimensionPixelSize(R.dimen.max_fish_size);
        initAnimations(mode);
        initViews(context, mode, scrollDirection);
        setThisBg(attrs);//设置背景颜色
        setTextStyle(attrs);//设置字体样式
        // Try and get defined drawable from Attrs
        initLoadingDrawable(mode, attrs);
        reset();
    }

    private void initAnimations(PullToRefreshBase.Mode mode) {//用于文字模式的动画
        final int rotateAngle = mode == PullToRefreshBase.Mode.PULL_FROM_START ? -180 : 180;

        mRotateAnimation = new RotateAnimation(0, rotateAngle, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mRotateAnimation.setInterpolator(ANIMATION_INTERPOLATOR);
        mRotateAnimation.setDuration(FLIP_ANIMATION_DURATION);
        mRotateAnimation.setFillAfter(true);

        mResetRotateAnimation = new RotateAnimation(rotateAngle, 0, Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        mResetRotateAnimation.setInterpolator(ANIMATION_INTERPOLATOR);
        mResetRotateAnimation.setDuration(FLIP_ANIMATION_DURATION);
        mResetRotateAnimation.setFillAfter(true);
    }

    private void initViews(Context context, PullToRefreshBase.Mode mode, PullToRefreshBase.Orientation scrollDirection) {
        switch (scrollDirection) {
            case HORIZONTAL:
                break;
            case VERTICAL:
            default:
                //根据是属于上拉模式还是下拉模式的view来选择layout，下拉选择章鱼模式，上拉选择默认的文字模式
                layoutId = mode == PullToRefreshBase.Mode.PULL_FROM_START ? R.layout.pull_to_refresh_fish_header_vertical : R.layout.pull_to_refresh_header_vertical;
                LayoutInflater.from(context).inflate(layoutId, this);
                break;
        }
        mInnerLayout = (ViewGroup) findViewById(R.id.fl_inner);
        initFishViews();
        initTextModeViews();
        LayoutParams lp = (LayoutParams) mInnerLayout.getLayoutParams();
        switch (mode) {
            case PULL_FROM_END:
                // Load in labels
                mPullLabel = context.getString(R.string.pull_to_refresh_from_bottom_pull_label);
                mRefreshingLabel = context.getString(R.string.pull_to_refresh_from_bottom_refreshing_label);
                mReleaseLabel = context.getString(R.string.pull_to_refresh_from_bottom_release_label);
                FrameLayout leftContainer = (FrameLayout) findViewById(R.id.left_container);
                LayoutParams leftContainerlp = (LayoutParams) leftContainer.getLayoutParams();
                leftContainerlp.gravity = scrollDirection == PullToRefreshBase.Orientation.VERTICAL ? Gravity.TOP : Gravity.LEFT;
                lp.gravity = leftContainerlp.gravity;
                break;
            case PULL_FROM_START:
            default:
                lp.gravity = scrollDirection == PullToRefreshBase.Orientation.VERTICAL ? Gravity.BOTTOM : Gravity.RIGHT;
                break;
        }
    }

    private void initTextModeViews() {
        pull_to_refresh_text = (TextView) findViewById(R.id.pull_to_refresh_text);
        mHeaderProgress = (ProgressBar) mInnerLayout.findViewById(R.id.pull_to_refresh_progress);
        pull_to_refresh_sub_text = (TextView) findViewById(R.id.pull_to_refresh_sub_text);
        pull_to_refresh_image = (ImageView) findViewById(R.id.pull_to_refresh_image);
    }

    private void initFishViews() {
        fish_icon = (ImageView) mInnerLayout.findViewById(R.id.fish_icon);
        refresh_tip_icon = (ImageView) mInnerLayout.findViewById(R.id.refresh_tip_icon);
    }

    private void setThisBg(TypedArray attrs) {
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderBackground)) {
            Drawable background = attrs.getDrawable(R.styleable.PullToRefresh_ptrHeaderBackground);
            if (null != background) {
                ViewCompat.setBackground(this, background);
            }
        }
    }

    private void setTextStyle(TypedArray attrs) {
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrSubHeaderTextAppearance)) {
            TypedValue styleID = new TypedValue();
            attrs.getValue(R.styleable.PullToRefresh_ptrSubHeaderTextAppearance, styleID);
            setSubTextAppearance(styleID.data);
        }

        // Text Color attrs need to be set after TextAppearance attrs
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderTextColor)) {
            ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderTextColor);
            if (null != colors) {
                setTextColor(colors);
            }
        }
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrHeaderSubTextColor)) {
            ColorStateList colors = attrs.getColorStateList(R.styleable.PullToRefresh_ptrHeaderSubTextColor);
            if (null != colors) {
                setSubTextColor(colors);
            }
        }
    }

    private void initLoadingDrawable(PullToRefreshBase.Mode mode, TypedArray attrs) {
        Drawable imageDrawable = null;
        if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawable)) {
            imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawable);
        }
        // Check Specific Drawable from Attrs, these overrite the generic
        // drawable attr above
        switch (mode) {
            case PULL_FROM_START:
            default:
                if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableStart)) {
                    imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableStart);
                } else if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableTop)) {
                    Utils.warnDeprecation("ptrDrawableTop", "ptrDrawableStart");
                    imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableTop);
                }
                break;

            case PULL_FROM_END:
                if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableEnd)) {
                    imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableEnd);
                } else if (attrs.hasValue(R.styleable.PullToRefresh_ptrDrawableBottom)) {
                    Utils.warnDeprecation("ptrDrawableBottom", "ptrDrawableEnd");
                    imageDrawable = attrs.getDrawable(R.styleable.PullToRefresh_ptrDrawableBottom);
                }
                break;
        }


        // If we don't have a user defined drawable, load the default
        if (null == imageDrawable) {
            imageDrawable = context.getResources().getDrawable(getDefaultDrawableResId());
        }

        // Set Drawable, and save width/height
        setLoadingDrawable(imageDrawable);
    }

    private void setSubTextAppearance(int value) {
        if (null != pull_to_refresh_text) {
            pull_to_refresh_text.setTextAppearance(getContext(), value);
        }
    }

    private void setTextColor(ColorStateList color) {
        if (null != pull_to_refresh_text) {
            pull_to_refresh_text.setTextColor(color);
        }
        if (null != pull_to_refresh_sub_text) {
            pull_to_refresh_sub_text.setTextColor(color);
        }
    }

    private void setSubTextColor(ColorStateList color) {
        if (null != pull_to_refresh_sub_text) {
            pull_to_refresh_sub_text.setTextColor(color);
        }
    }

    @Override
    public void onPull(float scaleOfLayout) {
        if (!mUseIntrinsicAnimation) {
            onPullImpl(scaleOfLayout);
        }
    }

    @Override
    public void onScroll(int moveDistance) {
        setFishIconSize(moveDistance, moveDistance);
    }

    public void setFishIconSize(int width, int height) {
        if (fish_icon != null) {
            Drawable drawable = fish_icon.getDrawable();
            if (drawable != null && drawable instanceof AnimationDrawable) {
                return;
            }
            ViewGroup.LayoutParams params = fish_icon.getLayoutParams();
            int _width = width > MAX_FISH_SIZE ? MAX_FISH_SIZE : width;
            int _height = height > MAX_FISH_SIZE ? MAX_FISH_SIZE : height;
            params.width = _width;
            params.height = _height;
//            APPLog.error("setImageResource setFishIconSize");
            fish_icon.setImageResource(getRefreshIconResByImgSize(_width));
            fish_icon.setLayoutParams(params);
        }
    }

    private int getRefreshIconResByImgSize(int size) {
        float f = size * 1f / MAX_FISH_SIZE;
        int imgId;
        if (f <= 0.5) {
            imgId = R.drawable.icon_refresh_1;
        } else if (f > 0.5 && f <= 0.75) {
            imgId = R.drawable.icon_refresh_5;
        } else if (f > 0.75 && f <= 0.85) {
            imgId = R.drawable.icon_refresh_6;
        } else {
            imgId = R.drawable.icon_refresh_7;
        }
        return imgId;
    }

    @Override
    public int getContentSize() {
        switch (mScrollDirection) {
            case HORIZONTAL:
                return mInnerLayout.getWidth();
            case VERTICAL:
            default:
                return mInnerLayout.getHeight();
        }
    }

    @Override
    public void refreshing() {
        if (null != pull_to_refresh_text) {
            pull_to_refresh_text.setText(mRefreshingLabel);
        }

        if (mUseIntrinsicAnimation) {
            ((AnimationDrawable) pull_to_refresh_image.getDrawable()).start();
        } else {
            // Now call the callback
            refreshingImpl();
        }
    }

    public final void reset() {
        if (null != pull_to_refresh_text) {
            pull_to_refresh_text.setText(mPullLabel);
        }
        if (pull_to_refresh_image != null) {
            pull_to_refresh_image.setVisibility(View.VISIBLE);
        }
        if (mUseIntrinsicAnimation) {
            ((AnimationDrawable) pull_to_refresh_image.getDrawable()).stop();
        } else {
            // Now call the callback
            resetImpl();
        }
    }

    @Override
    public void setWidth(int width) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.width = width;
        requestLayout();
    }

    @Override
    public void setHeight(int height) {
        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = height;
        requestLayout();
    }

    @Override
    public void pullToRefresh() {
        if (null != pull_to_refresh_text) {
            pull_to_refresh_text.setText(mPullLabel);
        }
        // Now call the callback
        pullToRefreshImpl();
    }

    public void resetImpl() {
        resetTextModeViews();
        resetFishIcon();
    }

    public void resetFishIcon() {//章鱼模式
        if (fish_icon != null) {
            fish_icon.clearAnimation();
            fish_icon.setImageResource(R.drawable.icon_refresh_1);
        }
    }

    private void resetTextModeViews() {//文字模式
        if (pull_to_refresh_text != null && mHeaderProgress != null && pull_to_refresh_image != null) {
            pull_to_refresh_text.clearAnimation();
            mHeaderProgress.setVisibility(View.GONE);
            pull_to_refresh_image.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void setLastUpdatedLabel(CharSequence label) {
//        setSubHeaderText(label);//fish模式没有该文案
    }

    @Override
    public void setLoadingDrawable(Drawable drawable) {
        // Set Drawable
        mUseIntrinsicAnimation = (drawable instanceof AnimationDrawable);
        if (pull_to_refresh_image != null) {
            pull_to_refresh_image.setImageDrawable(drawable);
            onLoadingDrawableSet(drawable);
        }
    }

    @Override
    public void setPullLabel(CharSequence pullLabel) {
        mPullLabel = pullLabel;
    }

    @Override
    public void setRefreshingLabel(CharSequence refreshingLabel) {
        mRefreshingLabel = refreshingLabel;
    }

    @Override
    public void setReleaseLabel(CharSequence releaseLabel) {
        mReleaseLabel = releaseLabel;
    }

    @Override
    public void setTextTypeface(Typeface tf) {
        pull_to_refresh_text.setTypeface(tf);
    }

    public int getDefaultDrawableResId() {
        return R.drawable.default_ptr_flip;
    }

    public final void hideAllViews() {
        hideTextModeViews();
        hideFishModeViews();
    }

    public final void showInvisibleViews() {
        showTextModeViews();
        showFishModeViews();
    }

    private void showFishModeViews() {
        setInvisibleViewToVisible(fish_icon);
        setInvisibleViewToVisible(refresh_tip_icon);
    }

    private void showTextModeViews() {
        setInvisibleViewToVisible(pull_to_refresh_text);
        setInvisibleViewToVisible(mHeaderProgress);
        setInvisibleViewToVisible(pull_to_refresh_image);
        setInvisibleViewToVisible(pull_to_refresh_sub_text);
    }

    private void setInvisibleViewToVisible(View view) {
        if (view != null && View.INVISIBLE == view.getVisibility()) {
            view.setVisibility(View.VISIBLE);
        }
    }

    private void hideFishModeViews() {//隐藏章鱼模式的views
        setViewInvisible(fish_icon);
        setViewInvisible(refresh_tip_icon);
    }

    private void hideTextModeViews() {//隐藏文字模式的views
        setViewInvisible(pull_to_refresh_text);
        setViewInvisible(mHeaderProgress);
        setViewInvisible(pull_to_refresh_image);
        setViewInvisible(pull_to_refresh_sub_text);
    }

    private void setViewInvisible(View view) {
        if (view != null && View.VISIBLE == view.getVisibility()) {
            view.setVisibility(View.INVISIBLE);
        }
    }

    public final void releaseToRefresh() {
        if (null != pull_to_refresh_text) {
            pull_to_refresh_text.setText(mReleaseLabel);
        }
        // Now call the callback
        releaseToRefreshImpl();
    }

    public void releaseToRefreshImpl() {
        if (pull_to_refresh_image != null){
            pull_to_refresh_image.startAnimation(mRotateAnimation);
        }
    }

    public void pullToRefreshImpl() {
        // Only start reset Animation, we've previously show the rotate anim
        if (pull_to_refresh_image != null && mRotateAnimation == pull_to_refresh_image.getAnimation()) {
            pull_to_refresh_image.startAnimation(mResetRotateAnimation);
        }
    }

    @Override
    public void refreshingImpl() {
        textModeViewRefreshing();
        startFishLoadingAnimation();
    }

    public void startFishLoadingAnimation() {
        if (fish_icon != null) {
            fish_icon.clearAnimation();
            fish_icon.setImageResource(R.anim.fish_loading);
            AnimationDrawable drawable = (AnimationDrawable) fish_icon.getDrawable();
            if (drawable != null) {
                drawable.start();
            }
        }
    }

    private void textModeViewRefreshing() {
        if (pull_to_refresh_image != null) {
            pull_to_refresh_image.clearAnimation();
            pull_to_refresh_image.setVisibility(View.INVISIBLE);
        }
        if (mHeaderProgress != null) {
            mHeaderProgress.setVisibility(View.VISIBLE);
        }
    }

    public void onLoadingDrawableSet(Drawable imageDrawable) {
        if (null != imageDrawable) {
            final int dHeight = imageDrawable.getIntrinsicHeight();
            final int dWidth = imageDrawable.getIntrinsicWidth();

            /**
             * We need to set the width/height of the ImageView so that it is
             * square with each side the size of the largest drawable dimension.
             * This is so that it doesn't clip when rotated.
             */
            ViewGroup.LayoutParams lp = pull_to_refresh_image.getLayoutParams();
            lp.width = lp.height = Math.max(dHeight, dWidth);
            pull_to_refresh_image.requestLayout();

            /**
             * We now rotate the Drawable so that is at the correct rotation,
             * and is centered.
             */
            pull_to_refresh_image.setScaleType(ImageView.ScaleType.MATRIX);
            Matrix matrix = new Matrix();
            matrix.postTranslate((lp.width - dWidth) / 2f, (lp.height - dHeight) / 2f);
            matrix.postRotate(getDrawableRotationAngle(), lp.width / 2f, lp.height / 2f);
            pull_to_refresh_image.setImageMatrix(matrix);
        }
    }

    @Override
    public void onPullImpl(float scaleOfLayout) {
        //do nothing
    }

    private float getDrawableRotationAngle() {
        float angle = 0f;
        switch (mMode) {
            case PULL_FROM_END:
                if (mScrollDirection == PullToRefreshBase.Orientation.HORIZONTAL) {
                    angle = 90f;
                } else {
                    angle = 180f;
                }
                break;
            case PULL_FROM_START:
                if (mScrollDirection == PullToRefreshBase.Orientation.HORIZONTAL) {
                    angle = 270f;
                }
                break;

            default:
                break;
        }
        return angle;
    }
}
