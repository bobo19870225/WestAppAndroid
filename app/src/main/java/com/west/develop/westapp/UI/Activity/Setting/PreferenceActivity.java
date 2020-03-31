package com.west.develop.westapp.UI.Activity.Setting;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.R;
import com.west.develop.westapp.UI.base.BaseActivity;

/**
 * Created by Develop0 on 2018/5/26.
 */

public class PreferenceActivity extends BaseActivity implements View.OnClickListener{
    private TextView back_tv;
    private TextView title;

    private EditText TimeoutActive_ET;
    private EditText TimeoutBackup_ET;

    private Button TimeoutActive_BTN;
    private Button TimeoutBackup_BTN;





    @Override
    protected View getContentView() {
        return this.getLayoutInflater().inflate(R.layout.activity_preference,null);
    }



    @Override
    protected void initView() {
        back_tv = (TextView) findViewById(R.id.car_back);
        title = (TextView) findViewById(R.id.car_title);

        TimeoutActive_ET = (EditText)findViewById(R.id.TimeOut_notActive_ET);
        TimeoutBackup_ET = (EditText)findViewById(R.id.TimeOut_Backup_ET);

        TimeoutActive_BTN = (Button)findViewById(R.id.TimeOut_NotActive_BTN);
        TimeoutBackup_BTN = (Button)findViewById(R.id.TimeOut_Backup_BTN);
    }

    @Override
    protected void initData() {
        TimeoutActive_ET.setText(Config.getInstance(this).getTimeoutActive() + "");
        TimeoutBackup_ET.setText(Config.getInstance(this).getTimeoutBackup() + "");
    }

    @Override
    protected void initListener() {
        back_tv.setOnClickListener(this);
        TimeoutActive_BTN.setOnClickListener(this);
        TimeoutBackup_BTN.setOnClickListener(this);

        TimeoutActive_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e("active",s.toString());
                if(s.toString().length() > 0) {
                    int newTimeout = Integer.parseInt(s.toString());
                    if (newTimeout != Config.getInstance(PreferenceActivity.this).getTimeoutActive()) {
                        TimeoutActive_BTN.setVisibility(View.VISIBLE);
                    } else {
                        TimeoutActive_BTN.setVisibility(View.GONE);
                    }
                }

            }
        });

        TimeoutBackup_ET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.e("active",s.toString());
                if(s.toString().length() > 0) {
                    int newTimeout = Integer.parseInt(s.toString());
                    if (newTimeout != Config.getInstance(PreferenceActivity.this).getTimeoutBackup()) {
                        TimeoutBackup_BTN.setVisibility(View.VISIBLE);
                    } else {
                        TimeoutBackup_BTN.setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    /**
     * 设定响应超时时间
     */
    private void modifyTimeoutActive(){
        String timeStr = TimeoutActive_ET.getText().toString();

        if(timeStr == null || timeStr.isEmpty()){
            Toast.makeText(this,getString(R.string.toast_Preference_Input_none),Toast.LENGTH_SHORT).show();
            return;
        }
        int timeout = Integer.parseInt(timeStr);
        if(timeout > 10){
            Toast.makeText(this,getString(R.string.toast_Preference_Input_Large),Toast.LENGTH_SHORT).show();
            return;
        }
        if(timeout < 1){
            Toast.makeText(this,getString(R.string.toast_Preference_Input_Small),Toast.LENGTH_SHORT).show();
            return;
        }
        Config.getInstance(this).setTimeoutActive(timeout);
        TimeoutActive_BTN.setVisibility(View.GONE);
        Toast.makeText(this,getString(R.string.toast_Preference_Success),Toast.LENGTH_SHORT).show();
    }


    /**
     * 修改备份数据上传超时时间
     */
    private void modifyTimeoutBackup(){
        String timeStr = TimeoutBackup_ET.getText().toString();

        if(timeStr == null || timeStr.isEmpty()){
            Toast.makeText(this,getString(R.string.toast_Preference_Input_none),Toast.LENGTH_SHORT).show();
            return;
        }
        int timeout = Integer.parseInt(timeStr);
        if(timeout > 30){
            Toast.makeText(this,getString(R.string.toast_Preference_Input_Large),Toast.LENGTH_SHORT).show();
            return;
        }
        if(timeout < 5){
            Toast.makeText(this,getString(R.string.toast_Preference_Input_Small),Toast.LENGTH_SHORT).show();
            return;
        }
        Config.getInstance(this).setTimeoutBackup(timeout);
        TimeoutBackup_BTN.setVisibility(View.GONE);
        Toast.makeText(this,getString(R.string.toast_Preference_Success),Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.car_back:
                onBackPressed();
                break;
            case R.id.TimeOut_NotActive_BTN:
                modifyTimeoutActive();
                break;
            case R.id.TimeOut_Backup_BTN:
                modifyTimeoutBackup();
                break;
        }
    }
}
