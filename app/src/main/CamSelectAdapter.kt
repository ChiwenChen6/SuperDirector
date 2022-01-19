package com.aver.superdirector

import android.content.Context
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.iterator
import androidx.recyclerview.widget.RecyclerView
import com.aver.superdirector.utility.WidthConverter

class CamSelectAdapter() : RecyclerView.Adapter<CamSelectAdapter.ViewHolder>() {
    private val TAG = "CamSelectAdapter"
    var mList: List<CamInfo>? = null
    var mListener: ItemActionListener? = null
    private var mIsDelete = false
    private var mType = ""
    private var mContext: Context? = null
    private var mWindowNumber = -1
    private var mPage = "video"
    var isClick = true

    interface ItemActionListener {
        fun onEditClick(info: CamInfo?)
        fun onDeleteClick(info: CamInfo?)
        fun onFavoriteClick(info: CamInfo?)
        fun onItemLongClick(info: CamInfo?)
        fun onItemClick(type: String, int: Int, info: View)
        fun onCallDisconnect(windowNumber: Int)
        fun disableOnClick()
    }

    constructor(context: Context, type: String, list: List<CamInfo>?, listener: ItemActionListener?) : this() {
        mContext = context
        mType = type
        mList = list
        mListener = listener
    }

    fun setAccountList(list: List<CamInfo>?) {
        mList = list
        notifyDataSetChanged()
    }

    fun setDeleteMode(enable: Boolean) {
        mIsDelete = enable
        notifyDataSetChanged()
    }

    fun getDeleteMode(): Boolean {
        return mIsDelete
    }

    fun setWindowNumber(num: Int){
        mWindowNumber = num
    }

    ////////////////
    // Life Cycle //
    ////////////////

    ////////////////
    // Life Cycle //
    ////////////////
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val tView = LayoutInflater.from(parent.context).inflate(
            R.layout.view_caminfo,
            parent,
            false
        )
        return ViewHolder(tView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tInfo = mList!![position]
        val tvName = holder.TxtName
        val tvIp = holder.TxtIp
        val tvLine = holder.CenterLine
        tvName?.text = tInfo.Name
        tvIp?.text = tInfo.Ip
        Log.v(TAG, "onBindViewHolder: ${holder.itemView.isEnabled}")
        holder.InfoRoot.isActivated = tInfo.isStreaming
        holder.InfoRoot.isEnabled = isClick
        holder.InfoRoot.setOnClickListener(View.OnClickListener {
            if (mPage == "video") {
                if (mType == "UsbCam") {
                    mList!!.forEach { info ->
                        if (info.isStreaming) {
                            mListener?.onCallDisconnect(info.windowNumber)
                        }
                    }
                }

                if (tInfo.isStreaming) {
                    Log.v(TAG, "click 1")
                    if (tInfo.windowNumber == mWindowNumber) {
                        Log.v(TAG, "click 1-1")
                        return@OnClickListener
                    } else {
                        Log.v(TAG, "click 1-2")
                        //disconnect tInfo.windowNumber
                        mListener?.onCallDisconnect(tInfo.windowNumber)

                        //disconnect windowNumber
                        mListener?.onCallDisconnect(mWindowNumber)
                        tInfo.isStreaming = true
                        tInfo.windowNumber = mWindowNumber
                    }
                } else {
                    Log.v(TAG, "click 2")
                    val c = mContext!!.resources!!.getColor(R.color.usbcam_title_bg, null)
                    tvName?.setTextColor(c)
                    tvLine?.setTextColor(c)
                    tvIp?.setTextColor(c)

                    mListener?.onCallDisconnect(mWindowNumber)
                    holder.InfoRoot.isSelected = true
                    holder.InfoRoot.isActivated = true
                    tInfo.isStreaming = true
                    tInfo.windowNumber = mWindowNumber
                }
                if (isClick) {
                    isClick = false
                    mListener?.disableOnClick()
                }
                mListener?.onItemClick(mType, position, it)
            }
        })

        if(tInfo.isStreaming) {
            Log.v(TAG, "mWindowNumber:s $mType $mWindowNumber, $position ${tInfo.windowNumber}")
            holder.InfoRoot.isSelected = mWindowNumber == tInfo.windowNumber
            holder.InfoRoot.isActivated = true
            if(holder.InfoRoot.isSelected) {
                val c = mContext!!.resources!!.getColor(R.color.usbcam_title_bg, null)
                tvName?.setTextColor(c)
                tvLine?.setTextColor(c)
                tvIp?.setTextColor(c)
            }else{
                val c = mContext!!.resources!!.getColor(R.color.white, null)
                tvName?.setTextColor(c)
                tvLine?.setTextColor(c)
                tvIp?.setTextColor(c)
            }
            val drawableId = when (tInfo.windowNumber) {
                2 -> mContext?.resources?.getDrawable(R.drawable.ic__2t, null)
                3 -> mContext?.resources?.getDrawable(R.drawable.ic__3t, null)
                4 -> mContext?.resources?.getDrawable(R.drawable.ic__4t, null)
                else -> mContext?.resources?.getDrawable(R.drawable.ic__1t, null)
            }
            holder.IvVideoIndex?.background = drawableId
            holder.IvVideoIndex?.visibility = View.VISIBLE
        }else{
            Log.v("chuck", "mWindowNumber:ns $mType $mWindowNumber, $position ${tInfo.windowNumber}")
            holder.IvVideoIndex?.visibility = View.INVISIBLE
            holder.InfoRoot.isActivated = false
            holder.InfoRoot.isSelected = false
            val c = mContext!!.resources!!.getColor(R.color.white, null)
            tvName?.setTextColor(c)
            tvLine?.setTextColor(c)
            tvIp?.setTextColor(c)
        }
    }

