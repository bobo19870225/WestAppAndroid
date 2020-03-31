package com.west.develop.westapp.Protocol.Drivers;

import android.content.Context;
import android.util.Log;

import com.west.develop.westapp.Bean.AppBean.DeviceBean;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Tools.Utils.HexUtil;
import com.west.develop.westapp.Tools.Utils.ReportUntil;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Communicate.COMFunAPI;
import com.west.develop.westapp.Tools.Diagnosis.DiagnosisAPI;
import com.west.develop.westapp.Protocol.TIOPack;
import com.west.develop.westapp.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Develop12 on 2017/10/16.
 */
public class UpDriver extends BaseDriver {
    public static final int DEFAULT_PACK_MAX_SIZE = 260;

    private static UpDriver instance;


    //private byte[] UNPack = new byte[DEFAULT_PACK_MAX_SIZE];
    private int[] IdCount = new int[3];
    private int[] UserCount = new int[3];
    private byte[] CodePack = new byte[8];
    //private TIOPack pack = new TIOPack();
    private byte COMCHKM;  //下位机主CPU KEY
    private byte COMCHKS;  //下位机付CPU KEY

    public static UpDriver getInstance(Context context) {
        mContext = context;
        if (instance == null) {
            instance = new UpDriver();
        }
        return instance;
    }

    public void initDebug(boolean isDebug){
        pack.setDebugMode(isDebug);
    }

    public TIOPack getPack() {
        return pack;
    }

    private UpDriver() {
    }


    /**
     * 加载下载文件大小
     * @param _type         更新类型：0--APP程序,1--固件程序
     * @param file          APP程序文件
     * @param buffer        固件程序缓存
     * @param memorySize
     * @return
     */
    public boolean DownFileLoad(int _type,File file, byte[] buffer,int memorySize) {
        long fileSize = 0;
        if(_type == UPDATE_TYPE_APP) {
            fileSize = file == null?0:file.length();
        }
        else if(_type == UPDATE_TYPE_FW){
            fileSize = buffer==null?0:buffer.length;
        }

        if(fileSize <= 0){
            return false;
        }

        if (fileSize % (pack.getPackSize() + 1) != 0) {
            fileSize += ((pack.getPackSize() + 1) - (fileSize % (pack.getPackSize() + 1)));//MEGA64的每页数据需要256 BYTE，不足则要补充0xFF
        }
        pack.setCtrlSize((int)fileSize); //文件大小
        if (pack.getCtrlSize() > memorySize + 4096){
            pack.setStateRe((byte) 0x40);   //文件太大
            return false;
        }
        if (pack.getCtrlSize() == 0x20000){
            pack.setCtrlSize(pack.getCtrlSize() - 4096);
        }
        return true;
    }

    /**
     *  加载校验值
     * @param _type         更新类型：0--APP程序,1--固件程序
     * @param file          APP程序文件
     * @param buffer        固件程序缓存
     * @return
     */
    public boolean DownloadCRC(int _type,File file,byte[] buffer){
        byte[] CRC = new byte[2];
        boolean result = false;

        if(_type == UPDATE_TYPE_APP) {
            if(!pack.isDebugMode()) {
                String crcPath = file.getParent() + "/" + FileUtil.DOWNLOAD_CRC_NAME;
                File CRCFile = new File(crcPath);
                if (!CRCFile.exists()) {
                    return result;
                }
                try {
                    FileInputStream fis = new FileInputStream(CRCFile);
                    fis.read(CRC);
                } catch (FileNotFoundException ex) {
                    return result;
                } catch (IOException ex) {
                    return result;
                }
            }
            else{

            }
        }
        else if(_type == UPDATE_TYPE_FW){
            if(buffer == null || buffer.length == 0){
                return result;
            }
            int Index = 0;
            long sum = 0;
            for(int i = 0;i <= buffer.length;i++){
                sum += buffer[i];
                if(buffer[i] < 0){
                    //byte 转 int 变成负数
                    sum += 256;
                }
                Index++;
                if(Index >= buffer.length){
                    //不足一包的，在包后补 0xFF
                    for(;i <= pack.getPackSize();i++){
                        sum += 0xFF;
                    }
                    break;
                }
            }

            CRC[0] = (byte)(sum >> 8);
            CRC[1] = (byte)sum;
        }

        pack.setDownloadCRC(CRC);
        return true;
    }


