package com.ssn.framework.uikit;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.ssn.framework.R;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.foundation.TR;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Created by lingminjun on 15/9/24.
 */
public final class UINavigationBar extends RelativeLayout {

    /**
     * view加载回调
     */
    private interface ButtonItemLoader {
        public void onLoader(NavigationItem.ButtonItem item,View view);
    }

    /**
     * Navigation Item
     */
    public final static class NavigationItem implements ButtonItemLoader {

        /**
         * Navigation Button Item
         */
        public final static class ButtonItem {

            public static ButtonItem BackButtonItem() {
                return new ButtonItem(R.drawable.ssn_back_icon);
            }

            public ButtonItem() {}
            public ButtonItem(String title){
                this.title = title;
            }
            public ButtonItem(int imageID){
                this.imageID = imageID;
            }
            public ButtonItem(View customView) {
                this.customView = customView;
            }

            /**
             * 对外保留getter setter
             */
            public String title() {return title;}
            public void setTitle(String title) {this.title = title; display();}
            public void setImage(int image) {this.imageID = image;display();}
            public void setCustomView(View view) {this.customView = view;display();}
            public void setTitleColor(int color) {this.textColor = color;display();}
            public void setTitleFontSize(int size) {this.textSize = size;display();}
            public boolean isHidden() {return hidden;}
            public void setHidden(boolean hidden) {this.hidden = hidden; display();}
            public void setOnClick(View.OnClickListener listener) {this.listener = listener;eventCheck();}

            /**
             * 不对外暴露方法
             */
            protected void setView(View view) {
                this.view = view;

                this.textView = (TextView) view.findViewById(R.id.item_name);
                this.container = (LinearLayout) view.findViewById(R.id.item_custom);
                this.imageView = (ImageView) view.findViewById(R.id.item_img);

                display();
                eventCheck();

                if (loader != null) {
                    loader.onLoader(this,view);
                }
            }

            private void display() {
                if (view == null) {
                    return;
                }

                if (hidden) {
                    view.setVisibility(GONE);
                    return;
                }
                view.setVisibility(VISIBLE);

                if (textView != null) {
                    if (TextUtils.isEmpty(title)) {
                        textView.setVisibility(GONE);
                    } else {
                        textView.setVisibility(VISIBLE);
                        textView.setText(title);

                        //颜色设置
                        if (textColor > 0) {
                            textView.setTextColor(Res.resources().getColorStateList(textColor));
                        }

                        //字体大小
                        if (textSize > 0) {
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                        }
                    }
                }

                //设置图片
                if (imageView != null) {
                    if (imageID > 0) {
                        imageView.setVisibility(VISIBLE);
                        imageView.setImageDrawable(Res.resources().getDrawable(imageID));
                    }
                    else {
                        imageView.setVisibility(GONE);
                    }
                }

                //设置自定义View
                if (container != null) {
                    container.removeAllViews();
                    if (container != null) {
                        container.setVisibility(VISIBLE);
                        container.addView(customView);
                    }
                    else {
                        container.setVisibility(GONE);
                    }
                }
            }

            private void eventCheck() {
                if (view != null) {
                    if (listener != null) {
                        view.setOnClickListener(ViewEvent.click(listener));
                    }
                    else {
                        view.setOnClickListener(null);
                    }
                }
            }

            private String title;
            private int textColor;
            private int textSize;
            private int imageID;
            private View customView;
            private boolean hidden;
            private View.OnClickListener listener;
            private ButtonItemLoader loader;

            private View view;
            private ImageView imageView;
            private TextView textView;
            private LinearLayout container;
        }


        /**
         * 构造tab
         */
        public NavigationItem() {}
        public NavigationItem(String title) {
            this.title = title;
        }

        /**
         * 提供getter 和 setter
         */
        public String title() {return title;}
        public void setTitle(String title) {this.title = title;displayTitle();}
        public void setTitleColor(int color) {this.textColor = color;displayTitle();}
        public void setTitleFontSize(int size) {this.textSize = size;displayTitle();}

        public ButtonItem backItem() {
            if (backItem == null) {
                backItem = ButtonItem.BackButtonItem();
                displayBackButton();
            }
            return backItem;
        }

        public ButtonItem rightItem() {
            if (rightItem == null) {
                rightItem = ButtonItem.BackButtonItem();
                displayRightButtons();
            }
            return rightItem;
        }

        public void addRightButtonItem(ButtonItem item) {
            if (item != null) {
                rigthItems.add(item);
                displayRightButtons();
            }
        }

        /**
         * 所有右边按钮，包括right item
         */
        public List<ButtonItem> rightButtonItems() {
            List<ButtonItem> items = new ArrayList<ButtonItem>();
            if (rigthItems != null) {
                items.add(rightItem);
            }

            items.addAll(rigthItems);
            return items;
        }

        public boolean isHidden() {return hidden;}
        public void setHidden(boolean hidden) {this.hidden = hidden; display();}
        public void setBottomLineHidden(boolean hidden) {this.hiddenBottomLine = hidden; display();}
        public void setBackgroundColor(int color) {this.backgroundColor = color;display();}