    fun setItemClickable(view: View?, clickable: Boolean) {
        isClick = false
        if (view != null) {
            val vg = (view as ViewGroup)
            for (child in vg) {
                child.isEnabled = clickable
            }
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return mList!!.size
    }

    //////////////////
    // Class Member //
    //////////////////

    //////////////////
    // Class Member //
    //////////////////
    class ViewHolder(ViewMain: View): RecyclerView.ViewHolder(ViewMain) {
        val TAG = "ViewHolder"
        var InfoRoot = ViewMain
        var TxtName: TextView? = null
        var TxtIp: TextView? = null
        var CenterLine: TextView? = null
        var IvVideoIndex: ImageView? = null

        init {
            InfoRoot = ViewMain.findViewById(R.id.info_root)
            TxtName = ViewMain.findViewById(R.id.txtName)
            TxtIp = ViewMain.findViewById(R.id.txtIp)
            CenterLine = ViewMain.findViewById(R.id.center_line)
            IvVideoIndex = ViewMain.findViewById(R.id.ivVideoIndex)

            val sz = WidthConverter.getConvertedTextSize(R.dimen.info_name_text_size)
            TxtName!!.textSize = sz
            TxtIp!!.textSize = sz
            CenterLine!!.textSize = sz
            InfoRoot.layoutParams.height = WidthConverter.getConvertedWidth(TAG, R.dimen.info_root_height)
            (InfoRoot.layoutParams as ViewGroup.MarginLayoutParams).bottomMargin = WidthConverter.getConvertedWidth(TAG, R.dimen.info_root_bottom)

            var w = WidthConverter.getConvertedWidth(TAG, R.dimen.ivVideoIndex_width)
            IvVideoIndex!!.layoutParams.width = w
            IvVideoIndex!!.layoutParams.height = w
            (InfoRoot.layoutParams as ViewGroup.MarginLayoutParams).marginEnd = WidthConverter.getConvertedWidth(TAG, R.dimen.ivVideoIndex_end)

            val cl = ViewMain.findViewById<ConstraintLayout>(R.id.info_name_cl)
            w = WidthConverter.getConvertedWidth(TAG, R.dimen.info_name_cl_width)
            cl.layoutParams.width = w
            cl.layoutParams.height = WidthConverter.getConvertedWidth(TAG, R.dimen.info_root_height)

        }
    }
}