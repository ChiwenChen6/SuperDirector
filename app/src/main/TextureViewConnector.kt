package com.aver.superdirector

import android.content.Context
import android.graphics.*
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.android.volley.DefaultRetryPolicy
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.RetryPolicy
import com.android.volley.toolbox.ImageRequest
import com.aver.superdirector.BaseView.WebSocket.JWebSocketClient
import com.aver.superdirector.BaseView.WebSocket.handshake.ServerHandshake
import java.net.URI
import java.util.*
import java.util.concurrent.Executors

class TextureViewConnector(context: Context, ip: String, texture: TextureView, index: String, queue: RequestQueue) {
    private var TAG = "TVC"
    private var mContext: Context
    private var mLoginIP = ""
    private lateinit var mTextureView: TextureView
    private var webSocketClient: JWebSocketClient? = null
    private var isWebSocketHandClose = true
    private var webSocketHandlerThread: HandlerThread
    private lateinit var webSocketHandler: Handler
    private var mLiveQueue: RequestQueue
    private var t1 = 0L
    private var c1 = 0L
    private var c2 = 0L
    private var c3 = 0L
    private var mBitmapWidth = 0
    private var framerate = 0
    private lateinit var mMatrix: Matrix
    private var mIndex = 0
    private var mInfoView: TextView? = null
    private var mHandler: Handler? = null
    private var mIsClose = false


    private val mSurfaceTextureListener: TextureView.SurfaceTextureListener
    private lateinit var runnableLiveTimerVB130E: Runnable
    private lateinit var sendAlive: Runnable

    init{
        TAG = StringBuilder().append(TAG).append("-${index}").toString()
        Log.v(TAG, "init: ")
        runnableLiveTimerVB130E = Runnable {
            if(!mIsClose) {
                if(mTextureView.isAvailable) {
                    linkNetforLiveImageVB130E()
                }else{
                    webSocketHandler.postDelayed(runnableLiveTimerVB130E, 40)
                }
            }
        }

        sendAlive = Runnable {
            if (!mIsClose && webSocketClient != null && webSocketClient!!.isOpen) {
                webSocketClient?.send("alive")
            }
            Log.v(TAG, "sendAlive")

            if (!isWebSocketHandClose) {
                webSocketHandler.postDelayed(sendAlive, 60000)
            }
        }

        mContext = context
        mLoginIP = ip
        mTextureView = texture
        mLiveQueue = queue
//        mLiveQueue = Volley.newRequestQueue(mContext)
        mIndex = Integer.valueOf(index)
        webSocketHandlerThread = HandlerThread(mLoginIP)
        webSocketHandlerThread.start()
        webSocketHandler = Handler(webSocketHandlerThread.looper)

        mSurfaceTextureListener = object: TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                Log.v(TAG, "onSurfaceTextureAvailable: 4")
            }

