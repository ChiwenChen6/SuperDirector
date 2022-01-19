package com.aver.superdirector

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aver.superdirector.MainActivity.*
import com.aver.superdirector.MainActivity.Companion.cameraItemAdapterUSB
import com.aver.superdirector.MainActivity.Companion.cameraItemList
import com.aver.superdirector.MainActivity.Companion.currentCameraItemUSB
import com.aver.superdirector.utility.*
import kotlinx.android.synthetic.main.dialog_add_contact.view.*
import kotlinx.android.synthetic.main.fragment_bind.*
import kotlinx.android.synthetic.main.fragment_bind.view.*
import kotlinx.android.synthetic.main.fragment_contact.view.*
import kotlinx.android.synthetic.main.fragment_keypad.*
import java.util.*
import kotlin.collections.ArrayList


/**
 * A simple [Fragment] subclass.
 */
class BindFragment : Fragment(), CardViewListenerInterface, View.OnClickListener {
    private val TAG = "BindFragment"
    private var mIsFavorite = false

    private lateinit var mViewAccountList: RecyclerView
    private var mAdapter: CameraItemAdapter? = null

    // private lateinit var mLayoutManager: RecyclerView.LayoutManager
    // 這個數量關係到後面增加時的位置計算
    //private var cameraItemAdapter = CameraItemAdapter( null, this  )
    private val mAccountList = ArrayList<AVerAccountInfo>()
    private val mFavoriteList = ArrayList<AVerAccountInfo>()
    private val mSearchAccountList = ArrayList<AVerAccountInfo>()
    private var mContentResolver: ContentResolver? = null
    private var mH323Selected = true
    private var isInit = false
    private lateinit var BindFragmentView: View
    private lateinit var mDialCallback: VideoFragment.IDialCallback
    private lateinit var mFragmentCallback: MainActivity.FragmentCallback

    private var mDelayTimer: Timer? = null
    private var timer: Timer? = null

    private val mMainHandler = Handler(Looper.getMainLooper())
    private var cpProgress: CircleProgressBar? = null

