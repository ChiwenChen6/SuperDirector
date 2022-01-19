package com.aver.superdirector.utility;

import android.util.Log;

import static com.aver.superdirector.utility.CameraFinder.SearchData.SD_SEARCH_TOTAL_SIZE;

public class CameraItem {
    public int id;
    public Enums.CameraItemType itemType;
    public String modelName;
    public String ip, mask, dns, gateway;
    public String softwareVersion, firmwareVersion, serialNumber, macNumber;
    //public String loginName, loginPassword;
    public boolean autoBring;
    //private SearchData cameraData;
    public byte[] rawData = new byte[SD_SEARCH_TOTAL_SIZE];

    public CameraItem(int id, Enums.CameraItemType itemType, byte[] cameraData, String modelName, String ip, String softwareVersion,
                      String firmwareVersion, String serialNumber, String macNumber, boolean autoBring) {

        Log.v("AVDebug", "******* new camera item: " + id + " - " + itemType + " - " + modelName);
        this.id = id;
        this.itemType = itemType;
        this.modelName = modelName;
        this.ip = ip;
        this.softwareVersion = softwareVersion;
        this.firmwareVersion = firmwareVersion;
        this.serialNumber = serialNumber;
        this.macNumber = macNumber;
        this.autoBring = autoBring;
        Log.v("AVDebug", "******* new camera softwareVersion: " + softwareVersion + " - " + modelName);

        if (cameraData != null) {
            //this.cameraData = new SearchData(cameraData);

            System.arraycopy(cameraData, 0, this.rawData, 0, SD_SEARCH_TOTAL_SIZE);

        }
    }
}
