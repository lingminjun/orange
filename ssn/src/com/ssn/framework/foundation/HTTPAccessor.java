package com.ssn.framework.foundation;

import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import org.apache.http.*;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.CharArrayBuffer;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Created by lingminjun on 15/10/21.
 */
public final class HTTPAccessor {

    /**
     * 通用配置
     */
    private static int _cn_timeout               = 5000;//连接超时时间（毫秒）
    private static int _so_timeout               = 30000;//数据响应超时时间（毫秒）
    private static String _agent                 = "Android App";
    private static int _keep_alive               = 5000;//小于零禁用
    private static boolean _gzip                 = false;//是否使用gzip

    private static boolean _debug                = false;//连接超时时间（毫秒）

    private static long    _ntt_diff             = 0;//客户端-服务端时间差

//    private static String _device_token          = "";
    private static String _auth_token            = "";


    /**
     * 服务器返回的响应数据
     */
    public static final class ServerResponse {

        private int statusCode = 500;
        private Header[] responseHeaders;
        private String contentString;
        private long contentLength;

        public ServerResponse(HttpResponse response) {

            assert response!=null:"HttpResponse should not be null";
            try {
                if (response !=null){

                    InputStream inputStream=null;

                    this.statusCode = response.getStatusLine().getStatusCode();
                    this.responseHeaders=response.getAllHeaders();

                    HttpEntity httpEntity = response.getEntity();

                    this.contentLength = httpEntity.getContentLength();

                    inputStream = httpEntity.getContent();

                    Header contentType = httpEntity.getContentEncoding();
                    if (contentType != null) {
                        String value = contentType.getValue();
                        if (value != null && value.contains("gzip")) {
                            inputStream = new GZIPInputStream(inputStream);
                        }
                    }

                    InputStreamReader in = new InputStreamReader(inputStream, "utf-8");
                    StringBuilder sb = new StringBuilder();
                    char[] cs = new char[2048];
                    int size = 0;
                    while ((size = in.read(cs)) >= 0) {
                        sb.append(cs, 0, size);
                    }

                    this.contentString = sb.toString();

                    if (inputStream != null) {
                        inputStream.close();
                    }
                } else {
                    throw new NullPointerException("HTTP response is null. Please check availability of endpoint service.");
                }
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }catch(Exception e){
                e.printStackTrace();
            }finally{}//try

        }//ServiceResponse

        /**
         * Get status code of http response
         *
         * @return http status code
         */
        public int getStatusCode() {
            return statusCode;
        }//getStatusCode

        /**
         * @return HTTP response content as a string
         */

        public String getResponseString() {
            return contentString;
        }//getResponseInString

        /**
         * Get the response headers of the HTTP response.
         *
         * @return Array of response headers, in name-value pair in {@link org.apache.http.Header} objects.
         */
        public Header[] getResponseHeaders(){
            assert responseHeaders.length>0:"Response headers can not be null.";
            return responseHeaders;
        }//getResponseHeaders

        /**
         * Returns the content length of the HTTP response
         *
         * @return Content length
         */
        public long getResponseLength(){
            return contentLength;
        }//getResponseLengths


        @Override
        public String toString(){
            long length=150;
            length=contentLength+length;
            for(Header header:responseHeaders){
                length+=header.getName().length()+header.getValue().length()+3;
            }
            CharArrayBuffer buffer= new CharArrayBuffer((int)length);
            buffer.append("\nServiceResponse\n---------------\nHTTP Status: ");
            buffer.append(statusCode);
            buffer.append("\nHeaders: \n");
            for(Header header:responseHeaders){
                buffer.append(header.getName());
                buffer.append(" : ");
                buffer.append(header.getValue());
                buffer.append("\n");
            }

            buffer.append("Response body: \n");
            buffer.append(contentString);
            buffer.append("\n----------------\n");

            return buffer.toString();
        }//toString
    }

    /**
     * 请求方式
     */
    public static enum REST_METHOD {
        GET,POST,PUT,DELETE
    }

    /**
     * 参数编码方式
     * QUERY:值键对，通过&分割
     * JSON:
     */
    public static enum PARAM_ENCODE_TYPE {
        QUERY,JSON
    }

//    /**
//     * 认证权限
//     */
//    public static enum AUTH_LEVEL {
//        NONE,DEVICE,TOKEN
//    }

    /**
     * HTTP请求协议
     */
    public static interface HTTPRequest {
        public String methodURI();//请求地址 如 http://www.xxx.com/mothod
        public HashMap<String,Object> parameter();//返回参数，Object仅仅支持String
        public boolean auth();
        public REST_METHOD method();    //调用方法

        public PARAM_ENCODE_TYPE encode();//是否传输json文件
    }

