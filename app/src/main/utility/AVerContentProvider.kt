package com.aver.superdirector.utility

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.util.Log

class AVerContentProvider : ContentProvider() {
    private val TAG = "AVerContentProvider"
    private lateinit var mAVerDbHelper: SQLiteDatabase
    private lateinit var sUriMatcher : UriMatcher

    private fun initializeUriMatching() {
        sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        sUriMatcher.addURI(DatabaseConfig.AUTHORITY, DatabaseConfig.CONTACT_PATH, DatabaseConfig.URI_ALL_ITEMS_CODE)
        sUriMatcher.addURI(DatabaseConfig.AUTHORITY, DatabaseConfig.CONTACT_PATH + "/#", DatabaseConfig.URI_ONE_USER_CODE)
        sUriMatcher.addURI(DatabaseConfig.AUTHORITY, DatabaseConfig.CONTACT_PATH + "/" + DatabaseConfig.COLUMN_SIP + "/#", DatabaseConfig.URI_SIP_CODE)
        sUriMatcher.addURI(DatabaseConfig.AUTHORITY, DatabaseConfig.CONTACT_PATH + "/" + DatabaseConfig.COLUMN_H323 + "/#", DatabaseConfig.URI_H323_CODE)
        sUriMatcher.addURI(DatabaseConfig.AUTHORITY, DatabaseConfig.CONTACT_PATH + "/" + DatabaseConfig.COLUMN, DatabaseConfig.URI_COLUMN_CODE)
    }

    override fun onCreate(): Boolean {
        val context = context?: return false
        mAVerDbHelper = AVerDbHelper(context).writableDatabase
        initializeUriMatching()
        return true
    }

    override fun getType(uri: Uri): String? {
        return when(sUriMatcher.match(uri)){
            DatabaseConfig.URI_ALL_ITEMS_CODE -> DatabaseConfig.MULTIPLE_RECORDS_MIME_TYPE
            DatabaseConfig.URI_ONE_USER_CODE -> DatabaseConfig.SINGLE_RECORD_MIME_TYPE
            DatabaseConfig.URI_SIP_CODE -> DatabaseConfig.SINGLE_RECORD_MIME_TYPE
            DatabaseConfig.URI_H323_CODE -> DatabaseConfig.SINGLE_RECORD_MIME_TYPE
            DatabaseConfig.URI_COLUMN_CODE -> DatabaseConfig.SINGLE_RECORD_MIME_TYPE
            else -> null
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        Log.v(TAG, "delete: $uri")
        var check = false
        var res = -1
        when(sUriMatcher.match(uri)) {
            DatabaseConfig.URI_H323_CODE -> {
                Log.v(TAG, "deleteContact: URI_H323_CODE")
                val value = ContentValues()
                value.put(DatabaseConfig.COLUMN_H323, "")
                value.put(DatabaseConfig.COLUMN_H323_Favorite, 0)
                res = update(uri, value, null, null)
                check = true
            }
            DatabaseConfig.URI_SIP_CODE -> {
                Log.v(TAG, "deleteContact: URI_SIP_CODE")
                val value = ContentValues()
                value.put(DatabaseConfig.COLUMN_SIP, "")
                value.put(DatabaseConfig.COLUMN_SIP_Favorite, 0)
                res = update(uri, value, null, null)
                check = true
            }
            DatabaseConfig.URI_ALL_ITEMS_CODE ->{
                Log.v(TAG, "delete: URI_ALL_ITEMS_CODE")
                res = mAVerDbHelper.delete(DatabaseConfig.TABLE_NAME, null, null)
                Log.v(TAG, "delete: Delete all contacts, $res")
                mAVerDbHelper.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" + DatabaseConfig.TABLE_NAME + "'")
            }
            UriMatcher.NO_MATCH -> {
                Log.v(TAG, "delete: NO_MATCH")
            }
            else -> { Log.v(TAG, "delete: else")}
        }
        if(check){
            val contactId = ContentUris.parseId(uri)
            val qUri = Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}/$contactId")
            val cursor = query(qUri, null, null, null)
            if(cursor != null){
                cursor.moveToFirst()
                Log.v(TAG, "delete: check id=${cursor.getString(0)} , Name=${cursor.getString(1)}, SIP=${cursor.getString(2)}, H323=${cursor.getString(3)}")
                if(cursor.getString(2) == "" && cursor.getString(3) == ""){
                    Log.v(TAG, "delete: remove item ${cursor.getInt(0)} in database.")
                    res = mAVerDbHelper.delete(DatabaseConfig.TABLE_NAME, "${DatabaseConfig.COLUMN_USER_ID}=?", arrayOf(contactId.toString()))
                }
            }
        }
        return res
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        Log.v(TAG, "insert: $uri")
        val rowID = mAVerDbHelper.insert(DatabaseConfig.TABLE_NAME, null, values)
        return Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}/$rowID")
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        //projection 欄位
        //selection 條件
        //selection 條件value
        val order = "${DatabaseConfig.COLUMN_NAME} ASC"
        var cursor: Cursor? = null
        when(sUriMatcher.match(uri)) {
            DatabaseConfig.URI_ALL_ITEMS_CODE -> {
                cursor = mAVerDbHelper.query(DatabaseConfig.TABLE_NAME, projection,
                        selection, selectionArgs,null,null, order)
                Log.v(TAG, "query: URI_ALL_ITEMS_CODE, ${cursor.count}")
            }
            DatabaseConfig.URI_ONE_USER_CODE -> {
                Log.v(TAG, "query: URI_ONE_USER_CODE")
                val contactId = ContentUris.parseId(uri)
                cursor = mAVerDbHelper.query(DatabaseConfig.TABLE_NAME,
                        arrayOf(DatabaseConfig.COLUMN_USER_ID, DatabaseConfig.COLUMN_NAME, DatabaseConfig.COLUMN_SIP, DatabaseConfig.COLUMN_H323, DatabaseConfig.COLUMN_SIP_Favorite, DatabaseConfig.COLUMN_H323_Favorite, DatabaseConfig.COLUMN_Quality),
                        "${DatabaseConfig.COLUMN_USER_ID}=?", arrayOf(contactId.toString()),null,null, order)
            }
            DatabaseConfig.URI_COLUMN_CODE -> {
                Log.v(TAG, "query: URI_COLUMN_CODE")
                cursor = mAVerDbHelper.query(DatabaseConfig.TABLE_NAME,
                        projection, "$selection=?", selectionArgs,null,null, order)
            }
            UriMatcher.NO_MATCH -> { Log.v(TAG, "query: NO_MATCH")}
            else -> { Log.v(TAG, "query: else")}
        }
        return cursor
    }

    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        Log.v(TAG, "update: $uri, ${values.toString()}")
        val contactId = ContentUris.parseId(uri)
        return  mAVerDbHelper.update(DatabaseConfig.TABLE_NAME, values, "${DatabaseConfig.COLUMN_USER_ID}=?", arrayOf(contactId.toString()))
    }
}