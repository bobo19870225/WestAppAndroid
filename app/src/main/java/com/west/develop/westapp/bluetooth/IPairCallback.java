package com.west.develop.westapp.bluetooth;

/**
 * @Description: 配对回调
 */
public interface IPairCallback {
    void unBonded();
    void bonding();
    void bonded();
    void bondFail();
}
