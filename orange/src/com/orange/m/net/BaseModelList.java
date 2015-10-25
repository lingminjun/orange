package com.orange.m.net;

import java.util.List;

/**
 * Created by lingminjun on 15/10/25.
 */
public class BaseModelList<T extends BaseModel> extends BaseModel {
    //分页信息
    public int size;//每页条数
    public int page;//当前页码 从0开始
    public int total;//数据总数

    public List<T> list;
}
