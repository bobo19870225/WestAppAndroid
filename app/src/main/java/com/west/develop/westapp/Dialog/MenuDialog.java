package com.west.develop.westapp.Dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.west.develop.westapp.R;

/**
 * Created by Develop0 on 2018/3/15.
 */

public class MenuDialog {

    private PopupWindow popupWindow;

    private View mView;

    private LinearLayout mContentLayout;

    private RelativeLayout mDialogContent;

    private Context mContext;
    public MenuDialog(Context context){
        mContext = context.getApplicationContext();
        mView = LayoutInflater.from(context).inflate(R.layout.dialog_menu_layout,null);
        mDialogContent = (RelativeLayout)mView.findViewById(R.id.dialog_Comment);
        mContentLayout = (LinearLayout)mView.findViewById(R.id.dialog_Content);
    }

    public void addButton(String text, final OnClickListener listener){
        Button button = new Button(mContext);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            button.setBackgroundColor(mContext.getResources().getColor(R.color.main_toolbar,null));
        }
        else {
            button.setBackgroundColor(mContext.getResources().getColor(R.color.main_toolbar));
        }
        button.setTextColor(Color.parseColor("#232323"));
        button.setTextSize(18);

        button.setText(text);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listener != null){
                    listener.onClick(MenuDialog.this);
                }
            }
        });

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dip2px(49.5f));

        View border = new View(mContext);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            border.setBackgroundColor(mContext.getResources().getColor(R.color.border,null));
        }
        else {
            border.setBackgroundColor(mContext.getResources().getColor(R.color.border));
        }
        LinearLayout.LayoutParams borderLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dip2px(1f));
        border.setBackgroundColor(Color.parseColor("#232323"));

        if(mContentLayout.getChildCount() > 0) {
            mContentLayout.addView(border, borderLP);
        }
        mContentLayout.addView(button, lp);
    }

    public void addButton(String text,View.OnClickListener listener,int color){
        Button button = new Button(mContext);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            button.setBackgroundColor(mContext.getResources().getColor(R.color.main_toolbar,null));
        }
        else {
            button.setBackgroundColor(mContext.getResources().getColor(R.color.main_toolbar));
        }

        button.setTextSize(18);
        button.setTextColor(color);

        button.setText(text);
        button.setOnClickListener(listener);


        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,dip2px(49.5f));

        View border = new View(mContext);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            border.setBackgroundColor(mContext.getResources().getColor(R.color.border,null));
        }
        else {
            border.setBackgroundColor(mContext.getResources().getColor(R.color.border));
        }

        LinearLayout.LayoutParams borderLP = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,dip2px(1f));
        border.setBackgroundColor(Color.parseColor("#232323"));

        if(mContentLayout.getChildCount() > 0) {
            mContentLayout.addView(border, borderLP);
        }
        mContentLayout.addView(button,lp);

    }

    public void show(View parent,int off_bottom){
        popupWindow = new PopupWindow(mView,ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuDialog.this.hide();
            }
        });
        mView.findViewById(R.id.dialog_Comment_Cancel_Btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuDialog.this.hide();
            }
        });

        mView.setFocusable(true); // 这个很重要
        mView.setFocusableInTouchMode(true);
        popupWindow.setFocusable(true);

        mView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    //accountDialog.dismiss();
                    //accountDialog = null;
                    hide();
                    return true;
                }
                return false;
            }
        });
        mDialogContent.setPadding(0,0,0,off_bottom);
        //popupWindow.setAnimationStyle(R.style.comment_dialog_style);
        popupWindow.showAtLocation(parent, Gravity.BOTTOM, 0, 0);

        Animation fadeIn_anim = (Animation) AnimationUtils.loadAnimation(mContext,R.anim.anim_fadein_bottom);

        mView.findViewById(R.id.dialog_Comment_Content).startAnimation(fadeIn_anim);

    }

    public void hide(){

        Animation fadeOut_anim = (Animation) AnimationUtils.loadAnimation(mContext,R.anim.anim_fadeout_bottom);

        fadeOut_anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                popupWindow.dismiss();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        mView.findViewById(R.id.dialog_Comment_Content).startAnimation(fadeOut_anim);
    }

    private int dip2px(float dip){

        float density = mContext.getResources().getDisplayMetrics().density;

        return (int)(density * dip);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public boolean isShow(){

        return popupWindow != null && popupWindow.isShowing();
    }


    public static class Builder{
        MenuDialog dialog;
        public Builder(Context context){
            dialog = new MenuDialog(context);
        }

        public Builder addButton(String text, OnClickListener listener){
            dialog.addButton(text,listener);
            return this;
        }

        public MenuDialog build(){
            return  dialog;
        }
    }


    public interface OnClickListener{
        void onClick(MenuDialog dialog);
    }
}
