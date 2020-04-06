package com.west.develop.westapp.Common;

import android.util.Log;

import com.west.develop.westapp.usb.HexDump;
import com.west.develop.westapp.usb.UsbSerialPort;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Develop0 on 2017/12/26.
 */

public abstract class BaseSerialPort {
    private static final int DEFAULT_READ_BUFFER_SIZE = 4 * 1024;
    private static final int DEFAULT_WRITE_BUFFER_SIZE = 4 * 1024;

    private final Object mReadBufferLock = new Object();
    private final Object mWriteBufferLock = new Object();

    protected int mReadIndex = 0;
    protected int mReceiveIndex = 0;


    private ReceiveListener mReceiveListener;

    private ReentrantLock mReceiveLock = new ReentrantLock();


    /**
     * Internal read buffer.  Guarded by {@link #mReadBufferLock}.
     */
    private byte[] mReadBuffer;

    /**
     * Internal write buffer.  Guarded by {@link #mWriteBufferLock}.
     */
    protected byte[] mWriteBuffer;


    private volatile boolean isBreak = false;


    public BaseSerialPort() {
        mReadBuffer = new byte[DEFAULT_READ_BUFFER_SIZE];
        mWriteBuffer = new byte[DEFAULT_WRITE_BUFFER_SIZE];
    }


