package com.aver.superdirector.utility

class DatabaseConfig {
    companion object {
        const val TABLE_NAME = "ContactTable"
        const val AUTHORITY = "com.aver.avrcontact"
        const val CONTACT_PATH = "contacts"
        const val COLUMN = "column"

        const val COLUMN_USER_ID = "_id"
        const val COLUMN_NAME = "name"
        const val COLUMN_SIP = "SIP"
        const val COLUMN_H323 = "H323"
        const val COLUMN_SIP_Favorite = "sip_favorite"
        const val COLUMN_H323_Favorite = "h323_favorite"
        const val COLUMN_Quality = "Quality"

        const val URI_ALL_ITEMS_CODE = 30
        const val URI_ONE_USER_CODE = 31
        const val URI_SIP_CODE = 32
        const val URI_H323_CODE = 33
        const val URI_COLUMN_CODE = 34
        const val SINGLE_RECORD_MIME_TYPE = "SINGLE_RECORD_MIME_TYPE"
        const val MULTIPLE_RECORDS_MIME_TYPE = "MULTIPLE_RECORDS_MIME_TYPE"
    }
}