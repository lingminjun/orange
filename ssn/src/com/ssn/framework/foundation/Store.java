package com.ssn.framework.foundation;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

/**
 * Created by lingminjun on 15/11/6.
 * 负责文件存储
 */
public final class Store {

    private static RigidFactory<Store> _factory = null;
    private static RigidFactory<Store> newFactory() {
        RigidFactory.SingletonCreator<Store> creator = new RigidFactory.SingletonCreator() {
            @Override
            public Store onCreate(String key, Map params) {
                return new Store(key);//构建实例
            }
        };
        return new RigidFactory<Store>(creator);
    }

    /**
     * Store工程
     * @return
     */
    public static RigidFactory<Store> factory() {
        if (_factory != null) {return _factory;}
        synchronized (Store.class) {
            if (_factory == null) {
                _factory = newFactory();
            }
        }
        return _factory;
    }

    /**
     * 返回缓存目录
     * @return
     */
    public static Store caches() {
        return factory().get("caches");
    }

    /**
     * 返回临时目录
     * @return
     */
    public static Store temporary() {
        return factory().get("temp");
    }

    /**
     * 返回文件目录
     * @return
     */
    public static Store documents() {
        return factory().get("documents");
    }

    /**
     * 返回库目录
     * @return
     */
    public static Store library() {
        return factory().get("library");
    }

    private final static String STORE_FINDER = "/ssn_store/";
    private final static String STORE_TAIL = ".tail";
    private final static String STORE_READ_ONLY = "r";
    private final static String STORE_READ_WRITE = "rw";


    private String finder;//目录，不能包含“\ / : * ?”字符


    public Store(String finder) {
        this.finder = Res.context().getFilesDir() + STORE_FINDER + finder + "/";
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
        if (path == null || path.length() == 0) {return;}

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
        return accessData(key,false);
    }

    /**
     * 获取数据，不更新时效，若数据过期，仍然返回数据，并删除过期文件
     * @param key
     * @return
     */
    public byte[] accessData(String key) {
        return accessData(key,true);
    }


    /**
     * 移除存储数据
     * @param key
     */
    public void remove(String key) {
        String path = path(key);
        if (path == null || path.length() == 0) {return;}
        _remove(path);
    }


    ////////////////////////////////////////////////////////////
    /*********************私有实现******************************/
    ////////////////////////////////////////////////////////////

    private byte[] accessData(String key,boolean readonly) {

        String path = path(key);
        if (path == null || path.length() == 0) {return null;}

        byte[] data = null;
        try {
            data = _accessData(path,readonly);
        } catch (Throwable e) {}

        return data;
    }

    private void _store(String path,byte[] data, int expire) throws Exception {

        File file = new File(path);
        if (file.exists()) { //删除已存在文件
            file.delete();
        }

        RandomAccessFile out = new RandomAccessFile(file, STORE_READ_WRITE);
        out.write(data);
        out.close();

        String tail = path + STORE_TAIL;
        if (expire > 0) {
            RandomAccessFile exp = new RandomAccessFile(tail, STORE_READ_WRITE);
            exp.writeLong(now());//存储访问时间
            exp.writeLong(expire);//过期时长
            exp.close();
        } else {//方式包含隐私访问日志文件,存在时序和线程问题
            File tailFile = new File(tail);
            if (tailFile.exists()) {//存在tail文件
                tailFile.delete();
            }
        }
    }

    private void _remove(String path) {
        File file = new File(path);
        if (file.exists()) { //删除已存在文件
            file.delete();
        }

        String tail = path + STORE_TAIL;
        file = new File(tail);
        if (file.exists()) {
            file.delete();
        }
    }

    private byte[] _accessData(String path,boolean readonly) throws Exception {
        byte[] data = null;

        boolean isExpire = false;

        String tail = path + STORE_TAIL;
        File tailFile = new File(tail);

        if (tailFile.exists()) {//存在tail文件
            long now = now();
            RandomAccessFile exp = new RandomAccessFile(tailFile, (readonly?STORE_READ_ONLY:STORE_READ_WRITE));
            long latest = exp.readLong();//存储上次访问时间
            long expire = exp.readLong();//过期时长

            if (expire > 0 && now >= latest + expire) {//过期，删除文件
                exp.close();
                tailFile.delete();//删除tail
                isExpire = true;
            } else if (!readonly) {//更新访问时间
                exp.seek(0);//回到其实位置
                exp.writeLong(now);
                exp.close();
            }
        }

        File file = new File(path);
        if(file.exists()) {//文件存在再进行操作

            RandomAccessFile in = new RandomAccessFile(file, STORE_READ_ONLY);
            long len = in.length();
            data = new byte[(int) len];
            in.read(data);
            in.close();

            //删除原始数据
            if (isExpire) {//删除文件
                file.delete();
                if (!readonly) {//过期不再返回数据
                    data = null;
                }
            }
        }

        return data;
    }

    private String path(String key) {
        if (key == null || key.length() == 0) {return null;}

        String md5 = md5(key);
        if (md5 == null || md5.length() == 0) {return null;}

        String sub = md5.substring(0,2);

        String finder = this.finder + sub;

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

    /*
    public static void main(String var[]) {
        Store.caches().store("aaaa","dssssddss".getBytes(),1000);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        {//还能取到
            byte[] data = Store.caches().accessData("aaaa");
            if (data != null) {
                System.out.println("取到：" + new String(data));
            } else {
                System.out.println("没取到");
            }
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        {//任然能取到，但是过期
            byte[] data = Store.caches().accessData("aaaa");
            if (data != null) {
                System.out.println("取到：" + new String(data));
            } else {
                System.out.println("没取到");
            }
        }

        {//取不到
            byte[] data = Store.caches().accessData("aaaa");
            if (data != null) {
                System.out.println("取到：" + new String(data));
            } else {
                System.out.println("没取到");
            }
        }

        System.out.println("====================================");

        Store.caches().store("aaaa","dssssddss".getBytes(),1000);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        {//取到并更新时间
            byte[] data = Store.caches().data("aaaa");
            if (data != null) {
                System.out.println("取到：" + new String(data));
            } else {
                System.out.println("没取到");
            }
        }
        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        {//取到并更新时间
            byte[] data = Store.caches().data("aaaa");
            if (data != null) {
                System.out.println("取到：" + new String(data));
            } else {
                System.out.println("没取到");
            }
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        {//取不到
            byte[] data = Store.caches().data("aaaa");
            if (data != null) {
                System.out.println("取到：" + new String(data));
            } else {
                System.out.println("没取到");
            }
        }
    }
    */
}
