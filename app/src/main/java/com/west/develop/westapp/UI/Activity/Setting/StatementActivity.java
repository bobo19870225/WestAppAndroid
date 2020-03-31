package com.west.develop.westapp.UI.Activity.Setting;

import android.content.Intent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.ConnectStatus;
import com.west.develop.westapp.R;
import com.west.develop.westapp.UI.Activity.MainActivity;
import com.west.develop.westapp.UI.Activity.SplashActivity;
import com.west.develop.westapp.UI.base.BaseActivity;

public class StatementActivity extends BaseActivity implements  View.OnClickListener{
    public static final String kAgreed = "isFIRSTRUN";

    boolean isAgreed = false;

    private WebView webView;
    private TextView back;
    private TextView title;

    private LinearLayout mConfirmLayout;
    private CheckBox readCheck;
    private Button sureBTN;
    @Override
    protected View getContentView() {
        return this.getLayoutInflater().inflate(R.layout.activity_statement,null);
    }


    @Override
    protected void initView() {
        isAgreed = getIntent().getBooleanExtra(kAgreed,true);
        setStatusShow(isAgreed);

        back = (TextView) findViewById(R.id.car_back);
        title = (TextView) findViewById(R.id.car_title);
        webView = (WebView) findViewById(R.id.webview);
        mConfirmLayout = (LinearLayout)findViewById(R.id.layout_confirm);
        readCheck = (CheckBox)findViewById(R.id.confirm_CHKBox);
        sureBTN = (Button)findViewById(R.id.sure_BTN);

        String html = getString(R.string.htmlText);
        webView.loadData(html,"text/html; charset=UTF-8", "utf-8");

    }

    @Override
    protected void initData() {
        title.setText(R.string.about_disclaimer);
        if(!isAgreed){
            back.setText(getString(R.string.exit));
            mConfirmLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void initListener() {
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        readCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    sureBTN.setBackground(getDrawable(R.drawable.bg_dialog_positive));
                    sureBTN.setClickable(true);
                }
                else{
                    sureBTN.setBackground(getDrawable(R.drawable.bg_dialog_negative));
                    sureBTN.setClickable(false);
                }
            }
        });

        sureBTN.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.sure_BTN:
                if(readCheck.isChecked()){
                    Config.getInstance(StatementActivity.this).setAgreed(true);
                    Intent intent = new Intent(StatementActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}
