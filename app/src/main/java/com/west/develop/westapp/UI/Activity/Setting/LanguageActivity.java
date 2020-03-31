package com.west.develop.westapp.UI.Activity.Setting;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.LoadDialog;
import com.west.develop.westapp.UI.base.BaseActivity;
import com.west.develop.westapp.Tools.Utils.LanguageUtil;
import com.west.develop.westapp.Dialog.ConnectStatus;
import com.west.develop.westapp.UI.Activity.MainActivity;
import com.west.develop.westapp.R;

import java.util.Locale;

public class LanguageActivity extends BaseActivity {
    private TextView back;
    private TextView title;
    private RadioButton radioButton_zh;
    private RadioButton radioButton_en;
    private RelativeLayout relativeLayout_zh;
    private RelativeLayout relativeLayout_en;


    @Override
    protected View getContentView() {
        return this.getLayoutInflater().inflate(R.layout.activity_language,null);
    }

    @Override
    protected void initView() {
        back = (TextView) findViewById(R.id.car_back);
        title = (TextView) findViewById(R.id.car_title);
        radioButton_en = (RadioButton) findViewById(R.id.radio_en);
        radioButton_zh = (RadioButton) findViewById(R.id.radio_zh);
        relativeLayout_zh = (RelativeLayout) findViewById(R.id.language_zh_rl);
        relativeLayout_en = (RelativeLayout) findViewById(R.id.language_en_rl);

        if(Config.getInstance(this).getLanguage() == Config.LANGUAGE_EN){
        //if (Config.getInstance(this).getRadio_id() == Config.RADIOBUTTON_EN_ID){
            selectRadioEN();
        }else {
            selectRadioZH();
        }
    }

    //选中英文时radiobutton的变化
    private void selectRadioEN() {
        radioButton_en.setChecked(true);
        radioButton_en.setBackground(getResources().getDrawable(R.mipmap.btn_radio_on_holo_light2,null));
        radioButton_zh.setChecked(false);
        radioButton_zh.setBackground(getResources().getDrawable(R.mipmap.btn_radio_on_disabled_holo_light,null));

    }

    //选中中文时radiobutton的变化
    private void selectRadioZH() {
        radioButton_en.setChecked(false);
        radioButton_en.setBackground(getResources().getDrawable(R.mipmap.btn_radio_on_disabled_holo_light,null));
        radioButton_zh.setChecked(true);
        radioButton_zh.setBackground(getResources().getDrawable(R.mipmap.btn_radio_on_holo_light2,null));

    }

    @Override
    protected void initData() {
        title.setText(R.string.my_language);
    }

    @Override
    protected void initListener() {
        relativeLayout_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Config.getInstance(LanguageActivity.this).getLanguage() != Config.LANGUAGE_EN) {
                    setLanguage(Config.LANGUAGE_EN);
                }

            }
        });

        relativeLayout_zh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Config.getInstance(LanguageActivity.this).getLanguage() != Config.LANGUAGE_CH) {
                    setLanguage(Config.LANGUAGE_CH);
                }
            }
        });
        radioButton_zh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               relativeLayout_zh.performClick();
            }
        });

        radioButton_en.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                relativeLayout_en.performClick();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    LoadDialog dialog;
    //设置中文
    private void setLanguage(final int language) {
        if (language == Config.LANGUAGE_EN || language == Config.LANGUAGE_CH) {
            dialog = new LoadDialog.Builder(this).setTitle(getResources().getString(R.string.tip_message_language_change)).requestSystemAlert(true).build();
            dialog.show();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    SystemClock.sleep(300);

                    if(language == Config.LANGUAGE_CH){
                        LanguageUtil.setAppLanguage(LanguageActivity.this, Locale.SIMPLIFIED_CHINESE);
                    }
                    else if( language == Config.LANGUAGE_EN){
                        LanguageUtil.setAppLanguage(LanguageActivity.this,Locale.ENGLISH);
                    }

                    Config.getInstance(LanguageActivity.this).setLanguage(language);

                    //MDBHelper.getInstance(LanguageActivity.this).updateCarNames();

                    dialog.dismiss();
                    mHandler.sendEmptyMessage(MSG_HIDE_STATUS);
                    //跳到主页面，重启app，通过将activity的栈清除，只有重启app，设置的语言才会生效
                    Intent intent = new Intent(LanguageActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                    Looper.loop();
                }
            }).start();
        }
    }

    public static final int MSG_HIDE_STATUS = 1;
    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case MSG_HIDE_STATUS:
                    try {
                        getApplicationContext().unbindService(ConnectStatus.getInstance(LanguageActivity.this).usbConnection);
                        getApplicationContext().unbindService(ConnectStatus.getInstance(LanguageActivity.this).bluetoothConnection);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

}
