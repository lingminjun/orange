package com.juzistar.m.net;

import android.util.Log;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;

/**
 * Created by lingminjun on 15/10/25.
 */
public abstract class BaseModel implements Serializable {

    public BaseModel() {}

    public boolean fillFromJSON(JSONObject object) {
        return false;
    }

    public boolean fillFromOther(Object obj) {
        if (obj == null){
            return false;
        }

        Class<?> objC = null;//

        try {
            objC = Class.forName(obj.getClass().getName());
        } catch (Throwable e) {
            objC = null;
            e.printStackTrace();
        }

        if (objC == null) {
            return false;
        }

        Field[] fV = objC.getDeclaredFields();
        for (Field field : fV) {
            Field fieldToSet = null;
            try {
                fieldToSet = this.getClass().getDeclaredField(field.getName());
            } catch (Throwable e) {//忽略继续下一个
//                    e.printStackTrace();
                continue;
            }

            if (fieldToSet == null) {
                continue;
            }

            if (!(field.getType().equals(fieldToSet.getType()))) {

                //全部是基本类型，可以赋值，若出现短值域赋值给长值域的，请entity实现者注意重载setValue
                if (!field.getType().isPrimitive() || !fieldToSet.getType().isPrimitive()) {
                    continue;
                }
            }

            try {
                field.setAccessible(true);
                Object value = field.get(obj);
                fieldToSet.set(this, value);
            } catch (Throwable e) {
                Log.e("BaseSerialEntity", field.getType().getName() + " can not convert " + fieldToSet.getType().getName());
            }
        }

        return true;
    }

}
