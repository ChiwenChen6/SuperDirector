package com.aver.superdirector

import android.annotation.SuppressLint
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aver.superdirector.MainActivity.Companion.cameraItemAdapter
import com.aver.superdirector.MainActivity.Companion.cameraItemAdapterUSB
import com.aver.superdirector.MainActivity.Companion.cameraItemList
import com.aver.superdirector.MainActivity.Companion.cameraItemListUSB
import com.aver.superdirector.utility.*
import kotlinx.android.synthetic.main.dialog_add_contact.view.*
import kotlinx.android.synthetic.main.fragment_contact.view.*
import kotlinx.android.synthetic.main.fragment_contact.view.btnBack
import kotlinx.android.synthetic.main.fragment_scan.view.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * A simple [Fragment] subclass.
 */
class ScanFragment : Fragment(), CardViewListenerInterface {
    private val TAG = "ContactFragment"
    private var mIsFavorite = false

    private lateinit var mViewAccountList: RecyclerView
    private var mAdapter: AccountInfoAdapter? = null
    // private lateinit var mLayoutManager: RecyclerView.LayoutManager

    private val mAccountList = ArrayList<AVerAccountInfo>()
    private val mFavoriteList = ArrayList<AVerAccountInfo>()
    private val mSearchAccountList = ArrayList<AVerAccountInfo>()
    private var mContentResolver: ContentResolver? = null
    private var mH323Selected = true
    private var isInit = false
    private lateinit var ScanFragmentView: View
    private lateinit var mFragmentCallback: MainActivity.FragmentCallback
    private lateinit var mDialCallback: VideoFragment.IDialCallback

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView: ")
        // Inflate the layout for this fragment

        var spacingInPixels=72
        ScanFragmentView = inflater.inflate(R.layout.fragment_scan, container, false)
        ScanFragmentView.setOnClickListener(View.OnClickListener {

        })
        val cameraViewVERTICALUSB= ScanFragmentView.findViewById<RecyclerView>(R.id.viewUSBitem) //id RecyclerView
        cameraViewVERTICALUSB.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
        // 這個數量關係到後面增加時的位置計算
        cameraItemAdapterUSB = CameraItemAdapter(cameraItemListUSB, this)
        cameraViewVERTICALUSB.adapter = cameraItemAdapterUSB
        cameraViewVERTICALUSB.scrollToPosition(0)
        ScanFragmentView.isClickable = false

        val cameraViewVerticalIP= ScanFragmentView.findViewById<RecyclerView>(R.id.viewIPitem) //id RecyclerView
        cameraViewVerticalIP.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
        // 這個數量關係到後面增加時的位置計算
        cameraItemAdapter = CameraItemAdapter(cameraItemList, this)
        cameraViewVerticalIP.adapter = cameraItemAdapter
        cameraViewVerticalIP.scrollToPosition(0)
        ScanFragmentView.isClickable = false



        ScanFragmentView.btnscan.setOnClickListener(View.OnClickListener {
            (activity as MainActivity).findCamera()

        })

