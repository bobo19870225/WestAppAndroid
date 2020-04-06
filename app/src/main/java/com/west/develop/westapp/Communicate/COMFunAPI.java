package com.west.develop.westapp.Communicate;

import android.os.SystemClock;
import android.util.Log;

import com.west.develop.westapp.Common.BaseSerialPort;
import com.west.develop.westapp.Communicate.Service.BluetoothService;
import com.west.develop.westapp.Communicate.Service.UsbService;
import com.west.develop.westapp.bluetooth.BluetoothSerialPort;
import com.west.develop.westapp.usb.UsbSerialPort;

/**
 * Created by Develop0 on 2017/10/16.
 */

public class COMFunAPI {
    private static COMFunAPI instance;

    public static COMFunAPI getInstance() {
        if (instance == null) {
            instance = new COMFunAPI();
        }
        return instance;
    }

    private COMFunAPI() {
    }


    /**
     * 打开串口
     *
     * @param port
     * @param baud  波特率
     * @return
     */
    public boolean COMPortOpen(BaseSerialPort port, int baud) {
        //boolean result = UsbService.getInstance().COMPortOpen(port,baud);
        if (port instanceof UsbSerialPort) {
            UsbSerialPort usbPort = (UsbSerialPort) port;
            boolean result = false;

            UsbService.getInstance().COMPortOpen(usbPort, baud);
            //usbPort.setDTR(true);
            if (usbPort == null || !usbPort.isOpened()) {
                return result;
            }

            usbPort.setParameters(baud, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);

            usbPort.purgeHwBuffers(true, true);

            return true;
        } else if (port instanceof BluetoothSerialPort) {
            BluetoothSerialPort bluetoothPort = (BluetoothSerialPort) port;

            if (!bluetoothPort.isOpened()) {
                bluetoothPort.open(BluetoothService.getInstance().mConnectListener);
            }
            if (!bluetoothPort.isOpened()) {
                return false;
            }

            return true;
        }

        return false;

    }




    /**
     * 发送一个字节数据函数
     *
     * @param port
     * @param outData
     * @return
     */
    public boolean COMOutByte(BaseSerialPort port, byte outData) {
        boolean result = false;

        if (port == null || !port.isOpened()) {
            return result;
        }

        byte[] buffer = new byte[1];
        buffer[0] = outData;
        int len = port.write(buffer, 0);
        if (len == 1) {
            return true;
        }
        return result;
    }


    /**
     * 接收一个字节
     *
     * @param port
     * @return
     */

    public byte COMInByte(BaseSerialPort port) {
        byte result = -1;

        if (port == null || !port.isOpened()) {
            return result;
        }
        result = port.getReadByte();

        return result;
    }


    /**
     * 关闭串口
     * @param port
     */
    public void COMPortClose(BaseSerialPort port) {
        boolean result = false;
        if (port == null || !port.isOpened()) {
            return;
        }
        //port.close();
    }



    public void COMDTRSet(BaseSerialPort port, boolean enable) {
        if (port == null || !port.isOpened()) {
            return;
        }
        port.setDTR(enable);
    }


    public void COMRTSSet(BaseSerialPort port, boolean enable) {
        if (port == null || !port.isOpened()) {
            return;
        }
        port.setRTS(enable);
    }


    /**
     * 延时
     * @param T 时间
     */
    public void Delayms(int T){
        SystemClock.sleep(T);
        /*try {
            Thread.sleep(T);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }*/
        /*SystemClock.sleep(T);*/

    }


    public void Delayms(long T){
        SystemClock.sleep(T);
        /*try {
            Thread.sleep(T);
        }
        catch (Exception ex){
            ex.printStackTrace();
        }*/
    }


    /**
     * 已接收到的字节长度
     * @param port
     * @return
     */
    public int COMInSize(BaseSerialPort port) {
        int size = 0;
        if (port == null || !port.isOpened()) {
            return size;
        }
        size = port.getReadBufferSize();

        return size;
    }


    /**
     * 发送字符串
     * @param port
     * @param OutStr
     * @return
     */
    public boolean COMOut(BaseSerialPort port, String OutStr) {
        boolean result = false;

        if (port == null || !port.isOpened()) {
            return result;
        }

        if (OutStr == null) {
            return result;
        }

        int len = port.write(OutStr.getBytes(), 0);
        if (len == OutStr.length()) {
            return true;
        }

        return false;
    }


    /**
     * 发送 {@UNPack} 起始的{@size} 长度的数据
     * @param port
     * @param UNPack
     * @param size
     * @return
     */
    public boolean COMOutCh(BaseSerialPort port, byte[] UNPack, int size) {
        boolean result = false;

        if (port == null || !port.isOpened()) {
            Log.e("Write", "Port Closed" + size);
            return false;
        }

        byte[] buf = new byte[size];
        for (int i = 0; i < size; i++) {
            buf[i] = UNPack[i];
        }
        int len = port.write(buf, 0);
        byte[] temp = new byte[12];
        for (int j = 0; j < 12; j++) {
            temp[j] = UNPack[j];
        }
        if (len == size) {
            result = true;
        }

        return result;
    }


