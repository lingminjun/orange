package com.juzistar.m.net;

/**
 * Created by lingminjun on 15/11/14.
 */
public class BoolModel extends BaseModel {
    public boolean success;
    public boolean exist;

    public boolean isTrue() {
        return success || exist;
    }
}