        mContentResolver = context?.contentResolver

//        ===== Create test data ====
//        val cursor = mContentResolver?.query(Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}")
//                ,null, null, null, null)
//        if(cursor != null){
//            if(cursor.count == 0){
//                for (i in 0 until 5) {
//                    val values = ContentValues()
//                    values.put(DatabaseConfig.COLUMN_NAME, "abb$i")
//                    values.put(DatabaseConfig.COLUMN_SIP, "192.168.1.10$i")
//                    values.put(DatabaseConfig.COLUMN_H323, "192.168.1.30$i")
//                    mContentResolver?.insert(Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}"), values)
//                }
//            }
//        }

//        mViewAccountList = mFragmentView.viewAccountList
//        // mLayoutManager = GridLayoutManager(activity,2)
//        mViewAccountList.layoutManager = GridLayoutManager(activity,2)
//        mAdapter = AccountInfoAdapter(mAccountList, listener)
//        mViewAccountList.adapter = mAdapter
//        createAccountList()
//        isInit = true
        return ScanFragmentView
    }

    override fun onPause() {
        super.onPause()

    }

    private fun createAccountList() {
        mAccountList.clear()
        mFavoriteList.clear()
        mAdapter?.notifyDataSetChanged()
        var cursor: Cursor?
        if(mH323Selected){
            cursor = mContentResolver?.query(Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}"),
                arrayOf(DatabaseConfig.COLUMN_USER_ID, DatabaseConfig.COLUMN_NAME, DatabaseConfig.COLUMN_H323, DatabaseConfig.COLUMN_H323_Favorite, DatabaseConfig.COLUMN_Quality), null, null, null)
        }else{
            cursor = mContentResolver?.query(Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}"),
                arrayOf(DatabaseConfig.COLUMN_USER_ID, DatabaseConfig.COLUMN_NAME, DatabaseConfig.COLUMN_SIP, DatabaseConfig.COLUMN_SIP_Favorite, DatabaseConfig.COLUMN_Quality), null, null, null)
        }

        if(cursor != null) {
            if (cursor.count > 0) {
                //從第一筆開始輸出
                cursor.moveToFirst()
                //Step3:用迴圈將Cursor內的資料取出
                for (i in 0 until cursor.count) {
                    if(cursor.getString(2) != "") {
                        val tInfo = AVerAccountInfo()
                        tInfo.Id = cursor.getLong(0)
                        tInfo.AccountName = cursor.getString(1)
                        tInfo.RemoteUri = cursor.getString(2)
                        tInfo.IsFavorite = cursor.getString(3).toInt() != 0
                        if(tInfo.IsFavorite){
                            mFavoriteList.add(tInfo)
                        }
                        mAccountList.add(tInfo)
                    }
                    cursor.moveToNext()
                }
                cursor.close()
            }
        }

        mAdapter?.notifyDataSetChanged()

    }

    /////////////////////
    // Public Function //
    /////////////////////
    fun setCallback(cb: VideoFragment.IDialCallback){
        mDialCallback = cb
    }

    fun setFragmentCallback(cb: MainActivity.FragmentCallback){
        mFragmentCallback = cb
    }

    fun onParentClick(){
        if (mAdapter?.deleteMode!!) {
            mAdapter?.deleteMode = !mAdapter?.deleteMode!!
            ScanFragmentView.clSearch.visibility = View.VISIBLE
            ScanFragmentView.isClickable = false
            mFragmentCallback.onDeleteModeChange(false)
            Log.v(TAG, "Delete mode: disable")
        }
    }

    //////////////////////
    // Private Function //
    //////////////////////

    private fun showAddAccountDialog(info: AVerAccountInfo) {
        val tRect = Rect()
        val tWindow: Window = requireActivity().window
        tWindow.decorView.getWindowVisibleDisplayFrame(tRect)

        val tDialog = LayoutInflater.from(context).inflate(
                R.layout.dialog_add_contact, activity?.findViewById(android.R.id.content), false)
        tDialog.minimumWidth = (tRect.width() * 1f).toInt()
        tDialog.minimumHeight = (tRect.height() * 1f).toInt()

        val cursor = mContentResolver?.query(Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}/${info.Id}"),
                null, null, null, null)
        cursor?.moveToFirst()
        val name = cursor?.getString(1)
        val h323Ip = cursor?.getString(3)
        val sipIp = cursor?.getString(2)
        var qualityIndex = cursor?.getInt(6)
        cursor?.close()
        tDialog.edtName.setText(name)
        tDialog.edtH323Ip.setText(h323Ip)
        tDialog.edtSipIp.setText(sipIp)

        when (qualityIndex) {
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

        var textChange = false
        var indexChange = false
        val modify = object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                var res = false
                if(tDialog.edtName.text.toString() != name){
                    res = true
                }
                if(tDialog.edtH323Ip.text.toString() != h323Ip){
                    res = true
                }
                if(tDialog.edtSipIp.text.toString() != sipIp){
                    res = true
                }
                textChange = res
                tDialog.btnSave.isEnabled = textChange || indexChange
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
        var newQualityIndex = qualityIndex
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

            var siteNameCheck = true
            val cursor = mContentResolver?.query(Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}/${DatabaseConfig.COLUMN}"),
                    arrayOf(DatabaseConfig.COLUMN_USER_ID, DatabaseConfig.COLUMN_NAME), DatabaseConfig.COLUMN_NAME, arrayOf(siteName), null)

            if(cursor != null && cursor.count > 0) {
                cursor.moveToFirst()
                for (i in 0 until cursor.count) {
                    Log.v(TAG, "Edit contact info: ${cursor.getLong(0)}, ${cursor.getString(1)}")
                    if(cursor.getLong(0) != info.Id) {
                        siteNameCheck = false
                    }
                    cursor.moveToNext()
                }
                cursor.close()
                if(!siteNameCheck){
                    tDialog.txtInputHint.visibility = View.VISIBLE
                    Log.w(TAG, "Edit contact info: Site name duplicate.")
                    return@OnClickListener
                }
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
            mContentResolver?.update(Uri.parse("content://${DatabaseConfig.AUTHORITY}/${DatabaseConfig.CONTACT_PATH}/${info.Id}"),
                cv, null, null)
            createAccountList()
            mCallingDialog.dismiss()
        })
    }



    override fun onItemClicked(position: Int) {
        MainActivity.currentCameraItemUSB = cameraItemAdapterUSB!!.getCameraItem(position)
        var UCID = MainActivity.currentCameraItemUSB?.ip?.let {
            (activity as MainActivity?)!!.conpareIPSaved_modelname(
                it
            )
        }
        Log.v("AVDebug", "UCID :$UCID")
        MainActivity.currentCameraItemUSB?.ip?.let { (activity as MainActivity?)!!.usbDeviceDetectState(it,UCID) }    }

}
