package com.west.develop.westapp.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.west.develop.westapp.Protocol.Drivers.RunningDriver;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.Utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Develop0 on 2018/5/22.
 */

public class BackupDialog extends Dialog implements View.OnClickListener {
    public static final int READ_TIMEOUT_SECOND = 8;

    public static final int RESULT_OK = 0;
    public static final int RESULT_NOT_COMPLETE = 1;
    public static final int RESULT_OVER = 2;
    public static final int RESULT_READ_ERROR = 3;
    public static final int RESULT_TIMEOUT = 4;

    private static final int STEP_UP_NEW = 1;
    private static final int STEP_UP_FETCH = 2;
    private static final int STEP_UP_FINISH = 3;
    private static final int STEP_UP_TIMEOUT = 4;


    private static BackupDialog instance;

    public static BackupDialog getInstance() {
        return instance;
    }


    public static BackupDialog newInstance(Context context, int length, OnUPFileListener listener) {
        instance = new BackupDialog(context);
        instance.length = length;
        instance.mBuffer = new byte[length];
        instance.mUploadListener = listener;
        return instance;
    }

    private Handler mHandler = new Handler();

    private Context mContext;

    private int length;

    int uploadIndex = 0;
    byte[] mBuffer;


    private int mSTEP;
    //private static final int

    public static final String BACKUP_FILE_TYPE = ".168bak";

    private OnUPFileListener mUploadListener;

    /**
     * MODE_UPLOAD
     */
    LinearLayout UP_Step1_Layout;
    EditText UP_FileName_ET;

    LinearLayout UP_Step2_Layout;
    TextView UP_Progress_TV;
    LinearLayout UP_Step3_Layout;
    TextView UP_Result_TV;

    Button UP_Commit_BTN;
    Button UP_Cancel_BTN;


