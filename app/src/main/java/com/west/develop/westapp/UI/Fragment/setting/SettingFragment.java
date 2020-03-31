package com.west.develop.westapp.UI.Fragment.setting;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.west.develop.westapp.Application.MyApplication;
import com.west.develop.westapp.Bean.AppBean.DeviceBean;
import com.west.develop.westapp.Bean.AppBean.DocumentVersion;
import com.west.develop.westapp.CallBack.FragmentBackHandler;
import com.west.develop.westapp.Communicate.Service.UsbService;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.LoadDialog;
import com.west.develop.westapp.Dialog.SignDialog;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.Utils.VolleyUtil;
import com.west.develop.westapp.Tools.Utils.VolleyUtil.IVolleyCallback;
import com.west.develop.westapp.Tools.constant.RequestCodeConstant;
import com.west.develop.westapp.Tools.constant.URLConstant;
import com.west.develop.westapp.UI.Activity.APPUpdate.AppUpdateActivity;
import com.west.develop.westapp.UI.Activity.Diagnosis.DiagnosisActivity;
import com.west.develop.westapp.UI.Activity.MainActivity;
import com.west.develop.westapp.UI.Activity.Setting.AboutActivity;
import com.west.develop.westapp.UI.Activity.Setting.HelpActivity;
import com.west.develop.westapp.UI.Activity.Setting.LanguageActivity;
import com.west.develop.westapp.UI.Activity.Setting.PortActivity;
import com.west.develop.westapp.UI.Activity.Setting.PreferenceActivity;
import com.west.develop.westapp.UI.Activity.Setting.StatementActivity;
import com.west.develop.westapp.UI.base.BaseFragment;
import com.west.develop.westapp.usb.UsbSerialPort;

import org.json.JSONObject;

import java.util.List;

public class SettingFragment extends BaseFragment implements View.OnClickListener,FragmentBackHandler{

    private OnFragmentInteractionListener mListener;

    public SettingFragment() {

    }

    public static SettingFragment newInstance(String param1, String param2) {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.fragment_setting, container, false);
        initView(inflate);
        initData();
        initListener();

        return inflate;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initData() {
        menu_title.setText(R.string.main_setting);
        if (Config.getInstance(getContext()).getLanguage() == Config.LANGUAGE_EN){
            language_tv.setText(getContext().getResources().getString(R.string.language_en));
        }else {
            language_tv.setText(getContext().getResources().getString(R.string.language_zh));
        }
        if (Config.getInstance(getContext()).isSigned()){
            portTv.setText("");
        }

        bondDevice = Config.getInstance(getContext()).getBondDevice();
        if ( bondDevice != null && bondDevice.getDeviceMode()== bondDevice.MODE_DEBUG ){
            testTv.setVisibility(View.VISIBLE);
        }else {
            testTv.setVisibility(View.GONE);
        }


        if(!Config.getInstance(getContext()).isConfigured()){
            setSuccess_ll.setVisibility(View.VISIBLE);
        }
        else{
            setSuccess_ll.setVisibility(View.GONE);
        }

        //获取老版本的app
        getOldApkVersion();
        //检测apk是否有新版本
        if (bondDevice != null) {
            if(((MyApplication)getContext().getApplicationContext()).getNewAPPVersion() != null){
                refresh();
            }
        }

    }


    /**
     * 比较APP版本
     */
    public void refresh(){
        DocumentVersion newVersion = ((MyApplication)getContext().getApplicationContext()).getNewAPPVersion();
        if(newVersion == null){
            return;
        }
        int oldMain = Integer.parseInt(oldVersion.getMain());
        int newMain = Integer.parseInt(newVersion.getMain());

        String oldCodeStr = oldVersion.getCode();
        String newCodeStr = newVersion.getCode();
        String oldSlaveStr = oldVersion.getSlave();
        String newSlaveStr = newVersion.getSlave();

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
            updateTv.setVisibility(View.VISIBLE);
            isNewVer = true;

        } else if (oldMain == newMain && (oldSlave < newSlave)) {
            updateTv.setVisibility(View.VISIBLE);
            isNewVer = true;

        } else if (oldCode < newCode) {
            updateTv.setVisibility(View.VISIBLE);
            isNewVer = true;
        }


