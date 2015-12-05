package com.juzistar.m.biz.pop;

import android.content.Intent;
import android.text.TextUtils;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.juzistar.m.biz.MessageBiz;
import com.juzistar.m.biz.NoticeBiz;
import com.juzistar.m.biz.lbs.LBService;
import com.juzistar.m.biz.lbs.Location;
import com.juzistar.m.net.BaseModelList;
import com.ssn.framework.foundation.*;

import java.util.*;

/**
 * Created by lingminjun on 15/11/9.
 * 弹幕接受与播发
 */
public final class BarrageCenter {

    public final static String RECEIVED_BARRAGE_NOTIFICATION = "received_barrage_notification";
    public final static String BARRAGE_KEY = "barrage_key";

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

        mAddress = UserDefaults.getInstance().get(BARRAGE_LB_ADDR_KEY, "");
        mCity = UserDefaults.getInstance().get(BARRAGE_LB_CITY_KEY, "");
        mCountry = UserDefaults.getInstance().get(BARRAGE_LB_STAT_KEY, "");
        mProvince = UserDefaults.getInstance().get(BARRAGE_LB_PROV_KEY, "");
        mLongitude = UserDefaults.getInstance().get(BARRAGE_LB_LAT_KEY, 0.0f);
        mLatitude = UserDefaults.getInstance().get(BARRAGE_LB_LON_KEY,0.0f);
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
        long latest_pull_at = UserDefaults.getInstance().get(LATEST_PULL_KEY,0l);
        String longitude = Double.toString(LBService.shareInstance().getLatestLongitude());
        String latitude = Double.toString(LBService.shareInstance().getLatestLatitude());
        NoticeBiz.getList(latest_pull_at,longitude,latitude,new RPC.Response<NoticeBiz.NoticeList>(){
            @Override
            public void onSuccess(NoticeBiz.NoticeList list) {
                super.onSuccess(list);

                if (list == null) {
                    return;
                }

                //存储游标
                UserDefaults.getInstance().put(LATEST_PULL_KEY,list.latestTime);

                int time = 0;
                for (final NoticeBiz.Notice notice : list.list) {

                    //若收到的是标签消息，则将标签消息存储下来
                    if (notice.category != NoticeBiz.NoticeCategory.NAN) {
                        tagNotice.add(notice);//标签消息
                    }

                    time += 300;
                    TaskQueue.mainQueue().executeDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(RECEIVED_BARRAGE_NOTIFICATION);
                            intent.putExtra(BARRAGE_KEY,notice);
                            BroadcastCenter.shareInstance().postBroadcast(intent);
                        }
                    },time);
                }
            }
        });

//        RPC.Response<BaseModelList<MessageBiz.Message>> res = new RPC.Response<BaseModelList<MessageBiz.Message>>() {
//            @Override
//            public void onSuccess(BaseModelList<MessageBiz.Message> messageBaseModelList) {
//                super.onSuccess(messageBaseModelList);
//
//                //接受到数据并做去重处理
//            }
//        };
//
//        MessageBiz.fetchMessage(0,res);
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
    private List<NoticeBiz.Notice> tagNotice = new ArrayList<>();

    private static final int PULL_INTERVAL = 15;//秒
    private static final String CLOCK_KEY = "pull_barrage";
    private static final String LATEST_PULL_KEY = "latest_pull_at";

    //最后设置的位置信息
    public static final String BARRAGE_LB_ADDR_KEY = "barrage_lb_addr_key";
    public static final String BARRAGE_LB_CITY_KEY = "barrage_lb_city_key";
    public static final String BARRAGE_LB_PROV_KEY = "barrage_lb_prov_key";
    public static final String BARRAGE_LB_STAT_KEY = "barrage_lb_stat_key";
    public static final String BARRAGE_LB_LAT_KEY = "barrage_lb_lat_key";
    public static final String BARRAGE_LB_LON_KEY = "barrage_lb_lon_key";
    private String mAddress;
    private String mCity;
    private String mProvince;
    private String mCountry;
    private double mLatitude;
    private double mLongitude;
    public void setLocation(Location location) {
        mAddress = location.mAddress;
        mCity = location.mCity;
        mLongitude = location.mLongitude;
        mLatitude = location.mLatitude;

        UserDefaults.getInstance().put(BARRAGE_LB_ADDR_KEY,TR.string(mAddress));
        UserDefaults.getInstance().put(BARRAGE_LB_CITY_KEY,TR.string(mCity));
        UserDefaults.getInstance().put(BARRAGE_LB_LAT_KEY,(float)mLatitude);
        UserDefaults.getInstance().put(BARRAGE_LB_LON_KEY,(float)mLongitude);

        if (!TextUtils.isEmpty(location.mCountry)) {
            mCountry = location.mCountry;
            UserDefaults.getInstance().put(BARRAGE_LB_STAT_KEY,TR.string(mCountry));
        }

        if (!TextUtils.isEmpty(location.mProvince)) {
            mProvince = location.mProvince;
            UserDefaults.getInstance().put(BARRAGE_LB_PROV_KEY, TR.string(mProvince));
        }

    }

    public Location getLocation () {
        Location location = new Location();

        //如果位置不存在，则取定位地址
        if (TextUtils.isEmpty(mAddress)) {
            location.mCity = LBService.shareInstance().getLatestCity();
            location.mLatitude = LBService.shareInstance().getLatestLatitude();
            location.mLongitude = LBService.shareInstance().getLatestLongitude();
            location.mAddress = LBService.shareInstance().getLatestAddress();
        } else {
            location.mAddress = mAddress;
            location.mCity = mCity;
            location.mCountry = mCountry;
            location.mProvince = mProvince;
            location.mLongitude = mLongitude;
            location.mLatitude = mLatitude;
        }

        return location;
    }

    public void refreshCurrentLocation(final Runnable callback) {
        LBService.shareInstance().asyncLocation(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {

                //表示找到地址
                if (bdLocation.hasAddr()) {
                    Location location = new Location();
                    location.mAddress = LBService.shareInstance().getLatestAddress();
                    location.mCity = LBService.shareInstance().getLatestCity();
                    location.mLatitude = LBService.shareInstance().getLatestLatitude();
                    location.mLongitude = LBService.shareInstance().getLatestLongitude();
                    setLocation(location);
                }

                if (callback != null) {
                    callback.run();
                }
            }
        });
    }

    public void publishNotice(final NoticeBiz.Notice notice, final RPC.Response<NoticeBiz.Notice> response) {

        if (TextUtils.isEmpty(mAddress)) {
            notice.latitude = Float.toString((float)(LBService.shareInstance().getLatestLatitude()));
            notice.longitude = Float.toString((float)(LBService.shareInstance().getLatestLongitude()));
        } else {
            notice.latitude = Float.toString((float)(mLatitude));
            notice.longitude = Float.toString((float)(mLongitude));
        }

        if (notice.category != NoticeBiz.NoticeCategory.NAN) {
            tagNotice.add(notice);
        }

        NoticeBiz.create(notice,response);
    }

    public List<NoticeBiz.Notice> getAllTabNotice() {
        return new ArrayList<>(tagNotice);
    }
}
