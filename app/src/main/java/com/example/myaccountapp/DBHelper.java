package com.example.myaccountapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 4;
    private static final String DB_NAME = "account_daily.db";

    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE account (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "Title VARCHAR(20), " +
                "Date VARCHAR(20), " +
                "Money VARCHAR(20), " +
                "Category VARCHAR(10) DEFAULT '支出', " +
                "Year INTEGER, " +
                "Month INTEGER, " +
                "Day INTEGER, " +
                "MonthBudget DOUBLE DEFAULT 0.0, " +
                "Photo BLOB" +
                ");";
        db.execSQL(sql);
    }

    // 在 DBHelper 的 onUpgrade 方法中添加数据迁移逻辑
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            String sql = "ALTER TABLE account ADD COLUMN Category VARCHAR(10) DEFAULT '支出';";
            db.execSQL(sql);
        }
        if (oldVersion < 3) {
            String sql1 = "ALTER TABLE account ADD COLUMN Year INTEGER;";
            String sql2 = "ALTER TABLE account ADD COLUMN Month INTEGER;";
            String sql3 = "ALTER TABLE account ADD COLUMN Day INTEGER;";
            String sql4 = "ALTER TABLE account ADD COLUMN MonthBudget DOUBLE DEFAULT 0.0;";
            db.execSQL(sql1);
            db.execSQL(sql2);
            db.execSQL(sql3);
            db.execSQL(sql4);

            // 数据迁移：从 Date 字段提取 Year、Month 和 Day
            String updateSql = "UPDATE account SET " +
                    "Year = CASE " +
                    "    WHEN Date LIKE '____-__-__' THEN CAST(SUBSTR(Date, 1, 4) AS INTEGER) " +
                    "    ELSE 0 " +
                    "END, " +
                    "Month = CASE " +
                    "    WHEN Date LIKE '____-__-__' THEN CAST(SUBSTR(Date, 6, 2) AS INTEGER) " +
                    "    ELSE 0 " +
                    "END, " +
                    "Day = CASE " +
                    "    WHEN Date LIKE '____-__-__' THEN CAST(SUBSTR(Date, 9, 2) AS INTEGER) " +
                    "    ELSE 0 " +
                    "END " +
                    "WHERE Date IS NOT NULL;";
            db.execSQL(updateSql);
        }
        if (oldVersion < 4) {
            String sql = "ALTER TABLE account ADD COLUMN Photo BLOB;";
            db.execSQL(sql);
        }
    }

}