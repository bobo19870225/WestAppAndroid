package com.west.develop.westapp.Download.ProgramDownload;

import android.app.Dialog;
import android.content.Context;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.Bean.NCarBean;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.constant.RequestCodeConstant;
import com.west.develop.westapp.Tools.constant.URLConstant;
import com.west.develop.westapp.Tools.MDBHelper;
import com.west.develop.westapp.Tools.Utils.VolleyUtil;
import com.west.develop.westapp.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URLDecoder;
import java.util.ArrayList;

import static com.west.develop.westapp.Bean.Upgrade.DownloadDB.*;

/**
 * Created by Develop0 on 2017/9/11.
 */
public class ProgramDownLoadThread{
    private static final int MAX_FAIL_TIMES = 5;

    private Context mContext;

    /**
     * 下载连接
     */
    private String mUrl;

    /**
     * 文件大小
     */
    private long mContentSize = 0;

    /**
     * 每一个文件大小
     */
    private long mFileSize = 0;

    /**
     * 已下载的大小
     */
    private long mDownloadSize;

    /**
     * 保存的文件名
     */
    private String mFileName;

    /**
     * 下载任务 线程
     */
    private MyDownloadThread mDownLoadThread;
    /**
     * 下载失败的监听
     */
    private MyDownloadThread.DownLoadFailedListener mFailedListener;




    /**
     * 任务状态，初始为等待状态
     */
    private int mStatus = STATUS_WAIT;


    /**
     * 下载地址
     */
    private String mDownloadUrl;


    /**
     * 进度
     */
    private double mProgress = 0;

    /**
     * 下载失败次数
     */
    private int mFailTimes = 0;

    /**
     * 标识是否在从网上加载基本信息数据
     */
    private boolean isLoadInfo = false;

    /**
     * 标识开始下载程序，调用startDownload函数
     */
    private boolean isDownloading = false;

    /**
     * 表示程序下载完成
     */
    private boolean isFinished = false;

    private NCarBean mNCarBean;

    /**
     * 图标的大小
     */
    private long  carLogoSize = 0;

    /**
     * 下载的url
     */
    private ArrayList<String> listUrl = new ArrayList<>();

