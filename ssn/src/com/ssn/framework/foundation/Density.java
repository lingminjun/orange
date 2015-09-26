package com.ssn.framework.foundation;

import android.graphics.Point;
import android.view.Display;
import android.view.WindowManager;

/**
 * Created by lingminjun on 15/7/8.
 */
public class Density {

    private static Point point;

    /**
     * 屏幕宽度像素
     *
     * @return
     */
    public static int screenWidthpx() {
        if (point != null && point.x > 0) {
            return point.x;
        }
        synchronized (Density.class) {
            if (point != null) {
                return point.x;
            }
            WindowManager a = (WindowManager) Res.context().getSystemService(Res.context().WINDOW_SERVICE);
            Display d1 = a.getDefaultDisplay(); // 获取屏幕宽、高用
            point = new Point();
            d1.getSize(point);
            return point.x;
        }
    }

    /**
     * 屏幕高度像素
     *
     * @return
     */
    public static int screenHeightpx() {
        if (point != null && point.y > 0) {
            return point.y;
        }
        synchronized (Density.class) {
            if (point != null) {
                return point.y;
            }
            WindowManager a = (WindowManager) Res.context().getSystemService(Res.context().WINDOW_SERVICE);
            Display d1 = a.getDefaultDisplay(); // 获取屏幕宽、高用
            point = new Point();
            d1.getSize(point);
            return point.y;
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dipTopx(float dpValue) {
        final float scale = Res.resources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
     */
    public static int pxTodip(float pxValue) {
        final float scale = Res.resources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }

}
