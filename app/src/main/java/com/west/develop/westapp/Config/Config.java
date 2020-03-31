package com.west.develop.westapp.Config;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.google.gson.Gson;
import com.west.develop.westapp.Application.MyApplication;
import com.west.develop.westapp.Bean.AppBean.DeviceBean;
import com.west.develop.westapp.Bean.AppBean.Preference;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by Develop0 on 2017/8/30.
 */

public class Config {
    private static final String KEY_SHAREDP = "west_Config_SHAREDP";
    private static final String kSharedPSave = "sharedP_Config_OBJ";

    public static final int MAX_NUM = 7;
    public static final int MIN_NUM = 3;

    public static final float MAX_TEXTSIZE = 30;
    public static final float MIN_TEXTSIZE = 10;

    /**
     * 主页图标 排序方式
     */
    public static final int SORT_BY_PINYIN = 1;
    public static final int SORT_BY_NUMBER = 2;
    public static final int SORT_BY_ID = 3;


    /**
     * 语言切换
     */
    public static final int LANGUAGE_CH = 1;
    public static final int LANGUAGE_EN = 2;

    /**
     * 检查设备的最高次数
     */
    private int setRegCount = 0;
    private int RegCount = 0;

    public static final int TIP_REGCOUNT = 20;
    /**
     * 试用最高次数
     */
    public static final int TRYCOUNT = 10;
    public static final int TIP_CHECKTIME = 10;
    public static final int TIP_TRYCOUNT = 5;

    private static SharedPreferences mSharepP;

    private static Config instance;

    private static Context mContext;

    public static boolean checked = false;

    private int sortBy = SORT_BY_ID;


    private int iconNum = 5;
    private float textSize = 15;


    private boolean isFirstRun = true;
    private boolean toggle = false ;

    //默认中文
    private int language = LANGUAGE_CH;

   // private User user;

    private boolean isSigned = false;

    private DeviceBean bondDevice; //平板与设备绑定

    private boolean isConfigured = false; //标志是否配置完成

    private Calendar mLastDate = null ;


    private Preference mPreference = new Preference();


    //免责申明已同意
    private boolean isAgreed = false;


    private ArrayList<String> mBTNameList = null;

    public static Config getInstance(Context context){
        mContext = context.getApplicationContext();
        if(mSharepP == null){
            mSharepP = mContext.getSharedPreferences(KEY_SHAREDP,Context.MODE_PRIVATE);
        }

        if(instance == null){
            getInstanceFromShareP();
        }

        return instance;
    }

    private static void getInstanceFromShareP(){
        String configStr = mSharepP.getString(kSharedPSave,"");

        try{
            Gson gson = new Gson();
            instance = gson.fromJson(configStr,Config.class);
        }
        catch (Exception ex){

        }
        if(instance == null){
            synchronized (Config.class){
                instance = new Config();
            }
        }

    }

    private void save(){
        Gson gson = new Gson();
        String configStr = gson.toJson(instance);
        SharedPreferences.Editor editor = mSharepP.edit();
        editor.putString(kSharedPSave,configStr);
        editor.commit();
    }

    public int getIconNum() {
        return iconNum;
    }

    public void setIconNum(int iconNum) {
        if (iconNum >= MAX_NUM){
            iconNum = MAX_NUM;
        }
        if (iconNum <= MIN_NUM){
            iconNum = MIN_NUM;
        }
        this.iconNum = iconNum;
        save();
    }

    public float getTextSize() {
        return textSize;
    }

    public void setTextSize(float textSize) {
        if (textSize >= MAX_TEXTSIZE){
            textSize = MAX_TEXTSIZE;
        }
        if (textSize <= MIN_TEXTSIZE){
            textSize = MIN_TEXTSIZE;
        }
        this.textSize = textSize;
        save();
    }

    public int getSortBy() {
        return sortBy;
    }

    public void setSortBy(int sortBy) {
        if(sortBy != SORT_BY_ID && sortBy != SORT_BY_NUMBER && sortBy != SORT_BY_PINYIN){
            return;
        }
        this.sortBy = sortBy;
        save();
    }


    public boolean isFirstRun() {
        return isFirstRun;
    }

    public void setFirstRun(boolean firstRun) {
        isFirstRun = firstRun;
        save();
    }

    public boolean isToggle() {
        return toggle;
    }

    public void setToggle(boolean toggle) {
        this.toggle = toggle;
        save();
    }

    public int getLanguage() {
        return language;
    }

    public void setLanguage(int language) {
        if (language == LANGUAGE_CH || language == LANGUAGE_EN){
            this.language = language;
        }
       save();
    }


    public boolean isSigned() {
        if(isSigned && bondDevice == null){
            setSigned(false);
        }
        return isSigned;
    }

    public void setSigned(boolean signed) {
        isSigned = signed;
        save();

        /**
         * 发送刷新设备广播
         */
        Intent intent = new Intent(MyApplication.ACTION_DEVICE_REFRESH);
        mContext.sendBroadcast(intent);
    }

    /**
     * 获取设备IMEI码
     *
     * @param context
     * @return
     */
    public static String getAndroidID(Context context) {
        String androidID;
        try {
            androidID = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            androidID = "";
        }
        return androidID;
    }

    public DeviceBean getBondDevice() {
        return bondDevice;
    }

    public void setBondDevice(DeviceBean bondDevice) {
        if(bondDevice == null){
            setSigned(false);
        }
        this.bondDevice = bondDevice;
        save();
    }

    public boolean isConfigured() {
        return isConfigured;
    }

    public void setConfigured(boolean configured) {
        this.isConfigured = configured;
        save();
    }

    public int getRegCount(){
        return RegCount;
    }

    public void setRegCount(int count){
        RegCount = count;
        save();
    }

    public void addRegCount(){
        RegCount = RegCount + 1;
        save();
    }

    public void setSetRegCount(int count){
        setRegCount = count;
        save();
    }

    public int getSetRegCount(){
        return setRegCount;
    }

    public Calendar getLastDate() {
        if(mLastDate == null){
            mLastDate = Calendar.getInstance();
        }
        return mLastDate;
    }

    /**
     * 设置时间为一个月之后
     * @return
     */
    public void setLastDate(Calendar mLastDate) {
        this.mLastDate = mLastDate;
        mLastDate.add(Calendar.DAY_OF_MONTH,30);
        save();
    }


    public ArrayList<String> getBTList() {
        return mBTNameList;
    }

    public void addBTName(String name){
        if(mBTNameList == null){
            mBTNameList = new ArrayList<>();
        }
        if(name == null || name.isEmpty()){
            return;
        }

        if(!mBTNameList.contains(name)){
            mBTNameList.add(name);
        }
        save();
    }

    public int getTimeoutActive(){
        return mPreference.getTimeoutActive();
    }

    public int getTimeoutBackup(){
        return mPreference.getTimeoutBackup();
    }

    public void setTimeoutActive(int minuts){
        mPreference.setTimeoutActive(minuts);
        save();
    }

    public void setTimeoutBackup(int seconds){
        mPreference.setTimeoutBackup(seconds);
        save();
    }

    public boolean isAgreed() {
        return isAgreed;
    }

    public void setAgreed(boolean agreed) {
        isAgreed = agreed;
        save();
    }
}
