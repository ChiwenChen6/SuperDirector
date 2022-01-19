package com.aver.superdirector.BaseView;

import android.graphics.Bitmap;

import com.android.volley.VolleyError;

public abstract class HttpCallback {

    /**
     * 一般 Http 請求成功時回調
     *
     * @param method HttpRequest 請求命令字串
     * @param data "getXXX" 時 "data" 回傳值
     */
    public void onSuccess(String method, String data) {
    }

    /**
     * 一般 Http 請求失敗時回調
     *
     * @param method : Http 請求命令字串
     */
    public void onFailure(String method, VolleyError error) {
    }

    /**
     * Preset preview Bitmap 請求成功時回調 (getPresetImage)
     * @param id : Preset id
     * @param stopId : 連續收取畫面，到 stopId 為止
     */
    public void onGetImageSuccess(int id, int stopId, Bitmap bitmap) {
    }

    /**
     * Preset preview Bitmap 請求失敗時回調
     * @param id : Preset id
     */
    public void onGetImageFailure(int id, int stopId, VolleyError error) {
    }
}
