package com.west.develop.westapp.Download.Threads;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.Tools.constant.RequestCodeConstant;
import com.west.develop.westapp.Tools.Utils.VolleyUtil;
import com.west.develop.westapp.Tools.constant.URLConstant;
import com.west.develop.westapp.R;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * 下载固件程序
 * Created by Develop0 on 2018/3/3.
 */
public class FWDownloadThread extends Thread {

    private Context mContext;

    private Callback mListener;

    private Handler mHandler = new Handler();

    public FWDownloadThread(Context context,Callback listener){
        mContext = context;
        mListener = listener;
    }


    @Override
    public void run() {
        try {
            URL downUrl = new URL(URLConstant.urldownloadFirmware + Config.getInstance(mContext).getBondDevice().getDeviceSN());

            HttpURLConnection http = (HttpURLConnection) downUrl.openConnection();
            http.setConnectTimeout(5000);
            http.setRequestMethod("GET");

            http.connect();
            int fileSize = 0;
            int code = http.getResponseCode();
            if (code == 200 || code == 206) {
                InputStream inputStream = http.getInputStream();
                String size = http.getHeaderField("fileSize");
                fileSize = Integer.parseInt(size);

                final byte[] FWBuffer = new byte[fileSize];

                final byte[] buffer = new byte[1024];
                int offset = 0;
                int index = 0;
                //获取下载的字节流，写入文件
                while ((offset = inputStream.read(buffer)) != -1) {
                    for(int i = 0;i < offset;i++){
                        FWBuffer[index] = buffer[i];
                        index++;
                    }
                    Log.e("buffer", offset + ""/*HexUtil.toHexString(buffer)*/);
                }
                Log.e("FWBuffer", FWBuffer.length + ""/*HexUtil.toHexString(buffer)*/);
                inputStream.close();

                if (mListener != null){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.callback(true,FWBuffer);
                        }
                    });

                }
                return;
            }
            else if (code == RequestCodeConstant.CODE_HTTP_FORBIDDENT){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        TipDialog tipDialog = new TipDialog.Builder(mContext)
                                .setTitle(mContext.getResources().getString(R.string.tip_title))
                                .setMessage(mContext.getResources().getString(R.string.tip_message_useCount))
                                .setPositiveClickListener(mContext.getResources().getString(R.string.tip_kown), new TipDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialogInterface, int index, String label) {
                                        dialogInterface.dismiss();
                                        if (mListener != null){
                                            mHandler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    mListener.callback(false,null);
                                                }
                                            });
                                        }
                                        interrupt();
                                    }
                                })
                                .requestSystemAlert(true)
                                .build();
                        tipDialog.show();
                    }
                });
            }else if (code == RequestCodeConstant.CODE_HTTP_DISABLE){
                //注销设备
                VolleyUtil.initDevice(mContext);
                interrupt();
            }else if (code == RequestCodeConstant.CODE_HTTP_CONFLICT){
                //mDownLoadFile.delete();
                if (mListener != null){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.callback(false,null);
                        }
                    });

                }
            }else {
                if (mListener != null){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mListener.callback(false,null);
                        }
                    });

                }
            }

        } catch (Exception e) {
            if (mListener != null){
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mListener.callback(false,null);
                    }
                });

            }
            e.printStackTrace();
        } finally {
            this.interrupt();
        }
    }



    public interface Callback{
        void callback(boolean success, byte[] buffer);
    }
}
