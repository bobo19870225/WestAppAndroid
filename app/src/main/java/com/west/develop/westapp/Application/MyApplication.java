package com.west.develop.westapp.Application;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.ConnectStatus;
import com.west.develop.westapp.Bean.AppBean.DocumentVersion;
import com.west.develop.westapp.Tools.CrashHandler;
import com.west.develop.westapp.Tools.Utils.LanguageUtil;
import com.west.develop.westapp.Tools.MDBHelper;

import java.util.Locale;

/**
 * Created by Develop14 on 2017/5/27.
 */
public class MyApplication extends Application {
    public static final String ACTION_APP_FOREGROUND = "com.west.develop.westapp.Action_Foreground";
    public static final String ACTION_APP_BACKGROUND = "com.west.develop.westapp.Action_Background";
    public static final String ACTION_FWVERSION_REFRESH = "com.west.develop.westapp.ACTION_FWVERSION";
    public static final String ACTION_DEVICE_REFRESH = "com.west.develop.westapp.ACTION_DEVICE_REFRESH";


    private boolean isBackGround = false;

    private DocumentVersion NewFWVersion = null;
    private DocumentVersion CurrentFWVersion = null;

    private DocumentVersion NewAPPVersion = null;

    @Override
    public void onCreate() {
        super.onCreate();
        MDBHelper.getInstance(this).deleteTb();
        CrashHandler.getInstance().init(this);

        if (Config.getInstance(this).getLanguage() == Config.LANGUAGE_EN){
            Locale.setDefault(Locale.ENGLISH);
            LanguageUtil.setAppLanguage(this, Locale.ENGLISH);
        } else if (Config.getInstance(this).getLanguage() == Config.LANGUAGE_CH) {
            Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
            LanguageUtil.setAppLanguage(this, Locale.SIMPLIFIED_CHINESE);
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {}
            @Override
            public void onActivityStarted(Activity activity) {}
            @Override
            public void onActivityResumed(Activity activity) {
                if (isBackGround) {
                    isBackGround = false;
                    Intent intent = new Intent(ACTION_APP_FOREGROUND);
                    sendBroadcast(intent);
                }
            }
            @Override
            public void onActivityPaused(Activity activity) {}
            @Override
            public void onActivityStopped(Activity activity) {}
            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {}
            @Override
            public void onActivityDestroyed(Activity activity) {}
        });

    }

    @Override
    public void onTerminate() {
        MDBHelper.getInstance(this).close();
        if (ConnectStatus.getInstance(this).getBTPort() != null){
            ConnectStatus.getInstance(this).getBTPort().destroy();
        }
        super.onTerminate();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        if (level == TRIM_MEMORY_UI_HIDDEN) {
            isBackGround = true;
            Intent intent = new Intent(ACTION_APP_BACKGROUND);
            sendBroadcast(intent);
        }
    }

    // 横竖屏切换，键盘等
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Config.getInstance(this).getLanguage() == Config.LANGUAGE_EN){
            newConfig.setLocale(Locale.ENGLISH);
        } else if (Config.getInstance(this).getLanguage() == Config.LANGUAGE_CH) {
            newConfig.setLocale(Locale.SIMPLIFIED_CHINESE);
        }
        this.createConfigurationContext(newConfig);
        getResources().updateConfiguration(newConfig,getResources().getDisplayMetrics());

    }


    public void setNewFWVersion(DocumentVersion version){
        this.NewFWVersion = version;
    }

    public void setCurrentFWVersion(DocumentVersion version){
        this.CurrentFWVersion = version;
        if(this.CurrentFWVersion != null){
            Intent intent = new Intent(ACTION_FWVERSION_REFRESH);
            sendBroadcast(intent);
        }
    }

    public DocumentVersion getCurrentFWVersion(){
        return this.CurrentFWVersion;
    }

    public DocumentVersion getNewAPPVersion() {
        return NewAPPVersion;
    }

    public void setNewAPPVersion(DocumentVersion newAPPVersion) {
        NewAPPVersion = newAPPVersion;
    }

    /**
     * 是否可以升级
     * @return
     */
    public boolean updateFWValid(){
        if(CurrentFWVersion == null || NewFWVersion == null){
            return false;
        }

        String newMain = NewFWVersion.getMain();
        String newSlave = NewFWVersion.getSlave();
        String currentMain = CurrentFWVersion.getMain();
        String currentSlave = CurrentFWVersion.getSlave();

        String verNew = newMain + "." + newSlave;
        String verCurrent = currentMain + "." + currentSlave;

        try{
            double versionNew = Double.parseDouble(verNew);
            double versionCurrent = Double.parseDouble(verCurrent);

            if(versionNew > versionCurrent){
                return true;
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 固件程序升级完成
     */
    public void updateFWSuccess(){
        CurrentFWVersion = NewFWVersion;
    }
}
