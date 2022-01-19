package com.aver.superdirector.utility;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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

public class ComputerItemAdapter extends RecyclerView.Adapter<ComputerItemAdapter.ViewHolder> {
    private final List<CameraItem> cameraItemList = new ArrayList<>();
    private final CardViewListenerInterface cardViewListenerInterface;
    private List<CameraItem> mList;
    private List<USBItem> mListUSB;
    private boolean holderClickable = true;
    private boolean holderSelected = false;
    private boolean BindState=false;

    public ComputerItemAdapter(List<CameraItem> itemList, CardViewListenerInterface listener) {
        this.cameraItemList.addAll(itemList);
        this.cardViewListenerInterface = listener;
    }
    public void setAccountList(List<CameraItem> list) {
        mList = list;
        notifyDataSetChanged();
    }
    public void setUSBAccountList(List<USBItem> list) {
        mListUSB = list;
        notifyDataSetChanged();
    }
    public void setHolderClicked(Boolean setBoolean) {
        holderClickable = setBoolean;
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
        CameraItem item = cameraItemList.get(position);
        Log.v("AVDebug", "id: " + item.id + "   ip: " + item.ip);

            if(item.modelName.equals("CAM540HI")){
                holder.modelName.setText("CAM540");
            } else {
                holder.modelName.setText(item.modelName);
            }
            holder.ip.setText( item.ip);
        holder.btnItemClicked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.btnItemClicked.setSelected(!holder.btnItemClicked.isSelected());
            }
        });
    }

    public static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return cameraItemList.size();
    }

    // TODO: 考慮加上 Search data
    public void addItem(int id, byte[] cameraData, String modelName, String ip, String softwareVersion,
                        String firmwareVersion, String serialNumber, String macNumber, boolean autoLogin) {
        Log.v("AVDebug", " ----- addItem  id: " + id);

        CameraItem p = new CameraItem(id, Enums.CameraItemType.IP, cameraData, modelName, ip, softwareVersion,
                firmwareVersion, serialNumber, macNumber, autoLogin);

        dumpData();
        cameraItemList.add(id, p);
        dumpData();
        //notifyItemInserted(id);
        notifyItemRangeChanged(id, cameraItemList.size());
    }

    public void removeItem(int position) {
        cameraItemList.remove(position);
        notifyItemRemoved(position);
    }

    public void cleanItem() {
        cameraItemList.clear();
        notifyDataSetChanged();
    }

    public void resetItem(List<CameraItem> itemList) {
        Collections.sort(itemList, new Comparator<CameraItem>() {
            public int compare(CameraItem o1, CameraItem o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        cameraItemList.addAll(itemList);
        //按照名稱排序

        notifyDataSetChanged();
        //notifyItemRangeChanged(0, cameraItemList.size());
    }

    public CameraItem getCameraItem(int position) {
        return cameraItemList.get(position);
    }

    private void dumpData() {
        for (int i = 0; i < cameraItemList.size(); i++) {
            CameraItem item = cameraItemList.get(i);
            Log.v("AVDebug", i + ": " + item.id + ": " + item.ip);
        }
        Log.v("AVDebug", "----------------------------------------------");
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        CameraItem InfoItem;

        private final TextView modelName;
        private final TextView ip;
        private final Button btnItemClicked;
        private final ImageButton itemClicked;
        ViewHolder(View itemView) {
            super(itemView);
            modelName = itemView.findViewById(R.id.camera_item_ModelName);
            ip = itemView.findViewById(R.id.camera_item_Ip);
            itemClicked = itemView.findViewById(R.id.itemClicked);

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
