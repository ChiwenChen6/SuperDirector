package com.aver.superdirector.utility;

import android.util.Log;

import com.aver.superdirector.utility.Enums;

import static com.aver.superdirector.utility.CameraFinder.SearchData.SD_SEARCH_TOTAL_SIZE;

public class USBItem {
    public int id;
    public Enums.CameraItemType itemType;
    public String PCName;
    public String ip, mask, dns, gateway;
    public String softwareVersion, firmwareVersion, serialNumber, macNumber;
    //public String loginName, loginPassword;
    public boolean autoBring;
    //private SearchData cameraData;
    public byte[] rawData = new byte[SD_SEARCH_TOTAL_SIZE];
    public String softwareState;

    public USBItem(int id, Enums.CameraItemType itemType, String ip, String cameraData, String PCName, String softwareVersion,
                   String softwareState, String serialNumber, String macNumber, boolean autoBring) {

        Log.v("AVDebug", "******* new usb camera item: " + ip + " - " + itemType + " - " + PCName);
        this.id = id;
        this.itemType = itemType;
        this.ip = ip;
        this.PCName = PCName;
        this.softwareVersion = softwareVersion;
        //this.firmwareVersion = firmwareVersion;
        this.softwareState = softwareState;

        this.serialNumber = serialNumber;
        this.macNumber = macNumber;
        this.autoBring = autoBring;

//        if (cameraData != null) {
//            //this.cameraData = new SearchData(cameraData);
//            System.arraycopy(cameraData, 0, this.rawData, 0, SD_SEARCH_TOTAL_SIZE);
//        }
    }
}
