package com.west.develop.westapp.UI.Activity.Diagnosis;

import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.west.develop.westapp.Bean.NCarBean;
import com.west.develop.westapp.Common.BaseSerialPort;
import com.west.develop.westapp.Communicate.Service.BluetoothService;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.ConnectStatus;
import com.west.develop.westapp.Dialog.LoadDialog;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.Protocol.Drivers.RunningDriver;
import com.west.develop.westapp.Protocol.Drivers.UpDriver;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.Diagnosis.DiagnosisAPI;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.Utils.ReportUntil;
import com.west.develop.westapp.Tools.Utils.SoundUtil;
import com.west.develop.westapp.Tools.constant.RequestCodeConstant;
import com.west.develop.westapp.UI.Adapter.Diagnosis.DiagnosisAdapter;
import com.west.develop.westapp.UI.base.BaseActivity;

import java.io.File;
import java.util.ArrayList;

/**
 * 诊断程序选择
 */
public class DiagnosisActivity extends BaseActivity {
    public static final String kCarBean = "kCarBean";
    public static final String kISDebug = "kISDebug";
    public static final int MSG_DIALOG_SHOW = 2;

    private boolean isDebug = false;
    private NCarBean mCarBean;


    private TextView backTv;
    //    private TextView totleTv;
    private TextView title;

    private ListView mListView;
    private DiagnosisAdapter mAdapter;
    private ArrayList<File> mFiles;

    private String mProgramRoot;
    private String mCurrentDir;


    @Override
    protected View getContentView() {
        return this.getLayoutInflater().inflate(R.layout.activity_diagnosis, null);
    }

