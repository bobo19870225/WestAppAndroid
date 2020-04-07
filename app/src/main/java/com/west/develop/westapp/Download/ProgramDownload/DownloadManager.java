package com.west.develop.westapp.Download.ProgramDownload;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.west.develop.westapp.Bean.NCarBean;
import com.west.develop.westapp.Bean.Upgrade.DownloadDB;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.MDBHelper;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.Utils.WifiUtil;
import com.west.develop.westapp.UI.Activity.Upgrade.DownloadTaskActivity;
import com.west.develop.westapp.UI.Fragment.Upgrade.UpgradeFragment;
import com.west.develop.westapp.usb.HexDump;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class DownloadManager {
    private static final String kURL = "download_URL";
    private static final String kFileName = "download_FileName";

    private Context mContext;
    private static DownloadManager instance;

    /**
     * 同时下载线程 数量
     */
    private static final int DOWN_COUNT = 3;

    /**
     * 更行列表
     */
    private CopyOnWriteArrayList<ProgramDownLoadThread> mUpgradeThreads;

    /**
     * 已暂停 的下载线程
     */
    private ArrayList<ProgramDownLoadThread> mPauseThreads;

    /**
     * 正在下载的线程
     */
    private ArrayList<ProgramDownLoadThread> mDownloadThreads;

    /**
     * 下载状态改变 监听
     */
    private OnDownloadChangeListener mChangeListener;

    /**
     * 打开下载/关闭下载 标识
     */
    private boolean isStarted;


    /**
     * 已完成，待提示 更新路径列表
     */
    private ArrayList<Map<String, String>> mFinishSet = new ArrayList<>();

    private RemoteViews remoteViews;
    private PendingIntent pendingIntent;
    private NotificationCompat.Builder mBuilder;
    private NotificationManager mNotificationManager;
    private final int NOTIFICATION_ID = 1;


    public synchronized static DownloadManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DownloadManager.class) {
                instance = new DownloadManager();
                instance.mContext = context;

                /**
                 * 初始化notifition
                 */
                instance.initNotification();
                /**
                 * 初始化线程列表
                 */
                instance.initAll();
                instance.initPause();
            }
        }
        instance.mContext = context;

        return instance;
    }

    private void initNotification() {
        Intent notifyIntent = new Intent(mContext, DownloadTaskActivity.class);
        int requestCode2 = (int) SystemClock.uptimeMillis();
        pendingIntent = PendingIntent.getActivity(mContext, requestCode2,
                notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.download_notifi);
        remoteViews.setImageViewResource(R.id.notifi_imag, R.mipmap.download1);
        remoteViews.setTextViewText(R.id.notifi_title, "升级程序下载");
        remoteViews.setTextViewText(R.id.notifi_downloaded_num, "0个正在下载");
        remoteViews.setTextViewText(R.id.notifi_downloading, "0个等待下载");
        mBuilder = new NotificationCompat.Builder(mContext);
        mBuilder.setSmallIcon(R.mipmap.download1)
                .setTicker("升级程序正在下载")
                .setContent(remoteViews)
                .setContentIntent(pendingIntent);
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

    }

    /**
     * 添加多条下载 URL
     * @param urls
     */
    public void addUrls(ArrayList<String> urls) {
        if (urls == null || urls.size() <= 0) {
            return;
        }
        for (int i = 0; i < urls.size(); i++) {
            addUrl(urls.get(i));
        }
    }

    /**
     * 添加单条 下载 URL
     * @param url
     */
    public void addUrl(String url) {
        boolean contain = false;
        if (mUpgradeThreads == null) {
            mUpgradeThreads = new CopyOnWriteArrayList<>();
        }

        for (int i = 0; i < mUpgradeThreads.size(); i++) {
            if (mUpgradeThreads.get(i).getUrl().equals(url)) {
                contain = true;
                break;
            }
        }

        /**
         * 已包含 添加 的URL，不再进行添加
         */
        if (contain) {
            return;
        }
        ProgramDownLoadThread thread = new ProgramDownLoadThread(mContext, url, 0);
        mUpgradeThreads.add(thread);

        DownloadDB bean = new DownloadDB();
        bean.setUrl(url);
        //初始为等待状态
        bean.setStatus(DownloadDB.STATUS_WAIT);
        //保存数据库
        MDBHelper.getInstance(mContext).insertDownloadUrl(bean);

    }
    /**
     * 初始化所有的下载
     * 从数据库读取
     */
    public void initAll() {
        if (!Config.getInstance(mContext).isSigned() && Config.getInstance(mContext).isConfigured()) {
            return;
        }
        ArrayList<DownloadDB> allList = MDBHelper.getInstance(mContext).getDownloadList(DownloadDB.STATUS_ALL);

        if (mUpgradeThreads == null) {
            mUpgradeThreads = new CopyOnWriteArrayList<>();
        }
        mUpgradeThreads.clear();
        if (allList != null) {
            for (int i = 0; i < allList.size(); i++) {
                DownloadDB bean = allList.get(i);
                ProgramDownLoadThread thread = new ProgramDownLoadThread(mContext, bean.getUrl(), bean.getContentSize());
                thread.setFileName(bean.getFileName());
                thread.setStatus(bean.getStatus());
                if (!isStarted() && bean.getStatus() == DownloadDB.STATUS_DOWNLOAD) {
                    thread.setStatus(DownloadDB.STATUS_WAIT);
                }
                //thread.setContentSize(bean.getContentSize());
                if (!mUpgradeThreads.contains(thread)) {
                    mUpgradeThreads.add(thread);
                }
            }
        }
        mHandler.sendEmptyMessage(MSG_REFRESH_LIST);
    }

    /**
     * 初始化 已暂停列表
     */
    public void initPause() {

        if (!Config.getInstance(mContext).isSigned() && Config.getInstance(mContext).isConfigured()) {
            return;
        }
        if (mPauseThreads == null) {
            mPauseThreads = new ArrayList<>();
        }

        if (mUpgradeThreads == null) {
            mUpgradeThreads = new CopyOnWriteArrayList<>();
        }
        for (ProgramDownLoadThread thread : mUpgradeThreads) {
            if (thread.getStatus() == DownloadDB.STATUS_PAUSE) {
                boolean contain = false;
                for (int i = 0; i < mPauseThreads.size(); i++) {
                    if (mPauseThreads.get(i).getUrl().equals(thread.getUrl())) {
                        contain = true;
                        break;
                    }
                }
                if (!contain) {
                    mPauseThreads.add(thread);
                }
            }
        }

    }

    /**
     * 初始化 正在下载列表
     */
    private void initDownload() {
        if (mDownloadThreads == null) {
            mDownloadThreads = new ArrayList<>();
        }

        //处于下载状态时，添加到正在下载列表
        if (isStarted()) {
            if (mDownloadThreads == null || mDownloadThreads.size() < DOWN_COUNT) {

                /**
                 * 添加 所有下载任务中 正在下载的任务
                 */
                for (ProgramDownLoadThread thread : mUpgradeThreads) {
                    if (thread.getStatus() == DownloadDB.STATUS_DOWNLOAD) {
                        boolean contain = false;
                        for (int i = 0; i < mDownloadThreads.size(); i++) {
                            if (mDownloadThreads.get(i).getUrl().equals(thread.getUrl())) {
                                contain = true;
                                break;
                            }

                        }
                        if (!contain) {
                            mDownloadThreads.add(thread);
                        }
                    }
                }
                /**
                 * 在点击开始下载的时候，将暂停的状态改为等待状态
                 */
                if (!isAllPauseUrl) {
                    for (ProgramDownLoadThread thread : mUpgradeThreads) {
                        if (thread.getStatus() == DownloadDB.STATUS_PAUSE) {
                            thread.setStatus(DownloadDB.STATUS_WAIT);
                            MDBHelper.getInstance(mContext).updateDownStatus(thread.getUrl(), DownloadDB.STATUS_WAIT);
                            mPauseThreads.remove(thread);
                        }
                    }
                }

                /**
                 * 所有下载任务中正在下载的任务数量 小于 可同时下载的任务数量,
                 * 将等待状态的任务变为正在下载
                 */
                if (mDownloadThreads.size() < DOWN_COUNT) {
                    for (int i = 0; i < mUpgradeThreads.size(); i++) {
                        ProgramDownLoadThread thread = mUpgradeThreads.get(i);

                        if (mDownloadThreads.size() >= DOWN_COUNT) {
                            break;
                        }
                        if (thread.getStatus() == DownloadDB.STATUS_WAIT) {
                            thread.setStatus(DownloadDB.STATUS_DOWNLOAD);
                            MDBHelper.getInstance(mContext).updateDownStatus(thread.getUrl(), DownloadDB.STATUS_DOWNLOAD);
                            boolean contain = false;
                            for (int j = 0; j < mDownloadThreads.size(); j++) {
                                if (mDownloadThreads.get(j).getUrl().equals(thread.getUrl())) {
                                    contain = true;
                                    break;
                                }

                            }
                            if (!contain) {
                                mDownloadThreads.add(thread);
                            }
                        }

                    }
                }

            }
        }
        /**
         * 处于非下载状态，所有正在下载的任务改变状态为等待
         */
        else {
            for (ProgramDownLoadThread thread : mUpgradeThreads) {
                if (thread.getStatus() == DownloadDB.STATUS_DOWNLOAD) {
                    thread.setStatus(DownloadDB.STATUS_WAIT);
                    MDBHelper.getInstance(mContext).updateDownStatus(thread.getUrl(), DownloadDB.STATUS_WAIT);
                }
            }
            mDownloadThreads.clear();
        }
        //刷新页面显示列表
        mHandler.sendEmptyMessage(MSG_REFRESH_LIST);
    }


    /**
     * 任务 @url 下载完成
     * @param url
     */
    public void onDownloadFinish(final String url, String fileName) {
        //获取图标
        getIcon(url);
        //删除任务
        deleteUrl(url);
        Log.e("Finish-url", fileName);
        //刷新下载列表
        refreshDowndload();
        Log.e("Finish-refre", fileName);
        Message message = new Message();
        Map<String, String> map = new HashMap<String, String>();
        map.put(kURL, url);
        map.put(kFileName, fileName);
        message.obj = map;
        message.what = MSG_REPLACE_PROGRAM_FILE;
        mHandler.sendMessage(message);
        Log.e("Finish-FileName", fileName);

    }

    private ArrayList<NCarBean> carBeanList;
    private void getIcon(String url) {

        if (carBeanList == null) {
            carBeanList = new ArrayList<NCarBean>();
        }
        if (mDownloadThreads != null) {
            for (int i = 0; i < mDownloadThreads.size(); i++) {
                if (mDownloadThreads.get(i).getUrl().equals(url)) {
                    if (!carBeanList.contains(mUpgradeThreads.get(i).getmNCarBean())) {
                        carBeanList.add(mDownloadThreads.get(i).getmNCarBean());
                    }
                }
            }
        } else if (mUpgradeThreads != null) {
            for (int j = 0; j < mUpgradeThreads.size(); j++) {
                if (mUpgradeThreads.get(j).getUrl().equals(url)) {
                    if (!carBeanList.contains(mUpgradeThreads.get(j).getmNCarBean())) {
                        carBeanList.add(mUpgradeThreads.get(j).getmNCarBean());
                    }
                }

            }
        }

    }


    /**
     * 替换程序文件
     * @param fileName
     */
    public void replaceProgram(String fileName) {
        fileName = fileName.replace("\\", "/");
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }


        /**
         * 删除本地中的版本
         */
        String LocalName = fileName.substring(0, fileName.lastIndexOf("/"));
        String programUrl = FileUtil.getProgramRoot(mContext);
        File file = new File(programUrl + LocalName);
        if (file.exists() && file.isDirectory()) {
            File[] files = file.listFiles();
            if (files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    try {
                        String childName = files[i].getPath();//files[i].getParent() + "/" + files[i].getName().substring(5);
                        childName = childName.substring(childName.indexOf(FileUtil.PROGRAM_ROOT) + FileUtil.PROGRAM_ROOT.length(), childName.lastIndexOf("_v"));
                        if (files[i].isDirectory() && fileName.equals(childName)) {
                            File[] filesend = files[i].listFiles();
                            for (int j = 0; j < filesend.length; j++) {
                                filesend[j].delete();
                            }
                        }
                        files[i].delete();
                    } catch (Exception ex) {
                        continue;
                    }
                }
            }
        }



        try {
            /**
             * 移动文件
             * 从下载目录移动到程序存放目录
             */
            String downloadRoot = FileUtil.getProgramDownloadRoot(mContext);
            File fileDownloadRoot = new File(downloadRoot + LocalName);
            File[] fileDownloadList = fileDownloadRoot.listFiles();
            for (int j = 0; j < fileDownloadList.length; j++) {
                File mDownloadFile = fileDownloadList[j];
                String mDownloadPath = "";
                try {
                    mDownloadPath = mDownloadFile.getPath();//mDownloadFile.getParent() + "/" + mDownloadFile.getName().substring(5);
                    mDownloadPath = mDownloadPath.substring(mDownloadPath.indexOf(FileUtil.PRO_DOWNLOAD_ROOT) + FileUtil.PRO_DOWNLOAD_ROOT.length() + 1, mDownloadPath.lastIndexOf("_v"));
                } catch (Exception e) {
                    e.printStackTrace();
                    if (mDownloadFile.exists() && mDownloadFile.isDirectory()) {
                        File[] files = mDownloadFile.listFiles();
                        for (int i = 0; i < files.length; i++) {
                            if (files[i].exists()) {
                                files[i].delete();
                            }
                        }
                    }
                    mDownloadFile.delete();
                    continue;
                }

                if (mDownloadFile.isDirectory() && fileName.equals(mDownloadPath)) {
                    File[] downloadfiles = null;
                    downloadfiles = mDownloadFile.listFiles();

                    for (int i = 0; i < downloadfiles.length; i++) {
                        File mDownLoadFileEnd = downloadfiles[i];
                        String name = mDownLoadFileEnd.getPath();
                        name = name.substring(name.indexOf(FileUtil.PRO_DOWNLOAD_ROOT) + FileUtil.PRO_DOWNLOAD_ROOT.length() + 1);
                        File mProgramFile = new File(FileUtil.getProgramRoot(mContext), name);
                        if (!mProgramFile.exists()) {
                            mProgramFile.getParentFile().mkdirs();
                        }
                        try {
                            InputStream fis = new FileInputStream(mDownLoadFileEnd);
                            FileOutputStream fos = new FileOutputStream(mProgramFile);

                            byte[] buffer = new byte[1024];

                            int length = 0;
                            while ((length = fis.read(buffer)) != -1) {
                                fos.write(buffer, 0, length);
                            }
                            fis.close();
                            fos.close();
                            //删除下载目录下的文件
                            mDownLoadFileEnd.delete();
                            File[] files = mDownLoadFileEnd.getParentFile().listFiles();
                            int listSize = files.length;
                            while (files == null || listSize <= 0) {
                                mDownLoadFileEnd = mDownLoadFileEnd.getParentFile();
                                String path = mDownLoadFileEnd.getPath();
                                if (path.endsWith(FileUtil.PRO_DOWNLOAD_ROOT)) {
                                    break;
                                }
                                mDownLoadFileEnd.delete();

                                files = mDownLoadFileEnd.getParentFile().listFiles();
                                listSize = files.length;

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //替换图标
    private void replaceIcon(String url) {
        url = url.replace("\\", "/");
        String name;
        name = url.substring(0, url.indexOf("/"));
        if (carBeanList.size() > 0) {

            for (int i = 0; i < carBeanList.size(); i++) {
                NCarBean mNCarBean = carBeanList.get(i);
                if (mNCarBean == null) {
                    carBeanList.remove(carBeanList.get(i));
                }
                if (mNCarBean != null && (mNCarBean.getCarName_CN().equals(name) || mNCarBean.getCarName_EN().equals(name))) {
                    carBeanList.remove(mNCarBean);
                    //先删除原来版本的图标
                    String path = FileUtil.getProgramIcon(mContext) + mNCarBean.getLogoPath();

                    //移动图标
                    String logoPath = FileUtil.getProgramDownloadRoot(mContext) + url + "/" + mNCarBean.getLogoPath();
                    File file = new File(logoPath);
                    if (file.exists()) {
                        File programIconFile = new File(path);
                        if (!programIconFile.getParentFile().exists()) {
                            programIconFile.getParentFile().mkdirs();
                        }

                        try {
                            if (file.length() != programIconFile.length()) {
                                InputStream fis = new FileInputStream(file);
                                FileOutputStream fos = new FileOutputStream(programIconFile);
                                byte[] buffer = new byte[1024];

                                int length = 0;
                                while ((length = fis.read(buffer)) != -1) {
                                    fos.write(buffer, 0, length);
                                }
                                fis.close();
                                fos.close();
                                file.delete();//删除文件
                                //将文件父目录也删除
                                File filePath = file.getParentFile();
                                filePath.delete();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                    //插入数据库中,数据库中没有时才插入
                    if (!MDBHelper.getInstance(mContext).existCarBean(mNCarBean.getCarID())) {
                        MDBHelper.getInstance(mContext).insertCarBean(mNCarBean);
                    }
                }
            }
        }

    }


    /**
     * 删除任务
     * @param url
     */
    public void deleteUrl(String url) {
        //所有任务列表删除
        if (mUpgradeThreads != null) {
            for (int i = 0; i < mUpgradeThreads.size(); i++) {
                if (mUpgradeThreads.get(i).getUrl().equals(url)) {
                    mUpgradeThreads.remove(i);
                    i--;
                }
            }
        }
        //暂停列表删除
        if (mPauseThreads != null) {
            for (int i = 0; i < mPauseThreads.size(); i++) {
                if (mPauseThreads.get(i).getUrl().equals(url)) {
                    mPauseThreads.remove(i);
                    i--;
                }
            }
        }
        // 正在下载列表删除
        if (mDownloadThreads != null) {
            for (int i = 0; i < mDownloadThreads.size(); i++) {
                try {
                    if (mDownloadThreads.get(i).getUrl().equals(url)) {
                        mDownloadThreads.remove(i);
                        i--;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //数据库 下载表 删除
        MDBHelper.getInstance(mContext).deleteUrl(url);

    }

    boolean noWIFIDownloadEnable = false;

    private int mDownCount = 0;

    /**
     * 用于记录正在下载的线程个数
     */
    private int threadCount = 0;

    public int getThreadCount() {
        return threadCount;
    }

    public void setThreadCount(int threadCount) {
        this.threadCount = threadCount;
    }

    /**
     * 开始下载
     */
    public void startDownload() {
        if (!WifiUtil.isSupportNetwork(mContext)) {
            Toast.makeText(mContext, mContext.getResources().getString(R.string.tip_message_net_no_conn), Toast.LENGTH_SHORT).show();
            pauseAllDownload();
            mHandler.sendEmptyMessage(MSG_REFRESH_LIST);
            return;
        }

        /*if (WifiUtil.isSupportWifi(mContext) || noWIFIDownloadEnable){*/
        //改变状态为 下载
        isStarted = true;
        //刷新 下载列表
        initDownload();

        if (mUpgradeThreads != null && mUpgradeThreads.size() == 0 && mDownloadThreads != null && mDownloadThreads.size() == 0) {
            remoteViews.setTextViewText(R.id.notifi_downloaded_num, "0个正在下载");
            remoteViews.setTextViewText(R.id.notifi_downloading, "0个等待下载");
            //mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
            mNotificationManager.cancel(NOTIFICATION_ID);
        }
        //启动下载线程
        if (mDownloadThreads != null && mDownloadThreads.size() > 0) {
            try {
                mDownCount = 0;
                isAllPauseUrl = true;
                if (mDownloadThreads.size() >= DOWN_COUNT) {
                    for (int i = 0; i < DOWN_COUNT; i++) {
                        mDownCount++;
                        mDownloadThreads.get(i).startDownload(new MyDownloadThread.DownLoadFailedListener() {
                            @Override
                            public void onFailed(int code) {
                                //在下载过程中下载失败，返回，将状态改为停止，并加入停止列表中
                                mHandler.sendEmptyMessage(MSG_ITEM_PUASE);
                            }

                        });
                    }
                } else if (mDownloadThreads.size() < DOWN_COUNT) {
                    for (int i = 0; i < mDownloadThreads.size(); i++) {
                        mDownCount++;
                        mDownloadThreads.get(i).startDownload(new MyDownloadThread.DownLoadFailedListener() {
                            @Override
                            public void onFailed(int code) {
                                //在下载过程中下载失败，返回，将状态改为停止，并加入停止列表中
                                mHandler.sendEmptyMessage(MSG_ITEM_PUASE);
                            }

                        });
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            remoteViews.setTextViewText(R.id.notifi_downloaded_num, mDownCount + "个正在下载");
            remoteViews.setTextViewText(R.id.notifi_downloading, mUpgradeThreads.size() - mDownCount + "个等待下载");
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());

        }
    }

    /**
     * 刷新下载状态
     */
    public void refreshDowndload() {
        //下载状态开启时，启动下载任务
        if (isStarted()) {
            startDownload();
        }

        //下载状态关闭时，将正在下载的任务改变为 等待状态
        else {
            for (ProgramDownLoadThread thread : mUpgradeThreads) {
                if (isAllPauseUrl) {
                    boolean contain = false;
                    if (mPauseThreads != null) {
                        for (int i = 0; i < mPauseThreads.size(); i++) {
                            if (mPauseThreads.get(i).getUrl().equals(thread.getUrl())) {
                                contain = true;
                                break;
                            }
                        }
                    }

                    if (!contain) {
                        mPauseThreads.add(thread);
                    }
                }
            }
            isAllPauseUrl = false;
            if (mDownloadThreads != null) {
                mDownloadThreads.clear();
            }

            remoteViews.setTextViewText(R.id.notifi_downloaded_num, "全部暂停下载");
            remoteViews.setTextViewText(R.id.notifi_downloading, "");
            mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
        }
        //更新页面显示列表
        mHandler.sendEmptyMessage(MSG_REFRESH_LIST);

    }

    /**
     * 暂停所有下载任务
     */
    public void pauseAllDownload() {
        isStarted = false;
        for (ProgramDownLoadThread thread : mUpgradeThreads) {
            thread.pauseDownload();
        }
        DownloadManager.getInstance(mContext).setThreadCount(0);
        isAllPauseUrl = true;
        refreshDowndload();

    }

    /**
     * 设置 @url 状态变为下载
     * @param url
     */

    Boolean isAllPauseUrl = false;

    public void startUrl(String url, int status) {
        isStarted = true;
        isAllPauseUrl = true;
        //当状态是停止时的开始下载情况
        if (status == DownloadDB.STATUS_PAUSE) {
            for (int i = 0; i < mPauseThreads.size(); i++) {
                ProgramDownLoadThread thread = mPauseThreads.get(i);
                if (url.equals(thread.getUrl())) {
                    if (mDownloadThreads == null) {
                        mDownloadThreads = new ArrayList<>();
                    }
                    if (mDownloadThreads.size() > 0 && mDownloadThreads.size() == DOWN_COUNT) {
                        ProgramDownLoadThread downLoadthread = mDownloadThreads.get(0);
                        downLoadthread.waitDownload();
                        mDownloadThreads.remove(downLoadthread);
                    }

                    thread.setStatus(DownloadDB.STATUS_DOWNLOAD);
                    MDBHelper.getInstance(mContext).updateDownStatus(url, DownloadDB.STATUS_DOWNLOAD);
                    mPauseThreads.remove(thread);
                    if (!mDownloadThreads.contains(thread)) {
                        mDownloadThreads.add(thread);
                    }

                }
            }
            //当状态是等待时的开始下载情况
        } else if (status == DownloadDB.STATUS_WAIT) {
            for (int j = 0; j < mUpgradeThreads.size(); j++) {
                ProgramDownLoadThread thread = mUpgradeThreads.get(j);
                if (url.equals(thread.getUrl())) {
                    if (mDownloadThreads == null) {
                        mDownloadThreads = new ArrayList<>();
                    }
                    if (mDownloadThreads.size() > 0 && mDownloadThreads.size() == DOWN_COUNT) {
                        ProgramDownLoadThread downLoadthread = mDownloadThreads.get(0);
                        downLoadthread.waitDownload();
                        mDownloadThreads.remove(downLoadthread);
                    }
                    thread.setStatus(DownloadDB.STATUS_DOWNLOAD);
                    MDBHelper.getInstance(mContext).updateDownStatus(url, DownloadDB.STATUS_DOWNLOAD);
                    if (!mDownloadThreads.contains(thread)) {
                        mDownloadThreads.add(thread);
                    }
                }
            }
        }

        refreshDowndload();

    }

    /**
     * 设置 @url 状态变为等待
     * @param url
     */
    public void waitUrl(String url) {
        for (int i = 0; i < mPauseThreads.size(); i++) {
            if (url.equals(mPauseThreads.get(i).getUrl())) {
                ProgramDownLoadThread thread = mPauseThreads.get(i);
                thread.setStatus(DownloadDB.STATUS_WAIT);
                MDBHelper.getInstance(mContext).updateDownStatus(url, DownloadDB.STATUS_WAIT);
                mPauseThreads.remove(thread);
            }
        }
        refreshDowndload();

    }

    /**
     * 暂停 @url 任务
     * @param url
     */
    public void pauseUrl(String url) {
        for (ProgramDownLoadThread thread : mUpgradeThreads) {
            if (thread.getUrl().equals(url)) {
                thread.pauseDownload();
                if (mDownloadThreads != null) {
                    mDownloadThreads.remove(thread);
                    Log.e("pauseUrl", "pauseUrl" + thread.getFileName());
                }

                boolean contain = false;
                for (int i = 0; i < mPauseThreads.size(); i++) {
                    if (mPauseThreads.get(i).getUrl().equals(thread.getUrl())) {
                        contain = true;
                        break;
                    }
                }
                if (!contain) {
                    mPauseThreads.add(thread);
                }
            }
        }
        Log.e("mPauseThreads", "pauseUrl: " + mPauseThreads.size());
        isAllPauseUrl = true;

        refreshDowndload();
    }

    /**
     * 获取下载状态
     * @return
     */
    public boolean isStarted() {
        return isStarted;
    }


    /**
     * 请求任务
     * 刷新页面显示列表
     */
    public void requestThreads() {
        mHandler.sendEmptyMessage(MSG_REFRESH_LIST);
    }

    /**
     * 获取所有下载任务
     * @return
     */
    public CopyOnWriteArrayList<ProgramDownLoadThread> getUpgradeThreads() {
        return mUpgradeThreads;
    }

    /**
     * 获取正在下载的任务
     * @return
     */
    public ArrayList<ProgramDownLoadThread> getmDownloadThreads() {
        return mDownloadThreads;
    }

    public ArrayList<ProgramDownLoadThread> getPauseThreads() {
        return mPauseThreads;
    }

    /**
     * 刷新页面显示列表
     */
    public void onChange() {
        if (mChangeListener != null) {
            ArrayList<ProgramDownLoadThread> tempThreads = new ArrayList<>();
            tempThreads.addAll(mUpgradeThreads);
            mChangeListener.onChange(mUpgradeThreads);
        }
    }

    /**
     * 刷新 进度
     * 刷新页面显示列表
     */
    public void queryProgress() {
        mHandler.sendEmptyMessage(MSG_REFRESH_LIST);
    }

    public void queryProgressPerSecond() {
        mHandler.sendEmptyMessage(MSG_REQUEST_PROGRESS);

    }

    public void setOnChangeListener(OnDownloadChangeListener listener) {
        mChangeListener = listener;
    }


    /**
     * 对话框提示 替换文件
     */
    private void tipReplaceFile() {
        try {
            if (mFinishSet != null && mFinishSet.size() > 0) {
                final Map<String, String> map = mFinishSet.get(0);
                final String url = URLDecoder.decode(map.get(kURL), "utf-8");
                final String fileName = map.get(kFileName);
                Log.e("FinishSet", mFinishSet.size() + "");
                mFinishSet.remove(map);
                checkOut(url, fileName);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void checkOut(final String url, String fileName) {
        fileName = fileName.replace("\\", "/");
        boolean isCheckError = false; //判断检验是否正确
        int binCount = 0;
        try {
            String LocalName = fileName.substring(0, fileName.lastIndexOf("/"));
            String downloadRoot = FileUtil.getProgramDownloadRoot(mContext);
            File fileDownloadRoot = new File(downloadRoot + LocalName);
            File[] fileDownloadList = fileDownloadRoot.listFiles();
            for (int j = 0; j < fileDownloadList.length; j++) {
                File mDownloadFile = fileDownloadList[j];
                String mDownloadPath = "";
                try {
                    mDownloadPath = mDownloadFile.getPath();//mDownloadFile.getParent() + "/" + mDownloadFile.getName().substring(5);
                    mDownloadPath = mDownloadPath.substring(mDownloadPath.indexOf(FileUtil.PRO_DOWNLOAD_ROOT) + FileUtil.PRO_DOWNLOAD_ROOT.length() + 1, mDownloadPath.lastIndexOf("_v"));
                } catch (Exception ex) {
                    /**
                     * 不是保存 bin 文件的目录
                     */
                    continue;
                }

                if (mDownloadFile.isDirectory() && fileName.equals(mDownloadPath)) {
                    File[] downloadfiles = null;
                    downloadfiles = mDownloadFile.listFiles();

                    for (int i = 0; i < downloadfiles.length; i++) {
                        File mDownLoadFileEnd = downloadfiles[i];
                        String name = mDownLoadFileEnd.getPath();
                        if (name.toLowerCase().endsWith(".bin")) {
                            String hex = FileUtil.readBinFile(mDownLoadFileEnd);
                            String data = HexDump.toHexString(FileUtil.readHexData(mContext, name));
                            data = data.replace(" ", "");
                            binCount++;
                            if (!hex.equals(data)) {
                                mDownLoadFileEnd.delete();
                                isCheckError = true;
                                break;
                            }
                        }
                    }
                }
            }

            //校验成功
            if (!isCheckError || binCount > 0) {
                //确认替换
                try {
                    replaceIcon(url); //替换图标
                    replaceProgram(fileName); //替换程序
                    MDBHelper.getInstance(mContext).deleteCurrentVersion(url);
                    if (UpgradeFragment.getInstance() != null && UpgradeFragment.getInstance().isVisible()) {
                        UpgradeFragment.getInstance().refresh();
                    }
                    Toast.makeText(mContext, mContext.getResources().getString(R.string.tip_program) + url
                            + mContext.getResources().getString(R.string.tip_downLoad_Finished), Toast.LENGTH_SHORT).show();
                    mHandler.sendEmptyMessage(MSG_REPLACE_PROGRAM_FILE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            } else {

                new TipDialog.Builder(mContext)
                        .setTitle(mContext.getResources().getString(R.string.tip_title))
                        .setMessage(mContext.getResources().getString(R.string.tip_program) + url + mContext.getString(R.string.upgrade_Failed))
                        .setPositiveClickListener(mContext.getResources().getString(R.string.tip_yes), new TipDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialogInterface, int index, String label) {
                                try {
                                    addUrl(URLEncoder.encode(url, "utf-8"));
                                    if (DownloadManager.getInstance(mContext).isStarted()) {
                                        DownloadManager.getInstance(mContext).startDownload();
                                    }
                                    dialogInterface.dismiss();
                                    mHandler.sendEmptyMessage(MSG_REPLACE_PROGRAM_FILE);
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .setNegativeClickListener(mContext.getResources().getString(R.string.tip_no), new TipDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialogInterface, int index, String label) {
                                dialogInterface.dismiss();
                                mHandler.sendEmptyMessage(MSG_REPLACE_PROGRAM_FILE);
                            }
                        })
                        .requestSystemAlert(true)
                        .build().show();

                for (int j = 0; j < fileDownloadList.length; j++) {
                    File mDownloadFile = fileDownloadList[j];
                    String mDownloadPath = "";
                    mDownloadPath = mDownloadFile.getPath();//mDownloadFile.getParent() + "/" + mDownloadFile.getName().substring(5);
                    mDownloadPath = mDownloadPath.substring(mDownloadPath.indexOf(FileUtil.PRO_DOWNLOAD_ROOT) + FileUtil.PRO_DOWNLOAD_ROOT.length() + 1, mDownloadPath.lastIndexOf("_v"));

                    if (mDownloadFile.isDirectory() && fileName.equals(mDownloadPath)) {
                        while (!("/" + mDownloadFile.getName()).equals(FileUtil.PRO_DOWNLOAD_ROOT)) {
                            mDownloadFile.delete();
                            mDownloadFile = mDownloadFile.getParentFile();
                            if (mDownloadFile.listFiles() != null) {
                                break;
                            }
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


    public static final int MSG_REFRESH_LIST = 1;
    public static final int MSG_REQUEST_PROGRESS = 2;
    public static final int MSG_REPLACE_PROGRAM_FILE = 3;
    public static final int MSG_ITEM_PUASE = 4;


    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REFRESH_LIST:
                    onChange();
                    break;
                case MSG_REQUEST_PROGRESS:
                    queryProgress();
                    mHandler.sendEmptyMessageDelayed(MSG_REQUEST_PROGRESS, 1000);
                    break;
                case MSG_REPLACE_PROGRAM_FILE:
                    if (msg.obj != null) {
                        final Map<String, String> map = (Map<String, String>) msg.obj;
                        mFinishSet.add(map);
                        Log.e("handleMessage", "handleMessage: " + map);
                    }
                    tipReplaceFile();
                    break;
                case MSG_ITEM_PUASE:
                    for (int i = 0; i < mUpgradeThreads.size(); i++) {
                        ProgramDownLoadThread thread = mUpgradeThreads.get(i);
                        if (thread.getStatus() == DownloadDB.STATUS_PAUSE) {
                            if (mDownloadThreads.size() > 0) {
                                mDownloadThreads.remove(thread);
                            }

                            boolean contain = false;
                            for (int j = 0; i < mPauseThreads.size(); i++) {
                                if (mPauseThreads.get(i).getUrl().equals(thread.getUrl())) {
                                    contain = true;
                                    break;
                                }
                            }
                            if (!contain) {
                                mPauseThreads.add(thread);
                            }
                        }
                    }
                    refreshDowndload();
                    break;
                default:
                    break;
            }
        }
    };


}
