package com.aver.superdirector.utility;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AccountLogHelper extends SQLiteOpenHelper {
    private final static String TAG = CallLogHelper.class.getName();

    private final static int DATABASE_VERSION = 1;
    private final static String DATABASE_NAME = "avervideoroom.db";

    // Table Name
    private final static String TABLE_NAME         = "account_log";
    private final static String FIELD_ID = "_id";
    private final static String FIELD_NAME    = "AccountName";
    private final static String FIELD_PROTOCOL        = "Protocol";
    private final static String FIELD_SRTP        = "SRTP";
    private final static String FIELD_PORT           = "Port";
    private final static String FIELD_USERNAME       = "UserName";
    private final static String FIELD_PASSWORD       = "Password";
    private final static String FIELD_REGISTERSERVER = "RegisterServer";
    private final static String FIELD_PROXYSERVER    = "ProxyServer";
    private final static String FIELD_SERVERID       = "ServerID";
    private final static String FIELD_MEETID       = "MeetId";

    private static final String[] PROJECTION = new String[] {
        FIELD_ID,
        FIELD_NAME,
        FIELD_PROTOCOL,
        FIELD_SRTP,
        FIELD_PORT,
        FIELD_USERNAME,
        FIELD_PASSWORD,
        FIELD_REGISTERSERVER,
        FIELD_PROXYSERVER,
        FIELD_SERVERID,
        FIELD_MEETID
    };

    // 2017-12-18 Netpool Modify --- End ---
    // private SQLiteDatabase db;
    AccountLogHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
       @Override
       public void onCreate(SQLiteDatabase db) {
         db.execSQL("CREATE TABLE " + TABLE_NAME + " ("
             + FIELD_ID + " INTEGER PRIMARY KEY,"
             + FIELD_NAME + " TEXT,"
             + FIELD_PROTOCOL + " TEXT,"
             + FIELD_SRTP + " INTEGER,"
             + FIELD_PORT + " INTEGER,"
             + FIELD_USERNAME + " TEXT,"
             + FIELD_PASSWORD + " TEXT,"
             + FIELD_REGISTERSERVER + " TEXT,"
             + FIELD_PROXYSERVER + " TEXT,"
             + FIELD_SERVERID + " TEXT,"
             + FIELD_MEETID + " TEXT "
             + ");");
       }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion <= oldVersion) {
            onCreate(db);
            return;
        }

        db.beginTransaction();

        if (oldVersion < 3) {
            boolean success = false;     
            String str
                = " ALTER TABLE "+ TABLE_NAME
                + " ADD COLUMN "+ FIELD_SRTP + " INTEGER DEFAULT 0 ";
            db.execSQL(str);
            oldVersion++;
            success = true;
            if (success) {
                db.setTransactionSuccessful();
            }
        }
        
        if (oldVersion == 3 && newVersion == 4) {
            String tSql
                = " ALTER TABLE " + TABLE_NAME
                + " ADD COLUMN " + FIELD_MEETID + " TEXT ";
            db.execSQL(tSql);
            db.setTransactionSuccessful();
            oldVersion = 4;
        }

        db.endTransaction();
    }
    
        public Cursor get(String rowId) throws SQLException {
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.query(true,
            TABLE_NAME,
            PROJECTION,    //欄位名稱
            "_ID=" + rowId,                //WHERE
            null, // WHERE 的參數
            null, // GROUP BY
            null, // HAVING
            null, // ORDOR BY
            null  // 限制回傳的rows數量
            );
     
            // 注意：不寫會出錯
            if (cursor != null) {
                cursor.moveToFirst();    //將指標移到第一筆資料
            }
            return cursor;
        }

}