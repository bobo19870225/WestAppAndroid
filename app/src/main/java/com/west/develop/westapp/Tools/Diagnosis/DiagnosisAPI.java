package com.west.develop.westapp.Tools.Diagnosis;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.west.develop.westapp.Application.MyApplication;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.LoadDialog;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.Download.Threads.FWDownloadThread;
import com.west.develop.westapp.Tools.Utils.ReportUntil;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.Utils.WifiUtil;
import com.west.develop.westapp.UI.Activity.Diagnosis.DescActivity;
import com.west.develop.westapp.UI.Activity.Diagnosis.DiagnosisActivity;
//import com.west.develop.westapp.DisplayAndOperate.Dialog.LoadDialog;
import com.west.develop.westapp.Dialog.ConnectStatus;
//import com.west.develop.westapp.DisplayAndOperate.DisplayAndOperateAPI;
import com.west.develop.westapp.Protocol.Drivers.BaseDriver;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Protocol.Drivers.UpDriver;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Develop0 on 2017/9/6.
 */

public class DiagnosisAPI {
    private Context mContext;

    private static DiagnosisAPI instance;

    private static OnSelectModeListener mSelectListener;


    /**
     * 正在运行对话框
     */
    private LoadDialog mLoadDialog;


    public static void init(Context context){
        instance = new DiagnosisAPI(context);
        //DisplayAndOperateAPI.init(context);
    }

    public static DiagnosisAPI getInstance(){
        return instance;
    }

    private DiagnosisAPI(Context context){
        mContext = context;
    }


    public Handler mHandler = new Handler();

