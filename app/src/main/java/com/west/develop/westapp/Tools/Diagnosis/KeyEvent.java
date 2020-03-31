package com.west.develop.westapp.Tools.Diagnosis;

import com.west.develop.westapp.Protocol.Drivers.RunningDriver;

/**
 * Created by Develop11 on 2017/8/17.
 */

public class KeyEvent {
    public static final String TAG = KeyEvent.class.getSimpleName();


    /**
     * 释放按键
     * 当手指离开屏幕时  Action_UP 执行
     */
    public static final int KEY_RELEASE = 0x00;
    /**
     * NO 按钮
     */
    public static final  int KEY_ESC = 0x03;

    /**
     * YES 按钮
     */
    public static final  int KEY_ENTER = 0x01;
    /**
     * UP 按钮
     */
    public static final  int KEY_UP = 0x02;
    /**
     * DOWN 按钮
     */
    public static final  int KEY_DOWN = 0x05;
    /**
     * LEFT 按钮
     */
    public static final  int KEY_LEFT = 0x04;
    /**
     * RIGHT 按钮
      */
    public static final  int KEY_RIGHT = 0x06;

    /**
     * 单例
     */
    private static KeyEvent instance;

    /**
     * 获取单例
     * @return
     */
    public static KeyEvent getInstance(){
        if(instance == null){
            instance = new KeyEvent();
        }

        return instance;
    }

    /**
     * 用户按键 回调
     * @param value
     */
    public static void onKeyClick(int value){
        if(RunningDriver.getInstance() != null) {
            RunningDriver.getInstance().onKeyEvent(value);
        }
    }


}
