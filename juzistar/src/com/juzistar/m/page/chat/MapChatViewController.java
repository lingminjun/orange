package com.juzistar.m.page.chat;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.utils.DistanceUtil;
import com.juzistar.m.R;
import com.juzistar.m.biz.MessageBiz;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.biz.lbs.LBService;
import com.juzistar.m.constants.Constants;
import com.juzistar.m.entity.MapMarkPoint;
import com.juzistar.m.page.base.BaseViewController;
import com.juzistar.m.view.com.UIDic;
import com.ssn.framework.foundation.App;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.foundation.TR;

import java.util.ArrayList;
import java.util.List;

/**
 * 演示覆盖物的用法
 */
public class MapChatViewController extends BaseViewController {

	/**
	 * MapView 是地图主控件
	 */
	private MapView mMapView;
	private BaiduMap mBaiduMap;

    private List<OverlayOptions> marks;
    private OverlayManager manager;

	private Marker mMarkerMe;
	private Marker mMarkerOther;
//	private Marker mMarkerC;
//	private Marker mMarkerD;

    private long otherId;
    private MapMarkPoint latestReceiveMessage;
    private MapMarkPoint latestSendMessage;

	private InfoWindow mInfoWindow;

    BitmapDescriptor bdMe; //= BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
    BitmapDescriptor bdOther;// = BitmapDescriptorFactory.fromResource(R.drawable.icon_markb);

    @Override
    public void onInit(Bundle args) {
        super.onInit(args);

        navigationItem().setTitle(Res.localized(R.string.encounters));
        navigationItem().setTitleColor(Res.color(R.color.normal_text));

        otherId = args.getLong(Constants.PAGE_ARG_OTHER_ID,0);
        latestReceiveMessage = (MapMarkPoint)args.getSerializable(Constants.PAGE_ARG_LATEST_RECEIVE_MESSAGE);

        latestSendMessage = (MapMarkPoint)args.getSerializable(Constants.PAGE_ARG_LATEST_SEND_MESSAGE);
        if (latestSendMessage == null) {//在没有发过消息时，取最后一次消息，若没有发过消息，取当前位置

//            if ()

            latestSendMessage = new MapMarkPoint();
            latestSendMessage.uid = UserCenter.shareInstance().UID();
            latestSendMessage.latitude = (float)LBService.shareInstance().getLatestLatitude();
            latestSendMessage.longitude = (float)LBService.shareInstance().getLatestLongitude();
        }

        bdMe = BitmapDescriptorFactory.fromResource(UIDic.mascotResourceId(UserCenter.shareInstance().UID(),true));
        bdOther = BitmapDescriptorFactory.fromResource(UIDic.mascotResourceId(otherId,false));
    }



    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.map_chat_layout, null);

        mMapView = (MapView) view.findViewById(R.id.map_background_view);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMarkerClickListener(markClick);

        refreshMapZoom();

        return view;
    }

    private void refreshMapZoom() {

        /*
        //自己计算比例
        float latitude = latestReceiveMessage.latitude + ((latestSendMessage.latitude - latestReceiveMessage.latitude)/2.0f);
        float longitude = latestReceiveMessage.longitude + ((latestSendMessage.longitude - latestReceiveMessage.longitude)/2.0f);

        LatLng p1 = new LatLng(latestReceiveMessage.latitude, latestReceiveMessage.longitude);
        LatLng p2 = new LatLng(latestSendMessage.latitude, latestSendMessage.longitude);

        float zoom = 16;//3~18，我们一般取4~16
        double distance = DistanceUtil.getDistance(p1, p2);

        if (distance >= 8000) {
            zoom = 10.0f;
        } else if (distance >= 6000) {
            zoom = 13.0f;
        } else if (distance >= 4000) {
            zoom = 14.0f;
        } else if (distance >= 2000) {
            zoom = 15.0f;
        } else {
            zoom = 16.0f;
        }

        LatLng point = new LatLng(latitude, longitude);
        MapStatusUpdate msu = MapStatusUpdateFactory.newLatLngZoom(point, zoom);	//设置地图中心点以及缩放级别

        mBaiduMap.setMapStatus(msu);
        mBaiduMap.animateMapStatus(msu);

        */

        initOverlay();
    }

    OnMarkerClickListener markClick = new OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(final Marker marker) {
            Button button = new Button(Res.context());
            button.setBackgroundResource(R.drawable.popup);
            OnInfoWindowClickListener listener = null;
//            if (marker == mMarkerMe || marker == mMarkerOther) {
//                if (marker == mMarkerMe) {
//                    button.setText(TR.string(latestSendMessage.message));
//                } else if (marker == mMarkerOther) {
//                    button.setText(TR.string(latestReceiveMessage.message));
//                }
                listener = new OnInfoWindowClickListener() {
                    public void onInfoWindowClick() {
                        LatLng ll = marker.getPosition();
                        LatLng llNew = new LatLng(ll.latitude + 0.005,
                                ll.longitude + 0.005);
                        marker.setPosition(llNew);
                        mBaiduMap.hideInfoWindow();
                    }
                };
                LatLng ll = marker.getPosition();
                mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -47, listener);
                mBaiduMap.showInfoWindow(mInfoWindow);
