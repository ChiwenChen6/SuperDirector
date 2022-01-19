package com.aver.superdirector.BaseView;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import com.aver.superdirector.BaseView.WebSocket.JWebSocketClient;
import com.aver.superdirector.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;

import static java.lang.Thread.sleep;


@SuppressLint("Registered")
public abstract class BaseCameraActivity extends AppCompatActivity {

    protected String loginIP, loginName, loginPassword, loginToken, modelName;
    protected int screenWidth, screenHeight;
    protected boolean useLoginName;
    protected boolean liveEnable;   // 紀錄 Camera 是否可用

    // 用來判斷是否是自己 執行 login 時 ( 例如恢復出廠值時，需重新取得 token ) 目前流程還沒用到此變數
    protected boolean re_login;
    protected URI uri;
    protected JWebSocketClient webSocketClient;
    protected Handler webSocketHandler = null;
    protected HandlerThread webSocketHandlerThread = null;
    protected String webSocketHandlerThreadName = "KeepAlive";
    protected int aliveCount = 60;
    boolean isWebSocketHandClose;

    // Dialog
    // OK back Home dialog
    private AlertDialog dlgOkBackHome;
    private TextView tvOkBackHomeMessage;
    // waiting dialog
    private AlertDialog dlgWaiting;
    private TextView tvWaitingMessage;

    protected RequestQueue mQueue, mLiveQueue, mLiveQueue2;
    protected RequestQueue moveQueue;
    protected JSONObject jsonData;



    // timer for UDP live image
    protected Handler handlerLiveTimer;
    protected Runnable runnableLiveTimer;
    protected Handler handlerAILiveTimer;
    protected Runnable runnableAILiveTimer;
    protected Handler handlerLiveTimerVB130E;
    protected Runnable runnableLiveTimerVB130E;
    protected HttpCallback getLiveEnableCallback;

    protected Fragment currentFragment, nextFragment;
    protected String nextFragmentTitle;

    // Aver Settings
    protected String averPasswordEnable;
    protected String averPassword;
    protected boolean currentVdoState;
    // View Timer
    protected String srTmpTime = "0";
    protected long lgTmpTime = 0;
    Calendar mCalendar;
    String mFormat = "HH:mm:ss";
    // check latest FW version
    protected String latestFWVersion;
    protected HttpCallback FWCallback;

    // led
    public int tmpLED;
    protected long newsrTmpTime = 0;
    private final static String LED_GREEN = "D3";
    private final static String LED_BLUE = "D5";
    private final static String LED_RED = "D2";

    private final static String LED_COLOR = "EXTRA_LED_COLOR";
    private final static String LED_VALUE = "EXTRA_LED_VALUE";
    private final static String LED_STATE = "EXTRA_LED_STATE";
    private int countLEDStatus;
    private int countLEDtmp;
    private int countCheckFWversion;

    // mic mute state
    public int tmpMicMute;
    // 獲取時間
    public long currentTime;
    public long currentStartTime;
    public long endTime;

    private Timer timer;
    private int cntKeepAlive;
    public RadioButton rbUSBCameraItem_MB_Audio;
    public RadioButton rbUSBCameraItem_MB_Network;
    public String deviceFWVersion;
    public String sys_vdo_mode;

    public void updateLoginToken(String token) {
        closeWebSocket();
        loginToken = token;
        createWebSocket();
    }

    public long getTime() {
        currentTime = System.currentTimeMillis(); // 獲取開始時間
        return currentTime;
    }

    public long getDurationTime() {
        return endTime - currentTime;
    }

    public boolean gettmpLED() {
        return (tmpLED == 1);
    }

    public void setLEDstate(int newLEDtmp) {
        tmpLED = newLEDtmp;
    }

    public Handler LEDhandler = new Handler();

    public Runnable LEDrunnable = new Runnable() {
        @Override
        public void run() {

            currentStartTime = getTime();
            //Log.v("AVDebug", "tmpMicMute : " + tmpMicMute);
            //要做的事情
            if (newsrTmpTime != lgTmpTime) {
                //Log.v("AVDebug", "newsrTmpTime != lgTmpTime");
                //Log.v("AVDebug", "tmpLED = " + tmpLED);
                countLEDtmp = 0;
                if (tmpLED != 1) {
                    Log.v("AVDebug", "turnLedBlue");
                    turnBlueLed();
                }
            } else {
                countLEDtmp++;
                if (tmpMicMute == 1 && tmpLED != 0) {
                    tmpLED = 0;
                }
                //Log.v("AVDebug", "newsrTmpTime == lgTmpTime");
                if (tmpLED == 1 && countLEDtmp > 3) {
                    Log.v("AVDebug", "turnLedOff");

                    turnBlueLedOff();
                }
            }
            newsrTmpTime = lgTmpTime;

            LEDhandler.postDelayed(this, 1002);
        }
    };

