package com.ssn.framework.coredata;


import android.text.TextUtils;
import android.util.Log;
import com.ssn.framework.foundation.APPLog;

import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by lingminjun on 16/3/19.
 * 实体生产器，仅限于主线程执行，否则将出现异常，若数据需要导入，必输放入只读数据
 */
public final class EntityGenerator {

    public static String genUUID(String bizKey, Class entityType, Object ... formatArgs) {
        if (bizKey == null) {
            throw new RuntimeException("请用传入业务的主键来做uuid");
        }

        StringBuilder builder = new StringBuilder(bizKey);
        if (entityType == null) {
            builder.append(".0");
        } else {
            builder.append("." + entityType.hashCode());
        }

        if (formatArgs != null && formatArgs.length > 0) {
            builder.append(".");
            for (Object object : formatArgs) {
                builder.append(object.hashCode() + "&");
            }
        }

        return builder.toString();
    }

    /**
     * 实体数据，
     * 注意：Entity暂时仅仅支持浅拷贝，从EntityGenerator中取出的对象其复合属性需要重新处理
     */
    public static interface Entity extends Serializable {
        /**
         * 每一个实例都应该有唯一主键，否则无法注册到发生器中
         * 请使用业务上的主键合成，建议调用EntityGenerator.genUUID生成uuid
         * @return
         */
        public String gen_uuid();

        /**
         * 从另外一个对象填充数据
         * @param other
         */
        public void gen_fill(Entity other);
    }

    /**
     * 唯一实例
     * @return
     */
    public static final EntityGenerator getInstance() {
        return Singleton.instance;
    }

    /**
     * 获取数据
     * @param uuid
     * @return
     */
    public Entity getEntityForUUID(String uuid) {
        return getEntity(uuid);
    }

    /**
     * 注册并生成数据实体
     * @param entity
     * @return
     */
    public Entity generatorEntity(Entity entity) {
        return registerEntity(entity,true);
    }

    /**
     * 注册并生成数据实体，从另一个对象
     * @param object
     * @param entityClass
     * @return
     */
    public Entity generatorEntityFromObject(Object object, Class entityClass) {
        return registerEntityFromObject(entityClass,object);
    }

    ////////////////////////////////////////////////////////////
    //私有实现
    ////////////////////////////////////////////////////////////
    private EntityGenerator () {}

    private static class Singleton {
        private static final EntityGenerator instance = new EntityGenerator();
    }

    private static class WeakEntity extends SoftReference<Entity> {

        private String _uuid;

        public WeakEntity(Entity r, ReferenceQueue<? super Entity> q) {
            super(r, q);
            _uuid = r.gen_uuid();
        }

        public String getUUID() {
            return _uuid;
        }
    }

    private ConcurrentHashMap<String,WeakEntity> _store = new ConcurrentHashMap<>();
    private ReferenceQueue<Entity> _gcQueue = new ReferenceQueue<Entity>();

    private Entity getEntity(String key) {
        WeakEntity weakEntity = _store.get(key);
        if (weakEntity == null) {return null;}

        Entity entity = weakEntity.get();
        if (entity != null) {
            return entity;
        }

        return null;
    }

