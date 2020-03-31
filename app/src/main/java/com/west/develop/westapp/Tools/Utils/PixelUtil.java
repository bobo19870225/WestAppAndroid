package com.west.develop.westapp.Tools.Utils;

import android.content.Context;

/**
 * Created by Develop12 on 2017/9/2.
 */
public class PixelUtil {


    public static int dp2px(Context context,int dp){
        /**
         * density  dp/px
         * dp 固定  手机360dp  平板600dp
         * px（像素）不固定 可能是800，也可能1200 或者其他
         */
        float scale = context.getResources().getDisplayMetrics().density;
        int size = (int)(scale * dp);
        return size;
    }

    public static int px2dp(Context context,int px){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(px/scale);
    }

    /**
     * 屏幕宽度
     * 像素
     * @param context
     * @return
     */
    public static int getScreenWidthPx(Context context){
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 屏幕高度
     * 像素
     * @param context
     * @return
     */
    public static int getScreenHeightPx(Context context){
        return context.getResources().getDisplayMetrics().heightPixels;
    }


    /**
     * 获取屏幕宽度
     * dp
     * @param context
     * @return
     */
    public static int getScreenWidthDP(Context context){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(getScreenWidthPx(context) /scale);
    }

    /**
     * 获取屏幕高度
     * dp
     * @param context
     * @return
     */
    public static int getScreenHeightDP(Context context){
        float scale = context.getResources().getDisplayMetrics().density;
        return (int)(getScreenHeightPx(context) /scale);
    }
}
