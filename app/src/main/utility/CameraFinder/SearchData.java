package com.aver.superdirector.utility.CameraFinder;

import android.util.Base64;
import android.util.Log;

public class SearchData {
    // 0x0:none; 0x1:update; 0x2: search;
    //private static final byte SD_HEAD_NONE = 0x0;
    private static final byte SD_HEAD_UPDATE = 0x1;
    //private static final byte SD_HEAD_SEARCH = 0x2;
    private static final byte[] SD_MODE_DHCP = {'d', 'h', 'c', 'p'};      // for update
    private static final byte[] SD_MODE_STATICIP = {'e', 't', 'h', '0'};  // for update

    // IPCam_Utility_PC
    private static final byte[] SD_SETTING_HEAD = {'P', 'C', 'E', 'd', 'i', 't'};  //"PCEdit";
    public static final int SD_SETTING_HEAD_SIZE = 6;
    // -- IPCam_Setting_IP
    private static final int SD_MODE_HEAD_SIZE = 1;
    private static final int SD_MODE_SIZE = 4;
    public static final int SD_IP_SIZE = 4;
    public static final int SD_MAC_SIZE = 6;
    public static final int SD_MASK_SIZE = 4;
    // -- IPCam_Setting_GW
    private static final int SD_GW_HEAD_SIZE = 3;
    private static final byte[] SD_GW_HEAD = {SD_HEAD_UPDATE, 'g', 'w'};     // for update
    public static final int SD_GW_SIZE = 4;
    // -- IPCam_Setting_DNS
    private static final int SD_DNS_HEAD_SIZE = 1;
    public static final int SD_DNS_SIZE = 4;           // dns1, dns2
    // -- IPCam_Setting_Name
    private static final int SD_NAME_HEAD_SIZE = 1;
    public static final int SD_MODEL_NAME_SIZE = 32;
    public static final int SD_DEVICE_NAME_SIZE = 32;
    public static final int SD_FW_VERSION_SIZE = 8;    // update 時使用 8byte!
    // -- IPCam_Setting_Account
    public static final int SD_USER_NAME_SIZE = 33;
    public static final int SD_PASSWORD_SIZE = 33;
    // -- UPDATE TOTAL SIZE
    private static final int SD_SETTING_TOTAL_SIZE = SD_SETTING_HEAD_SIZE +
            SD_MODE_HEAD_SIZE + SD_MODE_SIZE + SD_IP_SIZE + SD_MAC_SIZE + SD_MASK_SIZE + SD_GW_HEAD_SIZE + SD_GW_SIZE +
            SD_DNS_HEAD_SIZE + SD_DNS_SIZE + SD_DNS_SIZE +
            SD_NAME_HEAD_SIZE + SD_MODEL_NAME_SIZE + SD_DEVICE_NAME_SIZE + SD_FW_VERSION_SIZE +
            SD_USER_NAME_SIZE + SD_PASSWORD_SIZE;

    // 以下為 Search 時使用到的，因 IPCam_Setting_Name - SD_FW_VERSION_SIZE 不同
    //IPCam_Utility_Cam
    private static final int SD_SETARCH_HEAD_SIZE = 5;
    //private static final byte SD_SETARCH_HEAD[] = {'I', 'P', 'C', 'a', 'm'};  //"IPCam";
    // ----IPCam_Setting_Name_2
    private static final int SD_FW_VERSION_SIZE_2 = 32;    // Search 時使用 32byte!
    // -- IPCam_Setting_Account
    private static final int SD_HTTP_PORT_SIZE = 2;      // UINT16
    private static final int SD_IPV6_ENABLE_SIZE = 2;    // UINT16
    private static final int SD_IPV6_ADDR_SIZE = 46;     // CHAR[46]
    // -- UPDATE TOTAL SIZE
    public static final int SD_SEARCH_TOTAL_SIZE = SD_SETARCH_HEAD_SIZE +
            SD_MODE_HEAD_SIZE + SD_MODE_SIZE + SD_IP_SIZE + SD_MAC_SIZE + SD_MASK_SIZE + SD_GW_HEAD_SIZE + SD_GW_SIZE +
            SD_DNS_HEAD_SIZE + SD_DNS_SIZE + SD_DNS_SIZE +
            SD_NAME_HEAD_SIZE + SD_MODEL_NAME_SIZE + SD_DEVICE_NAME_SIZE + SD_FW_VERSION_SIZE_2 +
            SD_USER_NAME_SIZE + SD_PASSWORD_SIZE +
            SD_HTTP_PORT_SIZE + SD_IPV6_ENABLE_SIZE + SD_IPV6_ADDR_SIZE;

