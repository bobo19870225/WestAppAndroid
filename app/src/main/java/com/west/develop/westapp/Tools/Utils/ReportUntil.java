package com.west.develop.westapp.Tools.Utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Download.Threads.PostReportTask;
import com.west.develop.westapp.Protocol.Drivers.UpDriver;
import com.west.develop.westapp.Tools.constant.URLConstant;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Develop12 on 2017/11/17.
 */

public class ReportUntil {

    public static String REPORT_FILENAME = "FILENAME"; //报告名称
    public static String REPORT_UNSN = "UNSN";         //序列号
    public static String REPORT_HARDTYPE = "HARDTYPE";  //硬件类型
    public static String REPORT_FUNCTION = "FUNCTION";   //诊断功能
    public static String REPORT_FAULT = "FUALT";        //故障
    public static final String TAG = ReportUntil.class.getSimpleName();        //故障
    private static Handler mHandler = new Handler();

    /**
     * 向报告文件中写入数据
     *
     * @param context
     * @param data
     */
    public static void writeDataToReport(Context context, String data) {
        String path = FileUtil.getProgramReport(context);
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        data = data + "\r\n";
        try {
            String fileName = UpDriver.getInstance(context).getPack().getFileName();
            File destFile = new File(path + fileName);
            FileOutputStream fout = new FileOutputStream(destFile, true);
            byte[] bytes = data.getBytes();
            fout.write(bytes);
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 读取文件中的数据
     *
     * @param context
     * @param reportName
     * @return
     */
    public static String readReports(Context context, String reportName) {
        String path = FileUtil.getProgramReport(context);

        if (!reportName.toLowerCase().endsWith(".txt")) {
            reportName = reportName + ".txt";
        }
        File reportFile = new File(path, reportName);

        if (reportFile.exists()) {
            return FileUtil.readIniData(reportFile);
        }

        return null;
    }

    /**
     * 删除报告文件
     *
     * @param context
     * @param reportName
     */
    public static void deleteReport(Context context, String reportName) {
        String path = FileUtil.getProgramReport(context);
        if (!reportName.toLowerCase().endsWith(".txt")) {
            reportName = reportName + ".txt";
        }
        File reportFile = new File(path, reportName);

        if (reportFile.exists()) {
            reportFile.delete();
        }

    }

    /**
     * 记录上传完成
     *
     * @param context
     * @param reportName
     */
    public static void postedReport(Context context, String reportName) {
        String path = FileUtil.getProgramReport(context);
        if (reportName.toLowerCase().endsWith(".txt")) {
            reportName = reportName.substring(0, reportName.length() - 4);
        }

        /**
         * 记录上传成功后，将文件名更改为：xxxxxxxxxxxxxxxxxx_2.txt
         */
        if (reportName.endsWith("_1")) {
            File reportFile = new File(path, reportName + ".txt");
            reportName = reportName.substring(0, reportName.length() - 2);

            File destFile = new File(path, reportName + "_2.txt");
            reportFile.renameTo(destFile);
        } else {
            File reportFile = new File(path, reportName + ".txt");
            File destFile = new File(path, reportName + "_2.txt");
            reportFile.renameTo(destFile);
        }
    }

    /**
     * 获取文件夹下的诊断记录
     *
     * @param context
     * @return
     */
    public static File[] getReports(Context context) {
        String path = FileUtil.getProgramReport(context);
        File file = new File(path);
        if (file.exists()) {
            File[] files = file.listFiles();
            return files;
        }
        return null;

    }

    /**
     * 上传诊断记录
     */
    public static void postReport(final Context context, final String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String time = fileName.substring(0, 19);
                    String program = fileName.substring(20);
                    if (program.endsWith(".txt")) {
                        program = program.substring(0, program.length() - 4);
                    }
                    String deviceSN = Config.getInstance(context).getBondDevice().getDeviceSN();
                    final String content = readReports(context, fileName);
                    program = program.replace("$", "/");
                    if (program.endsWith("_1")) {
                        program = program.substring(0, program.length() - 2);
                    }

                    Map<String, String> mParams = new HashMap<String, String>();
                    mParams.put("deviceSN", deviceSN);
                    mParams.put("time", time);
                    mParams.put("program", program);
                    mParams.put("content", content);
                    mParams.put("prev", (!Config.getInstance(context).isConfigured()) + "");
                    final String postName = time + "_" + program;
                    PostReportTask task = new PostReportTask();
                    task.setOnFinishListener(new PostReportTask.OnRequestFinishListener() {
                        @Override
                        public void onFinish(int code, String responseBody) {
                            Log.e("response", responseBody);

                            if (code == HttpURLConnection.HTTP_OK) {
                                /*
                                 * 网络连接成功
                                 */
                                try {
                                    JSONObject json = new JSONObject(responseBody);
                                    /*
                                     * 上传成功
                                     */
                                    if (json.getInt("code") == 0) {
                                        postedReport(context, fileName);
                                    }
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }
                            }

                        }
                    });
                    task.setParams(URLConstant.urlReportPost, mParams);
                    task.execute(0);
                } catch (Exception ex) {
                    Log.e(TAG, ex.toString());
                }
            }
        }).start();
    }

    /**
     * 启动时自动上传 需要上传的诊断记录
     */
    public static void autoPostReport(final Context context) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                File reportPath = new File(FileUtil.getProgramReport(context));
                if (!reportPath.exists()) {
                    return;
                }
                File[] fileList = reportPath.listFiles();
                if (fileList != null) {
                    for (File file : fileList) {
                        String fileName = file.getName();
                        if (fileName.toLowerCase().endsWith("_1.txt")) {
                            postReport(context, fileName);
                        }
                    }
                }
            }
        }).start();
    }
}
