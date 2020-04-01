package com.west.develop.westapp.Dialog;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.west.develop.westapp.Application.MyApplication;
import com.west.develop.westapp.Tools.Diagnosis.KeyEvent;
import com.west.develop.westapp.R;

/**
 * Created by Develop11 on 2017/8/17.
 */

public class TipDialog extends Dialog implements View.OnClickListener {

    private Context mContext;


    /**
     * 标题文字
     */
    String mTitle_Lable;


    /**
     * 标题
     */
    private TextView mTitle;

    /**
     * 消息
     */
    private TextView mMessageTV;

    /**
     * 消息内容
     */
    private String mMessage;

    private String mPositiveText;

    private String mNegativeText;

    /**
     * 取消按钮
     */
    private Button mNegativeBTN;

    /**
     * 确定按钮
     */
    private Button mPositiveBTN;

    /**
     * 确定按钮 点击监听
     */
    private OnClickListener mPositiveClickListener;

    /**
     * 取消按钮 点击监听
     */
    private OnClickListener mNegativeClickListener;

    /**
     * 是否具有 System_Dialog权限
     */
    private boolean isOverlay = false;


    /**
     * 暂停显示
     * 只有当 对话框为 System_Dialog 且正在显示时 应用切换到后台 时，其值为true
     * 当应用切回前台，其值变为false
     */
    private boolean isPaused = false;

    private MReceiver mReceiver = new MReceiver();

    /**
     * 图片控件
     */
    private ImageView image;

    /**
     * 图片内容
     */
    private Drawable mImageDrawable;


    private TipDialog(Context context) {
        super(context);
        mContext = context;

        //checkWindowPermission();
    }


