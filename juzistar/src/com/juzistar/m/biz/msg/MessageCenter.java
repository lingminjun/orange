package com.juzistar.m.biz.msg;

import android.content.Intent;
import android.text.TextUtils;
import com.alibaba.fastjson.JSON;
import com.juzistar.m.Utils.Utils;
import com.juzistar.m.biz.MessageBiz;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.biz.lbs.LBService;
import com.juzistar.m.constants.Constants;
import com.juzistar.m.entity.MapMarkPoint;
import com.ssn.framework.foundation.*;

import java.util.*;

/**
 * Created by lingminjun on 15/11/29.
 */
public final class MessageCenter {

    public static final String RECEIVED_MSG_NOTIFICATION = "received_msg_notification";
    public static final String MSG_KEY = "msg_key";

    private static MessageCenter _instance = null;

    /**
     * 用户中心
     * @return 唯一实例
     */
    static public MessageCenter shareInstance() {
        if (_instance != null) return _instance;
        synchronized(MessageCenter.class){
            if (_instance != null) return _instance;
            _instance = newInstance();
            return _instance;
        }
    }

    private static MessageCenter newInstance() {
        return new MessageCenter();
    }

    private static final String SESSION_STORE_KEY = "session.store.key";
    private static final int SESSION_MAX_SIZE = 500;
    private Map<String,Session> snMap = new HashMap<>();
    private List<Session> snlist = new LinkedList<>();

    /**
     * 防止构造实例
     */
    private MessageCenter() {
        super();

        //读取数据
        readSessions();
    }

    private void readSessions() {
        byte[] data = Store.documents().data(SESSION_STORE_KEY);
        //读取数据
        if (data != null) {
            try {

                String json = new String(data);
                List<Session> ss = JSON.parseArray(json,Session.class);

                if (ss != null) {
                    for (Session sn : ss) {
                        snlist.add(sn);
                        snMap.put(sn.sid,sn);
                        unreadCount += sn.unreadCount;
                    }
                }
            } catch (Throwable e) {}

        }
    }

    private void saveSessions() {
        if (snlist.size() > 0) {
            try {

                String json = JSON.toJSONString(snlist);
                byte[] data = json.getBytes();

                Store.documents().store(SESSION_STORE_KEY,data);

            } catch (Throwable e){e.printStackTrace();}
        } else {
            Store.documents().remove(SESSION_STORE_KEY);
        }
    }

    /**
     * 聊天会话
     */
    public static class Session {
        public String sid;

        public long other;
        public String otherName;

        //session 列表显示数据
        public int unreadCount;//未读数
        public String msg;//最后显示的msg

        public double lastLng;//最后一条收到消息
        public double lastLat;//最后一条收到消息

        public String lastRcvMsg;//最后一条收到消息（进入地图时展示）
        public String lastSndMsg;//最后一条发送消息（进入地图时展示）

        public List<String> unrdmsgs;//最后三条

        @Override
        public boolean equals(Object o) {
            if (o instanceof Session) {
                return other == ((Session) o).other;
            }
            return super.equals(o);
        }

        public static String composedSessionID(long from,long to) {
            if (to > from) {
                return from + ":" + to;
            } else {
                return to + ":" + from;
            }
        }

        @Override
        public int hashCode() {
            return (int)other;
        }

        public Session() {
            unrdmsgs = new ArrayList<>();
        }

        public static String toJSONString(Session session) {
            try {
                return JSON.toJSONString(session);
            } catch (Throwable e) {}
            return "";
        }

        public static Session toSession(String json) {
            try {
                return JSON.parseObject(json,Session.class);
            } catch (Throwable e) {}
            return null;
        }
    }

    public List<Session> getSessions() {
        return new ArrayList<>(snlist);
    }


    /**
     * 是否开启服务
     * @return
     */
    public boolean isOpenService() {
        return _open;
    }

    /**
     * 开启高频刷新
     * @return
     */
    public void start() {
        if (_open) {return;}

        _open = true;
        Clock.shareInstance().addListener(clock,CLOCK_KEY);
    }

    /**
     * 停止高频刷新
     */
    public void stop() {
        _open = false;
        _open = false;
        Clock.shareInstance().removeListener(CLOCK_KEY);
    }

