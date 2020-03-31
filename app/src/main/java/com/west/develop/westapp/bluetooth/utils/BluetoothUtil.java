package com.west.develop.westapp.bluetooth.utils;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;


import java.util.Arrays;

/**
 * @Description: 蓝牙基础操作工具类
 */
public class BluetoothUtil {
    public static void enableBluetooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    public static boolean isSupportBle(Context context){
        if (context == null || !context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getAdapter() != null;
//        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
//        return mAdapter != null;
    }

    public static boolean isBleEnable(Context context){
       /* if(!isSupportBle(context)){
            return false;
        }*/
        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        return manager.getAdapter().isEnabled();
//        BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
//        return mAdapter.isEnabled();
    }

    public static void printServices(BluetoothGatt gatt) {
        if (gatt != null) {
            for (BluetoothGattService service : gatt.getServices()) {
                printService(service);
            }
        }
    }

    public static void printService(BluetoothGattService service){
        Log.e("BluetoothUtil","service: " + service.getUuid());
        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
            Log.e("BluetoothUtil", "      characteristic: " + characteristic.getUuid() +
                    " value: " + Arrays.toString(characteristic.getValue()) +
                    "   permision:" + characteristic.getPermissions());
            for (BluetoothGattDescriptor descriptor : characteristic.getDescriptors()) {
                Log.e("BluetoothUtil", "              descriptor: " + descriptor.getUuid() +
                        " value: " + Arrays.toString(descriptor.getValue()) +
                        "   permision:" + characteristic.getPermissions());
            }
        }
    }

}
