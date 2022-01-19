package com.aver.superdirector.utility

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkInfo
import android.util.Log
import com.aver.superdirector.MainActivity

class NetworkStateReceiver: BroadcastReceiver() {
    val TAG = "NetworkStateReceiver"

    interface NetworkChangeObserver{
        fun onConnect(type: String)
        fun onDisconnect()
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if(intent?.action == NetworkIntentAction){
            val connManager = context?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val info: NetworkInfo? = connManager.activeNetworkInfo
            if (info != null) {
                if(info.isConnected){
                    val netCap = connManager.getNetworkCapabilities(connManager.activeNetwork)
                    val tw = netCap!!.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                    val th = netCap.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    if(th){
                        mType = "eth"
                    }
                    if(tw){
                        mType = "wifi"
                    }
                    mIsConnected = true
                    Log.v(TAG, "Connected: $mType")
                }else{
                    mIsConnected = false
                    Log.v(TAG, "Disconnected: ")
                }
            }else{
                mIsConnected = false
                Log.v(TAG, "Disconnected: ")
            }
            notifyAllObservers()
        }
    }

    private fun notifyAllObservers(){
        for(obs in mObservers){
            if(mIsConnected){
               obs.onConnect(mType)
            }else{
                obs.onDisconnect()
            }
        }
    }

    companion object{
        private var mIsConnected = false
        private var mType = "eth"
        const val NetworkIntentAction = "android.net.conn.CONNECTIVITY_CHANGE"
        private val mObservers = ArrayList<NetworkChangeObserver>()

        fun registerObserver(observer: MainActivity){
           // mObservers.add(observer)
        }

        fun unregisterObserver(observer: MainActivity){
           // mObservers.remove(observer)
        }

        fun getCurrentNetType(): String{
            return mType
        }

        fun isConnected(): Boolean{
            return mIsConnected
        }
    }
}