package com.west.develop.westapp.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.west.develop.westapp.R;

/**
 * Created by Develop0 on 2017/8/24.
 */

public class LoadDialog extends Dialog {

    private Context mContext;

    private TextView mTitleTV;

    private String mTitle;


    private TextView mProgressTV;

    private Button mCancleTV;

    private String mCancelText;

    private int mProgress = 0;

    private boolean showCancel = false;


    private OnClickListener mNegativeClickListener;


    /**
     * 是否具有 System_Dialog权限
     */
    private boolean isOverlay = false;


    private LoadDialog(Context context){
        super(context);
        mContext = context;

    }

    @Override
    public void show() {
        dismiss();
        super.show();
        /*if(!isShowing()) {

        }*/
    }

    public void setOverlay(boolean overlay) {
        if(overlay) {
            isOverlay = true;
            getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
    }

    @Override
    protected void onCreate(Bundle saveInstanceState){
        super.onCreate(saveInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //设置 Dialog 布局
        setContentView(R.layout.dialog_load);
        //点击边缘无效
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        initView();
    }


    private void initView(){
        mTitleTV = (TextView)findViewById(R.id.dialog_Title_TV);
        mTitleTV.setText(mTitle == null?"":mTitle);
        mProgressTV = (TextView)findViewById(R.id.dialog_Process_TV);
        mCancleTV = (Button) findViewById(R.id.dialog_Cancle_TV);
        mCancleTV.setText(mCancelText == null?"":mCancelText);
        mCancleTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNegativeClickListener != null)
                {
                    mNegativeClickListener.onClick(LoadDialog.this);
                    //Toast.makeText(mContext,"cancle",Toast.LENGTH_LONG).show();
                }
                else {
                    dismiss();
                }
            }
        });

        if(showCancel){
            mCancleTV.setVisibility(View.VISIBLE);
        }
    }

    public void setTitle(String title){
        mTitle = title;
        if(mTitleTV != null) {
            mTitleTV.setText(mTitle);
        }
    }

    public void setNegativeClickListener(OnClickListener mNegativeClickListener) {

        this.mNegativeClickListener = mNegativeClickListener;
        mCancleTV.setVisibility(View.VISIBLE);

    }

    public void setProgress(int progress){
        mProgress = progress;
        if(mProgressTV != null){
            mProgress = progress;
            if(mProgress >= 0 && mProgress <= 100) {

                mCancleTV.post(new Runnable() {
                    @Override
                    public void run() {
                        Log.e("Progress",mProgress + "");
                        mProgressTV.setText(mProgress + "");
                        mProgressTV.setVisibility(View.VISIBLE);
                        mCancleTV.setVisibility(View.VISIBLE);

                    }
                });
            }
            else{
                mCancleTV.post(new Runnable() {
                    @Override
                    public void run() {
                        mProgressTV.setVisibility(View.GONE);
                        mCancleTV.setVisibility(View.GONE);
                    }
                });

            }
        }


    }

    public void setCancel(String text,OnClickListener onClickListener){
        showCancel = true;
        mNegativeClickListener = onClickListener;

        mCancelText = text;
        if(mCancleTV != null){
            mCancleTV.setText(text==null?"":text);
            mCancleTV.setVisibility(View.VISIBLE);
        }
    }


    public static class Builder{
        private LoadDialog dialog;
        public Builder(Context context){
            dialog = new LoadDialog(context);
        }

        public Builder setTitle(String message){
            dialog.setTitle(message);
            return this;
        }

        public Builder requestSystemAlert(boolean systemAlert){
            dialog.setOverlay(systemAlert);
            return this;
        }

        public Builder setCancel(String text,OnClickListener clickListener){
            dialog.setCancel(text,clickListener);
            return this;
        }
        public LoadDialog build(){
            //dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            return dialog;
        }
    }

    /**
     * 点击事件监听接口
     */
    public interface OnClickListener{
        void onClick(Dialog dialog);
    }
}
