/* Copyright 2011-2013 Google Inc.
 * Copyright 2013 mike wakerly <opensource@hoho.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: https://github.com/mik3y/usb-serial-for-android
 */

package com.west.develop.westapp.usb.subSerial;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.os.SystemClock;
import android.util.Log;

import com.west.develop.westapp.usb.HexDump;
import com.west.develop.westapp.usb.SerialInputOutputManager;
import com.west.develop.westapp.usb.UsbSerialPort;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A base class shared by several driver implementations.
 *
 * @author mike wakerly (opensource@hoho.com)
 */
abstract class CommonUsbSerialPort extends UsbSerialPort {

    public static final int DEFAULT_READ_BUFFER_SIZE = 4 * 1024;
    public static final int DEFAULT_WRITE_BUFFER_SIZE = 4 * 1024;

    protected final UsbDevice mDevice;
    protected final int mPortNumber;

    // non-null when open()
    protected UsbDeviceConnection mConnection = null;

    protected final Object mReadBufferLock = new Object();
    protected final Object mWriteBufferLock = new Object();

    /**
     * Internal read buffer.  Guarded by {@link #mReadBufferLock}.
     */
    protected byte[] mReadBuffer;

    /**
     * Internal write buffer.  Guarded by {@link #mWriteBufferLock}.
     */
    protected byte[] mWriteBuffer;

    private SerialInputOutputManager mSerialIoManager;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    private Thread mListenThread;

    public CommonUsbSerialPort(UsbDevice device, int portNumber) {
        mDevice = device;
        mPortNumber = portNumber;

        mReadBuffer = new byte[DEFAULT_READ_BUFFER_SIZE];
        mWriteBuffer = new byte[DEFAULT_WRITE_BUFFER_SIZE];
    }

    @Override
    public String toString() {
        return String.format("<%s device_name=%s device_id=%s port_number=%s>",
                getClass().getSimpleName(), mDevice.getDeviceName(),
                mDevice.getDeviceId(), mPortNumber);
    }

    /**
     * Returns the currently-bound USB device.
     *
     * @return the device
     */
    public final UsbDevice getDevice() {
        return mDevice;
    }

    @Override
    public int getPortNumber() {
        return mPortNumber;
    }

    /**
     * Returns the device serial number
     *
     * @return serial number
     */
    @Override
    public String getSerial() {
        return mConnection.getSerial();
    }


    @Override
    public boolean open(UsbDeviceConnection connection){
        startReceive();
        SystemClock.sleep(50);
        return false;
    }

    @Override
    public abstract void close();

    @Override
    public abstract int read(final byte[] dest, final int timeoutMillis) throws IOException;

    @Override
    public abstract int write(final byte[] src, final int timeoutMillis);

    @Override
    public abstract boolean setParameters(
            int baudRate, int dataBits, int stopBits, int parity);

    @Override
    public abstract boolean getCD();

    @Override
    public abstract boolean getCTS();

    @Override
    public abstract boolean getDSR();

    @Override
    public abstract boolean getDTR();

    @Override
    public abstract void setDTR(boolean value);

    @Override
    public abstract boolean getRI();

    @Override
    public abstract boolean getRTS();

    @Override
    public abstract void setRTS(boolean value);

    public void startReceive() {
        //stopReceive();
        Log.e("Port", "startReceive");
        if( mListenThread != null && mListenThread.isAlive()){
            return;
        }
        //Log.e("Port", "startReceive");
        mSerialIoManager = new SerialInputOutputManager(this, new SerialInputOutputManager.Listener() {
            @Override
            public void onRevData(byte[] data) {
                onReceive(data);
            }

            @Override
            public void onRunError(Exception e) {
                Log.e("ReceiveError", e.toString());
            }
        });
        //mExecutor.submit(mSerialIoManager);
        mListenThread = new Thread(mSerialIoManager);
        mListenThread.start();

    }

    public void stopReceive() {
        Log.e("Port", "startReceive");
        if(mListenThread != null) {
            mListenThread.interrupt();
            mListenThread = null;
        }
        if (mSerialIoManager != null) {
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    /**
     * 判断端口是否处于打开状态
     *
     * @return
     */
    @Override
    public boolean isOpened() {
        if (mConnection != null) {
            return true;
        }

        return false;
    }

    public boolean isListen(){
        return mListenThread != null && mListenThread.isAlive();
    }

    /**
     * 写入一个字节
     *
     * @param b
     * @return
     */
    @Override
    public boolean writeByte(byte b){
        byte[] buffer = new byte[1];
        buffer[0] = b;

        write(buffer, 0);

        return true;
    }


    @Override
    public boolean purgeHwBuffers(boolean flushReadBuffers, boolean flushWriteBuffers){
        mReadIndex = mReceiveIndex;
        return flushReadBuffers || flushWriteBuffers;
    }



}
