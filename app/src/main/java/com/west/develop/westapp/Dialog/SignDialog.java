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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.west.develop.westapp.Bean.AppBean.DeviceBean;
import com.west.develop.westapp.Bean.AppBean.User;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Develop0 on 2018/1/9.
 */

public class SignDialog extends Dialog implements View.OnClickListener{
    public static int STEP_NOTICE = 0;
    public static int STEP_READ = 1;
    public static int STEP_USER = 2;
    public static int STEP_SIGN = 3;
    Context mContext;

    private static SignDialog instance;


    public static SignDialog getInstance(){
        return instance;
    }

    public static SignDialog newInstance(Context context){
        if(instance == null){
            instance = new SignDialog(context);
        }
        instance.setStep(STEP_NOTICE);
        return instance;
    }

    public static SignDialog newInstance(Context context,int step){
        if(instance == null){
            instance = new SignDialog(context,step);
        }
        instance.setStep(step);

        return instance;
    }

    private LinearLayout mUserLine;
    private Button mNegativeBTN;
    private Button mPositiveBTN;

    private TextView mMessageTV;
    private TextView mTitleTV;

    private String mNegativeText;

    EditText realName_ET;
    EditText mobile_ET;
    EditText email_ET;
    EditText address_ET;

    private String detailAddress = null; //详细地址
    private String longitudeLat = null; //经纬度地址


    private int mStep_Index = STEP_NOTICE;

    private OnClickListener mNegativeClickListener;

    /**
     * 激活结果回调
     */
    private SignCallback mSignCallback;

    private Handler mHandler = new Handler();

    private boolean startSign = false;

    private ReadThread mReadThread;


    private ArrayList<BaseSerialPort> mPorts = new ArrayList<>();
    private int mSignIndex = 0;

    private User user;

    /**
     * 从设备读取到的信息
     * {@deviceInfos[0]}    device SN
     * {@deviceInfos[1]}    device id1
     * {@deviceInfos[2]}    device id2
     */
    private String[] deviceInfos;

    private BaseSerialPort mReadPort;

    private boolean isSigning = false;