    /**
     * 服务是否开启
     * @return 是否开启
     */
    public boolean isFrequency() {return _high;}


    /**
     * 开启高频刷新，同时启动服务
     * @return
     */
    public void startFrequency() {
        if (_high) {return;}
        _high = true;
        start();
    }

    /**
     * 停止高频刷新
     */
    public void stopFrequency() {
        _high = false;
    }

    private Clock.Listener clock = new Clock.Listener() {
        @Override
        public void fire(String flag) {
            if (!_open) {_high = false; count = 0;return;}

            count++;

            int mode;
            if (_high) {
                mode = count % HIGH_PULL_INTERVAL;
            } else {
                mode = count % NORMAL_PULL_INTERVAL;
            }

            if (mode == 0) {//触发请求
                pullMessage();
            }

            if (count >= NORMAL_PULL_INTERVAL) {
                count = 0;//循环；
            }
        }
    };

    private void pullMessage() {

        long latest_pull_at = UserDefaults.getInstance().get(LATEST_PULL_KEY,0l);

        RPC.Response<MessageBiz.MessageList> res = new RPC.Response<MessageBiz.MessageList>() {
            @Override
            public void onSuccess(MessageBiz.MessageList list) {
                super.onSuccess(list);

                //接受到数据并做去重处理
                if (list == null) {
                    return;
                }

                //存储游标
                if (list.latestTime > 0) {
                    UserDefaults.getInstance().put(LATEST_PULL_KEY, list.latestTime);
                }

                int time = 0;
                for (final MessageBiz.Message message : list.list) {

                    //不是发送给自己的先过滤掉
                    message.toUserId = UserCenter.shareInstance().UID();
//                    if () {
//                        continue;
//                    }

                    //查看session是否已经存在
                    String sid = Session.composedSessionID(message.fromUserId,UserCenter.shareInstance().UID());
                    Session session = snMap.get(sid);
                    if (session == null) {
                        session = new Session();
                        session.sid = sid;
                        session.unreadCount++;
                        unreadCount++;
                        session.other = message.fromUserId;
                        session.otherName = message.fromUserName;

                        snMap.put(sid,session);
                        snlist.add(0,session);

                        checkSessionMaxCount();
                    }

                    if(TextUtils.isEmpty(session.lastRcvMsg)) {
                        session.lastRcvMsg = message.content;
                        try {
                            session.lastLat = Double.parseDouble(message.latitude);
                        } catch (Throwable e){e.printStackTrace();}

                        try {
                            session.lastLng = Double.parseDouble(message.longitude);
                        } catch (Throwable e){e.printStackTrace();}

                        session.msg = message.content;
                    } else {
                        try {
                            String json = MessageBiz.Message.messageToJSON(message);
                            session.unrdmsgs.add(json);
                        } catch (Throwable e) {e.printStackTrace();}
                    }

                    saveSessions();

                    TaskQueue.mainQueue().executeDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(RECEIVED_MSG_NOTIFICATION);
                            intent.putExtra(MSG_KEY, message);
                            BroadcastCenter.shareInstance().postBroadcast(intent);
                            if (UserDefaults.getInstance().get(Constants.USER_DEFAULTS_MESSAGE_NOTICE_SOUND,true)) {
                                App.ringtone();
                            }
                        }
                    }, time);
                    time += 300;
                }

            }
        };

        MessageBiz.fetchMessage(latest_pull_at,res);
    }

    public void sendMessage(final String msg,final long to,final MapMarkPoint recPoint, final RPC.Response<MessageBiz.Message> response) {

        UserCenter.User user = UserCenter.shareInstance().user();
        MessageBiz.Message message = new MessageBiz.Message();
        message.fromUserName = user.nick;
        message.fromUserId = user.uid;
        message.toUserId = to;
        message.content = msg;
        message.timestamp = Utils.getServerTime();
        message.latitude = Double.toString(LBService.shareInstance().getLatestLatitude());
        message.longitude = Double.toString(LBService.shareInstance().getLatestLongitude());

        RPC.Response<MessageBiz.Message> res = new RPC.Response<MessageBiz.Message>() {
            @Override
            public void onStart() {
                if (response != null) {
                    response.onStart();
                }
            }

            @Override
            public void onSuccess(MessageBiz.Message message1) {
                if (response != null) {
                    response.onSuccess(message1);

                    String sid = Session.composedSessionID(UserCenter.shareInstance().UID(),to);
                    Session session = snMap.get(sid);
                    if (session == null) {
                        session = new Session();
                        session.sid = sid;
                        session.other = to;
                        session.otherName = recPoint.nick;

                        //模拟出一条消息
                        session.lastLng = recPoint.longitude;
                        session.lastLat = recPoint.latitude;
                        session.lastRcvMsg = recPoint.message;

                        snMap.put(sid,session);
                        snlist.add(0,session);

                        checkSessionMaxCount();
                    }

                    unreadCount -= session.unreadCount;
                    if (unreadCount <= 0) {
                        unreadCount = 0;
                    }
                    session.unreadCount = 0;

                    session.msg = message1.content;
                    session.lastSndMsg = message1.content;//用于下次进入时展示

                    saveSessions();
                }
            }

            @Override
            public void onFailure(Exception e) {
                if (response != null) {
                    response.onFailure(e);
                }
            }

            @Override
            public void onFinish() {
                if (response != null) {
                    response.onFinish();
                }
            }
        };
        MessageBiz.send(msg, to, Double.parseDouble(message.longitude), Double.parseDouble(message.latitude), res);
    }

    public Session getSession(long otherId) {
        String sid = Session.composedSessionID(UserCenter.shareInstance().UID(),otherId);
        return snMap.get(sid);
    }

    public Session getSession(String sid) {
        return snMap.get(sid);
    }

    public void visiableSessionMessage(MessageBiz.Message message) {
        if (message.toUserId != UserCenter.shareInstance().UID()) {return;}

        String sid = Session.composedSessionID(UserCenter.shareInstance().UID(),message.fromUserId);
        Session session = snMap.get(sid);
        if (session != null) {
            List<String> dels = new ArrayList<>();
            for (String msg : session.unrdmsgs) {
                dels.add(msg);
                if (msg.contains(message.id)) {
                    break;
                }
            }
            session.unrdmsgs.removeAll(dels);//删除更早的消息

            session.lastRcvMsg = message.content;
            try {
                session.lastLat = Double.parseDouble(message.latitude);
            } catch (Throwable e){e.printStackTrace();}

            try {
                session.lastLng = Double.parseDouble(message.longitude);
            } catch (Throwable e){e.printStackTrace();}

            if (dels.size() > session.unreadCount) {
                unreadCount -= session.unreadCount;
                session.unreadCount = 0;
            } else {
                session.unreadCount = session.unreadCount - dels.size();
                unreadCount -= dels.size();
            }
            if (unreadCount < 0) {unreadCount = 0;}

            session.msg = message.content;
            saveSessions();
        }
    }

    public List<MessageBiz.Message> getUnreadMessages(String sid) {
        Session session = snMap.get(sid);
        if (session == null) {
            return new ArrayList<>();
        }

        List<MessageBiz.Message> list = new ArrayList<>();
        for (String msg : session.unrdmsgs) {
            try {
                MessageBiz.Message m = JSON.parseObject(msg, MessageBiz.Message.class);
                list.add(m);
            } catch (Throwable e) {e.printStackTrace();}
        }

        if (session.unreadCount > 0) {
            session.unreadCount = 0;

            unreadCount -= session.unreadCount;
            if (unreadCount < 0) {unreadCount = 0;}

            saveSessions();
        }

        return list;
    }

    public void removeSession(String sid) {
        Session session = snMap.get(sid);
        if (session == null) {
            return ;
        }

        snMap.remove(sid);
        snlist.remove(session);
    }

    //限制session最大数
    private void checkSessionMaxCount() {

    }

    /**
     * 未读消息
     * @return
     */
    public int unreadCount() {
        return unreadCount;
    }

    private int count;
    private int unreadCount;
    private boolean _open;
    private boolean _high;//是否为高频
    private static final int HIGH_PULL_INTERVAL = 2;//秒
    private static final int NORMAL_PULL_INTERVAL = 5;//一分钟拉取一次足够了
    private static final String CLOCK_KEY = "pull_message";
    private static final String LATEST_PULL_KEY = "message_latest_pull_at";
}
