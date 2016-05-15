package com.example.gift;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DbHelper extends SQLiteOpenHelper {
    public static final String TABLE_NAME_MEMOS = "memos";
    public static final String TABLE_NAME_MEDIA = "media";
    public static final String COLUMN_NAME_ID = "_id";
    public static final String COLUMN_NAME_MEMO_NAME = "name";
    public static final String COLUMN_NAME_MEMO_CONTENT = "content";
    public static final String COLUMN_NAME_MEMO_DATE = "date";
    public static final String COLUMN_NAME_MEDIA_PATH = "path";
    public static final String COLUMN_NAME_MEDIA_OWNER_MEMO_ID = "memo_id";
    public DbHelper(Context context) {
        super(context, TABLE_NAME_MEMOS, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME_MEMOS + "(" + COLUMN_NAME_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_NAME_MEMO_NAME
                + " TEXT NOT NULL DEFAULT \"\"," + COLUMN_NAME_MEMO_CONTENT
                + " TEXT NOT NULL DEFAULT \"\"," + COLUMN_NAME_MEMO_DATE
                + " TEXT NOT NULL DEFAULT \"\"" + ")");
        db.execSQL("CREATE TABLE " + TABLE_NAME_MEDIA + "(" + COLUMN_NAME_ID
                + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME_MEDIA_PATH + " TEXT NOT NULL DEFAULT \"\","
                + COLUMN_NAME_MEDIA_OWNER_MEMO_ID
                + " INTEGER NOT NULL DEFAULT 0" + ")");



    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
