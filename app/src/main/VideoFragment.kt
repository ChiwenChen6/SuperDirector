package com.aver.superdirector

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.Volley
import com.aver.superdirector.utility.AVerAccountInfo
import com.aver.superdirector.utility.WidthConverter
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * A simple [Fragment] subclass.
 *
 */
class VideoFragment : Fragment() , View.OnClickListener {
    private val TAG = "VideoFragment"

    interface IDialCallback {
        fun onCallOut(info: AVerAccountInfo)
    }
    private lateinit var mContext: Context
    private lateinit var mFragmentCallback: MainActivity.FragmentCallback
    private var isInit = false

    private val mUiHandler = Handler(Looper.getMainLooper())
    private var mWebHandlerThread: HandlerThread? = null
    private var webSocketHandler: Handler? = null
    private var loginIP = "192.168.1.28"
    private var loginIP2 = "192.168.1.24"
    private lateinit var mVideoLayout: ConstraintLayout
    private lateinit var mSelectLayout: ConstraintLayout
    private lateinit var mSelectListView: ConstraintLayout
    private lateinit var mSelectAuthView: LinearLayout
    private lateinit var mTextureView: TextureView
    private lateinit var mTextureView2: TextureView
    private lateinit var mTextureView3: TextureView
    private lateinit var mTextureView4: TextureView
    private var mConnector1: TextureViewConnector? = null
    private var mConnector2: TextureViewConnector? = null
    private var mConnector3: TextureViewConnector? = null
    private var mConnector4: TextureViewConnector? = null
    private lateinit var mLiveQueue: RequestQueue

