package com.west.develop.westapp.UI.base;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.west.develop.westapp.Dialog.ConnectStatus;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.Utils.ProxyUtil;
import com.west.develop.westapp.Tools.constant.RequestCodeConstant;
import com.west.develop.westapp.videocache.HttpProxyCacheServer;

/**
 * Created by Develop14 on 2017/4/29.
 */
public abstract class BaseActivity extends AppCompatActivity {
    // 可以把常量单独放到一个Class中
    public static final String ACTION_NETWORK_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String ACTION_PUSH_DATA = "fm.data.push.action";
    public static final String ACTION_NEW_VERSION = "apk.update.action";

    boolean statusShow = true;
    public boolean isAttached = false;
    private HttpProxyCacheServer proxy;

    public BaseActivity() {

    }

    //这个地方有点“模板方法“的设计模式样子
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //initContentView(savedInstanceState);
        setContentView(R.layout.activity_base);

        LinearLayout contentLayout = (LinearLayout) this.findViewById(R.id.activity_Content);
        View contentView = getContentView();

        //布局改变监听（用于监听软键盘的显示与隐藏）
        contentLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                Rect rect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

                if (oldBottom != 0 && bottom != 0 && bottom - rect.bottom <= 0) {
                    if(statusShow) {
                        showStatusBar();
                    }
                } else {
                    hideStatusBar();
                }
            }
        });
        contentLayout.addView(contentView);
        initView();
        initListener();
        initData();
    }

    protected void setStatusShow(boolean show){
        statusShow = show;
        if(show){
            showStatusBar();
        }
        else{
            hideStatusBar();
        }
    }


    // 初始化UI，setContentView等
    //protected abstract void initContentView(Bundle savedInstanceState);
    protected abstract View getContentView();
    protected abstract void initView();
    protected abstract void initData();
    protected abstract void initListener();

    /**
     * 隐藏状态栏
     */
    public void hideStatusBar(){
        findViewById(R.id.statusBar_Layout).setVisibility(View.GONE);
        ConnectStatus.getInstance(this).dismiss();
    }

    /**
     * 显示状态栏
     */
    public void showStatusBar(){
        findViewById(R.id.statusBar_Layout).setVisibility(View.VISIBLE);
        ConnectStatus.getInstance(this).show();
    }

    public void showToast(Context context,String msg){
        Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
    }
    // 可能全屏或者没有ActionBar等
    private void setBase() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 例
    }

    protected void addLeftMenu(boolean enable) {
        // 如果你的项目有侧滑栏可以处理此方法
        if (enable) { // 是否能有侧滑栏

        } else {

        }
    }


    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RequestCodeConstant.CODE_SYSTEM_ALERT_WINDOW) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "not granted", Toast.LENGTH_SHORT).show();
                    finish();
                }
                else{
                    ConnectStatus.getInstance(this).show();
                }
            }
            else{
                ConnectStatus.getInstance(this).show();
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
     //   ConnectStatus.getInstance(this).show();

    }

    @Override
    protected void onPause() {
        super.onPause();
       // ConnectStatus.getInstance(this).dismiss();
        //还可能发送统计数据，比如第三方的SDK 做统计需求
    }


    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        isAttached = true;
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isAttached = false;
    }


    public  HttpProxyCacheServer getProxy(Context context){
        return proxy == null ? (proxy = newProxy()) : proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .fileNameGenerator(new ProxyUtil())
                .build();
    }



}
