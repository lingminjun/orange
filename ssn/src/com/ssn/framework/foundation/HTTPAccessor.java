package com.ssn.framework.foundation;

import android.os.Looper;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by lingminjun on 15/10/21.
 */
public final class HTTPAccessor {

    /**
     * 通用配置
     */
    private static long _cn_timeout              = 5000;//连接超时时间（毫秒）
    private static long _so_timeout              = 30000;//数据响应超时时间（毫秒）
    private static String _agent                 = "android";
    private static long _keep_alive              = 5000;//小于零禁用
    private static boolean _gzip                 = false;//是否使用gzip

    private static boolean _debug                = false;//连接超时时间（毫秒）

    private static long    _ntt_diff             = 0;//客户端-服务端时间差

    /**
     * HTTP请求协议
     */
    public static interface HTTPRequest {
        public String methodURI();//请求地址 如 http://www.xxx.com/mothod
        public HashMap<String,Object> parameter();//返回参数，Object仅仅支持String或者List<String>
        public boolean forcePost();//强制post
    }

    /**
     * 配置Accessor
     * @param connectTimeout 连接超时时间(ms)
     * @param requestTimeout 请求超时时间(ms)
     * @param aliveTimeout   请求保持时间(ms)
     */
    public static void configAccessor(long connectTimeout,long requestTimeout,long aliveTimeout) {
        _cn_timeout = connectTimeout;
        _so_timeout = requestTimeout;
        _keep_alive  = aliveTimeout;
    }

    /**
     * 设置
     * @param agent
     */
    public static void setUserAgent(String agent) {
        _agent = agent;
    }

    /**
     * 是否使用gzip
     * @param use
     */
    public static void setGZIP(boolean use) {
        _gzip = use;
    }

    /**
     * 用于调试
     * @param debug
     */
    public static void setDEBUG(boolean debug) {
        _debug = debug;
    }

    /**
     * 同步发起请求
     * @param request
     * @return
     * @throws Exception
     */
    public static HttpResponse access(HTTPRequest request) throws Exception {
        if (_debug) {
            if (Looper.myLooper() != null && Looper.myLooper() == Looper.getMainLooper()) {
                System.exit(-1);
                throw new RuntimeException("Don't request the api in the UI thread");
            }
        }

        if (request == null) {
            return null;
        }

        DefaultHttpClient hClient = getHttpClient();

        HashMap<String,Object> parameter = request.parameter();
        String params = URLHelper.URLQueryString(parameter);

//        String cid = "" + (long) (Math.random() * 10000000000L);
        HttpResponse response = null;

        boolean bigThan200 = params.length() > 200;
        HttpRequestBase httpRequest = null;
        if (bigThan200 || request.forcePost()) {
            httpRequest = new HttpPost(request.methodURI());

            try {
                StringEntity se = new StringEntity(params, "utf-8");
                se.setContentType("application/x-www-form-urlencoded;charset=UTF-8");
                ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(se);
            } catch (Throwable e) {
            }

        } else {
            String url = URLHelper.URLResetQuery(request.methodURI(), parameter);
            httpRequest = new HttpGet(url);
        }

        if (_keep_alive < 0) {
            httpRequest.setHeader("Connection", "close");
        }

        if (_gzip) {
            httpRequest.setHeader("Accept-Encoding", "gzip");
        }

        try {
            response = hClient.execute(httpRequest);
        } catch (Throwable e) {//ConnectException IOException ConnectTimeoutException连接或者io异常，停止
            client.set(null);
            hClient.getConnectionManager().shutdown();
            throw e;
        }


        int statusCode = response.getStatusLine().getStatusCode();

        //响应成功，获取时间或者ip
        if (statusCode == HttpStatus.SC_OK) {
            _ntt_diff = getNetTime(response);
        }

        return response;
    }


    public static long getNetTime() {
        long c_time = System.currentTimeMillis();
        return c_time - _ntt_diff;
    }

    /**
     * 从请求中读取响应时间
     * @param response
     * @return
     */
    private static long getNetTime(HttpResponse response) {

        Header header = response.getFirstHeader("Date");

        try {
            //获取网站回应请求的日期时间。如： Wed, 08 Feb 2012 06:34:58 GMT
            String dateString = header.getValue();

            SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z");

            try {
                long c_time = System.currentTimeMillis();
                Date date = sdf.parse(dateString);
                long s_time = date.getTime();

                return c_time - s_time;
            } catch (Throwable e) {}

        } catch (Throwable ex) {}

        return 0;
    }

    private static ThreadLocal<DefaultHttpClient> client = new ThreadLocal<DefaultHttpClient>();
    private static DefaultHttpClient getHttpClient() {
        DefaultHttpClient hClient = client.get();
        if (hClient == null) {
            hClient = new DefaultHttpClient();
            HttpParams p = hClient.getParams();
            p.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, _cn_timeout);
            p.setParameter(CoreConnectionPNames.SO_TIMEOUT, _so_timeout);
            p.setBooleanParameter(CoreProtocolPNames.USE_EXPECT_CONTINUE, false);
            p.setParameter(CoreProtocolPNames.USER_AGENT, _agent);
            hClient.setCookieStore(null);
            if (_keep_alive > 0) {
                hClient.setKeepAliveStrategy(new ConnectionKeepAliveStrategy() {

                    @Override
                    public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                        return _keep_alive;
                    }
                });
            }
            client.set(hClient);
        }
        return hClient;
    }

}
