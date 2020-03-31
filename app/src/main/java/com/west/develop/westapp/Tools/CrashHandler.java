package com.west.develop.westapp.Tools;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;

/**
 * Created by Oerl on 2016/1/7.
 * 在Application中统一捕获异常，保存到文件中下次再打开时上传
 */
public class CrashHandler  implements Thread.UncaughtExceptionHandler{

    /** 系统默认的UncaughtException处理类 */
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    /** CrashHandler实例 */
    private static CrashHandler INSTANCE;
    /** 程序的Context对象 */
    private Context mContext;


    public static CrashHandler getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new CrashHandler();
        }
        return INSTANCE;
    }

    /**
     * 初始化,注册Context对象,
     *
     * @param ctx
     */
    public void init(Context ctx) {
        mContext = ctx;

        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当UncaughtException发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (!handleException(ex) && mDefaultHandler != null) {
            //如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);
        } else {  //如果自己处理了异常，则不会弹出错误对话框，则需要手动退出app
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {

            }
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(10);
        }
    }

    /**
     * 自定义错误处理,收集错误信息
     * @return
     * true代表处理该异常，不再向上抛异常，
     * false代表不处理该异常(可以将该log信息存储起来)然后交给上层(这里就到了系统的异常处理)去处理，
     * 简单来说就是true不会弹出那个错误提示框，false就会弹出
     */
    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();
                    String errorMSG = ex.getMessage() + "\nErrorMSG:null";
                    storageInFile(errorMSG);
                    Looper.loop();
                }
            }).start();

            return false;
        }
        final StackTraceElement[] stack = ex.getStackTrace();
        //使用Toast来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    String errorMSG = ex.getMessage() + "\nErrorMSG:";

                    for (int i = 0; i < stack.length; i++) {
                        errorMSG += "\n" + stack[i].toString();
                    }
                    errorMSG = errorMSG.replaceAll("[\\{]","[");
                    errorMSG = errorMSG.replace("}","]");
                    errorMSG = errorMSG.replace("'","\"");
                    //postReport(errorMSG);
                    storageInFile(errorMSG);
                } catch (Exception e) {
                    ex.printStackTrace();
                }
                Looper.loop();
            }
        }.start();
        return false;
    }


    /**
     * 利用递归方法创建文件夹
     * @param dir
     */
    public void makeDir(File dir){
        if(dir.exists()){
            return;
        }
        else{
            makeDir(dir.getParentFile());
            dir.mkdir();
        }
    }

    /**
     * 将异常信息上传到服务器
     * @param msg
     */
    private void postReport(String msg) {

        /*try {
            URL url = new URL(URLConstant.PostErrorLogUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            //设置参数
            StringBuffer param = new StringBuffer();
            param.append("msg=" + msg + "&");
            param.append("model=" + Build.MODEL + "&");
            param.append("sdk=" + Build.VERSION.SDK_INT + " ");

            byte[] bypes = param.toString().getBytes();
            conn.connect();

            conn.getOutputStream().write(bypes);
            conn.getOutputStream().flush();

            conn.getOutputStream().close();

            int code = conn.getResponseCode();
            switch (conn.getResponseCode()) {
                case 200:
                    InputStream is = conn.getInputStream();

                    BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));

                    String line = "";
                    StringBuilder result = new StringBuilder();
                    while ((line = in.readLine()) != null)
                        result.append(line);
                    is.close();
                    break;
            }
        }
        catch (Exception ex){
            ex.printStackTrace();
        }*/
    }


    /**
     * 将异常信息保存到文件系统中
     * 保存路径     包名/Log
     * @param errorMSG
     */
    private void storageInFile(String errorMSG){
        try {
             String fileName = "crash-" + new SimpleDateFormat("MMdd-HHmmss").format(System.currentTimeMillis()) + ".txt";
                String packageName = mContext.getApplicationContext().getPackageName();
                String filePath = Environment.getExternalStorageDirectory() + "/" + packageName + "/Log/";

                File pathDir = new File(filePath);

                if(!pathDir.exists()) {
                    makeDir(pathDir);
                }
                File file = new File(filePath, fileName);


            FileOutputStream fos = new FileOutputStream(file,true);

            fos.write(errorMSG.getBytes());
           fos.flush();
                    fos.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
