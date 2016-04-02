package com.juzistar.m.application;

import android.content.res.XmlResourceParser;
import android.text.TextUtils;
import android.util.Log;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.juzistar.m.R;
import com.juzistar.m.biz.UserCenter;
import com.juzistar.m.biz.lbs.LBService;
import com.ssn.framework.coredata.EntityGenerator;
import com.ssn.framework.foundation.APPLog;
import com.ssn.framework.foundation.Res;
import com.ssn.framework.uikit.Navigator;
import com.ssn.framework.uikit.UIApplication;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Created by lingminjun on 15/9/26.
 */
public class AppAplication extends UIApplication {

    @Override
    protected void applicationDidLaunch() {
        super.applicationDidLaunch();

        //用户模块加载
        UserCenter.shareInstance().applicationDidLaunch(this);

        //页面路由加载
        loadPageRouter();

        LBService.shareInstance().asyncLocation(new BDLocationListener() {
            @Override
            public void onReceiveLocation(BDLocation bdLocation) {
                Log.e("定位","定位成功");
            }
        });
    }







    private static final String PAGE_ROUTER_NODE = "item";
    private static final String PAGE_ROUTER_URL = "url";
    private static final String PAGE_ROUTER_FRAGMENT = "fragmentclass";
    private static final String PAGE_ROUTER_ACTIVITY = "activityclass";
    private void loadPageRouter() {
        XmlPullParser xml = Res.resources().getXml(R.xml.page_router);

        Navigator navigator = Navigator.shareInstance();
        navigator.addScheme("http");
        navigator.addScheme("https");

        int eventType = -1;
        while (eventType != XmlResourceParser.END_DOCUMENT) {
            if (eventType == XmlResourceParser.START_TAG) {
                String strNode = xml.getName();
                if (strNode.equals(PAGE_ROUTER_NODE)) {

                    String url = xml.getAttributeValue(null, PAGE_ROUTER_URL);
                    String fragment_class_name = xml.getAttributeValue(null, PAGE_ROUTER_FRAGMENT);
                    String activity_class_name = xml.getAttributeValue(null, PAGE_ROUTER_ACTIVITY);
//                    String description = xml.getAttributeValue(null, "description");

                    Class fragment_class = null;
                    if (!TextUtils.isEmpty(fragment_class_name)) {
                        try {
                            fragment_class = (Class)Class.forName(fragment_class_name);
                        } catch (Throwable e) {
                            APPLog.error(e);
                            continue;
                        }
                    }

                    Class activity_class = null;
                    if (!TextUtils.isEmpty(activity_class_name)) {
                        try {
                            activity_class = (Class)Class.forName(activity_class_name);
                        } catch (Throwable e) {
                            APPLog.error(e);
                            continue;
                        }
                    }

                    navigator.addPageRouter(url,fragment_class,activity_class);

                }
            }
            try {
                eventType = xml.next();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
