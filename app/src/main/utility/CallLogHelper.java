package com.aver.superdirector.utility;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class CallLogHelper extends SQLiteOpenHelper {
    private final static String TAG = CallLogHelper.class.getName();

    private final static int DATABASE_VERSION = 2;
    private final static String DATABASE_NAME = "avervideoroom.db";
    private final int MaxLogSize = 150;

    // Table Name
    private final static String TABLE_NAME = "call_log";
    private final static String FIELD_ID = "_id";
    private final static String FIELD_START = "start_datetime";
    private final static String FIELD_END = "end_datetime";
    private final static String FIELD_DURATION = "duration_second";
    private final static String FIELD_REMOTE = "remote_uri";
    private final static String FIELD_SIPH323 = "sip_h323";
    private final static String FIELD_STAT = "call_stat";
    private final static String FIELD_CONTACT = "contact_name";

    // Database creation sql statement
    private final static String CREATE_DB
            = " CREATE TABLE IF NOT EXISTS " + TABLE_NAME
            + " ( " + FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT "
            + " , " + FIELD_START + " TEXT NOT NULL "
            + " , " + FIELD_END + " TEXT "
            + " , " + FIELD_DURATION + " INTEGER NOT NULL DEFAULT 0 "
            + " , " + FIELD_REMOTE + " TEXT NOT NULL "
            + " , " + FIELD_SIPH323 + " TEXT NOT NULL "
            + " , " + FIELD_STAT + " INTEGER NOT NULL "
            + " , " + FIELD_CONTACT + " TEXT ); ";

    public CallLogHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase db) {
        if (db != null)
            db.execSQL(CREATE_DB);
    }

    // Method is called during an upgrade of the database, e.g. if you increase
    // the database version
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVer, int newVer) {
        if (db == null)
            return;

        String tMsg
                = " Upgrading database "
                + " from version " + oldVer + " to " + newVer
                + " , which will destroy all old data. ";
        Log.d(TAG, tMsg);

        db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long addLog(AVerCallLog log)    {
        long res = (-1);
        String tInsert
                = " INSERT INTO " + TABLE_NAME
                + " VALUES(null, ?, ?, ?); ";
        try
        {
            SQLiteDatabase tDb = this.getWritableDatabase();
            /*
            // 帶兩個引數的execSQL()方法,採用佔位符引數?,把引數值放在後面,順序對應
            // 一個引數的execSQL()方法中,使用者輸入特殊字元時需要轉義
            // 使用佔位符有效區分了這種情況
            tDb.execSQL(tInsert, new Object[] {
                    log.StartDateTime, log.EndDateTime, log.SipH323,
                    log.CallStat, log.ContactName});
            */

            ContentValues tCV = new ContentValues();
            tCV.put(FIELD_START, log.StartDateTime);
            tCV.put(FIELD_END, log.EndDateTime);
            tCV.put(FIELD_DURATION, log.DurationSecond);
            tCV.put(FIELD_REMOTE, log.RemoteUri);
            tCV.put(FIELD_SIPH323, log.SipH323);
            tCV.put(FIELD_STAT, log.CallStat);
            tCV.put(FIELD_CONTACT, log.ContactName);
            res = tDb.insert(TABLE_NAME, null, tCV);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }

        return res;
    }

    public void updateDurationSecond(AVerCallLog log) {
        ContentValues tCV = new ContentValues();
        tCV.put(FIELD_END, log.EndDateTime);
        tCV.put(FIELD_DURATION, log.DurationSecond);

        String tWhere = (FIELD_ID + "=?");
        try {
            SQLiteDatabase tDb = this.getWritableDatabase();
            tDb.update(TABLE_NAME, tCV, tWhere, new String[] { String.valueOf(log.Id) });
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public void updateCallStat(AVerCallLog log) {
        ContentValues tCV = new ContentValues();
        tCV.put(FIELD_END, log.EndDateTime);
        tCV.put(FIELD_STAT, log.CallStat);

        String tWhere = (FIELD_ID + "=?");
        try {
            SQLiteDatabase tDb = this.getWritableDatabase();
            tDb.update(TABLE_NAME, tCV, tWhere, new String[] { String.valueOf(log.Id) });
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public List<AVerCallLog> queryLogs(String sipH323) {
        ArrayList<AVerCallLog> resObj = new ArrayList<AVerCallLog>();
        Cursor tCursor = this.queryCursor(sipH323);
        if (tCursor == null)
            return resObj;

        try {
            Log.v(TAG, "queryLogs: " + sipH323 + ", count " + tCursor.getCount());
            while (tCursor.moveToNext()) {
                AVerCallLog tLog = new AVerCallLog();
                tLog.Id = tCursor.getInt(tCursor.getColumnIndex(FIELD_ID));
                tLog.StartDateTime = tCursor.getString(tCursor.getColumnIndex(FIELD_START));
                tLog.EndDateTime = tCursor.getString(tCursor.getColumnIndex(FIELD_END));
                tLog.DurationSecond = tCursor.getInt(tCursor.getColumnIndex(FIELD_DURATION));
                tLog.RemoteUri = tCursor.getString(tCursor.getColumnIndex(FIELD_REMOTE));
                tLog.SipH323 = tCursor.getString(tCursor.getColumnIndex(FIELD_SIPH323));
                tLog.CallStat = tCursor.getInt(tCursor.getColumnIndex(FIELD_STAT));
                tLog.ContactName = tCursor.getString(tCursor.getColumnIndex(FIELD_CONTACT));
                resObj.add(tLog);
                if(resObj.size() == 100){
                    break;
                }
            }
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        } finally {
            tCursor.close();
        }

        return resObj;
    }

    public int clearAllLogs(){
        SQLiteDatabase tDb = this.getWritableDatabase();
        int count = tDb.delete(TABLE_NAME, null, null);
        Log.v(TAG, "clearAllLogs: " + count);
        tDb.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + TABLE_NAME + "'");
        return count;
    }

    //////////////////////
    // Private Function //
    //////////////////////

    private Cursor queryCursor(String sipH323) {
        String tSelect
                = " SELECT * "
                + " FROM " + TABLE_NAME
                + " WHERE 1=1 AND " + FIELD_SIPH323 + "='" + sipH323 + "' "
                + " ORDER BY " + FIELD_ID + " DESC ";

        Cursor resObj = null;
        try {
            SQLiteDatabase tDb = this.getReadableDatabase();
            resObj = tDb.rawQuery(tSelect, null);
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
        return resObj;
    }

    private void deleteLog(SQLiteDatabase tDb, long id) {
        Log.v(TAG, "deleteLog: id "+ id);
        try {
            tDb.delete(TABLE_NAME, FIELD_ID + " = ?", new String[]{String.valueOf(id)});
        } catch (Exception ex) {
            Log.e(TAG, ex.getMessage());
        }
    }

    public void checkAllLogSize(String sipH323){
        Cursor tCursor = this.queryCursor(sipH323);
        if(tCursor.getCount() > MaxLogSize) {
            SQLiteDatabase tDb = this.getReadableDatabase();
            int deleteSize = tCursor.getCount() - MaxLogSize;
            tCursor.moveToLast();
            int i = 1;
            long tid = tCursor.getInt(tCursor.getColumnIndex(FIELD_ID));
            Log.v(TAG, "checkAllLogSize: " + sipH323 + " delete " + deleteSize + " CallLogs.");
            deleteLog(tDb, tid);
            while (tCursor.moveToPrevious()) {
                i++;
                tid = tCursor.getInt(tCursor.getColumnIndex(FIELD_ID));
                deleteLog(tDb, tid);
                if (i >= deleteSize) {
                    break;
                }
            }
        }
    }
}