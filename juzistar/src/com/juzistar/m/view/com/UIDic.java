package com.juzistar.m.view.com;

import android.text.TextUtils;
import com.juzistar.m.R;
import com.juzistar.m.biz.NoticeBiz;
import com.ssn.framework.foundation.Res;

/**
 * Created by lingminjun on 15/11/27.
 * ui字典
 */
public final class UIDic {

    /**
     * 获取头像 resource id
     * @param uid
     * @return
     */
    public static int avatarResourceId(long uid) {
        int idx = (int)(uid%6);
        switch (idx) {
            case 0:return R.drawable.avatar_blue_0;
            case 1:return R.drawable.avatar_green_0;
            case 2:return R.drawable.avatar_green_1;
            case 3:return R.drawable.avatar_yellow_0;
            case 4:return R.drawable.avatar_yellow_1;
            case 5:return R.drawable.avatar_red_0;
            default:return R.drawable.avatar_blue_0;
        }
    }

    /**
     * 汽包颜色
     * @param noticeCategory
     * @return
     */
    public static int bubbleResourceId(int noticeCategory,boolean isSend) {
        if (noticeCategory == NoticeBiz.NoticeCategory.NAN) {return 0;}

        if (NoticeBiz.NoticeCategory.LOVE == noticeCategory) {
            return isSend?R.drawable.bubble_s_red0_icon:R.drawable.bubble_r_red0_icon;
        }
        else if (NoticeBiz.NoticeCategory.BOOK == noticeCategory) {
            return isSend?R.drawable.bubble_s_blue0_icon:R.drawable.bubble_r_blue0_icon;
        }
        else if (NoticeBiz.NoticeCategory.EAT == noticeCategory) {
            return isSend?R.drawable.bubble_s_yellow0_icon:R.drawable.bubble_r_yellow0_icon;
        }
        else if (NoticeBiz.NoticeCategory.TEST == noticeCategory) {
            return isSend?R.drawable.bubble_s_blue1_icon:R.drawable.bubble_r_blue1_icon;
        }
        else if (NoticeBiz.NoticeCategory.HOME == noticeCategory) {
            return isSend?R.drawable.bubble_s_yellow1_icon:R.drawable.bubble_r_yellow1_icon;
        }
        else if (NoticeBiz.NoticeCategory.FLOWER == noticeCategory) {
            return isSend?R.drawable.bubble_s_red0_icon:R.drawable.bubble_r_red0_icon;
        }
        else if (NoticeBiz.NoticeCategory.HELP == noticeCategory) {
            return isSend?R.drawable.bubble_s_blue1_icon:R.drawable.bubble_r_blue1_icon;
        }
        else if (NoticeBiz.NoticeCategory.DATING == noticeCategory) {
            return isSend?R.drawable.bubble_s_red0_icon:R.drawable.bubble_r_red0_icon;
        }
        else if (NoticeBiz.NoticeCategory.CAR == noticeCategory) {
            return isSend?R.drawable.bubble_s_green0_icon:R.drawable.bubble_r_green0_icon;
        }
        else if (NoticeBiz.NoticeCategory.WEAR == noticeCategory) {
            return isSend?R.drawable.bubble_s_yellow1_icon:R.drawable.bubble_r_yellow1_icon;
        }
        else if (NoticeBiz.NoticeCategory.SPORT == noticeCategory) {
            return isSend?R.drawable.bubble_s_green1_icon:R.drawable.bubble_r_green1_icon;
        }
        else if (NoticeBiz.NoticeCategory.MOVIE == noticeCategory) {
            return isSend?R.drawable.bubble_s_green0_icon:R.drawable.bubble_r_green0_icon;
        }
        else {
            return 0;
        }
    }

    /**
     * 文案对应
     * @param noticeCategory
     * @return
     */
    public static String bubbleTagResourceId(int noticeCategory) {
        if (noticeCategory == NoticeBiz.NoticeCategory.NAN) {return "";}

        if (NoticeBiz.NoticeCategory.LOVE == noticeCategory) {
            return Res.localized(R.string.love);
        }
        else if (NoticeBiz.NoticeCategory.BOOK == noticeCategory) {
            return Res.localized(R.string.book);
        }
        else if (NoticeBiz.NoticeCategory.EAT == noticeCategory) {
            return Res.localized(R.string.eat);
        }
        else if (NoticeBiz.NoticeCategory.TEST == noticeCategory) {
            return Res.localized(R.string.test);
        }
        else if (NoticeBiz.NoticeCategory.HOME == noticeCategory) {
            return Res.localized(R.string.home);
        }
        else if (NoticeBiz.NoticeCategory.FLOWER == noticeCategory) {
            return Res.localized(R.string.flower);
        }
        else if (NoticeBiz.NoticeCategory.HELP == noticeCategory) {
            return Res.localized(R.string.help);
        }
        else if (NoticeBiz.NoticeCategory.DATING == noticeCategory) {
            return Res.localized(R.string.dating);
        }
        else if (NoticeBiz.NoticeCategory.CAR == noticeCategory) {
            return Res.localized(R.string.car);
        }
        else if (NoticeBiz.NoticeCategory.WEAR == noticeCategory) {
            return Res.localized(R.string.wear);
        }
        else if (NoticeBiz.NoticeCategory.SPORT == noticeCategory) {
            return Res.localized(R.string.sport);
        }
        else if (NoticeBiz.NoticeCategory.MOVIE == noticeCategory) {
            return Res.localized(R.string.movie);
        }
        else {
            return "";
        }
    }
}
