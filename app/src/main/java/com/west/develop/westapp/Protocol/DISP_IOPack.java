package com.west.develop.westapp.Protocol;

import android.content.Context;
import android.util.Log;

import com.west.develop.westapp.Tools.Utils.HexUtil;
import com.west.develop.westapp.UI.Activity.Diagnosis.RunActivity;

/**
 * Created by Develop0 on 2017/11/15.
 */

public class DISP_IOPack {

    private static DISP_IOPack instance;

    byte[] strBuffer;

    Context mContext;

    private int PAG;
    private int COL;

    /**
     * 正显/反显
     *      0x00    正显
     *      0x01    反显
     */
    private byte NotDisp;

    /**
     * 执行的显示函数
     *      0x00    Clr_Scr
     *      0x10    GENERAL_CN_EN_STR
     *      0x20    SPECIFY_CN_EN_STR
     *      0x30    ASCII_6x8
     *      0x50    PROGRESS
     */
    private byte Disp_FUN;

    /**
     * 待接收的长度
     */
    private int StrLEN;


    /**
     * 创建显示字符接收实例
     * @param context
     * @param pag           行
     * @param col           列
     * @param disp_FUN
     *                   CLR_SCR:             0x00;
     *                   GENERAL_CN_EN_STR:   0x10;
     *                   SPECIFY_CN_EN_STR:   0x20;
     *                   ASCII_6x8:           0x30;
     *                   PROGRESS_BAR:        0x50
     * @param notDisp
     *                  0:正显示
     *                  1:反显示
     *
     * @param strLEN
     * @return
     */
    public static DISP_IOPack newInstance(Context context,byte pag,byte col,byte disp_FUN,byte notDisp,byte strLEN){
        Log.e("DISP_IOPack","create");
        instance = new DISP_IOPack();
        instance.mContext = context;

        instance.PAG = pag;
        if(instance.PAG < 0){
            instance.PAG += 256;
        }

        instance.COL = col;
        if(instance.COL < 0){
            instance.COL += 256;
        }
        instance.NotDisp = notDisp;
        instance.Disp_FUN = disp_FUN;

        instance.StrLEN = strLEN;
        if(instance.StrLEN < 0){
            instance.StrLEN += 256;
        }
        instance.strBuffer = new byte[instance.StrLEN];

        return instance;
    }

    public static DISP_IOPack getInstance(){
        return instance;
    }

    /**
     * 显示字符显示实例 的数据
     */
    public static void DispAndClrInstance(){
        String dispStr = "";
        try{
            Log.e("strBuf", HexUtil.toHexString(instance.strBuffer));
            dispStr = new String(instance.strBuffer,"GBK");
        }
        catch (Exception ex){
            ex.printStackTrace();
            return;
        }
        if(instance.mContext instanceof RunActivity || instance.StrLEN <= 0){
            RunActivity runActivity = (RunActivity)instance.mContext;

            switch (instance.Disp_FUN){
                case (byte)0x00:
                    Log.e("Clr_Scr",dispStr);
                    runActivity.Clr_Scr();
                    break;
                case (byte)0x10:
                    Log.e("GENERAL_CN_EN_STR",dispStr);
                    runActivity.GENERAL_CN_EN_STR(instance.PAG,instance.COL,instance.NotDisp,0,dispStr);
                    break;
                case (byte)0x20:
                    Log.e("SPECIFY_CN_EN_STR",dispStr);
                    runActivity.SPECIFY_CN_EN_STR(instance.PAG,instance.COL,instance.NotDisp,0,dispStr);
                    break;
                case (byte)0x30:
                    Log.e("ASCII_6x8", dispStr);
                    runActivity.ASCII_6x8(instance.PAG,instance.COL,instance.NotDisp,0,dispStr);
                    break;
            }
        }

        instance = null;
    }


    /**
     * 接收  中/英 文 数据
     * @param data
     * @param packIndex
     */
    public void In_EN_CN_ASCII(byte[] data,int packIndex){
        /**
         * 第0包
         * 从第 5(0开始) 个字节开始取值
         */
        if(packIndex == 0x00){
            int index = 5;
            while (StrLEN > 0 && index < 9){
                strBuffer[index - 5] = data[index];
                index++;
                StrLEN--;
            }
        }

        /**
         * 最后一包
         */
        else if(packIndex == 0xFF){
            /**
             * 如果 字符长度 <= 4 ，即第一包同时是最后一包
             * 从第 5(0开始) 个字节开始取值
             */
            if(strBuffer.length <= 0x04){
                int index = 5;
                while (index < 9 && StrLEN > 0){
                    strBuffer[index - 5] = data[index];
                    index++;
                    StrLEN--;
                }
            }

            /**
             * 是最后一包，但不是第一包
             * 从第 1(0开始) 个字节开始取值
             */
            else{
                int index = 1;
                int len = 0;
                if((strBuffer.length - 4) % 8 == 0){
                    len = strBuffer.length - (((strBuffer.length - 4) / 8) - 1) * 8 - 4;
                }
                else{
                    len = strBuffer.length - (((strBuffer.length - 4) / 8)) * 8 - 4;
                }
                while (index <= len && StrLEN > 0){
                    strBuffer[strBuffer.length - len + index - 1] = data[index];
                    index++;
                    StrLEN--;
                }
            }

            /**
             * 待接收字节数 <= 0，代表接收完成
             */
            if(StrLEN <= 0){
                DispAndClrInstance();
            }
            else{
                Log.e("DispFunc","Receive unFinish");
                //DispAndClrInstance();
            }

        }
        else{
            int index = 1;
            while (StrLEN > 0 && index < 9){
                strBuffer[packIndex * 8 - 4 + index -1] = data[index];
                index++;
                StrLEN--;
            }
        }
    }

}