    /**
     * 发送{@OutPack} 起始{@DataSize} 长度的数据，每个byte 间隔{@Time} ms
     * @param port
     * @param OutPack
     * @param DataSize
     * @param Time
     * @return
     */
    public boolean COMOutChT(BaseSerialPort port, byte[] OutPack, int DataSize, int Time) {
        boolean result = false;

        if (port == null || !port.isOpened()) {
            return result;
        }
        int size = Math.min(OutPack.length, DataSize);
        for (int i = 0; i < size; i++) {
            port.writeByte(OutPack[i]);

            Delayms(Time);
        }
        return true;
    }


    public int COMIn(BaseSerialPort port, String ReStr) {
        int FdwBytesRead = 0;
        int result = -1;
        int StrCount = 100;


        int len = port.getReadBufferSize();

        if (len > StrCount) {
            ReStr = "";
            return 0;
        }

        byte[] buffer;
        buffer = port.getReadBytes(StrCount);
        FdwBytesRead = StrCount;

        result = FdwBytesRead;
        byte[] strBuff = new byte[FdwBytesRead];


        System.arraycopy(buffer, 0, strBuff, 0, FdwBytesRead);
        ReStr = strBuff.toString();


        return result;
    }


    /**
     * 接收{@size}字节的数据
     * @param port
     * @param UNPack
     * @param size
     * @return
     */
    public boolean COMInCh(BaseSerialPort port, byte[] UNPack, int size) {
        boolean result = false;

        if (port == null || !port.isOpened()) {
            return result;
        }
        byte[] buffer = port.getReadBytes(size);

        for (int i = 0; i < size; i++) {
            if (i < UNPack.length) {
                UNPack[i] = buffer[i];
            }
        }
        return true;
    }


    public static final int FLUSH_TX = 0;
    public static final int FLUSH_RX = 1;
    public static final int FLUSH_BOTH = 2;

    /**
     * 清除缓冲区数据
     * @param port
     * @param flf
     *          {@FLUSH_TX}     清除发送缓冲区
     *          {@FLUSH_RX}     清楚接收缓冲区
     *          {@FLUSH_BOTH}   清除发送和接收缓冲区
     * @return
     */
    public boolean COMBufClt(BaseSerialPort port, int flf) {
        boolean result = false;
        if (port == null || !port.isOpened()) {
            return result;
        }
        switch (flf) {
            case 0:
                port.purgeHwBuffers(true, false);
                break;
            case 1:
                port.purgeHwBuffers(false, true);
                break;
            case 2:
                port.purgeHwBuffers(true, true);
                break;
        }
        return true;
    }


    public boolean COMBreak(BaseSerialPort port, long T) {
        boolean result = false;

        if (port == null || !port.isOpened()) {
            return result;
        }

        port.setCommBreak();
        Delayms(T);
        port.clrCommBreak();
        Delayms(T);

        return false;
    }


    public boolean COMbps(BaseSerialPort port, byte DeviceAddr, byte DataBitCount, int TimeT) {
        boolean result = false;
        boolean Verify;

        byte VerifyCount = 0;

        for (int TempCyc = 1; TempCyc <= DataBitCount; TempCyc++) {
            if ((DeviceAddr & (byte) 0x01) == 1) {
                VerifyCount++;
            }
        }

        if (VerifyCount % 2 == 0) {
            Verify = true;
        } else {
            Verify = false;
        }
        port.setCommBreak();
        Delayms(TimeT);

        for (int TempCyc = 1; TempCyc <= DataBitCount; TempCyc++) {
            if ((DeviceAddr & (byte) 0x01) == 1) {
                port.clrCommBreak();
            } else {
                port.setCommBreak();
            }
            Delayms(TimeT);
            DeviceAddr = (byte) ((DeviceAddr & 0xFF) >> 1);
        }

        if (Verify) {
            port.clrCommBreak();
        } else {
            port.setCommBreak();
            Delayms(TimeT);
        }

        port.clrCommBreak();
        Delayms(TimeT);

        return true;
    }


    public void COMAPIBreak(BaseSerialPort port, int ClrSetBreak) {
        switch (ClrSetBreak) {
            case 0:
                port.clrCommBreak();
                break;
            case 1:
                port.setCommBreak();
                break;
        }

    }


    /**
     * 发送数据
     * @param port
     * @param OutData
     * @return
     */
    public boolean COMOutBytesFun(BaseSerialPort port, byte[] OutData) {
        boolean result = false;

        if (port == null || !port.isOpened()) {
            return result;
        }
        port.purgeHwBuffers(false, true);
        int len = port.write(OutData, 0);

        if (len == OutData.length) {
            result = true;
        }
        return result;
    }


    public boolean COMInBytesFun(BaseSerialPort port, byte[] RevData) {
        boolean result = false;
        if (port == null || !port.isOpened()) {
            return result;
        }

        byte[] buffer = port.getReadBytes(0);
        System.arraycopy(buffer,0,RevData,0,buffer.length);
        port.purgeHwBuffers(true, true);
        result = true;
        return result;
    }


}