    /**
     * 下载程序
     * @param file              用户程序
     * @param buffer            系统程序缓存
     * @param memorySize
     * @param DeviceType
     * @param memoryArea
     * @return
     */
    public boolean DownLoadFun(int _type,File file, byte[] buffer,int memorySize, byte DeviceType, byte memoryArea) {
        boolean result = false;
        pack.setDownloading(true);

        if(_type != UPDATE_TYPE_APP && _type != UPDATE_TYPE_FW){
            pack.setDownloading(false);
            return result;
        }

        switch (memoryArea) {
            case 1:
                if (DeviceType == (byte) 0x35) {
                    pack.setPackSize(0xFF); //每次写 $FF + 1个字节
                }
                else if (DeviceType == (byte) 0x38) {
                    pack.setPackSize(0xFF);
//A168ToolSPICarEncry(UpData,0,UpdataSize - 1);
                } else {
                    pack.setStateRe((byte) 24);
                    COMFunAPI.getInstance().COMDTRSet(getPort(), false);
                    COMFunAPI.getInstance().COMPortClose(getPort());
                }
                break;
            case 2:
                pack.setPackSize(0);
                break;
            case 3:
                pack.setPackSize(0xFF);
                break;
            case 4:
                pack.setPackSize(0xFF);
                break;
            default:
                pack.setStateRe((byte) 24);
                //COMFunAPI.getInstance().COMDTRSet(getPort(), false);
                //COMFunAPI.getInstance().COMPortClose(getPort());
                break;
        }

        /**
         * 加载文件大小
         */
        if (!DownFileLoad(_type,file,buffer, memorySize)) {
            pack.setDownloading(false);
            ReportUntil.writeDataToReport(mContext,mContext.getString(R.string.large_File));
            return result;
        }

        /**
         * 加载校验和
         */
        if(!pack.isDebugMode() && !DownloadCRC(_type,file,buffer)){
            pack.setDownloading(false);
            ReportUntil.writeDataToReport(mContext,"Read DownloadTaskActivity CRC Failed");
            return result;
        }

        /**
         * 打开端口
         */
        if (!COMFunAPI.getInstance().COMPortOpen(getPort(), pack.getCOMBT())) {
            pack.setDownloading(false);
            ReportUntil.writeDataToReport(mContext,"port opened failed");
            return result;
        }
        COMFunAPI.getInstance().COMDTRSet(getPort(), true);
        COMFunAPI.getInstance().Delayms(100);

        if (!COMFunAPI.getInstance().COMBufClt(getPort(), 0)) {
            COMFunAPI.getInstance().Delayms(100);
        }


        /**
         * 升级固件程序需要跳转到BOOT
         */
        if(_type == UPDATE_TYPE_FW){
            if(!UserToBoot()){
                pack.setDownloading(false);
                COMFunAPI.getInstance().COMDTRSet(getPort(), false);
                COMFunAPI.getInstance().COMPortClose(getPort());
                ReportUntil.writeDataToReport(mContext,"Jump to BOOT failed");
            }
        }

        /**
         * 检查序列号
         */
        if (!pack.isDownloading() || !CheckUNSN()) {
            //BootToUserMode();
            pack.setDownloading(false);
            COMFunAPI.getInstance().COMDTRSet(getPort(), false);
            COMFunAPI.getInstance().COMPortClose(getPort());
            ReportUntil.writeDataToReport(mContext,"CheckUNSN failed");
            return result;
        }

        /**
         * 准备写入
         */
        if (!ReadyProg(_type,DeviceType, memoryArea)) {
            //BootToUserMode();
            pack.setDownloading(false);
            pack.setStateRe((byte) 38);
            COMFunAPI.getInstance().COMDTRSet(getPort(), false);
            COMFunAPI.getInstance().COMPortClose(getPort());
            ReportUntil.writeDataToReport(mContext,"ReadyPro failed");
            return result;
        }

        /**
         * 下载数据
         */
        if (!WriteDataFun(_type,file,buffer)) {
            //BootToUserMode();
            pack.setDownloading(false);
            pack.setStateRe((byte) 17);
            COMFunAPI.getInstance().COMDTRSet(getPort(), false);
            COMFunAPI.getInstance().COMPortClose(getPort());
            ReportUntil.writeDataToReport(mContext,"WriteData failed");
            return result;
        }

        /**
         * 检验校验和
         */
        if(!pack.isDebugMode() && !CheckDownloadCRC()){
            pack.setDownloading(false);
            switch (DeviceType) {
                case 0x34:
                    pack.setStateRe((byte) 39);
                    break;
                case 0x35:
                    pack.setStateRe((byte) 43);
                    break;
                case 0x37:
                    pack.setStateRe((byte) 43);
                    break;
                case 0x38:
                    pack.setStateRe((byte) 43);
                    break;
            }

            COMFunAPI.getInstance().COMDTRSet(getPort(), false);
            COMFunAPI.getInstance().COMPortClose(getPort());
            ReportUntil.writeDataToReport(mContext,"BootToUser failed");
            return result;
        }

        COMFunAPI.getInstance().COMDTRSet(getPort(), false);
        COMFunAPI.getInstance().COMPortClose(getPort());
        pack.setDownloading(false);
        return true;
    }

