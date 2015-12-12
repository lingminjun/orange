package com.ssn.framework.foundation;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * Created by lingminjun on 15/12/12.
 */
public final class DB {

    public final static class Table {
        private String name;//表明
        private List<Column> columns;//列属性

        public final static class Column {
            private String name;    //名称
            private String fill;    //默认填充值，default value
            private String mapping; //数据迁移时用，如(prevTable.ColumnName + 1)
            private int type;       //类型
            private int level;      //主键
            private int index;      //索引
        }
    }


    private String dbName;
    private SQLiteDatabase db;
    public DB(String name) {
        dbName = name;
        db = Res.context().openOrCreateDatabase(dbName, Context.MODE_PRIVATE, null);
    }

    public void exec(String sql, Object[] bindArgs) {
        db.execSQL(sql,bindArgs);
    }
}
