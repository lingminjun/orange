package com.juzistar.m.biz;

import android.text.TextUtils;
import com.juzistar.m.net.BaseModel;
import com.juzistar.m.net.BaseModelList;
import com.juzistar.m.net.BaseRequest;
import com.juzistar.m.net.BoolModel;
import com.ssn.framework.foundation.HTTPAccessor;
import com.ssn.framework.foundation.RPC;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lingminjun on 15/10/25.
 */
public final class NoticeBiz {

    public static final class NoticeType {
        //type 定义
        public static final String TEMP = "temp";//30秒间隔
        public static final String NORMAL = "normal";//30分钟间隔
    }

    //类别
    public static final class NoticeCategory {
        public static final int NAN = 0;//"nan";
        public static final int LOVE = 1;//"love";
        public static final int BOOK = 2;//"book";
        public static final int EAT  = 3;//"eat";
        public static final int TEST = 4;//"test";
        public static final int HOME = 5;//"home";
        public static final int FLOWER = 6;//"flower";
        public static final int HELP = 7;//"help";
        public static final int DATING = 8;//"dating";
        public static final int CAR = 9;//"car";
        public static final int WEAR = 10;//"wear";
        public static final int SPORT = 11;//"sport";
        public static final int MOVIE = 12;//"movie";
    }

    public static class Notice extends BaseModel {
        public String id;
        public String content;
        public String longitude;
        public String latitude;
        public int category;//类型
        public String type;

        public long creatorId;
        public String creator;
    }

    public static class NoticeList extends BaseModelList<Notice> {
        public long latestTime;

        @Override
        public boolean fillFromJSON(JSONObject object) {

            if (object != null) {
                try {
                    latestTime = object.getLong("latestTime");

                    JSONArray array = object.getJSONArray("notices");
                    size = array.length();
                    total = size;
                    page = 0;

                    List<Notice> list = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);

                        Notice notice = new Notice();
                        boolean fill = notice.fillFromJSON(obj);

                        if (!fill) {//先用fastjson处理
                            notice = (Notice) com.alibaba.fastjson.JSON.parseObject(obj.toString(), Notice.class);
                        }

                        list.add(notice);
                    }

                    this.list = list;
                } catch (Throwable e) {}

            }
            return true;
        }
    }

    public static RPC.Cancelable create(final Notice notice, final RPC.Response<BoolModel> response){

        BaseRequest<BoolModel> request = new BaseRequest<BoolModel>() {
            @Override
            public String path() {
                return "notice";
            }

            @Override
            public AUTH_LEVEL authLevel() {
                return AUTH_LEVEL.TOKEN;
            }

            @Override
            public HTTPAccessor.REST_METHOD method() {
                return HTTPAccessor.REST_METHOD.POST;
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("content",notice.content);

                if (NoticeCategory.NAN != notice.category) {
                    params.put("category",notice.category);
                }

                params.put("longitude",notice.longitude);
                params.put("latitude",notice.latitude);

                //默认采用temp
                if (TextUtils.isEmpty(notice.type)) {
                    params.put("type", NoticeType.TEMP);
                } else {
                    params.put("type", notice.type);
                }

//                params.put("creatorId",Long.toString(notice.creatorId));
//                params.put("creator",notice.creator);
            }
        };

        return RPC.call(request,response);
    }

    public static RPC.Cancelable getList(final long from, final String longitude, final String latitude, final RPC.Response<NoticeList > response){

        BaseRequest<NoticeList > request = new BaseRequest<NoticeList >() {
            @Override
            public String path() {
                return "notice/list";
            }

            @Override
            public HTTPAccessor.REST_METHOD method() {
                return HTTPAccessor.REST_METHOD.GET;
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("from",Long.toString(from));
                params.put("longitude",longitude);
                params.put("latitude",latitude);
            }
        };

        return RPC.call(request,response);
    }
}