    private void ReadCRC(){
        for(int i = 1;i <= 9;i++){
            WUNPack[i] = 0;
        }

        WUNPack[0] = CMD_READ_DOWNLOAD_CRC;
        PackCRC();
        COMFunAPI.getInstance().COMOutCh(getPort(), WUNPack, 12);
    }

    /**
     * 跳到Boot区
     * @return
     */
    public boolean UserToBoot() {
        int recDataSize = 0;

        for (int tempCrc = 1; tempCrc <= 9; tempCrc++) {
            WUNPack[tempCrc] = 0x0;
        }
        WUNPack[0] = CMD_USER_TO_BOOT; //检测序列号命令字
        PackCRC();               //算CRC
        COMFunAPI.getInstance().COMOutCh(getPort(), WUNPack, 12);  //发送包
        COMFunAPI.getInstance().COMDTRSet(getPort(), true);         //DTR 状态
        //while (pack.getCtrlSize() == 0) {
        while (recDataSize < 5000) {
            int size = COMFunAPI.getInstance().COMInSize(getPort());
            if (size >= 12) {
                RUNPack[0] = COMFunAPI.getInstance().COMInByte(getPort());
                if (RUNPack[0] == CMD_USER_TO_BOOT_BACK) {
                    COMFunAPI.getInstance().COMInCh(getPort(), RUNPack, 11); //读入11 Byte ,上面读了1 Byte

                    byte COMCHKM,COMCHKS;
                    COMCHKM = (byte) ((byte) (RUNPack[0] << 1) | (byte) ((RUNPack[1] & 0xFF) >>> 7));  //再 异或 8A
                    COMCHKM = (byte) (COMCHKM ^ 0x8A);
                    COMCHKM = (byte) (((COMCHKM & 0xFF) >> 5) | (COMCHKM << 3));
                    COMCHKS = (byte) (RUNPack[2] ^ 0x5B);
                    COMCHKS = (byte) ((byte) (COMCHKS << 2) | (byte) ((COMCHKS & 0xFF) >>> 6));
                    Log.e("UserBoot COMCHKM", HexUtil.toHexString(COMCHKM));
                    Log.e("UserBoot COMCHKS", HexUtil.toHexString(COMCHKS));

                    return true;
                }

            }
            if(size >= 1){
                byte TempV = COMFunAPI.getInstance().COMInByte(getPort());
                byte ResultData = CheckCOMCHK(TempV, WUNPack[9]);

                if(ResultData == CHK_BACK_CMD_UNSURPPOT){
                    return true;
                }
            }

            COMFunAPI.getInstance().Delayms(1);
            recDataSize++;
        }
        return false;
    }

    /**
     * 检验下载校验值
     * @return
     */
    public boolean CheckDownloadCRC(){
        int tempCyc;
        byte TempV;
        boolean result = false;
        for (tempCyc = 1;tempCyc <= 9;tempCyc++) {
            WUNPack[tempCyc] = 0;
        }

        WUNPack[0] = CMD_CHECK_DOWNLOAD_CRC;

        byte[] downloadCRC = pack.getDownloadCRC();

        if(downloadCRC != null){
            for(tempCyc = 1;tempCyc <= 2;tempCyc++){
                WUNPack[tempCyc] = downloadCRC[downloadCRC.length - tempCyc];
            }
        }

        PackCRC();

        COMFunAPI.getInstance().COMOutCh(getPort(),WUNPack,12);
        //本地调试没有校验，直接返回
        if(!pack.isDebugMode()) {
            for (int i = 0; i < 3000; i++) {
                int len = COMFunAPI.getInstance().COMInSize(getPort());
                if (len > 0) {
                    TempV = COMFunAPI.getInstance().COMInByte(getPort());
                    byte ResultData = CheckCOMCHK(TempV, WUNPack[9]);
                    Log.e("CheckDownloadCRC", HexUtil.toHexString(TempV));
                    if (TempV == CMD_CHECK_DOWNLOAD_CRC_TRUE) {
                        result = true;
                        return result;
                    }/* else if(ResultData == (byte)0x0E){
                    ReportUntil.writeDataToReport(mContext,mContext.getString(R.string.large_File));
                    return result;
                }*/ else {
                        ReportUntil.writeDataToReport(mContext, HexUtil.toHexString(ResultData));
                        return result;
                    }
                }
                COMFunAPI.getInstance().Delayms(1);
            }
        }
        else{
            return true;
        }

        return false;
    }