        if(Config.getInstance(getContext()).getBondDevice().getDeviceMode() == DeviceBean.MODE_DEBUG){
            testTv.setVisibility(View.VISIBLE);
        }
        else{
            testTv.setVisibility(View.GONE);
        }
    }

    private void getOldApkVersion() {
        PackageManager packageManager = getContext().getPackageManager();
        try {
            PackageInfo info = packageManager.getPackageInfo(getContext().getPackageName(), 0);
            String currentVersion = info.versionName;
            int currentVersionCode = info.versionCode;
            String str[] = currentVersion.split("\\.");
            if (str.length == 2) {
                oldVersion.setMain(str[0]);
                oldVersion.setSlave(str[1]);
            }
            oldVersion.setCode(currentVersionCode+"");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    DeviceBean bondDevice  =  null;

    boolean isNewVer = false;
    DocumentVersion oldVersion = new DocumentVersion();

    private ImageView menuIv;
    private ImageView port_im;
    private TextView portTv;
    private LinearLayout port_ll;
    private ImageView personinfo_im;
    private LinearLayout personinfo_ll;
    private TextView personinfoTv;
    private TextView updateTv;
    private LinearLayout update_ll;
    private ImageView update_ib;
    private LinearLayout helpTv;
    private LinearLayout aboutTv;
    private LinearLayout disclaimerTv;
    private LinearLayout preferenctTv;
    private LinearLayout testTv;
    private TextView menu_title;
    private TextView language_tv;
    private LinearLayout language_ll;
    private ImageView language_ib;
    private LinearLayout setSuccess_ll;




    private void initView(View inflate) {
        menuIv = (ImageView) inflate.findViewById(R.id.menu_iv);
        menu_title = (TextView) inflate.findViewById(R.id.menu_title);
        port_im = (ImageView) inflate.findViewById(R.id.setting_port_im);
        port_ll = (LinearLayout) inflate.findViewById(R.id.setting_port_ll);
        portTv = (TextView) inflate.findViewById(R.id.setting_port_tv);
        personinfo_im = (ImageView) inflate.findViewById(R.id.setting_info_im);
        personinfo_ll = (LinearLayout) inflate.findViewById(R.id.setting_info_ll);
        personinfoTv = (TextView) inflate.findViewById(R.id.setting_info_tv);
        update_ll = (LinearLayout) inflate.findViewById(R.id.setting_update_tv);
        update_ib = (ImageView) inflate.findViewById(R.id.setting_update_imagebt);
        updateTv = (TextView) inflate.findViewById(R.id.setting_update_new_tv);
        helpTv = (LinearLayout) inflate.findViewById(R.id.setting_help_tv);
        aboutTv = (LinearLayout) inflate.findViewById(R.id.setting_about_tv);
        disclaimerTv = (LinearLayout) inflate.findViewById(R.id.setting_disclaimer_tv);
        preferenctTv = (LinearLayout)inflate.findViewById(R.id.setting_preference_tv);
        testTv = (LinearLayout) inflate.findViewById(R.id.setting_test_tv);
        language_tv = (TextView) inflate.findViewById(R.id.setting_language_tv);
        language_ll= (LinearLayout) inflate.findViewById(R.id.setting_language_ll);
        language_ib = (ImageView) inflate.findViewById(R.id.setting_language_imagebt);
        setSuccess_ll = (LinearLayout) inflate.findViewById(R.id.setting_setSuccess_tv);

    }

    private void initListener() {
        menuIv.setOnClickListener(this);
        port_ll.setOnClickListener(this);
        port_im.setOnClickListener(this);
        aboutTv.setOnClickListener(this);
        update_ll.setOnClickListener(this);
        update_ib.setOnClickListener(this);
        helpTv.setOnClickListener(this);
        personinfo_ll.setOnClickListener(this);
        personinfo_im.setOnClickListener(this);
        testTv.setOnClickListener(this);
        language_ll.setOnClickListener(this);
        language_ib.setOnClickListener(this);
        disclaimerTv.setOnClickListener(this);
        preferenctTv.setOnClickListener(this);
        setSuccess_ll.setOnClickListener(this);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodeConstant.CODE_PORT_ACTIVITY){
            initData();
        }else if (requestCode == RequestCodeConstant.CODE_NEW_VER_ACTIVITY) {
            PackageManager packageManager = getContext().getPackageManager();
            try {
                PackageInfo info = packageManager.getPackageInfo(getContext().getPackageName(), 0);
                String currentVersion = info.versionName;
                int currentVersionCode = info.versionCode;
                DocumentVersion newVersion = new DocumentVersion();
                String str[] = currentVersion.split("\\.");
                if (str.length == 2) {
                    newVersion.setMain(str[0]);
                    newVersion.setSlave(str[1]);
                }
                newVersion.setCode(currentVersionCode+"");
                if (oldVersion.getMain().equals(newVersion.getMain()) &&
                        oldVersion.getCode().equals(newVersion.getCode()) &&
                        oldVersion.getSlave().equals(newVersion.getSlave()) && isNewVer){
                    updateTv.setVisibility(View.VISIBLE);
                }else {
                    updateTv.setVisibility(View.GONE);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.menu_iv:
                if (getContext() instanceof MainActivity) {
                    ((MainActivity)getActivity()).onFragSwitch(MainActivity.HOME_ID);
                }
                break;
            case R.id.setting_about_tv:
                Intent intent = new Intent(getContext(), AboutActivity.class);
                startActivity(intent);
                break;
            case R.id.setting_update_tv:
                Intent updateIntent = new Intent(getContext(), AppUpdateActivity.class);
                startActivityForResult(updateIntent, RequestCodeConstant.CODE_NEW_VER_ACTIVITY);
                break;
            case R.id.setting_port_ll:
                if(!Config.getInstance(getContext()).isConfigured()){
                    Toast.makeText(getContext(), getString(R.string.notSold), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Config.getInstance(getContext()).getBondDevice() != null && Config.getInstance(getContext()).isConfigured()) {
                    if (Config.getInstance(getContext()).isSigned()) {
                        Intent portintent = new Intent(getContext(), PortActivity.class);
                        startActivityForResult(portintent, RequestCodeConstant.CODE_PORT_ACTIVITY);
                    }else {
                        final TipDialog dialog = new TipDialog.Builder(getContext())
                                .setTitle(getResources().getString(R.string.tip_title))
                                .setMessage(getString(R.string.device_not_sign))
                                .setNegativeClickListener(getString(R.string.tip_no), new TipDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialogInterface, int index, String label) {
                                        dialogInterface.dismiss();
                                    }
                                })
                                .setPositiveClickListener(getString(R.string.tip_yes), new TipDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialogInterface, int index, String label) {
                                        dialogInterface.dismiss();
                                        SignDialog.newInstance(getContext())
                                                .setNegativeClickListener(new SignDialog.OnClickListener() {
                                                    @Override
                                                    public void onClick(Dialog dialog) {
                                                        dialog.dismiss();
                                                    }
                                                })
                                                .setSignCallback(new SignDialog.SignCallback() {
                                                    @Override
                                                    public void onFinish(boolean success, SignDialog dialogInterface, String message) {
                                                        if (success) {
                                                            Intent portintent = new Intent(getContext(), PortActivity.class);
                                                            startActivityForResult(portintent, RequestCodeConstant.CODE_PORT_ACTIVITY);
                                                        }
                                                        dialogInterface.dismiss();
                                                    }
                                            })
                                            .show();

                                    if (UsbService.getInstance().getSerialPorts() != null) {
                                        final Handler handler = new Handler();
                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {
                                                while (SignDialog.getInstance() != null && !SignDialog.getInstance().waitSign()) {
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
                                                                    SignDialog.getInstance().signWithPort(mUsbPorts.get(i));
                                                                }
                                                            }
                                                        }
                                                    });
                                                }
                                                handler.post(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        for (int i = 0; i < UsbService.getInstance().getSerialPorts().size(); i++) {
                                                            SignDialog.getInstance().signWithPort(UsbService.getInstance().getSerialPorts().get(i));
                                                        }
                                                    }
                                                });
                                            }
                                        }).start();

                                    }
                                    }
                                })
                                .requestSystemAlert(true).build();
                        dialog.show();
                    }
                }
                break;
            case R.id.setting_help_tv:
                Intent helpintent = new Intent(getContext(), HelpActivity.class);
                startActivity(helpintent);
                break;
            case R.id.setting_test_tv:
                Intent Testintent = new Intent(getContext(), DiagnosisActivity.class);
                Testintent.putExtra(DiagnosisActivity.kISDebug, true);
                startActivity(Testintent);
                break;
            case R.id.setting_language_ll:
                Intent Languageintent = new Intent(getContext(), LanguageActivity.class);
                startActivity(Languageintent);
                break;
            case R.id.setting_disclaimer_tv:
                Intent discIntent = new Intent(getContext(),StatementActivity.class);
                startActivity(discIntent);
                break;
            case R.id.setting_preference_tv:
                Intent preferenceIntent = new Intent(getContext(), PreferenceActivity.class);
                startActivity(preferenceIntent);
                break;
            case R.id.setting_language_imagebt:
                language_ll.performClick();
                break;
            case R.id.setting_update_imagebt:
                update_ll.performClick();
                break;
            case R.id.setting_port_im:
                port_ll.performClick();
                break;
            case R.id.setting_info_im:
                personinfo_ll.performClick();
                break;
            case R.id.setting_setSuccess_tv:
                if (Config.getInstance(getContext()).getBondDevice() != null){
                    TipDialog dialog = new TipDialog.Builder(getContext())
                            .setTitle(getResources().getString(R.string.tip_title))
                            .setMessage(getResources().getString(R.string.device_sail_message))
                            .setNegativeClickListener(getResources().getString(R.string.device_no_sail), new TipDialog.OnClickListener() {
                                @Override
                                public void onClick(Dialog dialogInterface, int index, String label) {
                                    dialogInterface.dismiss();
                                }
                            })
                            .setPositiveClickListener(getResources().getString(R.string.device_sail), new TipDialog.OnClickListener() {
                                @Override
                                public void onClick(Dialog dialogInterface, int index, String label) {
                                    dialogInterface.dismiss();
                                    if(Config.getInstance(getContext()).getBondDevice() == null){
                                        Toast.makeText(getContext(),getString(R.string.toast_unBond),Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    final LoadDialog postDialog = new LoadDialog.Builder(getContext()).build();
                                    postDialog.show();
                                    String url = URLConstant.urlConfigureDevice + "?" +
                                            "deviceSN=" + Config.getInstance(getContext()).getBondDevice().getDeviceSN() + "&" +
                                            "targetID=" + Config.getAndroidID(getContext());
                                    VolleyUtil.jsonPostRequest(url, getContext(), new IVolleyCallback() {
                                        @Override
                                        public void getResponse(JSONObject jsonObject) {
                                            postDialog.dismiss();
                                            try{
                                                int code = jsonObject.getInt("code");
                                                if(code == 0){
                                                    Config.getInstance(getContext()).setConfigured(true);
                                                    setSuccess_ll.setVisibility(View.GONE);
                                                }
                                                else{
                                                    Toast.makeText(getContext(),getString(R.string.toast_Config_Failed),Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            catch (Exception ex){
                                                Toast.makeText(getContext(),getString(R.string.toast_Config_Failed),Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            postDialog.dismiss();
                                            Toast.makeText(getContext(),getString(R.string.toast_inspect_netconn),Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            })
                            .build();
                    dialog.show();
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onBackPressed() {
        if(getContext() instanceof MainActivity){
            ((MainActivity)getActivity()).onFragSwitch(MainActivity.HOME_ID);
        }
        return true;
    }

}
