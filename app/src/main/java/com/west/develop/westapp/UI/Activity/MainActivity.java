package com.west.develop.westapp.UI.Activity;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.west.develop.westapp.Application.MyApplication;
import com.west.develop.westapp.Bean.AppBean.DeviceBean;
import com.west.develop.westapp.Bean.AppBean.DocumentVersion;
import com.west.develop.westapp.Communicate.Service.UsbService;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.BondDialog;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.BackHandlerHelper;
import com.west.develop.westapp.Tools.MDBHelper;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.Utils.ReportUntil;
import com.west.develop.westapp.Tools.Utils.VolleyUtil;
import com.west.develop.westapp.Tools.Utils.WifiUtil;
import com.west.develop.westapp.Tools.constant.URLConstant;
import com.west.develop.westapp.UI.Fragment.Diagnosis.DiagnosisFragment;
import com.west.develop.westapp.UI.Fragment.Upgrade.UpgradeFragment;
import com.west.develop.westapp.UI.Fragment.home.HomeFragment;
import com.west.develop.westapp.UI.Fragment.report.DataFragment;
import com.west.develop.westapp.UI.Fragment.setting.SettingFragment;
import com.west.develop.westapp.UI.base.BaseActivity;
import com.west.develop.westapp.usb.UsbSerialPort;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.west.develop.westapp.Application.AppReceiver.ACTION_CONNECTIVITY_CHANGE;

public class MainActivity extends BaseActivity {
    private FragmentManager supportFragmentManager;
    private HomeFragment homeFragment;
    private DiagnosisFragment diagnosisFragment;
    private UpgradeFragment upgradeFragment;
    private SettingFragment settingFragment;
    private DataFragment reportFragment;

    public static final int HOME_ID = 0;
    public static final int DIAGNOSIS_ID = 1;
    public static final int UPGRADE_ID = 2;
    public static final int SETTING_ID = 3;
    public static final int REPORT_ID = 4;

    private TipDialog mDialog;

    @Override
    protected View getContentView() {
        showStatusBar();
        registerReceiver();
        return this.getLayoutInflater().inflate(R.layout.activity_main, null);

    }

