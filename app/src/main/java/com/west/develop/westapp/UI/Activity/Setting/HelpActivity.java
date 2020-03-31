package com.west.develop.westapp.UI.Activity.Setting;

import android.os.Build;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.west.develop.westapp.R;
import com.west.develop.westapp.UI.Fragment.setting.Help.CommonFragment;
import com.west.develop.westapp.UI.Fragment.setting.Help.GuideFragment;
import com.west.develop.westapp.UI.Fragment.setting.Help.ManualFrament;
import com.west.develop.westapp.UI.base.BaseActivity;
import com.west.develop.westapp.UI.base.BaseFragment;

public class HelpActivity extends BaseActivity implements View.OnClickListener {

    private static final int INDEX_COMMON = 0;
    private static final int INDEX_GUIDE = 1;
    private static final int INDEX_MANUAL = 2;


    private TextView back_tv;
    private TextView title;


    private RadioButton commonButton;
    private LinearLayout guideButton;
    private LinearLayout userBookButton;

    private TextView guideTV;
    private TextView userBookTV;

    FragmentManager fragmentManager;


    CommonFragment commonFragment;
    GuideFragment guideFragment;
    ManualFrament manualFrament;

    @Override
    protected View getContentView() {
        return this.getLayoutInflater().inflate(R.layout.activity_help,null);
    }

    @Override
    protected void initView() {
        back_tv = (TextView) findViewById(R.id.car_back);
        title = (TextView) findViewById(R.id.car_title);

        commonButton = (RadioButton)findViewById(R.id.help_project);
        guideButton = (LinearLayout) findViewById(R.id.help_zhinan);
        userBookButton = (LinearLayout) findViewById(R.id.help_name);

        guideTV = (TextView) findViewById(R.id.help_zhinan_TV);
        userBookTV = (TextView) findViewById(R.id.help_userbook_TV);



    }

    @Override
    protected void initData() {
        //requestVersions();
        title.setText(getResources().getString(R.string.Help));
        refreshDisplay(INDEX_COMMON);
    }

    @Override
    protected void initListener() {
        //返回图标的监听
        back_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        commonButton.setOnClickListener(this);
        guideButton.setOnClickListener(this);
        userBookButton.setOnClickListener(this);
    }

    /**
     * 切换 Fragment
     * @param index
     */
    private void refreshDisplay(int index){
        if(fragmentManager == null){
            fragmentManager = getSupportFragmentManager();
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        BaseFragment fragment = CommonFragment.newInstance();

        hideFragment(fragmentTransaction);

        /**
         * 常见问题
         */
        if(index == INDEX_COMMON){
            commonButton.setBackground(getResources().getDrawable(R.drawable.bg_red_radius,null));
            guideButton.setBackground(getResources().getDrawable(R.drawable.bg_help_style,null));
            userBookButton.setBackground(getResources().getDrawable(R.drawable.bg_help_style,null));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                commonButton.setTextColor(getColor(R.color.white));
                guideTV.setTextColor(getColor(R.color.black));
                userBookTV.setTextColor(getColor(R.color.black));
            }else {
                commonButton.setTextColor(getResources().getColor(R.color.white));
                guideTV.setTextColor(getResources().getColor(R.color.black));
                userBookTV.setTextColor(getResources().getColor(R.color.black));
            }
            if(commonFragment == null) {
                commonFragment = CommonFragment.newInstance();
                fragmentTransaction.add(R.id.main_Frame,commonFragment);
            }
            else{
                fragmentTransaction.show(commonFragment);
            }
            //fragment = commonFragment;
        }

        /**
         * 快速指南
         */
        if(index == INDEX_GUIDE){
            commonButton.setBackground(getResources().getDrawable(R.drawable.bg_help_style,null));
            guideButton.setBackground(getResources().getDrawable(R.drawable.bg_red_radius,null));
            userBookButton.setBackground(getResources().getDrawable(R.drawable.bg_help_style,null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                commonButton.setTextColor(getColor(R.color.black));
                guideTV.setTextColor(getColor(R.color.white));
                userBookTV.setTextColor(getColor(R.color.black));
            }else {
                commonButton.setTextColor(getResources().getColor(R.color.black));
                guideTV.setTextColor(getResources().getColor(R.color.white));
                userBookTV.setTextColor(getResources().getColor(R.color.black));
            }
            if(guideFragment == null){
                guideFragment = GuideFragment.newInstance();
                fragmentTransaction.add(R.id.main_Frame,guideFragment);
            }
            else{
                fragmentTransaction.show(guideFragment);
            }


            //fragment = guideFragment;
        }

        /**
         * 用户手册
         */
        if(index == INDEX_MANUAL){
            commonButton.setBackground(getResources().getDrawable(R.drawable.bg_help_style,null));
            guideButton.setBackground(getResources().getDrawable(R.drawable.bg_help_style,null));
            userBookButton.setBackground(getResources().getDrawable(R.drawable.bg_red_radius,null));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                commonButton.setTextColor(getColor(R.color.black));
                guideTV.setTextColor(getColor(R.color.black));
                userBookTV.setTextColor(getColor(R.color.white));
            }else {
                commonButton.setTextColor(getResources().getColor(R.color.black));
                guideTV.setTextColor(getResources().getColor(R.color.black));
                userBookTV.setTextColor(getResources().getColor(R.color.white));
            }
            if(manualFrament == null){
                manualFrament = ManualFrament.newInstance();
                fragmentTransaction.add(R.id.main_Frame,manualFrament);
            }
            else{
                fragmentTransaction.show(manualFrament);
            }
            //fragment = manualFrament;
        }

        fragmentTransaction.commitAllowingStateLoss();
    }

    private void hideFragment(FragmentTransaction fragmentTransaction) {
        if(manualFrament != null) {
            fragmentTransaction.hide(manualFrament);
        }
        if(commonFragment != null){
            fragmentTransaction.hide(commonFragment);
        }
        if(guideFragment != null){
            fragmentTransaction.hide(guideFragment);
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.help_project:
                refreshDisplay(INDEX_COMMON);
                break;
            case R.id.help_zhinan:
                refreshDisplay(INDEX_GUIDE);
                break;
            case R.id.help_name:
                refreshDisplay(INDEX_MANUAL);
                break;
        }
    }



}