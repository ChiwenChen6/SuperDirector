package com.aver.superdirector

import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aver.superdirector.utility.*
import kotlinx.android.synthetic.main.dialog_add_contact.view.*
import kotlinx.android.synthetic.main.fragment_recent.view.*

/**
 * A simple [Fragment] subclass.
 */
class CombineFragment : Fragment() {
    private val TAG = "CombineFragment"

    //private var mSipH323 = ConferenceActivity.PROTOCOL_H323

    private lateinit var mViewCallLog: RecyclerView
    private var mAdapter: CallLogAdapter? = null
    private var mContentResolver: ContentResolver? = null
    private val mAccountList = ArrayList<AVerAccountInfo>()
    private val mOtherAccountList = ArrayList<AVerAccountInfo>()
    private var mH323Selected = true
    private var isInit = false
    private lateinit var mDialCallback: VideoFragment.IDialCallback
    private lateinit var mFragmentCallback: MainActivity.FragmentCallback
    private lateinit var mListListener: AccountInfoAdapter.ItemActionListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        mContentResolver = context?.contentResolver

        val resObj = inflater.inflate(R.layout.fragment_recent, container, false)
        mViewCallLog = resObj.viewCallLog
        mViewCallLog.layoutManager = LinearLayoutManager(activity)
        updateAccountList()
        mListListener = object : AccountInfoAdapter.ItemActionListener{
            override fun onDeleteClick(info: AVerAccountInfo?) {}
            override fun onEditClick(info: AVerAccountInfo?) {
                if (info != null) {
                    showAddAccountDialog(info)
                }
            }
            override fun onItemLongClick(info: AVerAccountInfo?) {}
            override fun onFavoriteClick(info: AVerAccountInfo?) {}
            override fun onItemClick(info: AVerAccountInfo?) {
                if (info != null) {
                    mDialCallback.onCallOut(info)
                }
            }
        }
        isInit = true
        return resObj
    }

    override fun onResume() {
        super.onResume()
        showCallLog()
    }

    /////////////////////
    // Private Function//
    /////////////////////

    private fun updateAccountList(){
        mAccountList.clear()
        mOtherAccountList.clear()
        var cursor: Cursor?
        if(mH323Selected){
            cursor = mContentResolver?.query(Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}"),
                    arrayOf(DatabaseConfig.COLUMN_USER_ID, DatabaseConfig.COLUMN_NAME, DatabaseConfig.COLUMN_H323, DatabaseConfig.COLUMN_H323_Favorite, DatabaseConfig.COLUMN_Quality), null, null, null)
            if(cursor != null) {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    for (i in 0 until cursor.count) {
                        if(cursor.getString(2) != "") {
                            val tInfo = AVerAccountInfo()
                            tInfo.Id = cursor.getLong(0)
                            tInfo.AccountName = cursor.getString(1)
                            tInfo.RemoteUri = cursor.getString(2)
                            tInfo.IsFavorite = cursor.getString(3).toInt() != 0
                            tInfo.Quality = cursor.getString(4).toInt()
                            mAccountList.add(tInfo)
                        }
                        cursor.moveToNext()
                    }
                    cursor.close()
                }
            }

            cursor = mContentResolver?.query(Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}"),
                    arrayOf(DatabaseConfig.COLUMN_USER_ID, DatabaseConfig.COLUMN_NAME, DatabaseConfig.COLUMN_SIP, DatabaseConfig.COLUMN_SIP_Favorite, DatabaseConfig.COLUMN_Quality), null, null, null)
            if(cursor != null) {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    for (i in 0 until cursor.count) {
                        if(cursor.getString(2) != "") {
                            val tInfo = AVerAccountInfo()
                            tInfo.Id = cursor.getLong(0)
                            tInfo.AccountName = cursor.getString(1)
                            tInfo.RemoteUri = cursor.getString(2)
                            tInfo.IsFavorite = cursor.getString(3).toInt() != 0
                            tInfo.Quality = cursor.getString(4).toInt()
                            mOtherAccountList.add(tInfo)
                        }
                        cursor.moveToNext()
                    }
                    cursor.close()
                }
            }
        }else{
            cursor = mContentResolver?.query(Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}"),
                    arrayOf(DatabaseConfig.COLUMN_USER_ID, DatabaseConfig.COLUMN_NAME, DatabaseConfig.COLUMN_SIP, DatabaseConfig.COLUMN_SIP_Favorite, DatabaseConfig.COLUMN_Quality), null, null, null)
            if(cursor != null) {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    for (i in 0 until cursor.count) {
                        if(cursor.getString(2) != "") {
                            val tInfo = AVerAccountInfo()
                            tInfo.Id = cursor.getLong(0)
                            tInfo.AccountName = cursor.getString(1)
                            tInfo.RemoteUri = cursor.getString(2)
                            tInfo.IsFavorite = cursor.getString(3).toInt() != 0
                            tInfo.Quality = cursor.getString(4).toInt()
                            mAccountList.add(tInfo)
                        }
                        cursor.moveToNext()
                    }
                    cursor.close()
                }
            }

            cursor = mContentResolver?.query(Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}"),
                    arrayOf(DatabaseConfig.COLUMN_USER_ID, DatabaseConfig.COLUMN_NAME, DatabaseConfig.COLUMN_H323, DatabaseConfig.COLUMN_H323_Favorite, DatabaseConfig.COLUMN_Quality), null, null, null)
            if(cursor != null) {
                if (cursor.count > 0) {
                    cursor.moveToFirst()
                    for (i in 0 until cursor.count) {
                        if(cursor.getString(2) != "") {
                            val tInfo = AVerAccountInfo()
                            tInfo.Id = cursor.getLong(0)
                            tInfo.AccountName = cursor.getString(1)
                            tInfo.RemoteUri = cursor.getString(2)
                            tInfo.IsFavorite = cursor.getString(3).toInt() != 0
                            tInfo.Quality = cursor.getString(4).toInt()
                            mOtherAccountList.add(tInfo)
                        }
                        cursor.moveToNext()
                    }
                    cursor.close()
                }
            }
        }
    }

    private fun showAddAccountDialog(info: AVerAccountInfo) {
        val tRect = Rect()
        val tWindow: Window = requireActivity().window
        tWindow.decorView.getWindowVisibleDisplayFrame(tRect)

        val tDialog = LayoutInflater.from(context).inflate(
                R.layout.dialog_add_contact, activity?.findViewById(android.R.id.content), false)
        tDialog.minimumWidth = (tRect.width() * 1f).toInt()
        tDialog.minimumHeight = (tRect.height() * 1f).toInt()

        var name = ""
        var h323Ip = ""
        var sipIp = ""
        if(mH323Selected){
            h323Ip = info.RemoteUri
            tDialog.edtH323Ip.setText(h323Ip)
        }else{
            sipIp = info.RemoteUri
            tDialog.edtSipIp.setText(sipIp)
        }

        var qualityIndex = -1
        tDialog.rbQuality_512.isChecked = true

        var textChange = false
        var indexChange = false
        var otherAccountInfo: AVerAccountInfo? = null
        val modify = object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                var vaildName = false
                var res = false
                if(tDialog.edtName.text.toString() != ""){
                    vaildName = true
                    res = true
                }

                if(tDialog.edtName.text.toString() != name){
                    name = tDialog.edtName.text.toString()
                    for (i in 0 until mOtherAccountList.size) {
                        var info = mOtherAccountList[i]
                        if(info.AccountName == name){
                            otherAccountInfo = info
                            val protocol = if(mH323Selected){
                                "H.323"
                            }else{
                                "SIP"
                            }
                            Log.v(TAG, "Site name found in $protocol list, ${info.Id} ${info.AccountName} ${info.RemoteUri} ${info.Quality}")
                            break
                        }
                        if(i == mOtherAccountList.size-1){
                            otherAccountInfo = null
                        }
                    }
                    if(otherAccountInfo != null) {
                        if (mH323Selected) {
                            tDialog.edtSipIp.setText(otherAccountInfo?.RemoteUri)
                        } else {
                            tDialog.edtH323Ip.setText(otherAccountInfo?.RemoteUri)
                        }
                        when (otherAccountInfo!!.Quality) {
                            0 -> tDialog.rbQuality_64.isChecked = true
                            1 -> tDialog.rbQuality_128.isChecked = true
                            2 -> tDialog.rbQuality_256.isChecked = true
                            3 -> tDialog.rbQuality_384.isChecked = true
                            4 -> tDialog.rbQuality_512.isChecked = true
                            5 -> tDialog.rbQuality_768.isChecked = true
                            6 -> tDialog.rbQuality_1024.isChecked = true
                            7 -> tDialog.rbQuality_1536.isChecked = true
                            8 -> tDialog.rbQuality_2048.isChecked = true
                            else -> {
                                tDialog.rbQuality_512.isChecked = true
                                Log.w(TAG, "Quality $qualityIndex not found.")
                            }
                        }
                    }else{
                        if (mH323Selected) {
                            tDialog.edtSipIp.setText("")
                        } else {
                            tDialog.edtH323Ip.setText("")
                        }
                        tDialog.rbQuality_512.isChecked = true
                    }
                }

                if(tDialog.edtH323Ip.text.toString() != h323Ip){
                    res = true
                }
                if(tDialog.edtSipIp.text.toString() != sipIp){
                    res = true
                }
                textChange = res
                tDialog.btnSave.isEnabled = (textChange || indexChange) && vaildName
                if(tDialog.btnSave.isEnabled){
                    tDialog.btnSave.setTextColor(resources.getColor(R.color.white, null))
                }else{
                    tDialog.btnSave.setTextColor(resources.getColor(R.color.gray_lite_line, null))
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(tDialog.txtInputHint.visibility == View.VISIBLE){
                    tDialog.txtInputHint.visibility = View.INVISIBLE
                }
            }
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        }
        tDialog.edtName.addTextChangedListener(modify)
        tDialog.edtSipIp.addTextChangedListener(modify)
        tDialog.edtH323Ip.addTextChangedListener(modify)

        var rg1ListenerEnable = true
        var rg2ListenerEnable = true
        var newQualityIndex = 4
        tDialog.rgQuality1.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener { _, i ->
            if (i >= 0 && rg1ListenerEnable) {
                rg2ListenerEnable = false
                tDialog.rgQuality2.clearCheck()
                rg2ListenerEnable = true
                newQualityIndex = when (i) {
                    tDialog.rbQuality_64.id -> 0
                    tDialog.rbQuality_128.id -> 1
                    tDialog.rbQuality_256.id -> 2
                    tDialog.rbQuality_384.id -> 3
                    tDialog.rbQuality_512.id -> 4
                    tDialog.rbQuality_768.id -> 5
                    tDialog.rbQuality_1024.id -> 6
                    else -> {
                        Log.w(TAG, "rgQuality1 button $i not found.")
                        4
                    }
                }
                indexChange = newQualityIndex != qualityIndex
                tDialog.btnSave.isEnabled = textChange || indexChange
                if(tDialog.btnSave.isEnabled){
                    tDialog.btnSave.setTextColor(resources.getColor(R.color.white, null))
                }else{
                    tDialog.btnSave.setTextColor(resources.getColor(R.color.gray_lite_line, null))
                }
            }
        })
        tDialog.rgQuality2.setOnCheckedChangeListener(RadioGroup.OnCheckedChangeListener{ _, i ->
            if(i >= 0 && rg2ListenerEnable) {
                rg1ListenerEnable = false
                tDialog.rgQuality1.clearCheck()
                rg1ListenerEnable = true
                newQualityIndex = when (i) {
                    tDialog.rbQuality_1536.id -> 7
                    tDialog.rbQuality_2048.id -> 8
                    else -> {
                        Log.w(TAG, "rgQuality2 button $i not found.")
                        4
                    }
                }
                indexChange = newQualityIndex != qualityIndex
                tDialog.btnSave.isEnabled = textChange || indexChange
                if(tDialog.btnSave.isEnabled){
                    tDialog.btnSave.setTextColor(resources.getColor(R.color.white, null))
                }else{
                    tDialog.btnSave.setTextColor(resources.getColor(R.color.gray_lite_line, null))
                }
            }
        })

        // Now we need an AlertDialog.Builder object
        val tBuilder = AlertDialog.Builder(requireContext(), R.style.DialogTheme)
        // setting the view of the builder to our custom view
        // that we already inflated
        tBuilder.setView(tDialog)
        // finally creating the alert dialog and displaying it
        val mCallingDialog = tBuilder.create()
        mCallingDialog.show()

        tDialog.btnBack.setOnClickListener(View.OnClickListener {
            mCallingDialog.dismiss()
        })

        tDialog.btnSave.setOnClickListener(View.OnClickListener {
            if(tDialog.edtH323Ip.text.toString() == "" && tDialog.edtSipIp.text.toString() == ""){
                tDialog.txtInputHint.visibility = View.VISIBLE
                Log.w(TAG, "Edit contact info: SIP and H.323 ip are empty.")
                return@OnClickListener
            }
            val siteName = tDialog.edtName.text.toString()
            if(siteName == ""){
                tDialog.txtInputHint.visibility = View.VISIBLE
                Log.w(TAG, "Edit contact info: Site name is empty.")
                return@OnClickListener
            }
            if(siteName.subSequence(0,1) == " "){
                tDialog.txtInputHint.visibility = View.VISIBLE
                Log.w(TAG, "Edit contact info: Site name first char is space.")
                return@OnClickListener
            }
            val spaceCheck = siteName.replace(" ", "")
            if(spaceCheck.isEmpty()){
                tDialog.txtInputHint.visibility = View.VISIBLE
                Log.w(TAG, "Edit contact info: Site name is space.")
                return@OnClickListener
            }

            var siteNameExist = false
            for(i in 0 until mAccountList.size){
                val info = mAccountList[i]
                if(info.AccountName == tDialog.edtName.text.toString()){
                    siteNameExist = true
                }
            }
            if(siteNameExist){
                tDialog.txtInputHint.visibility = View.VISIBLE
                Log.w(TAG, "Edit contact info: Site name duplicate.")
                return@OnClickListener
            }

            var ipCheck = true
            if(tDialog.edtSipIp.text.toString() != ""){
                ipCheck = if (tDialog.edtSipIp.text.toString().contains(".")) {
                    VideoFragment.isValidIPAddress(tDialog.edtSipIp.text.toString())
                }else{
                    VideoFragment.isValidText(tDialog.edtSipIp.text.toString())
                }
            }
            if(tDialog.edtH323Ip.text.toString() != ""){
//                if (tDialog.edtH323Ip.text.toString().contains(".")) {
//                    ipCheck = KeypadFragment.isValidIPAddress(tDialog.edtH323Ip.text.toString()) && ipCheck
//                } else {
                    ipCheck = true && ipCheck
//                }
            }
            if(!ipCheck){
                tDialog.txtInputHint.visibility = View.VISIBLE
                Log.w(TAG, "Edit contact info: IP format not correct.")
                return@OnClickListener
            }

            val cv = ContentValues()
            cv.put(DatabaseConfig.COLUMN_NAME, siteName)
            cv.put(DatabaseConfig.COLUMN_SIP, tDialog.edtSipIp.text.toString())
            cv.put(DatabaseConfig.COLUMN_H323, tDialog.edtH323Ip.text.toString())
            cv.put(DatabaseConfig.COLUMN_Quality, newQualityIndex)
            if(otherAccountInfo != null){
                val res = mContentResolver?.update(Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}/${otherAccountInfo!!.Id}"),
                        cv, null, null)
                Log.v(TAG, "Update account info to id = $res, $siteName.")
            }else{
                val res = mContentResolver?.insert(Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}"), cv)
                Log.v(TAG, "Add new account info, $res")
            }

            updateAccountList()
            showCallLog()
            mCallingDialog.dismiss()
            mFragmentCallback.showNotification()
        })
    }

    /////////////////////
    // Public Function //
    /////////////////////

    fun showCallLog() {
        if (context == null)
            return

        val tHelper = CallLogHelper(context)
        //val tList = tHelper.queryLogs(mSipH323)
        val mPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val mEditor = mPreferences.edit()
        //mEditor.putString("RecentLength", tList.size.toString())
        mEditor.apply()
       // val tTask: AsyncTask<String, Void, Void> = HttpRequestTask()
        //tTask.execute("set_string", "AVRSet", "RecentLength$mSipH323", tList.size.toString())
        //mAdapter = CallLogAdapter(resources, tList, mListListener)
        //mAdapter!!.setAccountList(mAccountList)
       //// mViewCallLog.adapter = mAdapter
        // mAdapter.notifyDataSetChanged()
    }

    fun setSipH323(sipH323: String) {
        Log.v(TAG, "setSipH323: $sipH323")
        //mSipH323 = sipH323
        //mH323Selected = sipH323 == ConferenceActivity.PROTOCOL_H323
        if(isInit) {
            updateAccountList()
        }
        showCallLog()
    }

    fun setFragmentCallback(cb: MainActivity.FragmentCallback){
        mFragmentCallback = cb
    }

    fun setCallback(callback: VideoFragment.IDialCallback?) {
        if (callback != null) {
            mDialCallback = callback
        }
    }
}