package com.west.develop.westapp.UI.Fragment.setting.Help;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.west.develop.westapp.Bean.AppBean.DocumentVersion;
import com.west.develop.westapp.CallBack.ResultListener;
import com.west.develop.westapp.Config.Config;
import com.west.develop.westapp.Dialog.LoadDialog;
import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.Download.Threads.DocumentDownloadThread;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.Utils.VolleyUtil;
import com.west.develop.westapp.Tools.Utils.WifiUtil;
import com.west.develop.westapp.Tools.constant.URLConstant;
import com.west.develop.westapp.UI.base.BaseFragment;
import com.west.develop.westapp.pdfviewer.PDFView;
import com.west.develop.westapp.pdfviewer.listener.OnLoadCompleteListener;
import com.west.develop.westapp.pdfviewer.listener.OnPageChangeListener;
import com.west.develop.westapp.pdfviewer.listener.OnPageErrorListener;
import com.west.develop.westapp.pdfviewer.scroll.DefaultScrollHandle;
import com.west.develop.westapp.pdfviewer.util.FitPolicy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;


/**
 * Created by Develop0 on 2017/12/29.
 */

public class ManualFrament extends BaseFragment implements OnPageChangeListener, OnLoadCompleteListener, OnPageErrorListener {
    private static final String TAG = ManualFrament.class.getSimpleName();

    public static ManualFrament newInstance() {

        ManualFrament fragment = new ManualFrament();
        return fragment;
    }


    //PDFViewPager pdfViewPager;
    //PDFPagerAdapter adapter;


    private Context mContext;

    PDFView pdfView;

    LoadDialog mLoadDialog;

    private File mFileName = null;

    Integer pageNumber = 0;


