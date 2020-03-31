package com.west.develop.westapp.bluetooth.Threads;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.west.develop.westapp.bluetooth.BluetoothSerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Develop0 on 2017/12/26.
 */
public class ConnectedThread extends Thread {
    private static final String TAG = ConnectedThread.class.getSimpleName();

    public static final int DEFAULT_BUFFER_SIZE = 2 * 1024;

    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;

    private BluetoothSerialPort mPort;

    public ConnectedThread(BluetoothSocket socket,BluetoothSerialPort port) {
       // Log.d(TAG, "create ConnectedThread: " + socketType);
        mPort = port;
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        // Get the BluetoothSocket input and output streams
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
            Log.e(TAG, "temp sockets not created");
        }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
        mPort.connectSuccess();
        //setState(STATE_CONNECTED);
           /* if(mConnectListener != null){
                mConnectListener.onSuccess();
            }*/
    }

    public void run() {
        Log.i(TAG, "BEGIN mConnectedThread");
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        int len;

        // Keep listening to the InputStream while connected
        while (mPort.getState() == BluetoothSerialPort.STATE_CONNECTED) {
            try {
                len = mmInStream.read(buffer);

                if(len > 0) {
                    byte[] readBuff = new byte[len];
                    for(int i = 0;i < len;i++){
                        readBuff[i] = buffer[i];
                    }
                    Log.i("Bluetooth-Receive:", new String(readBuff));

                    mPort.onReceive(readBuff);
                }
                // Send the obtained bytes to the UI Activity
            } catch (IOException e) {
                Log.e(TAG, "disconnected");
                mPort.connectionLost();
                // connectionLost();
                break;
            }

        }
    }

    /**
     * Write to the connected OutStream.
     *
     * @param buffer The bytes to write
     */
    public void write(byte[] buffer) {
        try {
            Log.e("bluetooth-Write", buffer.length + "");
            mmOutStream.write(buffer);
        } catch (IOException e) {
            cancel();
           // Log.e(TAG, "Exception during write", e);
        }
    }

    public void cancel() {
        try {
            mmSocket.close();
            mPort.connectionLost();
        } catch (IOException e) {
            Log.e(TAG, "close() of connect socket failed", e);
        }
    }
}