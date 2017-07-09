package com.ssn.framework.coredata;


import android.text.TextUtils;
import android.util.Log;

import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
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
    public static abstract class Entity implements Serializable {
        /**
         * 每一个实例都应该有唯一主键，否则无法注册到发生器中
         * 请使用业务上的主键合成，建议调用EntityGenerator.genUUID生成uuid
         * uuid的值的因子不要取自Entity和容器(list,map,array)属性的值，否则容易发生错误
         * @return
         */
        public abstract String gen_uuid();

        /**
         * 从另外一个对象填充数据，重载者务必先调用super.gen_fill(other);方法
         * @param other 其他数据类型，仅仅填充属性
         */
        public void gen_fill(Object other) {
            fillEntity(this,other,0);//填充所有属性
        }
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
     * 注册对象
     * @param entityClass
     * @param object 若object是Entity类型，则clone出object对象，忽略传入entityClass类型
     * @return
     */
    public Entity generateEntity(Class entityClass, Object object) {
        return genEntity(entityClass,object);
    }

    /**
     * 注册并生成数据实体
     * @param entity
     * @return
     */
    public Entity generateEntity(Entity entity) {
        return genEntity(entity, true);
    }

    /**
     * 注册并生成数据实体，从另一个对象
     * @param object
     * @param entityClass
     * @return
     */
    public Entity generatorEntityFromObject(Object object, Class entityClass) {
        return genEntity(entityClass, object);
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

    /////////////////////////////////////////////////////////////////
    //Entity之前的转化
    /////////////////////////////////////////////////////////////////
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

    private static void lookEntityListProperty(Entity src,Field field) throws RuntimeException {
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
                Entity nfd = genEntity((Entity) obj, false);//将属性注册进入
                if (obj != nfd) {//替换回去
                    list.remove(i);
                    list.add(i, nfd);
                }
            }
            i++;
        }
    }

    private static void lookEntityMapProperty(Entity src,Field field) throws RuntimeException {
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
                Entity nfd = genEntity((Entity) obj, false);//将属性注册进入
                if (obj != nfd) {//替换回去
                    map.put(key,nfd);
                }
            }
        }
    }

    private static void lookEntityArrayProperty(Entity src,Field field) throws RuntimeException {
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
                    Entity nfd = genEntity(obj, false);//将属性注册进入
                    if (obj != nfd) {//替换回去
                        Array.set(array,i,nfd);
                    }
                }
            }
        }
    }

    private static void lookEntityProperty(Entity src) throws RuntimeException {
        if (src == null){
            return ;
        }

        Class<?> objC = null;//

        try {
            objC = src.getClass();
        } catch (Throwable e) {
            objC = null;
            e.printStackTrace();
        }

        if (objC == null) {
            return ;
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

            Entity nfd = genEntity(fd, false);//将属性注册进入
            if (nfd != fd) {//若地址不相等，则替换属性
                try {
                    field.set(src, nfd);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static Entity genEntity(Entity other, boolean clone) {
        if (other == null) {return null;}

        String uuid = other.gen_uuid();

        if (TextUtils.isEmpty(uuid)) {return null;}

        Entity dist = getInstance().getEntity(uuid);
        if (dist != null) {//数据已经存在，不需要clone
            try {
                dist.gen_fill(other);
            } catch (Throwable e) {e.printStackTrace();}
            return dist;
        }

        Entity cloneEntity = other;
        if (clone) {//需要clone出新的对象
            try {
                cloneEntity = cloneTo(other);

                //检查属性 : 注意，此处是否可能出现循环递归（对象属性指向自己）？取决于clone是否真实完成
                lookEntityProperty(cloneEntity);
            } catch (Throwable e) {e.printStackTrace();}
        }

        getInstance().registerEntity(cloneEntity);

        return cloneEntity;
    }

    /////////////////////////////////////////////////////////////////
    //Entity与其他对象之间的转化
    /////////////////////////////////////////////////////////////////
    private static Entity genEntity(Class entityClass, Object other) {
        if (entityClass == null || other == null) {return null;}

        if (!Entity.class.isAssignableFrom(entityClass)) {return null;}

        //1、若从Entity clone出来新的对象
        if (other instanceof Entity) {

            //不考虑other clone子类或者父类情况
//            if (entityClass.isAssignableFrom(other.getClass())) {}

            return genEntity((Entity)other,true);
        }

        //2、创建实体对象
        Entity entity = null;
        try {
            entity = (Entity)entityClass.newInstance();
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (entity == null) {
            Log.e("GEN","create entity failed!");
            return null;
        }

        boolean quiet = false;
        if (quiet) {
            //3、填充基本属性，获取uuid
            fillEntity(entity, other, 1);

            //4、获取uuid
            String uuid = entity.gen_uuid();
            if (TextUtils.isEmpty(uuid)) {
                Log.e("GEN", "get entity uuid failed! please implement the gen_uuid method.");
                return null;
            }

            //5、取得新的Entity实例，数据已经存在，则有新数据填充
            Entity dist = getInstance().getEntity(uuid);
            if (dist != null) {//数据已经存在，不需要clone
                try {
                    dist.gen_fill(other);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return dist;
            }

            //6、数据不存在，则注册数据
            getInstance().registerEntity(entity);

            //7、填充复杂属性
            fillEntity(entity, other, 2);
        } else {
            //直接填充属性
            entity.gen_fill(other);

            //4、获取uuid
            String uuid = entity.gen_uuid();
            if (TextUtils.isEmpty(uuid)) {
                Log.e("GEN", "get entity uuid failed! please implement the gen_uuid method.");
                return null;
            }

            //5、取得新的Entity实例，数据已经存在，则有新数据填充
            Entity dist = getInstance().getEntity(uuid);
            if (dist != null) {//数据已经存在，不需要clone
                try {
                    dist.gen_fill(other);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
                return dist;
            }

            //6、数据不存在，则注册数据
            getInstance().registerEntity(entity);
        }

        //8、返回entity
        return entity;
    }

    /**
     *
     * @param entity
     * @param other
     * @param flag flag:0表示全部属性，flag:1表示基本属性，flag:2表示复杂属性
     */
    private static void fillEntity(Entity entity, Object other, int flag) {

        if (entity == other) {
            return;
        }

        //1、若来源对象本身是Entity类型，则以other数据来取值，效率更高
//        if (other instanceof Entity) {
//            //TODO :
//            return;
//        }

        //
        Class entityClass = entity.getClass();
        Field[] fieldSets = entityClass.getDeclaredFields();
        Class otherClass = other.getClass();
        for (Field fieldSet : fieldSets) {

            //容器数据支持
            Class fieldClass = fieldSet.getType();
            if (List.class.isAssignableFrom(fieldClass)) {
                if (flag != 1) {
                    fillEntityListProperty(entity,other,fieldSet);
                }
                continue;
            } else if (Map.class.isAssignableFrom(fieldClass)) {
                if (flag != 1) {
                    fillEntityMapProperty(entity,other,fieldSet);
                }
                continue;
            } else if (fieldClass.isArray()) {
                if (flag != 1) {
                    fillEntityArrayProperty(entity,other,fieldSet);
                }
                continue;
            } else if (Entity.class.isAssignableFrom(fieldClass)) {//复合类型，后面再处理
                if (flag != 1) {
                    fillEntityProperty(entity,other,fieldSet);
                }
                continue;
            } else {
                if (flag == 2) {//仅仅复杂属性赋值
                    continue;
                }
            }

            //基本属性赋值（非Entity和容器类型的属性）
            Field fieldToGet = null;
            try {
                fieldToGet = otherClass.getDeclaredField(fieldSet.getName());
            } catch (Throwable e) {//忽略继续下一个
                continue;
            }

            if (fieldToGet == null) {
                continue;
            }

            //保证类型相等，基本类型除外，主要是为了兼容，如int和long之间的转换等等
            if (!(fieldSet.getType().equals(fieldToGet.getType()))) {

                //全部是基本类型，可以赋值，若出现短值域赋值给长值域的，请entity实现者注意重载setValue
                if (!fieldSet.getType().isPrimitive() || !fieldToGet.getType().isPrimitive()) {
                    continue;
                }
            }

            try {
                fieldSet.setAccessible(true);
                fieldToGet.setAccessible(true);
                Object value = fieldToGet.get(other);
                fieldSet.set(entity, value);
            } catch (Throwable e) {
                Log.e("GEN", "set entity error! " + fieldToGet.getType().getName() + " can not convert " + fieldSet.getType().getName());
            }
        }
    }

    private static void fillEntityListProperty(Entity entity, Object other, Field fieldSet) throws RuntimeException {

        //基本属性赋值（非Entity和容器类型的属性）
        Field fieldToGet = null;
        try {
            fieldToGet = other.getClass().getDeclaredField(fieldSet.getName());
        } catch (Throwable e) {//忽略继续下一个
            return;
        }

        if (fieldToGet == null) {
            return;
        }

        //目标对象并不是List类型
        if (!List.class.isAssignableFrom(fieldToGet.getType())) {
            return;
        }

        //获取目标值
        List list = null;
        try {
            fieldToGet.setAccessible(true);
            list =  (List)fieldToGet.get(other);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (list == null) {
            return;
        }

        Class elementClass = containedClassGenericType(fieldSet.getGenericType(),Entity.class);
        Object value = list;

        //若属性为List<Entity>，则特殊处理元素
        if (elementClass != null) {
            List lt = new ArrayList<Entity>();
            Iterator it = list.iterator();
            while(it.hasNext()){
                Object obj = it.next();
                Entity ent = genEntity(elementClass,obj);
                if (ent != null) {
                    lt.add(ent);
                }
            }
            value = lt;
        }

        try {
            fieldSet.setAccessible(true);
            fieldSet.set(entity, value);
        } catch (Throwable e) {
            Log.e("GEN", "set entity error! " + fieldToGet.getType().getName() + " can not convert " + fieldSet.getType().getName());
        }
    }

    private static void fillEntityMapProperty(Entity entity, Object other, Field fieldSet) throws RuntimeException {

        //基本属性赋值（非Entity和容器类型的属性）
        Field fieldToGet = null;
        try {
            fieldToGet = other.getClass().getDeclaredField(fieldSet.getName());
        } catch (Throwable e) {//忽略继续下一个
            return;
        }

        if (fieldToGet == null) {
            return;
        }

        //目标对象并不是List类型
        if (!Map.class.isAssignableFrom(fieldToGet.getType())) {
            return;
        }

        //获取目标值
        Map map = null;
        try {
            fieldToGet.setAccessible(true);
            map =  (Map)fieldToGet.get(other);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (map == null) {
            return;
        }

        Class elementClass = containedClassGenericType(fieldSet.getGenericType(),Entity.class);
        Object value = map;

        //若属性为Map<Object,Entity>，则特殊处理元素
        if (elementClass != null) {
            Map mp = new HashMap<Object,Entity>();
            Iterator<Map.Entry> it = map.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry entry = it.next();
                Object key = entry.getKey();
                Object obj = entry.getValue();
                Entity ent = genEntity(elementClass,obj);
                if (ent != null) {
                    mp.put(key, ent);
                }
            }

            value = mp;
        }


        try {
            fieldSet.setAccessible(true);
            fieldSet.set(entity, value);
        } catch (Throwable e) {
            Log.e("GEN", "set entity error! " + fieldToGet.getType().getName() + " can not convert " + fieldSet.getType().getName());
        }
    }

    private static void fillEntityArrayProperty(Entity entity, Object other, Field fieldSet) throws RuntimeException {

        //基本属性赋值（非Entity和容器类型的属性）
        Field fieldToGet = null;
        try {
            fieldToGet = other.getClass().getDeclaredField(fieldSet.getName());
        } catch (Throwable e) {//忽略继续下一个
            return;
        }

        if (fieldToGet == null) {
            return;
        }

        //目标对象并不是List类型
        if (!fieldToGet.getType().isArray()) {
            return;
        }

        //获取目标值
        Object array = null;
        try {
            fieldToGet.setAccessible(true);
            array =  fieldToGet.get(other);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (array == null) {
            return;
        }

        Class elementClass = fieldToGet.getType().getComponentType();

        Object value = array;

        //若属性为Map<Object,Entity>，则特殊处理元素
        if (Entity.class.isAssignableFrom(elementClass)) {

            int len = Array.getLength(array);
            if (len > 0) {
                Object ary = Array.newInstance(elementClass,len);

                for (int i = 0; i < len; i++) {
                    Object obj = Array.get(array,i);
                    if (obj != null) {
                        //元素替换
                        Entity ent = genEntity(elementClass, obj);
                        if (ent != null) {//替换回去
                            Array.set(ary,i,ent);
                        }
                    }
                }

                value = ary;
            }
        }


        try {
            fieldSet.setAccessible(true);
            fieldSet.set(entity, value);
        } catch (Throwable e) {
            Log.e("GEN", "set entity error! " + fieldToGet.getType().getName() + " can not convert " + fieldSet.getType().getName());
        }
    }

    private static void fillEntityProperty(Entity entity, Object other, Field fieldSet) throws RuntimeException {

        //基本属性赋值（非Entity和容器类型的属性）
        Field fieldToGet = null;
        try {
            fieldToGet = other.getClass().getDeclaredField(fieldSet.getName());
        } catch (Throwable e) {//忽略继续下一个
            return;
        }

        if (fieldToGet == null) {
            return;
        }

        //获取目标值
        Object object = null;
        try {
            fieldToGet.setAccessible(true);
            object =  fieldToGet.get(other);
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (object == null) {
            return;
        }

        Entity nty = genEntity(fieldSet.getType(),object);
        if (nty != null) {
            try {
                fieldSet.setAccessible(true);
                fieldSet.set(entity, nty);
            } catch (Throwable e) {
                Log.e("GEN", "set entity error! " + fieldToGet.getType().getName() + " can not convert " + fieldSet.getType().getName());
            }
        }
    }

    private static Class containedClassGenericType(Type genericFieldType, Class clazz) {
        if(genericFieldType instanceof ParameterizedType){
            ParameterizedType aType = (ParameterizedType) genericFieldType;
            Type[] fieldArgTypes = aType.getActualTypeArguments();
            for(Type fieldArgType : fieldArgTypes){
                Class fieldArgClass = (Class) fieldArgType;
                if (clazz.isAssignableFrom(fieldArgClass)) {
                    return fieldArgClass;
                }
            }
        }

        return null;
    }

    private void registerEntity(Entity entity) {
        String uuid = entity.gen_uuid();
        WeakEntity weakEntity = new WeakEntity(entity,_gcQueue);
        _store.put(uuid,weakEntity);

        Log.e("GEN", "Entity:"+uuid + " object registered generator");
    }

    private void removeEntity(String uuid) {
        WeakEntity weak = _store.get(uuid);

        if (weak == null) {
            return;
        }

        _store.remove(uuid);
        weak.clear();

        Log.e("GEN", "Entity:"+uuid + " object released");
    }

    private void clean() {
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
    public static class XXX {
        public String name;
    }
    public static class TItem {
        public List<String> list;
        public Map<String,XXX> xlt;
        public XXX[] ary;
    }
    public static void main(String arg[]) {

        TItem item = new TItem();

        Field[] fV = TItem.class.getDeclaredFields();
        for (Field field : fV) {
            field.setAccessible(true);

            if (field.getType().isArray()) {
                Class elementClass = field.getType().getComponentType();
                if (XXX.class.isAssignableFrom(elementClass)) {
                    XXX x = new XXX();
                    x.name = "ggggg";

                    Object[] ary = null;
                    try {
                        ary = (Object[])Array.newInstance(elementClass,1);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    ary[0] = x;

                    try {
                        field.set(item,ary);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            }
            else if (containedClassGenericType(field.getGenericType(), XXX.class) != null) {
                Map obj = new HashMap<Object,XXX>();
                XXX x = new XXX();
                x.name = "ggggg";
                obj.put("key", x);

                try {
                    field.set(item,obj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            else if (containedClassGenericType(field.getGenericType(), String.class) != null) {
                ArrayList obj = new ArrayList<String>();
                obj.add("000000");

                try {
                    field.set(item,obj);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

//            try {
//                Type genericFieldType = field.getGenericType();
//
//                if(genericFieldType instanceof ParameterizedType){
//                    ParameterizedType aType = (ParameterizedType) genericFieldType;
//                    Type[] fieldArgTypes = aType.getActualTypeArguments();
//                    for(Type fieldArgType : fieldArgTypes){
//                        Class fieldArgClass = (Class) fieldArgType;
//                        System.out.println("fieldArgClass = " + fieldArgClass);
//                    }
//                }
//
////                obj = clzz.newInstance();
//            } catch (Throwable e) {
//                e.printStackTrace();
//            }

        }
    }
    */
    /*
    public static class XItem {
        public long itemId;
        public String name;
        public String des;
    }

    public static class TItem extends Entity {

        public long itemId;
        public String name;
        public String des;

        @Override
        public String gen_uuid() {
            return genUUID("" + itemId, TItem.class);
        }

//        @Override
//        public void gen_fill(Object other) {
//            if (other instanceof TItem) {
//                itemId = ((TItem) other).itemId;
//                name = ((TItem) other).name;
//                des = ((TItem) other).des;
//            }
//        }
    }

    public static class XPriceInfo {

        public long packageId;
        public float price;
        public float origin;
    }

    public static class TPriceInfo extends Entity {

        public long packageId;
        public float price;
        public float origin;

        @Override
        public String gen_uuid() {
            return genUUID("" + packageId, TPriceInfo.class);
        }

//        @Override
//        public void gen_fill(Object other) {
//            if (other instanceof TPriceInfo) {
//                packageId = ((TPriceInfo) other).packageId;
//                price = ((TPriceInfo) other).price;
//                origin = ((TPriceInfo) other).origin;
//            }
//        }
    }

    public static class XPackage {
        public long packageId;
        public String name;
        public List<XItem> items;
        public XPriceInfo priceInfo;
    }

    public static class TPackage extends Entity {

        public long packageId;
        public String name;
        public List<TItem> items;
        public TPriceInfo priceInfo;

        @Override
        public String gen_uuid() {
            return genUUID("" + packageId, TPackage.class);
        }

//        @Override
//        public void gen_fill(Object other) {
//            super.gen_fill(other);
//            if (other instanceof TPackage) {
//                packageId = ((TPackage) other).packageId;
//                name = ((TPackage) other).name;
//                items = ((TPackage) other).items;
//                priceInfo = ((TPackage) other).priceInfo;
//            }
//        }
    }

    public static void test_main(String arg[]) {

        {
            XItem itemA = new XItem();
            itemA.name = "商品A";
            itemA.itemId = 111;
            itemA.des = "商品描述yyyyy";

            XItem itemB = new XItem();
            itemB.name = "商品B";
            itemB.itemId = 112;
            itemB.des = "商品描述xxxxx";

            XPriceInfo priceInfo = new XPriceInfo();
            priceInfo.packageId = 111;
            priceInfo.price = 13.5f;
            priceInfo.origin = 11.4f;

            XPackage tPackage = new XPackage();
            tPackage.packageId = 111;
            tPackage.name = "包裹1";
            tPackage.items = new ArrayList<>();
            tPackage.items.add(itemA);
            tPackage.items.add(itemB);
            tPackage.priceInfo = priceInfo;

            TPackage tpt = (TPackage)EntityGenerator.getInstance().generateEntity(TPackage.class,tPackage);
            Log.e("GEN",tpt.items.get(0).name);
        }

//        {
//            TItem itemA = new TItem();
//            itemA.name = "商品A";
//            itemA.itemId = 111;
//            itemA.des = "商品描述yyyyy";
//
//            TItem itemB = new TItem();
//            itemB.name = "商品B";
//            itemB.itemId = 112;
//            itemB.des = "商品描述xxxxx";
//
//            TPriceInfo priceInfo = new TPriceInfo();
//            priceInfo.packageId = 111;
//            priceInfo.price = 13.5f;
//            priceInfo.origin = 11.4f;
//
//            TPackage tPackage = new TPackage();
//            tPackage.packageId = 111;
//            tPackage.name = "包裹1";
//            tPackage.items = new ArrayList<>();
//            tPackage.items.add(itemA);
//            tPackage.items.add(itemB);
//            tPackage.priceInfo = priceInfo;
//
//            TPackage tpt = (TPackage)EntityGenerator.getInstance().generateEntity(tPackage);
//            Log.e("GEN",tpt.items.get(0).name);
//        }

        {
            TItem itemA = new TItem();
            itemA.name = "商品A";
            itemA.itemId = 111;
            itemA.des = "商品描述修改";

            TItem item = (TItem)EntityGenerator.getInstance().generateEntity(itemA);
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