    private boolean setDataOK = false;
    private final byte[] mode = new byte[SD_MODE_SIZE];
    private final byte[] ip = new byte[SD_IP_SIZE];
    private final byte[] mac = new byte[SD_MAC_SIZE];
    private final byte[] mask = new byte[SD_MASK_SIZE];
    private final byte[] gw = new byte[SD_GW_SIZE];
    private final byte[] dns1 = new byte[SD_DNS_SIZE];
    private final byte[] dns2 = new byte[SD_DNS_SIZE];
    private final byte[] modelName = new byte[SD_MODEL_NAME_SIZE];
    private final byte[] deviceName = new byte[SD_DEVICE_NAME_SIZE];
    //private byte[] updateFwVersion = new byte[SD_FW_VERSION_SIZE];
    private final byte[] searchFwVersion = new byte[SD_FW_VERSION_SIZE_2];
    private final byte[] userName = new byte[SD_USER_NAME_SIZE];
    private final byte[] password = new byte[SD_PASSWORD_SIZE];
    private final byte[] httpPort = new byte[SD_HTTP_PORT_SIZE];
    private final byte[] ipv6Enable = new byte[SD_IPV6_ENABLE_SIZE];
    private final byte[] ipv6Addr = new byte[SD_IPV6_ADDR_SIZE];

    public SearchData() {
        super();
        setDataOK = false;
    }
    public SearchData(byte[] cameraData) {
        super();
        setDataOK = setCameraData(cameraData);
    }

    public boolean setCameraData(byte[] cameraData) {
        //Log.v("AVDebug", "SearchData:setCameraData  cameraData.length=" + cameraData.length);
        //Log.v("AVDebug", "   = " + SD_SEARCH_TOTAL_SIZE);
        if (cameraData.length < SD_SEARCH_TOTAL_SIZE) {
            Log.v("AVDebug", "  error:  cameraData.length < " + SD_SEARCH_TOTAL_SIZE);
            return false;
        }

        int copyIndex = 0;
        byte[] tHeader = new byte[SD_SETARCH_HEAD_SIZE];
        System.arraycopy(cameraData, copyIndex, tHeader, 0, SD_SETARCH_HEAD_SIZE);
        String sHeader = new String(tHeader);
        //Log.v("AVDebug", "sHeader: " + sHeader);
        if (sHeader.equals("IPCam")) {
            Log.v("AVDebug", "is IPCam ");

            copyIndex = copyIndex + SD_SETARCH_HEAD_SIZE + SD_MODE_HEAD_SIZE;
            System.arraycopy(cameraData, copyIndex, mode, 0, SD_MODE_SIZE);
            copyIndex += SD_MODE_SIZE;
            System.arraycopy(cameraData, copyIndex, ip, 0, SD_IP_SIZE);
            copyIndex += SD_IP_SIZE;
            System.arraycopy(cameraData, copyIndex, mac, 0, SD_MAC_SIZE);
            copyIndex += SD_MAC_SIZE;
            System.arraycopy(cameraData, copyIndex, mask, 0, SD_MASK_SIZE);

            copyIndex = copyIndex + SD_MASK_SIZE + SD_GW_HEAD_SIZE;
            System.arraycopy(cameraData, copyIndex, gw, 0, SD_GW_SIZE);
            copyIndex = copyIndex + SD_GW_SIZE + SD_DNS_HEAD_SIZE;
            System.arraycopy(cameraData, copyIndex, dns1, 0, SD_DNS_SIZE);
            copyIndex += SD_DNS_SIZE;
            System.arraycopy(cameraData, copyIndex, dns2, 0, SD_DNS_SIZE);

            copyIndex = copyIndex + SD_DNS_SIZE + SD_NAME_HEAD_SIZE;
            System.arraycopy(cameraData, copyIndex, modelName, 0, SD_MODEL_NAME_SIZE);
            copyIndex += SD_MODEL_NAME_SIZE;
            System.arraycopy(cameraData, copyIndex, deviceName, 0, SD_DEVICE_NAME_SIZE);
            copyIndex += SD_DEVICE_NAME_SIZE;
            System.arraycopy(cameraData, copyIndex, searchFwVersion, 0, SD_FW_VERSION_SIZE_2);

            copyIndex += SD_FW_VERSION_SIZE_2;
            System.arraycopy(cameraData, copyIndex, userName, 0, SD_USER_NAME_SIZE);
            copyIndex += SD_USER_NAME_SIZE;
            System.arraycopy(cameraData, copyIndex, password, 0, SD_PASSWORD_SIZE);

            copyIndex += SD_PASSWORD_SIZE;
            System.arraycopy(cameraData, copyIndex, httpPort, 0, SD_HTTP_PORT_SIZE);
            copyIndex += SD_HTTP_PORT_SIZE;
            System.arraycopy(cameraData, copyIndex, ipv6Enable, 0, SD_IPV6_ENABLE_SIZE);
            copyIndex += SD_IPV6_ENABLE_SIZE;
            System.arraycopy(cameraData, copyIndex, ipv6Addr, 0, SD_IPV6_ADDR_SIZE);

            return true;
        }

        return false;
    }

