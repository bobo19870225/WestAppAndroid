package com.west.develop.westapp.Communicate.Service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.Nullable;

import com.west.develop.westapp.Application.MyApplication;
import com.west.develop.westapp.Bean.AppBean.DocumentVersion;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.BondDialog;
import com.west.develop.westapp.Dialog.ConnectStatus;
import com.west.develop.westapp.Dialog.SignDialog;
import com.west.develop.westapp.Protocol.Drivers.BaseCMD;
import com.west.develop.westapp.Protocol.Drivers.DeviceDriver;
import com.west.develop.westapp.bluetooth.BluetoothAllUuid;
import com.west.develop.westapp.bluetooth.BluetoothSerialPort;
import com.west.develop.westapp.bluetooth.Profile;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Develop0 on 2017/12/25.
 */

public class BluetoothService extends Service {
    private static final int MSG_START_DISCOVERY = 1;

    public static final String ACTION_BLUETOOTH_CHECK_SUCCESS = "com.west.develop.westapp.ACTION_BLUETOOTH_CHECK_SUCCESS";
    public static final String ACTION_BLUETOOTH_DISCONNECTED = "com.west.develop.westapp.ACTION_BLUETOOTH_DISCONNECTED";

    private static final String mPared_PIN = "0000";

    IBinder mBinder = new BluetoothBinder();

    private List<BluetoothDevice> mDevices = new ArrayList<>();

    private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private static BluetoothService instance;

    private OpenThead mOpenThread;

    private ConnectCallback mConnectCallback;

    private Set<String> mUnsupportPortsMac = new HashSet<>();

    private boolean isRestart = false;
    /**
     * 连接回调
     */
    public final BluetoothSerialPort.ConnectListener mConnectListener = new BluetoothSerialPort.ConnectListener() {
        @Override
        public void onSuccess(final BluetoothSerialPort port) {
            Log.e("bluetooth-openDevice", "" + port.getDevice().getAddress() + "    success");
            if (mOpenThread != null) {
                mOpenThread.isOpening = false;
                mOpenThread = null;
            }

            if (BondDialog.getInstance() != null) {
                return;
            }
            if (SignDialog.getInstance() != null) {
                return;
            }

            /**
             * 连接成功
             */
            if (Config.getInstance(BluetoothService.this).getBondDevice() != null) {
                //检查设备

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        DeviceDriver.getInstance(BluetoothService.this).CheckDevice(port, new DeviceDriver.CheckCallback() {
                            @Override
                            public void callback(Byte resultByte, final String version) {
                                /**
                                 * 检查设备序列号正确
                                 */
                                if (resultByte != null && resultByte == BaseCMD.CHK_BACK_CMD_SUCCESS) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ConnectStatus.getInstance(BluetoothService.this).enableBT(true, port);

                                            /**
                                             * 设置固件当前版本
                                             */
                                            String verMain = version.substring(0, version.indexOf("."));
                                            String verSlave = version.substring(version.indexOf(".") + 1);
                                            DocumentVersion currentVersion = new DocumentVersion();
                                            currentVersion.setMain(verMain);
                                            currentVersion.setSlave(verSlave);
                                            ((MyApplication) getApplicationContext()).setCurrentFWVersion(currentVersion);

                                        }
                                    });


                                    if (mConnectCallback != null) {
                                        mConnectCallback.onFinish(true);
                                        mConnectCallback = null;
                                    }
                                    //restartDiscovery();
                                }
                                /**
                                 * 检查序列号返回错误
                                 */
                                else if (resultByte != null && resultByte == BaseCMD.CHK_BACK_SN_ERROR) {
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            ConnectStatus.getInstance(BluetoothService.this).detectBT(port);
                                        }
                                    });

