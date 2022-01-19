package com.aver.superdirector.utility;

import android.content.Context;
import android.content.SharedPreferences;

public class AppConfig {
    private final String UserDataName = "UserData";

    private static final String AutoBringModel = "AutoBringModel";
    private static final String AutoBringIP = "AutoBringIP";
    private static final String AutoBringState = "AutoBringState";
    private static final String AutoBringUCIO = "AutoBringUCID";

    public void writeAutoBring(Context context, String model, String ip,int state, String UCIO) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(UserDataName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(AutoBringModel, model);
        editor.putString(AutoBringIP, ip);
        editor.putInt(AutoBringState, state);
        editor.putString(AutoBringUCIO, UCIO);
        editor.apply();
    }

    public String readAutoBringModel(Context context) {

        SharedPreferences sharedPreferences = context.getSharedPreferences(UserDataName, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AutoBringModel, "");
    }

    public String readAutoBringIP(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(UserDataName, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AutoBringIP, "");
    }

    public int readAutoBringState(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(UserDataName, Context.MODE_PRIVATE);
        return sharedPreferences.getInt(AutoBringState, -1);
    }

    public String readAutoBringUCIO(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(UserDataName, Context.MODE_PRIVATE);
        return sharedPreferences.getString(AutoBringUCIO, "");
    }

    public void clearAutoBringData(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(UserDataName, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();     // 若需要 return 結果，則可改用 commit()
    }
}
