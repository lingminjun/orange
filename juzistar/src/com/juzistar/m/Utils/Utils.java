package com.juzistar.m.Utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.*;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import com.juzistar.m.R;
import com.juzistar.m.Utils.cropimg.CropImage;
import com.juzistar.m.net.APIErrorMessage;
import com.ssn.framework.foundation.APPLog;
import com.ssn.framework.foundation.App;
import com.ssn.framework.foundation.Res;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.*;
import java.lang.reflect.Array;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//import com.sfht.m.app.entity.City;
//import com.sfht.m.app.entity.Province;
//import com.sfht.m.app.entity.Provinces;
//import com.sfht.m.app.entity.USER_RecAddressInfo;

/**
 * 一句话功能简述<br>
 * 功能详细描述
 *
 * @version 1.0
 * @author： RWJ
 * @date：202014/10/28 14:20
 */
public final class Utils /*extends CommonUtil*/ {

    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * 网络是否已经连接
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) {
            return false;
        }
        NetworkInfo networkinfo = manager.getActiveNetworkInfo();
        return networkinfo != null && networkinfo.isAvailable();
    }


    public static boolean IsSdExist() {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return true;
        }
        return false;
    }

    /**
     * rom或者sd卡，优先sd卡
     *
     * @return
     */
    public static String getFileCacheDir() {
        String dir;
        String cacheDir = "/sfht/fileCache/";
        if (IsSdExist()) {
            String sdPath = Environment.getExternalStorageDirectory().toString();
            sdPath += cacheDir;
            dir = sdPath;
        } else {
            dir = Res.context().getCacheDir() + cacheDir;
        }
        File file = new File(dir);
        if (!file.exists()) {
            file.mkdirs();
        }
        return dir;
    }

    /**
     * Generate a value suitable for use in {setId(int)}.
     * This value will not collide with ID values generated at build time by aapt for R.id.
     *
     * @return a generated ID value
     */
    public static int generateViewId() {
        for (; ; ) {
            final int result = sNextGeneratedId.get();
            // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
            int newValue = result + 1;
            if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
            if (sNextGeneratedId.compareAndSet(result, newValue)) {
                return result;
            }
        }
    }

    public static void genIdForView(View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            view.setId(Utils.generateViewId());
        } else {
            view.setId(View.generateViewId());
        }
    }

    public static long getServerTime() {
        return (System.currentTimeMillis() + 0/*HtRequest.shareInstance().NTDiff()*/);
    }

    public static Application getApplication() {
        return (Application)Res.context();
    }

    public static String longToDate(long time, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        return sdf.format(time);
    }


    public static final String PIC_DIR_ON_SD             = "/sfAlbum/";
    public static String getAlbumPath(Context context) {
        String path = null;
        if (IsSdExist()) {
            path = Environment.getExternalStorageDirectory().getAbsolutePath() + PIC_DIR_ON_SD;
        } else {
            path = context.getFilesDir().toString() + PIC_DIR_ON_SD;
        }
        return path;
    }

    /**
     * 获取指定大小的bitMap
     *
     * @param filePath
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap getSpecifiedSizeBitmap(String filePath, int reqWidth, int reqHeight) {//图片所在SD卡的路径
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);//自定义一个宽和高
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    //计算图片的缩放值
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;//获取图片的高
        final int width = options.outWidth;//获取图片的框
        int inSampleSize = 4;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        return inSampleSize;//求出缩放值
    }

    /**
     * 计算高斯模糊
     * @param sentBitmap
     * @param radius
     * @return
     */
    public static Bitmap fastblur(Bitmap sentBitmap, int radius) {
        Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

        if (radius < 1) {
            return (null);
        }

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pix = new int[w * h];
//        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.getPixels(pix, 0, w, 0, 0, w, h);

        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = radius + radius + 1;

        int r[] = new int[wh];
        int g[] = new int[wh];
        int b[] = new int[wh];
        int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
        int vmin[] = new int[Math.max(w, h)];

        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int temp = 256 * divsum;
        int dv[] = new int[temp];
        for (i = 0; i < temp; i++) {
            dv[i] = (i / divsum);
        }

        yw = yi = 0;

        int[][] stack = new int[div][3];
        int stackpointer;
        int stackstart;
        int[] sir;
        int rbs;
        int r1 = radius + 1;
        int routsum, goutsum, boutsum;
        int rinsum, ginsum, binsum;

        for (y = 0; y < h; y++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            for (i = -radius; i <= radius; i++) {
                p = pix[yi + Math.min(wm, Math.max(i, 0))];
                sir = stack[i + radius];
                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;

            for (x = 0; x < w; x++) {

                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (y == 0) {
                    vmin[x] = Math.min(x + radius + 1, wm);
                }
                p = pix[yw + vmin[x]];

                sir[0] = (p & 0xff0000) >> 16;
                sir[1] = (p & 0x00ff00) >> 8;
                sir[2] = (p & 0x0000ff);

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[(stackpointer) % div];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
            yp = -radius * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;

                sir = stack[i + radius];

                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];

                rbs = r1 - Math.abs(i);

                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;

                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }

                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                // Preserve alpha channel: ( 0xff000000 & pix[yi] )
                pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;

                stackstart = stackpointer - radius + div;
                sir = stack[stackstart % div];

                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];

                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                p = x + vmin[y];

                sir[0] = r[p];
                sir[1] = g[p];
                sir[2] = b[p];

                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];

                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;

                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];

                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];

                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];

                yi += w;
            }
        }