    /**
     * 应用启动时为 {false},检查更新之后为 {true}
     *@false:检查更新
     * @true:显示
     */
    private static boolean checked = false;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(R.layout.fragment_help_manual,null);
        initView(contentView);
        initListener();
        initData();
        return contentView;
    }

    private void initView(View contentView){
        pdfView = (PDFView)contentView.findViewById(R.id.pdfView);

    }

    private void initListener(){

    }

    private void initData() {
        String filPath = FileUtil.getProgramDocument(getContext());
        File file = new File(filPath);
        File files[];

        if (file.exists() && file.isDirectory()) {
            files = new File(filPath).listFiles();
            if (files.length > 0) {
                for (int i = 0; i < files.length; i++) {
                    String path = files[i].getPath();
                    String name = getString(R.string.help_manual_dcm);
                    if(Config.getInstance(getContext()).getLanguage() == Config.LANGUAGE_EN){
                        name = name + "_EN";
                    }
                    else if(Config.getInstance(getContext()).getLanguage() == Config.LANGUAGE_CH){
                        name = name + "_CH";
                    }
                    if (path.contains(name)) {
                        mFileName = new File(path);
                    }
                }
            }
        }

        if (mFileName != null) {
            pdfView.fromFile(mFileName)
                    .defaultPage(pageNumber)
                    .onPageChange(this)
                    .enableAnnotationRendering(true)
                    .onLoad(this)
                    .scrollHandle(new DefaultScrollHandle(getContext()))
                    .spacing(10) // in dp
                    .onPageError(this)
                    .pageFitPolicy(FitPolicy.WIDTH)
                    .load();
        }

        if(!checked) {
            requestVersion();
        }


    }

    @Override
    public void loadComplete(int nbPages) {
    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = page;
        //setTitle(String.format("%s %s / %s", pdfFileName, page + 1, pageCount));
    }

    @Override
    public void onPageError(int page, Throwable t) {
        Log.e(TAG, "Cannot load page " + page);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }


    /**
     * 检查新版本
     */
    private void requestVersion(){
        /**
         * 未激活
         */
        if(!Config.getInstance(mContext).isSigned()){
            checked = true;
            checkFinish();
            return;
        }

        if(WifiUtil.isSupportNetwork(getContext())){
            String url = URLConstant.urlDucumentVersion + "?" +
                    "docType=" + URLConstant.DOC_MANUAL + "&" +
                    "deviceSN=" + Config.getInstance(getContext()).getBondDevice().getDeviceSN() + "&" +
                    "locale=" + Config.getInstance(mContext).getLanguage();

            /**
             * 检查版本
             */
            VolleyUtil.jsonRequest(url,getContext(), new VolleyUtil.IVolleyCallback() {
                @Override
                public void getResponse(JSONObject jsonObject) {
                    Log.e("Manual",jsonObject.toString());
                    if(checkUpdate(URLConstant.DOC_MANUAL,jsonObject)) {
                        TipDialog dialog = new TipDialog.Builder(getContext())
                                .setTitle(getString(R.string.tip_title))
                                .setMessage(getString(R.string.quest_document_update))
                                .setNegativeClickListener(getString(R.string.cancel), new TipDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialogInterface, int index, String label) {
                                        dialogInterface.dismiss();
                                        checked = true;
                                    }
                                })
                                .setPositiveClickListener(getString(R.string.tip_yes), new TipDialog.OnClickListener() {
                                    @Override
                                    public void onClick(Dialog dialogInterface, int index, String label) {
                                        dialogInterface.dismiss();
                                        final DocumentDownloadThread documentDownloadThread = new DocumentDownloadThread(getContext(), URLConstant.DOC_MANUAL, new ResultListener() {
                                            @Override
                                            public void onResult(boolean success) {
                                                mLoadDialog.dismiss();
                                                checked = true;
                                                checkFinish();
                                            }
                                        });
                                        mLoadDialog = new LoadDialog.Builder(getContext())
                                                .setTitle(getString(R.string.loadingDocument))
                                                .setCancel(getString(R.string.cancel), new LoadDialog.OnClickListener() {
                                                    @Override
                                                    public void onClick(Dialog dialog) {
                                                        documentDownloadThread.interrupt();
                                                    }
                                                }).build();
                                        mLoadDialog.show();
                                        documentDownloadThread.start();

                                    }
                                }).build();
                        dialog.show();
                    }
                    else{
                        checked = true;
                    }

                }
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });

        }
    }

    private void checkFinish(){
        if(checked){
            if(mLoadDialog != null){
                mLoadDialog.dismiss();
            }
            initData();
        }
    }


    /**
     * 检查版本
     * @param docType
     * @param jsonObject
     * @return
     */
    private boolean checkUpdate(int docType,JSONObject jsonObject){
        try {
            Log.e("Volley DOC_MANUAL",jsonObject.toString());
            int code = jsonObject.getInt("code");
            if (code == 0) {
                JSONObject arr = jsonObject.getJSONObject("data");
                DocumentVersion localDocument = new DocumentVersion();

                Gson gson = new Gson();
                DocumentVersion netDocument = gson.fromJson(arr.getJSONObject("version").toString(),DocumentVersion.class);

                if(mContext == null){
                    Log.e("Manualcontext","null");
                }
                localDocument = FileUtil.getDocumentVersion(mContext, docType);

                if (localDocument == null) {
                    return true;
                }

                int localMain = Integer.parseInt(localDocument.getMain());
                int netMain = Integer.parseInt(netDocument.getMain());

                int codeMax = Math.max(localDocument.getCode().length(), netDocument.getCode().length());

                for (int i = localDocument.getCode().length(); i < codeMax; i++) {
                    localDocument.setCode(localDocument.getCode() + "0");
                }

                for (int i = netDocument.getCode().length(); i < codeMax; i++) {
                    netDocument.setCode(netDocument.getCode() + "0");
                }

                int slaveMax = Math.max(localDocument.getSlave().length(), netDocument.getSlave().length());

                for (int i = localDocument.getSlave().length(); i < slaveMax; i++) {
                    localDocument.setSlave(localDocument.getSlave() + "0");
                }

                for (int i = netDocument.getSlave().length(); i < slaveMax; i++) {
                    netDocument.setSlave(netDocument.getSlave() + "0");
                }

                int localCode = Integer.parseInt(localDocument.getCode());
                int localSlave = Integer.parseInt(localDocument.getSlave());
                int netCode = Integer.parseInt(netDocument.getCode());
                int netSlave = Integer.parseInt(netDocument.getSlave());
                //将本地版本和网络上的版本进行比较
                if (localMain < netMain) {
                    return true;
                } else if (localMain == netMain && (localCode < netCode || localSlave < netSlave)) {
                    return true;
                } else {
                    return false;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return false;
    }
}
