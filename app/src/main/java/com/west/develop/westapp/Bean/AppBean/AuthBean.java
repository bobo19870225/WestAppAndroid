package com.west.develop.westapp.Bean.AppBean;

/**
 * Created by Develop0 on 2018/1/26.
 */

public class AuthBean {
    public static final int AUTH_YES = 1;
    public static final int AUTH_NO = 0;

    private String deviceSN;

    private String programCode;

    private int permission = AUTH_NO;

    public String getDeviceSN() {
        return deviceSN;
    }

    public void setDeviceSN(String deviceSN) {
        this.deviceSN = deviceSN;
    }

    public String getProgramCode() {
        return programCode;
    }

    public void setProgramCode(String programCode) {
        this.programCode = programCode;
    }

    public boolean isPermission() {
        return permission == AUTH_YES;
    }

    public void setPermission(boolean permission) {
        if(permission){
            this.permission = AUTH_YES;
        }
        else{
            this.permission = AUTH_NO;
        }
    }

    public void setPermission(int permission) {
        if(permission == AUTH_YES || permission == AUTH_NO){
            permission = permission;
        }
    }
}