//        Log.e("pix", w + " " + h + " " + pix.length);
        bitmap.setPixels(pix, 0, w, 0, 0, w, h);
        return (bitmap);
    }

//    public static void toast(Context context, String content) {
//        if (context == null) {
//            return;
//        }
//        if (context instanceof Activity && ((Activity) context).isFinishing()) {
//            return;
//        }
//        Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
//    }
//

    public static void toastException(Exception e, String placeholder) {

        String msg = APIErrorMessage.message(e);
        if (msg == null || msg.isEmpty()) {
            msg = placeholder;
        }

        if (msg != null) {
            App.toast(msg);
        }
    }

    public static String parseStrToMd5L32(String str) {
        String reStr = null;
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes());
            StringBuffer stringBuffer = new StringBuffer();
            for (byte b : bytes) {
                int bt = b & 0xff;
                if (bt < 16) {
                    stringBuffer.append(0);
                }
                stringBuffer.append(Integer.toHexString(bt));
            }
            reStr = stringBuffer.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return reStr;
    }

    @Deprecated //凡是使用此接口的都需要注意了，每次打包obj被混淆或者修改造成UID不一致
    public static void saveSerializableToFile(Serializable obj, String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath));
        out.writeObject(obj);
    }

    @Deprecated //凡是使用此接口的都需要注意了，每次打包obj被混淆或者修改造成UID不一致
    public static <T> T readSerializable(String filePath) {
        if (!(new File(filePath)).exists()) {
            return null;
        }
        T t = null;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath));
            t = (T) in.readObject();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return t;
    }


