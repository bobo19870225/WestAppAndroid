package com.west.develop.westapp.Communicate.Service;

import android.app.Dialog;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.west.develop.westapp.Application.MyApplication;
import com.west.develop.westapp.Bean.AppBean.DocumentVersion;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.BondDialog;
import com.west.develop.westapp.Dialog.ConnectStatus;
import com.west.develop.westapp.Dialog.SignDialog;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.Protocol.Drivers.BaseCMD;
import com.west.develop.westapp.Protocol.Drivers.DeviceDriver;
import com.west.develop.westapp.R;
import com.west.develop.westapp.usb.UsbSerialDriver;
import com.west.develop.westapp.usb.UsbSerialPort;
import com.west.develop.westapp.usb.UsbSerialProber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class UsbService extends Service {

    public static final String ACTION_USB_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED";
    public static final String ACTION_USB_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED";

    public static final String ACTION_USB_DISCONNECTED = "com.felhr.usbservice.USB_DISCONNECTED";
    public static final String ACTION_USB_CHECK_SUCCESS = "com.west.develop.westapp.USB_CHECK_SUCCESS";

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    public static final int DEFAULT_BAUD_RATE = 500000; // BaudRate. Change this value if you need
    public static boolean SERVICE_CONNECTED = false;


    private static UsbService instance;

    public static UsbService getInstance() {
        return instance;
    }

    public static void setInstance(UsbService service) {
        instance = service;
    }


    private IBinder binder = new UsbBinder();

    private Context context;
    private UsbManager usbManager;
    private UsbDeviceConnection connection;
    private int BAUD_RATE = DEFAULT_BAUD_RATE;


    //private UsbSerialPort mSerialPort = null;

    private Map<UsbDevice, Integer> mRequestTimesMap = new HashMap<>();

    private List<UsbSerialPort> mPorts = new ArrayList<>();

    private HashSet<UsbSerialPort> mUnsupportPorts = new HashSet<>();

    /*
     * Different notifications from OS will be received here (USB attached, detached, permission responses...)
     */
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context arg0, final Intent arg1) {
            if (arg1.getAction().equals(ACTION_USB_PERMISSION)) {
                boolean granted = arg1.getExtras().getBoolean(UsbManager.EXTRA_PERMISSION_GRANTED);

                final UsbDevice usbDevice = arg1.getExtras().getParcelable(UsbManager.EXTRA_DEVICE);

                if (granted) {
                    /**
                     * USB授权成功
                     */
                    UsbSerialPort port = null;

                    Log.e("UsbService", "mPorts-" + mPorts.size());
                    for (int i = 0; i < mPorts.size(); i++) {
                        if (mPorts.get(i).getDriver().getDevice().getVendorId() == usbDevice.getVendorId()) {
                            port = mPorts.get(i);
                            break;
                        }
                    }
                    // Log.e("UsbService", port.toString());
                    if (port == null) {
                        return;
                    }

                    if (Config.getInstance(context).getBondDevice() != null) {
                        //已激活，连接后检查设备序列号
                        connectSerialPort(port);
                    } else if (SignDialog.getInstance() != null && SignDialog.getInstance().waitSign()) {
                        /**
                         * 正在激活，通过{@mSerialPort} 读取序列号和激活码后激活
                         */
                        SignDialog.getInstance().signWithPort(port);
                    } else if (BondDialog.getInstance() != null && BondDialog.getInstance().waitSign()) {
                        BondDialog.getInstance().bondWithPort(port);
                    }
                } else {
                    /**
                     * USB 授权失败，尝试再授权一次
                     */
                    Integer requestTime = mRequestTimesMap.get(usbDevice);
                    if (requestTime == null || requestTime <= 1) {
                        TipDialog dialog = new TipDialog.Builder(context)
                                .setTitle(getString(R.string.tip_title))
                                .setMessage(getString(R.string.usb_not_Granted))
                                .setNegativeClickListener(getString(R.string.cancel), new TipDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialogInterface, int index, String label) {
                                        dialogInterface.dismiss();
                                        //mSerialPort = null;
                                    }
                                })
                                .setPositiveClickListener(getString(R.string.Sure), new TipDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialogInterface, int index, String label) {
                                        /**
                                         * 再次请求
                                         */
                                        requestUserPermission(usbDevice);
                                        dialogInterface.dismiss();
                                    }
                                })
                                .requestSystemAlert(true)
                                .build();
                        dialog.show();
                    }
                }
            } else if (arg1.getAction().equals(ACTION_USB_ATTACHED)) {
                //有USB设备连接，
                Log.e("UsbService", "ATTACHED");
                findSerialPortDevice(); // A USB device has been attached. Try to open it as a Serial port
            } else if (arg1.getAction().equals(ACTION_USB_DETACHED)) {
                // USB 设备断开连接
                final UsbDevice usbDevice = arg1.getExtras().getParcelable(UsbManager.EXTRA_DEVICE);

                ConnectStatus.getInstance(context).removeUsb(usbDevice);
                findSerialPortDevice();

            }
        }
    };

    /*
     * onCreate will be executed when service is started. It configures an IntentFilter to listen for
     * incoming Intents (USB ATTACHED, USB DETACHED...) and it tries to open a serial port.
     */
    @Override
    public void onCreate() {
        Log.e("UsbService", "start");
        this.context = this;
        UsbService.SERVICE_CONNECTED = true;
        setFilter();
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        findSerialPortDevice();
    }

    /* MUST READ about services
     * http://developer.android.com/guide/components/services.html
     * http://developer.android.com/guide/components/bound-services.html
     */
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        UsbService.SERVICE_CONNECTED = false;
        unregisterReceiver(usbReceiver);
    }


    /**
     * 查找 USB 设备
     */
    private void findSerialPortDevice() {
        // This snippet will try to open the first encountered usb device connected, excluding usb root hubs

        new AsyncTask<Void, Void, List<UsbSerialPort>>() {
            @Override
            protected List<UsbSerialPort> doInBackground(Void... params) {

                final List<UsbSerialDriver> drivers =
                        UsbSerialProber.getDefaultProber().findAllDrivers(usbManager);
                final List<UsbSerialPort> result = new ArrayList<UsbSerialPort>();

                for (final UsbSerialDriver driver : drivers) {
                    final List<UsbSerialPort> ports = driver.getPorts();
                    result.addAll(ports);

                    if (!usbManager.hasPermission(driver.getDevice())) {
                        requestUserPermission(driver.getDevice());
                    }
                }

                mPorts.clear();
                mPorts.addAll(result);
                Log.d("UsbService", "Ports:" + result.size() + "");
                for (int i = 0; i < mPorts.size(); i++) {
                    boolean hasPermission = usbManager.hasPermission(mPorts.get(i).getDriver().getDevice());
                    if (hasPermission) {
                        if (!mUnsupportPorts.contains(mPorts.get(i)) && !ConnectStatus.getInstance(context).containPort(mPorts.get(i))) {
                            /**
                             * 连接端口
                             */
                            connectSerialPort(mPorts.get(i));
                        }
                    }
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<UsbSerialPort> result) {

            }

        }.execute((Void) null);
    }


    /**
     * 连接设备(检查设备序列号)
     */
    public void connectSerialPort(final UsbSerialPort port) {
        if (port == null) {
            return;
        }

        DeviceDriver.getInstance(context).CheckDevice(port, new DeviceDriver.CheckCallback() {
            @Override
            public void callback(Byte resultByte, String version) {
                /**
                 * 检查设备序列号正确
                 */
                if (resultByte != null && resultByte == BaseCMD.CHK_BACK_CMD_SUCCESS) {
                    if (ConnectStatus.getInstance(context).getUSBPort() == null) {
                        ConnectStatus.getInstance(context).enableUSB(true, port);
                        Intent intent = new Intent(ACTION_USB_CHECK_SUCCESS);
                        sendBroadcast(intent);
                    }
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
                /**
                 * 检查序列号返回错误
                 */
                else if (resultByte != null && resultByte == BaseCMD.CHK_BACK_SN_ERROR) {
                    ConnectStatus.getInstance(context).detectUSB(port);
                    if (port != null) {
                        port.close();
                    }
                }

                /**
                 * 检查设备序列号没有返回或返回其它
                 */
                else {
                    mUnsupportPorts.add(port);
                    if (port != null) {
                        port.close();
                    }
                }
            }
        });

    }

    public List<UsbSerialPort> getSerialPorts() {
        return mPorts;
    }

    private void setFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(ACTION_USB_DETACHED);
        filter.addAction(ACTION_USB_ATTACHED);
        registerReceiver(usbReceiver, filter);
    }


    /**
     * 请求USB设备权限
     *
     * @param device
     */
    public void requestUserPermission(UsbDevice device) {
        if (device != null) {
            try {
                usbManager.getClass().getMethod("grantPermission", new Class[]{UsbDevice.class}).invoke(usbReceiver, device);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                PendingIntent mPendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                usbManager.requestPermission(device, mPendingIntent);
                mRequestTimesMap.put(device, mRequestTimesMap.get(device) == null ? 1 : mRequestTimesMap.get(device) + 1);
            }
        }
    }


    public class UsbBinder extends Binder {
        public UsbService getService() {
            return UsbService.this;
        }
    }

    /**
     * 打开USB 串口
     *
     * @param port
     * @param baud
     * @return
     */
    public boolean COMPortOpen(UsbSerialPort port, int baud) {
        boolean result = false;

        if (port == null) {
            return result;
        }

        //mSerialPort = port;
        final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        if (!usbManager.hasPermission(port.getDriver().getDevice())) {
            return result;
        }
        connection = usbManager.openDevice(port.getDriver().getDevice());
        if (connection == null) {
            Log.e("USBSerial Connect", "null connection");
            return result;
        }

        //连接设备并设置波特率
        if (!port.isOpened()) {
            port.open(connection);
        }
        port.setParameters(baud, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

        result = true;
        Log.e("open USBSerial", "success");
        return result;
    }

}


