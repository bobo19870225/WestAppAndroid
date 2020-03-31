package com.west.develop.westapp.Download.Threads;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.west.develop.westapp.Tools.MDBHelper;
import com.west.develop.westapp.Bean.Upgrade.UpdateDB;
import com.west.develop.westapp.Bean.Upgrade.VersionBean;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Develop12 on 2017/9/26.
 */

public class CompareVersionThread extends Thread {

    private static CompareVersionThread instance;
    private static Context mContext;

    private ConcurrentHashMap<String,UpdateDB> mLocalVersions;
    private ConcurrentHashMap<String,UpdateDB> mGetVersions;
    private List<UpdateDB> mDisplayVersions;
    private List<UpdateDB> mDeleteVersions;

    private CompareCallback mCallBack;

    ReentrantLock compareLock = new ReentrantLock();

    private Handler mHandler = new Handler();

    public static CompareVersionThread getInstance(){
        return instance;
    }

    public static CompareVersionThread newInstance(Context context,
                                                   ConcurrentHashMap<String,UpdateDB> localList,
                                                   ConcurrentHashMap<String,UpdateDB> getList,
                                                   List<UpdateDB> displayList,
                                                   CompareCallback callback

    ){
        mContext = context;

        synchronized (CompareVersionThread.class) {
            instance = new CompareVersionThread();
        }


        instance.mLocalVersions = new  ConcurrentHashMap<String,UpdateDB>();
        if(localList == null){
            localList = new  ConcurrentHashMap<String,UpdateDB>();
        }
       // instance.mLocalVersions.putAll(localList);

        instance.mLocalVersions = localList;


        instance.mGetVersions = new ConcurrentHashMap<>();
        if(getList == null){
            getList = new ConcurrentHashMap<>();
        }
        //instance.mGetVersions.putAll(getList);
        instance.mGetVersions = getList;

        if(displayList == null){
            displayList = new ArrayList<>();
        }
        instance.mDisplayVersions = new ArrayList<>();
        instance.mDisplayVersions.addAll(displayList);

        instance.mDeleteVersions = new ArrayList<>();

        instance.mCallBack = callback;
        return instance;
    }


    private CompareVersionThread(){
    }

    @Override
    public void interrupt() {
        try {
            mLocalVersions.clear();
            mGetVersions.clear();
            mDisplayVersions.clear();
            compareLock.unlock();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
        super.interrupt();

        cancel();
    }

    @Override
    public synchronized void start() {
        if(mGetVersions == null || mGetVersions.isEmpty() /*|| mLocalVersions == null || mLocalVersions.isEmpty()*/){
           /* if(mCallBack != null){
                mCallBack.onFinish(mDisplayVersions);
            }*/
            return;
        }
        super.start();
    }

    @Override
    public void run() {
        compareLock.lock();
        if(mGetVersions == null || mGetVersions.isEmpty()){
            compareLock.unlock();
           interrupt();
            return;
        }
        if(mCallBack != null){
            mCallBack.onStart();
        }
       //MDBHelper.getInstance(mContext).deleteTb();
        mDisplayVersions.clear();

        boolean delete = (mGetVersions != null && !mGetVersions.isEmpty());
        Set<Map.Entry<String, UpdateDB>> allSetlist = mGetVersions.entrySet();
        Set<Map.Entry<String, UpdateDB>> allSetver = mLocalVersions.entrySet();
        Iterator<Map.Entry<String, UpdateDB>> itever = allSetver.iterator();

        /**
         *   当第一次安装app时，本地一个程序都没有时执行
         */
        if (mLocalVersions.size() == 0  && mGetVersions.size() > 0){
            Iterator<Map.Entry<String, UpdateDB>> it = allSetlist.iterator();
            while (it.hasNext()){
                try {
                    Map.Entry<String, UpdateDB> entry = it.next();
                    UpdateDB updateDB = entry.getValue();

                    mDisplayVersions.add(updateDB);
                    MDBHelper.getInstance(mContext).insertUpdate(updateDB);

                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            //与本地中存在的程序进行比较，看是否可以进行升级
        }else if (mGetVersions.size() > 0 && mLocalVersions.size() > 0) {
            while (itever.hasNext()) {
                try {
                    Map.Entry<String, UpdateDB> mever = itever.next();
                    UpdateDB mLocalUpdateDB = mever.getValue();

                    UpdateDB getUpdateDB = mGetVersions.get(mLocalUpdateDB.getProgramName());
                    if (getUpdateDB != null) {
                        if (getUpdateDB.getVersionList() != null && getUpdateDB.getVersionList().size() > 0) {
                            for (int i = 0; i < getUpdateDB.getVersionList().size(); i++) {
                                boolean contain = false;

                                VersionBean bean = getUpdateDB.getVersionList().get(i);
                                for (int j = 0; j < mLocalUpdateDB.getVersionList().size(); j++) {
                                    if (mLocalUpdateDB.getVersionList().get(j).getParentVersion().equals(bean.getParentVersion()) &&
                                            mLocalUpdateDB.getVersionList().get(j).getChildVersion().equals(bean.getChildVersion())) {
                                        contain = true;
                                        break;
                                    }
                                }
                                if (!contain) {
                                    mDisplayVersions.add(getUpdateDB);
                                    MDBHelper.getInstance(mContext).insertUpdate(getUpdateDB);
                                    break;
                                }

                            }
                        }

                        mLocalVersions.remove(mLocalUpdateDB.getProgramName());
                    }

                    mGetVersions.remove(mLocalUpdateDB.getProgramName());

                }
                catch (Exception ex){
                    ex.printStackTrace();
                }
            }
            /**
             * 本地中没有的车程序
             */
            Log.e("mGetVersions", String.valueOf(mGetVersions.size()));
            if (mGetVersions != null) {
                Iterator<Map.Entry<String, UpdateDB>> netVerIt = allSetlist.iterator();
                while (netVerIt.hasNext()) {
                    try {
                        Map.Entry<String, UpdateDB> mever = netVerIt.next();
                        UpdateDB bean = mever.getValue();

                        mDisplayVersions.add(bean);
                        MDBHelper.getInstance(mContext).insertUpdate(bean);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        Log.e("LocalSize",mLocalVersions.size() + "");

        if(delete) {
            mDeleteVersions.addAll(mLocalVersions.values());
        }
        compareLock.unlock();
        if (mCallBack != null) {
            mCallBack.onFinish(mDisplayVersions,mDeleteVersions);

        }
        Log.e("finish", "finished ");

        interrupt();
    }


    public static void cancel(){
        instance = null;
    }

    public interface CompareCallback{
        void onFinish(List<UpdateDB> displayList, List<UpdateDB> deleteList);
        void onStart();
    }
}