//    public static void downLoadFile(String url, String filePath, final HtAsyncWorkViewCB<File> callBack) {
//        AjaxCallBack<File> ajaxCb = null;
//        if (callBack != null) {
//            ajaxCb = new AjaxCallBack<File>() {
//                @Override
//                public void onStart() {
//                    super.onStart();
//                    callBack.onStart();
//                }
//
//                @Override
//                public void onLoading(long count, long current) {
//                    super.onLoading(count, current);
//                    callBack.onLoading(count, current);
//                }
//
//                @Override
//                public void onSuccess(File file) {
//                    super.onSuccess(file);
//                    callBack.onSuccess(file);
//                }
//
//                @Override
//                public void onFailure(Throwable t, int errorNo, String strMsg) {
//                    super.onFailure(t, errorNo, strMsg);
//                    callBack.onFailure(new Exception(t));
//                }
//
//                @Override
//                public void onFinish() {
//                    super.onFinish();
//                    callBack.onFinish();
//                }
//            };
//        }
//        File file = new File(filePath);
//        if (!file.getParentFile().exists()) {
//            file.getParentFile().mkdirs();
//        }
//        APPLog.info("download module url = " + url);
//        APPLog.info("download file to path = " + filePath);
//        getHttp().download(url, null, filePath, false, ajaxCb);
//    }


    public static int getVersionCode(Context context) {
        PackageManager pm = context.getPackageManager();//context为当前Activity上下文
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_INSTRUMENTATION);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi == null ? 0 : pi.versionCode;
    }

    public static String getVersionName(Context context) {
        PackageManager pm = context.getPackageManager();//context为当前Activity上下文
        PackageInfo pi = null;
        try {
            pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_INSTRUMENTATION);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return pi == null ? "" : pi.versionName;
    }

    public static int versionCompare(String version1, String version2) {
        if (TextUtils.isEmpty(version1) && TextUtils.isEmpty(version2)) {
            return 0;
        }

        if (TextUtils.isEmpty(version1)) {
            return -1;
        }

        if (TextUtils.isEmpty(version2)) {
            return 1;
        }

        try {
            String[] v1s = version1.split("\\.");
            String[] v2s = version2.split("\\.");

            int l1 = v1s.length;
            int l2 = v2s.length;
            int max = l1 > l2 ? l1 : l2;

            for (int i = 0; i < max; i++) {
                String v1 = null;
                if (i < l1) {
                    v1 = v1s[i];
                }

                String v2 = null;
                if (i < l2) {
                    v2 = v2s[i];
                }

                if (TextUtils.isEmpty(v1) && TextUtils.isEmpty(v2)) {//说明是相等的
                    return 0;
                }

                if (TextUtils.isEmpty(v1)) {
                    return -1;
                }

                if (TextUtils.isEmpty(v2)) {
                    return 1;
                }

                int result = v1.compareTo(v2);
                if (result == 0) {
                    continue;
                } else {
                    return result;
                }
            }
        } catch (Throwable e) {
            APPLog.error(e);
        }

        return 0;
    }

    /**
     * 比较两个字符串是否相等，空字符和null视为相等
     *
     * @param a 可空
     * @param b 可空
     * @return
     */
    public static boolean equalString(CharSequence a, CharSequence b) {
        if (TextUtils.isEmpty(a) && TextUtils.isEmpty(b)) {
            return true;
        } else if (!TextUtils.isEmpty(a) && !TextUtils.isEmpty(b) && TextUtils.equals(a, b)) {
            return true;
        } else {
            return false;
        }
    }

    //
