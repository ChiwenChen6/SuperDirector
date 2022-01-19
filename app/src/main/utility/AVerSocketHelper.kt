package com.aver.superdirector.utility

import android.os.AsyncTask
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.aver.superdirector.MainActivity
import org.json.JSONException
import org.json.JSONObject
import tech.gusavila92.websocketclient.WebSocketClient
import java.io.*
import java.net.HttpURLConnection
import java.net.URI
import java.net.URISyntaxException
import java.net.URL

class AVerSocketHelper {
    interface AVerSocketListener{
        fun onTextReceived(message: String)
        fun onOptionListUpdate(options: String)
    }


    companion object {
        val TAG = "AVerSocketHelper"

        private lateinit var mWebSocketClient: WebSocketClient
        private var mHandlerThread: HandlerThread
        private var mSocketHandler: Handler
        private var mList: MutableList<AVerSocketListener> = mutableListOf()
        private var mOptionList: String = ""

        init {
            Log.d(TAG, "init: ")
            mHandlerThread = HandlerThread("AVerSocketHelper")
            mHandlerThread.start()
            mSocketHandler = Handler(mHandlerThread.looper)
            //createWebSocketClient()
        }

        @JvmStatic
        fun addListener(listener: AVerSocketListener){
            mList.add(listener)
            Log.v(TAG, "addListener: ${mList.size}")
        }

        @JvmStatic
        fun removeListener(listener: AVerSocketListener){
            mList.remove(listener)
            Log.v(TAG, "removeListener: ${mList.size}")

        }

        @JvmStatic
        fun removeAllListener(){
            mList.clear()
            Log.v(TAG, "removeAllListener: ${mList.size}")

        }

        /*@JvmStatic
        private fun createWebSocketClient() {
            Log.d(TAG, "createWebSocketClient: ")
            val tWsUri = "ws://127.0.0.1:9187/ws"
            val tUri: URI?
            try {
                // Connect to local host
                tUri = URI(tWsUri);
            } catch (ex: URISyntaxException) {
                Log.w(TAG, "onException: 3 ${ex.message}")
                return;
            }

            mWebSocketClient = object : WebSocketClient(tUri) {
                override fun onOpen() {
                    Log.v(TAG, "onOpen: ")
                    mWebSocketClient.send("token: ${MainActivity.mToken}")
                    mSocketHandler.post(mRunnable)
                }

                override fun onTextReceived(message: String) {
                    Log.v(TAG, "onTextReceived: $message")
                    mList.forEach {
                        it.onTextReceived(message)
                    }
                    mSocketHandler.removeCallbacks(mRunnable)
                    mSocketHandler.postDelayed(mRunnable, 500)
                }

                override fun onPongReceived(data: ByteArray?) {
                    Log.v(TAG, "onPongReceived: ")
                }

                override fun onException(e: Exception) {
                    Log.w(TAG, "onException: 1 ${e.message}")
                }

                override fun onCloseReceived() {
                    Log.v(TAG, "onCloseReceived: ")
                }

                override fun onBinaryReceived(data: ByteArray?) {
                    Log.v(TAG, "onBinaryReceived: ")
                }

                override fun onPingReceived(data: ByteArray?) {
                    Log.v(TAG, "onPingReceived: ")
                }
            }

            mWebSocketClient.setConnectTimeout(10000)
            mWebSocketClient.enableAutomaticReconnection(5000)
            mWebSocketClient.connect()
        }

        @JvmStatic
        fun closeWebSocketClient(){
            Log.v(TAG, "closeWebSocket: ")
            mWebSocketClient.close()
        }

        private val mRunnable = Runnable {
            val tTask = CommandTask(MainActivity(), object : MainActivity.CommandCallback {
                override fun onCommandFinish() {
                    mList.forEach {
                        it.onOptionListUpdate(mOptionList)
                    }
                }
            })
            tTask.execute("option_list", "")
        }*/
    }
}