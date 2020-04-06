package com.west.develop.westapp.Dialog;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.west.develop.westapp.Application.MyApplication;
import com.west.develop.westapp.Common.BaseSerialPort;
import com.west.develop.westapp.Communicate.Service.BluetoothService;
import com.west.develop.westapp.Communicate.Service.UsbService;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.Diagnosis.DiagnosisAPI;
import com.west.develop.westapp.Tools.Utils.SoundUtil;
import com.west.develop.westapp.bluetooth.BluetoothSerialPort;
import com.west.develop.westapp.usb.UsbSerialPort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.west.develop.westapp.Communicate.Service.BluetoothService.ACTION_BLUETOOTH_CHECK_SUCCESS;
import static com.west.develop.westapp.Communicate.Service.UsbService.ACTION_USB_DISCONNECTED;

/**
 * 状态栏,创建的同时开启蓝牙、USB服务，在隐藏的时候关闭
 */

public class ConnectStatus {

    private static Context mContext;
    private static ConnectStatus instance;
    private static final String TAG = ConnectStatus.class.getSimpleName();
    private boolean isPaused = false;

    private ConnStatusReceiver mReceiver = new ConnStatusReceiver();

    /**
     * 状态栏  内容
     */
    private View mContentView;

    /**
     * 参数
     */
    private WindowManager.LayoutParams mParams;

    /**
     * 是否正在显示
     */
    private boolean isShowing = false;


    private BluetoothSerialPort mBTPort;

    //private UsbDevice mUSBDevice;
    private UsbSerialPort mUSBPort;

    private UsbService mUsbService;

    private BluetoothService mBluetoothService;


    private ArrayList<BluetoothSerialPort> mBTSerialPorts = new ArrayList<>();
    private ArrayList<UsbSerialPort> mUsbSerialPorts = new ArrayList<>();


    /**
     * 获取单例
     */
    public static ConnectStatus getInstance(Context context) {
        mContext = context;

        if (instance == null) {
            instance = new ConnectStatus();
            instance.mContentView = LayoutInflater.from(mContext).inflate(R.layout.dialog_status, null);

            instance.mParams = new WindowManager.LayoutParams();
            instance.mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            instance.mParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            instance.mParams.format = PixelFormat.TRANSLUCENT;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {//6.0+
                instance.mParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
            } else {
                instance.mParams.type = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE;
            }
            instance.mParams.gravity = Gravity.LEFT + Gravity.BOTTOM;
            instance.mParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;

            /*
             * 启动USB服务
             */
            instance.startService(UsbService.class, instance.usbConnection, null);
            /*
             * 启动蓝牙服务
             */
            instance.startService(BluetoothService.class, instance.bluetoothConnection, null);

        }

        return instance;
    }

    public View getContentView() {
        return mContentView;
    }

    /**
     * 启动USB或蓝牙服务
     */
    private void startService(Class<?> service, ServiceConnection serviceConnection, Bundle extras) {
        if (!UsbService.SERVICE_CONNECTED) {
            Intent startService = new Intent(mContext, service);
            if (extras != null && !extras.isEmpty()) {
                Set<String> keys = extras.keySet();
                for (String key : keys) {
                    String extra = extras.getString(key);
                    startService.putExtra(key, extra);
                }
            }
            mContext.startService(startService);
        }
        Intent bindingIntent = new Intent(mContext, service);
        try {
            mContext.getApplicationContext().bindService(bindingIntent, serviceConnection, Context.BIND_AUTO_CREATE);
        } catch (Exception ex) {
            Log.e(TAG, ex.toString());
        }
    }

