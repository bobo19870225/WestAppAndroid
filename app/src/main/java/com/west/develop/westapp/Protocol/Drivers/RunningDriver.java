package com.west.develop.westapp.Protocol.Drivers;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

import com.west.develop.westapp.Common.ReceiveListener;
import com.west.develop.westapp.Communicate.COMFunAPI;
import com.west.develop.westapp.Dialog.ConnectStatus;
import com.west.develop.westapp.Protocol.DISP_IOPack;
import com.west.develop.westapp.Protocol.TIOPack;
import com.west.develop.westapp.Tools.Utils.HexUtil;
import com.west.develop.westapp.UI.Activity.Diagnosis.RunActivity;

import java.util.concurrent.locks.ReentrantLock;

//import com.west.develop.westapp.Protocol.UPFile_IOPack;

/**
 * Created by Develop0 on 2017/11/14.
 */
public class RunningDriver extends BaseDriver {

    private static Context mContext;
    private static RunningDriver instance;

    private ReentrantLock mCOMInLock = new ReentrantLock();

    private boolean COUNT_TIMEOUT = false;
    private long TIMEOUT_STAMP = 0;

    public synchronized static void init(TIOPack pack) {
        if (instance == null) {
            instance = new RunningDriver();
        }
        instance.pack = pack;
    }

    public synchronized static void initContext(Context context) {
        mContext = context;
    }

    public synchronized static RunningDriver getInstance() {
        return instance;
    }

    private RunningDriver() {
    }

    /**
     * 设置超时计数
     * @param count
     */
    public void countTimeout(boolean count){
        if(!count){
            TIMEOUT_STAMP = 0;
        }
        else{
            TIMEOUT_STAMP = System.currentTimeMillis();
        }
        COUNT_TIMEOUT = count;
    }

    /**
     * 获取超时时间
     * @return
     */
    public int getTimeout(){
        if(!COUNT_TIMEOUT || TIMEOUT_STAMP == 0){
            return  0;
        }

        long stamp = System.currentTimeMillis();
        long timeout = stamp - TIMEOUT_STAMP;
        return (int)timeout;
    }

    public void reCountTimeout(){
        if(COUNT_TIMEOUT){
            TIMEOUT_STAMP = System.currentTimeMillis();
        }
        else{
            TIMEOUT_STAMP = 0;
        }
    }