    @Override
    protected void initView() {
        backTv = findViewById(R.id.car_back);
        title = findViewById(R.id.car_title);    //标题
        title.setText(R.string.main_diagnosis);

        mListView = findViewById(R.id.list_Diagnosis);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                File file = mFiles.get(position);
                /*
                 * 选择程序
                 */
                if (!isDebug) {
                    /*
                     * 正常模式
                     */
                    File fileName = null;
                    File[] files = file.listFiles();
                    if (files != null) {
                        for (File value : files) {
                            if (value.getName().toLowerCase().contains(".bin")) {
                                fileName = value;
                            }
                        }
                    }
                    if (file.isDirectory() && fileName != null) {
                        Intent intent = new Intent(DiagnosisActivity.this, DescActivity.class);
                        intent.putExtra(RunActivity.kProgType, RunActivity.TYPE_RELEASE);
                        intent.putExtra(RunActivity.kStartFile, fileName.getPath());
                        startActivity(intent);
                        return;
                    }
                    setTitlePath(file, FileUtil.PROGRAM_ROOT);//设置标题栏
                } else {
                    /*
                     * 本地调试模式
                     */
                    if (file.getName().toLowerCase().endsWith(".bin")) {
                        mProFile = file;
                        onCommit();
                        return;
                    }

                    /*
                     * 调试模式下点击bin文件刷新的listview，点击item
                     */
                    if (isBin) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                BaseSerialPort port = null;
                                if (ConnectStatus.getInstance(DiagnosisActivity.this).getUSBPort() != null) {
                                    port = ConnectStatus.getInstance(DiagnosisActivity.this).getUSBPort();
                                } else if (ConnectStatus.getInstance(DiagnosisActivity.this).getBTPort() != null) {
                                    port = ConnectStatus.getInstance(DiagnosisActivity.this).getBTPort();
                                }
                                if (port == null) {
                                    return;
                                } else {
                                    UpDriver.getInstance(DiagnosisActivity.this).initPort(port);
                                }
                                if (UpDriver.getInstance(DiagnosisActivity.this).RUNFun(position + 1, false)) {
                                    RunningDriver.init(UpDriver.getInstance(DiagnosisActivity.this).getPack());
                                    RunningDriver.getInstance().initPort(UpDriver.getInstance(DiagnosisActivity.this).getPort());
                                    Intent intent = getIntent();
                                    intent.setClass(DiagnosisActivity.this, RunActivity.class);
                                    intent.putExtra(RunActivity.kFuncName, str[position]);
                                    intent.putExtra(RunActivity.kProgType, RunActivity.TYPE_DEBUG);
                                    intent.putExtra(RunActivity.kStartFile, mProFile.getPath());
                                    startActivityForResult(intent, RequestCodeConstant.CODE_RUN_ACTIVITY);

                                    ReportUntil.writeDataToReport(DiagnosisActivity.this, "\n\n\n");
                                    ReportUntil.writeDataToReport(DiagnosisActivity.this, ReportUntil.REPORT_FUNCTION + str[position]);
                                }

                            }
                        }).start();

                        return;

                    }
                    setTitlePath(file, FileUtil.DEBUG_ROOT);//设置标题栏
                }

                /**
                 * 点击目录
                 */
                if (file.isDirectory()) {
                    mCurrentDir = mCurrentDir + "/" + mFiles.get(position).getName();
                    refreshFiles();
                }
            }
        });
    }

    File mProFile;

    private void onCommit() {
        if (Config.getInstance(DiagnosisActivity.this).getBondDevice() != null) {
            if (ConnectStatus.getInstance(this).getUSBPort() != null) {
                Log.e("onCommit", "连接设备");
                DiagnosisAPI.init(this);
                DiagnosisAPI.getInstance().startWithFile(mProFile, true);
            } else if (ConnectStatus.getInstance(this).getBTPort() != null) {
                DiagnosisAPI.init(this);
                DiagnosisAPI.getInstance().startWithFile(mProFile, true);
            } else {
                Toast.makeText(this, getString(R.string.device_not_connect), Toast.LENGTH_SHORT).show();

                /**
                 * 判断设备是否可用蓝牙,蓝牙开关打开
                 */
                if (BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    if (ConnectStatus.getInstance(this).getBTPort() == null) {
                        mHandler.sendEmptyMessage(MSG_DIALOG_SHOW);
                    }
                } else {
                    new TipDialog.Builder(DiagnosisActivity.this).setMessage(getResources().getString(R.string.tip_Open_Bluetooth))
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
    }

    LoadDialog loadDialog;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MSG_DIALOG_SHOW:
                    final TipDialog tipDialog = new TipDialog.Builder(DiagnosisActivity.this).setTitle(getResources().getString(R.string.bt_conn_tip))
                            .setMessage(getResources().getString(R.string.bt_conn_tip_message))
                            .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                                @Override
                                public void onClick(Dialog dialogInterface, int index, String label) {
                                    dialogInterface.dismiss();
                                    loadDialog = new LoadDialog.Builder(DiagnosisActivity.this)
                                            .setTitle("正在连接蓝牙，请稍等...")
                                            .setCancel(getResources().getString(R.string.cancel), new LoadDialog.OnClickListener() {
                                                @Override
                                                public void onClick(Dialog dialog) {
                                                    loadDialog.dismiss();
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
                    BluetoothService.getInstance().setConnectCallback(new BluetoothService.ConnectCallback() {
                        @Override
                        public void onFinish(boolean success) {
                            if (success) {
                                if (tipDialog != null) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            tipDialog.dismiss();
                                            if (loadDialog != null && loadDialog.isShowing()) {
                                                loadDialog.dismiss();
                                                onCommit();
                                            }
                                        }
                                    });

                                }
                            }
                        }
                    });
                    break;
            }
        }
    };

    boolean result_exit = false;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodeConstant.CODE_SIGN_ACTIVITY && resultCode == RESULT_OK) {
            onCommit();
        }

        if (requestCode == RequestCodeConstant.CODE_RUN_ACTIVITY) {
            if (resultCode == RunActivity.RESULT_EXIT) {
                final File currentFile = new File(mCurrentDir);
                File programeRootFile = new File(mProgramRoot);
                if (!currentFile.getPath().equals(programeRootFile.getPath())) {
                    String parentPath = currentFile.getParent();
                    setTitlePath(new File(parentPath), FileUtil.DEBUG_ROOT);//设置标题栏
                    mCurrentDir = parentPath;
                    isBin = false;
                    refreshFiles();
                } else {
                    isBin = false;
                    result_exit = true;
                    onBackPressed();
                }
            }
        }
    }

    String[] str;
    boolean isBin = false;

    //程序下载成功后，刷新列表
    public void refreshIniFile(final File file) {

        mListView.post(new Runnable() {
            @Override
            public void run() {

                mFiles.clear();
                str = getResources().getStringArray(R.array.menu_Debug);
                for (int i = 0; i < str.length; i++) {
                    File file = new File(str[i]);
                    mFiles.add(file);
                }

                mAdapter.notifyDataSetChanged();
                setTitlePath(file, FileUtil.DEBUG_ROOT);//设置标题栏
                isBin = true;
            }
        });
    }


    @Override
    protected void initData() {
        isDebug = getIntent().getBooleanExtra(kISDebug, false);
        //非本地调试     即运行已完成的程序
        if (!isDebug) {
            String beanJson = getIntent().getStringExtra(kCarBean);
            try {
                Gson gson = new Gson();
                mCarBean = gson.fromJson(beanJson, NCarBean.class);
            } catch (Exception ex) {
                finish();
            }
            if (mCarBean == null) {
                finish();
            }
            mProgramRoot = FileUtil.getProgramRoot(this) + mCarBean.getBinRoot();
            String totleName = mProgramRoot.substring(mProgramRoot.lastIndexOf("/") + 1);
            title.setText(totleName);

        } else {
            title.setText(R.string.setting_test);
            mProgramRoot = FileUtil.getDebugRoot(this);

            File debugRoot = new File(mProgramRoot);
            if (!debugRoot.exists()) {
                debugRoot.mkdirs();
            }
        }

        if (mFiles == null) {
            mFiles = new ArrayList<>();
        }

        mAdapter = new DiagnosisAdapter(this, mFiles, isDebug);
        mListView.setAdapter(mAdapter);

        mCurrentDir = mProgramRoot;
        refreshFiles();

    }

    /**
     * 刷新列表
     */
    public void refreshFiles() {
        File programRoot = new File(mCurrentDir);
        mFiles.clear();
        if (programRoot.exists()) {
            File[] files = programRoot.listFiles();

            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (files[i].getName().toLowerCase().endsWith(".bin") || files[i].isDirectory()) {
                        mFiles.add(files[i]);
                    }
                }
            }
        }
        mAdapter.notifyDataSetChanged();
    }

    @Override
    protected void initListener() {
        backTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void onBackPressed() {

        /**
         * 未达到 该车品牌程序根目录
         * 显示上一级
         */
        final File currentFile = new File(mCurrentDir);
        File programeRootFile = new File(mProgramRoot);
        if (!currentFile.getPath().equals(programeRootFile.getPath())) {
            String parentPath = currentFile.getParent();
            if (!isDebug) {
                setTitlePath(new File(parentPath), FileUtil.PROGRAM_ROOT);//设置标题栏
                mCurrentDir = parentPath;
                refreshFiles();
            } else if (isBin) {//调试模式下
                new TipDialog.Builder(this).setTitle(getResources().getString(R.string.tip_title))
                        .setMessage(getString(R.string.exit_diagnosis))
                        .setPositiveClickListener(getResources().getString(R.string.tip_yes), new TipDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialogInterface, int index, String label) {
                                String parentPath = currentFile.getPath();
                                setTitlePath(new File(parentPath), FileUtil.DEBUG_ROOT);//设置标题栏
                                isBin = false;
                                mCurrentDir = parentPath;
                                refreshFiles();
                                dialogInterface.dismiss();

                                /**
                                 * 播放警告声音，直到点击确定按钮才停止
                                 */
                                //SoundUtil.programExitSound(DiagnosisActivity.this);
                                SoundUtil.deviceExitTipSound(DiagnosisActivity.this);
                                new TipDialog.Builder(DiagnosisActivity.this).setTitle(getResources().getString(R.string.tip_title))
                                        .setImageDrawable(getResources().getDrawable(R.mipmap.user_warning, null))
                                        .setMessage(getString(R.string.pullout_device))
                                        .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                                            @Override
                                            public void onClick(Dialog dialogInterface, int index, String label) {
                                                SoundUtil.deviceExitTipSoundStop();//停止声音
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
                                String parentPath = currentFile.getPath();
                                setTitlePath(new File(parentPath), FileUtil.DEBUG_ROOT);//设置标题栏
                                mCurrentDir = parentPath;
                                refreshIniFile(mProFile);
                                dialogInterface.dismiss();

                            }
                        }).requestSystemAlert(true).build().show();
            } else {
                setTitlePath(new File(parentPath), FileUtil.DEBUG_ROOT);//设置标题栏
                mCurrentDir = parentPath;
                refreshFiles();
            }
            return;
        } else {
            // 针对于bin文件直接放在第一目录下
            if (isDebug && isBin) {
                new TipDialog.Builder(this).setTitle(getResources().getString(R.string.tip_title))
                        .setMessage(getString(R.string.exit_diagnosis))
                        .setPositiveClickListener(getResources().getString(R.string.tip_yes), new TipDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialogInterface, int index, String label) {
                                String parentPath = currentFile.getPath();
                                setTitlePath(new File(parentPath), FileUtil.DEBUG_ROOT);//设置标题栏
                                isBin = false;
                                mCurrentDir = parentPath;
                                refreshFiles();
                                dialogInterface.dismiss();

                                /**
                                 * 播放警告声音，直到点击确定按钮才停止
                                 */
                                //SoundUtil.programExitSound(DiagnosisActivity.this);
                                SoundUtil.deviceExitTipSound(DiagnosisActivity.this);
                                new TipDialog.Builder(DiagnosisActivity.this).setTitle(getResources().getString(R.string.tip_title))
                                        .setImageDrawable(getResources().getDrawable(R.mipmap.user_warning, null))
                                        .setMessage(getString(R.string.pullout_device))
                                        .setPositiveClickListener(getResources().getString(R.string.Sure), new TipDialog.OnClickListener() {
                                            @Override
                                            public void onClick(Dialog dialogInterface, int index, String label) {
                                                SoundUtil.deviceExitTipSoundStop();//停止声音
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
                                String parentPath = currentFile.getPath();
                                setTitlePath(new File(parentPath), FileUtil.DEBUG_ROOT);//设置标题栏
                                mCurrentDir = parentPath;
                                refreshIniFile(mProFile);
                                dialogInterface.dismiss();

                            }
                        }).requestSystemAlert(true).build().show();
                return;
            } else if (isDebug && !isBin && result_exit) {
                result_exit = false;
                String parentPath = currentFile.getPath();
                setTitlePath(new File(parentPath), FileUtil.DEBUG_ROOT);//设置标题栏
                mCurrentDir = parentPath;
                refreshFiles();
                return;
            }
        }

       /* String parentPath = currentFile.getParent();
        if (!isDebug) {
            setTitlePath(new File(parentPath), FileUtil.PROGRAM_ROOT);//设置标题栏
            mCurrentDir = parentPath;
            refreshFiles();
            return;
        }*/
        super.onBackPressed();
    }


    /***
     * 点击item时 回退时 标题栏的名称
     *
     * @param file
     * @param programRoot
     */
    private void setTitlePath(File file, String programRoot) {
        String fileStr = file.toString();
        int index = fileStr.indexOf(programRoot);
        String fileTitle = "";
        if (programRoot.equals(FileUtil.PROGRAM_ROOT)) {
            fileTitle = fileStr.substring(index + programRoot.length());
            if (fileTitle.contains("_v")) {
                String fileParent = new File(fileTitle).getParent();
                fileTitle = fileParent.substring(0, fileParent.lastIndexOf("_v"));
            }
        } else {
            String name = getString(R.string.setting_test);
            if (index <= 0) {
                fileTitle = name;
            } else {
                fileTitle = name + "/" + fileStr.substring(index + FileUtil.DEBUG_ROOT.length());
            }
        }
        fileTitle = fileTitle.replace("/", ">");
        title.setText(fileTitle);
    }
}