//    public static void setListViewMaxItemL(BaseAdapter adapter, ListView listView, int count) {
//        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) listView.getLayoutParams();
//        View item = adapter.getView(0, null, listView);
//        item.measure(0, 0);
//        if (adapter.getCount() > count) {
//            params.height = (count * item.getMeasuredHeight()) + 2;
//        } else {
//            params.height = (adapter.getCount() * item.getMeasuredHeight()) + 2;
//        }
//    }
//
//    public static String getPicUrlWithSuf(String picPathNoHost, int w, int h) {
//        return WebUrlUtil.urlString(picPathNoHost,w,h,true);
//    }

    public static String getPicUrlSuffix(int w, int h) {
        StringBuilder sb = new StringBuilder("@");
        sb.append(w).append("w_");
        sb.append(h).append("h_1e_1c.jpeg");
        return sb.toString();
    }

    /**
     * @param path 文件保存路径
     */
    public static void writeImage(String path, Bitmap bitmap) {
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            file.createNewFile();

            //将Bitmap保存为png图片
            FileOutputStream out = new FileOutputStream(path);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }

    public static String compressPic(String picPathToCompress) {
        int uploadPicMaxWidth = 1242;
        int uploadPicMaxHeight = 2208;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picPathToCompress, options);
        int width = options.outWidth;
        int height = options.outHeight;
        if (width > uploadPicMaxWidth || height > uploadPicMaxHeight) {
            float rateWidth = width * 1f / uploadPicMaxWidth;
            float rateHeight = height * 1f / uploadPicMaxHeight;
            float rate = rateWidth > rateHeight ? rateWidth : rateHeight;
            width = (int) (width * 1f / rate);
            height = (int) (height * 1f / rate);
        }
        Bitmap bp = getSpecifiedSizeBitmap(picPathToCompress, width, height);
        File fileToCom = new File(picPathToCompress);
        String resultPath = getAlbumPath(Res.context()) + "com_" + fileToCom.getName();
        return compress(100, bp, resultPath);
    }

    public static String compressPic(String picPathToCompress, int quality) {
        Bitmap photo = BitmapFactory.decodeFile(picPathToCompress);
        File fileToCom = new File(picPathToCompress);
        String resultPath = getAlbumPath(Res.context()) + "com_" + fileToCom.getName();
        return compress(quality, photo, resultPath);
    }

    /**
     * 返回压缩完成后的文件path
     *
     * @param quality
     * @param photo
     * @param resultPath
     * @return
     */
    private static String compress(int quality, Bitmap photo, String resultPath) {
        File file = new File(resultPath);
        try {
            file.createNewFile();
            FileOutputStream fOut = new FileOutputStream(file);
            if (photo.compress(Bitmap.CompressFormat.JPEG, quality, fOut)) {
                fOut.flush();
                fOut.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return resultPath;
    }

    public static <T> T[] jsonArrayToArray(Class<T> type, JSONArray jsonArray) {
        T[] t = (T[]) Array.newInstance(type, jsonArray.length());
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                t[i] = (T) jsonArray.get(i);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return t;
    }


    public static void playVideo(Context context, String path) {
        if (TextUtils.isEmpty(path)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW);
        String ext = path.substring(path.lastIndexOf(".") + 1, path.length());
        String type = "video/" + ext;
        Uri name = Uri.parse(path);
        intent.setDataAndType(name, type);
        context.startActivity(intent);
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics dm = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public static void setListViewFullHeight(ListView listView) {
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }
        int totalHeight = 0;
        for (int i = 0, len = listAdapter.getCount(); i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
//            listItem.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));//测量结果偏小，原因不明
            int size = Integer.MAX_VALUE / 10;//先用该方式
            listItem.measure(View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(size, View.MeasureSpec.AT_MOST));
            // 统计所有子项的总高度
            int measuredHeight = listItem.getMeasuredHeight();
            totalHeight += measuredHeight;
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static boolean isEdtEmpty(EditText edt) {
        return edt.getText() == null || TextUtils.isEmpty(edt.getText().toString().trim());
    }
//

    /**
     * 匹配正则表达式
     *
     * @param pattern
     * @param content
     * @return
     */
    public static boolean matchPattern(String pattern, String content) {
        Pattern p = Pattern.compile(pattern);
        Matcher m = p.matcher(content);
        return m.matches();
    }


//    public static FinalHttp getHttp() {
//        FinalHttp http = new FinalHttp();
//        http.configTimeout(8 * 1000);
//        return http;
//    }

    /**
     * 获取单个文件的MD5值
     *
     * @param file
     * @return
     */
    public static String getFileMD5(File file) {
        if (!file.isFile()) {
            return "";
        }
        MessageDigest digest = null;
        FileInputStream in = null;
        byte buffer[] = new byte[1024];
        int len;
        try {
            digest = MessageDigest.getInstance("MD5");
            in = new FileInputStream(file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
//        BigInteger bigInt = new BigInteger(1, digest.digest());
        String result = toHexString(digest.digest());
        if (result == null) {
            result = "";
        }
        return result;
//        return bigInt.toString(16);
    }

    private static final char[] hexArray = "0123456789abcdef".toCharArray();

    public static final String toHexString(byte[] bs) {
        if (bs == null) return null;
        char[] hexChars = new char[bs.length * 2];
        for (int i = 0; i < bs.length; i++) {
            int v = bs[i] & 0xff;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0f];
        }
        return new String(hexChars);
    }

//
//    /**
//     * 获取分页的总页数
//     *
//     * @param pageSize
//     * @param totalNum
//     * @return
//     */
//    public static int getTotalPageNum(int pageSize, long totalNum) {
//        int remain = (int) (totalNum % pageSize);
//        int i = (int) (totalNum / pageSize);
//        if (remain > 0) {
//            i++;
//        }
//        return i;
//    }
//
//    /**
//     * 获取用于展示的商品价格
//     *
//     * @return
//     */
//    public static String getDisplayPrice(long price) {
//        DecimalFormat df2 = new DecimalFormat("##0.00");
//        double dPrice = (double) price / 100;
//        String format = df2.format(dPrice);
//        if (format.endsWith(".00")) {
//            format = format.substring(0, format.length() - 3);
//        } else if (format.endsWith("0")) {
//            format = format.substring(0, format.length() - 1);
//        }
//        return format;
//    }
//
//    /**
//     * @param urlList json格式的url列表数组
//     * @return
//     */
//    public static String getFirstImgUrl(String urlList) {
//        try {
//            JSONArray array = new JSONArray(urlList);
//            return array.getString(0);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return "";
//    }
//
//    /**
//     * 根据index获取包邮包税策略
//     *
//     * @param index 正确取值1，2
//     * @return
//     */
//    public static String getPostageTaxByIndex(int index) {
//        String[] array = HTApplication.application.getResources().getStringArray(R.array.postage_tax);
//        return array[index - 1];
//    }
//
//    public static String getDisplayRecAddress(USER_RecAddressInfo addressInfo) {
//        StringBuilder sb = new StringBuilder();
//        sb.append(addressInfo.nationName).append(" ");
//        sb.append(addressInfo.provinceName).append(" ");
//        sb.append(addressInfo.cityName).append(" ");
//        sb.append(addressInfo.regionName).append(" ");
//        sb.append(addressInfo.detail);
//        return sb.toString();
//    }
//
//    /**
//     * 获取本地保存的省份信息
//     *
//     * @return
//     */
//    public static Provinces getProvinces() {
//        Provinces provinces = null;
//        try {
//            String content = loadProvinceFile();
//            JSONArray jsonArray = new JSONArray(content);
//            provinces = new Provinces();
//            provinces.provinces = new ArrayList<Province>();
//            for (int i = 0; i < jsonArray.length(); i++) {//省份列表
//                Province province = new Province();
//                JSONObject provinceJson = jsonArray.getJSONObject(i);
//                province.provinceName = provinceJson.optString("provinceName");
//                province.cities = new ArrayList<City>();
//                JSONArray citiesArray = provinceJson.optJSONArray("cities");
//                for (int c = 0; c < citiesArray.length(); c++) {//城市列表
//                    City city = new City();
//                    JSONObject cityJson = citiesArray.getJSONObject(c);
//                    city.cityName = cityJson.optString("cityName");
//                    city.zipCode = cityJson.optString("zipCode");
//                    city.regions = new ArrayList<String>();
//                    JSONArray regionsArray = cityJson.optJSONArray("regions");
//                    for (int r = 0; r < regionsArray.length(); r++) {//区列表
//                        city.regions.add(regionsArray.getString(r));
//                    }
//                    province.cities.add(city);
//                }
//                provinces.provinces.add(province);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//        return provinces;
//    }
//
//    private static String loadProvinceFile() throws IOException {
//        AssetManager assetManager = HTApplication.application.getAssets();
//        InputStream inputStream = assetManager.open("provinces.ll");
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        byte[] bytes = new byte[4096];
//        int len;
//        while ((len = inputStream.read(bytes)) > 0) {
//            byteArrayOutputStream.write(bytes, 0, len);
//        }
//        return new String(byteArrayOutputStream.toByteArray(), "UTF8");
//    }
//

    /**
     * 调用系统相机拍照
     *
     * @param filePath
     * @param activity
     * @param requestCode
     */
    public static void takePhoto(String filePath, Activity activity, int requestCode) {
        Intent intent = new Intent();
        // 指定开启系统相机的Action
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        // 根据文件地址创建文件
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        // 把文件地址转换成Uri格式
        Uri uri = Uri.fromFile(file);
        // 设置系统相机拍摄照片完成后图片文件的存放地址
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void takePhoto(String filePath, Fragment fragment, int requestCode) {
        Intent intent = new Intent();
        // 指定开启系统相机的Action
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        // 根据文件地址创建文件
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        // 把文件地址转换成Uri格式
        Uri uri = Uri.fromFile(file);
        // 设置系统相机拍摄照片完成后图片文件的存放地址
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        fragment.startActivityForResult(intent, requestCode);
    }

    /**
     * 从相册中选择图片
     *
     * @param activity
     * @param requestCode
     */
    public static void selPhoto(Fragment activity, int requestCode) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");

        activity.startActivityForResult(Intent.createChooser(intent, Res.localized(R.string.sel_pic)), requestCode);
    }

    public static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }

        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String getPathFromUri(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    public static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    public static void startPotoZoom(Fragment activity, Uri uri, int requestCode) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
// 设置裁剪
        intent.putExtra("crop", "true");
// aspectX aspectY 是宽高的比例

        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
// outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 340);
        intent.putExtra("outputY", 340);
        intent.putExtra("return-data", true);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startPhotoZoom(Activity context, String path, int requestCode) {
        Intent intent = new Intent(context, CropImage.class);
        intent.putExtra(CropImage.IMAGE_PATH, path);
        intent.putExtra(CropImage.SCALE, true);
        intent.putExtra(CropImage.ASPECT_X, 1);
        intent.putExtra(CropImage.ASPECT_Y, 1);
        context.startActivityForResult(intent, requestCode);
    }

    /**
     * 判断view是否包含坐标点
     *
     * @param view
     * @param rx
     * @param ry
     * @return
     */
    public static boolean isViewContains(View view, int rx, int ry) {
        int[] l = new int[2];
        view.getLocationOnScreen(l);
        int x = l[0];
        int y = l[1];
        int w = view.getWidth();
        int h = view.getHeight();
        if (rx < x || rx > x + w || ry < y || ry > y + h) {
            return false;
        }
        return true;
    }

    /**
     * 测量view
     *
     * @param view
     */
    public static void measure(View view) {
        int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(w, h);
    }


    /**
     * 环境更换后不需要删除应用
     *
     * @param key
     * @return
     */
//    private static String serverHost = null;
//
//    public static String preferenceSaveKey(String key) {
//        if (serverHost == null) {
//            synchronized (Utils.class) {
//                if (serverHost == null) {
//                    Uri uri = Uri.parse(GlobalConfig.getApiHost());
//                    serverHost = uri.getHost();
//                }
//            }
//        }
//        return key + "." + serverHost;
//    }

    /**
     * 判断是否是手机号
     *
     * @param num
     * @return
     */
    public static boolean isPhoneNum(String num) {
        Pattern pattern = Pattern.compile("^1[0-9]{10}$");
        return pattern.matcher(num).matches();
    }

    public static void copyToClipboard(String contentToCopy) {
        if (contentToCopy == null) {
            return;
        }
        ClipboardManager cmb = (ClipboardManager) Res.context().getSystemService(Activity.CLIPBOARD_SERVICE);
        ClipData myClip;
        String text = contentToCopy;
        myClip = ClipData.newPlainText("text", text);
        cmb.setPrimaryClip(myClip);
    }

    public static String getPicPathFromTakePhotoResult(Intent data,String tempPicName) {
        String result = "";
        if (data != null) {
            Bundle extras = data.getExtras();
            Uri uri = data.getData();
            if (extras == null && uri == null) {
                return result;
            }
            if (extras != null) {
                Bitmap photo = extras.getParcelable("data");
                String albumPath = Utils.getAlbumPath(Res.context());
                result = albumPath + tempPicName;
                Utils.writeImage(result, photo);
            } else if (uri != null) {
                result = Utils.getPathFromUri(Res.context(), uri);
            }
        }
        return result;
    }


    public static String getInputString(EditText editText) {
        Editable editable = editText.getText();
        if (editable != null) {
            return editable.toString().trim();
        }
        return "";
    }
}
