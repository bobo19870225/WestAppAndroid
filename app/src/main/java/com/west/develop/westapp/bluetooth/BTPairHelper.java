package com.west.develop.westapp.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Develop0 on 2017/8/22.
 */

public class BTPairHelper {

    private static BTPairHelper instance;

    private static Activity mActivity;

    private PairBroadcastReceiver mPairBroadcastReceiver;

    public static BTPairHelper getInstance(Activity activity){
        mActivity = activity;
        if(instance == null){
            instance = new BTPairHelper();
        }
        return instance;
    }

    //配对蓝牙接口回调
    private final IPairCallback pairCallback = new IPairCallback() {
        @Override
        public void unBonded() {
            Log.i("BTPair","unBonded");
        }

        @Override
        public void bonding() {
            Log.i("BTPair","bonding");
        }

        @Override
        public void bonded() {
            Log.i("BTPair","bonded");
        }

        @Override
        public void bondFail() {
            Log.i("BTPair","bondFail");
        }
    };

    //配对蓝牙
    public void startPair(IPairCallback callback) {
        if (mPairBroadcastReceiver == null) {
            mPairBroadcastReceiver = new PairBroadcastReceiver(callback);
        }
        //注册蓝牙配对监听器
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        mActivity.registerReceiver(mPairBroadcastReceiver, intentFilter);
    }

    public void createPair(BluetoothDevice device,IPairCallback callback){
        startPair(callback);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            device.createBond();
        } else {
            //利用反射方法调用BluetoothDevice.createBond(BluetoothDevice remoteDevice);
            Method createBondMethod = null;
            try {
                createBondMethod = BluetoothDevice.class.getMethod("createBond");
                createBondMethod.invoke(device);

            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    public void unRegisterReceiver(){
        if(mPairBroadcastReceiver != null){
            mActivity.unregisterReceiver(mPairBroadcastReceiver);
        }
    }
}
