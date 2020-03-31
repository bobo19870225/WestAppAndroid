package com.west.develop.westapp.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.west.develop.westapp.Dialog.DialogAdapter.BackupAdapter;
import com.west.develop.westapp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by Develop0 on 2018/5/18.
 */

public class FileDialog extends Dialog implements View.OnClickListener {
    public static final int PAGE_SIZE = 0x0200;//读取备份文件每一页的大小

    public static final int RESULT_OK = 0;
    public static final int RESULT_NOT_COMPLETE = 1;
    public static final int RESULT_OVER = 2;
    public static final int RESULT_READ_ERROR = 3;

    private static final int STEP_DOWN_NEW = 8;
    //private static final int STEP_DOWN_STATUS = 9;
    private static final int STEP_DOWN_SEND = 10;

    private static final int STEP_DOWN_STATUS_READY = 0;
    private static final int STEP_DOWN_STATUS_FAILED = 1;
    private static final int STEP_DOWN_STATUS_SUCCESS = 2;
    private static final int STEP_DOWN_STATUS_MISMATCH = 3;

    private int mDOWN_Status = STEP_DOWN_STATUS_READY;


    private int mSTEP;
    //private static final int

    public static final String BACKUP_FILE_TYPE = ".168bak";

    private static FileDialog instance;

    public static FileDialog getInstance() {
        return instance;
    }

    public static FileDialog newDownInstance(Context context,byte flag, OnLoadFileListener listener) {
        instance = new FileDialog(context);
        instance.flag = flag;
        instance.mLoadListener = listener;
        instance.backupAdapter = new BackupAdapter(instance.mContext,flag);
        return instance;
    }

    private Context mContext;

    private byte flag;

    private byte[] mBuffer;

    private OnLoadFileListener mLoadListener;

    /**
     * MODEL_DOWNLOAD
     *
     * @param savedInstanceState
     */
    LinearLayout DOWN_Step1_Layout;
    TextView DOWN_List_NONE;
    ListView DOWN_File_LV;
    //LinearLayout DOWN_Step2_Layout;
    //TextView DOWN_Progress_TV;
    /*LinearLayout DOWN_Step3_Layout;
    TextView DOWN_Step3_Status_TV;*/
    Button DOWN_Negative_BTN;
    Button DOWN_Positive_BTN;

    BackupAdapter backupAdapter;// = new BackupAdapter(mContext,flag);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置 Dialog 布局

