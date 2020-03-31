package com.west.develop.westapp.Download;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.west.develop.westapp.CallBack.RequestCallBack;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Download.Threads.APKDownloadThread;
import com.west.develop.westapp.R;
import com.west.develop.westapp.UI.Activity.APPUpdate.AppUpdateActivity;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


/**
 * Created by Develop12 on 2018/1/27.
 */

public class AppUpdateManager {

    private Context mContext;
    private static AppUpdateManager instance;


    private  RemoteViews remoteViews;
    private  PendingIntent pendingIntent;
    private  NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private final int APPNOTIFICATION_ID = 2;
    private String time;

    public synchronized static AppUpdateManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AppUpdateManager.class) {
                instance = new AppUpdateManager();
                instance.mContext = context;
                instance.initNotification();
            }
        }
        instance.mContext = context;
        return instance;
    }

    private void initNotification() {
        Intent notifyIntent = new Intent(mContext, AppUpdateActivity.class);
        int requestCode2 = (int) SystemClock.uptimeMillis();
        pendingIntent = PendingIntent.getActivity(mContext, requestCode2,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.app_notifi);
        remoteViews.setImageViewResource(R.id.app_notifi_icon, R.mipmap.app_icon);
        remoteViews.setTextViewText(R.id.app_notifi_title, "APP升级下载");
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        time = formatter.format(new Date());
        remoteViews.setTextViewText(R.id.app_notifi_when, time);
        remoteViews.setProgressBar(R.id.app_notifi_progress,100,0,false);
        remoteViews.setTextViewText(R.id.app_notifi_progress_text,"0%");
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.mipmap.app_icon)
                .setContentTitle("APP升级下载")
                .setTicker("APP升级下载")
                .setContent(remoteViews)
                .setContentIntent(pendingIntent);
        mNotificationManager = (NotificationManager)mContext.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    String result;
    APKDownloadThread apk;
    public void downLoadApk() {
        // 开始下载
        if (apk == null || apk.isInterrupted()) {
            if(Config.getInstance(mContext).getBondDevice() == null){
                return;
            }
            apk = new APKDownloadThread(mContext, requestCallBack);
            apk.start();
            mNotificationManager.notify(APPNOTIFICATION_ID, mBuilder.build());
        }

    }

    RequestCallBack requestCallBack = new RequestCallBack() {
        @Override
        public void onResult(boolean success, File file) {
            if (success){
                if (!file.exists()){
                    return;
                }
                remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.app_notifi);
                remoteViews.setImageViewResource(R.id.app_notifi_icon,R.mipmap.app_icon);
                remoteViews.setTextViewText(R.id.app_notifi_title,"APP升级下载");
                remoteViews.setTextViewText(R.id.app_notifi_when,time);
                remoteViews.setProgressBar(R.id.app_notifi_progress,100,(int) (Float.parseFloat(result) * 100),false);
                remoteViews.setTextViewText(R.id.app_notifi_progress_text,"下载完成");
                mBuilder.setSmallIcon(R.mipmap.app_icon)
                        .setContentTitle("APP升级下载")
                        .setContent(remoteViews)
                        .setContentIntent(pendingIntent);
                mNotificationManager.notify(APPNOTIFICATION_ID, mBuilder.build());

                //下载好后就进行安装
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://"+file.toString()),"application/vnd.android.package-archive");
                mContext.startActivity(intent);

            }

        }

        //更新进度条
        @Override
        public void onLoading(long total, long current) {
            double dcurrent = current *1.0;
            double resultTemp = dcurrent / total;
            DecimalFormat df = new DecimalFormat("0.00");
            result = df.format(resultTemp);
            remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.app_notifi);
            remoteViews.setTextViewText(R.id.app_notifi_title,"APP升级下载");
            remoteViews.setImageViewResource(R.id.app_notifi_icon,R.mipmap.app_icon);
            remoteViews.setTextViewText(R.id.app_notifi_when,time);
            remoteViews.setProgressBar(R.id.app_notifi_progress,100,(int) (Float.parseFloat(result) * 100),false);
            remoteViews.setTextViewText(R.id.app_notifi_progress_text,(int) (Float.parseFloat(result) * 100) +"%");
            mBuilder.setSmallIcon(R.mipmap.app_icon)
                    .setContentTitle("APP升级下载")
                    .setContent(remoteViews)
                    .setContentIntent(pendingIntent);
            mNotificationManager.notify(APPNOTIFICATION_ID, mBuilder.build());

        }
    };

}
