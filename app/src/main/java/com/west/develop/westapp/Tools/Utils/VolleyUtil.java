package com.west.develop.westapp.Tools.Utils;

import android.app.Dialog;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.west.develop.westapp.Communicate.Service.UsbService;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.BondDialog;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.MDBHelper;
import com.west.develop.westapp.Tools.constant.RequestCodeConstant;
import com.west.develop.westapp.usb.UsbSerialPort;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by Develop14 on 2017/6/7.
 */
public class VolleyUtil {

    static RequestQueue queue;
    static TipDialog dialogBond;
    static TipDialog dialogUseFinish;
    public static void jsonRequest(String url, final Context context, final IVolleyCallback callback) {

        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Log.e("volley",jsonObject.toString());
                        callback.getResponse(jsonObject);
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        callback.onErrorResponse(volleyError);

                        if(volleyError.networkResponse != null){
                            int code = volleyError.networkResponse.statusCode;
                            Log.e("code",code + "");
                            //当序列号或者targetId不正确时
                            if (code == RequestCodeConstant.CODE_HTTP_DISABLE){
                                initDevice(context);
                            }
                            if (code == RequestCodeConstant.CODE_HTTP_FORBIDDENT){
                                //使用次数用完
                                useNumFinish(context);
                            }
                        }

                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                                        5*1000,//默认超时时间，应设置一个稍微大点儿的
                                       DefaultRetryPolicy.DEFAULT_MAX_RETRIES,//默认最大尝试次数
                                       DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }


    public static void jsonPostRequest(String url, Context context, final IVolleyCallback callback) {

        if (queue == null) {
            queue = Volley.newRequestQueue(context);
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        callback.getResponse(jsonObject);
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        callback.onErrorResponse(volleyError);
                    }
                }
        );
        request.setRetryPolicy(new DefaultRetryPolicy(
                5*1000,//默认超时时间，应设置一个稍微大点儿的
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,//默认最大尝试次数
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        queue.add(request);
    }


    public interface IVolleyCallback {
        abstract void getResponse(JSONObject jsonObject);
        abstract void onErrorResponse(VolleyError error);
    }


    //将设备解绑和注销
    public static void initDevice(final Context context) {
        Config.getInstance(context).setBondDevice(null);
       // Config.getInstance(context).setTryCount(0);

        //删除文件
        String root = Environment.getExternalStorageDirectory().getPath() + "/";
        String packageName = context.getPackageName();
        String path = root + packageName;
        FileUtil.deleteApp(path,context);
        FileUtil.deleteAppVideo(context);
        //删除数据库
        MDBHelper.getInstance(context).deleteAppDB();
        //提示绑定设备
        if (Config.getInstance(context).getBondDevice() == null) {
            if (dialogBond != null && dialogBond.isShowing()) {
                //dialogBond.dismiss();
                return;
            }
            dialogBond = new TipDialog.Builder(context)
                    .setTitle(context.getResources().getString(R.string.device_bond_title))
                    .setMessage(context.getResources().getString(R.string.device_bond_message))
                    .setNegativeClickListener(context.getResources().getString(R.string.bond_cancle), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            dialogInterface.dismiss();
                            System.exit(0);

                        }
                    })
                    .setPositiveClickListener(context.getResources().getString(R.string.bond_sure), new TipDialog.OnClickListener() {
                        @Override
                        public void onClick(Dialog dialogInterface, int index, String label) {
                            dialogInterface.dismiss();
                            BondDialog.newInstance(context)
                                    .setNegativeClickListener(context.getResources().getString(R.string.cancel), new BondDialog.OnClickListener() {
                                        @Override
                                        public void onClick(Dialog dialog) {
                                            dialog.dismiss();
                                            System.exit(0);
                                        }
                                    })
                                    .show();
                            if (UsbService.getInstance().getSerialPorts() != null) {
                                final Handler handler = new Handler();
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        while (BondDialog.getInstance() != null && !BondDialog.getInstance().waitSign()) {
                                            //Log.e("signUsb",UsbService.getInstance().getSerialPorts().size() + "");
                                            if (UsbService.getInstance().getSerialPorts() == null || UsbService.getInstance().getSerialPorts().size() <= 0) {
                                                return;
                                            }
                                            handler.post(new Runnable() {
                                                @Override
                                                public void run() {
                                                    List<UsbSerialPort> mUsbPorts = UsbService.getInstance().getSerialPorts();
                                                    if (mUsbPorts != null && mUsbPorts.size() > 0) {
                                                        for (int i = 0; i < mUsbPorts.size(); i++) {
                                                            BondDialog.getInstance().bondWithPort(mUsbPorts.get(i));
                                                        }
                                                    }
                                                }
                                            });
                                        }
                                        handler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                for (int i = 0; i < UsbService.getInstance().getSerialPorts().size(); i++) {
                                                    BondDialog.getInstance().bondWithPort(UsbService.getInstance().getSerialPorts().get(i));
                                                }
                                            }
                                        });
                                    }
                                }).start();

                            }
                        }
                    })
                    .requestSystemAlert(true)
                    .build();
            dialogBond.show();
        }
    }

    //使用次数用完，提示
    private static void useNumFinish(Context context) {
        if (dialogUseFinish != null && dialogUseFinish.isShowing()){
            //dialogUseFinish.dismiss();
            return;
        }
        dialogUseFinish = new TipDialog.Builder(context)
                .setTitle(context.getResources().getString(R.string.tip_title))
                .setMessage(context.getResources().getString(R.string.tip_message_useCount))
                .setPositiveClickListener(context.getResources().getString(R.string.tip_kown), new TipDialog.OnClickListener() {
                    @Override
                    public void onClick(Dialog dialogInterface, int index, String label) {
                        dialogInterface.dismiss();
                    }
                })
                .requestSystemAlert(true)
                .build();
        dialogUseFinish.show();
    }


}