    @Override
    public void show() {
        if (!isShowing()) {
            super.show();
            try {
                if (isOverlay && !isPaused) {
                    Log.e("bo", "Register");
                    IntentFilter filter = new IntentFilter();
                    filter.addAction(MyApplication.ACTION_APP_BACKGROUND);
                    filter.addAction(MyApplication.ACTION_APP_FOREGROUND);
                    mContext.registerReceiver(mReceiver, filter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置 Dialog 布局
        setContentView(R.layout.dialog_show_list);
        //点击边缘无效
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
    }

    /**
     * 初始化
     */
    private void initView() {
        mTitle = (TextView) findViewById(R.id.dialog_title);
        mMessageTV = (TextView) findViewById(R.id.dialog_Message_TV);
        mNegativeBTN = (Button) findViewById(R.id.dialog_Negative_BTN);
        mPositiveBTN = (Button) findViewById(R.id.dialog_Positive_BTN);
        image = (ImageView) findViewById(R.id.dialog_Message_Img);

        initMessage();

        mTitle.setText(mTitle_Lable == null ? "" : mTitle_Lable);

        if (mImageDrawable != null) {
            image.setVisibility(View.VISIBLE);
            image.setImageDrawable(mImageDrawable);
        }

        if (mNegativeText != null) {
            mNegativeBTN.setVisibility(View.VISIBLE);
            mNegativeBTN.setText(mNegativeText);
        }

        if (mPositiveText != null) {
            mPositiveBTN.setVisibility(View.VISIBLE);
            mPositiveBTN.setText(mPositiveText);
        }

        mNegativeBTN.setOnClickListener(this);
        mPositiveBTN.setOnClickListener(this);

    }

    public void setMessage(String message) {
        mMessage = message;
    }


    /**
     * 设置 Message
     */
    private void initMessage() {
        if (mMessageTV == null || mMessage == null || mMessage.length() <= 0) {
            if (mMessageTV != null) {
                mMessageTV.setVisibility(View.GONE);
            }
            return;
        }
        mMessageTV.setVisibility(View.VISIBLE);
        mMessageTV.setText(mMessage);
    }

    /**
     * 设置图片
     *
     * @param mImageDrawable
     */

    public void setImageDrawable(Drawable mImageDrawable) {
        this.mImageDrawable = mImageDrawable;
    }


    /**
     * 设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        mTitle_Lable = title;
    }


    /**
     * 设置 确定按钮 点击事件
     *
     * @param listener
     */
    public void setPositiveClickListener(OnClickListener listener) {
        mPositiveClickListener = listener;
    }

    /**
     * 设置 取消按钮 点击事件
     *
     * @param listener
     */
    public void setNegativeClickListener(OnClickListener listener) {
        mNegativeClickListener = listener;
    }

    /**
     * 设置 确定按钮 点击事件
     *
     * @param listener
     */
    public void setPositiveClickListener(String text, OnClickListener listener) {
        mPositiveText = text;
        mPositiveClickListener = listener;
    }

    /**
     * 设置 取消按钮 点击事件
     *
     * @param listener
     */
    public void setNegativeClickListener(String text, OnClickListener listener) {
        mNegativeText = text;
        mNegativeClickListener = listener;
    }


    public void isOverDialog(boolean isOver) {
        if (isOver) {
            isOverlay = true;
            if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {//6.0+
                getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY);
            } else {
                getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            }
        }
    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (isOverlay && !isPaused) {
            try {
                mContext.unregisterReceiver(mReceiver);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            //取消按钮 点击监听
            case R.id.dialog_Negative_BTN:
                if (mNegativeClickListener != null) {
                    if (mMessageTV.getVisibility() == View.VISIBLE) {
                        mNegativeClickListener.onClick(TipDialog.this, -1, mMessage);
                    }
                } else {
                    //未设置 取消按钮 点击事件
                    TipDialog.this.dismiss();
                    KeyEvent.onKeyClick(KeyEvent.KEY_ESC);
                }
                break;
            //确定按钮 点击监听
            case R.id.dialog_Positive_BTN:
                if (mPositiveClickListener != null) {
                    if (mMessageTV.getVisibility() == View.VISIBLE) {
                        mPositiveClickListener.onClick(TipDialog.this, -1, mMessage);
                    }
                } else {
                    //未设置 确定按钮 点击事件
                    TipDialog.this.dismiss();
                    KeyEvent.onKeyClick(KeyEvent.KEY_ENTER);
                }
                break;
            default:
                break;
        }

    }

    /**
     * Dialog 构建器
     */
    public static class Builder {
        private TipDialog dialog;

        public Builder(Context context) {
            dialog = new TipDialog(context);
        }

        public Builder setTitle(String title) {
            dialog.setTitle(title);
            return this;
        }

        public Builder setMessage(String message) {
            dialog.setMessage(message);
            return this;
        }

      /*  public Builder setPositiveClickListener(TipDialog.OnClickListener listener){
            dialog.setPositiveClickListener(listener);
            return this;
        }

        public Builder setNegativeClickListener(TipDialog.OnClickListener listener){
            dialog.setNegativeClickListener(listener);
            return this;
        }*/

        public Builder setPositiveClickListener(String text, OnClickListener listener) {
            dialog.setPositiveClickListener(text, listener);
            return this;
        }

        public Builder setNegativeClickListener(String text, OnClickListener listener) {
            dialog.setNegativeClickListener(text, listener);
            return this;
        }

        public Builder requestSystemAlert(boolean systemAlert) {
            dialog.isOverDialog(systemAlert);
            return this;
        }

        public Builder setImageDrawable(Drawable mImageDrawable) {
            dialog.setImageDrawable(mImageDrawable);
            return this;
        }

        public TipDialog build() {
            return dialog;
        }
    }

    /**
     * 点击事件监听接口
     */
    public interface OnClickListener {
        public abstract void onClick(Dialog dialogInterface, int index, String label);
    }


    class MReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                case MyApplication.ACTION_APP_BACKGROUND:
                    isPaused = true;
                    dismiss();
                    break;
                case MyApplication.ACTION_APP_FOREGROUND:
                    show();
                    isPaused = false;
                    break;
            }
        }
    }
}