    private BackupDialog(Context context) {
        super(context);
        mContext = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置 Dialog 布局
        setContentView(R.layout.dialog_backup);

        Window dialogWindow = getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int)mContext.getResources().getDimension(R.dimen.width_backupdialog);
        dialogWindow.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);



        dialogWindow.setAttributes(lp);
        //点击边缘无效
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
    }


    private void initView() {
        UP_Step1_Layout = (LinearLayout) findViewById(R.id.Upload_Step1);
        UP_Step2_Layout = (LinearLayout) findViewById(R.id.Upload_Step2);
        UP_Step3_Layout = (LinearLayout) findViewById(R.id.UPload_Step3);
        UP_FileName_ET = (EditText) findViewById(R.id.Input_FileName);
        UP_Progress_TV = (TextView) findViewById(R.id.Upload_Progress);
        UP_Result_TV = (TextView) findViewById(R.id.UPload_Result_TV);

        UP_Commit_BTN = (Button)findViewById(R.id.UPload_Commit_BTN);
        UP_Cancel_BTN = (Button)findViewById(R.id.UPload_Cancel_BTN);


        UP_Commit_BTN.setOnClickListener(this);
        UP_Cancel_BTN.setOnClickListener(this);


        UP_FileName_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String str = s.toString();
                if (str.indexOf("\\") >= 0 || str.indexOf("/") >= 0) {
                    int indexRevert = str.indexOf("\\");
                    int index = str.indexOf("/");
                    int selection = UP_FileName_ET.getSelectionStart();
                    if ((indexRevert >= 0 && indexRevert <= selection) || (index >= 0 && index <= selection)) {
                        selection--;
                    }

                    str = str.replaceAll("[/\\\\]", "");
                    UP_FileName_ET.setText(str);
                    UP_FileName_ET.setSelection(selection);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        setStep(STEP_UP_NEW);
    }


    private void timeOut(){
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                setStep(STEP_UP_TIMEOUT);
            }
        });
    }

    /**
     * 设置当前步骤
     * @param step
     */
    private void setStep(int step) {
        if (step == STEP_UP_NEW) {
            UP_Cancel_BTN.setVisibility(View.VISIBLE);
            UP_Commit_BTN.setVisibility(View.VISIBLE);
            UP_Step1_Layout.setVisibility(View.VISIBLE);
            UP_Step2_Layout.setVisibility(View.GONE);
            UP_Step3_Layout.setVisibility(View.GONE);
        } else if (step == STEP_UP_FETCH) {
            hideKeyBoard(UP_FileName_ET);
            if(!TimeoutThread.isRunning()){
                TimeoutThread.Start();
            }
            UP_Cancel_BTN.setVisibility(View.VISIBLE);
            UP_Commit_BTN.setVisibility(View.GONE);
            UP_Step1_Layout.setVisibility(View.GONE);
            UP_Step2_Layout.setVisibility(View.VISIBLE);
            UP_Step3_Layout.setVisibility(View.GONE);
        } else if (step == STEP_UP_FINISH) {
            hideKeyBoard(UP_FileName_ET);
            UP_Step1_Layout.setVisibility(View.GONE);
            UP_Step2_Layout.setVisibility(View.GONE);
            UP_Step3_Layout.setVisibility(View.VISIBLE);

            UP_Commit_BTN.setVisibility(View.VISIBLE);
            UP_Cancel_BTN.setVisibility(View.GONE);
            if (uploadIndex == length) {
                saveFile();
                UP_Result_TV.setText(mContext.getString(R.string.file_Result_OK));
                destory();
            } else if (uploadIndex < length) {
                UP_Result_TV.setText(mContext.getString(R.string.file_Result_NotComplete));
            } else {
                UP_Result_TV.setText(mContext.getString(R.string.file_Result_Over));
            }
        }
        else if(step == STEP_UP_TIMEOUT){
            hideKeyBoard(UP_FileName_ET);
            UP_Commit_BTN.setVisibility(View.VISIBLE);
            UP_Cancel_BTN.setVisibility(View.VISIBLE);
            UP_Step1_Layout.setVisibility(View.GONE);
            UP_Step2_Layout.setVisibility(View.GONE);
            UP_Step3_Layout.setVisibility(View.VISIBLE);

            UP_Result_TV.setText(mContext.getString(R.string.file_Result_TimeOut));
        }


        mSTEP = step;
    }

    /**
     * 验证文件名
     */
    private void confirmFileName() {
        String fileName = UP_FileName_ET.getText().toString();
        if (fileName.isEmpty()) {
            Toast.makeText(mContext, mContext.getString(R.string.toast_InputFile), Toast.LENGTH_SHORT).show();
            return;
        }

        //TimeoutThread.Start();
        if (mUploadListener != null) {
            mUploadListener.onName(fileName);
        }

        setStep(STEP_UP_FETCH);
    }

    /**
     * 刷新上传进度
     */
    private void onUPProgress() {
        int progress = (int) (((float) uploadIndex * 100) / length);
        if (progress >= 0 && progress <= 100) {
            UP_Progress_TV.setText(progress + "");
        }

        if(uploadIndex == length){
            finishUP();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.UPload_Commit_BTN:
                if(mSTEP == STEP_UP_NEW){
                    confirmFileName();
                }
                else if(mSTEP == STEP_UP_FINISH){
                    if (uploadIndex == length) {
                        if (mUploadListener != null) {
                            mUploadListener.onFinish(RESULT_OK, mBuffer);
                        }
                    } else if (uploadIndex < length) {
                        if (mUploadListener != null) {
                            mUploadListener.onFinish(RESULT_NOT_COMPLETE, null);
                        }
                    }
                    else if(uploadIndex > length) {
                        if (mUploadListener != null) {
                            mUploadListener.onFinish(RESULT_OVER, null);
                        }
                    }
                }
                else if(mSTEP == STEP_UP_TIMEOUT){
                    RunningDriver.getInstance().reCountTimeout();
                    setStep(STEP_UP_FETCH);
                }
                break;
            case R.id.UPload_Cancel_BTN:
                if(mSTEP == STEP_UP_NEW){
                    if (mUploadListener != null) {
                        mUploadListener.onCancel();
                    }
                }
                else if(mSTEP == STEP_UP_FETCH){
                    TimeoutThread.Stop();
                    if (mUploadListener != null) {
                        mUploadListener.onCancel();
                    }
                }else if(mSTEP == STEP_UP_TIMEOUT){
                    if (mUploadListener != null) {
                        mUploadListener.onFinish(RESULT_TIMEOUT, null);
                    }
                }
                break;
        }
    }


    /**
     * 接收到上传的数据
     * @param buff
     */
    public void inUPData(byte[] buff) {
        setStep(STEP_UP_FETCH);
        for (int i = 2; i < buff.length - 2; i++) {
            if (uploadIndex >= length) {
                break;
            }
            mBuffer[uploadIndex] = buff[i];
            uploadIndex++;
        }

        onUPProgress();
    }

    /**
     * 接收完成
     */
    public void finishUP() {
        TimeoutThread.Stop();

        setStep(STEP_UP_FINISH);
    }


    /**
     * 隐藏软键盘
     *
     * @param view
     */
    private void hideKeyBoard(View view) {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void saveFile() {
        String fileName = UP_FileName_ET.getText().toString() + BACKUP_FILE_TYPE;
        fileName = System.currentTimeMillis() + "_" + fileName;
        File destFile = new File(FileUtil.getBackUPRoot(mContext), fileName);

        //长度不一致
        if (uploadIndex != length) {
            return;
        }

        if (!destFile.exists()) {
            destFile.getParentFile().mkdirs();
        }

        try {
            FileOutputStream fos = new FileOutputStream(destFile);
            fos.write(mBuffer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }


    }

    /**
     * 销毁
     */
    public static void destory() {
        TimeoutThread.Stop();
        if (instance != null) {
            instance.dismiss();
            instance = null;
        }
    }


    public interface OnUPFileListener {
        void onName(String filename);
        void onFinish(int resultCode, byte[] buffer);
        void onCancel();
    }




    private static class TimeoutThread extends Thread{

        private static TimeoutThread instance;

        private boolean run = false;
        private static TimeoutThread getInstance(){
            return instance;
        }

        public static void Start(){
            if(instance != null){
                if(instance.run){
                    return;
                }
                Stop();
            }

            instance = new TimeoutThread();
            instance.start();
        }

        public static void Stop(){
            if(instance != null) {
                instance.run = false;
                instance.interrupt();
                instance = null;
            }

        }

        public static boolean isRunning(){
            return instance != null && instance.run;
        }
        @Override
        public void run() {
            run = true;

            while(run){
                try{
                    Thread.sleep(2000);
                    int timeout = RunningDriver.getInstance().getTimeout();
                    Log.e("timeout",timeout + "");
                    /*if(timeout > READ_TIMEOUT_SECOND * 1000){
                        BackupDialog.getInstance().timeOut();
                        break;
                    }*/
                }
                catch (InterruptedException ex){
                    break;
                }
            }
            run = false;
        }
    }
}
