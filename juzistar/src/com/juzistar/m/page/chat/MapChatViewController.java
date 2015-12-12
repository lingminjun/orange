package com.juzistar.m.page.chat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerDragListener;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MarkerOptions.MarkerAnimateType;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.juzistar.m.R;
import com.juzistar.m.biz.MessageBiz;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.biz.lbs.LBService;
import com.juzistar.m.biz.msg.MessageCenter;
import com.juzistar.m.biz.pop.BarrageCenter;
import com.juzistar.m.constants.Constants;
import com.juzistar.m.entity.MapMarkPoint;
import com.juzistar.m.page.base.BaseViewController;
import com.juzistar.m.view.com.UIDic;
import com.ssn.framework.foundation.*;
import com.ssn.framework.uikit.UILoading;
import com.ssn.framework.uikit.UILockScreenKeyboard;

import java.util.ArrayList;
import java.util.List;

/**
 * 演示覆盖物的用法
 */
public class MapChatViewController extends BaseViewController {

    private static final int MSG_SHOW_INTERVAL = 3;

	/**
	 * MapView 是地图主控件
	 */
	private MapView mMapView;
	private BaiduMap mBaiduMap;

    private List<OverlayOptions> marks;
    private OverlayManager manager;

	private Marker mMarkerMe;
	private Marker mMarkerOther;

    private long otherId;
    private MapMarkPoint latestReceiveMessage;
    private MapMarkPoint latestSendMessage;

    private List<MessageBiz.Message> recMsgs = new ArrayList<>();

