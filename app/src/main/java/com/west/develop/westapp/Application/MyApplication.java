package com.west.develop.westapp.Application;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.west.develop.westapp.Bean.AppBean.DocumentVersion;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.ConnectStatus;
import com.west.develop.westapp.Tools.CrashHandler;
import com.west.develop.westapp.Tools.MDBHelper;
import com.west.develop.westapp.Tools.Utils.LanguageUtil;

import java.util.Locale;

/**
 * 1.删除汽车更新数据库表
 * 2.初始化自定义异常捕获
 * 3.初始化语言（英文或中文）
 * 4.监听应用运行在前台？后台
 * 5.退出程序时关闭数据库链接
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
        if (Config.getInstance(this).getLanguage() == Config.LANGUAGE_EN) {
            Locale.setDefault(Locale.ENGLISH);
            LanguageUtil.setAppLanguage(this, Locale.ENGLISH);
        } else if (Config.getInstance(this).getLanguage() == Config.LANGUAGE_CH) {
            Locale.setDefault(Locale.SIMPLIFIED_CHINESE);
            LanguageUtil.setAppLanguage(this, Locale.SIMPLIFIED_CHINESE);
        }

        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(@NonNull Activity activity, Bundle savedInstanceState) {
            }

            @Override
            public void onActivityStarted(@NonNull Activity activity) {
            }

            @Override
            public void onActivityResumed(@NonNull Activity activity) {
                if (isBackGround) {
                    isBackGround = false;
                    Intent intent = new Intent(ACTION_APP_FOREGROUND);
                    sendBroadcast(intent);
                }
            }

            @Override
            public void onActivityPaused(@NonNull Activity activity) {
            }

            @Override
            public void onActivityStopped(@NonNull Activity activity) {
            }

            @Override
            public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {
            }

            @Override
            public void onActivityDestroyed(@NonNull Activity activity) {
            }
        });

    }

    @Override
    public void onTerminate() {
        MDBHelper.getInstance(this).close();
        if (ConnectStatus.getInstance(this).getBTPort() != null) {
            //释放蓝牙
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (Config.getInstance(this).getLanguage() == Config.LANGUAGE_EN) {
            newConfig.setLocale(Locale.ENGLISH);
        } else if (Config.getInstance(this).getLanguage() == Config.LANGUAGE_CH) {
            newConfig.setLocale(Locale.SIMPLIFIED_CHINESE);
        }
        this.createConfigurationContext(newConfig);
        getResources().updateConfiguration(newConfig, getResources().getDisplayMetrics());

    }


    public void setNewFWVersion(DocumentVersion version) {
        this.NewFWVersion = version;
    }

    public void setCurrentFWVersion(DocumentVersion version) {
        this.CurrentFWVersion = version;
        if (this.CurrentFWVersion != null) {
            Intent intent = new Intent(ACTION_FWVERSION_REFRESH);
            sendBroadcast(intent);
        }
    }

    public DocumentVersion getCurrentFWVersion() {
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
     */
    public boolean updateFWValid() {
        if (CurrentFWVersion == null || NewFWVersion == null) {
            return false;
        }

        String newMain = NewFWVersion.getMain();
        String newSlave = NewFWVersion.getSlave();
        String currentMain = CurrentFWVersion.getMain();
        String currentSlave = CurrentFWVersion.getSlave();

        String verNew = newMain + "." + newSlave;
        String verCurrent = currentMain + "." + currentSlave;

        try {
            double versionNew = Double.parseDouble(verNew);
            double versionCurrent = Double.parseDouble(verCurrent);

            if (versionNew > versionCurrent) {
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    /**
     * 固件程序升级完成
     */
    public void updateFWSuccess() {
        CurrentFWVersion = NewFWVersion;
    }
}
