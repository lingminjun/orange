package com.juzistar.m.biz.msg;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import com.juzistar.m.Utils.Utils;
import com.juzistar.m.biz.MessageBiz;
import com.juzistar.m.biz.NoticeBiz;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.biz.lbs.LBService;
import com.ssn.framework.foundation.*;
import org.json.JSONArray;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
                ObjectInputStream oin = new ObjectInputStream(new ByteArrayInputStream(data));
                List<Session> ss = (List<Session>) oin.readObject();
                oin.close();

                if (ss != null) {
                    snlist.addAll(ss);
                    for (Session sn : ss) {
                        snMap.put(sn.sid,sn);
                    }
                }
            } catch (Throwable e) {}

        }
    }

    private void saveSessions() {
        if (snlist.size() > 0) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024*1024);

                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                out.writeObject(snlist);
                out.close();

                byte[] data = outputStream.toByteArray();
                Store.documents().store(SESSION_STORE_KEY,data);

            } catch (Throwable e){e.printStackTrace();}
        } else {
            Store.documents().remove(SESSION_STORE_KEY);
        }
    }

    /**
     * 聊天会话
     */
    public static class Session implements Parcelable {
        public String sid;
        public long other;
        public String otherName;
        public int unreadCount;//未读数
        public String msg;//最后显示的msg
        public List<String> unrdmsgs;//最后三条
//        public boolean hasNew;//有新的消息


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

        public Session(Parcel parcel) {
            // 反序列化 顺序要与序列化时相同
            sid = parcel.readString();
            other = parcel.readLong();
            otherName = parcel.readString();
            unreadCount = parcel.readInt();
            msg = parcel.readString();
            unrdmsgs = new ArrayList<>();
            parcel.readStringList(unrdmsgs);

        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            // 序列化
            dest.writeString(TR.string(sid));
            dest.writeLong(other);
            dest.writeString(TR.string(otherName));
            dest.writeInt(unreadCount);
            dest.writeString(msg);
            dest.writeStringList(unrdmsgs);
        }

        public static final Creator<Session> CREATOR = new Creator<Session>() {

            @Override
            public Session createFromParcel(Parcel source) {
                // 反序列化 顺序要与序列化时相同
                return new Session(source);
            }

            @Override
            public Session[] newArray(int i) {
                return new Session[i];
            }
        };
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
                    if (message.toUserId != UserCenter.shareInstance().UID()) {
                        continue;
                    }

                    //查看session是否已经存在
                    String sid = Session.composedSessionID(UserCenter.shareInstance().UID(),message.toUserId);
                    Session session = snMap.get(sid);
                    if (session == null) {
                        session = new Session();
                        session.sid = sid;
                        session.unreadCount++;
                        session.other = message.fromUserId;
                        session.otherName = message.fromName;

                        snMap.put(sid,session);
                        snlist.add(0,session);
                    }

                    if(TextUtils.isEmpty(session.msg)) {
                        session.msg = message.content;
                    } else {
                        String json = MessageBiz.Message.messageToJSON(message);
                        session.unrdmsgs.add(json);
                    }

                    TaskQueue.mainQueue().executeDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(RECEIVED_MSG_NOTIFICATION);
                            intent.putExtra(MSG_KEY, message);
                            BroadcastCenter.shareInstance().postBroadcast(intent);
                        }
                    }, time);
                    time += 300;
                }

            }
        };

        MessageBiz.fetchMessage(latest_pull_at,res);
    }

    public void sendMessage(final String msg,final long to, final RPC.Response<MessageBiz.Message> response) {

        UserCenter.User user = UserCenter.shareInstance().user();
        MessageBiz.Message message = new MessageBiz.Message();
        message.fromName = user.nick;
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
        MessageBiz.send(msg, to, LBService.shareInstance().getLatestLatitude(), LBService.shareInstance().getLatestLongitude(), res);
    }


    private int count;
    private boolean _open;
    private boolean _high;//是否为高频
    private static final int HIGH_PULL_INTERVAL = 5;//秒
    private static final int NORMAL_PULL_INTERVAL = 60;//一分钟拉取一次足够了
    private static final String CLOCK_KEY = "pull_message";
    private static final String LATEST_PULL_KEY = "message_latest_pull_at";
}
