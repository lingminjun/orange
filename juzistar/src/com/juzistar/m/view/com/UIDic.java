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
     * @param noticeType
     * @return
     */
    public static int bubbleResourceId(String noticeType,boolean isSend) {
        if (TextUtils.isEmpty(noticeType)) {return 0;}

        if (NoticeBiz.NoticeType.LOVE.equals(noticeType)) {
            return isSend?R.drawable.bubble_s_red0_icon:R.drawable.bubble_r_red0_icon;
        }
        else if (NoticeBiz.NoticeType.BOOK.equals(noticeType)) {
            return isSend?R.drawable.bubble_s_blue0_icon:R.drawable.bubble_r_blue0_icon;
        }
        else if (NoticeBiz.NoticeType.EAT.equals(noticeType)) {
            return isSend?R.drawable.bubble_s_yellow0_icon:R.drawable.bubble_r_yellow0_icon;
        }
        else if (NoticeBiz.NoticeType.TEST.equals(noticeType)) {
            return isSend?R.drawable.bubble_s_blue1_icon:R.drawable.bubble_r_blue1_icon;
        }
        else if (NoticeBiz.NoticeType.HOME.equals(noticeType)) {
            return isSend?R.drawable.bubble_s_yellow1_icon:R.drawable.bubble_r_yellow1_icon;
        }
        else if (NoticeBiz.NoticeType.FLOWER.equals(noticeType)) {
            return isSend?R.drawable.bubble_s_red0_icon:R.drawable.bubble_r_red0_icon;
        }
        else if (NoticeBiz.NoticeType.HELP.equals(noticeType)) {
            return isSend?R.drawable.bubble_s_blue1_icon:R.drawable.bubble_r_blue1_icon;
        }
        else if (NoticeBiz.NoticeType.DATING.equals(noticeType)) {
            return isSend?R.drawable.bubble_s_red0_icon:R.drawable.bubble_r_red0_icon;
        }
        else if (NoticeBiz.NoticeType.CAR.equals(noticeType)) {
            return isSend?R.drawable.bubble_s_green0_icon:R.drawable.bubble_r_green0_icon;
        }
        else if (NoticeBiz.NoticeType.WEAR.equals(noticeType)) {
            return isSend?R.drawable.bubble_s_yellow1_icon:R.drawable.bubble_r_yellow1_icon;
        }
        else if (NoticeBiz.NoticeType.SPORT.equals(noticeType)) {
            return isSend?R.drawable.bubble_s_green1_icon:R.drawable.bubble_r_green1_icon;
        }
        else if (NoticeBiz.NoticeType.MOVIE.equals(noticeType)) {
            return isSend?R.drawable.bubble_s_green0_icon:R.drawable.bubble_r_green0_icon;
        }
        else {
            return 0;
        }
    }

    /**
     * 文案对应
     * @param noticeType
     * @return
     */
    public static String bubbleTagResourceId(String noticeType) {
        if (TextUtils.isEmpty(noticeType)) {return "";}

        if (NoticeBiz.NoticeType.LOVE.equals(noticeType)) {
            return Res.localized(R.string.love);
        }
        else if (NoticeBiz.NoticeType.BOOK.equals(noticeType)) {
            return Res.localized(R.string.book);
        }
        else if (NoticeBiz.NoticeType.EAT.equals(noticeType)) {
            return Res.localized(R.string.eat);
        }
        else if (NoticeBiz.NoticeType.TEST.equals(noticeType)) {
            return Res.localized(R.string.test);
        }
        else if (NoticeBiz.NoticeType.HOME.equals(noticeType)) {
            return Res.localized(R.string.home);
        }
        else if (NoticeBiz.NoticeType.FLOWER.equals(noticeType)) {
            return Res.localized(R.string.flower);
        }
        else if (NoticeBiz.NoticeType.HELP.equals(noticeType)) {
            return Res.localized(R.string.help);
        }
        else if (NoticeBiz.NoticeType.DATING.equals(noticeType)) {
            return Res.localized(R.string.dating);
        }
        else if (NoticeBiz.NoticeType.CAR.equals(noticeType)) {
            return Res.localized(R.string.car);
        }
        else if (NoticeBiz.NoticeType.WEAR.equals(noticeType)) {
            return Res.localized(R.string.wear);
        }
        else if (NoticeBiz.NoticeType.SPORT.equals(noticeType)) {
            return Res.localized(R.string.sport);
        }
        else if (NoticeBiz.NoticeType.MOVIE.equals(noticeType)) {
            return Res.localized(R.string.movie);
        }
        else {
            return "";
        }
    }
}