    //Interface
    public int getMode() {
        String dhcpString = new String(SD_MODE_DHCP);
        String modeString = new String(mode);

        if (dhcpString.equals(modeString)) {
            return 0;   // dhcp
        } else {
            return 1;   // eth0
        }
    }
    public String getIpString() {
        String sIp = ( ip[0] & 0xFF ) + "." + ( ip[1] & 0xFF ) + "." +
                ( ip[2] & 0xFF ) + "." + ( ip[3] & 0xFF );
        return sIp;
    }
    public int getHttpPort() {
        return ( httpPort[0] + (httpPort[1]*256) );
    }
    public String getMacString() {
        StringBuffer hexStringBuffer = new StringBuffer();
        for (int i = 0; i < 6; i++) {
            hexStringBuffer.append(byteToHex(mac[i]));
            if ( i<5) {
                hexStringBuffer.append(":");
            }
        }

        return hexStringBuffer.toString();
    }
    public String getMaskString() {
        String sMask = ( mask[0] & 0xFF ) + "." + ( mask[1] & 0xFF ) + "." +
                ( mask[2] & 0xFF ) + "." + ( mask[3] & 0xFF );
        return sMask;
    }
    public String getGwString() {
        String sGw = ( gw[0] & 0xFF ) + "." + ( gw[1] & 0xFF ) + "." +
                ( gw[2] & 0xFF ) + "." + ( gw[3] & 0xFF );
        return sGw;
    }
    public String getDns1String() {
        String sDns = ( dns1[0] & 0xFF ) + "." + ( dns1[1] & 0xFF ) + "." +
                ( dns1[2] & 0xFF ) + "." + ( dns1[3] & 0xFF );
        return sDns;
    }
    public String getModelNameString() {
        return new String(modelName);
    }
    public String getDeviceNameString() {
        return new String(deviceName);
    }
    public String getFwVersionString() {
        return new String(searchFwVersion);
    }

