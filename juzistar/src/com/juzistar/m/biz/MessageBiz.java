package com.juzistar.m.biz;

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
        String id;

    	String content;
    	Long toUserId;
    	
    	Long receiverId;
    	Long senderId;
    	Long timestamp;
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

    public static RPC.Cancelable send(final String content, final long toId, final RPC.Response<Message> response){

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
