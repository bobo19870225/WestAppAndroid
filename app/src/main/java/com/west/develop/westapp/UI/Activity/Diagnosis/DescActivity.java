package com.west.develop.westapp.UI.Activity.Diagnosis;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.west.develop.westapp.Communicate.Service.BluetoothService;
import com.west.develop.westapp.Communicate.Service.UsbService;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.ConnectStatus;
import com.west.develop.westapp.Dialog.LoadDialog;
import com.west.develop.westapp.Dialog.SignDialog;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.Protocol.Drivers.RunningDriver;
import com.west.develop.westapp.Protocol.Drivers.UpDriver;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.Diagnosis.DiagnosisAPI;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.Utils.ReportUntil;
import com.west.develop.westapp.Tools.Utils.SoundUtil;
import com.west.develop.westapp.Tools.constant.RequestCodeConstant;
import com.west.develop.westapp.UI.Adapter.Diagnosis.DescAdapter;
import com.west.develop.westapp.UI.Fragment.report.ReportFragment;
import com.west.develop.westapp.UI.base.BaseActivity;
import com.west.develop.westapp.bluetooth.BluetoothSerialPort;
import com.west.develop.westapp.usb.UsbSerialPort;

import java.io.File;
import java.util.List;

/**
 * Created by Develop0 on 2017/11/13.
 */

public class DescActivity extends BaseActivity {
    public static final int MSG_DIALOG_SHOW = 2;

    private String mProgName;
    private File mProFile;

    TextView backTv;
    TextView title;
    TextView help;
    TextView helpContent;
    TextView commit_TV;

    View line;
    ListView mFuncListView;
    RelativeLayout helpRelativeLayout;
    DescAdapter adapter;

    String[] mFuncs;


    @Override
    protected View getContentView() {
        return this.getLayoutInflater().inflate(R.layout.activity_desc, null);
    }

    @Override
    protected void initView() {
        backTv = findViewById(R.id.car_back);
        title = findViewById(R.id.car_title);    //标题
        help = findViewById(R.id.diagnosis_help);
        help.setVisibility(View.GONE);
        title.setText(R.string.main_diagnosis);
        helpRelativeLayout = findViewById(R.id.help_RL);
        helpContent = findViewById(R.id.help_content);
        commit_TV = findViewById(R.id.commit_TV);

        mFuncListView = findViewById(R.id.Function_LV);
        line = findViewById(R.id.listline);

    }

