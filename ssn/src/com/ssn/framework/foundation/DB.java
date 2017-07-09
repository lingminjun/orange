package com.ssn.framework.foundation;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lingminjun on 15/12/12.
 */
public final class DB {

    /**
     * 字段类型定义
     */
    public final static class ColumnType {
        public static final int ColumnInt = Cursor.FIELD_TYPE_INTEGER;  //"Int" SQLITE_INTEGER;
        public static final int ColumnFloat = Cursor.FIELD_TYPE_FLOAT;  //"Float" SQLITE_FLOAT;
        public static final int ColumnBool = Cursor.FIELD_TYPE_INTEGER; //"Bool" SQLITE_INTEGER;
        public static final int ColumnBlob = Cursor.FIELD_TYPE_BLOB;    //"Blob" SQLITE_BLOB;
        public static final int ColumnText = Cursor.FIELD_TYPE_STRING;  //"Text" SQLITE_TEXT;
        public static final int ColumnNull = Cursor.FIELD_TYPE_NULL;    //"Null" SQLITE_NULL;
    }

    /**
     * 字段级别定义，是否为主键
     */
    public final static class ColumnLevel {                           //属性描述
        public static final int ColumnNormal = 0;  //一般属性(可为空)
        public static final int ColumnNotNull = 1; //"NotNull" 一般属性(不允许为空)
        public static final int ColumnPrimary = 2; //"Primary" 主键（不允许为空）,多个时默认形成联合组件
    }

    /**
     * 字段索引级别定义
     */
    public final static class ColumnIndex {
        public static final int ColumnNotIndex = 0;    //不需要索引
        public static final int ColumnNormalIndex = 1; //"Index" 索引（不允许为空）
        public static final int ColumnUniqueIndex = 2; //"Unique" 唯一索引（不允许为空）
    }

    /**
     * 表状态
     */
    public final static class TableStatus {
        public static final int TableNone = 0;  //表示数据表还不存在
        public static final int TableUpdate = 1; //表示数据表待更新
        public static final int TableOK = 2;     //数据表 已经 可以操作
    }
    /*
 JSON方式 实现table 版本管理
 json文件定义:与数据Column对应
 下面定义仅供参考，数据结构并非合理，主要注意type、level和index的值设置
 {
 "tb":"Person",
 "its":[{
            "vs":1,
            "cl":[  {"name":"uid",    "type":"Int",   "level":"Primary",  "fill":"",  "index":"Index",   "mapping":""},
                    {"name":"name",   "type":"Text",  "level":"NotNull",  "fill":"",  "index":"Unique",  "mapping":""},
                    {"name":"sex",    "type":"Bool",  "level":"",         "fill":"",  "index":"",        "mapping":""},
                    {"name":"height", "type":"Float", "level":"",         "fill":"",  "index":"",        "mapping":""},
                    {"name":"avatar", "type":"Blob",  "level":"",         "fill":"",  "index":"",        "mapping":""},
                    {"name":"other",  "type":"Null",  "level":"",         "fill":"",  "index":"",        "mapping":""}
                ]
        },
        {
            "vs":2,
            "cl":[  {"name":"uid",    "type":"Int",   "level":"Primary",  "fill":"",  "index":"Index",   "mapping":""},
                    {"name":"name",   "type":"Text",  "level":"NotNull",  "fill":"",  "index":"Unique",  "mapping":""},
                    {"name":"sex",    "type":"Bool",  "level":"",         "fill":"",  "index":"",        "mapping":""},
                    {"name":"height", "type":"Float", "level":"",         "fill":"",  "index":"",        "mapping":""},
                    {"name":"avatar", "type":"Blob",  "level":"",         "fill":"",  "index":"",        "mapping":""},
                    {"name":"mobile", "type":"Text",  "level":"",         "fill":"",  "index":"Index",   "mapping":""},
                    {"name":"other",  "type":"Null",  "level":"",         "fill":"",  "index":"",        "mapping":""}
                  ]
        }]
 }
 */
    public final static class Table {
        private DB _db;
        private String _name;//表名
        private List<Column> _columns;//列属性
        private int _status;
        private Table _meta;
        private int _vs;
        private Map<Integer,List<Column> > _its;

        public final static class Column {
            private String name;    //名称
            private String fill;    //默认填充值，default value
            private String mapping; //数据迁移时用，如(prevTable.ColumnName + 1)
            private int type;       //类型
            private int level;      //主键
            private int index;      //索引
        }

        /**
         * 普通表创建
         * @param db
         * @param jsonDescriptionFilePath
         */
        public Table(DB db, String jsonDescriptionFilePath) {
            this._db = db;
//            this.path = [path copy];

            // 1、检查并创建表日志表
            checkCreateTableLog();

            // 2、解析数据表描述
            _its = parseJSONForFilePath(jsonDescriptionFilePath);
            if (_its == null || _its.size() == 0) {
                throw new RuntimeException("db table json 解析失败！！");
            }

            // 3、缓存列名
//            [self peelcolumns];

            // 4、检查表状态
//            [self checkTableStatus];

            // 5、监听变化
//            [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(dbupdatedNotification:) name:SSNDBUpdatedNotification object:db];
        }

        /**
         * 模板表创建
         * @param jsonDescriptionFilePath
         */
        public Table(String jsonDescriptionFilePath) {

        }

        /**
         * 分表创建，分表名不能与模板表名一致
         * @param name
         * @param meta 模板表
         * @param db
         */
        public Table(String name, Table meta, DB db) {

        }

        public int status() { //表状态，非常重要的接口
            return _status;
        }

        public void update() { //创建数据表并升级到最新，使用者需要操作某个数据表时，一定要保证此方法已经被执行，此方法不放在初始化中调用主要用于多表集中迁移思路
        }
        public void drop() { //删除数据表
        }

        public String name() {return _name;}
        public DB db() {return _db;}
        public int currentVersion() {return _vs;}
        public Table meta() {return _meta;}
        public List<String> columnNames() {return null;}
        public List<String> primaryColumnNames() {return null;}

        //////////////////////////////////////////////////
        private void checkCreateTableLog() {

        }

        private Map<Integer,List<Column> > parseJSONForFilePath(String path) {

            String json = Res.jsonResource(path);
            if (json == null) {return null;}

            JSONObject jsonObject = null;
            Map<Integer,List<Column> > map = new HashMap<>();
            try {
                jsonObject = new JSONObject(json);

                _name = jsonObject.getString("tb");

                JSONArray its = jsonObject.getJSONArray("its");

                for (int i = 0; i < its.length(); i++) {
                    JSONObject object = its.getJSONObject(i);

                    int v = object.getInt("vs");
                    JSONArray cls = object.getJSONArray("cl");

                    List<Column> list = new ArrayList<>();
                    for (int j = 0; j < cls.length(); j++) {
                        JSONObject jsonCl = cls.getJSONObject(j);

                        // {"name":"uid",    "type":"Int",   "level":"Primary",  "fill":"",  "index":"Index",   "mapping":""},
                        String name = jsonCl.getString("name");
                        Object obj = jsonCl.get("type");
                    }
                }

            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            return map;
        }
    }

    private String _dbName;
    private SQLiteDatabase _db;
    public DB(String name) {
        _dbName = name;
        _db = Res.context().openOrCreateDatabase(name, Context.MODE_PRIVATE, null);
    }

    public void exec(String sql, Object[] bindArgs) {
        _db.execSQL(sql,bindArgs);
    }
}