    public final ServiceConnection usbConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName arg0, IBinder arg1) {
            mUsbService = ((UsbService.UsbBinder) arg1).getService();
            UsbService.setInstance(mUsbService);
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mUsbService = null;
        }
    };

    public final ServiceConnection bluetoothConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBluetoothService = ((BluetoothService.BluetoothBinder) service).getService();
            BluetoothService.setInstance(mBluetoothService);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBluetoothService = null;
        }
    };


    public UsbSerialPort getUSBPort() {
        return mUSBPort;
    }

    /**
     * 获取已连接蓝牙
     */
    public BluetoothSerialPort getBTPort() {
        return mBTPort;
    }


    /**
     * 使能蓝牙
     */
    public void enableBT(boolean enable, BluetoothSerialPort device) {
        /*
         * 如果蓝牙设备未连接，设备断开连接提示音
         */
        if (mBTPort != null && (!enable || device == null)) {
            SoundUtil.deviceLostSound(mContext);
        }
        if (enable) {
            mBTPort = device;
        } else {
            mBTPort = null;
        }

        Intent intent = new Intent(ACTION_BLUETOOTH_CHECK_SUCCESS);
        mContext.sendBroadcast(intent);

        refreshState();

    }

    /**
     * 使能USB
     */
    public void enableUSB(boolean enable, UsbSerialPort port) {
        if (enable) {
            mUSBPort = port;
        } else {
            mUSBPort = null;
        }

        /*
         * 如果USB设备未连接，设备断开连接提示音
         */
        if (getUSBPort() == null && getBTPort() == null) {
            SoundUtil.deviceLostSound(mContext);
        }

        Intent intent = new Intent(ACTION_USB_DISCONNECTED);
        mContext.sendBroadcast(intent);
        if (DiagnosisAPI.getInstance() != null) {
            DiagnosisAPI.getInstance().dismiss();
        }
        refreshState();

    }

    /**
     * 发现 蓝牙设备
     * 所述设备是 检测序列号 有返回，但返回不正确的设备
     */
    public void detectBT(BluetoothSerialPort btPort) {
        if (btPort == null) {
            return;
        }
        boolean contain = false;
        for (int i = 0; i < mBTSerialPorts.size(); i++) {
            if (mBTSerialPorts.get(i).getDevice().getAddress().equals(btPort.getDevice().getAddress())) {
                contain = true;
                break;
            }
        }

        if (!contain) {
            mBTSerialPorts.add(btPort);
        }

        refreshState();
    }

    /**
     * 发现USB设备
     * 发现的设备 检测序列号 有返回，但不是与Android 设备绑定的设备
     */
    public void detectUSB(UsbSerialPort usbPort) {
        if (usbPort == null) {
            return;
        }
        mUsbSerialPorts.add(usbPort);
        refreshState();
    }


    /**
     * 检查是否设备列表中是否包含 设备
     */
    public boolean containPort(BaseSerialPort port) {
        if (port == null) {
            return false;
        }

        if (port instanceof BluetoothSerialPort) {
            for (int i = 0; i < mBTSerialPorts.size(); i++) {
                if (mBTSerialPorts.get(i).getDevice().getAddress().equals(((BluetoothSerialPort) port).getDevice().getAddress())) {
                    return true;
                }
            }
        }

        if (port instanceof UsbSerialPort) {
            for (int i = 0; i < mUsbSerialPorts.size(); i++) {
                if (((UsbSerialPort) port).getDriver().getDevice().getVendorId() == mUsbSerialPorts.get(i).getDriver().getDevice().getVendorId()) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * 检查设备列表中已经搜索不到的设备，将其移除
     * 所述设备是 检测序列号 有返回，但返回不正确的设备
     */
    public void compareBTDevice(List<BluetoothDevice> devices) {
        for (int i = 0; i < mBTSerialPorts.size(); i++) {
            boolean contain = false;
            BluetoothDevice device = mBTSerialPorts.get(i).getDevice();
            for (int j = 0; j < devices.size(); j++) {
                if (device.getAddress().equals(devices.get(j).getAddress())) {
                    contain = true;
                    break;
                }
            }
            if (!contain) {
                mBTSerialPorts.remove(i);
                i--;
            }

        }

        refreshState();
    }

    public void removeAllTB() {
        mBTSerialPorts.clear();
        refreshState();
    }


    /**
     * 移除USB设备
     * 所述设备是 检测序列号 有返回，但返回不正确的设备
     */
    public void removeUsb(UsbDevice device) {
        if (device == null) {
            return;
        }
        if (mUSBPort != null) {
            if (mUSBPort.getDriver().getDevice().getVendorId() == device.getVendorId()) {
                enableUSB(false, null);
            }
        }

        for (int i = 0; i < mUsbSerialPorts.size(); i++) {
            if (mUsbSerialPorts.get(i).getDriver().getDevice().getVendorId() == device.getVendorId()) {
                mUsbSerialPorts.remove(i);
                i--;
            }
        }

        refreshState();
    }


    /**
     * 刷新状态
     */
    private void refreshState() {
        if (mContentView != null) {
            final TextView stateDeviceTV = mContentView.findViewById(R.id.status_Device_TV);
            final TextView stateUSBTV = mContentView.findViewById(R.id.status_USB_TV);
            final TextView stateBTTV = mContentView.findViewById(R.id.status_BT_TV);
            final ImageView imageDevice = mContentView.findViewById(R.id.state_Device_IMG);
            final ImageView imageBT = mContentView.findViewById(R.id.state_BT_IMG);
            final ImageView imageUSB = mContentView.findViewById(R.id.state_USB_IMG);
            mContentView.post(new Runnable() {
                @Override
                public void run() {

                    //未发现蓝牙设备
                    if ((mBTSerialPorts == null || mBTSerialPorts.size() <= 0) && mBTPort == null) {
                        stateBTTV.setText(R.string.interface_not_found);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            stateBTTV.setTextColor(mContext.getColor(R.color.grey_black));
                        } else {
                            stateBTTV.setTextColor(mContext.getResources().getColor(R.color.grey_black));
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageBT.setImageDrawable(mContext.getDrawable(R.mipmap.connect_bt_unconnected));
                        } else {
                            imageBT.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.connect_bt_unconnected));
                        }
                    }
                    //发现蓝牙设备
                    else {
                        int count = 0;
                        if (mBTSerialPorts != null) {
                            count = mBTSerialPorts.size();
                        }
                        if (mBTPort != null) {
                            count++;
                        }
                        stateBTTV.setText(mContext.getString(R.string.interface_bt_connect) + count + mContext.getString(R.string.interface_unit_Device));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            stateBTTV.setTextColor(mContext.getColor(R.color.green_dark));
                        } else {
                            stateBTTV.setTextColor(mContext.getResources().getColor(R.color.green_dark));
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageBT.setImageDrawable(mContext.getDrawable(R.mipmap.connect_bt_connected));
                        } else {
                            imageBT.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.connect_bt_connected));
                        }
                    }

                    //未发现USB设备
                    if ((mUsbSerialPorts == null || mUsbSerialPorts.size() <= 0) && mUSBPort == null) {
                        stateUSBTV.setText(R.string.interface_not_found);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            stateUSBTV.setTextColor(mContext.getColor(R.color.grey_black));
                        } else {
                            stateUSBTV.setTextColor(mContext.getResources().getColor(R.color.grey_black));
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageUSB.setImageDrawable(mContext.getDrawable(R.mipmap.connect_usb_unconnected));
                        } else {
                            imageUSB.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.connect_usb_unconnected));
                        }
                    }
                    //发现USB设备
                    else {
                        int count = 0;
                        if (mUsbSerialPorts != null) {
                            count = mUsbSerialPorts.size();
                        }
                        if (mUSBPort != null) {
                            count++;
                        }
                        stateUSBTV.setText(mContext.getString(R.string.interface_usb_connect) + count + mContext.getString(R.string.interface_unit_Device));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            stateUSBTV.setTextColor(mContext.getColor(R.color.green_dark));
                        } else {
                            stateUSBTV.setTextColor(mContext.getResources().getColor(R.color.green_dark));
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageUSB.setImageDrawable(mContext.getDrawable(R.mipmap.connect_usb_connected));
                        } else {
                            imageUSB.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.connect_usb_connected));
                        }
                    }

                    //USB或蓝牙连接
                    if (mUSBPort != null || mBTPort != null) {
                        stateDeviceTV.setText(R.string.interface_device_connect);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            stateDeviceTV.setTextColor(mContext.getColor(R.color.green_dark));
                        } else {
                            stateDeviceTV.setTextColor(mContext.getResources().getColor(R.color.green_dark));
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageDevice.setImageDrawable(mContext.getDrawable(R.mipmap.connect_device_connected));
                        } else {
                            imageDevice.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.connect_device_connected));
                        }
                    }
                    //USB与蓝牙均未连接
                    else {
                        stateDeviceTV.setText(R.string.interface_no_connect);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            stateDeviceTV.setTextColor(mContext.getColor(R.color.grey_black));
                        } else {
                            stateDeviceTV.setTextColor(mContext.getResources().getColor(R.color.grey_black));
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            imageDevice.setImageDrawable(mContext.getDrawable(R.mipmap.connect_device_unconnected));
                        } else {
                            imageDevice.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.connect_device_unconnected));
                        }
                    }

                }
            });
        }
    }


    /**
     * 显示
     */
    public void show() {
        try {
            if (!isShowing) {
                if (!isPaused) {
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(MyApplication.ACTION_APP_BACKGROUND);
                    filter.addAction(MyApplication.ACTION_APP_FOREGROUND);
                    mContext.getApplicationContext().registerReceiver(mReceiver, filter);
                }
                WindowManager windowManager = (WindowManager) mContext.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                if (windowManager != null) {
                    if (mContentView.getParent() != null) {
                        windowManager.removeViewImmediate(mContentView);
                    }
                    windowManager.addView(mContentView, mParams);
                }

                isShowing = true;
            }

            refreshState();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 隐藏
     */
    public void dismiss() {

        if (isShowing) {
            WindowManager windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager != null) {
                windowManager.removeView(mContentView);
            }
            isShowing = false;
            try {
                if (isPaused) {
                    mContext.unregisterReceiver(mReceiver);
                }
                mContext.unbindService(this.usbConnection);
                mContext.unbindService(this.bluetoothConnection);

            } catch (Exception e) {
                e.printStackTrace();
            }
            isPaused = false;

        }
    }

    class ConnStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    case MyApplication.ACTION_APP_BACKGROUND:
                        isPaused = true;
                        dismiss();
                        break;
                    case MyApplication.ACTION_APP_FOREGROUND:
                        isPaused = false;
                        show();
                        break;
                }
            }
        }
    }

}
