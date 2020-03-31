package com.west.develop.westapp.Download.Threads;

import android.app.Dialog;
import android.content.Context;
import android.os.Looper;

import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.CallBack.RequestCallBack;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.Tools.constant.RequestCodeConstant;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.Utils.VolleyUtil;
import com.west.develop.westapp.Tools.constant.URLConstant;
import com.west.develop.westapp.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Develop12 on 2017/12/13.
 */

public class APKDownloadThread extends Thread {

   private Context mContext;
   private RequestCallBack mListener;

    public APKDownloadThread(Context mContext, RequestCallBack mListener) {
        this.mContext = mContext;
        this.mListener = mListener;
    }

    @Override
    public void run() {
        try {
            URL downUrl = new URL(URLConstant.urldownloadAPK + Config.getInstance(mContext).getBondDevice().getDeviceSN());

            HttpURLConnection http = (HttpURLConnection) downUrl.openConnection();
            http.setConnectTimeout(5000);
            http.setRequestMethod("GET");

            File mDownLoadFile = new File(FileUtil.getProgramDocument(mContext), mContext.getResources().getString(R.string.app_Name)+".apk");
            if(!mDownLoadFile.getParentFile().exists()){
                mDownLoadFile.getParentFile().mkdirs();
            }
            Long fileLen = mDownLoadFile.length();
            //设置开始下载的位置
            http.setRequestProperty("Range", "bytes=" + fileLen + "");
            http.connect();
            long fileSize = 0;
            int code = http.getResponseCode();
            if (code == 200 || code== 206){
                InputStream inputStream =  http.getInputStream();
                String size = http.getHeaderField("fileSize");
                fileSize = Long.parseLong(size);

                byte[] buffer = new byte[1024];
                FileOutputStream fos = new FileOutputStream(mDownLoadFile, true);
                int offset = 0;
                long downSize = fileLen;
                //获取下载的字节流，写入文件
                while ((offset = inputStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, offset);
                    downSize += offset;
                    if (mListener != null){
                        mListener.onLoading(fileSize,downSize);
                    }
                }
                if (mListener != null) {
                    mListener.onResult(true, mDownLoadFile);
                }

                fos.close();
                inputStream.close();
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
                                    mListener.onResult(false,null);
                                    }
                                    interrupt();
                            }
                        })
                        .requestSystemAlert(true)
                        .build();
                tipDialog.show();
                Looper.loop();

            }else if (code == RequestCodeConstant.CODE_HTTP_DISABLE){
                //注销设备
                VolleyUtil.initDevice(mContext);
                interrupt();
            }else if (code == RequestCodeConstant.CODE_HTTP_CONFLICT){
                mDownLoadFile.delete();
                if (mListener != null){
                    mListener.onLoading(0,0);
                }
                run();
            }else {
                mListener.onResult(false,null);
            }

        } catch (Exception e) {
            e.printStackTrace();
            run();
        }finally {
            this.interrupt();
        }


    }
}