    public ProgramDownLoadThread(Context context, String url,long contentSize){
        mContentSize = contentSize;
        mContext = context;
        mUrl = url;

        try {
            mFileName = URLDecoder.decode(url,"utf-8");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        updateDownloadSize(null);
        //开始从网络上加载基本信息
        getInfo();

    }


    /**
     * 获取下载信息
     */
    private void getInfo(){
        isLoadInfo =true;//表示正在从网络上加载数据

        /**
         * 设备已卖出
         */
        if(Config.getInstance(mContext).isConfigured()){
            /**
             * 设备未激活，无法下载
             */
            if(!Config.getInstance(mContext).isSigned()){
                Toast.makeText(mContext,mContext.getString(R.string.toast_unsign),Toast.LENGTH_SHORT).show();
                return;
            }
            mDownloadUrl = URLConstant.urlProgramCheckVersion +
                    "?deviceSN=" + Config.getInstance(mContext).getBondDevice().getDeviceSN()
                    + "&program=" + mUrl;
        }
        else{
            /**
             * 设备未绑定
             */
            if(Config.getInstance(mContext).getBondDevice() != null){
                mDownloadUrl = URLConstant.urlProgramCheckVersion +
                        "?deviceSN=" + Config.getInstance(mContext).getBondDevice().getDeviceSN()
                        + "&program=" + mUrl;
            }
            else{
                Toast.makeText(mContext,mContext.getString(R.string.toast_unBond),Toast.LENGTH_SHORT).show();
                return;
            }
        }

        getDownLoadUrl(mDownloadUrl, mContext, callBack);

    }

    public ArrayList<String> getListUrl() {
        return listUrl;
    }

    public void setListUrl(ArrayList<String> listUrl) {
        this.listUrl = listUrl;
    }

    /**
     * 获取文件名
     * @return
     */
    public String getFileName(){
        String fileName = "";
        if(mFileName != null){
            fileName = mFileName;
        }
        else {
            try {
                fileName = URLDecoder.decode(mUrl, "utf-8");
                this.mFileName = fileName;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return fileName;
    }

    /**
     * 获取链接
     * @return
     */
    public String getUrl(){
        return mUrl;
    }

    /**
     * 设置文件名
     * @param fileName
     */
    public void setFileName(String fileName){
        this.mFileName = fileName;
    }


    /**
     * 获取任务状态
     */
    public int getStatus() {
        return mStatus;
    }

    /**
     * 设置任务状态
     * @param mState
     */
    public void setStatus(int mState) {
        this.mStatus = mState;
    }



    /**
     * 获取文件大小
     * @return
     */
    public long getContenetSize(){
        return this.mContentSize;
    }

    public NCarBean getmNCarBean() {
        return mNCarBean;
    }

    /**
     * 启动下载任务
     * @param downLoadFailedListener
     */
    public void startDownload(MyDownloadThread.DownLoadFailedListener downLoadFailedListener){
        this.mFailedListener = downLoadFailedListener;
        isDownloading = true;

        mStatus = STATUS_DOWNLOAD;


        if(isLoadInfo){
            return;
        }
        if(mContentSize > 0 && getListUrl().size() > 0) {
            if (mDownLoadThread == null || !mDownLoadThread.isAlive()) {
                if(mDownloadSize > mContentSize){
                    deleteFiles();
                    mDownloadSize = 0;
                }
                if(mDownloadSize == mContentSize){
                    refreshProgress();
                    return;
                }
                mDownLoadThread = new MyDownloadThread(mContext, mDownloadSize, mContentSize, getListUrl(), getFileName(),
                        new ProgressListener() {
                            @Override
                            public void onProgress(long size) {
                                mFailTimes = 0;
                                mDownloadSize = size;
                                refreshProgress();
                            }
                        },
                        new MyDownloadThread.DownLoadFailedListener() {
                            @Override
                            public void onFailed(int code) {
                                if (code == RequestCodeConstant.CODE_HTTP_FORBIDDENT){
                                    Looper.prepare();
                                    if (DownloadManager.getInstance(mContext).getThreadCount()== 0) {
                                        int count = DownloadManager.getInstance(mContext).getThreadCount();
                                        count++;
                                        DownloadManager.getInstance(mContext).setThreadCount(count);
                                        TipDialog tipDialog = new TipDialog.Builder(mContext)
                                                .setTitle(mContext.getResources().getString(R.string.tip_title))
                                                .setMessage(mContext.getResources().getString(R.string.tip_message_useCount))
                                                .setPositiveClickListener(mContext.getResources().getString(R.string.tip_kown), new TipDialog.OnClickListener() {
                                                    @Override
                                                    public void onClick(Dialog dialogInterface, int index, String label) {
                                                        dialogInterface.dismiss();
                                                        DownloadManager.getInstance(mContext).pauseAllDownload();
                                                        mDownLoadThread.interrupt();
                                                    }
                                                })
                                                .requestSystemAlert(true)
                                                .build();
                                        tipDialog.show();
                                    }
                                    mDownLoadThread.interrupt();
                                    Looper.loop();
                                    return;
                                }
                                if (code == RequestCodeConstant.CODE_HTTP_DISABLE){
                                    //注销设备
                                    VolleyUtil.initDevice(mContext);
                                    return;
                                }
                                mFailTimes++;
                                if (mFailTimes < MAX_FAIL_TIMES && mStatus == STATUS_DOWNLOAD) {
                                    mDownLoadThread.interrupt();
                                    startDownload(mFailedListener);
                                } else {
                                    mFailTimes = 0;
                                   // Looper.prepare();
                                  /* *
                                  * 在网络较好情况下，正在下载状态下，点击按钮，使它暂停的同时，也触发了线程中断异常，
                                  * 所以在暂停模式下不能再让它等待了
                                  * */

                                    if (mStatus != STATUS_PAUSE) {
                                        //waitDownload();
                                        pauseDownload();
                                        mDownLoadThread.interrupt();
                                        //对下载失败的程序进行监听
                                        if (mFailedListener != null) {
                                            mFailedListener.onFailed(0);
                                        }
                                    }
                                   // Looper.loop();
                                }
                            }
                        }
                );
                if (!mDownLoadThread.isAlive()) {
                    mDownLoadThread.start();
                }
            }
        }
        else{
            getInfo();
        }

        DownloadManager.getInstance(mContext).onChange();

    }

    /**
     * 暂停下载任务
     */
    public void pauseDownload(){
       // Log.e("onFailed", "run: pauseDownload");
        isDownloading = false;
        isFinished = false;
        mFailTimes = 0;
        if(mDownLoadThread != null) {
            mDownLoadThread.interrupt();
        }
        mStatus = STATUS_PAUSE;
        MDBHelper.getInstance(mContext).updateDownStatus(getUrl(),STATUS_PAUSE);

    }

    /**
     * 等待任务
     */
    public void waitDownload(){
        isDownloading = false;
        isFinished = false;
        mFailTimes = 0;
        try {
            if (mDownLoadThread != null) {
                mDownLoadThread.interrupt();
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        mStatus = STATUS_WAIT;
        MDBHelper.getInstance(mContext).updateDownStatus(getUrl(),STATUS_WAIT);
    }

    public double getProgress(){
        return mProgress;
    }


    /**
     * 网络请求回调
     */
    private VolleyUtil.IVolleyCallback callBack = new VolleyUtil.IVolleyCallback() {
        @Override
        public void getResponse(JSONObject jsonObject) {
            Log.e("Volley ProgramCheckVers",jsonObject.toString());
            if ("".equals(jsonObject) || jsonObject == null) {
                return;
            }
            JSONObject arr = null;
            try {
                int code = jsonObject.getInt("code");
                if (code == 0) {
                    arr = jsonObject.getJSONObject("data");
                    updateDownloadSize(arr);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            isLoadInfo = false;//表示从网络上加载数据完成

            //只有当状态在正在下载和还没有下载完成的时候在调用，否则就会一个文件有多个提示框
            if(isDownloading && !isFinished){
                startDownload(mFailedListener);
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            isLoadInfo = false;
            Log.e("onErrorResponse", "onErrorResponse: "+error);
        }
    };

    private void getDownLoadUrl(String urlProgramInfo, Context mContext, VolleyUtil.IVolleyCallback callBack) {
        VolleyUtil.jsonRequest(urlProgramInfo,mContext,callBack);
    }

    //检查图标是否存在，或者需要更新
    private void checkIcon(String logoPath) {
        String path = FileUtil.getProgramIcon(mContext) + logoPath;
        File iconFile = new File(path);
        if (iconFile.exists()){
            carLogoSize = iconFile.length();
        }
    }

    /**
     * 更新已下载大小
     */
    private void updateDownloadSize(JSONObject arr) {

        if(arr != null) {
            try {
                mDownloadSize = 0 ;
                mContentSize = 0;
                //检查本地是否存在图标，存在比较是否需要更新
                JSONObject car = arr.getJSONObject("car");
                JSONObject jsonObjectlogo = new JSONObject();
                checkIcon(car.getString("logoPath"));

                long logoSize = arr.getLong("logoSize");

                Gson gson = new Gson();
                mNCarBean = gson.fromJson(car.toString(), NCarBean.class);

                if (logoSize != carLogoSize) {
                    mFileSize += logoSize;

                    jsonObjectlogo.put("filePath", mNCarBean.getLogoPath());
                    jsonObjectlogo.put("fileSize", arr.getLong("logoSize"));
                    jsonObjectlogo.put("url",mNCarBean.getLogoPath());
                    listUrl.add(jsonObjectlogo.toString());

                    File fileLogo = new File(FileUtil.getProramDownloadRoot(mContext) + mFileName);
                    if (fileLogo.exists() && fileLogo.isDirectory()) {
                        File files2[] = fileLogo.listFiles();
                        for (int n = 0; n < files2.length; n++) {
                            //mDownloadSize += files2[n].length();
                            if(files2[n].getName().equals(mNCarBean.getLogoPath())){
                                mDownloadSize += files2[n].length();
                            }
                        }
                    }
                }

                JSONArray array = arr.getJSONArray("verList");
                for (int i = 0; i < array.length(); i++) {
                    String json = array.getString(i);
                    listUrl.add(json);
                    long fileSize = array.getJSONObject(i).getLong("fileSize");
                    mFileSize += fileSize;

                    String filePath = FileUtil.getProramDownloadRoot(mContext) + array.getJSONObject(i).getString("filePath");
                    File file = new File(filePath);
                    if (file.exists()) {
                        long length = file.length();
                        if (length > fileSize) {
                            file.delete();
                            continue;
                        }
                        mDownloadSize += length;
                    }

                }
                setListUrl(listUrl);

                mContentSize = mFileSize;
                MDBHelper.getInstance(mContext).updateFileNameAndSize(mUrl, mFileName,mContentSize);

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
        }else if (arr == null){
            mDownloadSize = 0 ;
            String programRoot = FileUtil.getProramDownloadRoot(mContext);
            File fileRoot = new File(programRoot + mFileName.substring(0,mFileName.lastIndexOf("/")));
            File files[] = fileRoot.listFiles();
            if (fileRoot.exists()) {
                for (int j = 0; j < files.length; j++) {
                    if (files[j].exists() && files[j].isDirectory()) {
                        File filesEnd[] = files[j].listFiles();
                        for (int i = 0; i < filesEnd.length; i++) {
                            File file = filesEnd[i];
                            if (file.exists() && (!file.getName().equals(FileUtil.DOWNLOAD_CRC_NAME) ) ) {
                                long length = file.length();
                                mDownloadSize += length;
                            }
                        }
                    }
                }
            }
        }
        refreshProgress();
        DownloadManager.getInstance(mContext).queryProgress();
    }

    /**
     * 删除文件
     */
    private void deleteFiles(){
        MDBHelper.getInstance(mContext).updateFileNameAndSize(mUrl,mFileName,mContentSize);
        String programRoot = FileUtil.getProramDownloadRoot(mContext);
        File fileRoot = new File(programRoot + mFileName.substring(0,mFileName.lastIndexOf("/")));
        File files[] = fileRoot.listFiles();
        if (fileRoot.exists()) {
            for (int j = 0; j < files.length; j++) {
                String filePath = files[j].getPath();

                int indexStart = filePath.indexOf(FileUtil.PRO_DOWNLOAD_ROOT) + FileUtil.PRO_DOWNLOAD_ROOT.length() + 1;
                int indexEnd = filePath.lastIndexOf("_v");

                Log.e("startIndex",indexStart + "");
                Log.e("endIndex",indexEnd + "");
                Log.e("length",filePath.length() + "");
                filePath = filePath.substring(indexStart, indexEnd);


                if (files[j].exists() && files[j].isDirectory()) {
                    File filesEnd[] = files[j].listFiles();
                    for (int i = 0; i < filesEnd.length; i++) {
                        File file = filesEnd[i];
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }
            }
        }
        File fileLogo = new File(FileUtil.getProramDownloadRoot(mContext) + mFileName);
        if (fileLogo.exists() && fileLogo.isDirectory()) {
            File files2[] = fileLogo.listFiles();
            for (int n = 0; n < files2.length; n++) {
                files2[n].delete();
            }
        }
    }
    /**
     * 刷新下载进度
     */
    private void refreshProgress(){
        if(mContentSize >0) {
            double progress = (double) mDownloadSize / (double) mContentSize;
            mProgress = progress * 100;
            if(mProgress > 100){
                mProgress = 0;
            }
            if (mDownloadSize == mContentSize) {
                isFinished = true;
                //Log.e("DownloadFile-" + mFileName,mDownloadSize + "/" + mContentSize);
                if (mDownLoadThread != null && mDownLoadThread.isAlive()) {
                    mDownLoadThread.interrupt();
                }
                DownloadManager.getInstance(mContext).onDownloadFinish(mUrl,mFileName);
            }

            if(mDownloadSize > mContentSize){
                mContentSize = 0;
                startDownload(mFailedListener);
            }
        }
    }

}
