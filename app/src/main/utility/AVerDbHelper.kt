package com.aver.superdirector.utility

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class AVerDbHelper(context: Context): SQLiteOpenHelper(context, DbName, CursorFactory, Version){
    companion object{
        const val DbName = "AvrContacts"
        const val Version = 1
        val CursorFactory = null
    }

    private val TAG = "AVerDbHelper"

    override fun onCreate(db: SQLiteDatabase?) {
        Log.v(TAG, "onCreate: ${DatabaseConfig.AUTHORITY}, $DbName ver$Version")
        val sql = "CREATE TABLE IF NOT EXISTS " + DatabaseConfig.TABLE_NAME +"(" +
                "${DatabaseConfig.COLUMN_USER_ID} INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "${DatabaseConfig.COLUMN_NAME} TEXT, " +
                "${DatabaseConfig.COLUMN_SIP} TEXT DEFAULT '', " +
                "${DatabaseConfig.COLUMN_H323} TEXT DEFAULT '', " +
                "${DatabaseConfig.COLUMN_SIP_Favorite} INTEGER DEFAULT 0, " +
                "${DatabaseConfig.COLUMN_H323_Favorite} INTEGER DEFAULT 0, " +
                "${DatabaseConfig.COLUMN_Quality} INTEGER DEFAULT 4" +
                ")"
        db?.execSQL(sql)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        if (oldVersion === 1 && newVersion === 2) {
//            val sql = "alter table ${DatabaseConfig.TABLE_NAME} add Quality INTEGER DEFAULT 4"
//            db!!.execSQL(sql)
        }
    }
}