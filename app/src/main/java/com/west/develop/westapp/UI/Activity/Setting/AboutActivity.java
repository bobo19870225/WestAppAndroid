package com.west.develop.westapp.UI.Activity.Setting;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import com.west.develop.westapp.R;
import com.west.develop.westapp.UI.base.BaseActivity;

public class AboutActivity extends BaseActivity {

    private TextView back_tv;
    private TextView title;
    private TextView phoneTV1;
    private TextView phoneTV2;
    private TextView phoneTV3;
    private TextView phoneTV4;
    private TextView phoneTV5;
    private TextView phoneTV6;


    @Override
    protected View getContentView() {
        return this.getLayoutInflater().inflate(R.layout.activity_about, null);
    }

    @Override
    protected void initView() {
        back_tv = (TextView) findViewById(R.id.car_back);
        title = (TextView) findViewById(R.id.car_title);
        phoneTV1 = (TextView) findViewById(R.id.about_mobile1);
        phoneTV2 = (TextView) findViewById(R.id.about_mobile2);
        phoneTV3 = (TextView) findViewById(R.id.about_mobile3);
        phoneTV4 = (TextView) findViewById(R.id.about_mobile4);
        phoneTV5 = (TextView) findViewById(R.id.about_mobile5);
        phoneTV6 = (TextView) findViewById(R.id.about_mobile6);

    }

    @Override
    protected void initData() {
        title.setText(R.string.setting_about);

    }

    @Override
    protected void initListener() {
        back_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        phoneTV1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = phoneTV1.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num));
                startActivity(intent);
            }
        });

        phoneTV2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = phoneTV2.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num));
                startActivity(intent);
            }
        });

        phoneTV3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = phoneTV3.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num));
                startActivity(intent);
            }
        });

        phoneTV4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = phoneTV4.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num));
                startActivity(intent);
            }
        });

        phoneTV5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = phoneTV5.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num));
                startActivity(intent);
            }
        });

        phoneTV6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = phoneTV6.getText().toString();
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + num));
                startActivity(intent);
            }
        });

    }

}
