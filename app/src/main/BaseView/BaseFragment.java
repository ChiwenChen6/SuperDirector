package com.aver.superdirector.BaseView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.aver.superdirector.MainActivity;


public class BaseFragment extends Fragment {
    protected String loginIP, loginToken, modelName;


    public void parseMessage(String option, String value) {
    }

    protected void loadArguments() {
        assert getArguments() != null;
//        loginToken =(String) getArguments().get(MainActivity.mToken);
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.v("AVDebug", "      ---------- " + getClass().getName() + " onCreateView.....(B)");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v("AVDebug", "      ---------- " + getClass().getName() + " onCreate.....(B)");
    }

    @Override
    public void onStart() {
        super.onStart();
        // Activity將要被顯示出來
        Log.v("AVDebug", "      ---------- " + getClass().getName() + " onStart.....(B)");
    }

    @Override
    public void onResume() {
        super.onResume();
        // Activity已經被顯示出來
        Log.v("AVDebug", "      ---------- " + getClass().getName() + " onResume.....(B)");
    }

    @Override
    public void onPause() {
        super.onPause();
        // 另一個Activity已經得到焦點，而該Activity將要被暫停
        // Another activity is taking focus (this activity is about to be "paused").
        Log.v("AVDebug", "      ---------- " + getClass().getName() + " onPause.....(B)");
    }

    @Override
    public void onStop() {
        super.onStop();
        // Activity已經為不可見狀態，且停止運作了
        Log.v("AVDebug", "      ---------- " + getClass().getName() + " onStop.....(B)");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v("AVDebug", "      ---------- " + getClass().getName() + " onDestroy.....(B)");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v("AVDebug", "      ---------- " + getClass().getName() + " onActivityCreated.....(B)");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.v("AVDebug", "      ---------- " + getClass().getName() + " onViewStateRestored.....(B)");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.v("AVDebug", "      ---------- " + getClass().getName() + " onDestroyView.....(B)");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.v("AVDebug", "      ---------- " + getClass().getName() + " onDetach.....(B)");
    }
}
