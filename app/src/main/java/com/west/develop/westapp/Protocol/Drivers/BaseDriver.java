package com.west.develop.westapp.Protocol.Drivers;

import android.content.Context;
import android.util.Log;

import com.west.develop.westapp.Common.BaseSerialPort;
import com.west.develop.westapp.Communicate.COMFunAPI;
import com.west.develop.westapp.Protocol.TIOPack;
import com.west.develop.westapp.Tools.Utils.HexUtil;
/**
 * Created by Develop0 on 2017/11/14.
 */
public abstract class BaseDriver extends BaseCMD {
    public static final int DEFAULT_PACK_MAX_SIZE = 260;

    // protected byte[] UNPack = new byte[DEFAULT_PACK_MAX_SIZE];

    public static final int UPDATE_TYPE_APP = 0;
    public static final int UPDATE_TYPE_FW = 1;


    /**
     * 应用程序下载地址
     */
    public static final int START_ADDR_APPLICATION = 0x10000;
    /**
     * 固件程序下载地址
     */
    public static final int START_ADDR_SYSTEM = 0x00000;

    protected byte[] RUNPack = new byte[DEFAULT_PACK_MAX_SIZE];
    protected byte[] WUNPack = new byte[DEFAULT_PACK_MAX_SIZE];

    protected TIOPack pack = new TIOPack();

    protected static Context mContext;

    BaseSerialPort mPort;


    public BaseSerialPort getPort() {
        return mPort;
    }

    public void initPort(BaseSerialPort port) {
        mPort = port;
    }


    /**
     * 计算长包 CRC 校验值
     * @param length
     */
    public void LongPackCRC(int length) {
        WUNPack[length + 3] = 0;
        WUNPack[length + 4] = 0;

        int sum = 0;

        for (int i = 0; i <= length + 2; i++) {
            int value = WUNPack[i];
            if (value < 0) {
                value += 256;
            }
            sum += value;
        }

        WUNPack[length + 3] = (byte) (sum >> 8);
        WUNPack[length + 4] = (byte) sum;
    }


    /**
     * 发送长包
     * @param pack
     * @return
     */
    public boolean SendLongPack(TIOPack pack) {
        LongPackCRC(pack.getPackSize());
        return COMFunAPI.getInstance().COMOutCh(getPort(), WUNPack, (pack.getPackSize() + 1) + 4);

    }


    /**
     * 读出仪器程序和数据
     * 为了适应大容量器件提高通讯效率，2010 01 29增加了可设置每次读写数据长度的新协议
     * //检查信息包中的CRC,正确是返回true
     *
     * @param packlong
     * @return
     */
    public boolean CheckLongPackCRC(int packlong) {
        int tempCyc;
        int tempV = 0;
        byte packCRCH;
        byte packCRCL;
        for (tempCyc = 0; tempCyc <= packlong + 2; tempCyc++) {
            tempV += RUNPack[tempCyc];
        }
        packCRCH = (byte) ((tempV & 0xFF) >> 8);//取高位
        packCRCL = (byte) ((tempV << 24) >> 24);//取低位
        return packCRCH == RUNPack[packlong + 3] && packCRCL == RUNPack[packlong + 4];
    }

    /**
     * 读出仪器程序和数据
     * 为了适应大容量器件提高通讯效率，2010 01 29增加了可设置每次读写数据长度的新协议
     * //检查信息包中的CRC,正确是返回true
     *
     * @param packlong
     * @return
     */
    public boolean CheckLongPackCRC(byte TempV, byte lenByt, int packlong) {
        int length = lenByt & 0xFF;
        int sum = (TempV & 0xFF) + (lenByt & 0xFF);

        int tempCyc;
        for (tempCyc = 0; tempCyc < packlong; tempCyc++) {
            // Log.e("tempV",RUNPack[tempCyc] + "");
            sum += RUNPack[tempCyc] & 0xFF;
        }
        byte packCRCH = (byte) (sum >> 8);//取高位
        byte packCRCL = (byte) (sum);//取低位

        Log.e("crcH", HexUtil.toHexString(packCRCH) + "");
        Log.e("crcH", HexUtil.toHexString(RUNPack[packlong]) + "");
        return packCRCH == RUNPack[packlong] && packCRCL == RUNPack[packlong + 1];
    }