    /**
     * 接收到数据
     *
     * @param data 数据
     */
    public void onReceive(byte[] data) {
        mReceiveLock.lock();
        Log.e("InData", HexDump.dumpHexString(data));
        try {
            for (byte datum : data) {
                if (mReceiveIndex >= mReadIndex) {
                    if (mReceiveIndex >= DEFAULT_READ_BUFFER_SIZE) {
                        mReceiveIndex = 0;
                    }
                    mReadBuffer[mReceiveIndex] = datum;
                    mReceiveIndex++;
                } else {
                    mReadBuffer[mReceiveIndex] = datum;
                    mReceiveIndex++;
                    if (mReceiveIndex == mReadIndex) {
                        mReadIndex++;
                        if (mReadIndex >= DEFAULT_READ_BUFFER_SIZE) {
                            mReadIndex = 0;
                        }
                    }
                }

                if (mReceiveIndex >= DEFAULT_READ_BUFFER_SIZE) {
                    mReceiveIndex = 0;
                }
            }
            if (mReceiveListener != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //try {
                        mReceiveListener.onReceive(getReadBufferSize());
                       /* } catch (IOException ex) {
                            ex.printStackTrace();
                        }*/
                    }
                }).start();
            } else {
                Log.e("ReceiveListener", "null");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            mReceiveLock.unlock();
        }
    }


    /**
     * 获取接收缓冲区 已接收大小
     *
     * @return 已接收大小
     */
    public int getReadBufferSize() {
        int size;

        if (mReadIndex <= mReceiveIndex) {
            size = mReceiveIndex - mReadIndex;
        } else {
            size = mReceiveIndex + (DEFAULT_READ_BUFFER_SIZE - mReadIndex);
        }

        return size;
    }


    /**
     * 从接收缓冲区读取一个字节
     *
     * @return 缓冲区读取一个字节
     */
    public byte getReadByte() {
        if (getReadBufferSize() <= 0) {
            return -1;
        }
        //Log.e("ReadBuffer",HexDump.toHexString(mReadBuffer));
        byte readByte = mReadBuffer[mReadIndex];
        mReadIndex++;
        if (mReadIndex >= DEFAULT_READ_BUFFER_SIZE) {
            mReadIndex = 0;
        }
        return readByte;

    }


    /**
     * 从接收缓冲区读取 指定长度的数据
     *
     * @param len 指定长度
     * @return 数据
     */
    public byte[] getReadBytes(int len) {
        if (len < 0) {
            return new byte[0];
            //throw new IOException("Invalid lenght");
        }
        byte[] buffer = new byte[len];

        if (getReadBufferSize() <= 0) {
            return buffer;
        }

        for (int i = 0; i < len; i++) {
            if (mReadIndex >= DEFAULT_READ_BUFFER_SIZE) {
                mReadIndex = 0;
            }
            if (mReadIndex > mReceiveIndex) {
                buffer[i] = mReadBuffer[mReadIndex];
                mReadIndex++;
                if (mReadIndex >= DEFAULT_READ_BUFFER_SIZE) {
                    mReadIndex = 0;
                }
            } else if (mReadIndex < mReceiveIndex) {
                buffer[i] = mReadBuffer[mReadIndex];
                mReadIndex++;
                if (mReadIndex >= DEFAULT_READ_BUFFER_SIZE) {
                    mReadIndex = 0;
                }
            } else {
                break;
            }
        }

        return buffer;
    }


    /**
     * 接收监听
     *
     * @param listener 接收监听
     */
    public void setRevListener(ReceiveListener listener) {
        if (mReceiveListener != listener) {
            mReceiveListener = listener;

            if (getReadBufferSize() > 0) {
                mReceiveListener.onReceive(getReadBufferSize());
            }
        }
    }


    public void stopRevListener() {
        mReceiveListener = null;
    }

    /**
     * Closes the port.
     *
     * @throws IOException on error closing the port.
     */
    public abstract void close();

    public abstract boolean isOpened();

    public abstract boolean writeByte(byte b);

    /**
     * Writes as many bytes as possible from the source buffer.
     *
     * @param src           the source byte buffer
     * @param timeoutMillis the timeout for writing
     * @return the actual number of bytes written
     * @throws IOException if an error occurred during writing
     */
    public abstract int write(final byte[] src, final int timeoutMillis);


    /**
     * Gets the CD (Carrier Detect) bit from the underlying UART.
     *
     * @return the current state, or {@code false} if not supported.
     * @throws IOException if an error occurred during reading
     */
    public abstract boolean getCD();

    /**
     * Gets the CTS (Clear To Send) bit from the underlying UART.
     *
     * @return the current state, or {@code false} if not supported.
     * @throws IOException if an error occurred during reading
     */
    public abstract boolean getCTS();

    /**
     * Gets the DSR (Data Set Ready) bit from the underlying UART.
     *
     * @return the current state, or {@code false} if not supported.
     * @throws IOException if an error occurred during reading
     */
    public abstract boolean getDSR();

    /**
     * Gets the DTR (Data Terminal Ready) bit from the underlying UART.
     *
     * @return the current state, or {@code false} if not supported.
     * @throws IOException if an error occurred during reading
     */
    public abstract boolean getDTR();

    /**
     * Sets the DTR (Data Terminal Ready) bit on the underlying UART, if
     * supported.
     *
     * @param value the value to set
     * @throws IOException if an error occurred during writing
     */
    public abstract void setDTR(boolean value);

    /**
     * Gets the RI (Ring Indicator) bit from the underlying UART.
     *
     * @return the current state, or {@code false} if not supported.
     * @throws IOException if an error occurred during reading
     */
    public abstract boolean getRI();

    /**
     * Gets the RTS (Request To Send) bit from the underlying UART.
     *
     * @return the current state, or {@code false} if not supported.
     * @throws IOException if an error occurred during reading
     */
    public abstract boolean getRTS();

    /**
     * Sets the RTS (Request To Send) bit on the underlying UART, if
     * supported.
     *
     * @param value the value to set
     * @throws IOException if an error occurred during writing
     */
    public abstract void setRTS(boolean value);

    /**
     * Flush non-transmitted output data and / or non-read input data
     *
     * @param flushRX {@code true} to flush non-transmitted output data
     * @param flushTX {@code true} to flush non-read input data
     * @return {@code true} if the operation was successful, or
     * {@code false} if the operation is not supported by the driver or device
     * @throws IOException if an error occurred during flush
     */
    public abstract boolean purgeHwBuffers(boolean flushRX, boolean flushTX) /*throws IOException*/;


    /**
     * 设置接收 和 读取 挂起
     */
    public void setCommBreak() {
        isBreak = true;
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (mReadBufferLock) {
                    while (isBreak) {

                    }
                }
                synchronized (mWriteBufferLock) {
                    while (isBreak) {

                    }
                }
            }
        }).start();
    }


    /**
     * 清除挂起
     */
    public void clrCommBreak() {
        isBreak = false;
    }

}