        setContentView(R.layout.dialog_downfile);
        //点击边缘无效
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
    }

    private FileDialog(Context context) {
        super(context);
        mContext = context;
    }

    private void initView() {
        DOWN_Step1_Layout = (LinearLayout) findViewById(R.id.Load_Step1_Layout);
        DOWN_List_NONE = (TextView) findViewById(R.id.Load_List_NONE_Tip);
        DOWN_File_LV = (ListView) findViewById(R.id.Load_backUp_ListView);
        //DOWN_Step2_Layout = (LinearLayout) findViewById(R.id.Load_Step2_Layout);
        //DOWN_Progress_TV = (TextView)findViewById(R.id.Load_Progress_TV);
      /*  DOWN_Step3_Layout = (LinearLayout) findViewById(R.id.Load_Step3_Layout);
        DOWN_Step3_Status_TV = (TextView) findViewById(R.id.Load_Step3_Status);
*/
        DOWN_Negative_BTN = (Button) findViewById(R.id.DOWN_Negative_BTN);
        DOWN_Positive_BTN = (Button) findViewById(R.id.DOWN_Positive_BTN);

        //backupAdapter = new BackupAdapter(mContext,flag);
        DOWN_File_LV.setAdapter(backupAdapter);

        if (backupAdapter.getCount() > 0) {
            DOWN_List_NONE.setVisibility(View.GONE);
        } else {
            DOWN_List_NONE.setVisibility(View.VISIBLE);
        }

        findViewById(R.id.DOWN_Negative_BTN).setOnClickListener(this);
        findViewById(R.id.DOWN_Positive_BTN).setOnClickListener(this);

        setStep(STEP_DOWN_NEW);
    }

    /**
     * 设置当前步骤
     * @param step
     */
    private void setStep(int step) {

        if (step == STEP_DOWN_NEW) {
           /* if(!isShowing()) {
                show();
            }*/
            DOWN_Negative_BTN.setVisibility(View.VISIBLE);
            DOWN_Positive_BTN.setVisibility(View.VISIBLE);

            DOWN_Step1_Layout.setVisibility(View.VISIBLE);
           /* DOWN_Step2_Layout.setVisibility(View.GONE);
            DOWN_Step3_Layout.setVisibility(View.GONE);*/
        }else if (step == STEP_DOWN_SEND) {
            dismiss();
           /* DOWN_Negative_BTN.setVisibility(View.VISIBLE);
            DOWN_Positive_BTN.setVisibility(View.GONE);

            DOWN_Step1_Layout.setVisibility(View.GONE);*/
        }
            /*DOWN_Step2_Layout.setVisibility(View.VISIBLE);
           // DOWN_Step3_Layout.setVisibility(View.GONE);*//*
        }/*  else if (step == STEP_DOWN_STATUS) {
            DOWN_Step1_Layout.setVisibility(View.GONE);
            *//*DOWN_Step2_Layout.setVisibility(View.GONE);

            DOWN_Step3_Layout.setVisibility(View.VISIBLE);*//*
            if (mDOWN_Status == STEP_DOWN_STATUS_READY) {
                DOWN_Negative_BTN.setVisibility(View.VISIBLE);
                DOWN_Positive_BTN.setVisibility(View.GONE);

                //DOWN_Step3_Status_TV.setText(R.string.file_DOWN_ready);

            } else if (mDOWN_Status == STEP_DOWN_STATUS_FAILED) {
                DOWN_Positive_BTN.setVisibility(View.VISIBLE);
                DOWN_Negative_BTN.setVisibility(View.GONE);
                //DOWN_Step3_Status_TV.setText(R.string.file_DOWN_failed);
            } else if (mDOWN_Status == STEP_DOWN_STATUS_SUCCESS) {
                DOWN_Positive_BTN.setVisibility(View.VISIBLE);
                DOWN_Negative_BTN.setVisibility(View.GONE);
               // DOWN_Step3_Status_TV.setText(R.string.file_DOWN_sucess);
            } else if (mDOWN_Status == STEP_DOWN_STATUS_MISMATCH) {
                DOWN_Negative_BTN.setVisibility(View.GONE);
                DOWN_Positive_BTN.setVisibility(View.VISIBLE);
               // DOWN_Step3_Status_TV.setText(R.string.file_DOWN_mismatch);
            }
        }*/

        mSTEP = step;
    }


    public void LENGH_MATCH() {
        setStep(STEP_DOWN_SEND);
        /*if (match) {
            mDOWN_Status = STEP_DOWN_STATUS_READY;
        } else {
            mDOWN_Status = STEP_DOWN_STATUS_MISMATCH;
        }
        setStep(STEP_DOWN_STATUS);*/
    }


    public void BACKUP_START(int addr, int length) {
        File file = backupAdapter.getSelectedItem().getFile();

        int fileSize = (int) file.length();

        if (addr < fileSize && (length <= 0 || length + addr > fileSize)) {
            length = fileSize - addr;
        }

        ReadBACKUP_BUF(addr, length);

    }

   /* public void BACKUP_FINISH(boolean success) {
        *//*if (success) {
            mDOWN_Status = STEP_DOWN_STATUS_SUCCESS;
        } else {
            mDOWN_Status = STEP_DOWN_STATUS_FAILED;
        }
        setStep(STEP_DOWN_STATUS);*//*
    }*/

    private void ReadBACKUP_BUF(int addr, int length) {
        File file = backupAdapter.getSelectedItem().getFile();
        if (file != null && file.exists()) {
            mBuffer = new byte[length];

            try {
                FileInputStream fis = new FileInputStream(file);
                fis.skip(addr);

                int len = 0;
                if(addr < file.length()){
                    len = fis.read(mBuffer);
                }
                fis.close();
                if (len < mBuffer.length) {
                    for (int i = len; i < mBuffer.length; i++) {
                        mBuffer[i] = (byte) 0xFF;
                    }
                }

                //setStep(STEP_DOWN_SEND);
                if (mLoadListener != null) {
                    mLoadListener.onRead(mBuffer);
                }
                return;
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        if (mLoadListener != null) {
            mLoadListener.onFinish(RESULT_READ_ERROR);
        }
    }


    public void onSelect(){
        if(backupAdapter.getSelectedItem() == null){
            Toast.makeText(mContext, mContext.getString(R.string.toast_SelectFile), Toast.LENGTH_SHORT).show();
        }
        if (mLoadListener != null) {
            mLoadListener.onSelect(backupAdapter.getSelectedItem().getFile());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.DOWN_Negative_BTN:
                if (mLoadListener != null) {
                    mLoadListener.onCancel();
                }
                break;
            case R.id.DOWN_Positive_BTN:
                if (mSTEP == STEP_DOWN_NEW) {
                    if (backupAdapter.getSelectedItem() == null) {
                        Toast.makeText(mContext, mContext.getString(R.string.toast_SelectFile), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    onSelect();

/*
                    mDOWN_Status = STEP_DOWN_STATUS_READY;
                    setStep(STEP_DOWN_STATUS);*/
                } /*else if (mSTEP == STEP_DOWN_SEND) {
                    *//*if(mLoadListener != null){
                        mLoadListener.onCancel();
                    }*//*
                } else if (mSTEP == STEP_DOWN_STATUS) {
                    if (mDOWN_Status == STEP_DOWN_STATUS_READY) {

                    } else if (mDOWN_Status == STEP_DOWN_STATUS_FAILED) {

                    } else if (mDOWN_Status == STEP_DOWN_STATUS_SUCCESS) {
                        if (mLoadListener != null) {
                            mLoadListener.onFinish(RESULT_OK);
                        }
                    } else if (mDOWN_Status == STEP_DOWN_STATUS_MISMATCH) {

                    }
                }*/
                break;
        }
    }

    /**
     * 销毁
     */
    public static void destory() {
        if (instance != null) {
            instance.dismiss();
            instance = null;
        }
    }

    public interface OnLoadFileListener {
        void onSelect(File file);

        void onFinish(int resultCode);

        void onCancel();

        void onRead(byte[] buffer);
    }

}
