package com.ssn.framework.foundation;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import java.util.*;

/**
 * Created by BlackDev on 3/30/15.
 */
public class URLHelper {

    public static final String URI_SPLIT = "/";

    /**
     * 将params key value编码，其中Object 只能是String，或者是List<String>
     * @param params
     * @return URL Query string , values url encode(UTF-8)
     */
    private static final String ALLOWED_URI_CHARS = "";//"!*'();:@&=+$,/?%#[]";
    public static String URLQueryString(HashMap<String,Object> params) {

        if (params == null) {
            return "";
        }

        if (params.size() == 0) {
            return "";
        }

        String query = "";
        try {
            List<String> keys = new ArrayList<String>(params.keySet());
            Collections.sort(keys);

            Boolean first = true;
            for (String key : keys) {
                Object obj = params.get(key);
                if (obj instanceof String) {
                    if (TextUtils.isEmpty((String)obj)) {
                        continue;
                    }

                    if (first) {
                        first = false;
                        query = query + key + "=" + Uri.encode((String)obj, ALLOWED_URI_CHARS);//RFC-2396
//                        query = query + key + "=" + URLEncoder.encode((String)obj, "UTF-8");//RFC-1738
                    }
                    else  {
                        query = query + "&" + key + "=" + Uri.encode((String)obj, ALLOWED_URI_CHARS);//RFC-2396
//                        query = query + "&" + key + "=" + URLEncoder.encode((String)obj, "UTF-8");//RFC-1738
                    }
                }
                else if (obj instanceof List) {
                    List<String> values = new ArrayList<String>((List<String>)obj);
                    Collections.sort(values);
                    for (String value : values) {

                        if (TextUtils.isEmpty(value)) {
                            continue;
                        }

                        if (first) {
                            first = false;
                            query = query + key + "=" + Uri.encode(value, ALLOWED_URI_CHARS);//RFC-2396
//                            query = query + key + "=" + URLEncoder.encode(value, "UTF-8");//RFC-1738
                        }
                        else  {
                            query = query + "&" + key + "=" + Uri.encode(value, ALLOWED_URI_CHARS);//RFC-2396
//                            query = query + "&" + key + "=" + URLEncoder.encode(value, "UTF-8");//RFC-1738
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            APPLog.error(e.toString());
            query = "";
        }

        return query;
    }

    public static String trim(String stream, String characters) {//

        // null或者空字符串的时候不处理
        if (stream == null || stream.length() == 0 || characters == null || characters.length() == 0) {
            return stream;
        }

        int begin = 0;
        int end = stream.length();

        while (begin < end) {
            if (characters.contains(stream.substring(begin,begin+1))) {
                begin++;
            }
            else {
                break;
            }
        }

        String target = stream.substring(begin);
        if (target.length() <= 1) {
            return target;
        }

        end = target.length() - 1;
        while (end > 0) {
            if (characters.contains(target.substring(end,end+1))) {
                end--;
            }
            else {
                break;
            }
        }

        return target.substring(0,end + 1);
    }


    /**
     * 取URL query 中的参数
     * @param queryString 只要是key=value&key1=value2方式
     * @return
     */
    public static HashMap<String,Object> URLParams(String queryString,boolean decode) {

        if (queryString == null || TextUtils.isEmpty(queryString)) {
            return new HashMap<String, Object>();
        }

        HashMap<String, Object> params = new HashMap<String, Object>();

        String string = trim(queryString,"?#;!&");
        try {

            String[] comps = string.split("&");
            for (int i = 0; i < comps.length; i++) {
                String str = comps[i];
                if (TextUtils.isEmpty(str)) {
                    continue;
                }

                Object obj = params.get(str);

                if (!str.contains("=")) {
                    if (obj == null) {//非空的情况才能加入
                        params.put(str,"");
                    }
                }
                else {
                    String[] innr = str.split("=",2);
                    String key = innr[0];
                    String value = innr[1];//必然有元素

                    if (TextUtils.isEmpty(key)) {//key本身是非法的，直接不要了
                        continue;
                    }

                    if (decode) {
                        value = Uri.decode(value);//RFC-2396标准
//                        value = URLDecoder.decode(value,"UTF-8");//RFC-1738标准
                    }

                    if (obj == null) {
                        params.put(key,value);
                    }
                    else {
                        if (obj instanceof List) {
                            ((List<String>)obj).add(value);
                        }
                        else {
                            List<String> vls = new ArrayList<String>();
                            vls.add((String)obj);
                            vls.add(value);
                            params.put(key,vls);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            APPLog.error(e.toString());
        }

        return params;
    }

    /**
     * 取URL “<scheme>://<net_loc>/<path>;<params>?<query>” 内容，既#<fragment>以前的部分
     * @param url 只要是key=value&key1=value2方式
     * @return
     */
    public static String URLSource(String url) {
        if (!url.contains("#")) {
            return url;
        }

        int pos = url.indexOf("#");

        return url.substring(0,pos);
    }

    /**
     * 解析url中的query 参数 value url decode
     * @param url，注意此方法url必须是已经encode方法，否则可能出现解析参数出错
     * @return
     */
    public static HashMap<String,Object> URLQuery(String url) {
        if (url == null) {
            return new HashMap<String, Object>();
        }

        Uri uri = Uri.parse(url);
        if (uri == null) {
            return new HashMap<String, Object>();
        }

        HashMap<String, Object> params = null;// new HashMap<String, Object>();

        String queryString = uri.getEncodedQuery();
        return URLHelper.URLParams(queryString,true);
    }

    /**
     * 重置 url 的query
     * @param originURL 原始url
     * @param query query参数
     * @return
     */
    public static String URLResetQuery(String originURL, HashMap<String,Object> query) {
        return URLHelper.URLResetQuery(originURL,query,null);
    }

    /**
     * 重置 url 的query
     * @param originURL 原始url
     * @param query query参数，切记，参数采用明文传入，内部做encode
     * @param fragments 分段信息（采用&分割，切记，参数采用明文传入，内部做encode），可为空
     * @return
     */
    public static String URLResetQuery(String originURL, HashMap<String,Object> query, HashMap<String,Object> fragments) {
        if (TextUtils.isEmpty(originURL)) {
            return "";
        }

        Uri uri = Uri.parse(originURL);
        if (uri == null) {
            return "";
        }

        Uri.Builder builder = new Uri.Builder();
        String str = uri.getScheme();
        if (str != null) {
            builder.scheme(str);
        }

        str = uri.getEncodedAuthority();
        if (str != null) {
            builder.encodedAuthority(str);
        }

        str = uri.getHost();
        if (str != null) {
            int port = uri.getPort();
            if (port > 0) {
                str = str+":"+Integer.toString(port);
            }

            builder.encodedOpaquePart(str);
        }

        str = uri.getEncodedPath();
        if (str != null) {
            builder.encodedPath(str);
        }

        if (query != null) {
            str = URLHelper.URLQueryString(query);
        }
        else {
            str = uri.getEncodedQuery();
        }
        if (str != null) {
            builder.encodedQuery(str);
        }

        if (fragments != null) {
            str = URLHelper.URLQueryString(fragments);
        } else {
            str = uri.getEncodedFragment();
        }
        if (str != null) {
            if (!str.startsWith("!&")) {//新的协议支持
                str = "!&" + str;
            }
            builder.encodedFragment(str);
        }

        return builder.build().toString();
    }

    public static String URLReset(String originURL, String scheme, String host, int port) {
        if (TextUtils.isEmpty(originURL)) {
            return "";
        }

        Uri uri = Uri.parse(originURL);
        if (uri == null) {
            return "";
        }

        Uri.Builder builder = new Uri.Builder();
        String str = scheme != null ? scheme : uri.getScheme();
        if (str != null) {
            builder.scheme(str);
        }

        str = uri.getEncodedAuthority();
        if (str != null) {
            builder.encodedAuthority(str);
        }

        if (host != null) {
            str = host;
            if (port > 0) {
                str = str+":"+Integer.toString(port);
            }

            builder.encodedOpaquePart(str);
        }
        else {
            str = uri.getHost();
            if (str != null) {
                int aport = uri.getPort();
                if (aport > 0) {
                    str = str+":"+Integer.toString(aport);
                }
                builder.encodedOpaquePart(str);
            }
        }

        str = uri.getEncodedPath();
        if (str != null) {
            builder.encodedPath(str);
        }

        str = uri.getEncodedQuery();
        if (str != null) {
            builder.encodedQuery(str);
        }

        str = uri.getEncodedFragment();
        if (str != null) {
            if (!str.startsWith("!&")) {//新的协议支持
                str = "!&" + str;
            }
            builder.encodedFragment(str);
        }

        return builder.build().toString();
    }

    /**
     * 解析url中的fragment 参数 value url decode
     * @param url
     * @param decode
     * @return Object contains String, List<String> or ""
     */
    public static HashMap<String,Object> URLFragment(String url,boolean decode) {
        if (url == null) {
            return new HashMap<String, Object>();
        }

        Uri uri = Uri.parse(url);
        if (uri == null) {
            return new HashMap<String, Object>();
        }

        String fragment = uri.getEncodedFragment();//除非没有decode的fragment操作

        return URLParams(fragment,decode);
    }

    /**
     * url是否相等，忽略分段
     * @param urlOne
     * @param urlTwo
     * @return
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static boolean equalsURL(String urlOne, String urlTwo) {
        boolean isEqual = true;

        Uri uriOne = Uri.parse(urlOne);
        Uri uriTwo = Uri.parse(urlTwo);

        if (!uriOne.getScheme().equalsIgnoreCase(uriTwo.getScheme())) {
            isEqual = false;
            return isEqual;
        }
        if (!uriOne.getHost().equalsIgnoreCase(uriTwo.getHost())) {
            isEqual = false;
            return isEqual;
        }
        if (!uriOne.getPath().equalsIgnoreCase(uriTwo.getPath())) {
            isEqual = false;
            return isEqual;
        }
        if (uriOne.getPort() != (uriTwo.getPort())) {
            isEqual = false;
            return isEqual;
        }

        Set<String> keysOne = uriOne.getQueryParameterNames();
        Set<String> keysTwo = uriTwo.getQueryParameterNames();
        if (keysOne == null && keysTwo == null) {
            return isEqual;
        } else if ((keysOne != null && keysTwo == null) || (keysOne == null && keysTwo != null)) {
            return false;
        } else if (keysOne.equals(keysTwo)) {//比较内容
            for (String key : keysOne) {
                try {
                    List<String> valueOne = new ArrayList<String>(uriOne.getQueryParameters(key));
                    List<String> valueTwo = new ArrayList<String>(uriTwo.getQueryParameters(key));
                    Collections.sort(valueOne);
                    Collections.sort(valueTwo);
                    if (!valueOne.equals(valueTwo)) {
                        return false;
                    }
                } catch (Throwable e) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return isEqual;
    }

    public static boolean equalsHost(String url,String host) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        if (TextUtils.isEmpty(host)) {
            return false;
        }

        Uri uri = Uri.parse(url);
        if (uri == null) {
            return false;
        }

        String uri_host = uri.getHost();
        if (uri_host.equals(host)) {
            return true;
        }

        return false;
    }

    /**
     * 是否为有效url
     * @param url
     * @return 是否有效url
     */
    public static boolean isValidURL(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        }

        try {
            Uri uri = Uri.parse(url);
            if (uri != null) {
                String host = uri.getHost();
                if (TextUtils.isEmpty(host)) {
                    return false;
                }
                else {
                    return true;
                }
            }
            return false;
        }
        catch (Throwable e) {

        }
        return false;
    }

    /**
     * 获取url中除去host之后的path
     * @param url
     * @return
     */
    public static String getURLFinderPath(String url) {
        if (TextUtils.isEmpty(url)) {
            return null;
        }

        Uri uri = Uri.parse(url);
        if (uri == null) {return null;}

        String host = uri.getHost();
        if (TextUtils.isEmpty(host)) {
            return null;
        }

        List<String> paths = uri.getPathSegments();
        if (paths == null || paths.size() <= 0) {
            return null;
        }

        StringBuilder builder = new StringBuilder();
        boolean isFirst = true;
        for (String p : paths) {
            if (!TextUtils.isEmpty(p)) {
                if (isFirst) {
                    isFirst = false;
                }
                else {
                    builder.append(URI_SPLIT);
                }
                builder.append(p.toLowerCase());
            }
        }

        return builder.toString();
    }


    public static boolean isSameScheme(String url,final String scheme) {
        if (TextUtils.isEmpty(scheme) || TextUtils.isEmpty(url)) {return false;}

        Uri uri = Uri.parse(url);
        if (uri == null) {return false;}

        String urlScheme = uri.getScheme();

        return scheme.equalsIgnoreCase(urlScheme);
    }
}
