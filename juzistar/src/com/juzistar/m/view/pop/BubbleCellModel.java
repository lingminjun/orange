package com.juzistar.m.view.pop;

import com.juzistar.m.biz.NoticeBiz;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/11/27.
 */
public abstract class BubbleCellModel extends UITableViewCell.CellModel {
    public static final int DEFAULT_EXPIRE_TIME = 9;
    public static final int DEFAULT_DURATION_TIME = 5000;
    public static final int DEFAULT_ONCE_DURATION_TIME = 30;

    public NoticeBiz.Notice notice;//消息
    public BubbleCellListener cellListener;

    public int expireTime;//过期时效(默认值5秒)
    public int animationDuration;//毫秒

    public BubbleCellModel() {
        expireTime = DEFAULT_EXPIRE_TIME;
        animationDuration = DEFAULT_DURATION_TIME;
    }

    public static interface BubbleCellListener {
        public void onMessageClick(BubbleCellModel cellModel, NoticeBiz.Notice notice1);
        public void onHeaderClick(BubbleCellModel cellModel, NoticeBiz.Notice notice1);
        public void onErrorTagClick(BubbleCellModel cellModel, NoticeBiz.Notice notice1);//发送失败标签点击
        public void onDisappear(BubbleCellModel cellModel, NoticeBiz.Notice notice1);//消失时
    }

    public float getAlpha() {
        if (expireTime <= 0 || animationDuration <= 0) {
            return 0.0f;
        } else {
            return animationDuration * 1.0f / DEFAULT_DURATION_TIME;
//            return expireTime * 1.0f / DEFAULT_EXPIRE_TIME;
        }
    }
}