//            }

            return true;
        }
    };

    @Override
	public void onViewDidLoad () {
		super.onViewDidLoad();
        if (otherId == 0) {
            finish();
        }
	}

    /**
     * 根据位置展示头像，并且加载消息
     */
	public void initOverlay() {
		// add marker overlay
		LatLng llA = new LatLng(latestSendMessage.latitude, latestSendMessage.longitude);
		LatLng llB = new LatLng(latestReceiveMessage.latitude, latestReceiveMessage.longitude);


        //锚点设置（默认（0.5f, 1.0f）水平居中，垂直下对齐）
		MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdMe).zIndex(4).anchor(1.0f, 1.0f);//.draggable(true);
        ooA.animateType(MarkerAnimateType.grow);//生长动画
//		mMarkerMe = (Marker) (mBaiduMap.addOverlay(ooA));

		MarkerOptions ooB = new MarkerOptions().position(llB).icon(bdOther).zIndex(5).anchor(0.0f, 1.0f);
        ooB.animateType(MarkerAnimateType.grow);//生长动画
//		mMarkerOther = (Marker) (mBaiduMap.addOverlay(ooB));

        if (marks == null) {
            marks = new ArrayList<>();
            manager = new OverlayManager(mBaiduMap) {

                @Override
                public boolean onPolylineClick(Polyline polyline) {
                    return false;
                }

                @Override
                public boolean onMarkerClick(final Marker marker) {
                    Button button = new Button(Res.context());
                    button.setBackgroundResource(R.drawable.popup);
                    OnInfoWindowClickListener listener = null;
                    button.setText(TR.string("dddddd"));
//                    if (marker == mMarkerMe || marker == mMarkerOther) {
//                        if (marker == mMarkerMe) {
//                            button.setText(TR.string(latestSendMessage.message));
//                        } else if (marker == mMarkerOther) {
//                            button.setText(TR.string(latestReceiveMessage.message));
//                        }
                        listener = new OnInfoWindowClickListener() {
                            public void onInfoWindowClick() {
                                LatLng ll = marker.getPosition();
                                LatLng llNew = new LatLng(ll.latitude + 0.005,
                                        ll.longitude + 0.005);
                                marker.setPosition(llNew);
                                mBaiduMap.hideInfoWindow();
                            }
                        };
                        LatLng ll = marker.getPosition();
                        mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -47, listener);
                        mBaiduMap.showInfoWindow(mInfoWindow);
//                    }
                    return true;
                }

                @Override
                public List<OverlayOptions> getOverlayOptions() {
                    return marks;
                }
            };
        }

        marks.clear();
        marks.add(ooA);
        marks.add(ooB);


        manager.addToMap();
        manager.zoomToSpan();
	}

    private void showMessage(Marker marker,String message) {
        Button button = new Button(Res.context());
        button.setBackgroundResource(R.drawable.popup);
        button.setText(message);
        LatLng ll = marker.getPosition();
        mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), ll, -47, null);
        mBaiduMap.showInfoWindow(mInfoWindow);
    }

    /**
     * 拖拽事件
     */
    OnMarkerDragListener onMarkerDragListener = new OnMarkerDragListener() {
        public void onMarkerDrag(Marker marker) {
        }

        public void onMarkerDragEnd(Marker marker) {
            App.toast("拖拽结束，新位置：" + marker.getPosition().latitude + ", "
                    + marker.getPosition().longitude);
        }

        public void onMarkerDragStart(Marker marker) {
        }
    };

	/**
	 * 清除所有Overlay
	 * 
	 * @param view
	 */
	public void clearOverlay(View view) {
		mBaiduMap.clear();
		mMarkerMe = null;
		mMarkerOther = null;
//		mMarkerC = null;
//		mMarkerD = null;
	}

    /**
     * 重新添加Overlay
     *
     * @param view
     */
    public void resetOverlay(View view) {
        clearOverlay(null);
        initOverlay();
    }

	@Override
	public void onViewDidDisappear() {
        super.onViewDidDisappear();
		// MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		mMapView.onPause();
	}

	@Override
    public void onViewDidAppear() {
        super.onViewDidAppear();
		// MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		mMapView.onResume();
	}

    @Override
    public void onDestroyController() {
        super.onDestroyController();

        // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
        mMapView.onDestroy();

        // 回收 bitmap 资源
        bdMe.recycle();
        bdOther.recycle();
//        bdC.recycle();
//        bdD.recycle();
//        bd.recycle();
//        bdGround.recycle();
    }

    public static class MyOverlay extends Overlay
    {
        LatLng loc;
        public MyOverlay(LatLng point) {
            loc = point;
        }

        Paint paint = new Paint();

//        @Override
        public void draw(Canvas canvas, MapView mapView, boolean shadow)
        {
            // 在天安门的位置绘制一个String

            Point point = mapView.getMap().getProjection().toScreenLocation(loc);
            canvas.drawText("★这里是天安门", point.x, point.y, paint);
        }
    }

}