    public byte[] FillSettingPacket(boolean dhcp, byte[] setIp, byte[] setMask,
                                    byte[] setGw, byte[] setDns1,
                                    byte[] setModelName, byte[] setDeviceName, //byte[] setFwVersion,
                                    byte[] setUserName, byte[] setPassword) {

        if (setDataOK) {
            Log.v("AVDebug", "total size = " + SD_SETTING_TOTAL_SIZE);
            byte[] setingData = new byte[SD_SETTING_TOTAL_SIZE];
            // pcEditSignature
            Log.v("AVDebug", "----- PC_SIGNATURE: " + 0);
            int copyIndex = 0;
            System.arraycopy(SD_SETTING_HEAD, 0, setingData, copyIndex, SD_SETTING_HEAD_SIZE);

            // IPCam_Setting_IP
            copyIndex += SD_SETTING_HEAD_SIZE;
            setingData[copyIndex] = SD_HEAD_UPDATE;
            copyIndex += SD_MODE_HEAD_SIZE;
            if (dhcp) {
                Log.v("AVDebug", "----- NETWORK_MODE_DHCP: " + copyIndex);
                System.arraycopy(SD_MODE_DHCP, 0, setingData, copyIndex, SD_MODE_SIZE);
            } else {
                Log.v("AVDebug", "----- NETWORK_MODE_DHCP: " + copyIndex);
                System.arraycopy(SD_MODE_STATICIP, 0, setingData, copyIndex, SD_MODE_SIZE);
            }
            copyIndex += SD_MODE_SIZE;
            Log.v("AVDebug", "----- IP: " + copyIndex);
            System.arraycopy(setIp, 0, setingData, copyIndex, SD_IP_SIZE);
            copyIndex += SD_IP_SIZE;
            System.arraycopy(mac, 0, setingData, copyIndex, SD_MAC_SIZE);
            copyIndex += SD_MAC_SIZE;
            System.arraycopy(setMask, 0, setingData, copyIndex, SD_MASK_SIZE);

            // IPCam_Setting_GW
            copyIndex += SD_MASK_SIZE;
            Log.v("AVDebug", ">>>> IPCam_Setting_GW: = " + copyIndex);
            System.arraycopy(SD_GW_HEAD, 0, setingData, copyIndex, SD_GW_HEAD_SIZE);
            copyIndex += SD_GW_HEAD_SIZE;
            System.arraycopy(setGw, 0, setingData, copyIndex, SD_GW_SIZE);

            // IPCam_Setting_DNS
            copyIndex += SD_GW_SIZE;
            Log.v("AVDebug", ">>>> IPCam_Setting_DNS: = " + copyIndex);
            setingData[copyIndex] = SD_HEAD_UPDATE;
            copyIndex += SD_DNS_HEAD_SIZE;
            System.arraycopy(setDns1, 0, setingData, copyIndex, SD_DNS_SIZE);

            // IPCam_Setting_Name
            copyIndex = copyIndex + SD_DNS_SIZE + SD_DNS_SIZE;  // dns1 + dns2
            Log.v("AVDebug", ">>>> IPCam_Setting_Name: = " + copyIndex);
            setingData[copyIndex] = SD_HEAD_UPDATE;
            copyIndex += SD_NAME_HEAD_SIZE;
            System.arraycopy(setModelName, 0, setingData, copyIndex, SD_MODEL_NAME_SIZE);
            copyIndex += SD_MODEL_NAME_SIZE;
            Log.v("AVDebug", ">>>> setDeviceName: = " + copyIndex);
            System.arraycopy(setDeviceName, 0, setingData, copyIndex, SD_DEVICE_NAME_SIZE);
            copyIndex += SD_DEVICE_NAME_SIZE;
            Log.v("AVDebug", ">>>> setFwVersion: = " + copyIndex);

            // IPCam_Setting_Account
            copyIndex += SD_FW_VERSION_SIZE;
            Log.v("AVDebug", ">>>> IPCam_Setting_Account: = " + copyIndex);
            System.arraycopy(setUserName, 0, setingData, copyIndex, SD_USER_NAME_SIZE);
            copyIndex += SD_USER_NAME_SIZE;
            Log.v("AVDebug", ">>>> setPassword: = " + copyIndex);
            Log.v("AVDebug", "copyIndex = " + copyIndex + " >> " + SD_PASSWORD_SIZE);
            System.arraycopy(setPassword, 0, setingData, copyIndex, SD_PASSWORD_SIZE);

            /*/ DEBUG: dump data
            StringBuffer hexStringBuffer = new StringBuffer();
            for (int i = 0; i < 180; i++) {
                hexStringBuffer.append(byteToHex(setingData[i]));
                hexStringBuffer.append(" ");
            }
            Log.v("AVDebug", "data: " + hexStringBuffer.toString());
             */

            return Base64.encode(setingData, Base64.NO_WRAP);
        } else {
            return null;
        }
    }

    // Tools
    private String byteToHex(byte num) {
        char[] hexDigits = new char[2];
        hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
        hexDigits[1] = Character.forDigit((num & 0xF), 16);
        return new String(hexDigits);
    }

    public byte[] IPStringToByte(String sIp) {
        byte[] byteIp = new byte[SD_IP_SIZE];
        String[] sIpToken = sIp.split("\\.");       // TODO 要判斷格式是否正確
        for (int i=0; i<SD_IP_SIZE; i++) {
            byteIp[i] = (byte)Integer.parseInt(sIpToken[i]);
        }

        return byteIp;
    }

}