    @Override
    protected void initData() {
        String proFile = getIntent().getStringExtra(RunActivity.kStartFile);
        int mProType = getIntent().getIntExtra(RunActivity.kProgType, RunActivity.TYPE_RELEASE);
        if (proFile != null) {
            mProFile = new File(proFile);
        }
        if (mProType == RunActivity.TYPE_RELEASE) {
            String progRoot = FileUtil.getProgramRoot(this);
            String parent = mProFile.getParent();
            String fileName = mProFile.getName();
            String proName = parent + "/" + fileName.substring(4, fileName.toLowerCase().lastIndexOf(".bin"));
            /*if (Config.getInstance(this).getDevice() != null) {

            }else {
                proName = mProFile.getParent();
            }*/

            mProgName = proName.substring(proName.indexOf(progRoot) + progRoot.length());

            if (mProgName.endsWith("_1")) {
                mProgName = mProgName.substring(0, mProgName.length() - 2);
            }

            File programRoot = mProFile.getParentFile();
            String str = "";
            if (programRoot != null && programRoot.exists()) {
                File[] files = programRoot.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.getName().toLowerCase().endsWith(".txt")) {
                            str = FileUtil.readBinFileDecsData(file);
                        }
                    }
                }
            }

            //helpContent.setText("\u3000\u3000" +str);
            helpContent.setText(str);

        }
        if (mProType == RunActivity.TYPE_DEBUG) {
            mProgName = mProFile.getName();
            if (mProgName.toLowerCase().indexOf(".bin") > 0) {
                mProgName = mProgName.substring(0, mProgName.toLowerCase().lastIndexOf(".bin"));
            }
        }
        //mProgName = mProgName.replace("/",">");
        title.setText(mProgName);

    }

    /**
     * 运行程序
     */
    private void onCommit() {
        /*
         * 设备已激活，或未完成配置，或者未激活但剩余试用次数
         */
        if (Config.getInstance(this).isSigned() || !Config.getInstance(this).isConfigured() ||
                (Config.getInstance(this).getBondDevice() != null && Config.getInstance(this).getRegCount() < Config.TRYCOUNT)
        ) {
            if (ConnectStatus.getInstance(this).getUSBPort() != null) {
                DiagnosisAPI.init(this);
                DiagnosisAPI.getInstance().startWithFile(mProFile, false);
            } else if (ConnectStatus.getInstance(this).getBTPort() != null) {
                DiagnosisAPI.init(this);
                DiagnosisAPI.getInstance().startWithFile(mProFile, false);
            } else {
                //Toast.makeText(this, getString(R.string.device_not_connect), Toast.LENGTH_SHORT).show();
                if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    if (ConnectStatus.getInstance(this).getBTPort() == null) {
                        mHandler.sendEmptyMessage(MSG_DIALOG_SHOW);
                    }
                } else {
                    new TipDialog.Builder(DescActivity.this).setMessage(getResources().getString(R.string.tip_Open_Bluetooth))
                            .setTitle(getResources().getString(R.string.tip_title))
                            .setNegativeClickListener(getResources().getString(R.string.cancel), new TipDialog.OnClickListener() {
                                @Override
                                public void onClick(Dialog dialogInterface, int index, String label) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setPositiveClickListener(getResources().getString(R.string.tip_yes), new TipDialog.OnClickListener() {
                                @Override
                                public void onClick(Dialog dialogInterface, int index, String label) {
                                    BluetoothAdapter.getDefaultAdapter().enable();
                                    dialogInterface.dismiss();
                                    mHandler.sendEmptyMessage(MSG_DIALOG_SHOW);
                                }
                            })
                            .requestSystemAlert(true)
                            .build().show();
                }
            }
        }
        /*
         * 试用次数已经用完
         */
        else {
            if (!Config.getInstance(DescActivity.this).isConfigured()) {
                Toast.makeText(DescActivity.this, getString(R.string.notSold), Toast.LENGTH_SHORT).show();
                return;
            }
            TipDialog dialog = new TipDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.tip_title))
                    .setMessage(getString(R.string.device_not_sign))
                    .setNegativeClickListener(getString(R.string.tip_no), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            dialogInterface.dismiss();
                        }
                    })
                    .setPositiveClickListener(getString(R.string.tip_yes), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            dialogInterface.dismiss();

                            SignDialog.newInstance(DescActivity.this)
                                    .setNegativeClickListener(new SignDialog.OnClickListener() {
                                        @Override
                                        public void onClick(Dialog dialog) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setSignCallback(new SignDialog.SignCallback() {
                                        @Override
                                        public void onFinish(boolean success, SignDialog dialogInterface, String message) {
                                            if (success) {
                                                onCommit();
                                            }
                                            dialogInterface.dismiss();
                                        }
                                    })
                                    .show();
                            if (UsbService.getInstance().getSerialPorts() != null) {
                                final Handler handler = new Handler();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (SignDialog.getInstance() != null && !SignDialog.getInstance().waitSign()) {
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
                                                            SignDialog.getInstance().signWithPort(mUsbPorts.get(i));
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                for (int i = 0; i < UsbService.getInstance().getSerialPorts().size(); i++) {
                                                    SignDialog.getInstance().signWithPort(UsbService.getInstance().getSerialPorts().get(i));
                                                }
                                            }
                                        });
                                    }
                                }).start();

                            }
                        }
                    })
                    .requestSystemAlert(true).build();
            dialog.show();
        }
    }

    LoadDialog loadDialog;
    TipDialog tipDialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_DIALOG_SHOW:
                    if (tipDialog != null) {
                        tipDialog.dismiss();
                        tipDialog = null;
                    }
                    tipDialog = new TipDialog.Builder(DescActivity.this).setTitle(getResources().getString(R.string.bt_conn_tip))
                            .setMessage(getResources().getString(R.string.bt_conn_tip_message))
                            .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                                @Override
                                public void onClick(final Dialog dialogInterface, int index, String label) {
                                    dialogInterface.dismiss();
                                    loadDialog = new LoadDialog.Builder(DescActivity.this)
                                            .setTitle(getResources().getString(R.string.tip_blutooth_conn))
                                            .setCancel(getResources().getString(R.string.cancel), new LoadDialog.OnClickListener() {
                                                @Override
                                                public void onClick(Dialog dialog) {
                                                    loadDialog.dismiss();
                                                    unregisterConnect();
                                                }
                                            })
                                            .requestSystemAlert(true)
                                            .build();
                                    loadDialog.show();

                                }
                            })
                            .requestSystemAlert(true)
                            .build();

                    tipDialog.show();
                    registerConnect();
                    /*BluetoothService.getInstance().setConnectCallback(new BluetoothService.ConnectCallback(){
                        @Override
                        public void onFinish(boolean success) {
                            if(success){
                                if(tipDialog != null) {
                                    tipDialog.dismiss();
                                    if (loadDialog != null && loadDialog.isShowing()){
                                        loadDialog.dismiss();
                                        onCommit();
                                    }
                                }
                            }
                        }
                    });
*/
                    break;

            }
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodeConstant.CODE_SIGN_ACTIVITY && resultCode == RESULT_OK) {
            onCommit();
        }
        if (requestCode == RequestCodeConstant.CODE_RUN_ACTIVITY) {
            if (resultCode == RunActivity.RESULT_EXIT) {
                mFuncListView.setVisibility(View.GONE);
                helpRelativeLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    //程序下载成功后，刷新列表
    public void refreshIniFile(final File file) {
        mFuncListView.post(new Runnable() {
            @Override
            public void run() {
                helpRelativeLayout.setVisibility(View.GONE);
                mFuncListView.setVisibility(View.VISIBLE);
                line.setVisibility(View.VISIBLE);
                File programRoot = file.getParentFile();
                String line = "";
                if (programRoot != null && programRoot.exists()) {
                    File[] files = programRoot.listFiles();
                    if (files != null) {
                        for (File value : files) {
                            if (value.getName().toLowerCase().endsWith(".ini")) {
                                line = FileUtil.readIniData(value);
                            }
                        }
                    }
                }

                mFuncs = line.split("\n");

                adapter = new DescAdapter(DescActivity.this, mFuncs);
                mFuncListView.setAdapter(adapter);


                mFuncListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                if (UpDriver.getInstance(DescActivity.this).getPort() != null) {
                                    if (UpDriver.getInstance(DescActivity.this).getPort() instanceof UsbSerialPort) {
                                        if (ConnectStatus.getInstance(DescActivity.this).getUSBPort() == null) {
                                            UpDriver.getInstance(DescActivity.this).initPort(null);
                                            return;
                                        } else if (ConnectStatus.getInstance(DescActivity.this).getUSBPort() != UpDriver.getInstance(DescActivity.this).getPort()) {
                                            UpDriver.getInstance(DescActivity.this).initPort(ConnectStatus.getInstance(DescActivity.this).getUSBPort());
                                        }
                                    }
                                    if (UpDriver.getInstance(DescActivity.this).getPort() instanceof BluetoothSerialPort) {
                                        if (ConnectStatus.getInstance(DescActivity.this).getBTPort() == null) {
                                            UpDriver.getInstance(DescActivity.this).initPort(null);
                                            return;
                                        } else if (ConnectStatus.getInstance(DescActivity.this).getBTPort() != UpDriver.getInstance(DescActivity.this).getPort()) {
                                            UpDriver.getInstance(DescActivity.this).initPort(ConnectStatus.getInstance(DescActivity.this).getBTPort());
                                        }
                                    }
                                } else {
                                    if (ConnectStatus.getInstance(DescActivity.this).getUSBPort() != null) {
                                        UpDriver.getInstance(DescActivity.this).initPort(ConnectStatus.getInstance(DescActivity.this).getUSBPort());
                                    } else if (ConnectStatus.getInstance(DescActivity.this).getBTPort() != null) {
                                        UpDriver.getInstance(DescActivity.this).initPort(ConnectStatus.getInstance(DescActivity.this).getBTPort());
                                    } else {
                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(DescActivity.this, getString(R.string.device_not_connect), Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                        return;
                                    }
                                }

                                if (UpDriver.getInstance(DescActivity.this).RUNFun(position + 1, false)) {
                                    RunningDriver.init(UpDriver.getInstance(DescActivity.this).getPack());
                                    RunningDriver.getInstance().initPort(UpDriver.getInstance(DescActivity.this).getPort());

                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Intent intent = getIntent();
                                            intent.setClass(DescActivity.this, RunActivity.class);
                                            intent.putExtra(RunActivity.kFuncName, mFuncs[position]);
                                            startActivityForResult(intent, RequestCodeConstant.CODE_RUN_ACTIVITY);
                                        }
                                    });

                                    ReportUntil.writeDataToReport(DescActivity.this, "\n\n\n");
                                    ReportUntil.writeDataToReport(DescActivity.this, ReportUntil.REPORT_FUNCTION + mFuncs[position]);
                                }

                            }
                        }).start();

                    }
                });
            }
        });
    }

    @Override
    protected void initListener() {
        backTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        commit_TV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("regCount", Config.getInstance(DescActivity.this).getRegCount() + "");
                if (!Config.getInstance(DescActivity.this).isSigned() &&
                        (Config.getInstance(DescActivity.this).getBondDevice() != null &&
                                Config.getInstance(DescActivity.this).getRegCount() < Config.TRYCOUNT) &&                         //还剩余试用次数
                        Config.getInstance(DescActivity.this).getRegCount() >= Config.TRYCOUNT - Config.TIP_TRYCOUNT)   //还剩 {@Config.TIP_TRYCOUNT} 次时提示
                {
                    int tryCount = Config.getInstance(DescActivity.this).getRegCount();
                    TipDialog dialog = new TipDialog.Builder(DescActivity.this)
                            .setTitle(getResources().getString(R.string.tip_title))
                            .setMessage(getResources().getString(R.string.tryUseTip) + " " + tryCount + " " +
                                    getResources().getString(R.string.tryUseTip2) + " " + (Config.TRYCOUNT - tryCount) + " " +
                                    getResources().getString(R.string.tryUseTip_msg))
                            .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                                @Override
                                public void onClick(Dialog dialogInterface, int index, String label) {
                                    dialogInterface.dismiss();
                                    onCommit();
                                }
                            })
                            .setNegativeClickListener(getResources().getString(R.string.cancel), new TipDialog.OnClickListener() {
                                @Override
                                public void onClick(Dialog dialogInterface, int index, String label) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .requestSystemAlert(true)
                            .build();
                    dialog.show();

                } else {
                    int count = Config.getInstance(DescActivity.this).getRegCount();
                    /*
                     * 使用次数到达提示次数
                     */
                    if (Config.getInstance(DescActivity.this).getRegCount() < Config.getInstance(DescActivity.this).getSetRegCount() &&
                            Config.getInstance(DescActivity.this).getRegCount() >= Config.getInstance(DescActivity.this).getSetRegCount() - 20
                    ) {

                        TipDialog dialog = new TipDialog.Builder(DescActivity.this)
                                .setTitle(getResources().getString(R.string.tip_title))
                                .setMessage(getResources().getString(R.string.regCount_1) + " " + count + " " +
                                        getResources().getString(R.string.regCount_2) + " " +
                                        (Config.getInstance(DescActivity.this).getSetRegCount() - count) + " " +
                                        getResources().getString(R.string.regCount_msg))
                                .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialogInterface, int index, String label) {
                                        dialogInterface.dismiss();
                                        onCommit();
                                    }
                                })
                                .setNegativeClickListener(getResources().getString(R.string.cancel), new TipDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialogInterface, int index, String label) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .requestSystemAlert(true)
                                .build();
                        dialog.show();
                    } else if (Config.getInstance(DescActivity.this).getRegCount() >= Config.getInstance(DescActivity.this).getSetRegCount()) {
                        /*
                         * 使用次数已经使用完
                         */
                        TipDialog dialog = new TipDialog.Builder(DescActivity.this)
                                .setTitle(getResources().getString(R.string.tip_title))
                                .setMessage(getResources().getString(R.string.regCount_1) + " " + count + " " +
                                        getResources().getString(R.string.regCount_Over))
                                .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialogInterface, int index, String label) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .requestSystemAlert(true)
                                .build();
                        dialog.show();
                    } else {
                        onCommit();
                    }

                }

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (mFuncListView.getVisibility() == View.VISIBLE) {
            new TipDialog.Builder(this).setTitle(getResources().getString(R.string.tip_title))
                    .setMessage(getString(R.string.exit_diagnosis))
                    .setPositiveClickListener(getResources().getString(R.string.tip_yes), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            mFuncListView.setVisibility(View.GONE);
                            helpRelativeLayout.setVisibility(View.VISIBLE);
                            dialogInterface.dismiss();

                            if (ReportFragment.getInstance() != null) {
                                ReportFragment.getInstance().refresh();
                            }
                            /*
                             * 记录文件已  _1.txt 结尾，自动上传记录
                             */
                            String fileName = UpDriver.getInstance(DescActivity.this).getPack().getFileName();
                            if (fileName.toLowerCase().endsWith("_1.txt")) {
                                ReportUntil.postReport(DescActivity.this, fileName);
                            }
                            /*
                             * 播放警告声音，直到点击确定按钮才停止
                             */
                            //SoundUtil.programExitSound(DescActivity.this);
                            SoundUtil.deviceExitTipSound(DescActivity.this);

                            new TipDialog.Builder(DescActivity.this).setTitle(getResources().getString(R.string.tip_title))
                                    .setImageDrawable(getResources().getDrawable(R.mipmap.user_warning, null))
                                    .setMessage(getString(R.string.pullout_device))
                                    .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                                        @Override
                                        public void onClick(Dialog dialogInterface, int index, String label) {
                                            SoundUtil.deviceExitTipSoundStop();
                                            dialogInterface.dismiss();

                                        }
                                    })
                                    .requestSystemAlert(true)
                                    .build().show();

                        }
                    })
                    .setNegativeClickListener(getResources().getString(R.string.tip_no), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            mFuncListView.setVisibility(View.VISIBLE);
                            helpRelativeLayout.setVisibility(View.GONE);
                            dialogInterface.dismiss();

                        }
                    }).requestSystemAlert(true).build().show();

            return;
        }
        super.onBackPressed();
    }


    private void registerConnect() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbService.ACTION_USB_CHECK_SUCCESS);
        filter.addAction(BluetoothService.ACTION_BLUETOOTH_CHECK_SUCCESS);
        registerReceiver(mReceiver, filter);
    }

    private void unregisterConnect() {
        unregisterReceiver(mReceiver);
    }


    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action != null) {
                switch (action) {
                    case UsbService.ACTION_USB_CHECK_SUCCESS:
                    case BluetoothService.ACTION_BLUETOOTH_CHECK_SUCCESS:
                        if (tipDialog != null) {
                            tipDialog.dismiss();
                        }
                        if (loadDialog != null && loadDialog.isShowing()) {
                            loadDialog.dismiss();
                            onCommit();
                        }
                        unregisterConnect();
                        break;
                }
            }
        }
    };
}
