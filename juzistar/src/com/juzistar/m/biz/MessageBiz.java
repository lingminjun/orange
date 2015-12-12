package com.juzistar.m.biz;

import com.alibaba.fastjson.JSON;
import com.juzistar.m.net.BaseModel;
import com.juzistar.m.net.BaseModelList;
import com.juzistar.m.net.BaseRequest;
import com.ssn.framework.foundation.HTTPAccessor;
import com.ssn.framework.foundation.RPC;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lingminjun on 15/10/25.
 */
public final class MessageBiz {

    public static class Message extends BaseModel {
        public String id;

        public String content;
        public long toUserId;

        public long fromUserId;
        public String fromName;

        public long timestamp;

        public String longitude;
        public String latitude;

        public static Message messageFromJSON(String json) {
            return  (Message) com.alibaba.fastjson.JSON.parseObject(json, Message.class);
        }
        public static String messageToJSON(Message msg) {
            return JSON.toJSONString(msg);
        }
    }

    public static class MessageList extends BaseModelList<Message> {
        public long latestTime;

        @Override
        public boolean fillFromJSON(JSONObject object) {

            if (object != null) {
                try {
                    latestTime = object.getLong("latestTime");

                    JSONArray array = object.getJSONArray("messages");
                    size = array.length();
                    total = size;
                    page = 0;

                    List<Message> list = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject obj = array.getJSONObject(i);

                        Message notice = new Message();
                        boolean fill = notice.fillFromJSON(obj);

                        if (!fill) {//先用fastjson处理
                            notice = (Message) com.alibaba.fastjson.JSON.parseObject(obj.toString(), Message.class);
                        }

                        list.add(notice);
                    }

                    this.list = list;
                } catch (Throwable e) {}

            }
            return true;
        }
    }

    /*
    private String content;
	private Long toUserId;
	*/

    public static RPC.Cancelable send(final String content, final long toId, final float longitude,final float latitude, final RPC.Response<Message> response){

        BaseRequest<Message> request = new BaseRequest<Message>() {
            @Override
            public String path() {
                return "message";
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
                params.put("content",content);
                params.put("toUserId",Long.toString(toId));
                params.put("longitude",Float.toString(longitude));
                params.put("latitude",Float.toString(latitude));
            }
        };

        return RPC.call(request,response);
    }


    public static RPC.Cancelable fetchMessage(final long from, final RPC.Response<MessageList > response){

        BaseRequest<MessageList> request = new BaseRequest<MessageList>() {
            @Override
            public String path() {
                return "message/list";
            }

            @Override
            public AUTH_LEVEL authLevel() {
                return AUTH_LEVEL.TOKEN;
            }

            @Override
            public HTTPAccessor.REST_METHOD method() {
                return HTTPAccessor.REST_METHOD.GET;
            }

            @Override
            public void params(HashMap<String, Object> params) {
                params.put("from",Long.toString(from));
            }
        };

        return RPC.call(request,response);
    }
}
