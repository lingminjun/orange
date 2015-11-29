package com.juzistar.m.biz;

import com.juzistar.m.view.com.Keyboard;

/**
 * Created by lingminjun on 15/11/27.
 */
public final class Convert {
    public static int noticeCategory(int tag) {
        switch (tag) {
            case Keyboard.KEY.LOVE:return NoticeBiz.NoticeCategory.LOVE;
            case Keyboard.KEY.BOOK:return NoticeBiz.NoticeCategory.BOOK;
            case Keyboard.KEY.EAT:return NoticeBiz.NoticeCategory.EAT;
            case Keyboard.KEY.TEST:return NoticeBiz.NoticeCategory.TEST;
            case Keyboard.KEY.HOME:return NoticeBiz.NoticeCategory.HOME;
            case Keyboard.KEY.FLOWER:return NoticeBiz.NoticeCategory.FLOWER;
            case Keyboard.KEY.HELP:return NoticeBiz.NoticeCategory.HELP;
            case Keyboard.KEY.DATING:return NoticeBiz.NoticeCategory.DATING;
            case Keyboard.KEY.CAR:return NoticeBiz.NoticeCategory.CAR;
            case Keyboard.KEY.WEAR:return NoticeBiz.NoticeCategory.WEAR;
            case Keyboard.KEY.SPORT:return NoticeBiz.NoticeCategory.SPORT;
            case Keyboard.KEY.MOVIE:return NoticeBiz.NoticeCategory.MOVIE;

            default:return NoticeBiz.NoticeCategory.NAN;
        }
    }
}