    private static Entity cloneTo(Entity src) throws RuntimeException {

        ByteArrayOutputStream memoryBuffer = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        ObjectInputStream in = null;

        Entity dist = null;

        try {

            out = new ObjectOutputStream(memoryBuffer);
            out.writeObject(src);
            out.flush();
            in = new ObjectInputStream(new ByteArrayInputStream(memoryBuffer.toByteArray()));
            dist = (Entity) in.readObject();

        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (out != null)
                try {
                    out.close();
                    out = null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            if (in != null)

                try {
                    in.close();
                    in = null;
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
        }

        return dist;

    }

    private void lookEntityListProperty(Entity src,Field field) throws RuntimeException {
        List list = null;
        try {
            field.setAccessible(true);
            list =  (List)field.get(src);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (list == null || list.size() <=  0) {
            return;
        }

        Iterator it = list.iterator();
        int i = 0;
        while(it.hasNext()){
            Object obj = it.next();
            if (obj instanceof Entity) {
                //元素替换
                Entity nfd = registerEntity((Entity) obj, false);//将属性注册进入
                if (obj != nfd) {//替换回去
                    list.remove(i);
                    list.add(i, nfd);
                }
            }
            i++;
        }
    }

    private void lookEntityMapProperty(Entity src,Field field) throws RuntimeException {
        Map map = null;
        try {
            field.setAccessible(true);
            map =  (Map)field.get(src);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (map == null || map.size() <=  0) {
            return;
        }

        Iterator<Map.Entry> it = map.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry =  it.next();
            Object key = entry.getKey();
            Object obj = entry.getValue();
            if (obj instanceof Entity) {
                //元素替换
                Entity nfd = registerEntity((Entity) obj, false);//将属性注册进入
                if (obj != nfd) {//替换回去
                    map.put(key,nfd);
                }
            }
        }
    }

    private void lookEntityArrayProperty(Entity src,Field field) throws RuntimeException {
        Object array = null;
        try {
            field.setAccessible(true);
            array =  field.get(src);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (array == null) {
            return;
        }

        if (array instanceof Entity[]) {

            int len = Array.getLength(array);
            if (len <= 0) {
                return;
            }

            for (int i = 0; i < len; i++) {
                Entity obj = (Entity)Array.get(array,i);
                if (obj != null) {
                    //元素替换
                    Entity nfd = registerEntity(obj, false);//将属性注册进入
                    if (obj != nfd) {//替换回去
                        Array.set(array,i,nfd);
                    }
                }
            }
        }
    }

    private Entity lookEntityProperty(Entity src) throws RuntimeException {
        if (src == null){
            return src;
        }

        Class<?> objC = null;//

        try {
            objC = src.getClass();
        } catch (Throwable e) {
            objC = null;
            e.printStackTrace();
        }

        if (objC == null) {
            return src;
        }

        Field[] fV = objC.getDeclaredFields();
        for (Field field : fV) {

            //容器数据支持
            Class fieldClass = field.getType();
            if (List.class.isAssignableFrom(fieldClass)) {
                lookEntityListProperty(src,field);
                continue;
            } else if (Map.class.isAssignableFrom(fieldClass)) {
                lookEntityMapProperty(src, field);
                continue;
            } else if (fieldClass.isArray()) {
                lookEntityArrayProperty(src, field);
                continue;
            }

            if (!Entity.class.isAssignableFrom(fieldClass)) {
                continue;
            }

            //取Entity类型的属性
            Entity fd = null;
            try {
                field.setAccessible(true);
                fd =  (Entity)field.get(src);
            } catch (Throwable e) {
                e.printStackTrace();
            }

            if (fd == null) {continue;}

            Entity nfd = registerEntity(fd,false);//将属性注册进入
            if (nfd != fd) {//若地址不相等，则替换属性
                try {
                    field.set(src, nfd);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        return src;
    }

    private Entity registerEntity(Entity entity, boolean clone) {
        if (entity == null) {return null;}

        String uuid = entity.gen_uuid();

        if (TextUtils.isEmpty(uuid)) {return null;}

        Entity dist = getEntity(uuid);
        if (dist != null) {//数据已经存在，不需要clone
            try {
                dist.gen_fill(entity);
            } catch (Throwable e) {e.printStackTrace();}
            return dist;
        }

        Entity other = entity;
        if (clone) {//需要clone出新的对象
            try {
                other = cloneTo(entity);

                //检查属性 : 注意，此处是否可能出现循环递归（对象属性指向自己）？取决于clone是否真实完成
                lookEntityProperty(other);
            } catch (Throwable e) {e.printStackTrace();}
        }

        if (other != null) {//记录新的数据
            WeakEntity weakEntity = new WeakEntity(other,_gcQueue);
            _store.put(uuid,weakEntity);
        }

        return other;
    }

    private Entity registerEntityFromObject(Class entityClass, Object other) {
        if (entityClass == null || other == null) {return null;}

//        String uuid = entity.gen_uuid();
//
//        if (TextUtils.isEmpty(uuid)) {return null;}
//
//        Entity dist = getEntity(uuid);
//        if (dist != null) {//数据已经存在，不需要clone
//            try {
//                dist.gen_fill(entity);
//            } catch (Throwable e) {e.printStackTrace();}
//            return dist;
//        }
//
//        Entity other = entity;
//        if (clone) {//需要clone出新的对象
//            try {
//                other = cloneTo(entity);
//
//                //检查属性 : 注意，此处是否可能出现循环递归（对象属性指向自己）？取决于clone是否真实完成
//                lookEntityProperty(other);
//            } catch (Throwable e) {e.printStackTrace();}
//        }
//
//        if (other != null) {//记录新的数据
//            WeakEntity weakEntity = new WeakEntity(other,_gcQueue);
//            _store.put(uuid,weakEntity);
//        }
//
//        return other;
        return null;
    }

    private void removeEntity(String uuid) {
        WeakEntity weak = _store.get(uuid);

        if (weak == null) {
            return;
        }

        _store.remove(uuid);

        weak.clear();

        Log.e("GEN",uuid + "对象已经被GC回收");
    }

    private void clean() {
//        System.gc();

        WeakEntity se = null;
        synchronized (this) {
            while ((se = (WeakEntity) _gcQueue.poll()) != null) {
                removeEntity(se.getUUID());
            }
        }
    }

    ////////////////////////////////////////////
    //测试
    ////////////////////////////////////////////
    /*
    public static class TItem implements Entity {

        public long itemId;
        public String name;
        public String des;

        @Override
        public String gen_uuid() {
            return genUUID("" + itemId, TItem.class);
        }

        @Override
        public void gen_fill(Entity other) {
            if (other instanceof TItem) {
                itemId = ((TItem) other).itemId;
                name = ((TItem) other).name;
                des = ((TItem) other).des;
            }
        }
    }

    public static class TPriceInfo implements Entity {

        public long packageId;
        public float price;
        public float origin;

        @Override
        public String gen_uuid() {
            return genUUID("" + packageId, TPriceInfo.class);
        }

        @Override
        public void gen_fill(Entity other) {
            if (other instanceof TPriceInfo) {
                packageId = ((TPriceInfo) other).packageId;
                price = ((TPriceInfo) other).price;
                origin = ((TPriceInfo) other).origin;
            }
        }
    }

    public static class TPackage implements Entity {

        public long packageId;
        public String name;
        public List<TItem> items;
        public TPriceInfo priceInfo;

        @Override
        public String gen_uuid() {
            return genUUID("" + packageId, TPackage.class);
        }

        @Override
        public void gen_fill(Entity other) {
            if (other instanceof TPackage) {
                packageId = ((TPackage) other).packageId;
                name = ((TPackage) other).name;
                items = ((TPackage) other).items;
                priceInfo = ((TPackage) other).priceInfo;
            }
        }
    }

    public static void test_main(String arg[]) {

        {
            TItem itemA = new TItem();
            itemA.name = "商品A";
            itemA.itemId = 111;
            itemA.des = "商品描述yyyyy";

            TItem itemB = new TItem();
            itemB.name = "商品B";
            itemB.itemId = 112;
            itemB.des = "商品描述xxxxx";

            TPriceInfo priceInfo = new TPriceInfo();
            priceInfo.packageId = 111;
            priceInfo.price = 13.5f;
            priceInfo.origin = 11.4f;

            TPackage tPackage = new TPackage();
            tPackage.packageId = 111;
            tPackage.name = "包裹1";
            tPackage.items = new ArrayList<>();
            tPackage.items.add(itemA);
            tPackage.items.add(itemB);
            tPackage.priceInfo = priceInfo;

            TPackage tpt = (TPackage)EntityGenerator.getInstance().generatorEntity(tPackage);
            Log.e("GEN",tpt.items.get(0).name);
        }

        {
            TItem itemA = new TItem();
            itemA.name = "商品A";
            itemA.itemId = 111;
            itemA.des = "商品描述修改";

            TItem item = (TItem)EntityGenerator.getInstance().generatorEntity(itemA);
            Log.e("GEN",item.des);
        }

        {
            TItem item = (TItem)EntityGenerator.getInstance().getEntityForUUID(genUUID("112", TItem.class));
            item.des = "随便写点啥";
            Log.e("GEN",item.name);
        }

    }
    */
}
