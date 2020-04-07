package com.west.develop.westapp.Protocol.Drivers;

import android.content.Context;
import android.util.Log;

import com.west.develop.westapp.Bean.AppBean.DeviceBean;
import com.west.develop.westapp.Common.BaseSerialPort;
import com.west.develop.westapp.Communicate.COMFunAPI;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Tools.Utils.HexUtil;

/**
 * Created by Develop0 on 2017/12/28.
 */

public class DeviceDriver extends BaseDriver {

    private static DeviceDriver instance;

    public static DeviceDriver getInstance(Context context) {
        mContext = context;
        if (instance == null) {
            instance = new DeviceDriver();
        }

        return instance;
    }

    /**
     * USB连接后检查设备
     * 通过 callback 回调
     */
    public void CheckDevice(final BaseSerialPort port, final CheckCallback callback) {
        String result;

        if (Config.getInstance(mContext).getBondDevice() == null) {
            if (callback != null) {
                callback.callback(null, null);
            }
        }

        /*
         * 打开端口
         */
        Log.e("time", System.currentTimeMillis() + "");
        if (!COMFunAPI.getInstance().COMPortOpen(port, pack.getCOMBT())) {
            COMFunAPI.getInstance().COMPortClose(port);
            if (callback != null) {
                callback.callback(null, null);
            }
        }

        port.setDTR(true);

        /*
         * 检查设备序列号
         */
        Log.e("time", System.currentTimeMillis() + "");
        Byte resultByte = CheckUNSN(port);
        if (resultByte == null || resultByte != CHK_BACK_CMD_SUCCESS) {
            COMFunAPI.getInstance().COMPortClose(port);
            if (callback != null) {
                callback.callback(resultByte, null);
            }
        }

        /*
         * 读取设备版本
         */
        Log.e("time", System.currentTimeMillis() + "");
        result = ReadVersion(port);
        COMFunAPI.getInstance().COMPortClose(port);
        Log.e("version", result + "");
        Log.e("time", System.currentTimeMillis() + "");
        if (result == null) {
            result = "00.00";
        }

        if (callback != null) {
            callback.callback(resultByte, result);
        }
    }


    /**
     * 读取设备信息  UNSN ,COMCHKM,COMCHKS
     *
     * @param port
     * @return
     */
    public String[] ReadDevice(final BaseSerialPort port) {
        boolean result = false;
        if (!COMFunAPI.getInstance().COMPortOpen(port, pack.getCOMBT())) {
            return null;
        }

        COMFunAPI.getInstance().Delayms(100);
        String SN = ReadUNSN(port);
        if (SN == null) {
            COMFunAPI.getInstance().COMPortClose(port);
            return null;
        }

        byte[] COMCHK = UserToBoot(port);
        if (COMCHK == null) {
            BootToUserMode(port);
            COMFunAPI.getInstance().COMPortClose(port);

            return null;
        }

        BootToUserMode(port);

        String[] DeviceInfo = new String[]{SN, HexUtil.toHexString(COMCHK[0]), HexUtil.toHexString(COMCHK[1])};

        COMFunAPI.getInstance().COMPortClose(port);

        return DeviceInfo;

    }


    public String ReadVersion(final BaseSerialPort port) {
        String version = null;
        /*if(!COMFunAPI.getInstance().COMPortOpen(port,pack.getCOMBT())){
            return null;
        }*/

        for (int temp = 1; temp <= 9; temp++) {
            WUNPack[temp] = 0x0;
        }
        WUNPack[0] = CMD_READ_VER;
        PackCRC();
        COMFunAPI.getInstance().COMOutCh(port, WUNPack, 12);
        // COMFunAPI.getInstance().COMDTRSet(port, true);         //DTR 状态

        for (int delay = 0; delay <= 3000; delay++) {
            int size = COMFunAPI.getInstance().COMInSize(port);
            if (size >= 12) {
                RUNPack[0] = COMFunAPI.getInstance().COMInByte(port);

                if (RUNPack[0] == CMD_READ_VER_BACK) {
                    COMFunAPI.getInstance().COMInCh(port, RUNPack, 11); //读入11 Byte ,上面读了1 Byte

                    byte[] verBuf = new byte[5];
                    for (int index = 4; index <= 8; index++) {
                        verBuf[index - 4] = RUNPack[index];
                    }

                    version = new String(verBuf);
                    return version;
                }
            }
            COMFunAPI.getInstance().Delayms(1);
        }
        return version;
    }


