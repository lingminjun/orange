package com.juzistar.m.view.pop;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.juzistar.m.R;
import com.juzistar.m.biz.NoticeBiz;
import com.juzistar.m.view.com.UIDic;
import com.ssn.framework.foundation.TR;
import com.ssn.framework.uikit.UIDisplayLink;
import com.ssn.framework.uikit.UIEvent;
import com.ssn.framework.uikit.UITableViewCell;

/**
 * Created by lingminjun on 15/4/24.
 */
public abstract class BubbleCell extends UITableViewCell {

    private String UIDisplayLinkKey;
    public BubbleCell(Context context) {
        super(context);
        UIDisplayLinkKey = "" + context.hashCode() + "." + this.hashCode();
    }

    //刷新ui必须采用高频刷新
    UIDisplayLink.Listener timerListener = new UIDisplayLink.Listener() {
        @Override
        public void fire(String flag) {

            BubbleCellModel model = (BubbleCellModel)cellModel();
            if (model == null || model.animationDuration <= 0 || !model.autoDisappear) {
                UIDisplayLink.shareInstance().removeListener(UIDisplayLinkKey);

                if (model != null && model.cellListener != null && model.autoDisappear) {
                    model.cellListener.onDisappear(model,model.notice);
                }

                return;
            }

            //先修改数据
            model.animationDuration = model.animationDuration - BubbleCellModel.DEFAULT_ONCE_DURATION_TIME;

            //改变alpha值
            View view = displayView();
            if (view != null) {
                view.setAlpha(model.getAlpha());
            }
        }
    };

    @Override
    protected void onPrepareForReuse() {
        UIDisplayLink.shareInstance().removeListener(UIDisplayLinkKey);
    }

    @Override
    protected void onDisplay(CellModel cellModel, int row) {
        super.onDisplay(cellModel, row);

        BubbleCellModel model = (BubbleCellModel)cellModel;

        if (model.notice == null) {return;}

        //透明度设置
        View view = displayView();
        if (view != null) {
            if (model.autoDisappear) {
                UIDisplayLink.shareInstance().addListener(timerListener,UIDisplayLinkKey);
                view.setAlpha(model.getAlpha());
            } else {
                view.setAlpha(1.0f);
            }
        }
    }
}
