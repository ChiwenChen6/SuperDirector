package com.aver.superdirector.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aver.superdirector.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CallLogAdapter extends
        RecyclerView.Adapter<CallLogAdapter.ViewHolder> {
     private final String TAG = "CallLogAdapter";

    private final Resources mResources;
    private final List<AVerCallLog> mList;
    private ArrayList<AVerAccountInfo> mAccountList = new ArrayList<>();
    private final AccountInfoAdapter.ItemActionListener mListener;
    private Context context;

    public CallLogAdapter(
            Resources resources,
            List<AVerCallLog> list,
            AccountInfoAdapter.ItemActionListener listener) {

        mResources = resources;
        mList = list;
        mListener = listener;
    }

    ////////////////
    // Life Cycle //
    ////////////////

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View tView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.view_calllog,
                parent,
                false);
        context = parent.getContext();

        return (new ViewHolder(tView));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AVerCallLog tLog = mList.get(position);

        holder.LogItem = tLog;

        holder.TxtName.setText(tLog.ContactName);
        holder.TxtRemote.setText(tLog.RemoteUri);

        AVerAccountInfo accountInfo  = null;
        for(AVerAccountInfo info : mAccountList){
            if(tLog.RemoteUri.equals(info.RemoteUri)) {
//                Log.v(TAG, "AVerAccountInfo: info " + info.Id + "-" + info.AccountName + "-" + info.RemoteUri);
                accountInfo = info;
                break;
            }
        }
        if(accountInfo != null){
            holder.BtnAdd.setVisibility(View.INVISIBLE);
            holder.TxtName.setText(accountInfo.AccountName);
        }else{
            holder.BtnAdd.setVisibility(View.VISIBLE);
            accountInfo = new AVerAccountInfo();
            accountInfo.RemoteUri = tLog.RemoteUri;
        }
        holder.mInfo = accountInfo;
        /*
        // 2021-09-08 Netpool
        // Here is for UX fucking stupid UEC-UX Design
        if (tLog.ContactName.isEmpty()) {
            holder.TxtName.setText(tLog.RemoteUri);
            holder.TxtRemote.setVisibility(View.INVISIBLE);
        }
        */

        if (tLog.CallStat == AVerCallLog.STAT_MISS) {
            holder.ImgType.setImageResource(R.drawable.ic_call3);

            String tTxt = mResources.getString(R.string.missed_call);
            holder.TxtDuration.setText(tTxt);
        } else {
            if (tLog.CallStat == AVerCallLog.STAT_OUT)
                holder.ImgType.setImageResource(R.drawable.ic_call1);
            else if (tLog.CallStat == AVerCallLog.STAT_IN)
                holder.ImgType.setImageResource(R.drawable.ic_call2);

            String tTxt = mResources.getString(R.string.call_cancelled);
            if (0 < tLog.DurationSecond && tLog.DurationSecond < 60)
                tTxt = tLog.DurationSecond + " Sec";
            else if (60 <= tLog.DurationSecond)
                tTxt = (tLog.DurationSecond / 60) + " Min";
            else if (tLog.DurationSecond < 0)
                tTxt = mResources.getString(R.string.call_failed);
            holder.TxtDuration.setText(tTxt);
        }

        String[] tArray = tLog.StartDateTime.split(" ");
        if (tArray.length < 2)
            return;

        String tDate = tArray[0];
        for (int i = 0; i < 7; i++) {
            long tMs = i * 86400 * 1000 * (-1);
//            String tCurrent = getData(tMs);
//            if (tCurrent.compareTo(tDate) != 0)
//                continue;
//
//            if (i == 0)
//                tDate = mResources.getString(R.string.today);
//            else if (i == 1)
//                tDate = mResources.getString(R.string.yesterday);
//            else
               // tDate = getWeekday(tMs);
        }
        holder.TxtDate.setText(tDate);

        holder.TxtTime.setText(tArray[1]);
        try {
            TransCallLogInfoFormat2String(context,position,holder,tLog.SipH323);
        } catch (JSONException e) {
            Log.d("CallLog", "JSONException error");

        }
    }
    public ArrayList<AVerAccountInfo> getmAccountList() {
        return mAccountList;
    }
    public void TransCallLogInfoFormat2String (Context context,int position,ViewHolder holder,String SipH323Name) throws JSONException {
        //In each Holder
        SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor mEditor = mPreferences.edit();
        JSONObject mAccountListJsondata =new JSONObject();
        StringBuilder infobuilder = new StringBuilder();

        String[] tArray = holder.LogItem.StartDateTime.split(" ");
        if (tArray.length < 2)
            return;

        String tDate = tArray[0];
        for (int i = 0; i < 7; i++) {
            long tMs = i * 86400 * 1000 * (-1);
            //String tCurrent = SipH323Service.getData(tMs);
//            if (tCurrent.compareTo(tDate) != 0)
//                continue;
//
//            if (i == 0)
//                tDate = mResources.getString(R.string.today);
//            else if (i == 1)
//                tDate = mResources.getString(R.string.yesterday);
//            else
//                tDate = SipH323Service.getWeekday(tMs);
        }

        infobuilder.append(holder.LogItem.ContactName);
        infobuilder.append(",");
        infobuilder.append(holder.LogItem.RemoteUri);
        infobuilder.append(",");
        infobuilder.append(holder.LogItem.CallStat);
        infobuilder.append(",");
        infobuilder.append(tArray[0]);
        infobuilder.append(",");
        infobuilder.append(tArray[1]);
        mAccountListJsondata.put("Callin Log item Length :"+position , infobuilder);

        // write preference.xml

        //send HTTP request for websocket update
        // NAME  : Type +number
        // VALUE : Id,AccountName,RemoteUri,IsFavorite,Quality
        Log.d("CallLog Task", "$i:\n${infobuilder.toString()}");
        if(SipH323Name.equals("H.323")){
            mEditor.putString("RecentH.323"+position,infobuilder.toString());
            mEditor.apply();
            AsyncTask<String, Void, Void> tTask = new HttpRequestTask();
            tTask.execute("set_string", "AVRSet", "RecentH.323"+position, infobuilder.toString());
        }else if(SipH323Name.equals("SIP")) {
            mEditor.putString("RecentSIP"+position,infobuilder.toString());
            mEditor.apply();
            AsyncTask<String, Void, Void> tTask = new HttpRequestTask();
            tTask.execute("set_string", "AVRSet", "RecentSIP"+position, infobuilder.toString());
        }
    }
    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setAccountList(ArrayList<AVerAccountInfo> list){
        mAccountList.clear();
        mAccountList = list;
    }

    //////////////////
    // Class Member //
    //////////////////

    class ViewHolder extends RecyclerView.ViewHolder {
        View ViewMain;
        Button BtnAdd;
        TextView TxtName;
        TextView TxtRemote;
        ImageView ImgType;
        TextView TxtDuration;
        TextView TxtDate;
        TextView TxtTime;

        AVerCallLog LogItem;
        AVerAccountInfo mInfo;

        ViewHolder(View view) {
            super(view);

            ViewMain = view;

            ViewMain.findViewById(R.id.rlItem).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onItemClick(mInfo);
                }
            });

            BtnAdd = ViewMain.findViewById(R.id.btnAdd);
            BtnAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mListener.onEditClick(mInfo);
                }
            });

            TxtName = ViewMain.findViewById(R.id.txtName);
            TxtRemote = ViewMain.findViewById(R.id.txtRemote);
            ImgType = ViewMain.findViewById(R.id.imgType);
            TxtDuration = ViewMain.findViewById(R.id.txtDuration);

            TxtDate = ViewMain.findViewById(R.id.txtDate);
            TxtTime = ViewMain.findViewById(R.id.txtTime);
        }
    }

}