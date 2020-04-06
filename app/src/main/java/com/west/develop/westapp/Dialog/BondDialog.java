package com.west.develop.westapp.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.west.develop.westapp.Bean.AppBean.DeviceBean;
import com.west.develop.westapp.Common.BaseSerialPort;
import com.west.develop.westapp.Communicate.Service.BluetoothService;
import com.west.develop.westapp.Communicate.Service.UsbService;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Protocol.Drivers.DeviceDriver;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.Utils.VolleyUtil;
import com.west.develop.westapp.Tools.constant.URLConstant;
import com.west.develop.westapp.bluetooth.BluetoothSerialPort;
import com.west.develop.westapp.usb.UsbSerialPort;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * 设备激活对话框
 */

public class BondDialog extends Dialog implements View.OnClickListener {
    private static int STEP_NOTICE = 0;
    private static int STEP_READ = 1;
    private static int STEP_COMMIT = 2;
    private static int STEP_BOND = 3;
    private static int STEP_BOND_SUCCESS = 4;
    private static int STEP_BOND_FAILED = 5;
    private Context mContext;

    private static BondDialog instance;

    public static BondDialog getInstance() {
        return instance;
    }

    public static BondDialog newInstance(Context context) {
        if (instance == null) {
            instance = new BondDialog(context);
        }
        instance.setStep(STEP_NOTICE, "");

        return instance;
    }

    public static BondDialog newInstance(Context context, int step) {
        if (instance == null) {
            instance = new BondDialog(context, step);
        }
        instance.setStep(step, "");

        return instance;
    }

    private Button mNegativeBTN;
    private Button mPositiveBTN;

    private TextView mMessageTV;

    private String mNegativeText;

    private int mStep_Index = STEP_NOTICE;

    private OnClickListener mNegativeClickListener;

    /**
     * 绑定结果回调
     */
    private BondCallback mBondCallback;

    private Handler mHandler = new Handler();

    private boolean startSign = false;

    private ReadThread mReadThread;


    private ArrayList<BaseSerialPort> mPorts = new ArrayList<>();
    private int mSignIndex = 0;

    /**
     * 从设备读取到的信息
     * {@deviceInfos[0]}    device SN
     * {@deviceInfos[1]}    device COMCHK
     */
    private String[] deviceInfos;

    private BaseSerialPort mReadPort;

    private boolean isSigning = false;

    private boolean isBond = true;  //标志是否绑定该设备,默认是要绑定的

    private int readSNCount = 0; // 记录一个接口有几次读序列号的次数，设置最高为三次