        /**
         * 设置其显示的导航view
         * @param view
         */
        private void pushView(UINavigationBar view) {
            this.view = view;

            this.textView = (TextView) view.findViewById(R.id.title_text);
            this.line = view.findViewById(R.id.bottom_line);
            this.leftContainer = (LinearLayout) view.findViewById(R.id.left_layout);
            this.rightContainer = (LinearLayout) view.findViewById(R.id.right_layout);

            displayTitle();
            displayBackButton();
            displayRightButtons();
        }

        /**
         * 移除显示的导航view
         */
        private void popView() {
            this.view = null;
            this.textView = null;
            this.line = null;
            this.leftContainer = null;
            this.rightContainer = null;
        }

        private String title;
        private int textSize;
        private int textColor;
        private int backgroundColor;
        private boolean hidden;
        private boolean hiddenBottomLine;

        private ButtonItem backItem;
        private ButtonItem rightItem;
        private List<ButtonItem> rigthItems = new ArrayList<ButtonItem>();

        private View view;
        private TextView textView;
        private LinearLayout leftContainer;
        private LinearLayout rightContainer;
        private View line;

        /*****************私有展示方法实现*******************/
        private void display() {
            if (view != null) {
                if (hidden) {view.setVisibility(GONE);}
                else {
                    view.setVisibility(VISIBLE);

                    //颜色设置
                    if (backgroundColor > 0) {
                        view.setBackgroundColor(backgroundColor);
                    }

                    if (line != null) {
                        if (hiddenBottomLine) {
                            line.setVisibility(GONE);
                        }
                        else {
                            line.setVisibility(VISIBLE);
                        }
                    }
                }
            }
        }

        private void displayTitle() {
            if (view == null) {return;}

            if (textView != null) {
                textView.setText(TR.string(title));

                //颜色设置
                if (textColor > 0) {
                    textView.setTextColor(Res.color(textColor));
                }

                //字体大小
                if (textSize > 0) {
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                }
            }
        }

        public void onLoader(ButtonItem item,View view) {
            if (item == backItem) {
                displayBackButton();
            }
            else {
                displayRightButtons();
            }
        }

        private void displayBackButton() {
            if (view == null || leftContainer == null) {return;}

            leftContainer.removeAllViews();
            if (backItem.view != null) {
                backItem.loader = null;
                leftContainer.addView(backItem.view);
            }
            else {
                backItem.loader = this;
            }
        }

        private void displayRightButtons() {
            if (view == null || rightContainer == null) {return;}

            rightContainer.removeAllViews();

            //需要反过来加
            int size = rigthItems.size();
            if (size > 0) {
                for (int i = size -1; i >= 0; i++) {
                    ButtonItem item = rigthItems.get(i);
                    if (item.view != null) {
                        item.loader = null;
                        rightContainer.addView(item.view);
                    }
                    else {
                        item.loader = this;
                    }
                }
            }

            //最后加入右边按钮
            if (rightItem.view != null) {
                rightItem.loader = null;
                rightContainer.addView(rightItem.view);
            }
            else {
                rightItem.loader = this;
            }
        }
    }

    public UINavigationBar(Context context) {
        super(context);
        init();
    }

    public UINavigationBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public UINavigationBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(21)
    public UINavigationBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }


    private void init() {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.ssn_navigation_bar, this);

//        _container = (LinearLayout)findViewById(R.id.view_container);
    }

    private Stack<NavigationItem> stack = new Stack<NavigationItem>();

    public NavigationItem topNavigationItem() {
        if (stack.size() <=  0) {return null;}
        return stack.lastElement();
    }

    /**
     * 压入一个新的导航
     * @param item
     */
    public void pushItem(NavigationItem item) {
        if (item == null) {return;}
        if (stack.contains(item)) {
            Log.e("Navigation","不能重复push一个导航到栈中");
            return;
        }

        NavigationItem old = topNavigationItem();
        stack.push(item);

        if (old != null) {
            old.popView();
        }
        item.pushView(this);
    }

    /**
     * 重置栈方式
     * @param root
     */
    public void resetItemStack(NavigationItem root) {
        if (root == null) {return;}

        NavigationItem old = topNavigationItem();
        stack.clear();
        stack.push(root);

        if (old == root) {
            return;
        }

        if (old != null) {
            old.popView();
        }

        root.pushView(this);
    }

    /**
     * 推出一个栈
     */
    public void pop() {
        if (stack.size() <=  0) {return;}

        NavigationItem old = topNavigationItem();
        stack.pop();
        old.popView();
        NavigationItem item = topNavigationItem();
        if (item != null) {
            item.pushView(this);
        }
    }


    /**
     * 推出到指定栈
     * @param item
     */
    public void popToItem(NavigationItem item) {
        if (item == null) {return;}

        if (stack.size() <=  0) {return;}

        if (!stack.contains(item)) {return;}

        NavigationItem old = topNavigationItem();
        if (item == old) {return;}
        old.popView();

        NavigationItem tem;
        do {
            stack.pop();
            tem = stack.lastElement();
        } while (tem != null && item != tem);

        if (item != null) {
            item.pushView(this);
        }
    }
}
