package com.orange.m.biz;

import com.orange.m.net.BaseModel;
import com.orange.m.net.BaseModelList;
import com.orange.m.net.BaseRequest;
import com.ssn.framework.foundation.RPC;

import java.util.HashMap;

/**
 * Created by lingminjun on 15/10/25.
 */
public final class NoticeBiz {

    public static class Notice extends BaseModel {
        public String content;
        public String longitude;
        public String latitude;
        public String type;

        public long creatorId;
        public String creator;
    }

    public static RPC.Cancelable create(final Notice notice, final RPC.Response<Notice> response){

        BaseRequest<Notice> request = new BaseRequest<Notice>() {
            @Override
            public String path() {
                return "create";
            }

            @Override
            public AUTH_LEVEL authLevel() {
                return AUTH_LEVEL.TOKEN;
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("content",notice.content);
                params.put("longitude",notice.longitude);
                params.put("latitude",notice.latitude);
                params.put("type",notice.type);

                params.put("creatorId",Long.toString(notice.creatorId));
                params.put("creator",notice.creator);
            }
        };

        return RPC.call(request,response);
    }

    public static RPC.Cancelable getList(final long from, final String longitude, final String latitude, final RPC.Response<BaseModelList<Notice> > response){

        BaseRequest<BaseModelList<Notice> > request = new BaseRequest<BaseModelList<Notice> >() {
            @Override
            public String path() {
                return "list";
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("from",from);
                params.put("longitude",longitude);
                params.put("latitude",latitude);
            }
        };

        return RPC.call(request,response);
    }
}
