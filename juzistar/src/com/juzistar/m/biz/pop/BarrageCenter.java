package com.juzistar.m.biz.pop;

import android.content.Intent;
import android.text.TextUtils;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.juzistar.m.Utils.Utils;
import com.juzistar.m.biz.MessageBiz;
import com.juzistar.m.biz.NoticeBiz;
import com.juzistar.m.biz.UserCenter;
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
        mLatitude = Double.parseDouble(UserDefaults.getInstance().get(BARRAGE_LB_LAT_KEY, "0.0"));
        mLongitude = Double.parseDouble(UserDefaults.getInstance().get(BARRAGE_LB_LON_KEY, "0.0"));
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

        //设置用户自己设置的位置
        String longitude = Double.toString(getLatestLongitude());
        String latitude = Double.toString(getLatestLatitude());

        NoticeBiz.getList(latest_pull_at,longitude,latitude,new RPC.Response<NoticeBiz.NoticeList>(){
            @Override
            public void onSuccess(NoticeBiz.NoticeList list) {
                super.onSuccess(list);

                if (list == null) {
                    return;
                }

                //存储游标
                if (list.latestTime > 0) {
                    UserDefaults.getInstance().put(LATEST_PULL_KEY, list.latestTime);
                }

                int time = 0;
                for (final NoticeBiz.Notice notice : list.list) {

                    //过滤掉自己发送的
                    if (notice.creatorId == UserCenter.shareInstance().UID()) {
                        continue;
                    }

                    //若收到的是标签消息，则将标签消息存储下来
                    if (notice.category != NoticeBiz.NoticeCategory.NAN && notice.creatorId != UserCenter.shareInstance().UID()) {
                        tagNotice.add(notice);//标签消息
                    }

                    TaskQueue.mainQueue().executeDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(RECEIVED_BARRAGE_NOTIFICATION);
                            intent.putExtra(BARRAGE_KEY,notice);
                            BroadcastCenter.shareInstance().postBroadcast(intent);
                        }
                    },time);
                    time += 300;
                }

                //检查太大
                if (tagNotice.size() > 400) {
                    List<NoticeBiz.Notice> subList = tagNotice.subList(200,tagNotice.size());
                    tagNotice.clear();
                    tagNotice.addAll(subList);
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

    private static final int PULL_INTERVAL = 5;//秒
    private static final String CLOCK_KEY = "pull_barrage";
    private static final String LATEST_PULL_KEY = "barrage_latest_pull_at";

    //最后设置的位置信息
    public static final String BARRAGE_LB_ADDR_KEY = "barrage_lb_addr_key";
    public static final String BARRAGE_LB_CITY_KEY = "barrage_lb_city_key";
    public static final String BARRAGE_LB_PROV_KEY = "barrage_lb_prov_key";
    public static final String BARRAGE_LB_STAT_KEY = "barrage_lb_stat_key";
    public static final String BARRAGE_LB_LAT_KEY = "barrage_lb_lati_key";
    public static final String BARRAGE_LB_LON_KEY = "barrage_lb_long_key";
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
        UserDefaults.getInstance().put(BARRAGE_LB_LAT_KEY,"" + mLatitude);
        UserDefaults.getInstance().put(BARRAGE_LB_LON_KEY,"" + mLongitude);

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

    public double getLatestLatitude() {
        if (TextUtils.isEmpty(mAddress)) {
            return LBService.shareInstance().getLatestLatitude();
        } else {
            return mLatitude;
        }
    }

    public double getLatestLongitude() {
        if (TextUtils.isEmpty(mAddress)) {
            return LBService.shareInstance().getLatestLongitude();
        } else {
            return mLongitude;
        }
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

        //设置用户自己设置的位置
        notice.latitude = Double.toString(getLatestLatitude());
        notice.longitude = Double.toString(getLatestLongitude());

        if (notice.category != NoticeBiz.NoticeCategory.NAN) {
            notice.type = NoticeBiz.NoticeType.NORMAL;
        } else {
            notice.type = NoticeBiz.NoticeType.TEMP;
        }

        NoticeBiz.create(notice,new RPC.Response<NoticeBiz.Notice>(){
            @Override
            public void onStart() {
                if (response != null) {
                    response.onStart();
                }
            }

            @Override
            public void onSuccess(NoticeBiz.Notice notice) {
                if (response != null) {
                    response.onSuccess(notice);

                    if (notice.category != NoticeBiz.NoticeCategory.NAN) {
                        UserDefaults.getInstance().put(LAST_SEND_TAG_NOTICE_TIME,TAG_NOTICE_INTERVAL + HTTPAccessor.getNetTime());
                    }
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
        });
    }

    public List<NoticeBiz.Notice> getAllTabNotice() {
        return new ArrayList<>(tagNotice);
    }


    private static final long TAG_NOTICE_INTERVAL = 5 * 60 * 1000;//30分钟
    private static final String LAST_SEND_TAG_NOTICE_TIME = "barrage_last_send_tag_notice_key";
    public boolean isLimitSendingTagNotice() {
        long expired = UserDefaults.getInstance().get(LAST_SEND_TAG_NOTICE_TIME,0L);
        long now = HTTPAccessor.getNetTime() + 300;//防止界面误差
        return now <= expired;
    }

    public long limitSendingTagNoticeTime() {
        long expired = UserDefaults.getInstance().get(LAST_SEND_TAG_NOTICE_TIME,0L);
        long now = HTTPAccessor.getNetTime();
        if (now <= expired) {
            return expired - now;
        }
        return 0;
    }
}