    @SuppressLint("RestrictedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.v(TAG, "onCreateView: ")
        // Inflate the layout for this fragment        editText1.setOnFocusChangeListener(this);
        BindFragmentView = inflater.inflate(R.layout.fragment_bind, container, false)

        BindFragmentView.setOnClickListener(View.OnClickListener {

        })
        val cameraViewVERTICALUSB =
            BindFragmentView.findViewById<RecyclerView>(R.id.bind_viewAccountList) //id RecyclerView
        cameraViewVERTICALUSB.layoutManager = LinearLayoutManager(
            context,
            LinearLayoutManager.VERTICAL, false
        )
        // 這個數量關係到後面增加時的位置計算
        cameraItemAdapterUSB = CameraItemAdapter(cameraItemList, this)
        cameraViewVERTICALUSB.adapter = cameraItemAdapterUSB
        cameraViewVERTICALUSB.scrollToPosition(0)
        BindFragmentView.isClickable = false

        cpProgress = BindFragmentView.findViewById(R.id.cp_progress_bind)

        BindFragmentView.btnscan.setOnClickListener(View.OnClickListener {
            (activity as MainActivity?)!!.findUSBCamera()
            mDelayTimer = Timer()

            cpProgress!!.progress = 0
            timer = Timer()
            val task: TimerTask = object : TimerTask() {
                override fun run() {
                    var progress: Int = cpProgress!!.progress
                    cpProgress!!.progress = ++progress
                    Log.v("AVDebug", "progress : " + progress)

                    if (progress > 100 && timer != null) {
                        timer!!.cancel();
                    }

                }
            }
            timer!!.schedule(task, 0, 100)

        })

        BindFragmentView.rbScanType.setOnCheckedChangeListener { radioGroup, i ->
            val id: Int = rbScanType.checkedRadioButtonId
            when (id) {
                R.id.rbScanType_scan -> {
                    BindFragmentView.Bind_scanTip.visibility = View.VISIBLE
                    BindFragmentView.Bind_scanConstraintLayout.visibility = View.VISIBLE
                    BindFragmentView.Bind_manualConstraintLayout.visibility = View.INVISIBLE
                }
                R.id.rbScanType_manual -> {
                    if (BindFragmentView.Bind_scanTip.visibility == View.VISIBLE) {
                        BindFragmentView.Bind_scanTip.visibility = View.INVISIBLE

                    }
                    BindFragmentView.edtManualIP1.focusable = View.FOCUSABLE
                    BindFragmentView.Bind_scanConstraintLayout.visibility = View.INVISIBLE
                    BindFragmentView.Bind_manualConstraintLayout.visibility = View.VISIBLE
                    //預先發送搜尋 來存UCID
                    edtManualIP1.requestFocus()
                    edtManualIP1.focusable = View.FOCUSABLE
                    edtManualIP1.requestFocus()
                    (activity as MainActivity?)!!.findUSBCameraData()

                }

                else -> {
                }
            }
        }

        //key pad set listener
        BindFragmentView.btnDial0.setOnClickListener(this)
        BindFragmentView.btnDial1.setOnClickListener(this)
        BindFragmentView.btnDial2.setOnClickListener(this)
        BindFragmentView.btnDial3.setOnClickListener(this)
        BindFragmentView.btnDial4.setOnClickListener(this)
        BindFragmentView.btnDial5.setOnClickListener(this)
        BindFragmentView.btnDial6.setOnClickListener(this)
        BindFragmentView.btnDial7.setOnClickListener(this)
        BindFragmentView.btnDial8.setOnClickListener(this)
        BindFragmentView.btnDial9.setOnClickListener(this)
        BindFragmentView.btnDialNext.setOnClickListener(this)
        BindFragmentView.btnDialClear.setOnClickListener(this)


        // 四個輸入框皆不顯示虛擬鍵盤
        BindFragmentView.edtManualIP1.inputType = InputType.TYPE_NULL;
        BindFragmentView.edtManualIP1.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_FLAG_NO_ENTER_ACTION
            ) {
                // hide virtual keyboard
                val imm: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(BindFragmentView.edtManualIP1.windowToken, 0)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        BindFragmentView.edtManualIP1.focusable = View.FOCUSABLE

        BindFragmentView.edtManualIP2.inputType = InputType.TYPE_NULL;
        BindFragmentView.edtManualIP2.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_FLAG_NO_ENTER_ACTION
            ) {
                // hide virtual keyboard
                val imm: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(BindFragmentView.edtManualIP2.windowToken, 0)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        BindFragmentView.edtManualIP3.inputType = InputType.TYPE_NULL;
        BindFragmentView.edtManualIP3.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_FLAG_NO_ENTER_ACTION
            ) {
                // hide virtual keyboard
                val imm: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(BindFragmentView.edtManualIP3.windowToken, 0)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }
        BindFragmentView.edtManualIP4.inputType = InputType.TYPE_NULL;
        BindFragmentView.edtManualIP4.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT ||
                actionId == EditorInfo.IME_FLAG_NO_ENTER_ACTION
            ) {
                // hide virtual keyboard
                val imm: InputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(BindFragmentView.edtManualIP4.windowToken, 0)
                return@setOnEditorActionListener true
            }
            return@setOnEditorActionListener false
        }

        //目前沒有返回鍵
        //BindFragmentView.btnBack.setOnClickListener(View.OnClickListener {

        //    mAdapter?.setUSBAccountList(MainActivity.USBItemList)
        //    mAdapter?.notifyDataSetChanged()

        //})

        BindFragmentView.btnBindingScan.setOnClickListener(View.OnClickListener {
            Log.v("AVDebug", "Button click: btnBindingScan")


            var BindIPAppend =
                edtManualIP1.text.toString() + edtManualIP2.text.toString() + edtManualIP3.text.toString() + edtManualIP4.text.toString()
            var trueUCID = (activity as MainActivity?)!!.conpareIPSaved_UCID(BindIPAppend)
            var modelName = (activity as MainActivity?)!!.conpareIPSaved_modelname(BindIPAppend)

            (activity as MainActivity?)!!.usbDeviceDetectState(BindIPAppend, trueUCID)

        })


        mContentResolver = context?.contentResolver
        mViewAccountList = BindFragmentView.bind_viewAccountList
        // mLayoutManager = GridLayoutManager(activity,2)


        return BindFragmentView
    }

    override fun onResume() {
        super.onResume()
        BindFragmentView.rbScanType.check(R.id.rbScanType_scan)
        if (BindFragmentView.Bind_scanTip.visibility == View.INVISIBLE) {
            BindFragmentView.Bind_scanTip.visibility = View.VISIBLE

        }
        BindFragmentView.Bind_scanConstraintLayout.visibility = View.VISIBLE
        BindFragmentView.Bind_manualConstraintLayout.visibility = View.INVISIBLE
    }

    private fun createAccountList() {
        mAccountList.clear()
        mFavoriteList.clear()
        mAdapter?.notifyDataSetChanged()
        var cursor: Cursor?
        mAdapter?.setAccountList(MainActivity.cameraItemList)
        mAdapter?.notifyDataSetChanged()


    }

    /////////////////////
    // Public Function //
    /////////////////////

    fun setFavorite(isFavorite: Boolean) {
        Log.v(TAG, "setFavorite: $isFavorite")
        mIsFavorite = isFavorite
        var sipH323 = if (mH323Selected) {
            "H323"
        } else {
            "SIP"
        }

        if (isInit) {

        }
        setSipH323(sipH323)
    }

    fun setSipH323(sipH323: String) {
        Log.v(TAG, "setSipH323: $sipH323")
        mH323Selected = sipH323 == "H323"
        if (isInit) {
            createAccountList()
        }
    }

    fun setCallback(cb: VideoFragment.IDialCallback) {
        mDialCallback = cb
    }

    fun setFragmentCallback(cb: MainActivity.FragmentCallback) {
        mFragmentCallback = cb
    }

    //////////////////////
    // Private Function //
    //////////////////////


    override fun onItemClicked(position: Int) {
        currentCameraItemUSB = cameraItemAdapterUSB!!.getCameraItem(position)
        var UCID = currentCameraItemUSB?.ip?.let {
            (activity as MainActivity?)!!.conpareIPSaved_modelname(
                it
            )
        }
        Log.v("AVDebug", "UCID :$UCID")
        currentCameraItemUSB?.ip?.let { (activity as MainActivity?)!!.usbDeviceDetectState(it,UCID) }

    }

    override fun onClick(v: View?) {
        // key button clickes
        @SuppressLint("SetTextI18n")
        if (v is Button)
        ////////////
        //下一格按鈕//
        ////////////

            if (v.text.equals("Next")) {


                if (edtManualIP4.hasFocus()) {
                    edtManualIP4.clearFocus()
                } else if (edtManualIP3.hasFocus()) {
                    edtManualIP3.clearFocus()
                    edtManualIP4.setSelection(edtManualIP4.text.toString().length)
                    edtManualIP4.focusable = View.FOCUSABLE
                    edtManualIP4.requestFocus()
                } else if (edtManualIP2.hasFocus()) {
                    edtManualIP2.clearFocus()
                    edtManualIP3.requestFocus()
                    edtManualIP3.setSelection(edtManualIP3.text.toString().length)
                    edtManualIP3.focusable = View.FOCUSABLE
                    edtManualIP3.requestFocus()
                } else if (edtManualIP1.hasFocus()) {
                    edtManualIP1.clearFocus()
                    edtManualIP2.setSelection(edtManualIP2.text.toString().length)
                    edtManualIP2.focusable = View.FOCUSABLE
                    edtManualIP2.requestFocus()
                }
                Log.v(TAG, "Next 4: " + edtManualIP4.hasFocus())
                Log.v(TAG, "Next 3: " + edtManualIP3.hasFocus())
                Log.v(TAG, "Next 2: " + edtManualIP2.hasFocus())
                Log.v(TAG, "Next 1: " + edtManualIP1.hasFocus())
            } else if (v.text.equals("clear")) {
                if (edtManualIP4.text.toString() != "") {
                    edtManualIP4.text.delete(
                        edtManualIP4.text.toString().length - 1,
                        edtManualIP4.text.toString().length
                    )
                    edtManualIP4.setSelection(edtManualIP4.text.toString().length)
                    edtManualIP4.requestFocus()
                } else {
                    edtManualIP3.requestFocus()
                    edtManualIP3.focusable = View.FOCUSABLE
                    if (edtManualIP3.text.toString() != "") {
                        edtManualIP3.text.delete(
                            edtManualIP3.text.toString().length - 1,
                            edtManualIP3.text.toString().length
                        )
                        edtManualIP3.setSelection(edtManualIP3.text.toString().length)
                        edtManualIP3.requestFocus()
                    } else {
                        edtManualIP2.requestFocus()
                        edtManualIP2.focusable = View.FOCUSABLE
                        if (edtManualIP2.text.toString() != "") {
                            edtManualIP2.text.delete(
                                edtManualIP2.text.toString().length - 1,
                                edtManualIP2.text.toString().length
                            )
                            edtManualIP2.setSelection(edtManualIP2.text.toString().length)
                            edtManualIP2.requestFocus()
                        } else {
                            edtManualIP1.requestFocus()
                            edtManualIP1.focusable = View.FOCUSABLE
                            Log.v(TAG, "selectionStart: " + edtManualIP1.selectionStart)
                            if (edtManualIP1.text.toString() != "") {
                                edtManualIP1.text.delete(
                                    edtManualIP1.text.toString().length - 1,
                                    edtManualIP1.text.toString().length
                                )
                                edtManualIP1.setSelection(edtManualIP1.text.toString().length)
                                edtManualIP1.requestFocus()
                            }
                        }

                    }

                }
            } else {
                ///////////
                //數字按鈕//
                ///////////

                Log.v(
                    "AVDebug",
                    "edtManualIP1.text.toString().length  =>" + edtManualIP1.text.toString().length
                )
                Log.v("AVDebug", "e edtManualIP1.hasFocus =>" + edtManualIP1.hasFocus())

                if (edtManualIP1.text.toString().length < 3 && edtManualIP1.hasFocus()) {
                    edtManualIP1.setSelection(edtManualIP1.text.toString().length)
                    edtManualIP1.setText(edtManualIP1.text.toString() + v.text)
                    edtManualIP1.setSelection(edtManualIP1.text.toString().length)

                } else {
                    if (edtManualIP1.text.toString().length == 3) {
                        edtManualIP1.clearFocus()
                        edtManualIP2.requestFocus()
                        edtManualIP2.focusable = View.FOCUSABLE
                    }
                    if (edtManualIP2.text.toString().length < 3 && edtManualIP2.isFocused) {
                        edtManualIP2.setSelection(edtManualIP2.text.toString().length)
                        edtManualIP2.setText((edtManualIP2.text.toString() + v.text))
                        edtManualIP2.setSelection(edtManualIP2.text.toString().length)

                    } else {
                        if (edtManualIP2.text.toString().length == 3) {
                            edtManualIP2.clearFocus()
                            edtManualIP3.requestFocus()
                            edtManualIP3.focusable = View.FOCUSABLE
                        }
                        if (edtManualIP3.text.toString().length < 3 && edtManualIP3.isFocused) {
                            edtManualIP3.setSelection(edtManualIP3.text.toString().length)
                            edtManualIP3.setText((edtManualIP3.text.toString() + v.text))
                            edtManualIP3.setSelection(edtManualIP3.text.toString().length)

                        } else {
                            if (edtManualIP3.text.toString().length == 3) {
                                edtManualIP3.clearFocus()
                                edtManualIP4.requestFocus()
                                edtManualIP4.focusable = View.FOCUSABLE
                            }

                            edtManualIP4.setSelection(edtManualIP4.text.toString().length)
                            edtManualIP4.setText((edtManualIP4.text.toString() + v.text))
                            edtManualIP4.setSelection(edtManualIP4.text.toString().length)

                        }
                    }
                }


                //////////////////////////////////////////
                //////Scan按鈕判斷   isIPType:Boolean//////
                //////////////////////////////////////////
                var edtIP1: Int
                var edtIP2: Int
                var edtIP3: Int
                var edtIP4: Int
                var isIPType: Boolean


                if (edtManualIP1.text.toString().isNotEmpty() && edtManualIP2.text.toString()
                        .isNotEmpty() && edtManualIP3.text.toString()
                        .isNotEmpty() && edtManualIP4.text.toString()
                        .isNotEmpty()
                ) {

                    try {
                        edtIP1 = Integer.parseInt(edtManualIP1.text.toString())
                        edtIP2 = Integer.parseInt(edtManualIP2.text.toString())
                        edtIP3 = Integer.parseInt(edtManualIP3.text.toString())
                        edtIP4 = Integer.parseInt(edtManualIP4.text.toString())

                        if (edtIP1 < 256 && edtIP2 < 256 && edtIP3 < 256 && edtIP4 < 256) {
                            isIPType = true
                            btnBindingScan.isEnabled = true
                        }
                    } catch (e: Exception) {
                        isIPType = false
                        btnBindingScan.isEnabled = false

                    }
                } else {
                    isIPType = false
                    btnBindingScan.isEnabled = false
                }
            }
    }
}
