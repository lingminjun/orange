package com.orange.m.biz.pop;

import com.orange.m.biz.MessageBiz;
import com.orange.m.net.BaseModelList;
import com.ssn.framework.foundation.Clock;
import com.ssn.framework.foundation.RPC;
import com.ssn.framework.foundation.UserDefaults;

import java.util.Set;

/**
 * Created by lingminjun on 15/11/9.
 * 弹幕接受与播发
 */
public final class BarrageCenter {

    private static BarrageCenter _instance = null;

    /**
     * 用户中心
     * @return 唯一实例
     */
    static public BarrageCenter shareInstance() {
        if (_instance != null) return _instance;
        synchronized(BarrageCenter.class){
            if (_instance != null) return _instance;
            _instance = newInstance();
            return _instance;
        }
    }

    private static BarrageCenter newInstance() {
        return new BarrageCenter();
    }

    /**
     * 防止构造实例
     */
    private BarrageCenter() {
        super();
    }


    /**
     * 服务是否开启
     * @return 是否开启
     */
    public boolean isOpenService() {return _open;}
    private boolean _open;

    /**
     * 开启服务
     * @return
     */
    public void startService() {
        if (_open) {return;}

        _open = true;
        Clock.shareInstance().addListener(clock,CLOCK_KEY);
    }

    /**
     * 停止服务
     */
    public void stopService() {
        _open = false;
        Clock.shareInstance().removeListener(CLOCK_KEY);
    }


    private void pullBarrage() {
        long latest_pull_at = UserDefaults.getInstance().get(LATEST_PULL_KEY,0);

        RPC.Response<BaseModelList<MessageBiz.Message>> res = new RPC.Response<BaseModelList<MessageBiz.Message>>() {
            @Override
            public void onSuccess(BaseModelList<MessageBiz.Message> messageBaseModelList) {
                super.onSuccess(messageBaseModelList);

                //接受到数据并做去重处理
            }
        };

        MessageBiz.fetchMessage(0,res);
    }

    private Clock.Listener clock = new Clock.Listener() {
        @Override
        public void fire(String flag) {
            count++;
            if (count >= PULL_INTERVAL) {
                count = 0;//循环
                pullBarrage();
            }
        }
    };


    private int count;
//    private Set<msgId>
    

    private static final int PULL_INTERVAL = 15;//秒
    private static final String CLOCK_KEY = "pull_barrage";
    private static final String LATEST_PULL_KEY = "latest_pull_at";
}
