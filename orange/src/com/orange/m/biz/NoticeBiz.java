package com.orange.m.biz;

import android.text.TextUtils;
import com.orange.m.net.BaseModel;
import com.orange.m.net.BaseModelList;
import com.orange.m.net.BaseRequest;
import com.orange.m.net.BoolModel;
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
        public static final String TEMP = "temp";
        public static final String NORMAL = "normal";
    }

    public static class Notice extends BaseModel {
        public String id;
        public String content;
        public String longitude;
        public String latitude;
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
