package com.west.develop.westapp.UI.Activity.Diagnosis;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.west.develop.westapp.Communicate.COMFunAPI;
import com.west.develop.westapp.Communicate.Service.BluetoothService;
import com.west.develop.westapp.Communicate.Service.UsbService;
import com.west.develop.westapp.CustomView.ScreenView;
import com.west.develop.westapp.Dialog.BackupDialog;
import com.west.develop.westapp.Dialog.FileDialog;
import com.west.develop.westapp.Dialog.LoadDialog;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.Protocol.Drivers.RunningDriver;
import com.west.develop.westapp.Protocol.Drivers.UpDriver;
import com.west.develop.westapp.Protocol.HolderThread;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.Diagnosis.KeyEvent;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.Utils.HexUtil;
import com.west.develop.westapp.Tools.Utils.ReportUntil;
import com.west.develop.westapp.Tools.Utils.SoundUtil;
import com.west.develop.westapp.UI.base.BaseActivity;
import com.west.develop.westapp.bluetooth.BluetoothSerialPort;
import com.west.develop.westapp.usb.UsbSerialPort;

import java.io.File;

//import com.west.develop.westapp.Dialog.BackDialog;


/**
 * Created by Develop0 on 2017/11/14.
 */
public class RunActivity extends BaseActivity implements
        View.OnClickListener, View.OnTouchListener {
    public static final String kStartFile = "startFile";
    public static final String kProgType = "progType";
    public static final String kFuncName = "functionName";

    public static final String kForceExit = "ForceExit";
    public static final int RESULT_EXIT = 2;

    /**
     * 诊断模式
     */
    public static final int TYPE_RELEASE = 1;
    /**
     * 本地调试模式
     */
    public static final int TYPE_DEBUG = 2;

    private int mProType = TYPE_RELEASE;
    public String mProgName;
    public File mProFile;

    TextView backTv;
    TextView title;

    ScreenView mScreenView;
    RelativeLayout mContentLayout;

    private TipDialog backDialog = null;
    private TipDialog timeDialog = null;

    Handler mHandler = new Handler();


    VideoController mVideoController;

    @Override
    protected View getContentView() {
        /**
         * 隐藏系统的软键盘的显示
         */
        Window window = getWindow();
        WindowManager.LayoutParams params = window.getAttributes();
        params.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE;

        window.setAttributes(params);
        window.setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        registerReceiver();
        if (RunningDriver.getInstance() != null) {
            RunningDriver.initContext(this);
            RunningDriver.getInstance().startListen(listenCallback);
        }
        return this.getLayoutInflater().inflate(R.layout.activity_run, null);
    }

    @Override
    protected void initView() {
        mVideoController = new VideoController(this);


        backTv = findViewById(R.id.car_back);
        title = findViewById(R.id.car_title);    //标题

        title.setText(R.string.main_diagnosis);

        backTv.setOnClickListener(this);

        mScreenView = findViewById(R.id.screenView);
        mContentLayout = findViewById(R.id.contentLayout);
        onConfigurationChanged(this.getResources().getConfiguration());

    }

    @Override
    protected void initListener() {
        findViewById(R.id.dialog_No_BTN).setOnTouchListener(this);
        findViewById(R.id.dialog_Yes_BTN).setOnTouchListener(this);
        findViewById(R.id.dialog_Left_BTN).setOnTouchListener(this);
        findViewById(R.id.dialog_Down_BTN).setOnTouchListener(this);
        findViewById(R.id.dialog_Up_BTN).setOnTouchListener(this);
        findViewById(R.id.dialog_Right_BTN).setOnTouchListener(this);

        backTv.setOnClickListener(this);
    }

    @Override
    protected void initData() {
        String proFile = getIntent().getStringExtra(kStartFile);
        String funcName = getIntent().getStringExtra(kFuncName);
        mProType = getIntent().getIntExtra(kProgType, TYPE_RELEASE);

        mProFile = new File(proFile);

        if (mProType == TYPE_RELEASE) {
            String progRoot = FileUtil.getProgramRoot(this);

            File parentFile = mProFile.getParentFile();
            String proName = parentFile.getPath();//parentFile.getParentFile().getPath() + "/" + parentFile.getName().substring(5);

            int end = proName.length();
            if (proName.lastIndexOf("_v") > 0) {
                end = proName.lastIndexOf("_v");
            }
            mProgName = proName.substring(proName.indexOf(progRoot) + progRoot.length(), end);

            //检查视频和帮助文档是否存在
            mVideoController.initData();
        }
        if (mProType == TYPE_DEBUG) {
            findViewById(R.id.onlineVideo).setVisibility(View.GONE);
            findViewById(R.id.helpfile).setVisibility(View.GONE);
            mProgName = mProFile.getName();
            Log.e("initData", "initData: " + mProFile.getPath());
            Log.e("initData", "initData: " + mProgName);
            if (mProgName.toLowerCase().indexOf(".bin") > 0) {
                mProgName = mProgName.substring(0, mProgName.toLowerCase().lastIndexOf(".bin"));
            }
        }
        title.setText(mProgName + "/" + funcName);

    }

    private void showTimeOut() {
        if (waitThread != null) {
            waitThread.Stop();
        }
        if (timeDialog == null) {

            timeDialog = new TipDialog.Builder(this)
                    .setTitle(getString(R.string.tip_title))
                    .setMessage(getString(R.string.tip_UNReceive))
                    .setPositiveClickListener(getString(R.string.back_cancel_Btn), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            dialogInterface.dismiss();
                            if (waitThread != null) {
                                waitThread.Stop();
                                waitThread = null;
                            }
                            waitThread = new WaitThread();
                            waitThread.start();

                        }
                    })
                    .setNegativeClickListener(getString(R.string.back_commit_Btn), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            forceExit();
                            dialogInterface.dismiss();
                            backDialog = null;
                        }
                    }).build();
        }
        timeDialog.show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.car_back:
                onBackPressed();
                break;
            default:
                break;
        }

    }


    @Override
    protected void onDestroy() {
        if (RunningDriver.getInstance() != null) {
            RunningDriver.getInstance().stopListen();
            COMFunAPI.getInstance().COMPortClose(RunningDriver.getInstance().getPort());
        }
        unRegisterReceiver();
        if (waitThread != null) {
            waitThread.Stop();
        }
        waitThread = null;
        FileDialog.destory();
        BackupDialog.destory();

        HolderThread.Stop();

        super.onDestroy();
    }

    /**
     * 清除屏幕
     */
    public void Clr_Scr() {
        ReportUntil.writeDataToReport(RunActivity.this, "CLR_SCR");  //记录信息
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mScreenView.Clr_Scr();
            }
        };
        mHandler.post(runnable);
    }


    /**
     * 显示 12 * 12 字库里面的汉字或字符
     * @param PAG   行的页（0,2,4,6,8,10,12,14,16,18）
     * @param COL   列（0 - 159）
     * @param NOT_DISP  =0:正显  ;    !=0: 反显
     * @param STR_LEN
     * @param STRING
     */
    public void GENERAL_CN_EN_STR(final int PAG, final int COL, final int NOT_DISP, final int STR_LEN, final String STRING) {
        ReportUntil.writeDataToReport(RunActivity.this, "GENERAL_CN_EN_STR(PAG:" + PAG + ",COL:" + COL + ",NOT_DISP:" + (NOT_DISP == 0) + ",LEN:" + STRING.length() + ")\n   " + STRING);  //记录信息
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.e("GENERAL_CN_EN-STRING", "run: " + STRING);
                mScreenView.GENERAL_CN_EN_STR(PAG, COL, NOT_DISP, STR_LEN, STRING);
            }
        };

        mHandler.post(runnable);
    }

    /**
     * 显示 M128 0x10000后的 FLASH 常量型 12 * 12 汉字和 SRAM 文字
     * @param PAG   行的页（0,2,4,6,8,10,12,14,16,18）
     * @param COL   列（0 - 159）
     * @param NOT_DISP  =0:正显  ;    !=0: 反显
     * @param STR_LEN
     * @param STRING
     */
    public void SPECIFY_CN_EN_STR(final int PAG, final int COL, final int NOT_DISP, final int STR_LEN, final String STRING) {
        ReportUntil.writeDataToReport(RunActivity.this, "SPECIFY_CN_EN_STR(PAG:" + PAG + ",COL:" + COL + ",NOT_DISP:" + (NOT_DISP == 0) + ",LEN:" + STRING.length() + ")\n   " + STRING);  //记录信息
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.e("SPECIFY_CN_EN-STRING", "run: " + STRING);
                mScreenView.SPECIFY_CN_EN_STR(PAG, COL, NOT_DISP, STR_LEN, STRING);
            }
        };
        mHandler.post(runnable);
    }


    /**
     * 显示 6 * 8 字符串
     * @param PAG   行的页（0 - 19）
     * @param COL   列（0 - 159）
     * @param NOT_DISP  =0:正显  ;    !=0: 反显
     * @param STR_LEN
     * @param STRING
     */
    public void ASCII_6x8(final int PAG, final int COL, final int NOT_DISP, final int STR_LEN, final String STRING) {
        ReportUntil.writeDataToReport(RunActivity.this, "ASCII_6x8(PAG:" + PAG + ",COL:" + COL + ",NOT_DISP:" + (NOT_DISP == 0) + ",LEN:" + STRING.length() + ")\n   " + STRING);  //记录信息

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Log.e("ASCII_6x8-STRING", "run: " + STRING);
                mScreenView.ASCII_6x8(PAG, COL, NOT_DISP, STR_LEN, STRING);
            }
        };
        mHandler.post(runnable);
    }


    public void PROGRESS(final int progress) {
        ReportUntil.writeDataToReport(RunActivity.this, "PROGRESS(" + progress + ")");  //记录信息
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                mScreenView.onProgress(progress);
            }
        };

        mHandler.post(runnable);
    }


    /**
     * 启动文件上传接收
     * @param length
     */
    public void UPFILE_NEW(final int length) {
        ReportUntil.writeDataToReport(RunActivity.this, "BACKUP_START: " + length + "Bytes");  //记录信息
        if (BackupDialog.getInstance() != null) {
            BackupDialog.destory();
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                BackupDialog.newInstance(RunActivity.this, length, new BackupDialog.OnUPFileListener() {
                    @Override
                    public void onName(String filename) {
                        HolderThread.Stop();
                        RunningDriver.getInstance().receiveUPFILE();
                        RunningDriver.getInstance().countTimeout(true);
                    }

                    @Override
                    public void onFinish(int resultCode, byte[] buffer) {
                        ReportUntil.writeDataToReport(RunActivity.this, "BACKUP_FINISH: " + resultCode);  //记录信息
                        BackupDialog.destory();
                        RunningDriver.getInstance().countTimeout(false);
                        if (resultCode != BackupDialog.RESULT_OK) {
                            RunningDriver.getInstance().UPFile_Cancel();
                        }

                    }

                    @Override
                    public void onCancel() {
                        ReportUntil.writeDataToReport(RunActivity.this, "BACKUP_CANCEL: ");  //记录信息
                        HolderThread.Stop();
                        RunningDriver.getInstance().UPFile_Cancel();
                        BackupDialog.destory();
                    }
                }).show();
                HolderThread.newInstance(RunningDriver.getInstance()).start();
            }
        });

    }

    public boolean UPFILE_INDATA(final byte[] buffer) {
        if (BackupDialog.getInstance() == null) {
            ReportUntil.writeDataToReport(RunActivity.this, "BACKUP_REC: NO TASK");  //记录信息
            return false;
        }
        RunningDriver.getInstance().reCountTimeout();
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ReportUntil.writeDataToReport(RunActivity.this, "BACKUP_REC: " + (buffer == null ? 0 : buffer.length - 4));  //记录信息
                if (BackupDialog.getInstance() != null) {
                    BackupDialog.getInstance().inUPData(buffer);
                }

            }
        });

        return true;
    }


    public void UPFILE_FINISH() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (BackupDialog.getInstance() != null) {
                    BackupDialog.getInstance().finishUP();

                }
            }
        });

    }


    /**
     * 加载备份文件
     */
    public void LOAD_BACKUP_NEW(final byte flagByte) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                ReportUntil.writeDataToReport(RunActivity.this, "BROWSE_BACKUP: " + HexUtil.toHexString(flagByte));  //记录信息
                if (flagByte == (byte) 0x01 && FileDialog.getInstance() != null) {
                    FileDialog.getInstance().onSelect();
                } else {
                    FileDialog.newDownInstance(RunActivity.this, flagByte, new FileDialog.OnLoadFileListener() {
                        @Override
                        public void onSelect(File file) {
                            if (file != null && file.exists()) {
                                // FileDialog.getInstance().dismiss();
                                HolderThread.Stop();
                                RunningDriver.getInstance().BACKUP_Lengh(file.length(), flagByte);
                            }
                        }

                        @Override
                        public void onFinish(int resultCode) {

                        }

                        @Override
                        public void onCancel() {
                            //RunningDriver.getInstance().BACKUP_Cancel();
                            HolderThread.Stop();
                            FileDialog.destory();
                        }

                        @Override
                        public void onRead(byte[] buffer) {
                            ReportUntil.writeDataToReport(RunActivity.this, "BROWSE_BACKUP_RETURN:" + (buffer == null ? 0 : buffer.length));  //记录信息
                            boolean success = RunningDriver.getInstance().BACKUP_WRITE(buffer);
                            if (FileDialog.getInstance() != null) {
                                //FileDialog.getInstance().BACKUP_FINISH(success);
                            }
                        }
                    });
                    if (flagByte == (byte) 0x01) {
                        FileDialog.getInstance().onSelect();
                    } else if (flagByte == (byte) 0x02) {
                        FileDialog.getInstance().show();
                    }
                    HolderThread.newInstance(RunningDriver.getInstance()).start();
                }
                // FileDialog.getInstance().show();


            }
        });
    }

    /**
     * 长度匹配与否
     * @param match
     */
    public void LENGH_MATCH(final boolean match) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!match) {
                    Toast.makeText(RunActivity.this, getString(R.string.file_DOWN_mismatch), Toast.LENGTH_SHORT).show();
                } else {
                    if (FileDialog.getInstance() != null) {
                        FileDialog.getInstance().LENGH_MATCH();
                    }
                }

            }
        });

    }

    public void LOAD_BACKUP_START(final int addr, final int length) {
        ReportUntil.writeDataToReport(RunActivity.this, "BROWSE_BACKUP_ADDR:" + HexUtil.toHexString(addr));  //记录信息
        ReportUntil.writeDataToReport(RunActivity.this, "BROWSE_BACKUP_LENGTH:" + length);  //记录信息
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                if (FileDialog.getInstance() != null) {
                    FileDialog.getInstance().BACKUP_START(addr, length);
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mVideoController.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int keyValue = -1;
                switch (v.getId()) {
                    case R.id.dialog_No_BTN:
                        keyValue = KeyEvent.KEY_ESC;
                        ReportUntil.writeDataToReport(RunActivity.this, "NO" + getString(R.string.click_down));
                        break;
                    case R.id.dialog_Yes_BTN:
                        keyValue = KeyEvent.KEY_ENTER;
                        ReportUntil.writeDataToReport(RunActivity.this, "YES" + getString(R.string.click_down));
                        break;
                    case R.id.dialog_Left_BTN:
                        keyValue = KeyEvent.KEY_LEFT;
                        ReportUntil.writeDataToReport(RunActivity.this, "Left" + getString(R.string.click_down));
                        break;
                    case R.id.dialog_Down_BTN:
                        keyValue = KeyEvent.KEY_DOWN;
                        ReportUntil.writeDataToReport(RunActivity.this, "Down" + getString(R.string.click_down));
                        break;
                    case R.id.dialog_Up_BTN:
                        keyValue = KeyEvent.KEY_UP;
                        ReportUntil.writeDataToReport(RunActivity.this, "Up" + getString(R.string.click_down));
                        break;
                    case R.id.dialog_Right_BTN:
                        keyValue = KeyEvent.KEY_RIGHT;
                        ReportUntil.writeDataToReport(RunActivity.this, "Right" + getString(R.string.click_down));
                        break;
                    default:
                        break;
                }
                KeyEvent.onKeyClick(keyValue);
                break;
            case MotionEvent.ACTION_UP:
                KeyEvent.onKeyClick(KeyEvent.KEY_RELEASE);
                switch (v.getId()) {
                    case R.id.dialog_No_BTN:
                        ReportUntil.writeDataToReport(RunActivity.this, "NO" + getString(R.string.click_release));
                        break;
                    case R.id.dialog_Yes_BTN:
                        ReportUntil.writeDataToReport(RunActivity.this, "YES" + getString(R.string.click_release));
                        break;
                    case R.id.dialog_Left_BTN:
                        ReportUntil.writeDataToReport(RunActivity.this, "Left" + getString(R.string.click_release));
                        break;
                    case R.id.dialog_Down_BTN:
                        ReportUntil.writeDataToReport(RunActivity.this, "Down" + getString(R.string.click_release));
                        break;
                    case R.id.dialog_Up_BTN:
                        ReportUntil.writeDataToReport(RunActivity.this, "Up" + getString(R.string.click_release));
                        break;
                    case R.id.dialog_Right_BTN:
                        ReportUntil.writeDataToReport(RunActivity.this, "Right" + getString(R.string.click_release));
                        break;
                    default:
                        break;
                }
                break;

        }
        return false;
    }


    /**
     * 强制退出
     */
    private void forceExit() {
        /**
         * 记录文件已  _1.txt  结尾，自动上传记录
         */
        String fileName = UpDriver.getInstance(RunActivity.this).getPack().getFileName();
        if (fileName.toLowerCase().endsWith("_1.txt")) {
            ReportUntil.postReport(RunActivity.this, fileName);
        }
        /**
         * 播放警告声音，直到点击确定按钮才停止
         */
        // SoundUtil.programExitSound(RunActivity.this);
        SoundUtil.deviceExitTipSound(RunActivity.this);
        new TipDialog.Builder(RunActivity.this).setTitle(getResources().getString(R.string.tip_title))
                .setImageDrawable(getResources().getDrawable(R.mipmap.user_warning, null))
                .setMessage(getString(R.string.pullout_device))
                .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                    @Override
                    public void onClick(Dialog dialogInterface, int index, String label) {
                        SoundUtil.deviceExitTipSoundStop();//停止声音播放
                        dialogInterface.dismiss();

                        RunningDriver.getInstance().ExitFunc();
                        Intent intent = new Intent();
                        intent.putExtra(kForceExit, true);
                        setResult(RESULT_EXIT, intent);
                        finish();

                    }
                })
                .requestSystemAlert(true)
                .build().show();
    }



    @Override
    public void onBackPressed() {
        if (backDialog == null) {
            backDialog = new TipDialog.Builder(this)
                    .setTitle(getString(R.string.tip_title))
                    .setMessage(getString(R.string.Run_back_Tip))
                    .setPositiveClickListener(getString(R.string.back_cancel_Btn), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            RunningDriver.getInstance().startListen(listenCallback);
                            dialogInterface.dismiss();
                            backDialog = null;
                        }
                    })
                    .setNegativeClickListener(getString(R.string.back_commit_Btn), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            forceExit();
                            dialogInterface.dismiss();
                            backDialog = null;
                        }
                    })
                    .build();
            backDialog.show();
        } else {
            backDialog.dismiss();
            backDialog = null;
        }
    }


    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UsbService.ACTION_USB_CHECK_SUCCESS);
        intentFilter.addAction(UsbService.ACTION_USB_DISCONNECTED);
        intentFilter.addAction(BluetoothService.ACTION_BLUETOOTH_CHECK_SUCCESS);
        intentFilter.addAction(BluetoothService.ACTION_BLUETOOTH_DISCONNECTED);
        registerReceiver(mReceiver, intentFilter);

    }

    private void unRegisterReceiver() {
        unregisterReceiver(mReceiver);
        mVideoController.unRegisterReceiver();
    }


    private LoadDialog mDisconnectDialog;


    private ListenCallback listenCallback = new ListenCallback() {
        @Override
        public void onStart() {
            Log.e("listener", "start");
            if (mDisconnectDialog != null && !mDisconnectDialog.isShowing()) {
                mDisconnectDialog.dismiss();
                mDisconnectDialog = null;
            }
            mDisconnectDialog = new LoadDialog.Builder(RunActivity.this)
                    .setTitle(getString(R.string.deviceDisconnected))
                    .setCancel(getString(R.string.disconnect_Cancel), new LoadDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialog) {
                            onBackPressed();
                            dialog.dismiss();
                            mDisconnectDialog = null;
                        }
                    })
                    .build();
            mDisconnectDialog.show();

        }

        @Override
        public void onSuccess() {
            Log.e("listener", "success");
            if (waitThread != null) {
                waitThread.Stop();
                waitThread = null;
            }
            waitThread = new WaitThread();
            waitThread.start();


            if (mDisconnectDialog != null) {
                mDisconnectDialog.dismiss();
                mDisconnectDialog = null;
            }
        }
    };


    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case UsbService.ACTION_USB_DISCONNECTED:
                    if (UpDriver.getInstance(RunActivity.this).getPort() instanceof UsbSerialPort) {
                        UpDriver.getInstance(RunActivity.this).initPort(null);
                    }
                    if (RunningDriver.getInstance() != null) {
                        RunningDriver.initContext(RunActivity.this);
                        if (RunningDriver.getInstance().getPort() instanceof UsbSerialPort) {
                            RunningDriver.getInstance().initPort(null);
                            waitThread.Stop();
                            waitThread = null;
                        }
                    }

                    RunningDriver.getInstance().startListen(listenCallback);
                    break;

                case BluetoothService.ACTION_BLUETOOTH_DISCONNECTED:
                    if (UpDriver.getInstance(RunActivity.this).getPort() instanceof BluetoothSerialPort) {
                        UpDriver.getInstance(RunActivity.this).initPort(null);
                    }
                    if (RunningDriver.getInstance() != null) {
                        RunningDriver.initContext(RunActivity.this);
                        if (RunningDriver.getInstance().getPort() instanceof BluetoothSerialPort) {
                            RunningDriver.getInstance().initPort(null);
                            waitThread.Stop();
                            waitThread = null;
                        }
                    }

                    RunningDriver.getInstance().startListen(listenCallback);
                    break;

            }
        }
    };


    public void redoCount() {
        if (timeDialog != null) {
            timeDialog.dismiss();
            timeDialog = null;
        }
        if (waitThread != null) {
            waitThread.clearCount();
        }
    }


    private WaitThread waitThread = new WaitThread();

    private class WaitThread extends Thread {
        static final int time = 3 * 60;
        int count = 0;
        boolean stop = false;
        @Override
        public void run() {
            try {
                while (count < time && !stop) {
                    Log.e("Not active", count + "");
                    count++;
                    Thread.sleep(1000);
                }
                if (!stop) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            showTimeOut();
                        }
                    });
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
        }

        void clearCount() {
            count = 0;
        }

        void Stop() {
            stop = true;
        }
    }

    public interface ListenCallback {
        void onStart();
        void onSuccess();
    }


}
