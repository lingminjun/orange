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
import com.baidu.mapapi.model.LatLngBounds;
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
import com.ssn.framework.uikit.UIEvent;
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

    private View mBottomView;
    private ChatOverlayManager manager;

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

            latestSendMessage = new MapMarkPoint();
            latestSendMessage.uid = UserCenter.shareInstance().UID();
            latestSendMessage.latitude = LBService.shareInstance().getLatestLatitude();
            latestSendMessage.longitude = LBService.shareInstance().getLatestLongitude();
        }

        String sid = MessageCenter.Session.composedSessionID(UserCenter.shareInstance().UID(), otherId);

        //先获取未读的消息
        recMsgs.addAll(MessageCenter.shareInstance().getUnreadMessages(sid));
    }

    UILockScreenKeyboard.KeyboardListener keyboardListener = new UILockScreenKeyboard.KeyboardListener() {
        @Override
        public void onKeyboardDidLoad(UILockScreenKeyboard keyboard) {

        }

        @Override
        public void onSendButtonClick(UILockScreenKeyboard keyboard, View sender) {
            String msg = keyboard.text();
            sendMessage(msg);
            keyboard.dismiss();
            LBService.shareInstance().stop();
        }

        @Override
        public boolean onRightButtonClick(UILockScreenKeyboard keyboard, View sender) {
            return false;
        }

        @Override
        public void onScopeViewClick(UILockScreenKeyboard keyboard, View sender) {
            keyboard.dismiss();//直接隐藏
            LBService.shareInstance().stop();
        }

        @Override
        public void onCustomButtonClick(UILockScreenKeyboard keyboard, View sender, int buttonKey) {

        }

        @Override
        public void onKeyboardChanged(UILockScreenKeyboard keyboard, int newHeight, int oldHeight) {

        }

        @Override
        public void onKeyboardStatusChanged(UILockScreenKeyboard keyboard, boolean isShow) {
            if (!isShow) {
                keyboard.dismiss();//直接隐藏
                LBService.shareInstance().stop();
            }
        }
    };

    OnMarkerClickListener markClick = new OnMarkerClickListener() {
        @Override
        public boolean onMarkerClick(Marker marker) {
            return false;
        }
    };

    View.OnClickListener inputClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            UILockScreenKeyboard.show(getActivity(), keyboardListener, true);
            LBService.shareInstance().start();
        }
    };

    @Override
    public View loadView(LayoutInflater inflater) {
        View view = inflater.inflate(R.layout.map_chat_layout, null);

        mMapView = (MapView) view.findViewById(R.id.map_background_view);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setOnMarkerClickListener(markClick);
        mBaiduMap.setOnMapLoadedCallback(onMapLoadedCallback);

        mBottomView = view.findViewById(R.id.ssn_input_panel);
        mBottomView.setOnClickListener(UIEvent.click(inputClick));

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

        if (recMsgs != null && recMsgs.size() > 0) {
            intermittentRefresh();
        }
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

            latestSendMessage.latitude = LBService.shareInstance().getLatestLatitude();
            latestSendMessage.longitude = LBService.shareInstance().getLatestLongitude();
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
        MessageCenter.shareInstance().sendMessage(msg,otherId,latestReceiveMessage,sendMsgCallback);
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
                    latestReceiveMessage.longitude = Double.parseDouble(message.longitude);
                    latestReceiveMessage.latitude = Double.parseDouble(message.latitude);
                    latestReceiveMessage.msgId = message.id;

                    //更新session
                    MessageCenter.shareInstance().visiableSessionMessage(message);

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

        boolean isSend = false;
        if (point.uid == UserCenter.shareInstance().UID()) {
            view = inflater.inflate(R.layout.send_msg_pop, null);
            isSend = true;
        } else {
            view = inflater.inflate(R.layout.receive_msg_pop, null);
        }

        ImageView icon = (ImageView)view.findViewById(R.id.icon_image);
        icon.setImageResource(UIDic.mascotResourceId(point.uid,isSend));

        TextView text = (TextView)view.findViewById(R.id.title_label);
        text.setBackgroundResource(UIDic.dialogResourceId(point.uid,isSend));
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

        if (manager == null) {

            //锚点设置（默认（0.5f, 1.0f）水平居中，垂直下对齐）
            MarkerOptions ooA = new MarkerOptions().position(llA).icon(bdMe).zIndex(4).anchor(1.0f, 1.0f);//.draggable(true);
            ooA.animateType(MarkerAnimateType.grow);//生长动画

            MarkerOptions ooB = new MarkerOptions().position(llB).icon(bdOther).zIndex(5).anchor(0.0f, 1.0f);
            ooB.animateType(MarkerAnimateType.grow);//生长动画

            manager = new ChatOverlayManager(mBaiduMap,ooA,ooB);

        } else {
            manager.getMeMark().setPosition(llA);
            manager.getMeMark().setIcon(bdMe);

            manager.getOtherMark().setPosition(llB);
            manager.getOtherMark().setIcon(bdOther);
        }

        manager.zoomToSpan();
	}

    BaiduMap.OnMapLoadedCallback onMapLoadedCallback = new BaiduMap.OnMapLoadedCallback() {
        @Override
        public void onMapLoaded() {
            if (manager != null) {
                manager.zoomToSpan();//重新调整
            }
        }
    };

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
//		mMapView.onPause();
	}

	@Override
    public void onViewDidAppear() {
        super.onViewDidAppear();
		// MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
//		mMapView.onResume();
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

    private static class ChatOverlayManager extends OverlayManager {

        private List<OverlayOptions> marks;

        private Marker me;
        private Marker other;
        /**
         * 通过一个BaiduMap 对象构造
         *
         * @param baiduMap
         */
        public ChatOverlayManager(BaiduMap baiduMap, MarkerOptions meOverlay, MarkerOptions otherOverlay) {
            super(baiduMap);
            marks = new ArrayList<>();
            marks.add(meOverlay);
            marks.add(otherOverlay);

            this.addToMap();

            me = (Marker)getOverlayList().get(0);
            other = (Marker)getOverlayList().get(1);
        }

        public Marker getMeMark() {return me;}
        public Marker getOtherMark() {return other;}

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

        private static float LNG_OFFSET_1 = 0.015000f;
        private static float LNG_OFFSET_2 = 0.150000f;
        @Override
        public void zoomToSpan() {

            //比较谁在东，谁在西面，让后适当扩大经度
            LatLng meLL = me.getPosition();
            LatLng otherLL = other.getPosition();

            LatLng leftLL;
            LatLng rightLL;
            if (meLL.longitude > otherLL.longitude) {
                leftLL = new LatLng(otherLL.latitude,otherLL.longitude + LNG_OFFSET_1);
                rightLL = new LatLng(meLL.latitude,meLL.longitude - LNG_OFFSET_1);
            } else {//为了让消息显示完全
                leftLL = new LatLng(meLL.latitude,meLL.longitude + LNG_OFFSET_2);
                rightLL = new LatLng(otherLL.latitude,otherLL.longitude - LNG_OFFSET_2);
            }

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(leftLL);
            builder.include(rightLL);
            LatLngBounds bounds = builder.build();
            MapStatusUpdate status = MapStatusUpdateFactory.newLatLngBounds(bounds);

            getBaiduMap().setMapStatus(status);
        }
    }

}