    public void back() {


        Intent homeIntent = new Intent();
        homeIntent.putExtra("RES", "ans");
        //setting the result
        setResult(98, homeIntent);
        //this method will close the current activity
        finish();
    }

    private Bundle loadArguments() {
        Bundle bundle = getIntent().getExtras();
        assert bundle != null;
        //loginToken = bundle.getString(MainActivity.mToken);
        return bundle;
    }

    protected abstract void loadFragments(Bundle bundle);

    protected void transactionFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (currentFragment != null) {
            fragmentTransaction.hide(currentFragment);
        }

        try {
            fragmentTransaction.show(nextFragment);
            fragmentTransaction.commitAllowingStateLoss();
        } catch (Exception ignored) {
        }

        currentFragment = nextFragment;
    }

    public void turnRedLed() {
        // Turn LED RED
        ledCtl(LED_RED, 8, 1);
        //ledCtl(LED_GREEN, 0, 1);
        //ledCtl(LED_BLUE, 0, 1);
    }

    public void turnRedLedoff() {
        // Turn LED RED
        ledCtl(LED_RED, 0, 1);
        //ledCtl(LED_GREEN, 0, 1);
        //ledCtl(LED_BLUE, 0, 1);
    }

    public void turnBlueLed() {
        // Turn LED BLUE
        //ledCtl(LED_RED, 0, 1);
        //ledCtl(LED_GREEN, 0, 1);
        ledCtl(LED_BLUE, 8, 1);
        tmpLED = 1;

    }

    public void turnLedWhite() {
        // Turn LED green
        ledCtl(LED_RED, 8, 1);
        ledCtl(LED_GREEN, 8, 1);
        ledCtl(LED_BLUE, 8, 1);
    }

    public void turnBlueLedOff() {
        // Turn LED off
        //ledCtl(LED_RED, 0, 1);
        //ledCtl(LED_GREEN, 0, 1);
        ledCtl(LED_BLUE, 0, 1);
        tmpLED = 0;
    }

    public void turnLedOff() {
        // Turn LED off
        ledCtl(LED_RED, 0, 1);
        ledCtl(LED_GREEN, 0, 1);
        ledCtl(LED_BLUE, 0, 1);
    }


    public void ledCtl(String color, int value, int state) {

        Intent intent = new Intent("com.xhb.action.LED_CTL");
        intent.putExtra(LED_COLOR, color);
        intent.putExtra(LED_VALUE, value);
        intent.putExtra(LED_STATE, state);
        getApplicationContext().sendBroadcast(intent);
    }

    public void getCameraSetting(final String method, JSONObject jsonReq, final String nextMethod,
                                 final HttpCallback callback) {
        String url = "http://" + loginIP + "/" + method;

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.POST, url, jsonReq,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.v("AVDebug", response.toString());

                        try {
                            String responseCode = response.getString("code");
                            String responseMsg = response.getString("msg");
                            String responseData = response.getString("data");
                            Log.v("AVDebug", responseCode + "   msg:" + responseMsg);

                            if (callback != null && nextMethod != null) {
                                callback.onSuccess(nextMethod, responseData);
                            }
                        } catch (JSONException e) {
                            //some exception handler code.
                            Log.v("AVDebug", "parse error!!");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.v("AVDebug", "getCameraSetting onErrorResponse: " + error.toString());
                if (callback != null) {
                    callback.onFailure(method, error);
                }
                if (!isWebSocketHandClose) {
                    String msg = "Get camera setting fail!!  method: " + method + "\n[ " + error.toString() + " ]";
                    Log.v("AVDebug", msg);
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                Log.v("AVDebug", "getCameraSetting: call getHeaders ");
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Cache-Control", "no-store, no-cache");
                headers.put("Authorization", "bearer " + loginToken);
                return headers;
            }
        };
        jsonObjReq.setShouldCache(false);
        Log.v("AVDebug", "getCameraSetting mQueue.add: " + method);
        mQueue.add(jsonObjReq);
    }

    public String getCameraOption(String itemName) {
        if (jsonData != null) {
            String optionData = null;
            try {
                optionData = jsonData.getString(itemName);
            } catch (JSONException e) {
                //some exception handler code.
                Log.v("AVDebug", "getCameraOption() parse error!!");
            }

            if (optionData != null) {
                return optionData.replaceAll("[\\[\\]]", "");
            }
        }
        return "";
    }

    public String getCameraOption(String itemName, int index) {
        if (jsonData != null) {
            String optionData = null;
            try {
                optionData = jsonData.getString(itemName);
            } catch (JSONException e) {
                //some exception handler code.
                Log.v("AVDebug", "getCameraOption_index() parse error!!");
            }
            if (itemName.contains("sys_vdo_mode")) {
                Log.d("AVDebug", " getCameraOption currentVdoState=" + currentVdoState + ", optionData=" + optionData);
            }

            if (optionData != null) {
                String result = optionData.replaceAll("[\\[\\]]", "");
                String[] options = result.split(",");
                return options[index];
            }
        }
        return "";
    }

    public static JSONObject httpsRequest(String Url, String requestMethod, String outputStr) {
        JSONObject jsonObject = null;
        StringBuffer buffer = new StringBuffer();
        try {
            URL url = new URL(Url);
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setConnectTimeout(1000);
            httpUrlConn.setUseCaches(false);
            // 設置請求方式（GET/POST）
            httpUrlConn.setRequestMethod(requestMethod);

            if ("GET".equalsIgnoreCase(requestMethod)) {
                httpUrlConn.connect();
            }
            // 當有數據需要提交時
            if (null != outputStr) {
                OutputStream outputStream = httpUrlConn.getOutputStream();
                // 注意編碼格式，防止中文亂碼
                outputStream.write(outputStr.getBytes(StandardCharsets.UTF_8));
                outputStream.close();
            }

            // 將返回的輸入流轉換成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            // 釋放資源
            inputStream.close();
            inputStream = null;
            httpUrlConn.disconnect();
            jsonObject = new JSONObject(buffer.toString());
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    protected void getVideoMode(HttpCallback callback) {
        JSONObject jsonReq = new JSONObject();
        try {
            jsonReq.put("method", "Get");
            jsonReq.put("option", "sys_vdo_mode");
        } catch (JSONException e) {
            //some exception handler code.
            Log.v("AVDebug", "error!!");
        }
        getCameraSetting("get_option", jsonReq, "setLiveBtn", callback);
    }

    public boolean isLiveEnable() {
        return liveEnable;
    }

    // WebSocket & keep alive
    private final Handler SomeHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            String option = "";
            String value = "";
            int ledcount = 0;

            try {
                JSONObject jsonObject = new JSONObject((String) msg.obj);
                JSONObject json = jsonObject.getJSONObject("data");
                if (json.has("option")) {
                    option = json.getString("option");
                }
                if (json.has("value")) {
                    value = json.getString("value");
                }
                Log.i("AVDebug", "<WS> option: " + option + "     value: " + value);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            //websocket 的全部option 去做比對

            if (option.equals("sys_stream_time")) {
                // 讀取stram time時間  如果為空值 設定0 避免錯誤
                if (value == null) {
                    srTmpTime = "0";
                }
                if (value != null) {
                    srTmpTime = value;
                }
                lgTmpTime = Long.parseLong(srTmpTime);
                Log.v("AVDebug", "lgTmpTime :" + lgTmpTime);

            }
            //靜音 最大權限顯示紅色lED
            if (option.equals("ado_mic_mute")) {
                switch (option) {
                    case "0":
                        tmpMicMute = 0;
                        if (tmpLED == 1) {
                            turnBlueLed();
                            turnRedLedoff();
                        }
                        if (tmpLED == 0) {
                            turnBlueLedOff();
                            turnRedLedoff();
                        }
                        break;
                    case "1":
                        tmpMicMute = 1;
                        if (tmpLED == 1) {
                            turnBlueLedOff();
                            turnRedLed();
                        }
                        if (tmpLED == 0) {
                            turnBlueLedOff();
                            turnRedLed();
                        }
                        break;
                    default:
                        break;
                }
            }

            return true;
        }
    });
    private final Runnable sendAlive = new Runnable() {
        public void run() {


            while (webSocketClient != null && webSocketClient.isOpen()) {
                // alive 功能先保留
                cntKeepAlive = 0;
                aliveCount++;
                Log.v("AVDebug", "alive.....");
                countLEDStatus++;
                if (countLEDStatus >= 10) {     // 120秒內與 Server 連線一次
                    Log.v("AVDebug", "check LED.....");
//                    check_thread();
                    countLEDStatus = 0;
                }
                if (aliveCount >= 120) {     // 120秒內與 Server 連線一次
                    //if (aliveCount >= 59) {     // 60秒內與 Server 連線一次
                    Log.v("AVDebug", "send alive.....");
                    webSocketClient.send("alive");
                    aliveCount = 0;
                }
            }

            Log.v("AVDebug", " stop sendAlive thread");
            if (!isWebSocketHandClose) {
                String msg = "keep alive fail!!";
            }
        }
    };

    private void createWebSocket() {
        uri = URI.create("ws://" + loginIP + ":9187/ws");
        webSocketClient = new JWebSocketClient(uri) {
            @Override
            public void onMessage(String message) {
                //message 就是接收到的消息
                //Log.v("AVDebug", message);
                Message msg = new Message();
                msg.obj = message;
                msg.what = 0;
                SomeHandler.sendMessage(msg);
            }
        };
        try {
            webSocketClient.connectBlocking();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (webSocketClient != null && webSocketClient.isOpen()) {
            isWebSocketHandClose = false;
            webSocketClient.send("token:" + loginToken);
            if (webSocketHandlerThread == null) {
                webSocketHandlerThread = new HandlerThread(webSocketHandlerThreadName);
                webSocketHandlerThread.start();
            }
            webSocketHandler = new Handler(webSocketHandlerThread.getLooper());
            webSocketHandler.post(sendAlive);
        } else {
            isWebSocketHandClose = true;
            String msg = "create keep alive fail!!";
            Log.v("AVDebug", msg);
        }
    }

    public void closeWebSocket() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            isWebSocketHandClose = true;
            webSocketClient.close();
            int waitCount = 10;
            while (!webSocketClient.isClosed() && waitCount > 0) {
                try {
                    sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                waitCount--;
                Log.v("AVDebug", "client closeing......");
            }
            webSocketClient = null;
            Log.v("AVDebug", "set client = null");
        }
        if (webSocketHandlerThread != null) {
            webSocketHandlerThread.quit();
            webSocketHandlerThread.quitSafely();
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("AVDebug", " ---------- " + this.getLocalClassName() + " onCreate.....(B)");
        Bundle bundle = loadArguments();
        // load fragments
        loadFragments(bundle);

        // init
        HTTPSTrustManager.allowAllSSL();    // https 信任所有憑證
        mQueue = Volley.newRequestQueue(this);
        moveQueue = Volley.newRequestQueue(this);
        mLiveQueue = Volley.newRequestQueue(this);
        mLiveQueue2 = Volley.newRequestQueue(this);

        dlgWaiting = null;
        dlgOkBackHome = null;
        re_login = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Activity將要被顯示出來
        Log.v("AVDebug", " ---------- " + this.getLocalClassName() + " onStart.....(B)");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Activity已經被顯示出來
        Log.v("AVDebug", " ---------- " + this.getLocalClassName() + " onResume.....(B)");
        if (webSocketClient == null) {
            createWebSocket();
        }
        LEDhandler.postDelayed(LEDrunnable, 2200);//每两秒执行一次runnable.

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        // Activity正在被重新啟動
        Log.v("AVDebug", " ---------- " + this.getLocalClassName() + " onRestart.....(B)");
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 另一個Activity已經得到焦點，而該Activity將要被暫停
        Log.v("AVDebug", " ---------- " + this.getLocalClassName() + " onPause.....(B)");
        turnLedOff();
        LEDhandler.removeCallbacks(LEDrunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Activity已經為不可見狀態，且停止運作了
        Log.v("AVDebug", " ---------- " + this.getLocalClassName() + " onStop.....(B)");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Activity已經被終結並釋放所佔的記憶體
        Log.v("AVDebug", " ---------- " + this.getLocalClassName() + " onDestroy.....(B)");
        closeWebSocket();
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Log.v("AVDebug", " ---------- " + this.getLocalClassName() + " onAttachedToWindow.....(B)");
    }
}