    /**
     * 检查能用硬件序列号
     * @return
     */
    public boolean CheckUNSN() {
        boolean result = false;
        byte TempV;

        DeviceBean deviceBean = Config.getInstance(mContext).getBondDevice();
        if(!Config.getInstance(mContext).isSigned() && Config.getInstance(mContext).getBondDevice() == null){
            return result;
        }

        byte[] snBuf = deviceBean.getDeviceSN().getBytes();

        for (int i = 1; i <= snBuf.length; i++) {
            WUNPack[i] = snBuf[i - 1];    //序列号入包
        }
        WUNPack[0] = CMD_CHECK_SN;       //检测序列号命令字
        //UNPack[9] = COMCHKM;   //GCM就要那样,也许下位机程序搞错,就讲究那样写
        WUNPack[9] = (byte) 0x00;
        pack.setCOMCHK(WUNPack[9]);
        //SendPack(pack);
        PackCRC();
        SendPack(pack);
       // COMFunAPI.getInstance().COMOutCh(getPort(),WUNPack,12);

        COMFunAPI.getInstance().COMDTRSet(getPort(), true);         //DTR 状态
        for(int i = 0;i < 3000;i++){
            int len = COMFunAPI.getInstance().COMInSize(getPort());
            if (len > 0) {
                TempV = COMFunAPI.getInstance().COMInByte(getPort());
                byte ResultData = CheckCOMCHK(TempV, WUNPack[9]);
                Log.e("CheckUNSN", HexUtil.toHexString(ResultData));
                if (ResultData == CHK_BACK_CMD_SUCCESS) {
                    result = true;
                    return result;
                } else {
                    ReportUntil.writeDataToReport(mContext, HexUtil.toHexString(ResultData));
                    return result;
                }
            }
            COMFunAPI.getInstance().Delayms(1);
        }

        return result;
    }


    /**
     * 准备写入
     * @param progType
     * @param progArea
     * @return
     */
    public boolean ReadyProg(int _type, byte progType, byte progArea) {
        boolean result = false;
        WUNPack[0] = CMD_READY_PRO;
        long startAddress;

        if(_type == UPDATE_TYPE_APP){
            startAddress = START_ADDR_APPLICATION;
        }
        else if(_type == UPDATE_TYPE_FW){
            startAddress = START_ADDR_SYSTEM;
        }
        else {
            return result;
        }

        WUNPack[1] = (byte) startAddress;
        WUNPack[2] = (byte) (startAddress >> 8);
        WUNPack[3] = (byte) (startAddress >> 16);
        WUNPack[4] = (byte) (startAddress >> 24);
        WUNPack[6] = progType;
        WUNPack[8] = (byte) pack.getPackSize();
        WUNPack[9] = progArea;

        /**
         * 本地调试模式，下载的程序不需要解密
         */
        if(pack.isDebugMode()){
            //WUNPack[9] = BaseCMD.CMD_WRITE_TYPE_DEBUG;
        }

        PackCRC();
        COMFunAPI.getInstance().COMOutCh(getPort(), WUNPack, 12);
        int recTime = 1000;

        for (int tempCyc = 0; tempCyc < recTime; tempCyc++) {
            int len = COMFunAPI.getInstance().COMInSize(getPort());
            if (len > 0) {
                byte TempV = COMFunAPI.getInstance().COMInByte(getPort());
                byte ResultData = CheckCOMCHK(TempV, pack.getCOMCHK());

                Log.e("ReadyPro","result-" + HexUtil.toHexString(ResultData));
                if (ResultData == CHK_BACK_CMD_SUCCESS) {
                    return true;
                }
            }
            COMFunAPI.getInstance().Delayms(1);
        }
        return result;
    }

