package com.juzistar.m.biz.msg;

import android.os.Parcel;
import android.os.Parcelable;
import com.ssn.framework.foundation.Clock;
import com.ssn.framework.foundation.Store;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by lingminjun on 15/11/29.
 */
public final class MessageCenter {
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
    private static final int SESSION_MAX_SIZE = 50;
    private List<Session> list = new LinkedList<>();

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
                    list.addAll(ss);
                }
            } catch (Throwable e) {}

        }
    }

    private void saveSessions() {
        if (list.size() > 0) {
            try {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream(1024*1024);

                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                out.writeObject(list);
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
        public String latestMessage;
//        public boolean hasNew;//有新的消息
        public int unreadCount;//未读数

        @Override
        public boolean equals(Object o) {
            if (o instanceof Session) {
                return other == ((Session) o).other;
            }
            return super.equals(o);
        }

        @Override
        public int hashCode() {
            return (int)other;
        }

        public Session() {
        }

        public Session(Parcel parcel) {
            // 反序列化 顺序要与序列化时相同
            sid = parcel.readString();
            other = parcel.readLong();
            otherName = parcel.readString();
            latestMessage = parcel.readString();
            unreadCount = parcel.readInt();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            // 序列化
            dest.writeString(sid);
            dest.writeLong(other);
            dest.writeString(otherName);
            dest.writeString(latestMessage);
            dest.writeInt(unreadCount);
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
        return new ArrayList<>(list);
    }


    /**
     * 服务是否开启
     * @return 是否开启
     */
    public boolean isFrequency() {return _open;}
    private boolean _open;

    /**
     * 开启高频刷新
     * @return
     */
    public void startFrequency() {
        if (_open) {return;}

        _open = true;
        Clock.shareInstance().addListener(clock,CLOCK_KEY);
    }

    /**
     * 停止高频刷新
     */
    public void stopFrequency() {
        _open = false;
        Clock.shareInstance().removeListener(CLOCK_KEY);
    }
    private Clock.Listener clock = new Clock.Listener() {
        @Override
        public void fire(String flag) {
            count++;
            if (count >= PULL_INTERVAL) {
                count = 0;//循环
//                pullBarrage();
            }
        }
    };


    private int count;
    private static final int PULL_INTERVAL = 5;//秒
    private static final String CLOCK_KEY = "pull_message";
}