    /**
     * 配置Accessor
     * @param connectTimeout 连接超时时间(ms)
     * @param requestTimeout 请求超时时间(ms)
     * @param aliveTimeout   请求保持时间(ms)
     */
    public static void configAccessor(int connectTimeout,int requestTimeout,int aliveTimeout) {
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

//    /**
//     * 设备权限认证
//     * @param token
//     */
//    public static void setDeviceToken(String token) {
//        _device_token = TR.string(token);
//    }
//
    /**
     * 用户权限认证
     * @param token
     */
    public static void setAuthToken(String token) {
        _auth_token = TR.string(token);
    }


    private static volatile boolean isSingleChannel = false;
    /**
     * 单通道同步调用，将阻塞所有其他请求，慎用
     * @param request
     * @return
     * @throws Exception
     */
    public static ServerResponse singleChannelAccess(HTTPRequest request) throws Exception {
        ServerResponse response = null;
        try {
            isSingleChannel = true;

            response = _access(request);
        } catch (Throwable e) {
            throw  e;
        } finally {
            isSingleChannel = false;
        }
        return response;
    }

    /**
     * 同步发起请求
     * @param request
     * @return
     * @throws Exception
     */
    public static ServerResponse access(HTTPRequest request) throws Exception {
        return _access(request);
    }

    /**
     * 若设置为单通道后，所有请求被锁住
     * @param request
     * @return
     * @throws Exception
     */
    public static ServerResponse _access(HTTPRequest request) throws Exception {
        if (isSingleChannel) {
            synchronized (HTTPAccessor.class) {
                return _accessIMP(request);
            }
        } else {
            return _accessIMP(request);
        }
    }

    public static ServerResponse _accessIMP(HTTPRequest request) throws Exception {
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

        HttpResponse response = null;

        //rest支持
        //http://zhidao.baidu.com/link?url=b_cXrtCsZFejBqBqL2jTzxu-lu-X5U7pzskAC2WVrkwAFf1-Pq3lPAc62t_i2hGOBhsHyM-7hlZT0JCPsf3Bbq


        HttpRequestBase httpRequest = null;

        if (request.method() == REST_METHOD.GET) {
            String url = URLHelper.URLResetQuery(request.methodURI(), parameter);
            httpRequest = new HttpGet(url);
        } else if (request.method() == REST_METHOD.POST) {
            httpRequest = new HttpPost(request.methodURI());
            try {

                StringEntity se = encodeParam(parameter,request.encode());
                if (request.encode() == PARAM_ENCODE_TYPE.JSON) {
                    se.setContentType("application/json");
                } else {
                    se.setContentType("application/x-www-form-urlencoded;charset=UTF-8");
                }

                ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(se);
            } catch (Throwable e) { }
        } else if (request.method() == REST_METHOD.PUT) {
            httpRequest = new HttpPut(request.methodURI());

            try {

                StringEntity se = encodeParam(parameter,request.encode());
                if (request.encode() == PARAM_ENCODE_TYPE.JSON) {
                    se.setContentType("application/json");
                } else {
                    se.setContentType("application/x-www-form-urlencoded;charset=UTF-8");
                }

                ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(se);
            } catch (Throwable e) { }
        } else if (request.method() == REST_METHOD.PUT) {
            String url = URLHelper.URLResetQuery(request.methodURI(), parameter);
            httpRequest = new HttpDelete(url);
        }

        //设置接收数据为json
        httpRequest.setHeader("Accept", "application/json");

        if (_keep_alive < 0) {
            httpRequest.setHeader("Connection", "close");
        }

        if (_gzip) {
            httpRequest.setHeader("Accept-Encoding", "gzip");
        }

        //认证支持
        if (request.auth() && !TextUtils.isEmpty(_auth_token)) {
            httpRequest.setHeader("Authorization", _auth_token);
        }

        Log.i("HTTP","\nstart:["+request.methodURI()+"]");

        try {
            response = hClient.execute(httpRequest);
        } catch (Throwable e) {//ConnectException IOException ConnectTimeoutException连接或者io异常，停止
            client.set(null);
            hClient.getConnectionManager().shutdown();
            throw e;
        }

        if (response == null) {
            return null;
        }

        int statusCode = response.getStatusLine().getStatusCode();

        //响应成功，获取时间或者ip
        if (statusCode == HttpStatus.SC_OK) {
            _ntt_diff = getNetTime(response);
        }

        //返回响应
        if (response != null) {
            ServerResponse serverResponse = new ServerResponse(response);

            String url = URLHelper.URLResetQuery(request.methodURI(), parameter);
            Log.i("HTTP","\nreq:["+url+"]\nres:["+serverResponse.toString()+"]");

            return serverResponse;
        }

        return null;
    }


    private static StringEntity encodeParam(HashMap<String,Object> params,PARAM_ENCODE_TYPE type) throws Exception {
        if (type == PARAM_ENCODE_TYPE.JSON) {
            String json = new JSONObject(params).toString();
            return new StringEntity(json, "utf-8");
        } else if (type == PARAM_ENCODE_TYPE.QUERY) {

            /*
            final List<NameValuePair> dataList = new ArrayList<>();
            Set<String> keys = params.keySet();
            for (String key : keys) {
                String value = (String)params.get(key);
                dataList.add(new BasicNameValuePair(key, value));
            }
            return new UrlEncodedFormEntity(dataList, "UTF-8");
            */

            String query = URLHelper.URLQueryString(params);
            return new StringEntity(query, "utf-8");
        }
        return null;
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

    public static String getHostIP(String host) {

        java.net.InetAddress x;
        try {
            x = java.net.InetAddress.getByName(host);
            String ip_devdiv = x.getHostAddress();//得到字符串形式的ip地址
//            Log.d("TAG", ip_devdiv);
            return ip_devdiv;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.d("TAG", "域名解析出错");
        } catch (Throwable e) {
            Log.d("TAG", "域名解析出错");
        }

        return null;
    }

}