	private InfoWindow mInfoWindow;

//    BitmapDescriptor bdMe; //= BitmapDescriptorFactory.fromResource(R.drawable.icon_marka);
//    BitmapDescriptor bdOther;// = BitmapDescriptorFactory.fromResource(R.drawable.icon_markb);

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
    }

    UILockScreenKeyboard.KeyboardListener keyboardListener = new UILockScreenKeyboard.KeyboardListener() {
        @Override
        public void onSendButtonClick(UILockScreenKeyboard keyboard, View sender) {
            String msg = keyboard.text();
            sendMessage(msg);
            keyboard.finish();
        }

        @Override
        public boolean onRightButtonClick(UILockScreenKeyboard keyboard, View sender) {
            return false;
        }

        @Override
        public void onScopeViewClick(UILockScreenKeyboard keyboard, View sender) {
            keyboard.finish();//直接隐藏
        }

        @Override
        public void onCustomButtonClick(UILockScreenKeyboard keyboard, View sender, int buttonKey) {

        }

        @Override
        public void onKeyboardChanged(UILockScreenKeyboard keyboard, int newHeight, int oldHeight) {

        }
    };

    OnMarkerClickListener markClick = new OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            UILockScreenKeyboard.show(getActivity(), keyboardListener, true);
            return false;
        }
    };

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
        refreshOverlay();
    }

    @Override
	public void onViewDidLoad () {
		super.onViewDidLoad();
        if (otherId == 0) {
            finish();
        }

        addObservers();

        //开启高频刷新
        MessageCenter.shareInstance().startFrequency();
        LBService.shareInstance().start();
	}

    private void addObservers() {
        BroadcastCenter.shareInstance().addObserver(this, MessageCenter.RECEIVED_MSG_NOTIFICATION, observerMethod);
    }

    private BroadcastCenter.Method<MapChatViewController> observerMethod = new BroadcastCenter.Method<MapChatViewController>() {
        @Override
        public void onReceive(MapChatViewController observer, Context context, Intent intent) {
            MessageBiz.Message message = (MessageBiz.Message)intent.getSerializableExtra(MessageCenter.MSG_KEY);
            if (message == null) {return;}

            if (message.fromUserId != observer.otherId) {return;}

            observer.recMsgs.add(message);
            intermittentRefresh();
        }
    };

    private RPC.Response<MessageBiz.Message> sendMsgCallback = new RPC.Response<MessageBiz.Message>(){
        @Override
        public void onStart() {
            super.onStart();

            UILoading.show(getActivity());
        }

        @Override
        public void onSuccess(MessageBiz.Message message) {
            super.onSuccess(message);

            latestSendMessage.latitude = (float)LBService.shareInstance().getLatestLatitude();
            latestSendMessage.longitude = (float)LBService.shareInstance().getLatestLongitude();
            latestSendMessage.message = message.content;

            refreshMapZoom();
        }

        @Override
        public void onFailure(Exception e) {
            super.onFailure(e);
        }

        @Override
        public void onFinish() {
            super.onFinish();
            UILoading.dismiss(getActivity());
        }
    };

    private void sendMessage(String msg) {
        MessageCenter.shareInstance().sendMessage(msg,otherId,sendMsgCallback);
    }

    private boolean hasTimer;
    private String timer_key = "timer_key_" + this.hashCode();

    private int count;
    Clock.Listener timerListener = new Clock.Listener() {
        @Override
        public void fire(String flag) {
            if (recMsgs.size() <= 0) {
                count = 0;
                hasTimer = false;
                Clock.shareInstance().removeListener(timer_key);
                return;
            }

            if (count % MSG_SHOW_INTERVAL == 0) {
                MessageBiz.Message message = recMsgs.remove(0);
                if (message != null) {
                    latestReceiveMessage.message = message.content;
                    latestReceiveMessage.longitude = Float.parseFloat(message.longitude);
                    latestReceiveMessage.latitude = Float.parseFloat(message.latitude);

                    refreshMapZoom();//刷新显示
                }
            }

            count++;
            if (count>=MSG_SHOW_INTERVAL) {count = 0;}
        }
    };

    private void intermittentRefresh() {
        if (hasTimer) {
            return;
        }

        Clock.shareInstance().addListener(timerListener,timer_key);
    }

    private BitmapDescriptor getBitmapDescriptor(MapMarkPoint point) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = null;
        if (point.uid == UserCenter.shareInstance().UID()) {
            view = inflater.inflate(R.layout.send_msg_pop, null);

            ImageView icon = (ImageView)view.findViewById(R.id.icon_image);
            icon.setImageResource(UIDic.mascotResourceId(point.uid,true));
        } else {
            view = inflater.inflate(R.layout.receive_msg_pop, null);

            ImageView icon = (ImageView)view.findViewById(R.id.icon_image);
            icon.setImageResource(UIDic.mascotResourceId(point.uid, false));
        }

        TextView text = (TextView)view.findViewById(R.id.title_label);
        if (TextUtils.isEmpty(point.message)) {
            text.setVisibility(View.GONE);
        } else {
            text.setText(point.message);
        }

        return BitmapDescriptorFactory.fromBitmap(Res.bitmap(view));
    }

    /**
     * 根据位置展示头像，并且加载消息
     */
	public void refreshOverlay() {
		// add marker overlay
		LatLng llA = new LatLng(latestSendMessage.latitude, latestSendMessage.longitude);
		LatLng llB = new LatLng(latestReceiveMessage.latitude, latestReceiveMessage.longitude);


        BitmapDescriptor bdMe = getBitmapDescriptor(latestSendMessage);
        BitmapDescriptor bdOther = getBitmapDescriptor(latestReceiveMessage);

        if (marks == null) {

            //锚点设置（默认（0.5f, 1.0f）水平居中，垂直下对齐）
            MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdMe).zIndex(4).anchor(1.0f, 1.0f);//.draggable(true);
            ooA.animateType(MarkerAnimateType.grow);//生长动画

            MarkerOptions ooB = new MarkerOptions().position(llB).icon(bdOther).zIndex(5).anchor(0.0f, 1.0f);
            ooB.animateType(MarkerAnimateType.grow);//生长动画

            if (marks == null) {
                marks = new ArrayList<>();
                manager = new OverlayManager(mBaiduMap) {

                    @Override
                    public boolean onPolylineClick(Polyline polyline) {
                        return false;
                    }

                    @Override
                    public boolean onMarkerClick(final Marker marker) {
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

            mMarkerMe = (Marker)manager.getOverlayList().get(0);
            mMarkerOther = (Marker)manager.getOverlayList().get(1);


        } else {
            mMarkerMe.setPosition(llA);
            mMarkerMe.setIcon(bdMe);

            mMarkerOther.setPosition(llB);
            mMarkerOther.setIcon(bdOther);
        }

        manager.zoomToSpan();
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

        LBService.shareInstance().stop();
        MessageCenter.shareInstance().stopFrequency();

        Clock.shareInstance().removeListener(timer_key);
        // MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
        mMapView.onDestroy();
    }

    //发消息

}
