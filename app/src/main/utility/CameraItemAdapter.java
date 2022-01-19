package com.aver.superdirector.utility;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.aver.superdirector.CamSelectAdapter;
import com.aver.superdirector.MainActivity;
import com.aver.superdirector.R;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

public class CameraItemAdapter extends RecyclerView.Adapter<CameraItemAdapter.ViewHolder> {
    private final List<com.aver.superdirector.utility.CameraItem> cameraItemList = new ArrayList<>();
    private final CardViewListenerInterface cardViewListenerInterface;
    private List<com.aver.superdirector.utility.CameraItem> mList;
    private List<com.aver.superdirector.utility.USBItem> mListUSB;
    private boolean holderClickable = true;
    private boolean holderSelected = false;
    private boolean BindState=false;
    public CameraItemAdapter(List<com.aver.superdirector.utility.CameraItem> itemList, CardViewListenerInterface listener) {
        this.cameraItemList.addAll(itemList);
        this.cardViewListenerInterface = listener;
    }

    public void setAccountList(List<com.aver.superdirector.utility.CameraItem> list) {
        mList = list;
        notifyDataSetChanged();
    }

    public void setHolderClicked(Boolean setBoolean) {
        holderClickable = setBoolean;
    }
    public void setHolderSelected(Boolean setBoolean, int position) {
        holderSelected = setBoolean;
    }

    public void setBindState(Boolean setBoolean, int position) {
        BindState = setBoolean;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.camera_item, parent, false);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_camera_item, null);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Log.v("AVDebug", " ----- onBindViewHolder  position: " + position);
        CameraItem tInfo = cameraItemList.get(position);
        holder.InfoItem = tInfo;
        com.aver.superdirector.utility.CameraItem item = cameraItemList.get(position);
        Log.v("AVDebug", "id: " + item.id + "   ip: " + item.ip);
        holder.ip.setText(item.ip);

        if (item.modelName.equals("CAM540HI")) {
            holder.modelName.setText("CAM540");
        } else {
            holder.modelName.setText(item.modelName);
        }
        holder.ip.setText(item.ip);

        holder.btnItemClicked.setClickable(holderClickable);

        holder.btnItemClicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.v("AVDebug", "holder.btnItemClicked.isSelected(): " + holder.btnItemClicked.isSelected());

                holder.btnItemClicked.setSelected(holder.btnItemClicked.isSelected());
                cardViewListenerInterface.onItemClicked(position);

            }
        });

    }

    @Override
    public int getItemCount() {
        return cameraItemList.size();
    }

    // TODO: 考慮加上 Search data
    public void addItem(int id, byte[] cameraData, String modelName, String ip, String softwareVersion,
                        String firmwareVersion, String serialNumber, String macNumber, boolean autoLogin) {
        Log.v("AVDebug", " ----- addItem  id: " + id);

        com.aver.superdirector.utility.CameraItem p = new com.aver.superdirector.utility.CameraItem(id, Enums.CameraItemType.IP, cameraData, modelName, ip, softwareVersion,
                firmwareVersion, serialNumber, macNumber, autoLogin);

        dumpData();
        cameraItemList.add(id, p);
        dumpData();
        //notifyItemInserted(id);
        notifyItemRangeChanged(id, cameraItemList.size());
    }



    public void cleanItem() {
        cameraItemList.clear();
        notifyDataSetChanged();
    }

    public void resetItem(List<com.aver.superdirector.utility.CameraItem> itemList) {
        Collections.sort(itemList, new Comparator<com.aver.superdirector.utility.CameraItem>() {
            public int compare(com.aver.superdirector.utility.CameraItem o1, com.aver.superdirector.utility.CameraItem o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        cameraItemList.addAll(itemList);
        //按照名稱排序

        notifyDataSetChanged();
        //notifyItemRangeChanged(0, cameraItemList.size());
    }

    public com.aver.superdirector.utility.CameraItem getCameraItem(int position) {
        return cameraItemList.get(position);
    }

    private void dumpData() {
        for (int i = 0; i < cameraItemList.size(); i++) {
            com.aver.superdirector.utility.CameraItem item = cameraItemList.get(i);
            Log.v("AVDebug", i + ": " + item.id + ": " + item.ip);
        }
        Log.v("AVDebug", "----------------------------------------------");
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CameraItem InfoItem;

        private final TextView modelName;
        private final TextView ip;
        private final Button btnItemClicked;
        private final ImageButton itemClickicon;
        private final ConstraintLayout itemClicked;

        ViewHolder(View itemView) {
            super(itemView);
            modelName = itemView.findViewById(R.id.camera_item_ModelName);
            ip = itemView.findViewById(R.id.camera_item_Ip);
            itemClickicon = itemView.findViewById(R.id.itemClicked);
            itemClicked = itemView.findViewById(R.id.info_root_item);
            btnItemClicked = itemView.findViewById(R.id.camera_item_button);

//            btnItemClicked.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Log.v("AVerDebug","Adapter Position IP: " +getBindingAdapterPosition());
//                    btnItemClicked.setSelected(!btnItemClicked.isSelected());
//                    //TODO 如果確認連接後才顯示 SCAN圖式要改變
//
//                    //String trueUCID = MainActivity.conpareIPSaved_UCID(ip.getText());
//                    String modelName =((MainActivity) getActivity()).conpareIPSaved_modelname(ip.getText());
//
//                    //((MainActivity) getActivity()).usbDeviceDetectState(ip.getText(), trueUCID);
//
//
//                    itemClicked.setVisibility(View.VISIBLE);
//
//                }
//            });
//            btnSetIp = itemView.findViewById(R.id.camera_item_EditIp);
//            btnSetIp.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    cardViewListenerInterface.onSetIpClicked(getAdapterPosition());
//                }
//            });

        }
    }
}