    /**
     * 发送包
     * @param pack
     * @return
     */
    public boolean SendPack(TIOPack pack) {
        int tempCyc;
        boolean result = false;
        PackCRC(); //算CRC

        result = COMFunAPI.getInstance().COMOutCh(getPort(), WUNPack, 12);

        return result;
    }

    public boolean SendByte() {
        boolean result = false;

        result = COMFunAPI.getInstance().COMOutByte(getPort(), WUNPack[0]);
        return result;
    }


    /**
     * 检查信息包中的CRC,正确是返回true
     * @return
     */
    public boolean CheckPackCRC() {
        boolean result = false;
        int tempV = 0;
        int tempCyc;
        byte pack10;
        byte pack11;
        for (tempCyc = 0; tempCyc <= 9; tempCyc++) {
            int packV = RUNPack[tempCyc];
            if (packV < 0) {
                packV += 256;
            }
            tempV += packV;
        }
        pack10 = (byte) (tempV >> 8);  //去高位
        pack11 = (byte) tempV; //取低位
        if (pack10 == RUNPack[10] && pack11 == RUNPack[11]) {
            result = true;
        }
        Log.e("CheckPackCRC", "Checkresult: " + result);
        return result;
    }

    /**
     * 检查信息包中的CRC,正确是返回true
     * @CMD 指令
     * @return
     */
    public boolean CheckPackCRC(byte CMD) {
        boolean result = false;
        int tempV = 0;
        int tempCyc;
        byte pack10;
        byte pack11;
        for (tempCyc = 0; tempCyc <= 8; tempCyc++) {
            int packV = RUNPack[tempCyc];
            if (packV < 0) {
                packV += 256;
            }
            tempV += packV;
        }
        int cmd = CMD;
        if (cmd < 0) {
            cmd += 256;
        }
        tempV += cmd;
        pack10 = (byte) (tempV >> 8);  //去高位
        pack11 = (byte) tempV; //取低位
        if (pack10 == RUNPack[9] && pack11 == RUNPack[10]) {
            result = true;
        }
        return result;
    }

    /**
     * 解密返回值
     * @param rev
     * @param chk
     * @return //返回值
     * {
     * 0A  通读错误
     * 0E  硬件序列号错
     * 0D  超时
     * 0C  数据CRC错
     * 0F  写入错
     * 10  写入成功
     * 11  命令不支持
     * 13  电源控制错
     * 0B  命令完成或序列号正确
     * }dataBits = 5;
     */
    public byte CheckCOMCHK(byte rev, byte chk) {
        return (byte) (~rev - (chk & 0x0F));
    }

    public byte RevertCOMCHK(byte res, byte chk) {
        return (byte) ~res;
    }

    /**
     * 计算信息包中的CRC
     */
    public void PackCRC() {
        WUNPack[10] = 0x0;
        WUNPack[11] = 0x0;
        int sum = 0;
        for (int i = 0; i <= 9; i++) {
            int packI = WUNPack[i];
            if (packI < 0) {
                packI += 256;
            }
            sum = sum + packI;
        }

        WUNPack[10] = (byte) (sum >> 8);
        WUNPack[11] = (byte) sum;
    }


    public int CpyByte(byte[] from, int offset, byte[] dest) {
        int len = Math.min(from.length - offset, dest.length);

        for (int i = 0; i < len; i++) {
            dest[i] = from[i + offset];
        }

        return len;
    }


    public void SEND_TIMEOUT() {
        COMFunAPI.getInstance().COMOutByte(getPort(), CMD_CLEAR_TIMEOUT);
    }

}
