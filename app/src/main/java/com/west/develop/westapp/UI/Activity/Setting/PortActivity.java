package com.west.develop.westapp.UI.Activity.Setting;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.west.develop.westapp.Application.MyApplication;
import com.west.develop.westapp.Bean.AppBean.DeviceBean;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Bean.AppBean.DocumentVersion;
import com.west.develop.westapp.R;
import com.west.develop.westapp.UI.base.BaseActivity;

public class PortActivity extends BaseActivity {

    private TextView back_tv;
    private TextView title;

    private TextView androidID_TV;

    private TextView userName_TV;
    private TextView userPhone_TV;
    private TextView userMail_TV;
    private TextView userAddr_TV;

    private TextView portSN_TV;
    private TextView portMode_TV;
    private TextView portFWVer_TV;
    private TextView portSignDate_TV;
    private TextView portRemainCount_TV;

    @Override
    protected View getContentView() {
        return this.getLayoutInflater().inflate(R.layout.activity_port,null);
    }

    @Override
    protected void initView() {
        back_tv = findViewById(R.id.car_back);
        title =  findViewById(R.id.car_title);

        androidID_TV = findViewById(R.id.port_AndroidID);

        userName_TV = findViewById(R.id.value_UserName);
        userPhone_TV = findViewById(R.id.value_UserPhone);
        userMail_TV = findViewById(R.id.value_UserMail);
        userAddr_TV = findViewById(R.id.value_UserAddr);

        portSN_TV =  findViewById(R.id.port_sn);
        portMode_TV =  findViewById(R.id.port_hardType);
        portFWVer_TV =  findViewById(R.id.port_hardType_ver);
        portSignDate_TV =  findViewById(R.id.port_sign_date);
        portRemainCount_TV = findViewById(R.id.port_RemainCount);

//        LinearLayout port_info = (LinearLayout) findViewById(R.id.port_info);

    }

    @Override
    protected void initData() {
        title.setText(R.string.my_jietou);
        DeviceBean bean = Config.getInstance(PortActivity.this).getBondDevice();
        if(bean != null) {
            androidID_TV.setText(Config.getAndroidID(this));

            userName_TV.setText(bean.getUserName()==null?"":bean.getUserName());
            userPhone_TV.setText(bean.getUserPhone()==null?"":bean.getUserPhone());
            userMail_TV.setText(bean.getUserMail()==null?"":bean.getUserMail());
            userAddr_TV.setText(bean.getUserAddr()==null?"":bean.getUserAddr());


            portSN_TV.setText(bean.getDeviceSN());
            if(bean.getDeviceMode() == DeviceBean.MODE_RELEASE) {
                portSignDate_TV.setText(bean.getTime());
                portMode_TV.setText(getString(R.string.deviceType_Release));
            } else {
                portSignDate_TV.setText("");
                portMode_TV.setText(getString(R.string.deviceType_Debug));
            }
            DocumentVersion versionFW = ((MyApplication)getApplicationContext()).getCurrentFWVersion();
            if(versionFW != null){
                String versionStr = versionFW.getMain() + "." + versionFW.getSlave();
                portFWVer_TV.setText(versionStr);
            }

            portRemainCount_TV.setText((Config.getInstance(this).getSetRegCount() - Config.getInstance(this).getRegCount()) + "");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        IntentFilter filter = new IntentFilter();
        filter.addAction(MyApplication.ACTION_FWVERSION_REFRESH);
        filter.addAction(MyApplication.ACTION_DEVICE_REFRESH);
        registerReceiver(mReceiver,filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    protected void initListener() {
        back_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if(action.equals(MyApplication.ACTION_FWVERSION_REFRESH)){
                DocumentVersion versionFW = ((MyApplication)getApplicationContext()).getCurrentFWVersion();
                if(versionFW != null){
                    String versionStr = versionFW.getMain() + "." + versionFW.getSlave();
                    portFWVer_TV.setText(versionStr);
                }
            }
            if(action.equals(MyApplication.ACTION_DEVICE_REFRESH)){
                initData();
            }
        }
    };
}