                                    if (mConnectCallback != null) {
                                        mConnectCallback.onFinish(false);
                                        mConnectCallback = null;
                                    }
                                    if (SignDialog.getInstance() == null && BondDialog.getInstance() == null) {
                                        port.destroy();
                                    }
                                }
                                /**
                                 * 检查设备序列号没有返回或返回其它
                                 */
                                else {
                                    mUnsupportPortsMac.add(port.getDevice().getAddress());
                                    if (mConnectCallback != null) {
                                        mConnectCallback.onFinish(false);
                                        mConnectCallback = null;
                                    }
                                    if (SignDialog.getInstance() == null && BondDialog.getInstance() == null) {
                                        port.destroy();
                                    }
                                    //restartDiscovery();
                                    //startDiscovery(mDiscoveryListener);
                                }
                            }
                        });
                    }
                }).start();


            } else {
                if (SignDialog.getInstance() == null && BondDialog.getInstance() == null) {
                    port.destroy();
                }
                //restartDiscovery();
                if (mConnectCallback != null) {
                    mConnectCallback.onFinish(false);
                    mConnectCallback = null;
                }
            }
        }

        @Override
        public void onFailed(BluetoothSerialPort port) {
            Log.e("bluetooth-openDevice", "" + port.getDevice().getAddress() + "    failed");
            /**
             * 连接失败
             */
            if (mOpenThread != null) {
                mOpenThread.isOpening = false;
                mOpenThread = null;
            }
            // Log.e("BleutoothConnect","failed");
            //ConnectStatus.getInstance(BluetoothService.this).enableBT(false,null);
            if (mConnectCallback != null) {
                mConnectCallback.onFinish(false);
                mConnectCallback = null;
            }

        }

        @Override
        public void onLose(BluetoothSerialPort port) {

            if (ConnectStatus.getInstance(BluetoothService.this).getBTPort() != null) {
                if (ConnectStatus.getInstance(BluetoothService.this).getBTPort() == port) {
                    ConnectStatus.getInstance(BluetoothService.this).enableBT(false, null);
                }
            }

            Intent intent = new Intent(ACTION_BLUETOOTH_DISCONNECTED);
            sendBroadcast(intent);

        }
    };

    public static BluetoothService getInstance() {
        return instance;
    }

    /**
     * 初始化单例
     *
     * @param service
     */
    public static void setInstance(BluetoothService service) {
        instance = service;
        instance.registerReceiver();

        if (instance.mBluetoothAdapter.isEnabled()) {
            //蓝牙已打开，开始搜索蓝牙
            instance.mHandler.sendEmptyMessage(MSG_START_DISCOVERY);
        }

    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    /**
     * 开始搜索蓝牙
     */
    public void startDiscovery() {
        Log.e("bluetooth", "startScan");
        mDevices.clear();
        mBluetoothAdapter.startDiscovery();

    }

    /**
     * 停止搜索蓝牙
     */
    public void restartDiscovery() {
        isRestart = true;
        mBluetoothAdapter.cancelDiscovery();
        //startDiscovery();
    }

    public boolean isEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public void enableBluetooth() {
        mBluetoothAdapter.enable();
    }


    /**
     * 打开蓝牙串口
     *
     * @param port
     */
    public void openSerialPort(final BluetoothSerialPort port) {
        if (port == null) {
            return;
        }
        if (/*mUnsupportPortsMac.contains(port.getDevice().getAddress()) ||*/
                ConnectStatus.getInstance(BluetoothService.this).containPort(port)) {
            return;
        }
        if (mOpenThread != null) {
            if (mOpenThread.isOpening) {
                return;
            }
            mOpenThread.cancel();
            mOpenThread = null;
        }

        // mPort = port;
        /**
         * 开启连接线程
         */
        mOpenThread = new OpenThead(port);
        mOpenThread.start();
    }


    /**
     * 设置连接线程运行状态
     *
     * @param isOpening
     */
    public void setOpenThreadOpening(boolean isOpening) {
        if (mOpenThread != null) {
            mOpenThread.isOpening = isOpening;
        }
    }

    /**
     * 设置连接回调
     *
     * @param callback
     */
    public void setConnectCallback(ConnectCallback callback) {
        mConnectCallback = callback;
    }


    /**
     * 获取串口
     *
     * @return
     */
    public BluetoothSerialPort getBluetothPort() {
        return null;
    }

    /**
     * 绑定Service
     *
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    /**
     * 解绑Service
     *
     * @param conn
     */
    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
        unregisterReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        unregisterReceiver();
    }

    public class BluetoothBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    /**
     * 注册广播接收
     */
    public void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        registerReceiver(mReceiver, intentFilter);
    }

    /**
     * 注销广播
     */
    public void unregisterReceiver() {
        unregisterReceiver(mReceiver);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, Intent intent) {
            String action = intent.getAction();

            /**
             * 设备配对状态改变
             */
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);
                switch (state) {
                    case BluetoothDevice.BOND_BONDED:
                        //有设备配对成功
                        //device.fetchUuidsWithSdp();
                        String name = device.getName();
                        ArrayList<String> btList = Config.getInstance(BluetoothService.this).getBTList();

                        if (btList != null && btList.contains(name)) {
                            foundNewDevice(device);
                        }

                        break;
                }
            }

            /**
             * 蓝牙状态改变
             */
            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_ON:
                        //蓝牙开启，开始扫描
                        mHandler.sendEmptyMessage(MSG_START_DISCOVERY);
                        break;
                    case BluetoothAdapter.STATE_OFF:
                        //蓝牙关闭，清除检测到的设备
                        ConnectStatus.getInstance(context).enableBT(false, null);
                        ConnectStatus.getInstance(context).removeAllTB();
                        break;

                }
            }
            /**
             * 扫描到蓝牙设备
             */
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if (ConnectStatus.getInstance(context).getBTPort() != null) {

                    if (device.equals(ConnectStatus.getInstance(context).getBTPort().getDevice())) {
                        short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                        Log.e("Rssi", rssi + "");
                        // restartDiscovery();
                    }
                } else {
                    String name = device.getName();
                    ArrayList<String> btList = Config.getInstance(BluetoothService.this).getBTList();

                    if (btList != null && btList.contains(name)) {
                        foundNewDevice(device);
                    }
                    //foundNewDevice(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                /**
                 * 扫描结束后，启动下一轮扫描
                 */
                if (!isRestart) {
                    ConnectStatus.getInstance(BluetoothService.this).compareBTDevice(mDevices);
                }
                isRestart = false;
//              没有连接时重连
                if (null != port && port.getState() != BluetoothSerialPort.STATE_CONNECTED) {
                    mHandler.sendEmptyMessage(MSG_START_DISCOVERY);
                }
            }

            /**
             * 设备断开连接
             */
            if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (ConnectStatus.getInstance(context).getBTPort() != null) {
                    if (ConnectStatus.getInstance(context).getBTPort().getDevice().getAddress().equals(device.getAddress())) {
                        ConnectStatus.getInstance(context).enableBT(false, null);
                    }
                }
                Log.e("BT-Disconnected", device.getAddress());
            }

            if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {
                int RSSI = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                Log.e("RSSI", RSSI + "");
                //Log.e()
            }

        }
    };

    private BluetoothSerialPort port;

    /**
     * 发现新蓝牙设备
     *
     * @param device
     */
    private void foundNewDevice(BluetoothDevice device) {
        ParcelUuid[] uuids = device.getUuids();
        boolean isDevice = false;

        /**
         * 获取 SDP 协议UUID
         */
        if (uuids != null) {
            for (ParcelUuid uuid : uuids) {
                int service = BluetoothAllUuid.getServiceFromUuid(uuid);
                // if (service == Profile.ChatSecureService || service == Profile.ChatINSecureService) {
                if (service == Profile.SerialPortService) {
                    isDevice = true;
                    break;
                }
            }
        } else {
            /**
             * 未配对
             */
            if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                device.createBond();
            } else if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                device.fetchUuidsWithSdp();
            }
        }
        if (isDevice) {
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                if (!mDevices.contains(device)) {
                    mDevices.add(device);
                }
                port = new BluetoothSerialPort(device);

                if (Config.getInstance(BluetoothService.this).getBondDevice() != null) {
                    if (SignDialog.getInstance() != null) {
                        /**
                         * 如果激活对话框正在显示，且等待激活设备，激活设备
                         */
                        if (SignDialog.getInstance().waitSign()) {
                            SignDialog.getInstance().signWithPort(port);
                        }
                    } else {
                        /**
                         * 未连接设备，且没有设备正在连接，打开蓝牙串口{@port}
                         */
                        if (mOpenThread == null || !mOpenThread.isOpening) {
                            openSerialPort(port);
                        }
                    }
                } else if (BondDialog.getInstance() != null && BondDialog.getInstance().waitSign()) {
                    BondDialog.getInstance().bondWithPort(port);
                }

            } else if (device.getBondState() == BluetoothDevice.BOND_NONE) {
                device.createBond();
            }
        } else {
            device.fetchUuidsWithSdp();
        }
    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_START_DISCOVERY:
                    startDiscovery();
                    break;
            }
        }
    };


    class OpenThead extends Thread {
        private BluetoothSerialPort mPort;
        private boolean isOpening = false;

        OpenThead(BluetoothSerialPort port) {
            mPort = port;
        }

        @Override
        public void run() {
            if (mPort == null) {
                return;
            }

            if (Config.getInstance(BluetoothService.this).getBondDevice() != null) {
                /**
                 * 已经激活，尝试连接
                 */
                isOpening = true;
                Log.e("bluetooth-openDevice", "" + mPort.getDevice().getAddress());
                mPort.open(mConnectListener);
            }
        }

        public void cancel() {
            try {
                interrupt();
            } catch (Exception ex) {

            }
        }
    }

    public interface ConnectCallback {
        public void onFinish(boolean success);
    }
}
