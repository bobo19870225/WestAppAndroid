package com.west.develop.westapp.Download.Threads;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by Develop0 on 2018/3/14.
 */

public class PostReportTask extends AsyncTask<Integer, Integer, String> {

    private String mUrl;
    private Map<String,String> mParams = new HashMap<>();


    public PostReportTask() {
        super();
    }

    public void setParams(String url,Map<String,String> params){
        this.mUrl= url;
        this.mParams = params;
    }

    /**
     * 这里的Integer参数对应AsyncTask中的第一个参数
     * 这里的String返回值对应AsyncTask的第三个参数
     * 该方法并不运行在UI线程当中，主要用于异步操作，所有在该方法中不能对UI当中的空间进行设置和修改
     * 但是可以调用publishProgress方法触发onProgressUpdate对UI进行操作
     */
    @Override
    protected String doInBackground(Integer... params) {

        try {

            if(mUrl == null){
                return null;
            }
            URL url = new URL(mUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            StringBuffer param = new StringBuffer();
            if(mParams != null){
                Set<String> mKeys = mParams.keySet();
                Iterator<String> keyIn = mKeys.iterator();
                int index = 0;
                while (keyIn.hasNext()){
                    String key = keyIn.next();
                    String value = mParams.get(key);

                    if(index != 0){
                        param.append("&");
                    }
                    param.append(key + "=" + URLEncoder.encode(value,"utf-8"));
                    index++;
                }
            }

            String paramStr = param.toString();
            Log.e("paramStr",paramStr);
            byte[] bytes = paramStr.getBytes();
            conn.connect();
            int bufferLength = 1024;

            for (int i = 0; i < bytes.length; i += bufferLength) {
                int progress = (int) ((i / (float) bytes.length) * 100);
                publishProgress(progress);
                if (bytes.length - i >= bufferLength) {
                    conn.getOutputStream().write(bytes, i, bufferLength);
                } else {
                    conn.getOutputStream().write(bytes, i, bytes.length - i);
                }
            }
            publishProgress(100);

            conn.getOutputStream().flush();

            conn.getOutputStream().close(); // flush and close

            int code = conn.getResponseCode();
            InputStream is = conn.getInputStream();

            BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"));

            String line = "";
            StringBuilder result = new StringBuilder();
            while ((line = in.readLine()) != null)
                result.append(line);

            if (mFinishListener != null) {
                if (code == 200) {
                    mFinishListener.onFinish(code, result.toString());
                } else {
                    mFinishListener.onFinish(code, result.toString());
                }
            }

            is.close();
        }
        catch (Exception ex){

        }

        return null;
    }


    /**
     * 这里的String参数对应AsyncTask中的第三个参数（也就是接收doInBackground的返回值）
     * 在doInBackground方法执行结束之后在运行，并且运行在UI线程当中 可以对UI空间进行设置
     */
    @Override
    protected void onPostExecute(String result) {
        //Log.e("result",result);
    }


    //该方法运行在UI线程当中,并且运行在UI线程当中 可以对UI空间进行设置
    @Override
    protected void onPreExecute() {
    }


    /**
     * 这里的Intege参数对应AsyncTask中的第二个参数
     * 在doInBackground方法当中，，每次调用publishProgress方法都会触发onProgressUpdate执行
     * onProgressUpdate是在UI线程中执行，所有可以对UI空间进行操作
     */
    @Override
    protected void onProgressUpdate(Integer... values) {
    }


    public interface OnRequestFinishListener{
        public void onFinish(int code, String responseBody);
    }

    private OnRequestFinishListener mFinishListener;
    public void setOnFinishListener(OnRequestFinishListener listener){
        mFinishListener = listener;
    }
}