    private String errMessage = null; //获取服务器返回的错误信息

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置 Dialog 布局
        setContentView(R.layout.dialog_sign);
        //点击边缘无效
        setCanceledOnTouchOutside(false);
        //setCancelable(false);
        initView();
    }

    private BondDialog(Context context) {
        super(context);
        mContext = context;

        OnDismissListener onDismissListener = new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                clear();
            }
        };
        setOnDismissListener(onDismissListener);
    }

    private BondDialog(Context context, int step) {
        this(context);

        setStep(step, "");
    }


    private void initView() {
        mPositiveBTN = findViewById(R.id.dialog_Positive_BTN);
        mNegativeBTN = findViewById(R.id.dialog_Negative_BTN);
        mMessageTV = findViewById(R.id.dialog_Message_TV);
        TextView mTitleTV = findViewById(R.id.dialog_title);
        mTitleTV.setText(mContext.getResources().getString(R.string.device_bond_title));

        mPositiveBTN.setVisibility(View.VISIBLE);

        if (mNegativeText != null || mNegativeClickListener != null) {
            mNegativeBTN.setVisibility(View.VISIBLE);
            mNegativeBTN.setText(mNegativeText == null ? mContext.getString(R.string.bond_cancle) : mNegativeText);
        }

        mPositiveBTN.setOnClickListener(this);
        mNegativeBTN.setOnClickListener(this);

        setStep(mStep_Index, "");

    }


    private BondDialog setStep(int step, String s) {
        if (step == STEP_NOTICE) {
            BluetoothService.getInstance().restartDiscovery();
            mStep_Index = STEP_NOTICE;
            if (mMessageTV != null) {
                mMessageTV.setText(mContext.getString(R.string.bond_Step_NOTICE));
                mMessageTV.setGravity(Gravity.CENTER_VERTICAL);
            }
            if (mPositiveBTN != null) {
                mPositiveBTN.setVisibility(View.VISIBLE);
                mPositiveBTN.setText(mContext.getString(R.string.Sure));
            }

            if (mNegativeBTN != null) {
                mNegativeBTN.setVisibility(View.VISIBLE);
                mNegativeBTN.setText(mContext.getString(R.string.cancel));
            }

        }

        if (step == STEP_READ) {
            mStep_Index = STEP_READ;

            if (mMessageTV != null) {
                mMessageTV.setText(mContext.getString(R.string.sign_Step_READ));
                mMessageTV.setGravity(Gravity.CENTER);
                startSign();
            }
            isSigning = false;

            if (mPositiveBTN != null) {
                mPositiveBTN.setVisibility(View.VISIBLE);
                mPositiveBTN.setText(mContext.getString(R.string.cancel));
            }

            if (mNegativeBTN != null) {
                mNegativeBTN.setVisibility(View.GONE);

            }
        }
        if (step == STEP_COMMIT) {
            mStep_Index = STEP_COMMIT;

            if (mMessageTV != null) {
                mMessageTV.setText(mContext.getString(R.string.sign_deviceSN) + " " +
                        deviceInfos[0] + " " +
                        mContext.getString(R.string.sign_deviceSN_Ask));
            }

            if (mPositiveBTN != null) {
                mPositiveBTN.setVisibility(View.VISIBLE);
                mPositiveBTN.setText(mContext.getResources().getString(R.string.sign_Commit_YES));
            }

            if (mNegativeBTN != null) {
                mNegativeBTN.setVisibility(View.VISIBLE);
                mNegativeBTN.setText(mContext.getResources().getString(R.string.sign_Commit_NO));
            }
        }
        if (step == STEP_BOND) {
            mStep_Index = STEP_BOND;

            if (mMessageTV != null) {
                mMessageTV.setText(mContext.getResources().getString(R.string.bonding_device));
            }

            if (mPositiveBTN != null) {
                mPositiveBTN.setVisibility(View.GONE);
            }

            if (mNegativeBTN != null) {
                mNegativeBTN.setVisibility(View.GONE);
            }
        }
        if (step == STEP_BOND_SUCCESS) {
            //signDevice();
            mStep_Index = STEP_BOND_SUCCESS;
            if (mMessageTV != null) {
                mMessageTV.setText(mContext.getString(R.string.bond_success));
            }

            if (mPositiveBTN != null) {
                mPositiveBTN.setVisibility(View.VISIBLE);
                mPositiveBTN.setText(mContext.getResources().getString(R.string.Sure));
            }

        }

        if (step == STEP_BOND_FAILED) {
            //signDevice();
            mStep_Index = STEP_BOND_FAILED;
            if (mMessageTV != null) {
                mMessageTV.setText(s);
                mMessageTV.setGravity(Gravity.CENTER);
                if (errMessage != null || !isBond || mSignIndex >= mPorts.size()) { //最后一个接口的情况，还是没有绑定成功，还有是设备已经被绑定过的，就直接退出了
                    if (mPositiveBTN != null) {
                        mPositiveBTN.setVisibility(View.VISIBLE);
                        mPositiveBTN.setText(mContext.getResources().getString(R.string.Sure));
                    }
                    if (mNegativeBTN != null) {
                        mNegativeBTN.setVisibility(View.GONE);

                    }
                } else {
                    if (mPositiveBTN != null) {
                        mPositiveBTN.setVisibility(View.VISIBLE);
                        mPositiveBTN.setText(mContext.getResources().getString(R.string.bond_sure));
                    }
                    if (mNegativeBTN != null) {
                        mNegativeBTN.setVisibility(View.VISIBLE);
                        mNegativeBTN.setText(mContext.getResources().getString(R.string.bond_cancle));
                    }
                }
            }

        }

        return this;
    }

    private void startSign() {
        startSign = true;
    }

    public boolean waitSign() {
        return startSign;
    }

    /**
     * 设置 确定按钮 点击事件
     */
    public BondDialog setNegativeClickListener(String text, OnClickListener listener) {
        mNegativeText = text;
        if (mNegativeBTN != null) {
            mNegativeBTN.setText(mNegativeText == null ? mContext.getString(R.string.cancel) : mNegativeText);
        }
        setNegativeClickListener(listener);
        return this;
    }

    /**
     * 设置 取消按钮 点击事件
     */
    public BondDialog setNegativeClickListener(OnClickListener listener) {
        mNegativeClickListener = listener;
        if (mNegativeBTN != null) {
            mNegativeBTN.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public BondDialog setBondCallback(BondCallback callback) {
        mBondCallback = callback;
        return this;
    }


    private BaseSerialPort getReadPort() {
        return mReadPort;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_Negative_BTN:
                if (mStep_Index == STEP_COMMIT) {
                    isBond = false;
                    setStep(STEP_READ, "");
                    mSignIndex++;
                    signIndex(mSignIndex);
                } else {
                    dismiss();
                }
                if (mReadPort instanceof UsbSerialPort) {
                    mReadPort.close();
                }
                if (mReadPort instanceof BluetoothSerialPort) {
                    ((BluetoothSerialPort) mReadPort).destroy();
                }

                break;
            case R.id.dialog_Positive_BTN:
                if (errMessage != null || (!isBond && mStep_Index == STEP_BOND_FAILED) || mStep_Index == STEP_READ) {
                    if (mNegativeClickListener != null) {
                        mNegativeClickListener.onClick(this);
                    }
                    break;
                }
                if (mStep_Index == STEP_BOND_SUCCESS) {
                    dismiss();
                    break;
                }
                if (mStep_Index == STEP_BOND_FAILED) {
                    mStep_Index = STEP_NOTICE;
                    mSignIndex = 0;
                    setStep(mStep_Index, "");
                    break;
                } else if (mStep_Index == STEP_COMMIT) {
                    isBond = true;
                    bondDevice();
                    break;
                } else {
                    if (mReadPort instanceof UsbSerialPort) {
                        mReadPort.close();
                    }
                   /* if(mReadPort instanceof BluetoothSerialPort){
                        ((BluetoothSerialPort)mReadPort).destroy();
                    }*/
                }
                if (mStep_Index == STEP_NOTICE) {
                    if (!BluetoothService.getInstance().isEnabled()) {
                        BluetoothService.getInstance().enableBluetooth();
                    }

                }
                setStep(mStep_Index + 1, "");
                break;
        }

    }


    /**
     * 启动绑定
     */
    public void bondWithPort(BaseSerialPort port) {

        if (port instanceof BluetoothSerialPort) {
            boolean contain = false;
            for (int i = 0; i < mPorts.size(); i++) {
                if (mPorts.get(i) instanceof BluetoothSerialPort) {
                    BluetoothSerialPort bluetoothPort = (BluetoothSerialPort) port;
                    BluetoothSerialPort bluetoothSerialPort = (BluetoothSerialPort) mPorts.get(i);
                    if (bluetoothPort.getDevice().equals(bluetoothSerialPort.getDevice())) {
                        contain = true;
                        break;
                    }
                }
            }
            if (!contain) {
                mPorts.add(port);
            }
        } else if (!mPorts.contains(port)) {
            mPorts.add(port);
        }

        if (mPorts.size() > 0 && !isSigning) {
            signIndex(mSignIndex);
        }
        Log.e("sign", "port");
    }

    /**
     * 绑定第 {@index} 个串口
     */
    private void signIndex(int index) {
        isSigning = true;
        // setStep(STEP_READ);
        if (index >= mPorts.size()) {
            bondFinish(false, mContext.getString(R.string.bond_failed));
            //mBondCallback.onFinish(false,this,mContext.getString(R.string.bond_failed));
            if (!isBond) {
                setStep(STEP_BOND_FAILED, mContext.getString(R.string.bond_failed_msg));
            } else {
                setStep(STEP_BOND_FAILED, mContext.getString(R.string.bond_failed_sn));
            }

            return;
        }
        stopRead();

        BaseSerialPort port = mPorts.get(index);
        mReadThread = new ReadThread(port);
        mReadThread.start();
    }

    /**
     * 读取设备SN
     * {@deviceInfos[0]}   DeviceSN
     * {@deviceInfos[1]}   COMCHK
     */
    private void onDeviceRead(BaseSerialPort readPort) {
        stopRead();
        if (deviceInfos == null) {
            if (mReadPort instanceof UsbSerialPort) {
                mReadPort.close();
            }
            if (mReadPort instanceof BluetoothSerialPort) {
                ((BluetoothSerialPort) mReadPort).destroy();
            }
            readSNCount++;
            if (readSNCount >= 3) {
                mSignIndex++;
                readSNCount = 0;
            }
            signIndex(mSignIndex);
            return;
        }
        mReadPort = readPort;

        commitDevice();
    }


    /**
     * 确认设备SN
     * {@deviceInfos[0]}   DeviceSN
     * {@deviceInfos[1]}   COMCHK
     */
    private void commitDevice() {
        setStep(STEP_COMMIT, "");
    }


    /**
     * 确认设备SN 后在服务器绑定
     * {@deviceInfos[0]}   DeviceSN
     * {@deviceInfos[1]}   COMCHK
     */
    private void bondDevice() {
        setStep(STEP_BOND, "");
//        String authCode = deviceInfos[1].replace(" ","");

        String url = URLConstant.urlDeviceBond + "?" +
                "deviceSN=" + deviceInfos[0] + "&" +
                "id1=" + deviceInfos[1] + "&" +
                "id2=" + deviceInfos[2] + "&" +
                "targetID=" + Config.getAndroidID(getContext());
        url = url.replaceAll(" ", "");
        VolleyUtil.jsonPostRequest(url, getContext(), new VolleyUtil.IVolleyCallback() {
            @Override
            public void getResponse(JSONObject jsonObject) {
                try {
                    // mDialog.dismiss();
                    int code = jsonObject.getInt("code");
                    JSONObject data = jsonObject.getJSONObject("data");
                    //Success
                    if (code == 0) {
                        Gson gson = new Gson();

                        int setRegCount = data.getJSONObject("Device").getInt("setRegCount");
                        int RegCount = data.getJSONObject("Device").getInt("regCount");

                        Config.getInstance(mContext).setSetRegCount(setRegCount);
                        Config.getInstance(mContext).setRegCount(RegCount);

                        DeviceBean device = gson.fromJson(data.getJSONObject("Device").toString(), DeviceBean.class);
                        Config.getInstance(getContext()).setBondDevice(device);

                        boolean configured = data.getBoolean("configured");
                        Config.getInstance(getContext()).setConfigured(configured);


                        bondFinish(true, "");
                        //mBondCallback.onFinish(true,BondDialog.this, "");
                        setStep(STEP_BOND_SUCCESS, "");
                    } else {
                        errMessage = data.getString("error");

                        bondFinish(false, errMessage);
                        //mBondCallback.onFinish(false,BondDialog.this,errMessage);
                        setStep(STEP_BOND_FAILED, errMessage);

                    }
                } catch (JSONException ex) {
                    bondFinish(false, mContext.getString(R.string.bond_failed));
                    // mBondCallback.onFinish(false,BondDialog.this,mContext.getString(R.string.bond_failed));
                    setStep(STEP_BOND_FAILED, mContext.getString(R.string.bond_failed) + "," + mContext.getString(R.string.bonding_getnetdate));
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {
                if (error instanceof TimeoutError) {
                    bondFinish(false, mContext.getString(R.string.toast_netconn_moreTime));
                    //mBondCallback.onFinish(false,BondDialog.this,mContext.getString(R.string.toast_netconn_moreTime));
                    setStep(STEP_BOND_FAILED, mContext.getString(R.string.bond_failed) + "," + mContext.getString(R.string.toast_netconn_moreTime));
                } else {
                    bondFinish(false, mContext.getString(R.string.toast_inspect_netconn));
                    //mBondCallback.onFinish(false,BondDialog.this,mContext.getString(R.string.toast_inspect_netconn));
                    setStep(STEP_BOND_FAILED, mContext.getString(R.string.bond_failed) + "," + mContext.getString(R.string.toast_inspect_netconn));
                }

            }
        });
    }


    private void bondFinish(boolean success, String message) {
        if (success) {
            /*
             * 激活成功
             */
            BaseSerialPort readPort = getReadPort();
            /*
             * 更新状态栏
             */
            if (readPort != null) {
                if (readPort instanceof UsbSerialPort) {
                    ConnectStatus.getInstance(mContext).enableUSB(true, (UsbSerialPort) readPort);
                    if (BluetoothService.getInstance().getBluetothPort() != null) {
                        BluetoothService.getInstance().openSerialPort(BluetoothService.getInstance().getBluetothPort());
                    }
                }
                if (readPort instanceof BluetoothSerialPort) {
                    // BluetoothService.getInstance().openSerialPort((BluetoothSerialPort)readPort);
                    ConnectStatus.getInstance(mContext).enableBT(true, (BluetoothSerialPort) readPort);
                    //如果存在 USB 设备连接，尝试检查设备序列号
                    List<UsbSerialPort> mUsbPorts = UsbService.getInstance().getSerialPorts();
                    if (mUsbPorts != null && mUsbPorts.size() > 0) {
                        for (int i = 0; i < mUsbPorts.size(); i++) {
                            UsbService.getInstance().connectSerialPort(mUsbPorts.get(i));
                        }
                    }
                }
            }
        } else {
            /*
             * 激活失败
             */
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

            BaseSerialPort readPort = getReadPort();
            if (readPort instanceof UsbSerialPort) {
                readPort.close();
            }
            if (readPort instanceof BluetoothSerialPort) {
                ((BluetoothSerialPort) readPort).destroy();
            }
        }

        if (mBondCallback != null) {
            mBondCallback.onFinish(success, this, message);
        }
    }

    /**
     * 从设备读取信息的线程
     */
    private class ReadThread extends Thread {
        BaseSerialPort mPort;

        ReadThread(BaseSerialPort port) {
            mPort = port;
        }

        @Override
        public void run() {
            deviceInfos = DeviceDriver.getInstance(mContext).ReadDevice(mPort);

          /*  if(mPort instanceof UsbSerialPort){
                mPort.close();
            }
            if(mPort instanceof  BluetoothSerialPort){
                ((BluetoothSerialPort)mPort).destroy();
            }*/

            final BaseSerialPort port = mPort;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onDeviceRead(port);
                }
            });
        }
    }


    private void stopRead() {
        if (mReadThread != null) {
            mReadThread.interrupt();
            mReadThread = null;
        }
    }


    /**
     * 销毁
     */
    public static void clear() {
        if (instance != null) {
            instance.stopRead();
        }
        instance = null;
    }


    /**
     * 点击事件监听接口
     */
    public interface OnClickListener {
        void onClick(Dialog dialog);
    }

    public interface BondCallback {
        void onFinish(boolean success, BondDialog dialog, String message);
    }
}