    private val mUsbCamInfoList = ArrayList<CamInfo>()
    private val mIpCamInfoList = ArrayList<CamInfo>()
    private lateinit var mUsbRecyclerView: RecyclerView
    private lateinit var mIpRecyclerView: RecyclerView
    private lateinit  var mUsbCamSelectAdapter: CamSelectAdapter
    private lateinit  var mIpCamSelectAdapter: CamSelectAdapter
    private lateinit var mBtnDisconnect: LinearLayout
    private lateinit var mItemActionListener: CamSelectAdapter.ItemActionListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLiveQueue = Volley.newRequestQueue(requireContext())
    }

    private var mWindowClickId = -1
    private lateinit var mFragmentView: View

    private fun updateLayoutSize(){
        var cl = mFragmentView.findViewById<ConstraintLayout>(R.id.live_window_1)
        var w = WidthConverter.getConvertedWidth(TAG, R.dimen.texture_window_width)
        var h = WidthConverter.getConvertedWidth(TAG, R.dimen.texture_window_height)
        cl.layoutParams.width = w
        cl.layoutParams.height = h

        cl = mFragmentView.findViewById<ConstraintLayout>(R.id.live_window_2)
        cl.layoutParams.width = w
        cl.layoutParams.height = h

        cl = mFragmentView.findViewById<ConstraintLayout>(R.id.live_window_3)
        cl.layoutParams.width = w
        cl.layoutParams.height = h

        cl = mFragmentView.findViewById<ConstraintLayout>(R.id.live_window_4)
        cl.layoutParams.width = w
        cl.layoutParams.height = h

        var iv = mFragmentView.findViewById<ImageView>(R.id.live_bg_1)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.live_bg_width)
        iv.layoutParams.width = w
        iv.layoutParams.height = w

        iv = mFragmentView.findViewById<ImageView>(R.id.live_bg_2)
        iv.layoutParams.width = w
        iv.layoutParams.height = w

        iv = mFragmentView.findViewById<ImageView>(R.id.live_bg_3)
        iv.layoutParams.width = w
        iv.layoutParams.height = w

        iv = mFragmentView.findViewById<ImageView>(R.id.live_bg_4)
        iv.layoutParams.width = w
        iv.layoutParams.height = w

        var ll = mFragmentView.findViewById<LinearLayout>(R.id.live_banner_1)
        var top =  WidthConverter.getConvertedWidth(TAG, R.dimen.banner_top)
        (ll.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top

        ll = mFragmentView.findViewById<LinearLayout>(R.id.live_banner_2)
        (ll.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top

        ll = mFragmentView.findViewById<LinearLayout>(R.id.live_banner_3)
        (ll.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top

        ll = mFragmentView.findViewById<LinearLayout>(R.id.live_banner_4)
        (ll.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top

        iv = mFragmentView.findViewById<ImageView>(R.id.live_index_1)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.live_index_width)
        var end = WidthConverter.getConvertedWidth(TAG, R.dimen.live_index_end)
        iv.layoutParams.width = w
        iv.layoutParams.height = w
        (iv.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = end

        iv = mFragmentView.findViewById<ImageView>(R.id.live_index_2)
        iv.layoutParams.width = w
        iv.layoutParams.height = w
        (iv.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = end

        iv = mFragmentView.findViewById<ImageView>(R.id.live_index_3)
        iv.layoutParams.width = w
        iv.layoutParams.height = w
        (iv.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = end

        iv = mFragmentView.findViewById<ImageView>(R.id.live_index_4)
        iv.layoutParams.width = w
        iv.layoutParams.height = w
        (iv.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = end

        var btn = mFragmentView.findViewById<Button>(R.id.live_name_1)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.live_name_width)
        h = WidthConverter.getConvertedWidth(TAG, R.dimen.live_name_height)
        end = WidthConverter.getConvertedWidth(TAG, R.dimen.live_name_padding_start)
        var ts = WidthConverter.getConvertedTextSize(R.dimen.live_name_text_size)
        btn.layoutParams.width = w
        btn.layoutParams.height = h
        btn.setPadding(end, 2, end, 2)
        btn.textSize = ts

        btn = mFragmentView.findViewById<Button>(R.id.live_name_2)
        btn.layoutParams.width = w
        btn.layoutParams.height = h
        btn.setPadding(end, 2, end, 2)
        btn.textSize = ts

        btn = mFragmentView.findViewById<Button>(R.id.live_name_3)
        btn.layoutParams.width = w
        btn.layoutParams.height = h
        btn.setPadding(end, 2, end, 2)
        btn.textSize = ts

        btn = mFragmentView.findViewById<Button>(R.id.live_name_4)
        btn.layoutParams.width = w
        btn.layoutParams.height = h
        btn.setPadding(end, 2, end, 2)
        btn.textSize = ts

        var ibtn = mFragmentView.findViewById<ImageButton>(R.id.live_push_1)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.live_index_width)
        var start = WidthConverter.getConvertedWidth(TAG, R.dimen.live_index_end)
        ibtn.layoutParams.width = w
        ibtn.layoutParams.height = w
        (ibtn.layoutParams as ViewGroup.MarginLayoutParams).marginStart = start

        ibtn = mFragmentView.findViewById<ImageButton>(R.id.live_push_2)
        ibtn.layoutParams.width = w
        ibtn.layoutParams.height = w
        (ibtn.layoutParams as ViewGroup.MarginLayoutParams).marginStart = start

        ibtn = mFragmentView.findViewById<ImageButton>(R.id.live_push_3)
        ibtn.layoutParams.width = w
        ibtn.layoutParams.height = w
        (ibtn.layoutParams as ViewGroup.MarginLayoutParams).marginStart = start

        ibtn = mFragmentView.findViewById<ImageButton>(R.id.live_push_4)
        ibtn.layoutParams.width = w
        ibtn.layoutParams.height = w
        (ibtn.layoutParams as ViewGroup.MarginLayoutParams).marginStart = start

        ibtn = mFragmentView.findViewById<ImageButton>(R.id.live_push_4)
        ibtn.layoutParams.width = w
        ibtn.layoutParams.height = w
        (ibtn.layoutParams as ViewGroup.MarginLayoutParams).marginStart = start

        iv = mFragmentView.findViewById<ImageView>(R.id.live_finger_1)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.live_index_width)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.live_index_end)
        iv.layoutParams.width = w
        iv.layoutParams.height = w
        (iv.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top
        (iv.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = top

        iv = mFragmentView.findViewById<ImageView>(R.id.live_finger_2)
        iv.layoutParams.width = w
        iv.layoutParams.height = w
        (iv.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top
        (iv.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = top

        iv = mFragmentView.findViewById<ImageView>(R.id.live_finger_3)
        iv.layoutParams.width = w
        iv.layoutParams.height = w
        (iv.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top
        (iv.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = top

        iv = mFragmentView.findViewById<ImageView>(R.id.live_finger_4)
        iv.layoutParams.width = w
        iv.layoutParams.height = w
        (iv.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top
        (iv.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = top

        btn = mFragmentView.findViewById<Button>(R.id.btnBack)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.btnBack_width)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.btnBack_margin)
        btn.layoutParams.width = w
        btn.layoutParams.height = w
        (btn.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top
        (btn.layoutParams as ViewGroup.MarginLayoutParams).marginStart = top

        var tv = mFragmentView.findViewById<TextView>(R.id.tv_select_title)
        ts = WidthConverter.getConvertedTextSize(R.dimen.tv_select_title_text_size)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.tv_select_title_top)
        tv.textSize = ts
        (tv.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top

        iv = mFragmentView.findViewById<ImageView>(R.id.iv_select_index)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.iv_select_index_width)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.iv_select_index_top)
        iv.layoutParams.width = w
        iv.layoutParams.height = w
        (iv.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top

        cl = mFragmentView.findViewById<ConstraintLayout>(R.id.select_cl)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.select_cl_top)
        (cl.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top

        ll = mFragmentView.findViewById<LinearLayout>(R.id.usbcam_ll)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.usbcam_ll_width)
        h = WidthConverter.getConvertedWidth(TAG, R.dimen.usbcam_ll_height)
        ll.layoutParams.width = w
        ll.layoutParams.height = h
        end = WidthConverter.getConvertedWidth(TAG, R.dimen.usbcam_ll_end)
        (ll.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = end

        tv = mFragmentView.findViewById<TextView>(R.id.usbcam_title)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.usbcam_title_width)
        h = WidthConverter.getConvertedWidth(TAG, R.dimen.usbcam_title_height)
        tv.layoutParams.width = w
        tv.layoutParams.height = h
        tv.textSize = WidthConverter.getConvertedTextSize(R.dimen.usbcam_title_text_size)

        var rv = mFragmentView.findViewById<RecyclerView>(R.id.usbcam_recycle)
        h = WidthConverter.getConvertedWidth(TAG, R.dimen.usbcam_recycle_height)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.usbcam_recycle_top)
        rv.layoutParams.height = h
        (rv.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top

        ll = mFragmentView.findViewById<LinearLayout>(R.id.ipcam_ll)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.usbcam_ll_width)
        h = WidthConverter.getConvertedWidth(TAG, R.dimen.usbcam_ll_height)
        ll.layoutParams.width = w
        ll.layoutParams.height = h
        start = WidthConverter.getConvertedWidth(TAG, R.dimen.ipcam_ll_start)
        (ll.layoutParams as ViewGroup.MarginLayoutParams).marginStart = start

        tv = mFragmentView.findViewById<TextView>(R.id.ipcam_title)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.usbcam_title_width)
        h = WidthConverter.getConvertedWidth(TAG, R.dimen.usbcam_title_height)
        tv.layoutParams.width = w
        tv.layoutParams.height = h
        tv.textSize = WidthConverter.getConvertedTextSize(R.dimen.usbcam_title_text_size)

        rv = mFragmentView.findViewById<RecyclerView>(R.id.ipcam_recycle)
        h = WidthConverter.getConvertedWidth(TAG, R.dimen.usbcam_recycle_height)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.usbcam_recycle_top)
        rv.layoutParams.height = h
        (rv.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top

        ll = mFragmentView.findViewById<LinearLayout>(R.id.connection_ll)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.connection_ll_top)
        (ll.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top

        var pb = mFragmentView.findViewById<ProgressBar>(R.id.pb_progress)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.pb_progress_width)
        pb.layoutParams.width = w
        pb.layoutParams.height = w

        tv = mFragmentView.findViewById<TextView>(R.id.tv_connection)
        start = WidthConverter.getConvertedWidth(TAG, R.dimen.tv_connection_start)
        (ll.layoutParams as ViewGroup.MarginLayoutParams).marginStart = start
        tv.textSize = WidthConverter.getConvertedTextSize(R.dimen.tv_connection_text_size)

        tv = mFragmentView.findViewById<TextView>(R.id.tv_connection_fail)
        tv.textSize = WidthConverter.getConvertedTextSize(R.dimen.tv_connection_fail_text_size)

        ll = mFragmentView.findViewById<LinearLayout>(R.id.disconnect_ll)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.disconnect_ll_top)
        (ll.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top

        btn = mFragmentView.findViewById<Button>(R.id.btn_disconnect)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.btn_disconnect_width)
        btn.layoutParams.width = w
        btn.layoutParams.height = w

        tv = mFragmentView.findViewById<TextView>(R.id.tv_disconnect)
        tv.textSize = WidthConverter.getConvertedTextSize(R.dimen.tv_connection_text_size)

        ll = mFragmentView.findViewById<LinearLayout>(R.id.auth_ll)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.select_cl_top)
        (ll.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top

        ll = mFragmentView.findViewById<LinearLayout>(R.id.cl_auth_info)
        h = WidthConverter.getConvertedWidth(TAG, R.dimen.info_root_height)
        ll.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        ll.layoutParams.height = h

        tv = mFragmentView.findViewById<TextView>(R.id.tv_auth_title)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.select_cl_top)
        (tv.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top
        tv.textSize = WidthConverter.getConvertedTextSize(R.dimen.tv_auth_title_text_size)

        var et = mFragmentView.findViewById<EditText>(R.id.et_auth_password)
        w = WidthConverter.getConvertedWidth(TAG, R.dimen.et_auth_password_width)
        h = WidthConverter.getConvertedWidth(TAG, R.dimen.et_auth_password_height)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.et_auth_password_top)
        et.layoutParams.width = w
        et.layoutParams.height = h
        (et.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top
        et.textSize = WidthConverter.getConvertedTextSize(R.dimen.et_auth_password_text_size)

        tv = mFragmentView.findViewById<TextView>(R.id.tv_auth_error_hint)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.tv_auth_error_hint_top)
        (tv.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top
        tv.textSize = WidthConverter.getConvertedTextSize(R.dimen.tv_auth_error_hint_text_size)

        btn = mFragmentView.findViewById<Button>(R.id.btn_auth_continue)
        top = WidthConverter.getConvertedWidth(TAG, R.dimen.btn_auth_continue_top)
        (btn.layoutParams as ViewGroup.MarginLayoutParams).topMargin = top
        btn.textSize = WidthConverter.getConvertedTextSize(R.dimen.btn_auth_continue_text_size)

    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View? {
        mContext = requireContext()
        var resObj = inflater.inflate(R.layout.fragment_video, container, false)
        mFragmentView = resObj
        mVideoLayout = resObj.findViewById<ConstraintLayout>(R.id.Image_live_ivView_4)
        mSelectLayout = resObj.findViewById<ConstraintLayout>(R.id.source_select_layout)
        mSelectListView = resObj.findViewById<ConstraintLayout>(R.id.select_cl)
        mSelectAuthView = resObj.findViewById<LinearLayout>(R.id.auth_ll)

        mTextureView = resObj.findViewById(R.id.Image_live_ivView_4_1)
        mTextureView2 = resObj.findViewById(R.id.Image_live_ivView_4_2)
        mTextureView3 = resObj.findViewById(R.id.Image_live_ivView_4_3)
        mTextureView4 = resObj.findViewById(R.id.Image_live_ivView_4_4)

        mUsbCamInfoList.add(CamInfo("VB130E", "192.168.1.28"))
        mUsbCamInfoList.add(CamInfo("VB130E", "192.168.1.1"))
        mUsbCamInfoList.add(CamInfo("VB130E", "192.168.1.2"))
        mUsbCamInfoList.add(CamInfo("VB130E", "192.168.1.3"))
        mUsbCamInfoList.add(CamInfo("VB130E", "192.168.1.4"))
        mUsbCamInfoList.add(CamInfo("VB130E", "192.168.1.5"))
        mUsbCamInfoList.add(CamInfo("VB130E", "192.168.1.6"))
        mIpCamInfoList.add(CamInfo("VB130E", "192.168.1.28"))
        mIpCamInfoList.add(CamInfo("VB130E", "192.168.1.1"))
        mIpCamInfoList.add(CamInfo("VB130E", "192.168.1.2"))
        mIpCamInfoList.add(CamInfo("VB130E", "192.168.1.3"))
        mIpCamInfoList.add(CamInfo("VB130E", "192.168.1.4"))
        mIpCamInfoList.add(CamInfo("VB130E", "192.168.1.5"))
        mIpCamInfoList.add(CamInfo("VB130E", "192.168.1.6"))

        mItemActionListener = object: CamSelectAdapter.ItemActionListener{
            override fun onEditClick(info: CamInfo?) {
            }

            override fun onDeleteClick(info: CamInfo?) {
            }

            override fun onFavoriteClick(info: CamInfo?) {
            }

            override fun onItemLongClick(info: CamInfo?) {
            }

            override fun onItemClick(type: String, pos: Int, view: View) {
                var cnt = 0
//                mUsbCamInfoList.forEach{ info ->
//                    if(info.isSelect){
//                        cnt++
//                    }
//                }
                Log.v(TAG, "onItemClick: $type $pos select $cnt")
                mIpRecyclerView.requestDisallowInterceptTouchEvent(false)
                val selectedInfo = if(type == "UsbCam"){
                    mUsbCamInfoList[pos]
                }else{
                    mIpCamInfoList[pos]
                }

                mSelectLayout.findViewById<LinearLayout>(R.id.connection_ll).visibility = View.VISIBLE
                mSelectLayout.findViewById<LinearLayout>(R.id.progress_ll).visibility = View.VISIBLE

                mUiHandler.postDelayed(Runnable {
                    mUsbCamSelectAdapter.isClick = true
                    mIpCamSelectAdapter.isClick = true
                    if (type == "UsbCam") {
                        mVideoLayout.visibility = View.VISIBLE
                        mSelectLayout.visibility = View.INVISIBLE
                        selectedInfo.isStreaming = true
                        selectedInfo.windowNumber = mWindowClickId
                        mUsbCamSelectAdapter.notifyDataSetChanged()
                        mIpCamSelectAdapter.notifyDataSetChanged()
                    } else {
                        mSelectListView.visibility = View.GONE
                        mSelectAuthView.visibility = View.VISIBLE
                        mSelectAuthView.findViewById<TextView>(R.id.txtName).text = selectedInfo.Name
                        mSelectAuthView.findViewById<TextView>(R.id.txtIp).text = selectedInfo.Ip
                        selectedInfo.isStreaming = true
                        selectedInfo.windowNumber = mWindowClickId
                        mUsbCamSelectAdapter.notifyDataSetChanged()
                        mIpCamSelectAdapter.notifyDataSetChanged()
                    }
                }, 2000)
                when(mWindowClickId){
                    1 -> {
                        mConnector1 = TextureViewConnector(requireContext(), loginIP, mTextureView, "1", mLiveQueue)
                        mFragmentView.findViewById<ImageView>(R.id.live_index_1).visibility = View.VISIBLE
                        mFragmentView.findViewById<ImageView>(R.id.live_push_1).visibility = View.VISIBLE
                        mFragmentView.findViewById<ImageView>(R.id.live_finger_1).visibility = View.VISIBLE
                        mFragmentView.findViewById<TextView>(R.id.live_name_1).text = "${selectedInfo.Name} | ${selectedInfo.Ip}"
                    }
                    2 -> {
                        mConnector2 = TextureViewConnector(requireContext(), loginIP, mTextureView2, "2", mLiveQueue)
                        mFragmentView.findViewById<ImageView>(R.id.live_index_2).visibility = View.VISIBLE
                        mFragmentView.findViewById<ImageView>(R.id.live_push_2).visibility = View.VISIBLE
                        mFragmentView.findViewById<ImageView>(R.id.live_finger_2).visibility = View.VISIBLE
                        mFragmentView.findViewById<TextView>(R.id.live_name_2).text = "${selectedInfo.Name} | ${selectedInfo.Ip}"
                    }
                    3 -> {
                        mConnector3 = TextureViewConnector(requireContext(), loginIP, mTextureView3, "3", mLiveQueue)
                        mFragmentView.findViewById<ImageView>(R.id.live_index_3).visibility = View.VISIBLE
                        mFragmentView.findViewById<ImageView>(R.id.live_push_3).visibility = View.VISIBLE
                        mFragmentView.findViewById<ImageView>(R.id.live_finger_3).visibility = View.VISIBLE
                        mFragmentView.findViewById<TextView>(R.id.live_name_3).text = "${selectedInfo.Name} | ${selectedInfo.Ip}"
                    }
                    4 -> {
                        mConnector4 = TextureViewConnector(requireContext(), loginIP, mTextureView4, "4", mLiveQueue)
                        mFragmentView.findViewById<ImageView>(R.id.live_index_4).visibility = View.VISIBLE
                        mFragmentView.findViewById<ImageView>(R.id.live_push_4).visibility = View.VISIBLE
                        mFragmentView.findViewById<ImageView>(R.id.live_finger_4).visibility = View.VISIBLE
                        mFragmentView.findViewById<TextView>(R.id.live_name_4).text = "${selectedInfo.Name} | ${selectedInfo.Ip}"
                    }
                }
            }

            override fun onCallDisconnect(windowNumber: Int) {
                Log.v(TAG, "onCallDisconnect: usb $windowNumber")
                if(windowNumber != -1){
                    when(windowNumber){
                        2 -> {
                            if (mConnector2 != null) {
                                mConnector2?.close()
                                mFragmentView.findViewById<LinearLayout>(R.id.live_container_2).removeView(mTextureView2)
                                mFragmentView.findViewById<LinearLayout>(R.id.live_container_2).addView(mTextureView2)

                                mFragmentView.findViewById<ImageView>(R.id.live_index_2).visibility = View.INVISIBLE
                                mFragmentView.findViewById<ImageView>(R.id.live_push_2).visibility = View.INVISIBLE
                                mFragmentView.findViewById<ImageView>(R.id.live_finger_2).visibility = View.INVISIBLE
                                mFragmentView.findViewById<TextView>(R.id.live_name_2).text = context?.resources?.getText(R.string.select_device)
                            }
                        }
                        3 -> {
                            if (mConnector3 != null) {
                                mConnector3?.close()
                                resObj.findViewById<LinearLayout>(R.id.live_container_3).removeView(mTextureView3)
                                resObj.findViewById<LinearLayout>(R.id.live_container_3).addView(mTextureView3)

                                mFragmentView.findViewById<ImageView>(R.id.live_index_3).visibility = View.INVISIBLE
                                mFragmentView.findViewById<ImageView>(R.id.live_push_3).visibility = View.INVISIBLE
                                mFragmentView.findViewById<ImageView>(R.id.live_finger_3).visibility = View.INVISIBLE
                                mFragmentView.findViewById<TextView>(R.id.live_name_3).text = context?.resources?.getText(R.string.select_device)
                            }
                        }
                        4 -> {
                            if (mConnector4 != null) {
                                mConnector4?.close()
                                resObj.findViewById<LinearLayout>(R.id.live_container_4).removeView(mTextureView4)
                                resObj.findViewById<LinearLayout>(R.id.live_container_4).addView(mTextureView4)

                                mFragmentView.findViewById<ImageView>(R.id.live_index_4).visibility = View.INVISIBLE
                                mFragmentView.findViewById<ImageView>(R.id.live_push_4).visibility = View.INVISIBLE
                                mFragmentView.findViewById<ImageView>(R.id.live_finger_4).visibility = View.INVISIBLE
                                mFragmentView.findViewById<TextView>(R.id.live_name_4).text = context?.resources?.getText(R.string.select_device)
                            }
                        }
                        else -> {
                            if(mConnector1 != null){
                                mConnector1?.close()
                                mFragmentView.findViewById<LinearLayout>(R.id.live_container_1).removeView(mTextureView)
                                resObj.findViewById<LinearLayout>(R.id.live_container_1).addView(mTextureView)

                                mFragmentView.findViewById<ImageView>(R.id.live_index_1).visibility = View.INVISIBLE
                                mFragmentView.findViewById<ImageView>(R.id.live_push_1).visibility = View.INVISIBLE
                                mFragmentView.findViewById<ImageView>(R.id.live_finger_1).visibility = View.INVISIBLE
                                mFragmentView.findViewById<TextView>(R.id.live_name_1).text = context?.resources?.getText(R.string.select_device)

                            }
                        }
                    }
                    mUsbCamInfoList.forEach { info ->
                        if(info.windowNumber == windowNumber){
                            info.isStreaming = false
                            info.windowNumber = -1
                            Log.v(TAG, "onCallDisconnect: usb ${info.Ip}")
                            mUsbCamSelectAdapter.notifyDataSetChanged()
                        }
                    }
                    mIpCamInfoList.forEach { info ->
                        if(info.windowNumber == windowNumber){
                            info.isStreaming = false
                            info.windowNumber = -1
                            Log.v(TAG, "onCallDisconnect: ip ${info.Ip}")
                            mIpCamSelectAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }

            override fun disableOnClick() {
                mIpCamSelectAdapter.setItemClickable(mIpRecyclerView, false)
                mUsbCamSelectAdapter.setItemClickable(mUsbRecyclerView, false)
            }
        }
        mUsbCamSelectAdapter = CamSelectAdapter(requireContext(), "UsbCam", mUsbCamInfoList, mItemActionListener)
        mUsbRecyclerView = resObj.findViewById<RecyclerView>(R.id.usbcam_recycle)
        mUsbRecyclerView.layoutManager = LinearLayoutManager(context)
        mUsbRecyclerView.adapter = mUsbCamSelectAdapter
        mUsbCamSelectAdapter.mList = mUsbCamInfoList
        mUsbCamSelectAdapter.notifyDataSetChanged()

        mIpCamSelectAdapter = CamSelectAdapter(requireContext(), "IpCam", mIpCamInfoList, mItemActionListener)
        mIpRecyclerView = resObj.findViewById<RecyclerView>(R.id.ipcam_recycle)
        mIpRecyclerView.layoutManager = LinearLayoutManager(context)
        mIpRecyclerView.adapter = mIpCamSelectAdapter
        mIpCamSelectAdapter.mList = mIpCamInfoList
        mIpCamSelectAdapter.notifyDataSetChanged()

        resObj.findViewById<Button>(R.id.live_name_1).setOnClickListener(View.OnClickListener {
            mWindowClickId = 1
            initSelectionLayout()
            mVideoLayout.visibility = View.INVISIBLE
            mSelectLayout.visibility = View.VISIBLE
            mIpCamInfoList.forEach { info ->
                if (info.windowNumber == mWindowClickId && info.isStreaming) {
                    mBtnDisconnect.visibility = View.VISIBLE
                }
            }
            mUsbCamInfoList.forEach { info ->
                if (info.windowNumber == mWindowClickId && info.isStreaming) {
                    mBtnDisconnect.visibility = View.VISIBLE
                }
            }
            mUsbCamSelectAdapter.setWindowNumber(mWindowClickId)
            mUsbCamSelectAdapter.notifyDataSetChanged()
            mIpCamSelectAdapter.setWindowNumber(mWindowClickId)
            mIpCamSelectAdapter.notifyDataSetChanged()
        })
        resObj.findViewById<Button>(R.id.live_name_2).setOnClickListener(View.OnClickListener {
            mWindowClickId = 2
            initSelectionLayout()
            mVideoLayout.visibility = View.INVISIBLE
            mSelectLayout.visibility = View.VISIBLE
            mUsbCamSelectAdapter.setWindowNumber(2)
            mUsbCamSelectAdapter.notifyDataSetChanged()
            mIpCamSelectAdapter.setWindowNumber(2)
            mIpCamSelectAdapter.notifyDataSetChanged()
        })
        resObj.findViewById<Button>(R.id.live_name_3).setOnClickListener(View.OnClickListener {
            mWindowClickId = 3
            initSelectionLayout()
            mVideoLayout.visibility = View.INVISIBLE
            mSelectLayout.visibility = View.VISIBLE
            mUsbCamSelectAdapter.setWindowNumber(3)
            mUsbCamSelectAdapter.notifyDataSetChanged()
            mIpCamSelectAdapter.setWindowNumber(3)
            mIpCamSelectAdapter.notifyDataSetChanged()
        })
        resObj.findViewById<Button>(R.id.live_name_4).setOnClickListener(View.OnClickListener {
            mWindowClickId = 4
            initSelectionLayout()
            mVideoLayout.visibility = View.INVISIBLE
            mSelectLayout.visibility = View.VISIBLE
            mUsbCamSelectAdapter.setWindowNumber(4)
            mUsbCamSelectAdapter.notifyDataSetChanged()
            mIpCamSelectAdapter.setWindowNumber(4)
            mIpCamSelectAdapter.notifyDataSetChanged()
        })

        resObj.findViewById<Button>(R.id.btnBack).setOnClickListener(View.OnClickListener {
            mVideoLayout.visibility = View.VISIBLE
            mSelectLayout.visibility = View.INVISIBLE
        })

        mBtnDisconnect = resObj.findViewById<LinearLayout>(R.id.disconnect_ll)
        val bntDisconnect = resObj.findViewById<Button>(R.id.btn_disconnect)
        bntDisconnect.setOnClickListener {
            mItemActionListener.onCallDisconnect(mWindowClickId)
            mBtnDisconnect.visibility = View.GONE
        }

        updateLayoutSize()
        isInit = true

        mUiHandler.postDelayed(Runnable {
//            mConnector1 = TextureViewConnector(requireContext(), loginIP, mTextureView, "1", mLiveQueue)
//            mConnector2 = TextureViewConnector(requireContext(), loginIP, mTextureView2, "2", mLiveQueue)
//            mConnector3 = TextureViewConnector(requireContext(), loginIP2, mTextureView3, "3", mLiveQueue)
//            mConnector4 = TextureViewConnector(requireContext(), loginIP2, mTextureView4, "4", mLiveQueue)
//            mConnector1.setInfoView(TextView(requireContext()), mUiHandler)
//            mConnector2.setInfoView(TextView(requireContext()), mUiHandler)
//            mConnector3.setInfoView(TextView(requireContext()), mUiHandler)
//            mConnector4.setInfoView(TextView(requireContext()), mUiHandler)
        }, 1500)
        return resObj
    }

    fun initLayout(){
        mFragmentView.findViewById<ConstraintLayout>(R.id.Image_live_ivView_4).visibility = View.VISIBLE
        mFragmentView.findViewById<ImageView>(R.id.live_index_1).visibility = View.INVISIBLE
        mFragmentView.findViewById<ImageView>(R.id.live_push_1).visibility = View.INVISIBLE
        mFragmentView.findViewById<ImageView>(R.id.live_finger_1).visibility = View.INVISIBLE
        mFragmentView.findViewById<TextView>(R.id.live_name_1).text = context?.resources?.getText(R.string.select_device)

        mFragmentView.findViewById<ImageView>(R.id.live_index_2).visibility = View.INVISIBLE
        mFragmentView.findViewById<ImageView>(R.id.live_push_2).visibility = View.INVISIBLE
        mFragmentView.findViewById<ImageView>(R.id.live_finger_2).visibility = View.INVISIBLE
        mFragmentView.findViewById<TextView>(R.id.live_name_2).text = context?.resources?.getText(R.string.select_device)

        mFragmentView.findViewById<ImageView>(R.id.live_index_3).visibility = View.INVISIBLE
        mFragmentView.findViewById<ImageView>(R.id.live_push_3).visibility = View.INVISIBLE
        mFragmentView.findViewById<ImageView>(R.id.live_finger_3).visibility = View.INVISIBLE
        mFragmentView.findViewById<TextView>(R.id.live_name_3).text = context?.resources?.getText(R.string.select_device)

        mFragmentView.findViewById<ImageView>(R.id.live_index_4).visibility = View.INVISIBLE
        mFragmentView.findViewById<ImageView>(R.id.live_push_4).visibility = View.INVISIBLE
        mFragmentView.findViewById<ImageView>(R.id.live_finger_4).visibility = View.INVISIBLE
        mFragmentView.findViewById<TextView>(R.id.live_name_4).text = context?.resources?.getText(R.string.select_device)

        initSelectionLayout()
    }

    private fun initSelectionLayout(){
        mFragmentView.findViewById<ConstraintLayout>(R.id.source_select_layout).visibility = View.GONE
        mFragmentView.findViewById<ConstraintLayout>(R.id.select_cl).visibility = View.VISIBLE
        val drawableId = when(mWindowClickId){
            2 -> mContext.resources?.getDrawable(R.drawable.ic__2t, null)
            3 -> mContext.resources?.getDrawable(R.drawable.ic__3t, null)
            4 -> mContext.resources?.getDrawable(R.drawable.ic__4t, null)
            else -> mContext.resources?.getDrawable(R.drawable.ic__1t, null)
        }
        mFragmentView.findViewById<ImageView>(R.id.iv_select_index).background = drawableId

        mFragmentView.findViewById<LinearLayout>(R.id.connection_ll).visibility = View.GONE
        mFragmentView.findViewById<LinearLayout>(R.id.progress_ll).visibility = View.GONE
        mFragmentView.findViewById<TextView>(R.id.tv_connection_fail).visibility = View.GONE
        mBtnDisconnect.visibility = View.GONE

        mFragmentView.findViewById<LinearLayout>(R.id.auth_ll).visibility = View.GONE
        mFragmentView.findViewById<ViewGroup>(R.id.cl_auth_info).isSelected = true
        mFragmentView.findViewById<EditText>(R.id.et_auth_password).text.clear()
        mFragmentView.findViewById<Button>(R.id.btn_auth_continue).isEnabled = false
        mFragmentView.findViewById<TextView>(R.id.tv_auth_error_hint).visibility = View.INVISIBLE

        val c = mContext!!.resources!!.getColor(R.color.usbcam_title_bg, null)
        val info = mFragmentView.findViewById<LinearLayout>(R.id.cl_auth_info)
        info.findViewById<ImageView>(R.id.ivVideoIndex).visibility = View.GONE
        info.findViewById<TextView>(R.id.txtName).text = ""
        info.findViewById<TextView>(R.id.txtName).setTextColor(c)
        info.findViewById<TextView>(R.id.txtIp).text = ""
        info.findViewById<TextView>(R.id.txtIp).setTextColor(c)
        info.findViewById<TextView>(R.id.center_line).setTextColor(c)

    }

    override fun onClick(v: View) {
        @SuppressLint("SetTextI18n")
        if (v is Button){
//            mEdtIp.setText(edtIp.text.toString() + v.text)
        }
    }

    override fun onResume() {
        super.onResume()
        initLayout()
    }

    //////////////////////
    // Private Function //
    //////////////////////

    /////////////////////
    // Public Function //
    /////////////////////
    companion object {
        val mToken = ""
        fun isValidText(ip: String?): Boolean {
            val regex = ("[a-zA-Z0-9]+")
            val p: Pattern = Pattern.compile(regex)
            val m: Matcher = p.matcher(ip)
            return m.matches()
        }

        fun isValidIPAddress(ip: String?): Boolean {
            // Regex for digit from 0 to 255.
            val zeroTo255 = ("(\\d{1,2}|(0|1)\\" + "d{2}|2[0-4]\\d|25[0-5])")
            // Regex for a digit from 0 to 255 and
            // followed by a dot, repeat 4 times.
            // this is the regex to validate an IP address.
            val regex = (zeroTo255 + "\\."
                    + zeroTo255 + "\\."
                    + zeroTo255 + "\\."
                    + zeroTo255)
            // Compile the ReGex
            val p: Pattern = Pattern.compile(regex)
            // If the IP address is empty
            // return false
            if (ip == null) {
                return false
            }
            // Pattern class contains matcher() method
            // to find matching between given IP address
            // and regular expression.
            val m: Matcher = p.matcher(ip)
            // Return if the IP address
            // matched the ReGex
            return m.matches()
        }
    }
}