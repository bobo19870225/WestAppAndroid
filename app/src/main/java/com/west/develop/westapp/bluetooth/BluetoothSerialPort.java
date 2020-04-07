package com.west.develop.westapp.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import com.west.develop.westapp.Common.BaseSerialPort;
import com.west.develop.westapp.bluetooth.Threads.ConnectedThread;
import com.west.develop.westapp.usb.HexDump;

import java.util.UUID;

/**
 * Created by Develop0 on 2017/12/25.
 */

public class BluetoothSerialPort extends BaseSerialPort {
    private static final String TAG = BluetoothSerialPort.class.getSimpleName();

    public static final int DEFAULT_READ_BUFFER_SIZE = 4 * 1024;
    public static final int DEFAULT_WRITE_BUFFER_SIZE = 4 * 1024;

    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    // Unique UUID for this application
    public static final UUID MY_UUID_SECURE =
            UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");
    public static final UUID MY_UUID_INSECURE =
            UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    private BluetoothDevice mDevice;

    private ConnectedThread mConnectedThread;

    private int mState = STATE_NONE;

    private ConnectListener mConnectListener;

    private BluetoothSocket mmSocket;


    private Handler mHandler = new Handler();

    public BluetoothSerialPort(BluetoothDevice device) {
        super();
        mDevice = device;
    }

    public BluetoothDevice getDevice() {
        return mDevice;
    }


    public void setState(int state) {
        if (state == STATE_CONNECTED || state == STATE_CONNECTING || state == STATE_LISTEN || state == STATE_NONE) {
            mState = state;
        }
    }

    public int getState() {
        return mState;
    }


    /**
     * 连接成功
     */
    public void connectSuccess() {
        Log.i(TAG, "connectSuccess");
        setState(STATE_CONNECTED);
        if (mConnectListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mConnectListener.onSuccess(BluetoothSerialPort.this);
                }
            });

        }
    }


    /**
     * 连接失败
     */
    public void connectionFailed() {
        Log.i(TAG, "connectFailed");
        setState(STATE_NONE);
        if (mConnectListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mConnectListener.onFailed(BluetoothSerialPort.this);
                }
            });
        }
    }


    /**
     * 断开连接
     */
    public void connectionLost() {
        Log.i(TAG, "connectLost");
        setState(STATE_NONE);

        close();
        if (mConnectListener != null) {
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    mConnectListener.onLose(BluetoothSerialPort.this);
                }
            });
        }
    }


    /**
     * 打开（连接）
     */
    public void open(ConnectListener listener) {
        mConnectListener = listener;

        if (isOpened()) {
            return;
        }
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        setState(STATE_CONNECTING);
        try {
            BluetoothSocket socket = null;
            boolean isSuccess = false;


            socket = mDevice.createRfcommSocketToServiceRecord(BluetoothAllUuid.SerialPort.getUuid());
            //socket = mDevice.createRfcommSocketToServiceRecord(MY_UUID_SECURE);

            boolean isOpened = socket.isConnected();
            try {
                socket.connect();
                mmSocket = socket;
                isSuccess = true;
            } catch (Exception ex) {
                ex.printStackTrace();
                Log.e("bluetooth", ex.toString());
            }

            if (mmSocket != null && isSuccess) {
                connected(mmSocket);
                return;
            }
            connectionFailed();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public ConnectListener getConnectListener() {
        return mConnectListener;
    }


    @Override
    public void close() {

/*
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }*/
    }

    public void destroy() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
    }


    /**
     * 连接成功后建立通信
     * @param socket
     */
    public synchronized void connected(BluetoothSocket socket) {
        Log.i(TAG, "connectSuccess");

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        mConnectedThread = new ConnectedThread(socket, this);
        mConnectedThread.start();

    }


    @Override
    public synchronized int write(byte[] data, int timeoutMillis) {
        Log.e("write", HexDump.dumpHexString(data));
        if (mConnectedThread != null) {
            mConnectedThread.write(data);
            return data.length;
        }

        return 0;
    }

    @Override
    public boolean writeByte(byte b) {
        byte[] buffer = new byte[1];
        buffer[0] = b;

        write(buffer, 0);
        return false;
    }

    @Override
    public boolean isOpened() {
        return mmSocket != null && mmSocket.isConnected();
        //mmSocket.isConnected();
        // return mConnectedThread != null;
    }


    @Override
    public boolean getCD() {
        return false;
    }

    @Override
    public boolean getCTS() {
        return false;
    }

    @Override
    public boolean getDSR() {
        return false;
    }

    @Override
    public boolean getDTR() {
        return false;
    }

    @Override
    public void setDTR(boolean value) {

    }

    @Override
    public boolean getRI() {
        return false;
    }

    @Override
    public boolean getRTS() {
        return false;
    }

    @Override
    public void setRTS(boolean value) {

    }

    @Override
    public boolean purgeHwBuffers(boolean flushRX, boolean flushTX) {
        if (flushRX) {
            mReadIndex = 0;
            mReceiveIndex = 0;
        }
        if (flushTX) {
            mWriteBuffer = new byte[DEFAULT_WRITE_BUFFER_SIZE];
        }

        return flushRX || flushTX;
    }

    @Override
    public void setCommBreak() {

    }

    @Override
    public void clrCommBreak() {

    }

    public interface ConnectListener {
        void onSuccess(BluetoothSerialPort port);
        void onFailed(BluetoothSerialPort port);
        void onLose(BluetoothSerialPort port);
    }

}
