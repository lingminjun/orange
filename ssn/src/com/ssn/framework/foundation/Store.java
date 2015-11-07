package com.ssn.framework.foundation;

import android.text.TextUtils;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by lingminjun on 15/11/6.
 * 负责文件存储
 */
public final class Store {
    private final static String STORE_FINDER = "/ssn_store/";
    private final static String STORE_TAIL = ".tail";
    private final static String STORE_READ_ONLY = "r";
    private final static String STORE_READ_WRITE = "rw";


    private String finder;//目录，不能包含“\ / : * ?”字符


    public Store(String finder) {
        this.finder = finder;
    }

    /**
     * 存储数据
     * @param key 存储key
     * @param data 存储内容
     */
    public void store(String key, byte[] data) {
        store(key,data,0);
    }

    /**
     * 存储数据
     * @param key 存储的key
     * @param data 存储内容
     * @param expire 过期时间(毫秒)，小于等于零表示不过期
     * @throws IOException
     */
    public void store(String key, byte[] data, int expire) {
        if (data == null) {return;}

        String path = path(key);
        if (TextUtils.isEmpty(path)) {return;}

        try {
            _store(path,data,expire);
        } catch (Throwable e) {}
    }

    /**
     * 获取数据，更新时效，若数据过期，不返回数据，并删除过期文件
     * @param key
     * @return
     */
    public byte[] data(String key) {
        return accessData(key,true);
    }

    /**
     * 获取数据，不更新时效，若数据过期，仍然返回数据，并删除过期文件
     * @param key
     * @return
     */
    public byte[] accessData(String key) {
        return accessData(key,false);
    }




    ////////////////////////////////////////////////////////////
    /*********************私有实现******************************/
    ////////////////////////////////////////////////////////////

    private byte[] accessData(String key,boolean checkExpire) {

        String path = path(key);
        if (TextUtils.isEmpty(path)) {return null;}

        byte[] data = null;
        try {
            data = _accessData(path,checkExpire);
        } catch (Throwable e) {}

        return data;
    }

    private void _store(String path,byte[] data, int expire) throws Exception {
        RandomAccessFile out = new RandomAccessFile(path, STORE_READ_WRITE);
        out.write(data);
        out.close();

        if (expire > 0) {
            String tail = path + STORE_TAIL;
            RandomAccessFile exp = new RandomAccessFile(tail, STORE_READ_WRITE);
            exp.writeLong(now());//存储访问时间
            exp.writeLong(expire);//过期时长
            exp.close();
        }
    }

    private byte[] _accessData(String path,boolean checkExpire) throws Exception {
        byte[] data = null;

        boolean isExpire = false;

        if (checkExpire) {

            String tail = path + STORE_TAIL;
            File file = new File(tail);

            if (file.exists()) {//存在tail文件
                long now = now();
                RandomAccessFile exp = new RandomAccessFile(tail, STORE_READ_WRITE);
                long latest = exp.readLong();//存储上次访问时间
                long expire = exp.readLong();//过期时长

                if (now < latest + expire) {//过期，删除文件
                    exp.close();
                    isExpire = true;

                    //删除tail
                    file.delete();
                } else {
                    exp.seek(0);//回到其实位置
                    exp.writeLong(now);
                    exp.close();
                }
            }
        }

        File file = new File(path);

        if(file.exists()) {//文件存在再进行操作

            RandomAccessFile in = new RandomAccessFile(path, STORE_READ_WRITE);
            long len = in.length() + 1;
            data = new byte[(int) len];
            in.read(data);
            in.close();

            //删除原始数据
            if (isExpire) {//删除文件
                file.delete();

                if (checkExpire) {//过期不再返回数据
                    data = null;
                }
            }
        }

        return data;
    }

    private String path(String key) {
        if (TextUtils.isEmpty(key)) {return null;}

        String md5 = md5(key);
        if (TextUtils.isEmpty(md5)) {return null;}

        String sub = md5.substring(0,2);

        String finder = Res.context().getFilesDir() + STORE_FINDER + sub;

        File file = new File(finder);
        if (!file.exists()) {//判断文件夹是否存在,如果不存在则创建文件夹
            file.mkdirs();
        }

        return finder + "/" + md5;
    }

    private static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xff) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xff));
        }
        return hex.toString();
    }


    private static long now() {
        return System.currentTimeMillis();
    }
}
