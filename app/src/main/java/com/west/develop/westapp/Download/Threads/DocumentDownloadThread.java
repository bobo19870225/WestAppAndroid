package com.west.develop.westapp.Download.Threads;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.CallBack.ResultListener;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.constant.RequestCodeConstant;
import com.west.develop.westapp.Tools.Utils.VolleyUtil;
import com.west.develop.westapp.Tools.constant.URLConstant;
import com.west.develop.westapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created by Develop12 on 2017/12/13.
 */

public class DocumentDownloadThread extends Thread {

    Context mContext;
    int docType;
    ResultListener mListener;

    Handler mHandler = new Handler();

    public DocumentDownloadThread(Context mContext, int docType, ResultListener mListener) {
        this.mContext = mContext;
        this.docType = docType;
        this.mListener = mListener;
    }

    @Override
    public void run() {
        try {

            if(!Config.getInstance(mContext).isSigned()){
                return ;
            }
            String url = URLConstant.urldownloadDocumentversion + "?docType=" + docType +
                    "&deviceSN=" + Config.getInstance(mContext).getBondDevice().getDeviceSN() +
                    "&locale=" + Config.getInstance(mContext).getLanguage();
            URL downUrl = new URL(url);

            HttpURLConnection http = (HttpURLConnection) downUrl.openConnection();
            http.setConnectTimeout(5000);
            http.setRequestMethod("GET");
            http.connect();

            int code = http.getResponseCode();
            if (code == 200 || code== 206){
                InputStream inputStream =  http.getInputStream();
                String fileName = http.getHeaderField("fileName");
                fileName = URLDecoder.decode(fileName,"utf-8");
                /**
                 * 如果本地有一个版本的文件，首先先删除在下载
                 */
                File rootPath = new File(FileUtil.getProgramDocument(mContext));
                if (!rootPath.exists()){
                    rootPath.mkdirs();
                }
                File mDownLoadFile = new File(FileUtil.getProgramDocument(mContext), fileName + ".down");

                byte[] buffer = new byte[1024];
                FileOutputStream fos = new FileOutputStream(mDownLoadFile, true);
                int offset = 0;
                //获取下载的字节流，写入文件
                while ((offset = inputStream.read(buffer, 0, 1024)) != -1) {
                    fos.write(buffer, 0, offset);
                }

                File files[] = rootPath.listFiles();
                if (files != null){
                    for (int i = 0; i < files.length ; i++) {
                        String name = fileName.substring(0,fileName.lastIndexOf("_v"));
                        if (files[i].getName().contains(name)){
                            if(!files[i].getName().contains(fileName)) {
                                files[i].delete();
                            }
                            else{
                                if(files[i].getName().endsWith(".down")){
                                    String downFile = files[i].getPath();
                                    String dest = downFile.substring(0,downFile.length() - 5);
                                    files[i].renameTo(new File(dest));
                                }
                            }
                        }
                    }
                }


                fos.close();
                inputStream.close();
                if (mListener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onResult(true);
                        }
                    });

                    interrupt();
                }
            }else if (code == RequestCodeConstant.CODE_HTTP_FORBIDDENT){
                Looper.prepare();
                TipDialog tipDialog = new TipDialog.Builder(mContext)
                        .setTitle(mContext.getResources().getString(R.string.tip_title))
                        .setMessage(mContext.getResources().getString(R.string.tip_message_useCount))
                        .setPositiveClickListener(mContext.getResources().getString(R.string.tip_kown), new TipDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialogInterface, int index, String label) {
                                dialogInterface.dismiss();
                                if (mListener != null) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            mListener.onResult(false);
                                        }
                                    });
                                    interrupt();
                                }
                            }
                        })
                        .requestSystemAlert(true)
                        .build();
                tipDialog.show();
                Looper.loop();

            }else if (code == RequestCodeConstant.CODE_HTTP_DISABLE){
                //注销设备
                VolleyUtil.initDevice(mContext);
            }else {
                if (mListener != null) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onResult(false);
                        }
                    });
                    interrupt();
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (mListener != null) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.onResult(false);
                    }
                });

                interrupt();
            }
            this.interrupt();
        }


    }
}
