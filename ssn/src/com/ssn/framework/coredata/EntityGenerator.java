package com.ssn.framework.coredata;


import android.text.TextUtils;
import com.ssn.framework.foundation.Res;

import java.io.*;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.HashMap;

/**
 * Created by lingminjun on 16/3/19.
 * 实体生产器，仅限于主线程执行，否则将出现异常，若数据需要导入，必输放入只读数据
 */
public final class EntityGenerator {

    public static String genUUID(String bizKey, String entityType, Object ... formatArgs) {
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

//    public Entity

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

    private HashMap<String,WeakEntity> _store = new HashMap<>();
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

    public static Entity cloneTo(Entity src) throws RuntimeException {

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
            } catch (Throwable e) {e.printStackTrace();}
        }

        if (other != null) {//记录新的数据
            WeakEntity weakEntity = new WeakEntity(other,_gcQueue);
            _store.put(uuid,weakEntity);
        }

        return other;
    }
}