    /**
     * 写入数据
     * @param _type
     * @param file
     * @param buffer
     * @return
     */
    public boolean WriteDataFun(int _type,File file,byte[] buffer) {
        int RCycA, OverTime;
        byte RStr;

        boolean result = false;
        int pageIndex = 0;
        OverTime = 4000;

        WUNPack[9] = 0;

        long pageCount = pack.getCtrlSize() / (pack.getPackSize() + 1);
        //Log.e("WriteDataFun", "comchk: "+pack.getCOMCHK());
        if (pack.getCOMCHK() == 7){
            WUNPack[0] = CMD_DOWNLOAD_SHORT;
            for (pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                if(!pack.isDownloading()){
                    Log.e("downloadCancel","cancel");
                    COMFunAPI.getInstance().Delayms(1);
                    //BootToUserMode();
                    return result;
                }
                byte[] filePack = PackFile(_type,file,buffer, pageIndex);
                for (int i = 0; i < (pack.getPackSize() + 1); i++) {
                    WUNPack[i + 2] = filePack[i];            //缓冲数据送入数据包
                }
                if (!SendLongPack(pack)) {      //发送包发送失败
                    pack.setStateRe((byte) 26);//通讯错误
                    ReportUntil.writeDataToReport(mContext,mContext.getString(R.string.communite_Error));
                    return result;
                }
                int progress = (int) (((float) (pageIndex + 1) / pageCount) * 100);
                DiagnosisAPI.getInstance().refreshDownloadProgress(progress);
            }
        }else {
            WUNPack[0] = CMD_DOWNLOAD_WRITE;                     //动态字节数写入命令 09
            WUNPack[1] = (byte) pack.getPackSize();
            for (pageIndex = 0; pageIndex < pageCount; pageIndex++) {
                if (!pack.isDownloading()){
                    //Log.e("downloadCancel","cancel");
                    COMFunAPI.getInstance().Delayms(1);
                    //BootToUserMode();
                    return false;
                }
                int progress = (int) (((float) (pageIndex + 1) / pageCount) * 100);
                DiagnosisAPI.getInstance().refreshDownloadProgress(progress);

                Log.e("packIndex",pageIndex + "");
                byte[] filePack = PackFile(_type,file,buffer, pageIndex);

                if(filePack == null){
                    ReportUntil.writeDataToReport(mContext,mContext.getString(R.string.readData_Error));
                    return false;
                }

                for (int i = 0; i < (pack.getPackSize() + 1); i++) {
                    WUNPack[i + 2] = filePack[i];  //缓冲数据送入数据包
                }

                COMFunAPI.getInstance().COMBufClt(getPort(),COMFunAPI.FLUSH_RX);

                getPort().purgeHwBuffers(true,false);
                if (!SendLongPack(pack)) {    //发送包发送失败
                    pack.setStateRe((byte) 26);//通讯错误
                    //Log.e("WriteDataFun", "WriteDataFun: sendlongpack");
                    ReportUntil.writeDataToReport(mContext,mContext.getString(R.string.communite_Error));
                    return result;
                }
                RCycA = 0;
                for (int i = 0; i < 2000; i++) {
                    if (!pack.isDownloading()){
                        Log.e("WriteDataFun", "WriteDataFun: Cancel");
                        //BootToUserMode();
                        return false;
                    }
                    int len = COMFunAPI.getInstance().COMInSize(getPort());

                    if (len > 0) {
                        if (len == 1) {
                            RStr = COMFunAPI.getInstance().COMInByte(getPort());
                            //Log.e("WriteData", "receive_" + HexUtil.toHexString(CheckCOMCHK(RStr, pack.getCOMCHK())));
                            if (CheckCOMCHK(RStr, pack.getCOMCHK()) == CHK_BACK_WTITE_SUCCESS) {
                                RCycA = 0;
                                break;
                            }
                            else if(CheckCOMCHK(RStr,pack.getCOMCHK()) == CMD_DOWNLOAD_BACK_LARGE){
                                Log.e("WriteDataFun", "WriteDataFun: Large file");
                                ReportUntil.writeDataToReport(mContext,mContext.getString(R.string.large_File));
                                return result;
                            }
                            else {
                                pack.setStateRe((byte) 17);
                                Log.e("WriteDataFun", "WriteDataFun: setStateRe-17");
                                ReportUntil.writeDataToReport(mContext,mContext.getString(R.string.communite_Error) + " WriteDataFun: setStateRe-17");
                                return result;
                            }
                        } else {
                            pack.setStateRe((byte) 26);
                            Log.e("WriteDataFun", "WriteDataFun: sendlongpack-26---" + len);
                            ReportUntil.writeDataToReport(mContext,mContext.getString(R.string.communite_Error) + " WriteDataFun: sendlongpack-26");
                            return result;
                        }
                    } else {
                        RCycA++;
                    }
                    COMFunAPI.getInstance().Delayms(1);
                }

                if (RCycA > OverTime) {
                    pack.setStateRe((byte) 25);
                    Log.e("WriteDataFun", "WriteDataFun: sendlongpack-25");
                    ReportUntil.writeDataToReport(mContext,mContext.getString(R.string.communite_Error));
                    return result;
                }

            }
        }

        return true;
    }


