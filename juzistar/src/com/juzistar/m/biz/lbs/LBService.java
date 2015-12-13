package com.juzistar.m.biz.lbs;

import android.app.Service;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.juzistar.m.R;
import com.juzistar.m.biz.UserCenter;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.foundation.UserDefaults;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by lingminjun on 15/11/29.
 */
public final class LBService {
    private static final String LATEST_ADDR_KEY = "lbs.addr.key";
    private static final String LATEST_CITY_KEY = "lbs.city.key";
    private static final String LATEST_LATI_KEY = "lbs.lati.key";
    private static final String LATEST_LONG_KEY = "lbs.long.key";

    private static LBService _instance = null;

    /**
     * 用户中心
     * @return 唯一实例
     */
    static public LBService shareInstance() {
        if (_instance != null) return _instance;
        synchronized(LBService.class){
            if (_instance != null) return _instance;
            _instance = newInstance();
            return _instance;
        }
    }

    private static LBService newInstance() {
        return new LBService();
    }

    /**
     * 防止构造实例
     */
    private LBService() {
        super();
        mLocationClient = new LocationClient(Res.context());
        mLocationClient.registerLocationListener(mMyLocationListener);
        mVibrator =(Vibrator) Res.context().getSystemService(Service.VIBRATOR_SERVICE);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系，
        option.setScanSpan(1000);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIgnoreKillProcess(true);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死

        mLocationClient.setLocOption(option);

        mLatitude = Double.parseDouble(UserDefaults.getInstance().get(LATEST_LATI_KEY,"0.0"));
        mLongitude = Double.parseDouble(UserDefaults.getInstance().get(LATEST_LONG_KEY, "0.0"));
        mAddress = UserDefaults.getInstance().get(LATEST_ADDR_KEY,"");
        mCity = UserDefaults.getInstance().get(LATEST_CITY_KEY,"");

        // 在使用 SDK 各组间之前初始化 context 信息，传入 ApplicationContext
        SDKInitializer.initialize(Res.context());
    }



    private LocationClient mLocationClient;
    private Vibrator mVibrator;

    private BDLocation mLocation;
    private double mLatitude;
    private double mLongitude;
    private String mAddress;
    private String mCity;

    private BDLocationListener mMyLocationListener = new BDLocationListener() {
        @Override
        public void onReceiveLocation(BDLocation location) {

            if (location != null && location.hasAddr()) {
                mLocation = location;
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();

                if (!TextUtils.isEmpty(location.getAddrStr())) {
                    mAddress = location.getAddrStr();
                    mCity = location.getCity();
                    UserDefaults.getInstance().put(LATEST_ADDR_KEY,mAddress);
                    UserDefaults.getInstance().put(LATEST_CITY_KEY,mCity);
                    UserDefaults.getInstance().put(LATEST_LATI_KEY,"" + mLatitude);
                    UserDefaults.getInstance().put(LATEST_LONG_KEY,"" + mLongitude);
                }
            }

//Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation){// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");

            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation){// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }

//            logMsg(sb.toString());
            Log.i("BaiduLocationApiDem", sb.toString());

            for (BDLocationListener callback : callbacks) {
                try {
                    callback.onReceiveLocation(location);
                } catch (Throwable e) {e.printStackTrace();}
            }
            callbacks.clear();

            if (!isResident) {
                stop();
            }
        }
    };

    private boolean isResident;
    public void start() {
        isResident = true;
        restart();
    }

    public void stop() {
        isResident = false;
        if (callbacks.size() == 0) {
            mLocationClient.stop();
        }
    }

    private void restart() {
        mLocationClient.start();//定位SDK start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
        mLocationClient.requestLocation();
    }

    private Set<BDLocationListener> callbacks = new HashSet<>();
    /**
     * 异步发起一次定位请求
     */
    public void asyncLocation(BDLocationListener callback) {
        if (callback != null) {
            callbacks.add(callback);
            restart();
        }
    }

    /**
     * 获取最新的地址信息
     * @return
     */
    public double getLatestLatitude() {
        return mLatitude;
    }

    public double getLatestLongitude() {
        return mLongitude;
    }

    public String getLatestAddress() {
        return mAddress;
    }

    public String getLatestSimpleAddress() {
        String str = getLatestAddress();
        if (TextUtils.isEmpty(str)) {return "";}

        int idx = str.lastIndexOf(Res.localized(R.string.district));
        if (idx >= 0 && idx < str.length()) {
            return str.substring(idx+1,str.length());
        }
        return str;
    }

    public String getLatestCity() {
        return mCity;
    }

}
