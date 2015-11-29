package com.juzistar.m.view.pop;

import com.juzistar.m.biz.NoticeBiz;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/11/27.
 */
public abstract class BubbleCellModel extends UITableViewCell.CellModel {
    public NoticeBiz.Notice notice;//消息
    public BubbleCellListener cellListener;

    public static interface BubbleCellListener {
        public void onMessageClick(BubbleCellModel cellModel, NoticeBiz.Notice notice1);
        public void onHeaderClick(BubbleCellModel cellModel, NoticeBiz.Notice notice1);
        public void onErrorTagClick(BubbleCellModel cellModel, NoticeBiz.Notice notice1);//发送失败标签点击
    }
}