    /**
     * 运行函数
     * @param funcIndex
     * @param isTwice
     * @return
     */
    public boolean RUNFun(int funcIndex,boolean isTwice) {
        boolean result = false;

        if(!COMFunAPI.getInstance().COMPortOpen(getPort(),pack.getCOMBT())){
            return result;
        }

        WUNPack[0] = CMD_RUN_FUNC;
        WUNPack[1] = (byte)funcIndex;

        for (int i = 2;i < 10;i++){
            WUNPack[i] = 0;
        }

        PackCRC();

        getPort().purgeHwBuffers(true,false);
        SendPack(pack);

        for(int i = 0;i < 3000;i++){
            int len = COMFunAPI.getInstance().COMInSize(getPort());
            if(len >= 1){
                byte TempV = COMFunAPI.getInstance().COMInByte(getPort());

                byte ResultData = CheckCOMCHK(TempV, pack.getCOMCHK());
                Log.e("resultData", HexUtil.toHexString(ResultData));
                if(ResultData == BaseCMD.CHK_BACK_CMD_SUCCESS){
                    result = true;
                    COMFunAPI.getInstance().COMPortClose(getPort());
                    return result;
                }
                else if(ResultData == BaseCMD.CHK_BACK_COM_ERROR){
                    /**
                     * 失败后再次运行
                     */
                    if(!isTwice){
                        ExitFunc();

                        COMFunAPI.getInstance().Delayms(400);
                        return RUNFun(funcIndex,true);
                    }
                    else {
                        COMFunAPI.getInstance().COMPortClose(getPort());
                        return result;
                    }
                }
            }
            COMFunAPI.getInstance().Delayms(1);
        }

        COMFunAPI.getInstance().COMPortClose(getPort());
       // COMFunAPI.getInstance().Delayms(5);
        return  result;
    }


    /**
     * 强制退出运行
     */
    public void ExitFunc(){
        for(int i = 1;i <= 9;i++){
            WUNPack[i] = 0;
        }

        WUNPack[0] = CMD_EXIT_FUNC;
        PackCRC();

        SendPack(pack);
    }


    /**
     * 程序入包
     * @param _type
     * @param file
     * @param buffer
     * @param index
     * @return
     */
    private byte[] PackFile(int _type,File file,byte[] buffer, int index) {
        byte[] pack = new byte[this.pack.getPackSize() + 1];
        if(_type == UPDATE_TYPE_APP) {
            try {
                FileInputStream fis = new FileInputStream(file);

                int length = 0;

                fis.skip(index * (this.pack.getPackSize() + 1));

                if ((length = fis.read(pack)) != -1) {
                    if (length < (this.pack.getPackSize() + 1)) {
                        for (int i = this.pack.getPackSize(); i > length; i--) {
                            pack[i] = (byte) 0xFF;
                        }
                    }
                    return pack;
                }
                fis.close();

                return null;

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        else if(_type == UPDATE_TYPE_FW){
            int start = index * (this.pack.getPackSize() + 1);

            if(start >= buffer.length){
                return  null;
            }
            for(int i = 0;i < this.pack.getPackSize() + 1;i++){
                if(start + i >= buffer.length){
                    pack[i] = (byte)0xFF;
                }
                else{
                    pack[i] = buffer[start + i];
                }

            }
            return pack;
        }

        return null;
    }

    public void destroyDownload() {
        pack.setDownloading(false);
    }


}