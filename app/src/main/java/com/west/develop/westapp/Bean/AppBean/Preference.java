package com.west.develop.westapp.Bean.AppBean;

/**
 * Created by Develop0 on 2018/5/26.
 */

public class Preference {

    //未响应超时  单位：分钟
    private int Timeout_Active = 3;

    //备份文件接受超时  单位：秒钟
    private int Timeout_Backup = 10;

    public int getTimeoutActive() {
        return Timeout_Active;
    }

    public void setTimeoutActive(int timeoutActive) {
        Timeout_Active = timeoutActive;
    }

    public int getTimeoutBackup() {
        return Timeout_Backup;
    }

    public void setTimeoutBackup(int timeoutBackup) {
        Timeout_Backup = timeoutBackup;
    }
}
