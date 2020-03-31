package com.west.develop.westapp.Application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.west.develop.westapp.Tools.Utils.WifiUtil;
import com.west.develop.westapp.Download.ProgramDownload.DownloadManager;

/**
 * Created by Develop12 on 2017/9/26.
 */

public class AppReceiver extends BroadcastReceiver {

    public static final String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
    public static final String ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
    public static final String ACTION_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";


    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();
        Log.e("AppReceiver",action);
        switch (action){
            case ACTION_SCREEN_OFF:
                Log.e("AppReceiver","SCREEN_OFF");
                break;
            case ACTION_SCREEN_ON:
                Log.e("AppReceiver","SCREEN_ON");
                break;
            case ACTION_CONNECTIVITY_CHANGE: //网络切换
                if (WifiUtil.isSupportNetwork(context)){
                    //if (WifiUtil.isSupportWifi(context)){
                        //Toast.makeText(context,context.getString(R.string.Network_Connected),Toast.LENGTH_LONG).show();
                        DownloadManager.getInstance(context).initAll();//重新创建任务

                   // }
                }else {
                    //Toast.makeText(context,context.getString(R.string.Network_Disconnected),Toast.LENGTH_LONG).show();
                    if (DownloadManager.getInstance(context).getmDownloadThreads() != null){
                        DownloadManager.getInstance(context).pauseAllDownload();//暂停线程
                    }

                }
                break;
            default:
                break;
        }
    }


}
