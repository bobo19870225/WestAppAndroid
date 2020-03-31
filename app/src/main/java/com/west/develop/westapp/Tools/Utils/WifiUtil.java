package com.west.develop.westapp.Tools.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

/**
 * Created by Develop12 on 2017/9/13.
 */
public class WifiUtil {

    public static boolean isSupportWifi(Context context) {
        if (context != null) {
            WifiManager manager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
            boolean isEnable = manager.isWifiEnabled();
            return manager.isWifiEnabled();

        }
        return false;
    }

    public static boolean isSupportNetwork(Context context){
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return true;
            }
        }
        return false;
    }

}
