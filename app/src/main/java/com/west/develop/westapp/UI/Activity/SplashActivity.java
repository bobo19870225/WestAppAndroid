package com.west.develop.westapp.UI.Activity;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.constant.RequestCodeConstant;
import com.west.develop.westapp.UI.Activity.Setting.StatementActivity;
import com.west.develop.westapp.rxpermissions.Permission;
import com.west.develop.westapp.rxpermissions.RxPermissions;

import java.util.ArrayList;
import java.util.Calendar;

import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by 林泽烜 on 2017/9/16.
 */
public class SplashActivity extends Activity {
    private static final int MSG_START_MAIN = 1;
    private static final int MSG_ASSETS_COPY = 2;
    private static final int MSG_PERMISSION_DENIED = 3;
    private static final int MSG_IMAGE_COPY = 4;
    private static final int MSG_GUIDE_MANUAL = 6;
    private static final int MSG_START_STATEMENT = 7;

    private int denyCount = 0;

    private boolean permissionSuccess = true;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_MAIN:
                    if(Config.getInstance(SplashActivity.this).isConfigured() && !Config.getInstance(SplashActivity.this).isAgreed()){
                        sendEmptyMessageDelayed(MSG_START_STATEMENT,2000);
                    }
                    else {
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                    break;
                case MSG_START_STATEMENT:
                    Intent intentState = new Intent(SplashActivity.this, StatementActivity.class);
                    intentState.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intentState.putExtra(StatementActivity.kAgreed,false);
                    startActivity(intentState);
                    break;
                case MSG_GUIDE_MANUAL:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try{
                                String path = Environment.getExternalStorageDirectory().getPath() + "/" + getPackageName();
                                FileUtil.deleteApp(path,SplashActivity.this);
                                FileUtil.copyDefaulDocument(SplashActivity.this); //将用户手册和指南复制到本地文件中
                                Config.getInstance(SplashActivity.this).setFirstRun(false);
                                sendEmptyMessageDelayed(MSG_START_MAIN,2000);
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }
                    }).start();
                    break;
                case MSG_PERMISSION_DENIED:
                    System.exit(0);
                    break;

                default:
                    break;
            }
        }
    };

    private TextView versionTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_splash);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        versionTV = (TextView) findViewById(R.id.versionTV);
        PackageManager packageManager = getPackageManager();
        PackageInfo info = null;
        try {
            info = packageManager.getPackageInfo(getPackageName(), 0);
            versionTV.setText(info.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        //Config.getInstance(this).setBTList(null);
        //Config.getInstance(this).setBondDevice(null);
       /* Config.getInstance(this).setSigned(false);

        Config.getInstance(this).addBTName("Oerl");
        Config.getInstance(this).addBTName("Lenovo TAB 2 A10-30");
*/
       /* DeviceBean deviceBean = new DeviceBean();
        deviceBean.setDeviceSN(Config.getInstance(this).getBondDevice().getDeviceSN());
        deviceBean.setTargetID(Config.getInstance(this).getBondDevice().getTargetID());
        Config.getInstance(this).setDevice(deviceBean);
*/

        checkPermissions();

    }

    public void checkPermissions(){

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.setLogging(true);

        final ArrayList<String> permissions = new ArrayList<>();

        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        rxPermissions.requestEach(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(new Action1<Permission>() {
                               @Override
                               public void call(Permission permission) {
                                   Log.i("RequestPermission", "Permission result " + permission);
                                   if (permission.granted) {
                                       permissions.remove(permission.name);
                                   }
                               }
                           },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable t) {
                                Log.e("RequestPermissions", "onError", t);
                            }
                        },
                        new Action0() {
                            @Override
                            public void call() {
                                Log.e("RequestPermissions", "OnComplete-" + permissions.size());
                                if(permissions.size() == 0) {
                                    if (checkWindowPermission()) {
                                        startAPP();
                                    }
                                }
                                else{
                                    permissionSuccess = false;
                                    if(checkWindowPermission()){
                                        permissionDenied();
                                    }
                                }
                            }
                        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodeConstant.CODE_SYSTEM_ALERT_WINDOW) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    permissionDenied();
                }
                else{
                    if(!permissionSuccess) {
                        permissionDenied();
                    }
                    else{
                        startAPP();
                    }
                }
            }
            else{
                if(!permissionSuccess) {
                    permissionDenied();
                }
                else{
                    startAPP();
                }
            }
        }

        if(requestCode == RequestCodeConstant.CODE_APP_SETTING){
            checkPermissions();
        }

    }

    private void permissionDenied(){
        denyCount++;
        if(denyCount < 2){
            TipDialog dialog = new TipDialog.Builder(SplashActivity.this)
                    .setTitle(getString(R.string.tip_title))
                    .setMessage(getString(R.string.Storage_Permission_Denied_Request))
                    .setPositiveClickListener(getResources().getString(R.string.tip_yes),new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            //checkPermissions();
                            Intent intent = new Intent();
                            //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                            intent.setData(Uri.fromParts("package", getPackageName(), null));
                            startActivityForResult(intent, RequestCodeConstant.CODE_APP_SETTING);
                            dialogInterface.dismiss();
                        }
                    })
                    .setNegativeClickListener(getString(R.string.exit),new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            dialogInterface.dismiss();
                            mHandler.sendEmptyMessage(MSG_PERMISSION_DENIED);
                            //System.exit(0);
                        }
                    }).build();
            dialog.show();
        }
        else{
            Toast.makeText(SplashActivity.this,getString(R.string.Storage_Permission_Denied_Exit),Toast.LENGTH_SHORT).show();
            mHandler.sendEmptyMessageDelayed(MSG_PERMISSION_DENIED,1500);
        }
    }

    /**
     * 校验浮动窗口权限
     */
    public boolean checkWindowPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (! Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + this.getPackageName()));
                (this).startActivityForResult(intent, RequestCodeConstant.CODE_SYSTEM_ALERT_WINDOW);

                return false;
            }
            else {
                return true;
            }
        }
        else{
            return true;
        }

    }


    //启动app之前的操作复制文件和图片，只是在安装app的时候调用
    private void startAPP(){
        if(Config.getInstance(this).isFirstRun()) {
            Config.getInstance(this).setLastDate(Calendar.getInstance());
            mHandler.sendEmptyMessage(MSG_GUIDE_MANUAL);
        }
        else{
            mHandler.sendEmptyMessageDelayed(MSG_START_MAIN,3000);
        }
    }
}
