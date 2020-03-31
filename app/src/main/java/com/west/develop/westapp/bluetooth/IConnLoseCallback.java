package com.west.develop.westapp.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Develop0 on 2017/8/23.
 */

public interface IConnLoseCallback {
    void onLose(BluetoothDevice device);
}
