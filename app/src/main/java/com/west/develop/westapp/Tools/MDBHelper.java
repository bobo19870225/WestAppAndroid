package com.west.develop.westapp.Tools;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Bean.AppBean.AuthBean;
import com.west.develop.westapp.Bean.NCarBean;
import com.west.develop.westapp.Bean.Upgrade.DownloadDB;
import com.west.develop.westapp.Bean.Upgrade.UpdateDB;

import java.util.ArrayList;

import java.util.Locale;


/**
 * Created by Develop0 on 2017/8/31.
 */

public class MDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "west_DIAG_DB";
    public static final int CUR_VERSION = 1;

    private static final String TB_CAR = "TB_WEST_CAR";
    private static final String kCarID = "kCarId";
    private static final String kCarName_CN = "kCarName_CN";
    private static final String kCarName_EN = "kCarName_EN";
    private static final String kBinROOT  = "kBinROOT";
    private static final String kCarNumber ="kCarNumber";
    private static final String kLogoPath = "kLogoPath";

    private static final String TB_PRO_DOWNLOAD ="TB_Program_Download";
    private static final String kDownPath = "kDownloadPath";
    private static final String kDownFileName = "kDownFileName";
    private static final String kDownStatus = "kDownStatus";
    private static final String kDownContentSize = "kDownContentSize";

    private static final String TB_CAR_UPDATE = "TB_CAR_UPDATE";
    private static final String kUpdateName = "kUpdateName";
    private static final String kUpdateAuth = "kUpdateAuth";
    private static final String Sort_Name_CN = kCarName_CN + " COLLATE LOCALIZED ";
    private static final String Sort_Name_EN = kCarName_EN;
    private static String mSortBy = kCarID;

    private static final String TB_AuthProgram = "TB_AuthProgram";
    private static final String kAuthDeviceSN = "kDeviceSN";
    private static final String kAuthProgramCode = "kProgramCode";
    private static final String kAuthPermission = "kPermission";


    private static Context mContext;
    private static MDBHelper instance;


    /**
     * 获取单例
     * @param context
     * @return
     */
    public static MDBHelper getInstance(Context context){
        if(instance == null){
            mContext = context.getApplicationContext();
            instance = new MDBHelper(mContext);
        }

       refreshSortBy(Config.getInstance(mContext).getSortBy());
        return instance;
    }

    public MDBHelper(Context context) {
        super(context, DB_NAME, null, CUR_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE if not exists " + TB_CAR + " ( " +
                kCarID + " VARCHAR PRIMARY KEY ," +
                kCarName_CN + " VARCHAR NOT NULL ," +
                kCarName_EN + " VARCHAR NOT NULL ," +
                kBinROOT + " VARCHAR , " +
                kCarNumber + " Integer NOT NULL , " +
                kLogoPath + " VARCHAR " +
                ")");

        try {
            db.execSQL("CREATE TABLE if not exists " + TB_PRO_DOWNLOAD + " ( " +
                    kDownPath + " VARCHAR(100) PRIMARY KEY ," +
                    kDownFileName + " VARCHAR(100) ," +
                    kDownStatus + " INTEGER check(" +
                        kDownStatus + "=" + DownloadDB.STATUS_DOWNLOAD + " or " +
                        kDownStatus + "=" + DownloadDB.STATUS_PAUSE + " or " +
                        kDownStatus + "=" + DownloadDB.STATUS_WAIT + ")," +
                    kDownContentSize + " INGEGER " +
                    ")");
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

        try {
            db.execSQL("CREATE TABLE if not exists " + TB_CAR_UPDATE + " ( " +
                    kUpdateName + " VARCHAR(100) PRIMARY KEY," +
                    kUpdateAuth + " BOOLEAN " +
                    ")");
        }catch (Exception e){
            e.printStackTrace();
        }

        try{
            db.execSQL("CREATE TABLE if not exists " + TB_AuthProgram + " ( " +
                    kAuthDeviceSN + " VARCHAR(10) ," +
                    kAuthProgramCode + " VARCHAR(4) ," +
                    kAuthPermission + " INTEGER check(" +
                    kAuthPermission + "=" + AuthBean.AUTH_NO + " or " +
                    kAuthPermission + "=" + AuthBean.AUTH_YES +  ")" +
                    ")");
        }
        catch (Exception e){

        }

    }

    public void deleteDownload(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+  TB_PRO_DOWNLOAD);
    }

    //删除所有数据库中的数据
    public void deleteAppDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+  TB_CAR);
        db.execSQL("delete from "+  TB_AuthProgram);
        db.execSQL("delete from "+  TB_CAR_UPDATE);
        db.execSQL("delete from "+  TB_PRO_DOWNLOAD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    /**
     * 刷新排序方式
     * @param sortBy
     */
    public static void  refreshSortBy(int sortBy){
        if(sortBy == Config.SORT_BY_NUMBER){
            mSortBy = kCarNumber;
        }
        if(sortBy == Config.SORT_BY_PINYIN){
            if(Locale.getDefault() == Locale.SIMPLIFIED_CHINESE) {
                mSortBy = Sort_Name_CN;
            }
            if(Locale.getDefault() == Locale.ENGLISH){
                mSortBy = Sort_Name_EN;
            }
        }
        if(sortBy == Config.SORT_BY_ID){
            mSortBy = kCarID;
        }
        Config.getInstance(mContext).setSortBy(sortBy);
    }
    /**
     * 获取汽车列表
     * @param sortBy
     * @return
     */
    public  ArrayList<NCarBean> getCarList(int sortBy){
        ArrayList<NCarBean> list = new ArrayList<>();

        refreshSortBy(sortBy);

        String sql = "select * from " + TB_CAR   + " order by " +  mSortBy;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(sql,null);

        while (cursor.moveToNext()){
            NCarBean carBean = new NCarBean();
            carBean.setCarID(cursor.getString(cursor.getColumnIndex(kCarID)));
            carBean.setCarName_CN(cursor.getString(cursor.getColumnIndex(kCarName_CN)));
            carBean.setCarName_EN(cursor.getString(cursor.getColumnIndex(kCarName_EN)));
            carBean.setBinRoot(cursor.getString(cursor.getColumnIndex(kBinROOT)));
            carBean.setNumber(cursor.getInt(cursor.getColumnIndex(kCarNumber)));
            carBean.setLogoPath(cursor.getString(cursor.getColumnIndex(kLogoPath)));
            //updateBinRoot(carBean);
            list.add(carBean);
        }

        //String json = getListJson(list);

        return list;
    }

    /**
     * 插入汽车
     * 在添加汽车时使用
     * @param carBean
     */
    public void insertCarBean(NCarBean carBean){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(kCarID,carBean.getCarID());
        values.put(kCarName_CN,carBean.getCarName_CN());
        values.put(kCarName_EN,carBean.getCarName_EN());
        values.put(kBinROOT,carBean.getBinRoot());
        values.put(kCarNumber,carBean.getNumber());
        values.put(kLogoPath,carBean.getLogoPath());

        try {
            db.insert(TB_CAR, null, values);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public boolean existCarBean(String carId){
        SQLiteDatabase database = this.getReadableDatabase();

        Cursor cursor = database.rawQuery("select * from " + TB_CAR + " where " + kCarID + "=?",new String[]{carId});

        if(cursor.moveToNext()){
            return true;
        }
        return false;
    }


    public ArrayList<DownloadDB> getDownloadList(int status){
        SQLiteDatabase db = this.getReadableDatabase();
        ArrayList<DownloadDB> list = new ArrayList<>();
        Cursor cursor;
        if(status == DownloadDB.STATUS_DOWNLOAD || status == DownloadDB.STATUS_PAUSE || status == DownloadDB.STATUS_WAIT) {
            cursor = db.rawQuery("select * from " + TB_PRO_DOWNLOAD + " where " + kDownStatus + "=?", new String[]{status + ""});
        }
        else{
            cursor = db.rawQuery("select * from " + TB_PRO_DOWNLOAD , null);
        }

        while (cursor.moveToNext()) {
            DownloadDB bean = new DownloadDB();
            bean.setUrl(cursor.getString(cursor.getColumnIndex(kDownPath)));
            bean.setFileName(cursor.getString(cursor.getColumnIndex(kDownFileName)));
            bean.setStatus(cursor.getInt(cursor.getColumnIndex(kDownStatus)));
            bean.setContentSize(cursor.getLong(cursor.getColumnIndex(kDownContentSize)));
            list.add(bean);
        }
        return list;
    }


    public void insertDownloadUrl(DownloadDB bean){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(kDownPath,bean.getUrl());
        values.put(kDownFileName,bean.getFileName());
        values.put(kDownStatus,bean.getStatus());

        try{
            db.insert(TB_PRO_DOWNLOAD,null,values);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public void updateFileNameAndSize(String url,String fileName,long size){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(kDownFileName,fileName);
        values.put(kDownContentSize,size);

        try {
            db.update(TB_PRO_DOWNLOAD, values, kDownPath + "=?", new String[]{url});
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public void updateDownStatus(String url,int status){
        if(url == null || url.length() <= 0){
            return;
        }
        if(status != DownloadDB.STATUS_PAUSE && status != DownloadDB.STATUS_WAIT && status !=DownloadDB.STATUS_DOWNLOAD){
            return;
        }

        ContentValues values = new ContentValues();
        values.put(kDownStatus,status);
        SQLiteDatabase db = getWritableDatabase();
        db.update(TB_PRO_DOWNLOAD,values,kDownPath + "=?" ,new String[]{url});

    }

    public void deleteUrl(String url){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TB_PRO_DOWNLOAD,kDownPath + "=?",new String[]{url});

    }
    public void deleteTb(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TB_CAR_UPDATE,null,null);

    }


    public void insertUpdate(UpdateDB bean){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(kUpdateName, bean.getProgramName());
        values.put(kUpdateAuth,bean.isAuthen());
        try {
            /**
             * 如果数据库中存在该条数据，就不插入
             */
            Cursor cursor = db.rawQuery("select * from " + TB_CAR_UPDATE + " where " + kUpdateName +" = ? " , new String[]{bean.getProgramName()});
            if (!cursor.moveToNext()){
                db.insert(TB_CAR_UPDATE, null, values);
            }
            cursor.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public ArrayList<UpdateDB> getUpdateList(){
        SQLiteDatabase db = this.getReadableDatabase();

        ArrayList<UpdateDB> list = new ArrayList<UpdateDB>();
        Cursor cursor = db.rawQuery("select * from " + TB_CAR_UPDATE , null);
        while (cursor.moveToNext()){
            UpdateDB updateDB = new UpdateDB();
            updateDB.setProgramName(cursor.getString(cursor.getColumnIndex(kUpdateName)));
            int auth = cursor.getInt(cursor.getColumnIndex(kUpdateAuth));

            updateDB.setAuthen(auth != 0);
            list.add(updateDB);
        }
        
        return list;
    }


    public void deleteCurrentVersion(String programName) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TB_CAR_UPDATE, kUpdateName + "=?", new String[]{programName});
    }


    /**
     * 往授权表中插入程序
     * @param updateDB
     */
    public void insertProgramAuth(UpdateDB updateDB){
        if(!Config.getInstance(mContext).isSigned()){
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TB_AuthProgram +
                " where " + kAuthProgramCode + "=?",
                new String[]{updateDB.getAuthCode()});
        if(cursor.moveToNext()){
            ContentValues values = new ContentValues();
            values.put(kAuthPermission,updateDB.isAuthen()?AuthBean.AUTH_YES:AuthBean.AUTH_NO);
            values.put(kAuthDeviceSN,Config.getInstance(mContext).getBondDevice().getDeviceSN());
            db.update(TB_AuthProgram,values,
                   kAuthProgramCode + "=?",
                    new String[]{updateDB.getAuthCode()});
        }
        else{
            ContentValues values = new ContentValues();

            values.put(kAuthDeviceSN,Config.getInstance(mContext).getBondDevice().getDeviceSN());
            values.put(kAuthProgramCode,updateDB.getAuthCode());
            values.put(kAuthPermission,updateDB.isAuthen()?AuthBean.AUTH_YES:AuthBean.AUTH_NO);
            db.insert(TB_AuthProgram,null,values);
        }
    }
/*

    public boolean isProgramAuthen(String authCode){
        if(!Config.getInstance(mContext).isSigned()){
            return false;
        }
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("select * from " + TB_AuthProgram +
                " where " + kAuthProgramCode + "=? and " + kAuthDeviceSN + "=?",
                new String[]{authCode,Config.getInstance(mContext).getBondDevice().getDeviceSN()});

        if(cursor.moveToNext()){
            int permission = cursor.getInt(cursor.getColumnIndex(kAuthPermission));
            return permission == AuthBean.AUTH_YES;
        }
        return false;
    }
*/


    @Override
    public synchronized void close() {
        super.close();
    }
}
