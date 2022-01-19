package com.aver.superdirector.utility

import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection

class HttpRequestTask internal constructor() : AsyncTask<String, Void, Void>() {
    override fun doInBackground(vararg params: String?): Void? {
        val URL = params[0]
        val method = params[1]
        val option = params[2]
        var value =""
        if (params[3] != null) {
            value = params[3].toString()
        }

        val tLoginUrl = "http://127.0.0.1/$URL"
        val cred = JSONObject()
        try {
            cred.put("method", method)
            cred.put("option", option)
            if (params[3] != null) {
                cred.put("value", value)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val tData = cred.toString()

        var tReadJson: JSONObject? = null
        try {
            val tUrl = java.net.URL(tLoginUrl)
            val tConnection = tUrl.openConnection() as HttpURLConnection
            tConnection.requestMethod = "POST"
            tConnection.setRequestProperty("Content-Type", "application/json")

            val tOS = BufferedOutputStream(tConnection.outputStream)
            val tWriter = BufferedWriter(OutputStreamWriter(tOS, "UTF-8"))
            tWriter.write(tData)
            tWriter.flush()
            tWriter.close()

            val tIS = tConnection.inputStream
            val tReader = BufferedReader(InputStreamReader(tIS))
            var tLine: String?
            val tSB = StringBuilder()
            while (tReader.readLine().also { tLine = it } != null) {
                tSB.append(tLine)
                tSB.append('\r')
            }
            tReader.close()

            tReadJson = JSONObject(tSB.toString())
        } catch (e: Exception) {
            e.message?.let { Log.w("HttpTask", it) }
        }

        Log.e("HttpTask", "Result : " + tReadJson.toString())

        return null
    }

    override fun onPostExecute(result: Void?) {
    }
}