    /**
     * 运行程序
     * @param file
     */
    public void startWithFile(final File file, final boolean isDebug) {
        boolean updateValid = ((MyApplication)mContext.getApplicationContext()).updateFWValid();

        if(updateValid && Config.getInstance(mContext).isSigned()){
            if(WifiUtil.isSupportWifi(mContext)) {
                TipDialog dialog = new TipDialog.Builder(mContext)
                        .setTitle(mContext.getString(R.string.tip_title))
                        .setMessage(mContext.getString(R.string.update_FW_valid))
                        .setNegativeClickListener(mContext.getString(R.string.tip_no), new TipDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialogInterface, int index, String label) {
                                dialogInterface.dismiss();
                                downloadFile(BaseDriver.UPDATE_TYPE_APP, file, null, isDebug);
                            }
                        })
                        .setPositiveClickListener(mContext.getString(R.string.tip_yes), new TipDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialogInterface, int index, String label) {
                                dialogInterface.dismiss();
                                updateFW(file, isDebug, new FWUpdateCallback() {
                                    @Override
                                    public void callback(boolean success) {
                                        if(!success){
                                            Toast.makeText(mContext,mContext.getString(R.string.update_FW_Faild),Toast.LENGTH_SHORT).show();
                                        }
                                        downloadFile(BaseDriver.UPDATE_TYPE_APP, file, null, isDebug);
                                    }
                                });
                            }
                        }).build();
                dialog.show();
            }
            else if(WifiUtil.isSupportNetwork(mContext)){
                TipDialog dialog = new TipDialog.Builder(mContext)
                        .setTitle(mContext.getString(R.string.tip_title))
                        .setMessage(mContext.getString(R.string.update_FW_valid) + "\n" + mContext.getString(R.string.tip_net_monet))
                        .setNegativeClickListener(mContext.getString(R.string.tip_no), new TipDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialogInterface, int index, String label) {
                                dialogInterface.dismiss();
                                downloadFile(BaseDriver.UPDATE_TYPE_APP, file, null, isDebug);
                            }
                        })
                        .setPositiveClickListener(mContext.getString(R.string.tip_yes), new TipDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialogInterface, int index, String label) {
                                dialogInterface.dismiss();
                                updateFW(file, isDebug, new FWUpdateCallback() {
                                    @Override
                                    public void callback(boolean success) {
                                        if(!success){
                                            Toast.makeText(mContext,mContext.getString(R.string.update_FW_Faild),Toast.LENGTH_SHORT).show();
                                        }
                                        downloadFile(BaseDriver.UPDATE_TYPE_APP, file, null, isDebug);
                                    }
                                });
                            }
                        }).build();
                dialog.show();
            }
            else{
                downloadFile(BaseDriver.UPDATE_TYPE_APP,file,null,isDebug);
            }
        }
        else{
            downloadFile(BaseDriver.UPDATE_TYPE_APP,file,null,isDebug);
        }

    }

    /**
     * 升级固件程序
     * @param file
     * @param isDebug
     * @param callback
     */
    private void updateFW(final File file, final boolean isDebug, FWUpdateCallback callback){
        displayLoading(mContext.getString(R.string.tip_FW_downloading));
        final FWDownloadThread thread = new FWDownloadThread(mContext, new FWDownloadThread.Callback() {
            @Override
            public void callback(boolean success, byte[] buffer) {
                dismiss();
                if(!success){
                    Toast.makeText(mContext,mContext.getString(R.string.download_FW_Faild),Toast.LENGTH_SHORT).show();
                    return;
                }
                displayLoading(mContext.getString(R.string.tip_FW_updating));

                downloadFile(BaseDriver.UPDATE_TYPE_FW,file,buffer,isDebug);
               // startDownload(BaseDriver.UPDATE_TYPE_FW,file,buffer,"A168-TOOL",isDebug);

            }
        });
        thread.start();
        displayCancle(new LoadDialog.OnClickListener() {
            @Override
            public void onClick(Dialog dialog) {
                thread.interrupt();
            }
        });
    }

    /**
     * 下载应用程序文件
     * @param file
     * @param isDebug
     */
    private void downloadFile(final int _type,final File file,byte[] buffer,boolean isDebug){
        if (ConnectStatus.getInstance(mContext).getUSBPort() != null) {
            recordData(mContext, file);

            UpDriver.getInstance(mContext).initPort(ConnectStatus.getInstance(mContext).getUSBPort());
            startDownload(_type, file,buffer, "A168-TOOL",isDebug);
        }
        else if (ConnectStatus.getInstance(mContext).getBTPort() != null) {
            recordData(mContext, file);

            UpDriver.getInstance(mContext).initPort(ConnectStatus.getInstance(mContext).getBTPort());
            startDownload(_type, file,buffer, "A168-TOOL",isDebug);
        }
        else {
            Toast.makeText(mContext, mContext.getString(R.string.device_not_connect), Toast.LENGTH_SHORT).show();
        }
    }

    //记录数据
    private void recordData(Context context, File file) {
        File parentFile = file.getParentFile();
        String path = file.getPath();
        String fileReport = "";
        if (path.contains(FileUtil.DEBUG_ROOT)){
             fileReport = path.substring(path.indexOf(FileUtil.DEBUG_ROOT)+ FileUtil.DEBUG_ROOT.length(),path.lastIndexOf(".BIN"));
        }else if (path.contains(FileUtil.PROGRAM)) {
            boolean autoReport = false;
            if(path.toLowerCase().endsWith("_1.bin")){
                autoReport = true;
            }
            String parentPath =parentFile.getPath();

            fileReport = parentPath.substring(path.indexOf(FileUtil.PROGRAM) + FileUtil.PROGRAM.length() + 1);
            if(autoReport){
                fileReport += "_1";
            }
        }

        saveReport(context,fileReport);
        ReportUntil.writeDataToReport(context, ReportUntil.REPORT_FILENAME + fileReport);

    }

    //将报告的基本信息保存
    private void saveReport(Context context, String ReportName) {
        ReportName = ReportName.replace("/", "$");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        String now = df.format(new Date());// new Date()为获取当前系统时间

        //命名形式用时间+文件名+后缀名组合
        String name = now+"-"+ReportName + ".txt";
        UpDriver.getInstance(context).getPack().setFileName(name);
    }


    /**
     * 刷新进度条
     * @param progress
     */
    public void refreshDownloadProgress(int progress){
        if(mLoadDialog != null){
            mLoadDialog.setProgress(progress);
        }
        /*//Looper.prepare();
        DisplayAndOperateAPI.getInstance().refreshProgress(progress);
        //Looper.loop();*/
    }

    public void displayCancle(LoadDialog.OnClickListener listener){
        if (mLoadDialog != null){
            mLoadDialog.setCancel(mContext.getResources().getString(R.string.cancel),listener);
        }
        //Looper.prepare();
        //Looper.loop();
    }

    /**
     * 程序选择完成，尝试开始
     * 在本地运行：程序开始运行
     * 下载到手持机运行：开始下载程序到手持机
     * @param file
     */
    public  void startDownload(final int _type , final File file, final byte[] buffer, final String hardType, final boolean isDebug) {

        if (file.getName().toLowerCase().endsWith(".bin")) {
            if(_type == BaseDriver.UPDATE_TYPE_APP) {
                displayLoading(mContext.getResources().getString(R.string.tip_message_download));
            }
            else if(_type == BaseDriver.UPDATE_TYPE_FW){
                displayLoading(mContext.getResources().getString(R.string.tip_message_FW_download));
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    /**
                     * 升级应用程序才可以中途取消
                     */
                    if(_type == BaseDriver.UPDATE_TYPE_APP) {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                displayCancle(new LoadDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialog) {
                                        UpDriver.getInstance(mContext).destroyDownload();
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });
                    }
                    Looper.prepare();
                    byte ProgArea, ProType;
                    int MaxProSize = 0;

                    ProType = 0x00;

                    ProgArea = 0x01;

                    if (hardType.equals("A168-TOOL")) {
                        switch (ProgArea) {
                            case 1:
                                MaxProSize = 0x1F000;
                                ProType = 0x35;
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            case 4:
                                break;
                        }
                    } else if (hardType.equals("A168-ANT")) {
                        switch (ProgArea) {
                            case 1:
                                MaxProSize = 0x1E000 + 1;
                                ProType = 0x38;
                                break;
                            case 2:
                                MaxProSize = 0x1000;
                                ProType = 0x38;
                                break;
                            default:
                                Log.e("DownloadProgram", mContext.getString(R.string.A168_unsupport));
                                break;
                        }
                    }

                    UpDriver.getInstance(mContext).initDebug(isDebug);

                    if(!UpDriver.getInstance(mContext).DownLoadFun(_type, file,buffer,MaxProSize,ProType,ProgArea)){
                        Log.e("DownloadProgram", "Finish-False");
                        DiagnosisAPI.getInstance().finishDownloadProgram(_type,false,file,isDebug);
                    }
                    else{
                        DiagnosisAPI.getInstance().finishDownloadProgram(_type,true,file,isDebug);
                    }
                    Looper.loop();
                }
            }).start();
        }

    }


    /**
     * 程序下载到手持机完成
     */
    public void finishDownloadProgram(int _type,boolean isSuccess,File file,boolean isDebug){
        /**
         * 升级固件程序
         */
        if(_type == BaseDriver.UPDATE_TYPE_FW){
            dismiss();
            if(!isSuccess) {
                Toast.makeText(mContext, mContext.getString(R.string.update_FW_Faild), Toast.LENGTH_SHORT).show();
            }
            else{
                ((MyApplication)mContext.getApplicationContext()).updateFWSuccess();
            }
            downloadFile(BaseDriver.UPDATE_TYPE_APP,file,null,isDebug);


        }
        /**
         * 下载应用程序
         */
        else if(_type == BaseDriver.UPDATE_TYPE_APP) {
            if (!isSuccess) {
                ReportUntil.writeDataToReport(mContext, "downLoad failed");
                Toast.makeText(mContext, mContext.getString(R.string.downloadFailed), Toast.LENGTH_SHORT).show();
            } else if (mContext instanceof DescActivity) {
                ReportUntil.writeDataToReport(mContext, "downLoad success");
                ((DescActivity) mContext).refreshIniFile(file);
                int count = Config.getInstance(mContext).getRegCount();
               /* if (count < Config.TRYCOUNT) { //在试用次数大于试用的最高次数时，就不执行以下程序了
                    Config.getInstance(mContext).getRegCount();
                }*/
                if(Config.getInstance(mContext).isConfigured()) {
                    Config.getInstance(mContext).addRegCount();
                }
            }
            else if(mContext instanceof DiagnosisActivity){
                ((DiagnosisActivity)mContext).refreshIniFile(file);
            }

            dismiss();
        }

    }

    /**
     * 显示 正在 XX 对话框
     * @param message
     */
    public void displayLoading(String message){
        if(mLoadDialog != null) {
            mLoadDialog.dismiss();
        }
        mLoadDialog = null;
        mLoadDialog = new LoadDialog.Builder(mContext).setTitle(message).build();
        mLoadDialog.show();
        //DisplayAndOperateAPI.getInstance().displayLoading(message);
    }

    /**
     * 退出对话框
     */
    public void dismiss(){
        if(mLoadDialog != null){
            mLoadDialog.dismiss();
        }
    }


    private  interface OnSelectModeListener{
        void onSelect(int mode, String deviceSN, String hardType);
    }

    private interface FWUpdateCallback{
        void callback(boolean success);
    }


}
