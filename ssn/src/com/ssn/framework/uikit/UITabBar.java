package com.ssn.framework.uikit;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.ssn.framework.R;
import com.ssn.framework.foundation.APPLog;
import com.ssn.framework.foundation.Density;
import com.ssn.framework.foundation.Res;

import java.util.ArrayList;
import java.util.List;

/**
 * TabBar 实现
 */
public final class UITabBar extends LinearLayout {

    /**
     * tabItem
     */
    public final static class TabItem {

        public static final String BADGE_VALUE_DOT_VALUE = ".";//表示一个红点

        /**
         * 构造tab
         */
        public TabItem() {}
        public TabItem(int id,String name,int imageId) {
            this.id = id;
            this.tabName = name;
            this.tabImage = imageId;
        }

        /**
         * 提供getter 和 setter
         */
        public int tabItemID() {return id;}
        public void setTabItemID(int id) {this.id = id;}

        public String tabName() {return tabName;}
        public void setTabName(String name) {this.tabName = name;displayTabName();}

        public void setTabImage(int image) {this.tabImage = image;displayTabImage();}

        public boolean isHiddenTabName() {return hiddenTabName;}
        public void setHiddenTabName(boolean hidden) {hiddenTabName = hidden; displayTabName();}

        public void setTabNameColor(int color) {this.textColor = color;displayTabName();}

        public void setTabNameFontSize(int size) {this.textSize = size;displayTabName();}

        public String badgeValue() {return badgeValue;}
        public void setBadgeValue(String value) {this.badgeValue = value;displayBadgeValue();}

        /**
         * 私有方法
         */
        private boolean isSelected() {return isSelected;}
        private void setSelected(boolean selected) {isSelected = selected;displayTabName();displayTabImage();}

        private void setView(View view) {
            this.view = view;

            this.textView = (TextView) view.findViewById(R.id.tab_name);
            this.badgeView = (TextView) view.findViewById(R.id.tab_badge);
            this.imageView = (ImageView) view.findViewById(R.id.tab_img);

            displayTabName();
            displayTabImage();
            displayBadgeValue();
        }

        private int id;
        private String tabName;
        private int tabImage;
        private int textSize;
        private int textColor;
        private String badgeValue;
        private boolean hiddenTabName;

        private boolean isSelected;

        private View view;

        /*****************私有展示方法实现*******************/
        private void displayTabName() {
            if (textView != null) {
                if (hiddenTabName) {
                    textView.setVisibility(GONE);
                }
                else {
                    textView.setVisibility(VISIBLE);
                    textView.setText(tabName);

                    //颜色设置
                    if (textColor > 0) {
                        textView.setTextColor(getColor(isSelected, textColor));
                    }

                    //字体大小
                    if (textSize > 0) {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                    }
                }
            }
        }
        private TextView textView;

        private void displayBadgeValue() {
            if (badgeView == null) { return;}

            if (TextUtils.isEmpty(badgeValue) || badgeValue.equals("0")) {
                badgeView.setVisibility(GONE);
                return;
            }

            badgeView.setVisibility(VISIBLE);

            if (badgeValue.equals(BADGE_VALUE_DOT_VALUE)) {
                setBadgeRedDot(badgeView);
            }
            else {
                setBadgeContent(badgeView,badgeValue);
            }
        }
        private TextView badgeView;

        private void displayTabImage() {
            if (imageView != null && tabImage > 0) {
                imageView.setImageDrawable(getDrawable(isSelected, tabImage));
            }
        }
        private ImageView imageView;

        private static void setBadgeRedDot(TextView tv) {
            tv.setBackgroundResource(R.drawable.white_stroke_red_bg);
            ViewGroup.LayoutParams params = tv.getLayoutParams();
            tv.setText("");
            int size = Density.dipTopx(10);
            params.height = size;
            params.width = size;
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ((MarginLayoutParams) params).topMargin = 0;
            }
        }

        private static void setBadgeContent(TextView tv,String content){
            tv.setBackgroundResource(R.drawable.white_stroke_red_bg);
            ViewGroup.LayoutParams params = tv.getLayoutParams();
            tv.setText(content);
            params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ((MarginLayoutParams) params).topMargin = Density.dipTopx(-4);
            }
        }

        private static Drawable getDrawable(boolean isSelected, int imageID) {
            StateListDrawable listDrawable;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
                listDrawable = (StateListDrawable) Res.resources().getDrawable(imageID, null);
            } else {
                listDrawable = (StateListDrawable) Res.resources().getDrawable(imageID);
            }

            int[] attr = isSelected ? new int[]{android.R.attr.state_checked} : new int[]{};
            listDrawable.setState(attr);
            return listDrawable.getCurrent();
        }

        private static int getColor(boolean isSelected, int colorId) {
            ColorStateList list = Res.resources().getColorStateList(colorId);
            int defaultColor = list.getDefaultColor();
            int color = defaultColor;
            if (isSelected) {
                color = list.getColorForState(new int[]{android.R.attr.state_checked}, defaultColor);
            }
            return color;
        }
    }

    private LinearLayout _container;

    private int selectedIndex = -1;
    private OnTabSelectedListener selectedListener;

    private List<TabItem> _items = new ArrayList<TabItem>();

    public UITabBar(Context context) {
        super(context);
        init();
    }

    public UITabBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public UITabBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public UITabBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public void setTabSelectedListener(OnTabSelectedListener listener) {
        this.selectedListener = listener;
    }

    private void init() {
        this.setOrientation(LinearLayout.VERTICAL);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.ssn_tab, this);

        _container = (LinearLayout)findViewById(R.id.tab_container);
    }

    /**
     * 外部设置items
     * @param items
     */
    public void setTabItems(List<TabItem> items) {
        //清除原来的
        this._container.removeAllViews();
        selectedIndex = -1;
        _items.clear();

        if (items == null) {return;}
        LayoutInflater inflater = LayoutInflater.from(getContext());
        int size = items.size();

        for (int i = 0; i < size; i++) {
            TabItem item = items.get(i);

            item.setView(inflater.inflate(R.layout.ssn_tab_item, null));
            _items.add(item);

            this._container.addView(item.view);

            LinearLayout.LayoutParams params = (LayoutParams) item.view.getLayoutParams();
            params.weight = 1;

            final int index = i;
            item.view.setOnClickListener(ViewEvent.click(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkIndex(index);
                }
            }));
        }

        if (items.size() > 0) {
            selectedIndex = 0;
            _items.get(selectedIndex).setSelected(true);
        }
    }

    public void checkIndex(int index) {
        if (index < 0 || index >= _items.size()) {return;}

        if (selectedIndex == index) {
            return;
        }

        TabItem old_item = _items.get(selectedIndex);
        old_item.setSelected(false);

        TabItem item = _items.get(index);
        item.setSelected(true);

        selectedIndex = index;

        if (selectedListener != null) {
            try {
                selectedListener.onSelected(this,index);
            } catch (Throwable e) {
                APPLog.error(e);
            }
        }
    }

    public TabItem selectedTabItem() {
        if (selectedIndex >= 0 && selectedIndex < _items.size()) {
            return _items.get(selectedIndex);
        }
        return null;
    }

    public TabItem tabItemAtIndex(int index) {
        if (index >= 0 && index < _items.size()) {
            return _items.get(index);
        }
        return null;
    }

    public interface OnTabSelectedListener {
        public void onSelected(UITabBar tabbar,int index);
    }
}
