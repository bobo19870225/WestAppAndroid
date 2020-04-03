package com.west.develop.westapp.Download.ProgramDownload;

import android.content.Context;
import android.util.Log;

import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.constant.RequestCodeConstant;
import com.west.develop.westapp.Tools.constant.URLConstant;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by Develop0 on 2017/9/11.
 */

public class MyDownloadThread extends Thread {
    String mUrl;

    private Context mContext;

    /**
     * 已完成大小
     */
    private long mFinishSize;

    /**
     * 文件大小
     */
    private long mContentSize;


    /**
     * 文件名
     */
    private String fileName;

    /**
     * 进度更新监听
     */
    private ProgressListener mProgressListener;

    private DownLoadFailedListener mFailedListener;

    private ArrayList<String> mFileList;


    public MyDownloadThread(Context context, long downloadSize, long contentSize, ArrayList<String> fileList,
                            String fileName, ProgressListener listener, DownLoadFailedListener failedListener) {
        mFinishSize = downloadSize;
        mContentSize = contentSize;
        mFileList = fileList;
        mContext = context;
        this.fileName = fileName;
        mProgressListener = listener;
        mFailedListener = failedListener;
    }

    @Override
    public void interrupt() {
        super.interrupt();
    }

    @Override
    public synchronized void start() {
        super.start();
    }


    @Override
    public void run() {
        try {
            long fileLen = 0;//记录上一次文件下载的位置
            int offset = 0;//偏移量
            int count = 0;
            URL downUrl = null;
            File mDownLoadFile;
            for (int i = 0; i < mFileList.size(); i++) {
                String json = mFileList.get(i);
                JSONObject jsonObject = new JSONObject(json);
                String filePath = jsonObject.getString("filePath");
                String url = jsonObject.getString("url");
                String name;
                if (filePath.startsWith("/")) {
                    name = filePath.substring(1);
                } else {
                    name = filePath;
                }
                if (name.toLowerCase().endsWith(".bin")) {
                    String hexData = jsonObject.getString("checkSum");
                    FileUtil.writeHexData(mContext, name, hexData);
                }

                if (url.endsWith(".png")) {
                    if (Config.getInstance(mContext).getBondDevice() != null) {
                        downUrl = new URL(URLConstant.urlDownloadLogo + "?" +
                                "logoPath=" + URLEncoder.encode(url, "utf-8") + "&" +
                                "deviceSN=" + Config.getInstance(mContext).getBondDevice().getDeviceSN());
                    }
                    mDownLoadFile = new File(FileUtil.getProgramDownloadRoot(mContext), fileName + "/" + name);
                } else {
                    if (url.startsWith("/")) {
                        url = url.substring(1);
                    }

                    if(Config.getInstance(mContext).getBondDevice() != null &&
                            (Config.getInstance(mContext).isSigned() || !Config.getInstance(mContext).isConfigured())){
                        url = URLConstant.urlProgramDownload + "?deviceSN=" + Config.getInstance(mContext).getBondDevice().getDeviceSN() +
                                "&fileName=" + (URLEncoder.encode(url, "utf-8"));
                    }
                  /*  if (Config.getInstance(mContext).isSigned()) {
                        url = URLConstant.urlProgramDownload + "?deviceSN=" + Config.getInstance(mContext).getBondDevice().getDeviceSN() +
                                "&fileName=" + (URLEncoder.encode(url, "utf-8"));
                    } else if(Config.getInstance(mContext).getBondDevice() != null
                            && !Config.getInstance(mContext).isConfigured()){
                        url = URLConstant.urlProgramDownload + "?deviceSN=" + Config.getInstance(mContext).getBondDevice().getDeviceSN() +
                                "&fileName=" + (URLEncoder.encode(url, "utf-8"));
                    }*/
                    downUrl = new URL(url);
                    mDownLoadFile = new File(FileUtil.getProgramDownloadRoot(mContext), name);
                }

                HttpURLConnection http = (HttpURLConnection) downUrl.openConnection();
                http.setConnectTimeout(5000);
                http.setRequestMethod("GET");

                count += jsonObject.getLong("fileSize");

                //文件已存在，获取已下载大小
                if (mDownLoadFile.getParentFile().exists()) {
                    try {
                        fileLen = mDownLoadFile.length();
                        //如果下载的大小大第一个文件的大小，则说明第一个文件下载完成，跳过，否则获取第二个文件的大小
                        if (mFinishSize >= count && fileLen == jsonObject.getInt("fileSize")) {
                            continue;
                        }

                    } catch (Exception e) {
                        Log.e("onFailed", "file: " + e.toString());
                        e.printStackTrace();
                    }
                    //设置开始下载的位置
                    http.setRequestProperty("Range", "bytes=" + fileLen + "");
                } else {
                    mDownLoadFile.getParentFile().mkdirs();
                }

                /**
                 * 需下载的剩余的文件数
                 */
                http.setRequestProperty("Remain", "remain=" + (mFileList.size() - i) + "");

                http.connect();


                int code = http.getResponseCode();
                if (code == 200 || code == 206) {
                    InputStream inputStream = http.getInputStream();
                    byte[] buffer = new byte[1024];
                    FileOutputStream fos = new FileOutputStream(mDownLoadFile, true);
                    //获取下载的字节流，写入文件
                    while ((offset = inputStream.read(buffer, 0, 1024)) != -1) {
                        fos.write(buffer, 0, offset);
                        mFinishSize += offset;
                        if (mProgressListener != null) {
                            mProgressListener.onProgress(mFinishSize);
                        }
                    }
                    if (mProgressListener != null) {
                        mProgressListener.onProgress(mFinishSize);
                    }
                    fos.close();
                    inputStream.close();
                } else if (code == RequestCodeConstant.CODE_HTTP_FORBIDDENT){ //设备使用次数用完
                    this.interrupt();
                    mFailedListener.onFailed(RequestCodeConstant.CODE_HTTP_FORBIDDENT);
                }else if (code == RequestCodeConstant.CODE_HTTP_DISABLE){  //设备禁止使用
                    this.interrupt();
                    mFailedListener.onFailed(RequestCodeConstant.CODE_HTTP_DISABLE);
                }else if (code == RequestCodeConstant.CODE_HTTP_CONFLICT){
                    //删除文件，重新下载
                    mDownLoadFile.delete();
                    i--;
                    run();
                }else {
                    this.interrupt();
                    mFailedListener.onFailed(0);
                }

            }
        } catch (Exception e) {
            Log.e("onFailed", "run: " + e.toString());
            this.interrupt();
            mFailedListener.onFailed(0);
            e.printStackTrace();
        } finally {
            if (this.isAlive()) {
                this.interrupt();
            }
        }

    }

    public interface DownLoadFailedListener {
        void onFailed(int code);
    }
}
