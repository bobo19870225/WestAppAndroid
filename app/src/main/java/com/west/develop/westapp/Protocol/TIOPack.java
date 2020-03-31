package com.west.develop.westapp.Protocol;

/**
 * Created by Develop12 on 2017/10/17.
 */

public class TIOPack {
    private boolean pirate;             //防盗版; 为了不兼容以前所有版本,5.36版本以后增加这个参数,
    private byte[] dataPack;
    private int ctrlSize;               //要输入输出的数据大小
    private int ctrlAddrS;             //控制的起始地址
    private int ctrlAddrE;             //控制的结束地址
    private byte stateRe;              //状态返回
    private boolean stopComm;           //停止通讯
    private byte COMCHK;                //D9加密随机数
    //private UsbSerialPort comPort;               //通讯端口号
    private int packSize;
    private int COMBT = 500000;                  //比特率
    private boolean isDownloading = false;
    private String fileName; //记录文件路径
    private byte[] downloadCRC;
    private boolean isDebugMode = false;

    public boolean isPirate() {
        return pirate;
    }

    public void setPirate(boolean pirate) {
        this.pirate = pirate;
    }

    public byte[] getDataPack() {
        return dataPack;
    }

    public void setDataPack(byte[] dataPack) {
        this.dataPack = dataPack;
    }

    public int getCtrlSize() {
        return ctrlSize;
    }

    public void setCtrlSize(int ctrlSize) {
        this.ctrlSize = ctrlSize;
    }

    public int getCtrlAddrS() {
        return ctrlAddrS;
    }

    public void setCtrlAddrS(int ctrlAddrS) {
        this.ctrlAddrS = ctrlAddrS;
    }

    public int getCtrlAddrE() {
        return ctrlAddrE;
    }

    public void setCtrlAddrE(int ctrlAddrE) {
        this.ctrlAddrE = ctrlAddrE;
    }

    public byte getStateRe() {
        return stateRe;
    }

    public void setStateRe(byte stateRe) {
        this.stateRe = stateRe;
    }


    public boolean isStopComm() {
        return stopComm;
    }

    public void setStopComm(boolean stopComm) {
        this.stopComm = stopComm;
    }

    public byte getCOMCHK() {
        return COMCHK;
    }

    public void setCOMCHK(byte COMCHK) {
        this.COMCHK = COMCHK;
    }

   /* public UsbSerialPort getComPort() {
        return comPort;
    }

    public void setComPort(UsbSerialPort comPort) {
        this.comPort = comPort;
    }
*/
    public int getCOMBT() {
        return COMBT;
    }

    public void setCOMBT(int COMBT) {
        this.COMBT = COMBT;
    }

    public int getPackSize() {
        return packSize;
    }

    public void setPackSize(int packSize) {
        this.packSize = packSize;
    }

    public boolean isDownloading() {
        return isDownloading;
    }

    public void setDownloading(boolean downloading) {
        isDownloading = downloading;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public byte[] getDownloadCRC() {
        return downloadCRC;
    }

    public void setDownloadCRC(byte[] downloadCRC) {
        this.downloadCRC = downloadCRC;
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public void setDebugMode(boolean debugMode) {
        isDebugMode = debugMode;
    }

}
