package com.orange.m.biz;

import com.orange.m.net.BaseModel;
import com.orange.m.net.BaseModelList;
import com.orange.m.net.BaseRequest;
import com.ssn.framework.foundation.HTTPAccessor;
import com.ssn.framework.foundation.RPC;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by lingminjun on 15/10/25.
 */
public final class MessageBiz {

    public static class Message extends BaseModel {
    	String content;
    	Long toUserId;
    	
    	Long reeiverId;
    	Long senderId;
    	Long timestamp;
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


    public static RPC.Cancelable fetchMessage(final long from, final RPC.Response<BaseModelList<Message> > response){

        BaseRequest<BaseModelList<Message> > request = new BaseRequest<BaseModelList<Message> >() {
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