    @Override
    protected void initView() {

       /* new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                Toast.makeText(MainActivity.this, "正在拷贝", Toast.LENGTH_SHORT).show();
                FileUtil.copyAssetsBinToDebugRoot(MainActivity.this);

                Toast.makeText(MainActivity.this, "拷贝完成", Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        }).start();*/
        Calendar nowCalendar = Calendar.getInstance();
        Calendar laterCalendar = Config.getInstance(MainActivity.this).getLastDate();
        //计算时差
        int timeDistance = (int) ((laterCalendar.getTimeInMillis() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24));

        /**
         * 当前时间和设置的最大时间间隔，两个时间段进行比较
         * n == 1说明 nowCalendar比laterCalendar早
         * n == 0说明 nowCalendar与laterCalendar相等
         * n== -1说明 nowCalendar比laterCalendar晚
         */
        int n = laterCalendar.compareTo(nowCalendar);
        int checkCount = Config.getInstance(MainActivity.this).getRegCount();
        if (WifiUtil.isSupportNetwork(MainActivity.this)) {
            queryBTList();
            checkDeviceMode();
            ReportUntil.autoPostReport(MainActivity.this);
        }
        else {
            //如果没有网络的时候，那么就开始记录次数
            if (timeDistance <= 0 || n == -1 || checkCount >= Config.getInstance(MainActivity.this).getSetRegCount()) {
                mDialog = new TipDialog.Builder(MainActivity.this)
                        .setTitle(getResources().getString(R.string.tip_title))
                        .setMessage(getResources().getString(R.string.check_device))
                        .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialogInterface, int index, String label) {
                                if (WifiUtil.isSupportNetwork(MainActivity.this)) {
                                    // Config.getInstance(MainActivity.this).setCheckDeviceCount(0);
                                    checkDeviceMode();
                                    ReportUntil.autoPostReport(MainActivity.this);
                                    // dialogInterface.dismiss();
                                } else {
                                    Toast.makeText(MainActivity.this, getString(R.string.tip_net_nonect), Toast.LENGTH_LONG).show();
                                }
                            }
                        })
                        .setNegativeClickListener(getString(R.string.exit), new TipDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialogInterface, int index, String label) {
                                dialogInterface.dismiss();
                                finish();
                                System.exit(0);
                            }
                        })
                        .requestSystemAlert(true)
                        .build();
                mDialog.show();
            } else {
                if (timeDistance < Config.TIP_CHECKTIME || checkCount >= (Config.getInstance(MainActivity.this).getSetRegCount() - Config.TIP_REGCOUNT)) {
                    String tipStr = getString(R.string.check_deviceTip1) + timeDistance +
                            getString(R.string.check_deviceTip2) + (Config.getInstance(MainActivity.this).getSetRegCount() - checkCount) +
                            getString(R.string.check_deviceTip3);
                    mDialog = new TipDialog.Builder(MainActivity.this)
                            .setTitle(getResources().getString(R.string.tip_title))
                            .setMessage(tipStr)
                            .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                                @Override
                                public void onClick(Dialog dialogInterface, int index, String label) {
                                    if (WifiUtil.isSupportNetwork(MainActivity.this)) {
                                        checkDeviceMode();
                                        ReportUntil.autoPostReport(MainActivity.this);
                                    }
                                    dialogInterface.dismiss();
                                }
                            })
                            .requestSystemAlert(true)
                            .build();
                    mDialog.show();
                }
            }
        }
    }


    /**
     * 查询蓝牙列表
     */
    private void queryBTList(){
        VolleyUtil.jsonRequest(URLConstant.urlBluetoothList, this, new VolleyUtil.IVolleyCallback() {
            @Override
            public void getResponse(JSONObject jsonObject) {
                try {
                    int code = jsonObject.getInt("code");

                    if(code == 0){
                        JSONObject data = jsonObject.getJSONObject("data");

                        Gson gson = new Gson();
                        ArrayList<String>  list = gson.fromJson(data.getJSONArray("list").toString(),ArrayList.class);

                        if(list != null){
                            for(int i = 0;i < list.size();i++){
                                Config.getInstance(MainActivity.this).addBTName(list.get(i));
                            }
                        }
                    }
                }
                catch (JSONException ex){
                    ex.printStackTrace();
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {

            }
        });
    }

    /**
     * 检测设备是否可用
     */
    private void checkDeviceMode() {
        if (Config.getInstance(MainActivity.this).getBondDevice() != null) {
            String url = URLConstant.urlCheckDeviceMode + "?" +
                    "deviceSN=" + Config.getInstance(MainActivity.this).getBondDevice().getDeviceSN() + "&" +
                    "targetID=" + Config.getAndroidID(MainActivity.this) + "&" +
                    "regCount=" + Config.getInstance(MainActivity.this).getRegCount();
            VolleyUtil.jsonRequest(url, MainActivity.this, callback);

        }
    }



    VolleyUtil.IVolleyCallback callback = new VolleyUtil.IVolleyCallback() {
        @Override
        public void getResponse(JSONObject jsonObject) {
            if (mDialog != null) {
                mDialog.dismiss();
            }
            Log.e("Volley checkDeviceMode", jsonObject.toString());
            if ("".equals(jsonObject) || jsonObject == null) {
                return;
            }
            try {
                int code = jsonObject.getInt("code");
                JSONObject data = jsonObject.getJSONObject("data");
                if (code == 0) {
                    Config.getInstance(MainActivity.this).checked = true;
                    Config.getInstance(MainActivity.this).setLastDate(Calendar.getInstance());
                    //Config.getInstance(MainActivity.this).setCheckDeviceCount(0);
                    JSONObject device = data.getJSONObject("Device");
                    int setRegCount = device.getInt("setRegCount");
                    int RegCount = device.getInt("regCount");

                    Config.getInstance(MainActivity.this).setSetRegCount(setRegCount);
                    Config.getInstance(MainActivity.this).setRegCount(RegCount);
                   // Config.RegCount = RegCount;

                    Gson gson = new Gson();
                    DocumentVersion FWVersion = gson.fromJson(data.getJSONObject("sysVersion").toString(),DocumentVersion.class);
                    ((MyApplication)getApplicationContext()).setNewFWVersion(FWVersion);

                    DocumentVersion APPVersion = gson.fromJson(data.getJSONObject("appVersion").toString(),DocumentVersion.class);
                    ((MyApplication)getApplicationContext()).setNewAPPVersion(APPVersion);
                    if(settingFragment != null){
                        settingFragment.refresh();
                    }

                    DeviceBean deviceBean = gson.fromJson(device.toString(),DeviceBean.class);
                    Config.getInstance(MainActivity.this).setBondDevice(deviceBean);

                    boolean configured = data.getBoolean("configured");
                    Config.getInstance(MainActivity.this).setConfigured(configured);

                    if (setRegCount < 0) {
                        //将设备解绑和注销
                        Config.getInstance(MainActivity.this).setBondDevice(null);
                        //Config.getInstance(MainActivity.this).setTryCount(0);

                        //删除文件
                        String root = Environment.getExternalStorageDirectory().getPath() + "/";
                        String packageName = MainActivity.this.getPackageName();
                        String path = root + packageName;
                        FileUtil.deleteApp(path, MainActivity.this);
                        FileUtil.deleteAppVideo(MainActivity.this);
                        //删除数据库
                        MDBHelper.getInstance(MainActivity.this).deleteAppDB();
                        initData();
                    }
                    else if(setRegCount <= RegCount){
                        //使用次数已用完
                        String tipStr = getResources().getString(R.string.regCount_1) + " "+ RegCount + " " +
                                getResources().getString(R.string.regCount_Over);
                        mDialog = new TipDialog.Builder(MainActivity.this)
                                .setTitle(getResources().getString(R.string.tip_title))
                                .setMessage(tipStr)
                                .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialogInterface, int index, String label) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .requestSystemAlert(true)
                                .build();
                        mDialog.show();
                    }
                    else if(setRegCount > RegCount && (setRegCount - RegCount) <= Config.TIP_REGCOUNT){
                        //使用次数到达提示数
                        String tipStr = getResources().getString(R.string.regCount_1) + " "+ RegCount + " " +
                                getResources().getString(R.string.regCount_2) + " " +
                                (setRegCount - RegCount) + " " +
                                getResources().getString(R.string.regCount_msg);
                        mDialog = new TipDialog.Builder(MainActivity.this)
                                .setTitle(getResources().getString(R.string.tip_title))
                                .setMessage(tipStr)
                                .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialogInterface, int index, String label) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .requestSystemAlert(true)
                                .build();
                        mDialog.show();

                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (mDialog != null) {
                mDialog.dismiss();
            }
        }
    };

    @Override
    protected void initData() {
        supportFragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = supportFragmentManager.beginTransaction();
        if (homeFragment == null) {
            homeFragment = HomeFragment.newInstance("", "");
            fragmentTransaction.add(R.id.main_fl, homeFragment);
        } else {
            fragmentTransaction.show(homeFragment);
        }
        fragmentTransaction.commitAllowingStateLoss();

        if (Config.getInstance(MainActivity.this).getBondDevice() == null) {
            TipDialog dialog = new TipDialog.Builder(MainActivity.this)
                    .setTitle(getResources().getString(R.string.device_bond_title))
                    .setMessage(getResources().getString(R.string.device_bond_message))
                    .setNegativeClickListener(getResources().getString(R.string.bond_cancle), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            dialogInterface.dismiss();
                            System.exit(0);

                        }
                    })
                    .setPositiveClickListener(getResources().getString(R.string.bond_sure), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            dialogInterface.dismiss();
                            BondDialog.newInstance(MainActivity.this)
                                    .setNegativeClickListener(getResources().getString(R.string.cancel), new BondDialog.OnClickListener() {
                                        @Override
                                        public void onClick(Dialog dialog) {
                                            dialog.dismiss();
                                            System.exit(0);
                                        }
                                    })
                                    .show();
                            if (UsbService.getInstance().getSerialPorts() != null) {
                                final Handler handler = new Handler();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (BondDialog.getInstance() != null && !BondDialog.getInstance().waitSign()) {
                                            //Log.e("signUsb",UsbService.getInstance().getSerialPorts().size() + "");
                                            if (UsbService.getInstance().getSerialPorts() == null || UsbService.getInstance().getSerialPorts().size() <= 0) {
                                                return;
                                            }
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    List<UsbSerialPort> mUsbPorts = UsbService.getInstance().getSerialPorts();
                                                    if (mUsbPorts != null && mUsbPorts.size() > 0) {
                                                        for (int i = 0; i < mUsbPorts.size(); i++) {
                                                            BondDialog.getInstance().bondWithPort(mUsbPorts.get(i));
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                for (int i = 0; i < UsbService.getInstance().getSerialPorts().size(); i++) {
                                                    BondDialog.getInstance().bondWithPort(UsbService.getInstance().getSerialPorts().get(i));
                                                }
                                            }
                                        });
                                    }
                                }).start();

                            }
                        }
                    })
                    .requestSystemAlert(true)
                    .build();
            dialog.show();
        }
    }


    @Override
    protected void initListener() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
        unregisterReceiver();
    }

    /**
     * 点击返回
     */
    @Override
    public void onBackPressed() {
        if (!BackHandlerHelper.handleBackPress(this)) {
            TipDialog dialog = new TipDialog.Builder(MainActivity.this)
                    .setTitle(getResources().getString(R.string.tip_title))
                    .setMessage(getResources().getString(R.string.tip_message_exit))
                    .requestSystemAlert(true)
                    .setPositiveClickListener(getString(R.string.exit), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            dialogInterface.dismiss();
                            System.exit(0);
                        }
                    })
                    .setNegativeClickListener(getString(R.string.cancel), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            dialogInterface.dismiss();
                        }
                    }).build();
            dialog.show();
        }
    }

    //hide 所有fragment
    private void hideFragment(FragmentTransaction fragmentTransaction) {
        if (diagnosisFragment != null) {
            fragmentTransaction.hide(diagnosisFragment);
        }
        if (upgradeFragment != null) {
            fragmentTransaction.hide(upgradeFragment);
        }
        if (settingFragment != null) {
            fragmentTransaction.hide(settingFragment);
        }
        if (reportFragment != null) {
            fragmentTransaction.hide(reportFragment);
        }
        if (homeFragment != null) {
            fragmentTransaction.hide(homeFragment);
        }
    }


    @Override
    public void onAttachFragment(Fragment fragment) {
        if (diagnosisFragment == null && fragment instanceof DiagnosisFragment) {
            diagnosisFragment = (DiagnosisFragment) fragment;
        }
        if (upgradeFragment == null && fragment instanceof UpgradeFragment) {
            upgradeFragment = (UpgradeFragment) fragment;
        }
        if (settingFragment == null && fragment instanceof SettingFragment) {
            settingFragment = (SettingFragment) fragment;
        }
        if (reportFragment == null && fragment instanceof DataFragment) {
            reportFragment = (DataFragment) fragment;
        }
        if (homeFragment == null && fragment instanceof HomeFragment) {
            homeFragment = (HomeFragment) fragment;
        }
        super.onAttachFragment(fragment);
    }

    public void onFragSwitch(int position) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        hideFragment(fragmentTransaction);
        switch (position) {
            case HOME_ID:
                if (homeFragment == null) {
                    homeFragment = HomeFragment.newInstance("", "");
                    fragmentTransaction.add(R.id.main_fl, homeFragment);
                } else {
                    fragmentTransaction.show(homeFragment);
                }
                fragmentTransaction.commitAllowingStateLoss();
                break;
            case DIAGNOSIS_ID:
                if (diagnosisFragment == null) {
                    diagnosisFragment = DiagnosisFragment.newInstance("", "");
                    fragmentTransaction.add(R.id.main_fl, diagnosisFragment);
                } else {
                    fragmentTransaction.show(diagnosisFragment);
                    diagnosisFragment.questCarList();
                }
                fragmentTransaction.commitAllowingStateLoss();
                break;
            case REPORT_ID:
                if (reportFragment == null) {
                    reportFragment = DataFragment.newInstance();
                    fragmentTransaction.add(R.id.main_fl, reportFragment);
                } else {
                    fragmentTransaction.show(reportFragment);
                    reportFragment.refresh();
                }
                fragmentTransaction.commitAllowingStateLoss();
                break;
            case UPGRADE_ID:
                if (upgradeFragment == null) {
                    upgradeFragment = UpgradeFragment.newInstance();
                    fragmentTransaction.add(R.id.main_fl, upgradeFragment);
                } else {
                    fragmentTransaction.show(upgradeFragment);
                    upgradeFragment.refresh();
                }
                fragmentTransaction.commitAllowingStateLoss();

                break;
            case SETTING_ID:
                if (settingFragment == null) {
                    settingFragment = SettingFragment.newInstance("", "");
                    fragmentTransaction.add(R.id.main_fl, settingFragment);
                } else {
                    fragmentTransaction.show(settingFragment);
                }
                fragmentTransaction.commitAllowingStateLoss();
                break;
        }
    }


    private void registerReceiver(){
        //注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_CONNECTIVITY_CHANGE);
        registerReceiver(mReceiver, filter);
    }

    private void unregisterReceiver(){
        unregisterReceiver(mReceiver);
    }

    /**
     * 接收网络连接广播
     */
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_CONNECTIVITY_CHANGE:
                    if (WifiUtil.isSupportNetwork(context)) {
                        if (!Config.checked) {
                            queryBTList();
                            checkDeviceMode();
                        }

                        ReportUntil.autoPostReport(MainActivity.this);
                    }
            }
        }
    };

}
