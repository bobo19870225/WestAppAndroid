package com.west.develop.westapp.Bean.AppBean;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Develop0 on 2017/12/7.
 */

public class DeviceBean {
    public static final int MODE_RELEASE = 1;
    public static final int MODE_DEBUG = 2;

    String deviceSN;

    String targetID;

    int releaseMode = MODE_RELEASE;

    String signTime;

    String userName;


    @Expose
    @SerializedName("utel")
    String userPhone;


    @Expose
    @SerializedName("uMail")
    String userMail;


    @Expose
    @SerializedName("uAddress")
    String userAddr;

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public String getTargetID() {
        return targetID;
    }

    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }

    public int getDeviceMode() {
        return releaseMode;
    }

    public void setDeviceMode(int deviceMode) {
        if(deviceMode == MODE_RELEASE || deviceMode == MODE_DEBUG){
            this.releaseMode = deviceMode;
        }

    }

    public String getTime() {
        return signTime;
    }

    public void setTime(String time) {
        this.signTime = time;
    }

    public boolean isVariable(){
        return this != null && deviceSN != null && deviceSN.length() > 0;
    }

    public String getUserAddr() {
        if(userAddr.indexOf("[") == 0){
            userAddr = userAddr.substring(userAddr.indexOf("]") + 1);
        }
        return userAddr;
    }

    public void setUserAddr(String userAddr) {
        this.userAddr = userAddr;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserMail() {
        return userMail;
    }

    public void setUserMail(String userMail) {
        this.userMail = userMail;
    }
}