    /**
     * 跳到Boot区
     *
     * @return
     */
    public byte[] UserToBoot(final BaseSerialPort port) {
        int recDataSize = 0;

        byte[] COMCHK = null;

        for (int tempCrc = 1; tempCrc <= 9; tempCrc++) {
            WUNPack[tempCrc] = 0x0;
        }
        WUNPack[0] = CMD_USER_TO_BOOT; //检测序列号命令字
        PackCRC();               //算CRC
        COMFunAPI.getInstance().COMOutCh(port, WUNPack, 12);  //发送包
        COMFunAPI.getInstance().COMDTRSet(port, true);         //DTR 状态
        //while (pack.getCtrlSize() == 0) {
        while (recDataSize < 5000) {
            int size = COMFunAPI.getInstance().COMInSize(port);
            if (size >= 12) {
                RUNPack[0] = COMFunAPI.getInstance().COMInByte(port);
                if (RUNPack[0] == CMD_USER_TO_BOOT_BACK) {
                    COMFunAPI.getInstance().COMInCh(port, RUNPack, 11); //读入11 Byte ,上面读了1 Byte

                    byte COMCHKM, COMCHKS;
                    COMCHKM = (byte) ((byte) (RUNPack[0] << 1) | (byte) ((RUNPack[1] & 0xFF) >>> 7));  //再 异或 8A
                    COMCHKM = (byte) (COMCHKM ^ 0x8A);
                    COMCHKM = (byte) (((COMCHKM & 0xFF) >> 5) | (COMCHKM << 3));
                    COMCHKS = (byte) (RUNPack[2] ^ 0x5B);
                    COMCHKS = (byte) ((byte) (COMCHKS << 2) | (byte) ((COMCHKS & 0xFF) >>> 6));
                    Log.e("UserBoot COMCHKM", HexUtil.toHexString(COMCHKM));
                    Log.e("UserBoot COMCHKS", HexUtil.toHexString(COMCHKS));

                    COMCHK = new byte[2];
                    COMCHK[0] = COMCHKM;
                    COMCHK[1] = COMCHKS;

                    return COMCHK;
                }
            }

            COMFunAPI.getInstance().Delayms(1);
            recDataSize++;
        }
        return COMCHK;
    }


    /**
     * 检查能用硬件序列号Moto, EEPROM
     */
    private Byte CheckUNSN(final BaseSerialPort port) {
        Byte result = null;
        byte TempV;

        DeviceBean deviceBean = Config.getInstance(mContext).getBondDevice();
        if (!Config.getInstance(mContext).isSigned() && Config.getInstance(mContext).getBondDevice() == null) {
            return result;
        }
        byte[] snBuf = deviceBean.getDeviceSN().getBytes();
        //序列号入包
        System.arraycopy(snBuf, 0, WUNPack, 1, snBuf.length);
        WUNPack[0] = CMD_CHECK_SN;       //检测序列号命令字
        //UNPack[9] = COMCHKM;   //GCM就要那样,也许下位机程序搞错,就讲究那样写
        WUNPack[9] = (byte) 0x00;
        pack.setCOMCHK(WUNPack[9]);
        //SendPack(pack);
        PackCRC();

        //发送数据
        COMFunAPI.getInstance().COMOutCh(port, WUNPack, 12);

        // COMFunAPI.getInstance().COMDTRSet(port, true);         //DTR 状态
        for (int i = 0; i < 3000; i++) {
            int len = COMFunAPI.getInstance().COMInSize(port);
            if (len > 0) {
                TempV = COMFunAPI.getInstance().COMInByte(port);
                byte ResultData = CheckCOMCHK(TempV, WUNPack[9]);
                result = ResultData;
                Log.e("CheckUNSN", HexUtil.toHexString(ResultData));
                if (ResultData == CHK_BACK_CMD_SUCCESS) {
                    return result;
                } else {
                    // ReportUntil.writeDataToReport(mContext,HexUtil.toHexString(ResultData));
                    return result;
                }
            }
            COMFunAPI.getInstance().Delayms(1);
        }

        return result;
    }


    /**
     * 读设备序列号
     *
     * @param port
     * @return
     */
    public String ReadUNSN(final BaseSerialPort port) {
        boolean result = false;

        String SN = null;

        WUNPack[0] = CMD_READ_SN;

        for (int i = 1; i <= 9; i++) {
            WUNPack[i] = 0;
        }

        PackCRC();

        port.purgeHwBuffers(true, false);

        COMFunAPI.getInstance().COMOutCh(port, WUNPack, 12);  //发送包

        COMFunAPI.getInstance().COMDTRSet(port, true);         //DTR 状态

        for (int delay = 0; delay < 3000; delay++) {
            int size = COMFunAPI.getInstance().COMInSize(port);
            //Log.e("ReadSN",size + "");
            if (size >= 12) {
                RUNPack[0] = COMFunAPI.getInstance().COMInByte(port);

                Log.e("return", HexUtil.toHexString(RUNPack[0]));
                if (RUNPack[0] == CMD_READ_SN_BACK) {
                    COMFunAPI.getInstance().COMInCh(port, RUNPack, 11);

                    byte[] SNbuffer = new byte[8];
                    for (int i = 0; i < 8; i++) {
                        SNbuffer[i] = (byte) ~RUNPack[i];
                    }
                    SN = new String(SNbuffer);
                    Log.e("ReadSN", SN);
                    //pack.setFirmwareSN(SN);
                    result = true;
                    break;
                }
            }
            COMFunAPI.getInstance().Delayms(1);
        }

        return SN;
    }

    /**
     * 退出Boot
     *
     * @param port
     * @return
     */
    public boolean BootToUserMode(BaseSerialPort port) {
        int tempCyc;
        for (tempCyc = 1; tempCyc <= 9; tempCyc++) {
            WUNPack[tempCyc] = 0;//序列号入包
        }
        WUNPack[0] = CMD_CHECK_DOWNLOAD_CRC; //检测序列号命令字
        PackCRC();
        COMFunAPI.getInstance().COMOutCh(port, WUNPack, 12);
        Log.e("BootToUserMode", "BootToUserMode: send finished");

        COMFunAPI.getInstance().COMDTRSet(port, false); //DTR 状态

        return true;
    }

    public interface CheckCallback {
        void callback(Byte resultByte, String version);
    }

}