            override fun onSurfaceTextureSizeChanged(
                surface: SurfaceTexture,
                width: Int,
                height: Int
            ) {
                Log.v(TAG, "onSurfaceTextureSizeChanged: $width $height")
            }

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                Log.v(TAG, "onSurfaceTextureDestroyed: ")
                return true
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
//                Log.v(TAG, "onSurfaceTextureUpdated: ")
            }
        }
        createWebSocket()
    }

    fun setInfoView(view: TextView, handler: Handler){
//        mInfoView = view
        mHandler = handler
    }

    private fun createWebSocket() {
        Log.v(TAG, "createWebSocket: ")
        webSocketHandler.post(Runnable {
            val uri = URI.create("ws://$mLoginIP:9187/ws")
            webSocketClient = object : JWebSocketClient(uri) {
                override fun onMessage(message: String) {
                    //message 就是接收到的消息
                    Log.v(TAG, message)
                }

                override fun onOpen(handshakedata: ServerHandshake?) {
                    super.onOpen(handshakedata)
                    Log.v(TAG, "onOpen: ")
                    if (webSocketClient != null && webSocketClient!!.isOpen) {
                        Log.v(TAG, "createWebSocket:4")

                        isWebSocketHandClose = false
                        webSocketClient?.send("token:${VideoFragment.mToken}")
                        webSocketHandler.post(sendAlive)
                        mTextureView.surfaceTextureListener = mSurfaceTextureListener
                        webSocketHandler.postDelayed(Runnable { linkNetforLiveImageVB130E() }, mIndex.toLong() * 10)
                    } else {
                        isWebSocketHandClose = true
                    }
                }
            }
            try {
                webSocketClient?.connectBlocking()
                Log.v(TAG, "createWebSocket: 3")
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        })
    }

    private fun linkNetforLiveImageVB130E() {
        val url = "http://$mLoginIP:81/live"
        val cs = System.currentTimeMillis()
        val request: ImageRequest = object : ImageRequest(url, Response.Listener { bitmap ->
            if (webSocketClient == null) {
                Log.v(TAG, "getLiveImage VB130E webSocketClient=null ")
                return@Listener
            }
            val cs1 = System.currentTimeMillis()

            webSocketHandler.post(Runnable {
                if (framerate == 0) {
                    var scale = mTextureView.width.toFloat() / bitmap.width.toFloat()
                    mBitmapWidth = bitmap.width
                    mMatrix = Matrix()
                    mMatrix.setScale(scale, scale)
                }
                if (webSocketClient != null) {
                    if (!mIsClose && mTextureView.isAvailable) {
                        val parent = mTextureView.parent as ViewGroup
                        mHandler?.post(Runnable {
                            parent.getChildAt(2).visibility = View.VISIBLE
                        })
                        setBitmap2Canvas(mTextureView, bitmap)
                        framerate++
                    }
                    val cs2 = System.currentTimeMillis()
                    c1 += (cs2 - cs)
                    c2 += (cs1 - cs)
                    c3 += (cs2 - cs1)
                    if (System.currentTimeMillis() / 1000 != t1 / 1000) {
                        t1 = System.currentTimeMillis()
                        if (framerate > 0) {
                            val info = "avg: ${c1.toInt() / framerate} = ${c2.toInt() / framerate} + ${c3.toInt() / framerate}, framerate = $framerate, bitmap = ${bitmap.width} ${bitmap.height}, mTextureView = ${mTextureView.width} ${mTextureView.height}"
                            mHandler?.post(Runnable {
                                mInfoView?.text = info
                            })
//                            Log.v(TAG, info)
                        }
                        c1 = 0
                        c2 = 0
                        c3 = 0
                        framerate = 0
                    }
                    webSocketHandler.post(runnableLiveTimerVB130E) // delay 時間不能太短，否則 Camera 會有誤動作
                }
            })
        }, 0, 0, ImageView.ScaleType.CENTER, Bitmap.Config.RGB_565, Response.ErrorListener { error ->
            Log.v(TAG, "getLiveImage onErrorResponse: $error")
//            val bitmap = BaseCameraActivity.textAsBitmap(mContext.resources.getString(R.string.OpenPTZAppHint), 40f, Color.WHITE)
//            val paint = Paint()
//            paint.isAntiAlias = true
//            paint.style = Paint.Style.STROKE
//            paint.flags = Paint.ANTI_ALIAS_FLAG
//            val canvas = mTextureView.lockCanvas()
//            val matrix = Matrix()
//            matrix.setScale(0.5f, 0.5f)
//            canvas.drawBitmap(bitmap, matrix, paint)
//            mTextureView.unlockCanvasAndPost(canvas)
            webSocketHandler.postDelayed(runnableLiveTimerVB130E, 40) // delay 時間不能太短，否則 Camera 會有誤動作
        }) {
            override fun getHeaders(): Map<String, String> {
                //Log.v("AVDebug", "getLiveImage: call getHeaders ");
                val headers = HashMap<String, String>()
                headers.put("Cache-Control", "no-store, no-cache")
                headers.put("Authorization", "bearer ${VideoFragment.mToken}")
                return headers
            }
        }
        val retryPolicy: RetryPolicy = DefaultRetryPolicy(
                2000,
                2,
                1.0f
        )
        //設定重新連線機制以及CACHE清除，增進效能 原本為一秒一張現為一秒五張 如要更增進表現 要更換VIEW
        request.retryPolicy = retryPolicy
        request.setShouldCache(false)
        mLiveQueue.add<Bitmap>(request)
    }

    private fun setBitmap2Canvas(textureView: TextureView, bitmap: Bitmap){
        val paint = Paint()
        paint.isAntiAlias = true
        paint.style = Paint.Style.STROKE
        paint.flags = Paint.ANTI_ALIAS_FLAG
        val canvas = textureView.lockCanvas()
        canvas!!.drawBitmap(bitmap, mMatrix, paint)
        textureView.unlockCanvasAndPost(canvas)
    }

    fun setEnable(enable: Boolean){
        webSocketHandler.removeCallbacks(runnableLiveTimerVB130E)
    }

    fun close(){
        mIsClose = true
        mTextureView.surfaceTextureListener = null
        if(webSocketClient != null) {
            webSocketHandler.removeCallbacks(sendAlive)
            webSocketClient?.close()
        }
    }
}