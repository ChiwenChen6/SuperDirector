package com.aver.superdirector.utility;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.util.SparseIntArray;

import java.util.HashMap;


public class WidthConverter {
    private static String TAG = "WidthConverter";
    private static WidthConverter mWidthConverter = null;
    private static Resources mResources = null;
    private static SparseArray<Float> mTextSizes = null;
    private static HashMap<String, SparseIntArray> mList = null;
    private static float mDensity;
    private static int mWidthPixels;
    private static int mHeightPixels;
    private static float mWidthScale;
    private static float mHeightScale;
    private static boolean isScreen43 = false;

    private WidthConverter(Context context, DisplayMetrics dm){
        mResources = context.getResources();
        mDensity = dm.density;
        mWidthPixels = dm.widthPixels;
        mHeightPixels = dm.heightPixels;
        mWidthScale = ((float) dm.widthPixels)/(dm.density*1920);
        mHeightScale = ((float) mHeightPixels)/(dm.density*1080);
        mList = new HashMap<>();
        mTextSizes = new SparseArray<>();
    }

    public static WidthConverter getInstances(Context context, DisplayMetrics dm){
        if(mWidthConverter != null){
            mList.clear();
            mTextSizes.clear();
            mWidthConverter = null;
        }
        mWidthConverter = new WidthConverter(context, dm);
        return mWidthConverter;
    }

    // set Screen is 4:3, default is 16:9.
    public static void setScreen43(boolean is43){
        isScreen43 = is43;
        mHeightScale = ((float) mHeightPixels*4)/(mDensity*1080*3);
    }

    public static float getConvertedTextSize(int id){
        float res;
        if(mTextSizes.indexOfKey(id) < 0){
            res =  mResources.getDimension(id) * mWidthScale;
            mTextSizes.append(id, res);
        }else{
            res = mTextSizes.get(id);
        }
        return res;
    }

    public static int getConvertedWidth(String name, int id){
        SparseIntArray list = getWidthList(name);
        int res;
        if(list.indexOfKey(id) < 0){
            res = (int) (mResources.getDimension(id) * mWidthScale);
            list.append(id, res);
        }else{
            res = list.get(id);
        }
        return res;
    }

    public static int getConvertedWidth(String name, int id, boolean isHeight){
        String listName = isHeight? (name + "-height"):name;
        SparseIntArray list = getWidthList(listName);
        int res;
        if(list.indexOfKey(id) < 0){
            float scale = isHeight? mHeightScale:mWidthScale;
            res = (int) (mResources.getDimension(id) * scale);
            list.append(id, res);
        }else{
            res = list.get(id);
        }
        return res;
    }

    private static SparseIntArray getWidthList(String name){
        SparseIntArray res;
        if(mList.containsKey(name)){
            res = mList.get(name);
        }else{
            res = new SparseIntArray();
            mList.put(name, res);
        }
        return res;
    }

    public static int getScreenWidth(){
        return mWidthPixels;
    }

    public static int getScreenHeight(){
        return mHeightPixels;
    }
}