    /**
     * 接收监听
     */
    public void startListen(final RunActivity.ListenCallback callback) {

        if (getPort() != null) {
            if (!COMFunAPI.getInstance().COMPortOpen(getPort(), pack.getCOMBT())) {
                //打开端口失败
                initPort(null);
                startListen(callback);
            }
            else {
                //开始监听 接收
                getPort().setRevListener(mReceiveListener);
                if(callback != null) {
                    callback.onSuccess();
                }
            }
        }
        else{
            callback.onStart();
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    /**
                     * 当接口为空时，等待接口连接
                     */
                    while (getPort() == null){
                        try{
                            if(ConnectStatus.getInstance(mContext).getUSBPort() != null){
                                initPort(ConnectStatus.getInstance(mContext).getUSBPort());
                                if (COMFunAPI.getInstance().COMPortOpen(getPort(), pack.getCOMBT())) {
                                    break;
                                }
                            }
                            else if(ConnectStatus.getInstance(mContext).getBTPort() != null){
                                initPort(ConnectStatus.getInstance(mContext).getBTPort());
                                if (COMFunAPI.getInstance().COMPortOpen(getPort(), pack.getCOMBT())) {
                                    break;
                                }
                            }
                            Thread.sleep(1);

                        }
                        catch (InterruptedException ex){
                            ex.printStackTrace();
                        }
                    }
                    //Log.e("RunningDriver","port YES");
                    //开始接收
                    getPort().setRevListener(mReceiveListener);


                    if(callback != null) {
                        Looper.prepare();
                        callback.onSuccess();
                        Looper.loop();
                    }

                }
            });
            synchronized (thread) {
                thread.start();
            }
        }
    }

    /**
     * 停止接收
     */
    public void stopListen() {
        if (pack != null && getPort() != null) {
            getPort().stopRevListener();
        }
    }


    boolean PAUSE_LISTEN = false;
    /**
     * 接收监听
     */
    ReceiveListener mReceiveListener = new ReceiveListener() {
        @Override
        public void onReceive(int bufferSize) {
            if(mContext instanceof RunActivity){
                ((RunActivity)mContext).redoCount();
            }
            Log.e("PAUSE_LISTEN",PAUSE_LISTEN + "");
            if(PAUSE_LISTEN){
                return;
            }

            try {
                mCOMInLock.lock();
                int len = COMFunAPI.getInstance().COMInSize(getPort());
                byte TempV = 0x00;
                if(len > 0){
                    TempV = COMFunAPI.getInstance().COMInByte(getPort());
                }
                while (len > 0) {
                    len = COMFunAPI.getInstance().COMInSize(getPort());


                    while (TempV == CMD_DISPLAY) {
                        //接收到显示信息
                        DISPLAY_IN(TempV);

                        len = COMFunAPI.getInstance().COMInSize(getPort());
                        if (len <= 0) {
                            break;
                        }
                        TempV = COMFunAPI.getInstance().COMInByte(getPort());
                    }

                    if(TempV == CMD_FINISH_FUNC){
                        //函数执行完成
                        FINISH_FUNC(TempV);

                        len = COMFunAPI.getInstance().COMInSize(getPort());
                        if (len <= 0) {
                            break;
                        }
                        TempV = COMFunAPI.getInstance().COMInByte(getPort());

                    }

                    //重置超时
                    else if(TempV == CMD_CLEAR_TIMEOUT){
                        reCountTimeout();
                        len = COMFunAPI.getInstance().COMInSize(getPort());
                        if (len <= 0) {
                            break;
                        }
                        TempV = COMFunAPI.getInstance().COMInByte(getPort());
                    }

                    //启动接收文件
                    else if(TempV == CMD_UPFILE_NEW){
                        byte result = FILE_UPLOAD_IN(TempV);
                        if(result != (byte)0x00){
                            onReturn(result);
                        }
                        len = COMFunAPI.getInstance().COMInSize(getPort());
                        if (len <= 0) {
                            break;
                        }
                        TempV = COMFunAPI.getInstance().COMInByte(getPort());
                    }

                    //接收文件数据
                    else if(TempV == CMD_UPFILE_DATA){
                        reCountTimeout();
                        byte result = FILE_UPLOAD_IN(TempV);
                        if(result != (byte)0x00){
                            onReturn(result);
                        }
                        len = COMFunAPI.getInstance().COMInSize(getPort());
                        if (len <= 0) {
                            break;
                        }
                        TempV = COMFunAPI.getInstance().COMInByte(getPort());
                    }

                    //接收数据完成
                    else if(TempV == CMD_UPFILE_FINISH){
                        byte result = FILE_UPLOAD_IN(TempV);
                        if(result != (byte)0x00){
                            onReturn(result);
                        }
                        len = COMFunAPI.getInstance().COMInSize(getPort());
                        if (len <= 0) {
                            break;
                        }
                        TempV = COMFunAPI.getInstance().COMInByte(getPort());
                    }

                    else if(TempV == CMD_LOAD_BACKUP_NEW){
                        byte result = LOAD_BACKUP_IN(TempV);
                        if(result != (byte)0x00){
                            onReturn(result);
                        }
                        len = COMFunAPI.getInstance().COMInSize(getPort());
                        if (len <= 0) {
                            break;
                        }
                        TempV = COMFunAPI.getInstance().COMInByte(getPort());
                    }

                    else if(TempV == CMD_LOAD_BACKUP_START){
                        Log.e("UP_DATA","true");
                        byte result = LOAD_BACKUP_IN(TempV);

                        if(result != (byte)0x00){
                            onReturn(result);
                        }
                        len = COMFunAPI.getInstance().COMInSize(getPort());
                        if (len <= 0) {
                            break;
                        }
                        TempV = COMFunAPI.getInstance().COMInByte(getPort());
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                mCOMInLock.unlock();
            }

        }
    };


    /**
     * 等待完整包
     * @param length
     * @return
     */
    private boolean waitFullPack(final int length){
        boolean received = false;

        //未收到完整的包
        for (int i = 0; i < 20000; i++) {

            COMFunAPI.getInstance().Delayms(1);
            int len = COMFunAPI.getInstance().COMInSize(getPort());
            if (len >= length) {
                received = true;
                break;
            }
        }

        return received;
    }

    private void DISPLAY_IN(byte TempV){
        //接收到显示信息
        boolean received = false;
        int len = COMFunAPI.getInstance().COMInSize(getPort());
        if (len < 11) {
            //未收到完整的包
            received =  waitFullPack(11);
        } else {
            received = true;
        }
        if (received) {
            //已收到完整包
            COMFunAPI.getInstance().COMInCh(getPort(), RUNPack, 11);
            if (CheckPackCRC(TempV)) {
                //校验成功
                DisplayInData();
            } else {
                Log.e("CheckPackCRC-Error", "31 " + HexUtil.toHexString(RUNPack,0,11));
            }
        }
        else{
            //丢弃不足一包的数据
            COMFunAPI.getInstance().COMInCh(getPort(), new byte[len], len);
        }
    }

    private void FINISH_FUNC(byte TempV){
        boolean received = false;
        int len = COMFunAPI.getInstance().COMInSize(getPort());
        if (len < 11) {
            // 未收到完整包
            received = waitFullPack(11);
        } else {
            received = true;
        }

        if (received) {
            // 已收到完整包
            COMFunAPI.getInstance().COMInCh(getPort(), RUNPack, 11);

            if (CheckPackCRC(TempV)) {
                // 校验成功
                if(mContext instanceof RunActivity){
                    ((RunActivity)mContext).finish();
                }
            } else {
                Log.e("CheckPackCRC-Error", "31 " + HexUtil.toHexString(RUNPack,0,11));
            }
        }
        else{
            //丢弃不足一包的数据
            COMFunAPI.getInstance().COMInCh(getPort(), new byte[len], len);
        }
    }


    /**
     * 保存上传文件
     * @param TempV
     * @return
     */
    private byte FILE_UPLOAD_IN(byte TempV) {
        //接收到显示信息
        boolean received = false;
        int len = COMFunAPI.getInstance().COMInSize(getPort());

        if (TempV == CMD_UPFILE_NEW || TempV == CMD_UPFILE_FINISH) {
            if(len < 11) {
                received = waitFullPack(11);
            }
            else{
                received = true;
            }
        }
        if (TempV == CMD_UPFILE_DATA) {
            if(len < 259){
                received = waitFullPack(259);
            }
            else{
                received = true;
            }

        }
       /* if (len < 11) {
            //未收到完整的包
            if (TempV == CMD_UPFILE_NEW || TempV == CMD_UPFILE_FINISH) {
                received = waitFullPack(11);
            }
            if (TempV == CMD_UPFILE_DATA) {
                received = waitFullPack(259);
            }
        } else {
            received = true;
        }*/
        if (received) {
            //已收到完整包
            if (TempV == CMD_UPFILE_NEW) {
                COMFunAPI.getInstance().COMInCh(getPort(), RUNPack, 11);
                if (CheckPackCRC(TempV)) {
                    int length = 0;
                    for (int i = 0; i < 4; i++) {
                        int times = (int) Math.pow(256,3 - i);
                        int value = (RUNPack[i] & 0xFF);
                        length = times * value + length;
                    }
                    Log.e("length",length + "");
                    if(mContext instanceof RunActivity){
                        ((RunActivity)mContext).UPFILE_NEW(length);
                    }
                    return 0x00;
                }
                else{
                    return CHK_BACK_CRC_ERROR;
                }
            }
            else if (TempV == CMD_UPFILE_DATA) {

                byte lenByt = COMFunAPI.getInstance().COMInByte(getPort());
                int packLen = (lenByt & 0xFF) + 1;
                COMFunAPI.getInstance().COMInCh(getPort(), RUNPack,packLen + 2);
                if (CheckLongPackCRC(TempV, lenByt,packLen)) {
                    byte[] data = new byte[260];
                    data[0] = TempV;
                    data[1] = lenByt;
                    for (int i = 2; i < 260; i++) {
                        data[i] = RUNPack[i - 2];
                    }

                    byte result = CHK_BACK_CMD_UNSURPPOT;
                    if(mContext instanceof RunActivity){
                        if(((RunActivity)mContext).UPFILE_INDATA(data)){
                            result = CHK_BACK_WTITE_SUCCESS;
                        }
                    }
                    return result;
                }
                else{
                    return CHK_BACK_CRC_ERROR;
                }
            }
            /*else if (TempV == CMD_UPFILE_FINISH) {
                COMFunAPI.getInstance().COMInCh(getPort(), RUNPack, 11);
                if(CheckPackCRC(TempV)){
                    if(mContext instanceof RunActivity){
                        ((RunActivity)mContext).UPFILE_FINISH();
                    }
                    return CHK_BACK_CMD_SUCCESS;
                }
                else{
                    return CHK_BACK_CRC_ERROR;
                }
            }*/
        }
        else{
            return CHK_BACK_TIMEOUT;
        }

        return CHK_BACK_CMD_UNSURPPOT;

    }


    private byte LOAD_BACKUP_IN(byte TempV){
        //接收到显示信息
        boolean received = false;
        int len = COMFunAPI.getInstance().COMInSize(getPort());
        if (len < 11) {
            //未收到完整的包
            if (TempV == CMD_LOAD_BACKUP_NEW || TempV == CMD_LOAD_BACKUP_START) {
                received = waitFullPack(11);
            }
        } else {
            received = true;
        }
        if (received) {
            //已收到完整包
            if (TempV == CMD_LOAD_BACKUP_NEW || TempV == CMD_LOAD_BACKUP_START) {
                COMFunAPI.getInstance().COMInCh(getPort(), RUNPack, 11);
                if (CheckPackCRC(TempV)) {
                    if(TempV == CMD_LOAD_BACKUP_NEW) {
                        byte flagByte = RUNPack[8];
                        if (mContext instanceof RunActivity) {
                            ((RunActivity) mContext).LOAD_BACKUP_NEW(flagByte);
                        }
                        return 0x00;
                    }
                    else if(TempV == CMD_LOAD_BACKUP_START){
                        int start = 0,length = 0;
                        for (int i = 0; i < 4; i++) {
                            int times = (int) Math.pow(256, 3 - i);
                            int value = (RUNPack[i] & 0xFF);
                            start = times * value + start;
                        }

                        length = (RUNPack[8] & 0xFF) + 1;
                        if(RUNPack[8] == (byte)0x00){
                            length = 0xFF + 1;
                        }
                        /*for(int i = 0;i < 4;i++){
                            int times = (int) Math.pow(256, i);
                            int value = (RUNPack[7 - i] & 0xFF);
                            length = times * value + length;
                        }*/

                        Log.e("start",start + "");
                        Log.e("length",length + "");
                        if (mContext instanceof RunActivity) {
                            ((RunActivity) mContext).LOAD_BACKUP_START(start,length);
                        }
                        return 0x00;
                    }
                }
                return CHK_BACK_CRC_ERROR;
            }
        }
        else{
            return CHK_BACK_TIMEOUT;
        }

        return CHK_BACK_CMD_UNSURPPOT;

    }

    /**
     * 接收到要显示的数据
     */
    private void DisplayInData() {
        int packIndex = RUNPack[8];
        if (packIndex < 0) {
            packIndex += 256;
        }
        byte[] data = new byte[12];
        data[0] = CMD_DISPLAY;

        for (int i = 0; i < 11; i++) {
            data[i + 1] = RUNPack[i];
        }
        if (mContext instanceof RunActivity) {
            if(DISP_IOPack.getInstance() != null){
                //存在未接收完全的待显示字符
                DISP_IOPack.getInstance().In_EN_CN_ASCII(data, packIndex);
            }
            else{
                //第一包或最后一包
                if((RUNPack[8] == (byte)0x00) || (RUNPack[8] == (byte)0xFF)){
                    //字符显示
                    if (
                            ((byte) (RUNPack[2] & 0xF0) == (byte) 0x10) ||
                            ((byte) (RUNPack[2] & 0xF0) == (byte) 0x20) ||
                            ((byte) (RUNPack[2] & 0xF0) == (byte) 0x30))
                    {
                        byte PAG = RUNPack[0];
                        byte COL = RUNPack[1];
                        byte Disp_FUN = (byte) (RUNPack[2] & 0xF0);
                        byte NotDisp = (byte) (RUNPack[2] & 0x0F);
                        byte StrLen = RUNPack[3];
                        if(RUNPack[8] == (byte) 0x00 || DISP_IOPack.getInstance() == null) {
                           // 创建字符显示接收
                            DISP_IOPack.newInstance(mContext, PAG, COL, Disp_FUN, NotDisp, StrLen);
                        }
                        DISP_IOPack.getInstance().In_EN_CN_ASCII(data, packIndex);
                    }
                    else if (RUNPack[8] == (byte) 0xFF) {
                        // 清屏
                        if ((byte) (RUNPack[2] & 0xF0) == (byte) 0x00) {
                            ((RunActivity) mContext).Clr_Scr();
                        }
                        // 显示进度
                        else if ((byte) (RUNPack[2] & 0xF0) == (byte) 0x50
                                && RUNPack[8] == (byte) 0xFF
                                ) {
                            int progress = RUNPack[3];
                            if (progress < 0) {
                                progress += 0xFF;
                            }
                           // Log.e("PROGRESS", HexUtil.toHexString(data,0,12));
                            ((RunActivity) mContext).PROGRESS(progress);
                        }
                        else{
                            Log.e("UnDeal", HexUtil.toHexString(data,0,12));
                        }
                    }
                }

            }
        }
    }


    /**
     * 诊断函数执行完成
     */
    public void ExitFunc(){
        for(int i = 1;i <= 9;i++){
            WUNPack[i] = 0;
        }

        WUNPack[0] = CMD_EXIT_FUNC;
        PackCRC();

        SendPack(pack);
    }

    public boolean onReturn(byte[] pack){
        boolean result = false;

        return result;
    }

    public boolean onReturn(byte byt){
        boolean result = false;

        WUNPack[0] = RevertCOMCHK(byt,pack.getCOMCHK());

        SendByte();

        return result;
    }

    /**
     * 按钮事件
     * @param keyValue
     * @return
     */
    public boolean onKeyEvent(int keyValue) {
        boolean result = false;
        WUNPack[0] = CMD_KEY_EVENT;
        WUNPack[1] = (byte) keyValue;

        for (int i = 2; i < 10; i++) {
            WUNPack[i] = 0;
        }

        if (!SendPack(pack)) {
            return result;
        }
        result = true;
        return result;
    }


    /**
     * 准备接收上传文件数据
     * @return
     */
    public boolean receiveUPFILE(){
        boolean result = false;


       // SendByte();

        WUNPack[0] = RevertCOMCHK(CHK_BACK_CMD_SUCCESS,pack.getCOMCHK());

        SendByte();
       /* for (int i = 1; i < 10; i++) {
            WUNPack[i] = 0;
        }

        if (!SendPack(pack)) {
            return result;
        }*/
        result = true;

        return result;
    }

    /**
     * 取消备份
     * @return
     */
    public boolean UPFile_Cancel(){
        boolean result = false;

        WUNPack[0] = CMD_UPFILE_CANCEL;

        for (int i = 1; i < 10; i++) {
            WUNPack[i] = 0;
        }

        if (!SendPack(pack)) {
            return result;
        }
        result = true;

        return result;
    }


    public boolean BACKUP_Lengh(long length,byte flag){
        boolean result = false;

        WUNPack[0] = CMD_LOAD_BACKUP_LEN;

        for(int i = 1;i < 10;i++){
            WUNPack[i] = 0x00;
        }

        int index = 1;
        for(int i = 4;i >= 1;i--){
            WUNPack[i] = (byte)(length >> (8 * (4 - i)));
        }

        WUNPack[9] = flag;
      /*  while(length > 0){
            WUNPack[index] = (byte)length;
            length = (length >> 8);
        }
*/
        //暂停接收监听
        //PAUSE_LISTEN = true;
        SendPack(pack);

/*

        for(int i = 0;i < 2000;i++){
            int len = COMFunAPI.getInstance().COMInSize(getPort());
            if(len > 0){
                byte TempV = COMFunAPI.getInstance().COMInByte(getPort());
                byte resultByte = CheckCOMCHK(TempV, pack.getCOMCHK());
                if(resultByte == CHK_BACK_CMD_SUCCESS){
                    if(mContext instanceof RunActivity){
                        ((RunActivity)mContext).LENGH_MATCH(true);
                        result = true;
                        break;
                    }
                }
                else if (resultByte == CHK_BACK_CMD_MISMATCH){
                    if(mContext instanceof RunActivity){
                        ((RunActivity)mContext).LENGH_MATCH(false);
                        break;
                    }
                }
                else{
                    Log.e("TempV",HexUtil.toHexString(TempV));
                }
            }
            COMFunAPI.getInstance().Delayms(1);
        }
        //Log.e("PAUSE_LISTEN","RESET");
        //恢复接收监听
        PAUSE_LISTEN = false;
        int len = COMFunAPI.getInstance().COMInSize(getPort());
        if(len > 0){
            mReceiveListener.onReceive(COMFunAPI.getInstance().COMInSize(getPort()));
        }
*/

        result = true;
        return result;
    }


    public boolean BACKUP_WRITE(byte[] buffer){
        boolean result = false;

        byte[][] packArray = BACKUP_PACK_ARRAY(buffer);
        if(packArray == null){
            return result;
        }


        int failTimes = 0;
        for(int i = 0;i < packArray.length; i++) {
            WUNPack[0] = CMD_LOAD_BACKUP_DATA;
            WUNPack[1] = (byte) 0xFF;

            for (int index = 0; index < packArray[i].length; index++) {
                WUNPack[index + 2] = packArray[i][index];
            }
            SendLongPack(pack);
           /* PAUSE_LISTEN = true;
            if (SendLongPack(pack)) {
                int temp = 0;
                for (; temp < 2000; temp++) {
                    int len = COMFunAPI.getInstance().COMInSize(getPort());
                    if (len > 0) {
                        byte TempV = COMFunAPI.getInstance().COMInByte(getPort());
                        byte resultByte = CheckCOMCHK(TempV, pack.getCOMCHK());
                        if (resultByte == CHK_BACK_WTITE_SUCCESS) {
                            failTimes = 0;
                            break;
                        } else if (resultByte == CHK_BACK_CRC_ERROR) {
                            failTimes = 0;
                            i--;
                            break;
                        }
                    }
                    COMFunAPI.getInstance().Delayms(1);
                }
                if(temp >= 2000){
                    i--;
                }
            }
            failTimes++;

            PAUSE_LISTEN = false;
*/
         /*   //连续多次失败
            if (failTimes >= 2) {
                return result;
            }*/
        }


        return true;
    }


    /**
     * 备份数据包列表
     * @param buffer
     * @return
     */
    public byte[][] BACKUP_PACK_ARRAY(byte[] buffer){
        if(buffer == null){
            return null;
        }

        int length = buffer.length / (256);
        if(buffer.length % (256) != 0){
            length++;
        }

        byte[][] packArray = new byte[length][256];

        for(int index = 0;index < length;index++) {
            for (int i = 0; i < 256; i++) {
                if ((index * 256 + i) >= buffer.length) {
                    packArray[index][i] = (byte) 0xFF;
                } else {
                    packArray[index][i] = buffer[index * 256 + i];
                }

            }
        }

        return packArray;
    }



}