    OnDismissListener onDismissListener = new OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            clear();
        }
    };


    /**
     * 百度地图定位
     */
    private LocationClient mLocationClient = null;

    /**
     * 百度地图定位回调
     */
    private BDAbstractLocationListener myListener = new BDAbstractLocationListener(){
        @Override
        public void onReceiveLocation(BDLocation location) {
            detailAddress = location.getAddrStr();    //获取详细地址信息

            // address_ET.setText(longitudeLat);

            // mLocationClient.restart();

            // Toast.makeText(mContext,longitudeLat,Toast.LENGTH_SHORT).show();
            if (detailAddress == null) {
                /**
                 * 重启定位
                 */
                mLocationClient.restart();

            } else {
                Log.e("location", detailAddress);
                double lat = location.getLatitude();
                double log = location.getLongitude();

                longitudeLat = "[" + lat + ", " + log + "]";
                address_ET.setText(detailAddress);
            }
        }
    };

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

    private SignDialog(Context context){
        super(context);
        mContext = context;

        setOnDismissListener(onDismissListener);
    }

    private SignDialog(Context context,int step){
        this(context);

        setStep(step);
    }


    private void initView(){
        mTitleTV = (TextView) findViewById(R.id.dialog_title);
        mTitleTV.setText(mContext.getResources().getString(R.string.sign_Device));

        mPositiveBTN = (Button)findViewById(R.id.dialog_Positive_BTN);
        mNegativeBTN = (Button)findViewById(R.id.dialog_Negative_BTN);
        mMessageTV = (TextView)findViewById(R.id.dialog_Message_TV);
        mMessageTV.setVisibility(View.VISIBLE);
        mUserLine = (LinearLayout) findViewById(R.id.dialog_User);
        mUserLine.setVisibility(View.GONE);

        realName_ET = (EditText)findViewById(R.id.sign_RealName_ET);
        mobile_ET = (EditText)findViewById(R.id.sign_Mobile_ET);
        email_ET = (EditText)findViewById(R.id.sign_Email_ET);
        address_ET = (EditText)findViewById(R.id.sign_Add_ET);

        mPositiveBTN.setVisibility(View.VISIBLE);

        if(mNegativeText != null || mNegativeClickListener != null){
            mNegativeBTN.setVisibility(View.VISIBLE);
            mNegativeBTN.setText(mNegativeText == null?mContext.getString(R.string.cancel):mNegativeText);
        }

        mPositiveBTN.setOnClickListener(this);
        mNegativeBTN.setOnClickListener(this);

        mLocationClient = new LocationClient(getContext().getApplicationContext());
        LocationClientOption option = new LocationClientOption();


        option.setIsNeedAddress(true);
        option.setIsNeedLocationDescribe(true);
        option.setIsNeedLocationPoiList(true);

        mLocationClient.setLocOption(option);
        mLocationClient.registerLocationListener(myListener);

        /**
         * 开始定位
         */
        mLocationClient.start();

        setStep(mStep_Index);

    }


    public SignDialog setStep(int step) {
        if (step == STEP_NOTICE) {
            BluetoothService.getInstance().restartDiscovery();
            mStep_Index = STEP_NOTICE;
            if (mMessageTV != null) {
                mMessageTV.setText(mContext.getString(R.string.sign_Step_NOTICE));
                mMessageTV.setGravity(Gravity.CENTER_VERTICAL);
            }
            if (mPositiveBTN != null) {
                mPositiveBTN.setVisibility(View.VISIBLE);
                mPositiveBTN.setText(mContext.getString(R.string.Sure));
            }

            if(mNegativeBTN != null){
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

            if (mPositiveBTN != null) {
                mPositiveBTN.setVisibility(View.VISIBLE);
                mPositiveBTN.setText(mContext.getString(R.string.cancel));
            }

            if(mNegativeBTN != null){
                mNegativeBTN.setVisibility(View.GONE);
            }

        }

        if (step == STEP_USER){
            mStep_Index = STEP_USER;
            mMessageTV.setVisibility(View.GONE);
            mUserLine.setVisibility(View.VISIBLE);
            mTitleTV.setText(mContext.getResources().getString(R.string.user_sign));

            if (mPositiveBTN != null) {
                mPositiveBTN.setVisibility(View.VISIBLE);
                mPositiveBTN.setText(mContext.getString(R.string.Sure));
            }

            if(mNegativeBTN != null){
                mNegativeBTN.setVisibility(View.VISIBLE);
                mNegativeBTN.setText(mContext.getText(R.string.cancel));
            }

            if (detailAddress == null){
                address_ET.setHint(mContext.getResources().getString(R.string.location));

            }else {
                address_ET.setText(detailAddress);
            }

        }

        if (step == STEP_SIGN) {
            //signDevice();
            mStep_Index = STEP_SIGN;
            mMessageTV.setVisibility(View.VISIBLE);
            mUserLine.setVisibility(View.GONE);
            if (mMessageTV != null) {
                mMessageTV.setText(mContext.getString(R.string.sign_Step_SIGN));
            }
            signDevice();

            if (mPositiveBTN != null)
                mPositiveBTN.setVisibility(View.GONE);
            if (mNegativeBTN != null)
                mNegativeBTN.setVisibility(View.GONE);
        }


        return this;
    }

    public void startSign(){
        startSign = true;
        if(ConnectStatus.getInstance(mContext).getBTPort() != null){
            signWithPort(ConnectStatus.getInstance(mContext).getBTPort());
        }

        if(ConnectStatus.getInstance(mContext).getUSBPort() != null){
            signWithPort(ConnectStatus.getInstance(mContext).getUSBPort());
        }
    }

    public boolean waitSign(){
        return startSign;
    }

    /**
     * 设置 确定按钮 点击事件
     * @param listener
     */
    public SignDialog setNegativeClickListener(String text,OnClickListener listener){
        mNegativeText = text;
        if(mNegativeBTN != null){
            mNegativeBTN.setText(mNegativeText== null?mContext.getString(R.string.cancel):mNegativeText);
        }
        setNegativeClickListener(listener);
        return this;
    }

    /**
     * 设置 取消按钮 点击事件
     * @param listener
     */
    public SignDialog setNegativeClickListener(OnClickListener listener){
        mNegativeClickListener = listener;
        if(mNegativeBTN != null){
            mNegativeBTN.setVisibility(View.VISIBLE);
        }
        return this;
    }

    public SignDialog setSignCallback(SignCallback callback){
        mSignCallback = callback;
        return this;
    }


    public BaseSerialPort getReadPort(){
        return mReadPort;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.dialog_Negative_BTN:
                if (mNegativeClickListener != null) {
                    mNegativeClickListener.onClick(this);
                } else {
                    dismiss();
                }

                break;
            case R.id.dialog_Positive_BTN:
                if (mStep_Index == STEP_USER){
                    if (!signUser()){
                        break;
                    }
                }

                if (mReadPort instanceof UsbSerialPort) {
                    mReadPort.close();
                }
                /*if (mReadPort instanceof BluetoothSerialPort) {
                    ((BluetoothSerialPort) mReadPort).destroy();
                }*/
                if (mStep_Index == STEP_READ){
                    dismiss();
                    break;
                }

                if (mStep_Index == STEP_NOTICE) {
                    if (!BluetoothService.getInstance().isEnabled()) {
                        BluetoothService.getInstance().enableBluetooth();
                    }
                }

                setStep(mStep_Index + 1);
                break;
        }

    }

    //用户信息的输入
    private boolean signUser() {
        boolean result = false;
        String realName = realName_ET.getText().toString();
        String mobile = mobile_ET.getText().toString();
        String email = email_ET.getText().toString();
        String address = address_ET.getText().toString();

        if(realName == null || realName.length() <= 0){
            Toast.makeText(getContext(),mContext.getResources().getString(R.string.toast_Input_realName),Toast.LENGTH_SHORT).show();
            return result;
        }

        if(mobile == null || mobile.length() <= 0){
            Toast.makeText(getContext(),mContext.getResources().getString(R.string.toast_Input_phone),Toast.LENGTH_SHORT).show();
            return result;
        }


        if(address == null || address.length() <= 0){
            Toast.makeText(getContext(),mContext.getResources().getString(R.string.toast_Input_Address),Toast.LENGTH_SHORT).show();
            return result;
        }

        user = new User();
        user.setRealName(realName);
        user.setMobile(mobile);
        user.setEMail(email);
        user.setAddress(address);

        return true;

    }

    /**
     * 启动激活
     * @param port
     */
    public void signWithPort(BaseSerialPort port){

        if(port instanceof BluetoothSerialPort){
            boolean contain = false;
            for(int i= 0;i < mPorts.size();i++){
                if(mPorts.get(i)instanceof BluetoothSerialPort){
                    BluetoothSerialPort bluetoothPort = (BluetoothSerialPort)port;
                    BluetoothSerialPort bluetoothSerialPort = (BluetoothSerialPort)mPorts.get(i);
                    if(bluetoothPort.getDevice().equals(bluetoothSerialPort.getDevice())){
                        contain = true;
                    }
                }
            }
            if(!contain){
                mPorts.add(port);
            }
        }
        else if(!mPorts.contains(port)){
            mPorts.add(port);
        }

        if(mPorts.size() > 0 && !isSigning){
            signIndex(mSignIndex);
        }
        if(port instanceof UsbSerialPort) {
            Log.e("sign", "port-USB");
        }
        else{
            Log.e("sign", "port-Bluetooth");
        }
    }

    /**
     * 激活第 {@index} 个串口
     * @param index
     */
    public void signIndex(int index){
        isSigning = true;
        setStep(STEP_READ);
        if(index >= mPorts.size()){
            signFinish(false,mContext.getString(R.string.sign_Failed));
            return;
        }
        stopRead();

        BaseSerialPort port = mPorts.get(index);
        mReadThread = new ReadThread(port);
        mReadThread.start();
    }

    /**
     * 读取设备SN
     *          {@deviceInfos[0]}   DeviceSN
     *          {@deviceInfos[1]}   COMCHK
     */
    public void onDeviceRead(BaseSerialPort readPort){
        stopRead();
        if(deviceInfos == null || !deviceInfos[0].equals(Config.getInstance(mContext).getBondDevice().getDeviceSN())){
            if(mReadPort instanceof UsbSerialPort){
                mReadPort.close();
            }
            if(mReadPort instanceof BluetoothSerialPort){
                ((BluetoothSerialPort)mReadPort).destroy();
            }
            mSignIndex++;
            signIndex(mSignIndex);
            return;
        }
        mReadPort = readPort;

        if(mReadPort != null){
            if(mReadPort instanceof UsbSerialPort){
                ConnectStatus.getInstance(mContext).enableUSB(true,(UsbSerialPort)mReadPort);
            }
            if(getReadPort() instanceof  BluetoothSerialPort){
                ConnectStatus.getInstance(mContext).enableBT(true,(BluetoothSerialPort) mReadPort);
            }
        }

        setStep(STEP_USER);
    }

    /**
     * 确认设备SN 后在服务器激活
     *          {@deviceInfos[0]}   DeviceSN
     *          {@deviceInfos[1]}   COMCHK
     */
    private void signDevice(){
        String url = null;
        try {
            detailAddress = address_ET.getText().toString();
            url = URLConstant.urlDeviceSign + "?" +
                    "deviceSN=" + deviceInfos[0] + "&" +
                    "id1=" + deviceInfos[1] + "&" +
                    "id2=" + deviceInfos[2] + "&" +
                    "targetID=" + Config.getAndroidID(getContext()) + "&" +
                    "userName=" + URLEncoder.encode(user.getRealName(), "utf-8")  + "&" +
                    "userTel=" + URLEncoder.encode(user.getMobile(),"utf-8")  + "&" +
                    "userAddr=" + URLEncoder.encode((longitudeLat==null?"":longitudeLat) + detailAddress, "utf-8") + "&" +
                    "userEmail=" + URLEncoder.encode(user.getEMail(),"utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        url = url.replaceAll(" ","");
        VolleyUtil.jsonPostRequest(url, getContext(), new VolleyUtil.IVolleyCallback() {
            @Override
            public void getResponse(JSONObject jsonObject) {
                try{
                    // mDialog.dismiss();
                    int code = jsonObject.getInt("code");
                    JSONObject data = jsonObject.getJSONObject("data");
                    //Success
                    if(code == 0){

                        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

                        DeviceBean device = gson.fromJson(data.getJSONObject("Device").toString(),DeviceBean.class);

                        if (device.getDeviceSN().equals(Config.getInstance(mContext).getBondDevice().getDeviceSN())) {
                            Config.getInstance(getContext()).setBondDevice(device);
                            Config.getInstance(getContext()).setSigned(true);
                            signFinish(true,"");
                        }else {
                            signFinish(false,mContext.getResources().getString(R.string.sign_Failed));
                        }
                    }
                    else{
                        String message = data.getString("error");
                        signFinish(false,message);
                    }
                }
                catch (JSONException ex){
                    signFinish(false,mContext.getString(R.string.sign_Failed));
                }
            }

            @Override
            public void onErrorResponse(VolleyError error) {

                if(error instanceof TimeoutError){
                    Toast.makeText(mContext,mContext.getString(R.string.toast_netconn_moreTime),Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(mContext,mContext.getString(R.string.toast_inspect_netconn),Toast.LENGTH_SHORT).show();
                }
                setStep(STEP_USER);
            }
        });
    }


    private void signFinish(boolean success, String message){
        if (success) {
            /**
             * 激活成功
             */
            BaseSerialPort readPort = getReadPort();

            /**
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
                    if(mUsbPorts != null && mUsbPorts.size() > 0){
                        for(int i = 0;i < mUsbPorts.size();i++){
                            UsbService.getInstance().connectSerialPort(mUsbPorts.get(i));
                        }
                    }
                }
            }
        } else {
            /**
             * 激活失败
             */
            Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

            BaseSerialPort readPort = getReadPort();
            if(readPort instanceof UsbSerialPort){
                readPort.close();
            }
            if(readPort instanceof BluetoothSerialPort){
                ((BluetoothSerialPort)readPort).destroy();
            }
        }

        if(mSignCallback != null){
            mSignCallback.onFinish(success,this,message);
        }
    }

    /**
     * 从设备读取信息的线程
     */
    private class ReadThread extends Thread{
        BaseSerialPort mPort;
        public ReadThread(BaseSerialPort port){
            mPort = port;
        }

        @Override
        public void run() {
            Log.e("read",mPort.toString());
            deviceInfos = DeviceDriver.getInstance(mContext).ReadDevice(mPort);

            final BaseSerialPort port = mPort;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onDeviceRead(port);
                }
            });
        }
    }

    public void stopRead(){
        if(mReadThread != null){
            mReadThread.interrupt();
            mReadThread = null;
        }
    }

    /**
     * 销毁
     */
    public static void clear(){
        if(instance != null){
            instance.stopRead();
        }
        instance = null;
    }


    /**
     * 点击事件监听接口
     */
    public interface OnClickListener{
        public abstract void onClick(Dialog dialog);
    }

    public interface SignCallback{
        void onFinish(boolean success, SignDialog dialog, String message);
    }
}
