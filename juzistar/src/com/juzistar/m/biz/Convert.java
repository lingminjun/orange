package com.juzistar.m.biz;

import com.juzistar.m.view.com.Keyboard;

/**
 * Created by lingminjun on 15/11/27.
 */
public final class Convert {
    public static String noticeType(int tag) {
        switch (tag) {
            case Keyboard.KEY.LOVE:return NoticeBiz.NoticeType.LOVE;
            case Keyboard.KEY.BOOK:return NoticeBiz.NoticeType.BOOK;
            case Keyboard.KEY.EAT:return NoticeBiz.NoticeType.EAT;
            case Keyboard.KEY.TEST:return NoticeBiz.NoticeType.TEST;
            case Keyboard.KEY.HOME:return NoticeBiz.NoticeType.HOME;
            case Keyboard.KEY.FLOWER:return NoticeBiz.NoticeType.FLOWER;
            case Keyboard.KEY.HELP:return NoticeBiz.NoticeType.HELP;
            case Keyboard.KEY.DATING:return NoticeBiz.NoticeType.DATING;
            case Keyboard.KEY.CAR:return NoticeBiz.NoticeType.CAR;
            case Keyboard.KEY.WEAR:return NoticeBiz.NoticeType.WEAR;
            case Keyboard.KEY.SPORT:return NoticeBiz.NoticeType.SPORT;
            case Keyboard.KEY.MOVIE:return NoticeBiz.NoticeType.MOVIE;

            default:return NoticeBiz.NoticeType.TEMP;
        }
    }
}
