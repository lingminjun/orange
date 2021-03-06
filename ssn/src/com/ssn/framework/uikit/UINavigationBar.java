package com.ssn.framework.uikit;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

            public static View createButtonItemView(Context context) {
                LayoutInflater inflater = LayoutInflater.from(Res.context());
                View view = inflater.inflate(R.layout.ssn_bar_item,null);
                return view;
            }

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
            public void setImage(int imageId) {this.imageID = imageId;display();}
            public void setCustomView(View view) {this.customView = view;display();}
            public void setTitleColor(ColorStateList color) {this.textColor = color;display();}
            public void setTitleFontSize(int size) {this.textSize = size;display();}
            public boolean isHidden() {return hidden;}
            public void setHidden(boolean hidden) {this.hidden = hidden; display();}
            public void setOnClick(View.OnClickListener listener) {this.listener = listener;eventCheck();}

            /**
             * 不对外暴露方法
             */
            private void setView(View view) {
                this.view = view;

                this.textView = (TextView) view.findViewById(R.id.ssn_item_name);
                this.container = (LinearLayout) view.findViewById(R.id.ssn_item_custom);
                this.imageView = (ImageView) view.findViewById(R.id.ssn_item_img);

                display();
                eventCheck();

                if (loader != null) {
                    loader.onLoader(this,view);
                }
            }

            private void display() {
                if (view == null) {
                    setView(createButtonItemView(Res.context()));
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
                        if (textColor != null) {
                            textView.setTextColor(textColor);
                        }

                        //字体大小
                        if (textSize > 0) {
                            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                        }
                    }
                }

                //设置图片
                if (imageView != null) {
                    if (imageID != 0) {
                        imageView.setVisibility(VISIBLE);
                        imageView.setImageResource(imageID);
//                        imageView.setImageDrawable(Res.resources().getDrawable(imageID));
                    }
                    else {
                        imageView.setVisibility(GONE);
                    }
                }

                //设置自定义View
                if (customView != null) {
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
                        view.setOnClickListener(UIEvent.click(listener));
                    }
                    else {
                        view.setOnClickListener(null);
                    }
                }
            }

            private String title;
            private ColorStateList textColor;
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

        public void setTitleImage(int image) {this.image = image;display();}
        public void setTitleClick(View.OnClickListener click) {this.click = click; titleEventCheck();}

        public ButtonItem backItem() {
            if (backItem == null) {
                backItem = ButtonItem.BackButtonItem();
                displayBackButton();
            }
            return backItem;
        }

        public ButtonItem rightItem() {
            if (rightItem == null) {
                rightItem = new ButtonItem();
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

            this.titlePanel = (ViewGroup)view.findViewById(R.id.title_panel);
            this.titleText = (TextView) view.findViewById(R.id.title_text);
            this.titleIcon = (TextView) view.findViewById(R.id.title_icon);
            this.line = view.findViewById(R.id.bottom_line);
            this.leftContainer = (LinearLayout) view.findViewById(R.id.left_layout);
            this.rightContainer = (LinearLayout) view.findViewById(R.id.right_layout);

            display();
            displayTitle();
            displayBackButton();
            displayRightButtons();
        }

        /**
         * 移除显示的导航view
         */
        private void popView() {
            this.view = null;
            this.titleText = null;
            this.line = null;
            this.leftContainer = null;
            this.rightContainer = null;
        }

        private String title;
        private int image;
        private OnClickListener click;
        private int textSize;
        private int textColor;
        private int backgroundColor;
        private boolean hidden;
        private boolean hiddenBottomLine;

        private ButtonItem backItem;
        private ButtonItem rightItem;
        private List<ButtonItem> rigthItems = new ArrayList<ButtonItem>();

        private View view;
        private ViewGroup titlePanel;
        private TextView titleText;
        private TextView titleIcon;
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
                    if (backgroundColor != 0) {
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

            if (titleText != null) {
                titleText.setText(TR.string(title));

                //颜色设置
                if (textColor != 0) {
                    titleText.setTextColor(textColor);
                }

                //字体大小
                if (textSize > 0) {
                    titleText.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
                }
            }

            if (image != 0) {
                titleIcon.setVisibility(VISIBLE);
                titleIcon.setBackgroundResource(image);
            } else {
                titleIcon.setVisibility(GONE);
                titleIcon.setBackgroundResource(android.R.color.transparent);
            }
        }

        private void titleEventCheck() {
            if (view != null) {
                if (titlePanel != null) {
                    if (click != null) {
                        titlePanel.setOnClickListener(UIEvent.click(click));
                    } else {
                        titlePanel.setOnClickListener(null);
                    }
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
//            if (view == null || leftContainer == null) {return;}

            if (leftContainer != null) {
                leftContainer.removeAllViews();
            }
            if (backItem == null) {return;}
            if (backItem.view != null && leftContainer != null) {
                backItem.loader = null;
                leftContainer.addView(backItem.view,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT));
            }
            else {
                backItem.loader = this;
            }
        }

        private void displayRightButtons() {
//            if (view == null || rightContainer == null) {return;}

            if (rightContainer != null) {
                rightContainer.removeAllViews();
            }

            //需要反过来加
            int size = rigthItems.size();
            if (size > 0) {
                for (int i = size -1; i >= 0; i++) {
                    ButtonItem item = rigthItems.get(i);
                    if (item.view != null && rightContainer != null) {
                        item.loader = null;
                        rightContainer.addView(item.view,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT));
                    }
                    else {
                        item.loader = this;
                    }
                }
            }

            //最后加入右边按钮
            if (rightItem == null) {return;}
            if (rightItem.view != null && rightContainer != null) {
                rightItem.loader = null;
                rightContainer.addView(rightItem.view,new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.MATCH_PARENT));
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
        this.setId(R.id.ssn_navigation_bar);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        inflater.inflate(R.layout.ssn_navigation_bar, this);
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

        NavigationItem tem = stack.lastElement();
        while (tem != null && item != tem) {
            stack.pop();
            tem = stack.lastElement();
        }

        if (item != null) {
            item.pushView(this);
        }
    }
}
