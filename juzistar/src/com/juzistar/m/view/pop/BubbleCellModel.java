package com.juzistar.m.view.pop;

import com.juzistar.m.biz.NoticeBiz;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/11/27.
 */
public abstract class BubbleCellModel extends UITableViewCell.CellModel {
    public static final int DEFAULT_EXPIRE_TIME = 9;

    public NoticeBiz.Notice notice;//消息
    public BubbleCellListener cellListener;

    public int expireTime;//过期时效(默认值5秒)

    public BubbleCellModel() {
        expireTime = DEFAULT_EXPIRE_TIME;
    }

    public static interface BubbleCellListener {
        public void onMessageClick(BubbleCellModel cellModel, NoticeBiz.Notice notice1);
        public void onHeaderClick(BubbleCellModel cellModel, NoticeBiz.Notice notice1);
        public void onErrorTagClick(BubbleCellModel cellModel, NoticeBiz.Notice notice1);//发送失败标签点击
    }

    public float getAlpha() {
        if (expireTime <= 0) {
            return 0.0f;
        } else {
            return expireTime * 1.0f / DEFAULT_EXPIRE_TIME;
        }
    }
}
