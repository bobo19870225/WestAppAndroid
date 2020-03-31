package com.west.develop.westapp.UI.Activity.APPUpdate;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NoConnectionError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.LoadDialog;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.UI.base.BaseActivity;
import com.west.develop.westapp.Bean.AppBean.DocumentVersion;
import com.west.develop.westapp.Download.AppUpdateManager;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.constant.URLConstant;
import com.west.develop.westapp.Tools.Utils.VolleyUtil;
import com.west.develop.westapp.Tools.Utils.VolleyUtil.IVolleyCallback;
import com.west.develop.westapp.Tools.Utils.WifiUtil;
import com.west.develop.westapp.R;

import org.json.JSONObject;


public class AppUpdateActivity extends BaseActivity {
    public static final int CODE_NEW_VER_DOWNLOAD_FINISH_ACTIVITY =  4;


    private TextView back_tv;
    private TextView title;
    private TextView update;
    private TextView versionTV;

    @Override
    protected View getContentView() {
        return this.getLayoutInflater().inflate(R.layout.activity_app_update,null);
    }

    @Override
    protected void initView() {
        back_tv = (TextView) findViewById(R.id.car_back);
        title = (TextView) findViewById(R.id.car_title);
        update = (TextView) findViewById(R.id.inspect_update);
        versionTV = (TextView)findViewById(R.id.version);
    }

    @Override
    protected void initData() {
        title.setText(R.string.inspect_upgrade);


        try {
            String verName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;

            int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;

            String versionStr = getString(R.string.about_veision) + "V" + verName + "." + versionCode;
            versionTV.setText(versionStr);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private LoadDialog dialog;
    @Override
    protected void initListener() {
        back_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Config.getInstance(AppUpdateActivity.this).getBondDevice() == null){
                    return;
                }

                if ((WifiUtil.isSupportNetwork(AppUpdateActivity.this))){
                    dialog = new LoadDialog.Builder(AppUpdateActivity.this)
                            .setTitle(getResources().getString(R.string.toast_new_ver))
                            .build();
                    dialog.show();

                    getAPKVersion(URLConstant.urlAPK  +
                            Config.getInstance(AppUpdateActivity.this).getBondDevice().getDeviceSN(),
                            AppUpdateActivity.this,
                            callback);
                }else {
                    Toast.makeText(AppUpdateActivity.this, getResources().getString(R.string.toast_inspect_netconn), Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void getAPKVersion(String url, Context context, IVolleyCallback callback) {
        VolleyUtil.jsonRequest(url, context, callback);
    }

    //将本地的版本与服务上的版本进行比较，看是否需要更新
    DocumentVersion version = new DocumentVersion();
    IVolleyCallback callback = new IVolleyCallback() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            Log.e("Volley APK",jsonObject.toString());
            if ("".equals(jsonObject) || jsonObject == null) {
                return;
            }
            try{
                if (jsonObject.getInt("code") == 0) {
                    JSONObject arr = jsonObject.getJSONObject("data");

                    Gson gson = new Gson();

                    version = gson.fromJson(arr.getJSONObject("version").toString(),DocumentVersion.class);
                   /* version.setMain(arr.getString("Main"));
                    version.setCode(arr.getString("Code"));
                    version.setSlave(arr.getString("Slave"));
*/
                    PackageManager packageManager = getPackageManager();
                    PackageInfo info = packageManager.getPackageInfo(getPackageName(), 0);
                    String currentVersion = info.versionName;
                    int currentVersionCode = info.versionCode;
                    DocumentVersion oldVersion = new DocumentVersion();
                    String str[] = currentVersion.split("\\.");
                    if (str.length == 2) {
                        oldVersion.setMain(str[0]);
                        oldVersion.setSlave(str[1]);
                    }
                    oldVersion.setCode(currentVersionCode + "");

                    int oldMain = Integer.parseInt(oldVersion.getMain());
                    int newMain = Integer.parseInt(version.getMain());

                    String oldCodeStr = oldVersion.getCode();
                    String newCodeStr = version.getCode();
                    String oldSlaveStr = oldVersion.getSlave();
                    String newSlaveStr = version.getSlave();

                    if (oldCodeStr.length() > oldCodeStr.length()) {
                        for (int i = 0; i < oldCodeStr.length() - newCodeStr.length(); i++) {
                            newCodeStr = newCodeStr + "0";
                        }
                    } else {
                        for (int i = 0; i < newCodeStr.length() - oldCodeStr.length(); i++) {
                            oldCodeStr = oldCodeStr + "0";
                        }
                    }

                    if (oldSlaveStr.length() > newSlaveStr.length()) {
                        for (int i = 0; i < oldSlaveStr.length() - newSlaveStr.length(); i++) {
                            newSlaveStr = newSlaveStr + "0";
                        }
                    } else {
                        for (int i = 0; i < newSlaveStr.length() - oldSlaveStr.length(); i++) {
                            oldSlaveStr = oldSlaveStr + "0";
                        }
                    }

                    int oldCode = Integer.parseInt(oldCodeStr);
                    int newCode = Integer.parseInt(newCodeStr);
                    int oldSlave = Integer.parseInt(oldSlaveStr);
                    int newSlave = Integer.parseInt(newSlaveStr);
                    if (oldMain < newMain) {
                        dialog.dismiss();
                        displayTip();

                    } else if (oldMain == newMain && (oldSlave < newSlave)) {
                        dialog.dismiss();
                        displayTip();

                    } else if (oldCode < newCode) {
                        dialog.dismiss();
                        displayTip();
                    } else if (oldMain == newMain && oldSlave == newSlave && oldCode == newCode) {
                        dialog.dismiss();
                        Toast.makeText(AppUpdateActivity.this, getResources().getString(R.string.toast_current_ver), Toast.LENGTH_LONG).show();

                    }
                }
            }catch (Exception e){
                e.printStackTrace();
                dialog.dismiss();
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            dialog.dismiss();
            if (error instanceof TimeoutError) {
                Log.e("questVersionError", "TimeOut");
                Toast.makeText(AppUpdateActivity.this,getResources().getString(R.string.toast_netconn_moreTime),Toast.LENGTH_SHORT).show();
            }
            if (error instanceof NoConnectionError) {
                Log.e("questVersionError", "NoConnectionError");
                Toast.makeText(AppUpdateActivity.this, getResources().getString(R.string.toast_inspect_netconn), Toast.LENGTH_SHORT).show();
            }
            if (error.networkResponse != null) {
                String errorStr = new String(error.networkResponse.data);
                Log.e("questVersionError", errorStr);
            }
        }
    };

    private void displayTip() {
        new TipDialog.Builder(AppUpdateActivity.this)
                .setTitle(getResources().getString(R.string.tip_title))
                .setMessage(getResources().getString(R.string.tip_message_upgrade) + "V" + version.getMain() + "." + version.getSlave() + "." + version.getCode())
                .setPositiveClickListener(getResources().getString(R.string.tip_now_upgrade), new TipDialog.OnClickListener() {
                    @Override
                    public void onClick(Dialog dialogInterface, int index, String label) {
                        FileUtil.removeAppFile(AppUpdateActivity.this); //如果有老版本存在就删除
                        AppUpdateManager.getInstance(AppUpdateActivity.this).downLoadApk();
                        dialogInterface.dismiss();
                    }
                })
                .setNegativeClickListener(getResources().getString(R.string.tip_later), new TipDialog.OnClickListener() {
                    @Override
                    public void onClick(Dialog dialogInterface, int index, String label) {
                        dialogInterface.dismiss();

                    }
                })
                .requestSystemAlert(true)
                .build()
                .show();
    }

}
