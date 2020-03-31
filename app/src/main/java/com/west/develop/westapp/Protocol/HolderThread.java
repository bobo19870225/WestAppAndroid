package com.west.develop.westapp.Protocol;

import com.west.develop.westapp.Protocol.Drivers.BaseDriver;

/**
 * Created by Develop0 on 2018/5/22.
 */

public class HolderThread extends Thread {
    private static HolderThread instance;
    boolean run = false;
    private BaseDriver driver;

    public static HolderThread newInstance(BaseDriver driver) {
        if (instance != null) {
            instance.run = false;
            instance.interrupt();
        }

        instance = new HolderThread(driver);
        return instance;
    }

    public static void Stop() {
        if (instance != null) {
            instance.run = false;
            instance.interrupt();
        }
        instance = null;
    }


    private HolderThread(BaseDriver driver) {
        this.driver = driver;
    }


    @Override
    public void run() {
        run = true;

        while (run) {
            try {
                driver.SEND_TIMEOUT();
                Thread.sleep(2000);
            } catch (InterruptedException ex) {

            }
        }
    }
}
