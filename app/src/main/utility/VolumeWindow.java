package com.aver.superdirector.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.aver.superdirector.R;

public class VolumeWindow extends PopupWindow {
    private final static String TAG = VolumeWindow.class.getSimpleName();

    private final int mMaxLevel;

    private final TextView mTxtValue;
    private final VerticalSeekBar mBarValue;

    private final Button mBtnMute;
    private final TextView mTxtMute;

    private final AudioManager mAudioManager;
    private static int volumeValue = 0;
    private final SharedPreferences sharedPref;
    @SuppressLint({"InflateParams", "CommitPrefEdits"})
    public VolumeWindow(Context context) {
        View tView = LayoutInflater.from(context).inflate(
                R.layout.window_volume,
                null);
        this.setContentView(tView);
        this.setHeight(RelativeLayout.LayoutParams.MATCH_PARENT);
        this.setWidth(RelativeLayout.LayoutParams.MATCH_PARENT);

        this.setFocusable(true);
        this.setOutsideTouchable(true);
        sharedPref = PreferenceManager
                .getDefaultSharedPreferences(context);

        mAudioManager = (AudioManager)context.getSystemService(
                Context.AUDIO_SERVICE);
        mMaxLevel = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        Button tBtnBack = tView.findViewById(R.id.btnVolumeBack);
        tBtnBack.setOnClickListener(v -> this.dismiss());

        mTxtValue = tView.findViewById(R.id.txtVolumeValue);

        mBarValue = tView.findViewById(R.id.barVolumeValue);
        mBarValue.getThumb().mutate().setAlpha(0);
        mBarValue.setStopListener(progress -> {
            int tLevel = 0;
            if (0 < progress && progress < 100)
                tLevel = ((progress * (mMaxLevel - 1)) / 100) + 1;
            else if (100 <= progress)
                tLevel = mMaxLevel;

            mAudioManager.setStreamVolume(
                    AudioManager.STREAM_MUSIC,
                    tLevel,
                    AudioManager.FLAG_PLAY_SOUND);
            Log.d(TAG, "progress=" + progress + ", tLevel=" + tLevel);
            volumeValue = tLevel;

            ToneGenerator tGen = new ToneGenerator(AudioManager.STREAM_MUSIC, 50);
            tGen.startTone(ToneGenerator.TONE_CDMA_PIP,50);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Log.w(TAG, e.getMessage());
            } finally {
                tGen.release();
            }
            AsyncTask<String, Void, Void> tTask = new HttpRequestTask();
            tTask.execute("set_string", "AVRSet", "VolumeValue", String.valueOf(volumeValue));
            sharedPref.edit().putString( "VolumeValue", String.valueOf(volumeValue));
            sharedPref.edit().apply();
            initUI();
        });

        mBtnMute = tView.findViewById(R.id.btnVolumeMute);
        mBtnMute.setOnClickListener(v -> {
            if (mBtnMute.isSelected()) {
                mBtnMute.setSelected(false);
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_UNMUTE, 0);

                int tLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                Log.d("Joey", "Vol:" + tLevel);
                if (tLevel == 0)
                    mAudioManager.setStreamVolume(
                            AudioManager.STREAM_MUSIC,
                            volumeValue,
                            AudioManager.FLAG_PLAY_SOUND);
                int tValue = (tLevel * 100) / mMaxLevel;
                AsyncTask<String, Void, Void> tTask = new HttpRequestTask();
                tTask.execute("set_string", "AVRSet", "VolumeValue", String.valueOf(tValue));
                sharedPref.edit().putString( "VolumeValue", String.valueOf(tValue));
                sharedPref.edit().apply();


            } else {
                mBtnMute.setSelected(true);
                mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_MUTE, 0);
                AsyncTask<String, Void, Void> tTask = new HttpRequestTask();
                tTask.execute("set_string", "AVRSet", "VolumeValue", String.valueOf(0));
                sharedPref.edit().putString( "VolumeValue", String.valueOf(0));
                sharedPref.edit().apply();
            }

            initUI();
        });

        mTxtMute = tView.findViewById(R.id.txtVolumeMute);

        initUI();
    }

    //////////////////////
    // Private Function //
    //////////////////////

    private void initUI() {
        int tLevel = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        int tValue = (tLevel * 100) / mMaxLevel;
        volumeValue = tLevel;

        mTxtValue.setText(String.valueOf(tValue));
        mBarValue.setProgress(tValue);

        if (tValue == 0) {
            mBtnMute.setSelected(true);
            mTxtMute.setText(R.string.unmute);
        } else {
            mBtnMute.setSelected(false);
            mTxtMute.setText(R.string.mute);
        }
        AsyncTask<String, Void, Void> tTask = new HttpRequestTask();
        tTask.execute("set_string", "AVRSet", "VolumeValue", String.valueOf(tValue));
        sharedPref.edit().putString( "VolumeValue", String.valueOf(tValue));
        sharedPref.edit().apply();
    }

}