package com.aver.superdirector


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.*
import android.os.*
import android.provider.Settings
import android.util.Base64
import android.util.DisplayMetrics
import android.util.Log
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.aver.superdirector.utility.*
import com.aver.superdirector.utility.CameraFinder.SearchData
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.window_internet.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.*
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), CardViewListenerInterface {
    private val TAG = "PlaceActivity"

    companion object {
        private val deviceListIP = arrayOf(
            "CAM520 Pro",
            "CAM520 Pro2",
            "VC520 Pro",
            "VC520 Pro2",
            "Fone540",
            "VB130",
            "VB130E",
            "CAM550"
        )
        private val deviceListUSB = arrayOf(
            "CAM520",
            "CAM340+",
            "VB342+",
            "VB350",
            "CAM540",
            "CAM540HI",
            "FONE540",
            "CAM520 Pro",
            "VC520 Pro",
            "EXPFONE",
            "CAM520 Pro2",
            "VC520 Pro2",
            "VB130",
            "CAM130",
            "CAM130CT",
            "VB130E",
            "CAM540 Pro2",
            "CAM550"
        )


        private var screenWidth: Int = 0
        private var screenHeight: Int = 0

        //PTZApp usb alertdialog

        private var trueUCID = arrayOfNulls<String>(100)
        private var savePCName = arrayOfNulls<String>(100)

        private var saveIPAddress = arrayOfNulls<String>(100)
        private var saveSWVersion = arrayOfNulls<String>(100)
        var IPcount: Int = 1
        private var udpServer: UDP? = null

        // App config
        lateinit var appConfig: AppConfig
        private val stringBufferReceived: StringBuffer = StringBuffer()
        private var cameraCount = 0
        var isUpdateLoginIP = false


        // private var USBItemList: ArrayList<USBItem>? = null
        private var cameraCountusb = 0
        var dsCameraFinder: DatagramSocket? = null
        var dpSendBroadcast: DatagramPacket? = null
        var dpReceiveBroadcast: DatagramPacket? = null

        private var handlerCameraFinder = Handler(Looper.getMainLooper())
        private var handlerThreadCameraFinder: HandlerThread? = null
        private val handlerThreadNameCameraFinder = "CameraFinder"
        var cameraData: SearchData? = null
        private var firstInitialUsb = 0

        // Cardview
        var cameraItemList = ArrayList<CameraItem>()
        var cameraItemListUSB = ArrayList<CameraItem>()
        var cameraItemListUSBComputer = ArrayList<CameraItem>()

        val USBItemList = ArrayList<USBItem>()

        var cameraViewVertical: RecyclerView? = null
        var cameraItemAdapter: CameraItemAdapter? = null
        var cameraItemAdapterUSB: CameraItemAdapter? = null
        var currentCameraItem: CameraItem? = null
        var currentCameraItemUSB: CameraItem? = null

        private val mAccountList = ArrayList<AVerAccountInfo>()

        private var dlgWaiting: AlertDialog? = null

        private var tvWaitingMessage: TextView? = null
        private var tvScanText: TextView? = null

        private fun checkDeviceSupportListUSB(model: String): Boolean {
            for (s in deviceListUSB) {
                if (s == model) {
                    return true
                }
            }
            return false
        }

        private lateinit var mDialInfo: AVerAccountInfo
        var initialize // 用來註記是程式剛開始初始化，判斷是否該執行 Auto bring
                = false
        var initializeOpenFragment // 用來註記是否已經開啟fragment頁面，避免重複開啟
                = false
        private var mFragment: Fragment? = null

        @SuppressLint("StaticFieldLeak")
        private lateinit var mVideoFragment: VideoFragment
        private lateinit var mCombineFragment: CombineFragment

        @SuppressLint("StaticFieldLeak")
        private lateinit var mScanFragment: ScanFragment

        @SuppressLint("StaticFieldLeak")
        private lateinit var mBindFragment: BindFragment

        private lateinit var mPreferences: SharedPreferences
        private lateinit var mEditor: SharedPreferences.Editor
        private lateinit var mHandler: Handler
        private var mLogoutWindow: PopupWindow? = null


        private var notificationAnimation: Animation = TranslateAnimation(0.0f, 0.0f, -126.0f, 0.0f)
        private var mIsDeleteMode = false
        private var mInternetWindow: PopupWindow? = null
        private var mIsConnected = false
        private var mLastNetwork = ""
        private var mIsPause = false
        private var mSipStat = false
        private var mH323Stat = false


        private var timer: Timer? = null

        // Camera Finder timeout timer
        private val mMainHandler = Handler(Looper.getMainLooper())
        val DELAY_TIME = 3000 // 3sec
        val DELAY_TIME_UPDATE = 5000 // 5sec
        private var mDelayTimer: Timer? = null

        //for usb device
        var cntPCdevices = 0

        private val USBReceiveBroadcast: USBReceiveBroadcastFun = USBReceiveBroadcastFun()
        var mQueue: RequestQueue? = null

        var exec = Executors.newCachedThreadPool()
        private var timeStamp: String? = null

        var Cameraresponse = false //usb camera
        var menuTag: String=""

    }

    private var cpProgress: CircleProgressBar? = null

    private fun initFindCameraBroadcast() {
        handlerThreadCameraFinder = HandlerThread(handlerThreadNameCameraFinder)
        handlerThreadCameraFinder!!.start()
        Log.v("AVDebug", "btnSearch onClick ")
        val buf = ByteArray(1024)
        val msgBody =
            "UENFZGl0AgAAAAAAAAAAAAAAAAAAAAAAAAIAAAAAAAACAAAAAAAAAAACAAAAAAAAAAAAAAAAAAAAAAAA" +
                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA" +
                    "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        try {
            dsCameraFinder = DatagramSocket() // 傳送/接收 廣播
            dpReceiveBroadcast = DatagramPacket(buf, buf.size) // 用來接收 camera 回傳的封包資料
            dpSendBroadcast = DatagramPacket(
                msgBody.toByteArray(), 0, msgBody.length,
                InetAddress.getByName("255.255.255.255"), 8000
            ) // 設定送出 camera search 廣播封包內容
            dsCameraFinder!!.soTimeout = DELAY_TIME
            dsCameraFinder!!.broadcast = true
            // Thread

            handlerCameraFinder = Handler(handlerThreadCameraFinder!!.looper)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun findCamera() {
        initializeOpenFragment = true
        // ----- Camera Finder
        initFindCameraBroadcast()
        //確認USBReceiveBroadcast關閉，避免顯示USB裝置
        if (USBReceiveBroadcast != null) {
            try {
                Log.v("AVDebug", "unregisterReceiver USBReceiveBroadcast")
                unregisterReceiver(USBReceiveBroadcast)
            } catch (ignored: java.lang.Exception) {
            }
        }
        // 清空搜尋資訊
        cameraItemList.clear()
        // 清空記憶IP
        isUpdateLoginIP = false
        if (mDelayTimer != null) {
            mDelayTimer!!.cancel()
        }
        handlerCameraFinder.post(cameraFinderReceiverThread) // start receiver thread
        val thread = Thread {
            try {
                dsCameraFinder!!.send(dpSendBroadcast)
                Log.v("AVDebug", "dsCameraFinder.send(dpSendBroadcast)")
            } catch (e: java.lang.Exception) {
                Log.v("AVDebug", e.message!!)
            }
        }
        thread.start()

        mDelayTimer = Timer()
        mDelayTimer!!.schedule(object : TimerTask() {
            override fun run() {
                mMainHandler.post {
                    Log.v("AVDebug", "findCamera: mMainHandler timeout")
                    //Collections.sort(cameraItemList, new Comparator<cameraItemList>());
                    cameraItemAdapter!!.cleanItem()
                    if(menuTag.equals("BindFragment")){
                        cameraItemAdapter!!.setHolderClicked(true)
                    } else{
                        cameraItemAdapter!!.setHolderClicked(false)

                    }
                    cameraItemAdapter!!.resetItem(cameraItemList)
                    // cameraViewVertical!!.scrollToPosition(0)
                    findUSBCamera()
                    // page view

                }
            }
        }, DELAY_TIME.toLong())


    }


    class USBReceiveBroadcastFun : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val mAction = intent.action!!
            when (mAction) {
                UDP.RECEIVE_ACTION -> {
                    val msg = intent.getStringExtra(UDP.RECEIVE_STRING)
                    val bytes = intent.getByteArrayExtra(UDP.RECEIVE_BYTES)
                    val recieved_ipname = intent.getStringExtra(UDP.RECEIVE_IP)
                    val StringReceived: String =
                        stringBufferReceived.append("收到來自： ").append(recieved_ipname)
                            .append("  送來的訊息： ").append(msg).append("\n").toString()
                    Log.v("AverPTZDebug", StringReceived)
                    val devicedataSplit = msg!!.split(";".toRegex()).toTypedArray()
                    Log.v("AVDebug", " --------------------------- id  cameraCount: $cameraCount")
                    Log.v(
                        "AVDebug",
                        " --------------------------- id  cameraCountusb: $cameraCountusb"
                    )
                    Log.v(
                        "AVDebug",
                        " --------------------------- id  devicedataSplit " + devicedataSplit[1]
                    )
                    var findIP: String?
                    var PCName = ""
                    var softwareState = ""
                    var softwareVersion = ""
                    var MAC = "null"
                    val UCID: String

                    //注意這邊是HARDCODE
                    //socket index  0  receive message(from  "10.100.91.92" : 52144 ):  "AverPTZToPTZApp2;Scan.2;1627469243;1;;"
                    //message sent to device(IP: "10.100.91.92" , socket index: 0 ):  "PTZApp2ToAverPTZ;ScanAck.2;1627469243;1;DA00001849;Free;2.0.1018.19;74:D0:2B:7C:67:1F;YTM5YWI0ZmMtZTY5MC00ZjVhLThhZDUtZGY3YTVlYmQ2OThl;"
                    //scan 功能 紀錄電腦ip 建立array
                    //確認第一項目為PTZApp  回應ScanAck.2
                    if (devicedataSplit[1].replace("[", "").replace("]", "") == "ScanAck.2") {
                        findIP = recieved_ipname
                        PCName = devicedataSplit[4].replace("[", "").replace("]", "")
                        softwareState = devicedataSplit[5].replace("[", "").replace("]", "")
                        softwareVersion = devicedataSplit[6].replace("[", "").replace("]", "")
                        MAC = devicedataSplit[7].replace("[", "").replace("]", "")
                        UCID = devicedataSplit[8].replace("[", "").replace("]", "")
                        Log.v("AVerDebug", "show UCID :$UCID")
                        val KeyForUCID = Base64.decode(UCID.toByteArray(), Base64.DEFAULT)
                        //避免空值造成閃退
                        try {
                            //儲存找到的裝置
                            saveIPAddress[IPcount] = findIP
                            savePCName[IPcount] = PCName
                            saveSWVersion[IPcount] = softwareVersion
                            trueUCID[IPcount] = String(KeyForUCID, Charsets.UTF_8)
                            Log.v("AVerDebug", "show KeyForUCID :" + trueUCID[IPcount])
                            IPcount++
                        } catch (e: UnsupportedEncodingException) {
                            e.printStackTrace()
                        }
                    }
                    //確認收到的指令是 list 才顯示設備卡牌
                    //確認回傳值第一項 為UvcListAck.1
                    //注意這邊是HARDCODE
                    //socket index  0  receive message(from  "10.100.91.92" : 53816 ):  "AverPTZToPTZApp2;UvcList.1;1627469243;"
                    //message sent to device(IP: "10.100.91.92" , socket index: 0 ):  "PTZApp2ToAverPTZ;UvcListAck.1;1627469243;2;[1234567890123,5100249100001];[VB130,VB342+];[0.0.7300.57,0.0.0004.08];"
                    if (devicedataSplit[1].replace("[", "").replace("]", "") == "UvcListAck.1") {
                        val findModel = devicedataSplit[5].replace("[", "").replace("]", "")
                        //一台PC可能會連結超過一台機器
                        val PCdevices = findModel.split(",".toRegex()).toTypedArray()
                        findIP = recieved_ipname
                        //記錄不同機器的韌體版本
                        val findFWversion = devicedataSplit[6].replace("[", "").replace("]", "")
                        val PCdevicesFW = findFWversion.split(",".toRegex()).toTypedArray()

                        //val bringModel: String = appConfig.readAutoBringModel(getcontext)
                        //val bringIP: String = appConfig.readAutoBringIP(this@MainActivity)
                        Log.v("AVDbug", "show ALL Model : $findModel")
                        var eqsoftwareVersion: String? = null
                        cntPCdevices = 0
                        while (cntPCdevices < PCdevices.size) {
                            if (checkDeviceSupportListUSB(PCdevices[cntPCdevices])) {

                                val firstTimeInitial = 0
                                var element: String
                                var eqPCName: String
                                var eqsoftwareState: String

                                if (findIP in saveIPAddress) {
                                    var indexnum = saveIPAddress.indexOf(findIP)
                                    PCName = savePCName[indexnum].toString()
                                    softwareVersion = saveSWVersion[indexnum].toString()
                                }
                                //判斷式
                                //如果有預設開啟裝置、還沒顯示過預設裝置、裝置名稱相符、裝置IP相符 以上四項皆符合才顯示
                                /*if (appConfig.readAutoBringState() === 0 && firstInitialUsb == 0 && findIP == bringIP && PCdevices[cntPCdevices] == bringModel) {
                                      cameraCount = 1
                                      val model: String =
                                          appConfig.readAutoBringModel(this@MainActivity)
                                      val ip: String = appConfig.readAutoBringIP(this@MainActivity)
                                      if (model.length > 0 && ip.length > 0) {
                                          val p2 = CameraItem(
                                              cameraCount, Enums.CameraItemType.Camera, null,
                                              PCdevices[cntPCdevices], findIP, eqsoftwareVersion,
                                              PCdevicesFW[cntPCdevices], "SN", "mac", true
                                          )
                                          cameraItemList.add(1, p2)
                                          cameraCount++
                                          firstInitialUsb++
                                      }
                                  }*/
                                val USBp = USBItem(
                                    cameraCountusb,
                                    Enums.CameraItemType.USB,
                                    findIP,
                                    msg,
                                    PCName,
                                    softwareVersion,
                                    softwareState,
                                    "DDD",
                                    MAC,
                                    false
                                )
                                try {
                                    USBItemList.add(USBp)
                                    cameraCountusb++
                                    Log.v("AVDbug", "USBItemList  :$USBItemList")
                                    Log.v("AVDbug", "IPcount  :$IPcount")
                                } catch (ignored: Exception) {
                                }
//                                }
                                IPcount++
                                var jj = 0
                                while (jj < saveIPAddress.size) {
                                    if (findIP == saveIPAddress.get(jj)) {
                                        eqsoftwareVersion = saveSWVersion.get(jj)
                                    }
                                    jj++
                                }
                                if (eqsoftwareVersion == null) {
                                    val msgPCname2 = "AverPTZToPTZApp2;Scan.2;$timeStamp;2;;"
                                    try {
                                        udpServer!!.send(msgPCname2, "255.255.255.255", 6009)
                                    } catch (e: IOException) {
                                        e.printStackTrace()
                                    }
                                } else {
                                    //宣告卡牌的資訊內容
                                    if (eqsoftwareVersion != null && findIP != null) {
                                        val p2 = CameraItem(
                                            cameraCount, Enums.CameraItemType.USB,
                                            dpReceiveBroadcast!!.data,
                                            PCdevices[cntPCdevices], findIP, eqsoftwareVersion,
                                            PCdevicesFW[cntPCdevices], "DDD", " ", false
                                        )
                                        val p = CameraItem(
                                            cameraCount, Enums.CameraItemType.USB,
                                            dpReceiveBroadcast!!.data,
                                            PCName, findIP, eqsoftwareVersion,
                                            PCdevicesFW[cntPCdevices], "DDD", " ", false
                                        )
                                        Log.v("AVDbug", "show p :$p")
                                        if(menuTag.equals("BindFragment")){


                                            for(i in 0 until cameraItemListUSBComputer.size-1){
                                                if (!cameraItemListUSBComputer.get(i).modelName.equals(PCName)){
                                                    cameraItemListUSBComputer.add(p)
                                                    cameraCount++
                                                }
                                            }


                                        } else{
                                            cameraItemListUSB.add(p2)
                                            cameraCount++
                                        }
                                    }
                                }
                            }
                            cntPCdevices++
                        }
                    }
                    stringBufferReceived.delete(0, stringBufferReceived.length)
                }
            }
        }

    }

    private val cameraFinderReceiverThread = Runnable {
        Log.v("AVDebug", " start cameraFinderReceiverThread thread")
        try {
            while (true) {
                dsCameraFinder!!.receive(dpReceiveBroadcast)
                Log.v(
                    "AVDebug",
                    "   --- udpReceiver receive dp ip: " + dpReceiveBroadcast!!.socketAddress
                )
                if (dpReceiveBroadcast!!.data.size < 253) {
                    Log.v("AVDebug", "data fail!!")
                    continue
                }
                if (cameraData == null) {
                    cameraData = SearchData(dpReceiveBroadcast!!.data)
                } else {
                    cameraData!!.setCameraData(dpReceiveBroadcast!!.data)
                }
                Log.v("AVDebug", " --------------------------- id : $cameraCount")
                val findModel: String =
                    cameraData!!.modelNameString.replace("\\P{Print}".toRegex(), "")
                val findIP: String = cameraData!!.ipString.replace("\\P{Print}".toRegex(), "")

                if (checkDeviceSupportListIP(findModel)) {
                    val p = CameraItem(
                        cameraCount, Enums.CameraItemType.IP,
                        dpReceiveBroadcast!!.data, findModel, findIP, "xxx",
                        cameraData!!.fwVersionString, "DDD", cameraData!!.macString, false
                    )
                    Log.v("AVDbug", "show mac :" + cameraData!!.macString)

                    cameraItemList.add(p)
                    cameraCount++

                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        Log.v("AVDebug", "cameraFinderReceiverThread stop..............")
    }

    private fun checkDeviceSupportListIP(model: String): Boolean {
        for (s in deviceListIP) {
            if (s == model) {
                return true
            }
        }
        return false
    }


    fun getLocalIpAddress(): String? {
        try {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                        return inetAddress.getHostAddress()
                    }
                }
            }
        } catch (ex: SocketException) {
            ex.printStackTrace()
        }
        return null
    }

    internal fun findUSBCamera() {
        initializeOpenFragment = true
        cntPCdevices = 0
        firstInitialUsb = 0
        handlerCameraFinder.removeCallbacks(cameraFinderReceiverThread) // remove receiver thread
        Log.v("AVDebug", "cameraFinderReceiverThread remove")
        IPcount = 1
        //防止重複開啟
        cameraCount = 1
        cameraCountusb = 1
        if (udpServer == null) {
            udpServer = UDP(getLocalIpAddress(), this)
            Log.i("AVerPTZDebug", "getLocalIP set end :" + getLocalIpAddress())
            udpServer!!.setPort(9006)
            udpServer!!.changeServerStatus(true)
            exec.execute(udpServer)
            Log.i("AVerPTZDebug", "udpServer set end")
        }
        isUpdateLoginIP = false
        if (USBReceiveBroadcast != null) {
            try {
                Log.v("AVDebug", "unregisterReceiver USBReceiveBroadcast")
                unregisterReceiver(USBReceiveBroadcast)
            } catch (ignored: Exception) {
            }
        }
        val intentFilter = IntentFilter(UDP.RECEIVE_ACTION)
        registerReceiver(USBReceiveBroadcast, intentFilter)
        timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
        val msgPCname = "AverPTZToPTZApp2;Scan.2;$timeStamp;1;;"
        val msglist = "AverPTZToPTZApp2;UvcList.1;$timeStamp;"
        exec.execute(Runnable {
            try {
                udpServer!!.send(msgPCname, "255.255.255.255", 6009)
                Thread.sleep(300)
                udpServer!!.send(msglist, "255.255.255.255", 6009)
                Log.v("AVDebug", "msgPCname udpServer send 255.255.255.255 in 6009")
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        })

        mDelayTimer = Timer()
        mDelayTimer!!.schedule(object : TimerTask() {
            override fun run() {
                mMainHandler.post(Runnable {
                    Log.v("AVDebug", "findCamera: mMainHandler timeout")
                    //Collections.sort(cameraItemList, new Comparator<cameraItemList>());
                    // page view
                    if (cameraItemAdapterUSB != null) {
                        cameraItemAdapterUSB!!.cleanItem()
                        if(menuTag.equals("BindFragment")){
                            cameraItemAdapterUSB!!.setHolderClicked(true)
                        } else{
                            cameraItemAdapterUSB!!.setHolderClicked(false)

                        }
                        if(menuTag.equals("BindFragment")){
                            cameraItemAdapterUSB!!.resetItem(cameraItemListUSBComputer)

                        } else{
                            cameraItemAdapterUSB!!.resetItem(cameraItemListUSB)
                        }
                    } else throw NullPointerException("Expression 'cameraItemAdapter' must not be null")

                })
            }
        }, DELAY_TIME.toLong())

    }

    internal fun findUSBCameraData() {
        initializeOpenFragment = true
        cntPCdevices = 0
        firstInitialUsb = 0
        handlerCameraFinder.removeCallbacks(cameraFinderReceiverThread) // remove receiver thread
        Log.v("AVDebug", "cameraFinderReceiverThread remove")
        IPcount = 1
        //防止重複開啟
        cameraCount = 1
        cameraCountusb = 1
        if (udpServer == null) {
            udpServer = UDP(getLocalIpAddress(), this)
            Log.i("AVerPTZDebug", "getLocalIP set end :" + getLocalIpAddress())
            udpServer!!.setPort(9006)
            udpServer!!.changeServerStatus(true)
            exec.execute(udpServer)
            Log.i("AVerPTZDebug", "udpServer set end")
        }
        if(menuTag.equals("BindFragment")){
            cameraItemListUSBComputer.clear()
        } else{
            cameraItemListUSB.clear()
        }
        isUpdateLoginIP = false
        if (USBReceiveBroadcast != null) {
            try {
                Log.v("AVDebug", "unregisterReceiver USBReceiveBroadcast")
                unregisterReceiver(USBReceiveBroadcast)
            } catch (ignored: Exception) {
            }
        }
        val intentFilter = IntentFilter(UDP.RECEIVE_ACTION)
        registerReceiver(USBReceiveBroadcast, intentFilter)
        timeStamp = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis()).toString()
        val msgPCname = "AverPTZToPTZApp2;Scan.2;$timeStamp;1;;"
        val msglist = "AverPTZToPTZApp2;UvcList.1;$timeStamp;"
        val msgtest = "AverPTZToPTZApp2;Scan.2;timeStamp;1;"
        exec.execute(Runnable {
            try {
                udpServer!!.send(msgPCname, "255.255.255.255", 6009)
                Thread.sleep(300)
                udpServer!!.send(msglist, "255.255.255.255", 6009)
                Log.v("AVDebug", "msgPCname udpServer send 255.255.255.255 in 6009")
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        })
    }

    private val mFragmentCallback = object : FragmentCallback {
        override fun onDeleteModeChange(enable: Boolean) {
            placeMain.isClickable = enable
            for (i in 0 until grpMenu.childCount) {
                (grpMenu.getChildAt(i) as RadioButton).isClickable = !enable
            }

            mIsDeleteMode = enable
            placeMain.isClickable = enable
        }

        override fun showNotification() {
            txtNotification.text = resources.getText(R.string.notification_add_contact)
            notificationAnimation = TranslateAnimation(0.0f, 0.0f, -126.0f, 0.0f)
            notificationAnimation.duration = 1000
            viewNotification.visibility = VISIBLE
            viewNotification.animation = notificationAnimation
            mHandler.removeCallbacks(mRemoveAnimation)
            mHandler.postDelayed(mRemoveAnimation, 6000)
        }
    }

    interface FragmentCallback {
        fun onDeleteModeChange(enable: Boolean)
        fun showNotification()
    }

    @SuppressLint("CommitPrefEdits")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mHandler = Handler(Looper.getMainLooper())
        mPreferences = PreferenceManager
            .getDefaultSharedPreferences(this)
        mEditor = mPreferences.edit()
        val displayMetrics = baseContext?.resources?.displayMetrics
        val height = displayMetrics?.heightPixels
        val width = displayMetrics?.widthPixels
        Log.v(TAG, "displayMetrics: $width x $height")
        WidthConverter.getInstances(baseContext, displayMetrics)

        placeMain.setOnClickListener(View.OnClickListener {
            if (mFragment is ScanFragment) {
                if (mIsDeleteMode) {
                    (mFragment as ScanFragment).onParentClick()
                }
            }
        })
        placeMain.isClickable = false
        mQueue = Volley.newRequestQueue(this)

        // ----- create CardView
        // 取得設備螢幕數據
        val metric = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(metric)
        screenWidth = metric.widthPixels
        screenHeight = metric.heightPixels
        val density = metric.density // 螢幕密度

        // 轉換成目前螢幕螢幕實際 pixel 值, 廠商給的螢幕密度(density)剛好是 1.0, 所以值沒變.
        val spacingInPixels =
            72 //getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);

        Log.v("AVDebug", "w: $screenWidth   h: $screenHeight   d: $density   s: $spacingInPixels")

        // 這個數量關係到後面增加時的位置計算
        cameraItemAdapter = CameraItemAdapter(cameraItemList, this@MainActivity)
        cameraItemAdapterUSB = CameraItemAdapter(cameraItemListUSB, this@MainActivity)

        mBindFragment = BindFragment()
        mBindFragment.setFragmentCallback(mFragmentCallback)

        mScanFragment = ScanFragment()
        mScanFragment.setFragmentCallback(mFragmentCallback)

        mVideoFragment = VideoFragment()

        mCombineFragment = CombineFragment()
        mCombineFragment.setFragmentCallback(mFragmentCallback)

        radBind.setOnClickListener {
            menuTag = "BindFragment"
            cameraItemAdapterUSB!!.cleanItem()
            cameraItemAdapterUSB!!.resetItem(cameraItemListUSBComputer)


            showFragment(mBindFragment, "BindFragment")
            //findUSBCamera()
        }

        radScan.setOnClickListener {
            menuTag = "ScanFragment"
            cameraItemAdapterUSB!!.cleanItem()

            cameraItemAdapterUSB!!.resetItem(cameraItemListUSB)


            showFragment(mScanFragment, "ScanFragment")
        }

        radVideo.setOnClickListener {
            menuTag = "VideoFragment"

            showFragment(mVideoFragment, "VideoFragment")
        }

        radCombine.setOnClickListener {
            menuTag = "CombineFragment"

            showFragment(mCombineFragment, "CombineFragment")
        }


        showFragment(mBindFragment, "BindFragment")


        // camera finding dialog
        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        // R.style.my_dialog 用來設定 dialog 透明背景 (方法一)
        dlgWaiting = AlertDialog.Builder(this, R.style.TranslucentDialogStyle).create()
        val dlgViewWaiting = View.inflate(this@MainActivity, R.layout.dialog_wating, null)
//        dlgWaiting.setView(dlgViewWaiting, 0, 0, 0, 0)
//        dlgWaiting.setCanceledOnTouchOutside(false)
        tvWaitingMessage = dlgViewWaiting.findViewById<TextView>(R.id.dialog_waiting_Message)
        tvScanText = dlgViewWaiting.findViewById<TextView>(R.id.ScanText)
        cpProgress = dlgViewWaiting.findViewById(R.id.cp_progress)
    }

    override fun onResume() {
        super.onResume()
        mIsPause = false
        initFindCameraBroadcast()

        if (mFragment != null) {
            val tRbMenu = findViewById<RadioButton>(grpMenu.checkedRadioButtonId)
            Log.v(TAG, "onResume: ${mFragment?.tag} ${tRbMenu.tag}")

            when (tRbMenu.tag) {
                "Bind" -> {
                    menuTag = "BindFragment"
                    showFragment(mBindFragment, menuTag)
                }
                "Scan" -> {
                    menuTag = "ScanFragment"
                    showFragment(mScanFragment, menuTag)
                }
                "Video" -> {
                    menuTag = "VideoFragment"
                    showFragment(mVideoFragment, menuTag)
                }
                "Combine" -> {
                    menuTag = "CombineFragment"
                    showFragment(mCombineFragment, menuTag)
                }
            }
        }

        NetworkStateReceiver.registerObserver(this)
        //mH323Stat = mPreferences.getBoolean(H323LoginActivity.H323_STAT, false)

    }

    override fun onPause() {
        super.onPause()
        mIsPause = true
        NetworkStateReceiver.unregisterObserver(this)
    }

    private val mRemoveAnimation = Runnable {
        viewNotification.visibility = INVISIBLE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.v(TAG, "onActivityResult: $requestCode $resultCode ")

    }

    //////////////////////
    // Private Function //
    //////////////////////

    private fun showFragment(fragment: Fragment, tag: String) {
        if (mFragment != null &&
            mFragment == fragment
        )
            return
        Log.v(TAG, "showFragment: ${mFragment?.tag} $tag")

        val tTransaction = supportFragmentManager.beginTransaction()
        tTransaction.replace(R.id.fragSwitch, fragment, tag)
        tTransaction.addToBackStack(null)
        tTransaction.commit()

        mFragment = fragment
    }

    @SuppressLint("InflateParams")
    fun onConnect(type: String) {
        Log.v(TAG, "onConnect: $type")
        if (mInternetWindow != null) {
            mInternetWindow?.dismiss()
            mInternetWindow = null
        }

        mLastNetwork = type
        mIsConnected = true
    }

    fun conpareIPSaved_UCID(BindIPAppend: String): String? {
        Log.v(TAG, "conpareIPSaved ")
        var ii = 0
        while (ii < saveIPAddress.size) {
            //Log.v("AVDebug", "saveIPAddress" + ii + ": " + saveIPAddress[ii])
            if (BindIPAppend == saveIPAddress[ii]) {
                Log.v("AVDebug", "trueUCID[ii]: " + trueUCID[ii])
                if (initializeOpenFragment) {
                    usbDeviceDetectState(BindIPAppend, trueUCID[ii])
                    initializeOpenFragment = false
                }
                ii = ii + saveIPAddress.size
                initialize = false
                return trueUCID[ii]
            }
            if (ii == saveIPAddress.size - 1) {
                cameraItemList.clear()
                findUSBCamera()
            }
            ii++
        }
        return ""
    }

    fun conpareIPSaved_modelname(BindIPAppend: String): String? {
        Log.v(TAG, "conpareIPSaved ")
        var ii = 0
        while (ii < saveIPAddress.size) {
            Log.v("AVDebug", "saveIPAddress" + ii + ": " + saveIPAddress[ii])
            if (BindIPAppend == saveIPAddress[ii]) {
                Log.v("AVDebug", "trueUCID[ii]: " + trueUCID[ii])
                if (initializeOpenFragment) {
                    usbDeviceDetectState(BindIPAppend, trueUCID[ii])
                    initializeOpenFragment = false
                }
                var resUCID = trueUCID[ii]
                ii += saveIPAddress.size
                initialize = false
                return resUCID
            }
            if (ii == saveIPAddress.size - 1) {
                cameraItemList.clear()
                findUSBCamera()
            }
            ii++
        }
        return ""
    }

    private fun usbDeviceGetState(USBIPAdress: String, modelName: String, UUID: String) {
        val url = "http://$USBIPAdress:36680/list?action=getlist"
        //String url = "http://" + "10.100.91.31" + ":36680/list?action=startscanipdev";
        Cameraresponse = false
        val jsonReq = JSONObject()
        val jsonObjReq: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET, url, jsonReq,
            Response.Listener { response ->
                Log.v("AVDebug", "PTZapp response for ez use state:$response")
                var StateOfPTZApp: String?
                try {
                    StateOfPTZApp = response.getString("Reason")
                    if (StateOfPTZApp == "KICKED") {
                        Log.v("AVDebug", "PTZapp response for ez use state: KICKED")
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
                try {
                    Log.v("AVDebug", "  usbDeviceGetState get ")
                    val CountNum = response.getInt("Count")
                    val jsonDataDevType = response.getString("List")
                    val getSoftwareversion = response.getString("SwVer")
                    Log.v("AVDebug", "  jsonDataDevType test : $jsonDataDevType")
                    val jsonDataDevType2 = jsonDataDevType.replace("[", "")
                    val jsonDataDevType3 = jsonDataDevType2.replace("]", "")
                    Log.v("AVDebug", " CountNum  : $CountNum")
                    val jsonData = jsonDataDevType3.split("\\}").toTypedArray()
                    for (i in jsonData.indices) {
                        jsonData[i] = jsonData[i] + "}"
                        if (i > 0) {
                            jsonData[i] = jsonData[i].substring(1)
                        }
                        Log.v("AVDebug", " extract[" + i + "]  : " + jsonData[i])
                    }
                    val DevTypeModelName = arrayOfNulls<String>(CountNum)
                    val DevUvcId = arrayOfNulls<String>(CountNum)
                    val OtaFwVer = arrayOfNulls<String>(CountNum)
                    val usbDeviceVdSup = arrayOfNulls<String>(CountNum)
                    val usbDeviceAdSup = arrayOfNulls<String>(CountNum)
                    val usbDeviceNtSup = arrayOfNulls<String>(CountNum)
                    val usbDeviceBTSup = arrayOfNulls<String>(CountNum)
                    val usbDeviceAISup = arrayOfNulls<String>(CountNum)
                    val usbDeviceVDiagSu = arrayOfNulls<String>(CountNum)
                    val usbDeviceVFwVer = arrayOfNulls<String>(CountNum)
                    val usbMacAddress = arrayOfNulls<String>(CountNum)
                    val jsonObject = arrayOfNulls<JSONObject>(CountNum)
                    for (i in 0 until CountNum) {
                        jsonObject[i] = JSONObject(jsonData[i])
                        DevTypeModelName[i] = jsonObject[i]!!.getString("DevType")
                        DevUvcId[i] = jsonObject[i]!!.getString("UvcId")
                        OtaFwVer[i] = jsonObject[i]!!.getString("OtaFwVer")
                        usbDeviceVdSup[i] = jsonObject[i]!!.getString("VideoSupport")
                        usbDeviceAdSup[i] = jsonObject[i]!!.getString("AudioSupport")
                        //0: 不支援Audio
                        //1: 支援全Audio
                        //2:　僅支援麥克風，但不顯示
                        //3: 僅支援麥克風，但要顯示頁簽
                        usbDeviceNtSup[i] = jsonObject[i]!!.getString("NetworkSupport")
                        usbDeviceAISup[i] = jsonObject[i]!!.getString("AISupport")
                        usbDeviceVDiagSu[i] = jsonObject[i]!!.getString("DiagImageSupport")
                        usbDeviceVFwVer[i] = jsonObject[i]!!.getString("FwVer")
                        usbMacAddress[i] = jsonObject[i]!!.getString("MacAddress")
                        Log.v(
                            "AVDebug", " DevTypeModelName[" + i + "] :" + DevTypeModelName[i]
                                    + " DevUvcId[" + i + "] :" + DevUvcId[i]
                                    + " OtaFwVer[" + i + "] :" + OtaFwVer[i]
                                    + " VideoSupport[" + i + "] :" + usbDeviceVdSup[i]
                                    + " AudioSupport[" + i + "] :" + usbDeviceAdSup[i]
                                    + " NetworkSupport[" + i + "] :" + usbDeviceNtSup[i]
                                    + " AISupport[" + i + "] :" + usbDeviceAISup[i]
                                    + " DiagImageSupport[" + i + "] :" + usbDeviceVDiagSu[i]
                                    + " usbDeviceVFwVer[" + i + "] :" + usbDeviceVFwVer[i]
                                    + " usbMacAddress[" + i + "] :" + usbMacAddress[i]
                        )
                        if (modelName == DevTypeModelName[i]) {
                            var UVCID = DevUvcId[i]
                            //DeviceOtaFwVer = OtaFwVer[i]
                            //DeviceVFwVer = usbDeviceVFwVer[i]
                            //MacAddress = usbMacAddress[i]
                            //SoftwareVersion = getSoftwareversion
                            Cameraresponse = true
                            Log.v("AVDebug", "UVCID : $UVCID")
                            //chooseUSBDeviceLogin(USBIPAdress, UVCID)
                            USBItemList.clear()
                        }
                    }
                    Log.v("AVDebug", "modelName : $modelName")
                } catch (e: JSONException) {
                    //some exception handler code.
                    Log.v("AVDebug", "PTZapp parse error!!")
                }
            },
            Response.ErrorListener { error: VolleyError ->
                Log.v(
                    "AVDebug",
                    "usbDeviceGetState : $error"
                )
            }) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Cache-Control"] = "no-store, no-cache"
                return headers
            }
        }
        jsonObjReq.setShouldCache(false)
        mQueue!!.add(jsonObjReq)
    }


    fun usbDeviceDetectState(USBIPAdress: String, trueUCID: String?) {
        val url = "http://$USBIPAdress:36680/list?action=login&key=$trueUCID"
        val jsonReq = JSONObject()
        val jsonObjReq: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET, url, jsonReq,
            Response.Listener { response ->
                //Bind Computer
                Log.v("AVDebug", "USBIPAdress :$USBIPAdress")
                Log.v("AVDebug", "trueUCID :$trueUCID")

                Log.v("AVDebug", "usbDeviceDetectState:$response")
                try {
                    val LoginPTZAppstate = response.getString("Result")
                    if (LoginPTZAppstate == "true") {
                        Log.v("AVDebug", "LoginPTZAppstate true")

                        //usbDeviceGetState(USBIPAdress, modelName, trueUCID)
                    } else {
                        Log.v("AVDebug", "LoginPTZAppstate false")
                    }
                    val jsonDataKey = response.getJSONObject("Key")
                    Log.v("AVDebug", "  jsonDataKey : $jsonDataKey")
                } catch (e: JSONException) {
                    //some exception handler code.
                    Log.v("AVDebug", "usbDeviceDetectState parse error!!")
                }
            },
            Response.ErrorListener { error: VolleyError ->
                Log.v(
                    "AVDebug",
                    "usbDeviceGetState : $error"
                )
            }) {
            override fun getHeaders(): Map<String, String> {
                //headers.put("Cache-Control", "no-store, no-cache");
                return HashMap()
            }
        }
        jsonObjReq.setShouldCache(false)
        mQueue!!.add<JSONObject>(jsonObjReq)
    }

    private fun showInternet() {
        if (mInternetWindow != null)
            return

        Log.v(TAG, "showInternet: $mLastNetwork")
        val tView = LayoutInflater.from(this).inflate(
            R.layout.window_internet, null
        )
        tView.btnInternetBack.setOnClickListener {
            mInternetWindow!!.dismiss()
            mInternetWindow = null
        }

        tView.btnInternetSetting.setOnClickListener {
            val tIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
            tIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(tIntent)
        }
        if (mLastNetwork == "wifi") {
            tView.imgInternet.setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_no_wifi_white,
                    null
                )
            )
        }

        mInternetWindow = PopupWindow(
            tView,
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT,
            false
        )

        // disappear popupWindow if touch outside of it
        mInternetWindow!!.isOutsideTouchable = true
        mInternetWindow!!.showAsDropDown(tView)
    }

    override fun onItemClicked(position: Int) {
        TODO("Not yet implemented")
    }

}


