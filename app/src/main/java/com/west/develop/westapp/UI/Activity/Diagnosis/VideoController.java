package com.west.develop.westapp.UI.Activity.Diagnosis;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.VolleyError;
import com.west.develop.westapp.CustomView.ScreenView;
import com.west.develop.westapp.CustomView.VideoView.MyMediaController;
import com.west.develop.westapp.CustomView.VideoView.MyVideoView;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.Tools.Utils.VolleyUtil;
import com.west.develop.westapp.Tools.Utils.WifiUtil;
import com.west.develop.westapp.Tools.constant.URLConstant;
import com.west.develop.westapp.pdfviewer.PDFView;
import com.west.develop.westapp.pdfviewer.listener.OnLoadCompleteListener;
import com.west.develop.westapp.pdfviewer.listener.OnPageChangeListener;
import com.west.develop.westapp.pdfviewer.listener.OnPageErrorListener;
import com.west.develop.westapp.pdfviewer.scroll.DefaultScrollHandle;
import com.west.develop.westapp.pdfviewer.util.FitPolicy;
import com.west.develop.westapp.videocache.CacheListener;
import com.west.develop.westapp.videocache.HttpProxyCacheServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by Develop0 on 2018/5/24.
 */

public class VideoController implements View.OnClickListener,
        MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener,
        MyMediaController.OnClickIsFullScreenListener, MediaPlayer.OnErrorListener,
        OnPageErrorListener, OnLoadCompleteListener, OnPageChangeListener, CacheListener {

    RunActivity mActivity;

    MyMediaController mediaController;

    Button help;

    ScreenView mScreenView;
    RelativeLayout mScreenViewLayout;
    RelativeLayout mKeyboardLayout;
    LinearLayout mOthersArea;
    private LinearLayout mToolBar;

    private PDFView pdfView;
    private FrameLayout helpFrameLayout;
    private ImageView helpdelete;

    private MyVideoView videoView;
    private ImageView video_img;
    private TextView video_err;
    private Button onlineVideoBT;
    private ProgressBar progressBar;


    private String pathUrl; //
    private boolean fullyCached = false; //视频是否缓存完
    private Long cachedSize;  //视频缓存大小
    private boolean isVideoClick = false; //是否点击视频按钮
    private boolean isHelpClick = false; //是否点击帮助按钮
    private boolean isFull = false;

    String helpPath;


    private String url;   //视频的播放地址
    private HttpProxyCacheServer proxyCacheServer; //视频缓存服务

    Integer pageNumber = 0;
    float scale;

    Long netVideoSize;
    boolean isVideoSize = false;

    public VideoController(RunActivity activity) {
        mActivity = activity;

        initView();
    }

    private void initView() {
        help = (Button) mActivity.findViewById(R.id.helpfile);
        videoView = (MyVideoView) mActivity.findViewById(R.id.run_video);
        video_img = (ImageView) mActivity.findViewById(R.id.video_back);
        video_err = (TextView) mActivity.findViewById(R.id.error_tip);
        onlineVideoBT = (Button) mActivity.findViewById(R.id.onlineVideo);
        helpdelete = (ImageView) mActivity.findViewById(R.id.help_delete);
        helpFrameLayout = (FrameLayout) mActivity.findViewById(R.id.help_Frame);
        pdfView = (PDFView) mActivity.findViewById(R.id.helpfile_pdf);
        progressBar = (ProgressBar) mActivity.findViewById(R.id.load);

        mScreenView = (ScreenView) mActivity.findViewById(R.id.screenView);
        mScreenViewLayout = (RelativeLayout) mActivity.findViewById(R.id.screenView_Ll);
        mKeyboardLayout = (RelativeLayout) mActivity.findViewById(R.id.keyBoardLayout);
        mOthersArea = (LinearLayout) mActivity.findViewById(R.id.layout_AreaOthers);
        mToolBar = (LinearLayout) mActivity.findViewById(R.id.toolBar);


        help.setOnClickListener(this);
        helpdelete.setOnClickListener(this);
        videoView.setOnCompletionListener(this);
        videoView.setOnPreparedListener(this);
        videoView.setOnErrorListener(this);
        video_img.setOnClickListener(this);
        video_err.setOnClickListener(this);
        onlineVideoBT.setOnClickListener(this);
        video_img.setOnClickListener(this);
        video_err.setOnClickListener(this);

        scale = mActivity.getResources().getDisplayMetrics().density;
        onConfigurationChanged(mActivity.getResources().getConfiguration());
    }

    public void initData() {
        checkVideo();
        checkVideoCache();
        checkHelpPdfExist();
    }


    private void checkHelpPdfExist() {
        String path = mActivity.mProFile.getParent();
        String name = mActivity.mProFile.getName().substring(0, mActivity.mProFile.getName().length() - 4);
        if (name.endsWith("_1")) {
            name = name.substring(0, name.length() - 2);
        }
        helpPath = path + "/" + name.substring(4, name.length()) + ".pdf";
        File filePdf = new File(helpPath);
        if (filePdf.exists()) {
            help.setVisibility(View.VISIBLE);
        }

    }


    //检测服务上是否存在视频，或者存在新版本
    private void checkVideo() {
        String ver = mActivity.mProFile.getPath().substring(mActivity.mProFile.getPath().lastIndexOf("_v") + 2, mActivity.mProFile.getPath().lastIndexOf("/"));
        //通过路径查找视频是否存在
        String name = mActivity.mProgName.substring(mActivity.mProgName.lastIndexOf("_") + 1);
        try {
            String neturl = URLConstant.urlProgramCheckVideo + URLEncoder.encode(name, "utf-8") + "&version=" + ver;
            VolleyUtil.jsonRequest(neturl, mActivity, callback);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

    }

    //检测sd卡是否存在视频缓存
    private void checkVideoCache() {
        String videoName = mActivity.mProFile.getName().substring(4, mActivity.mProFile.getName().length() - 4);
        videoName = mActivity.mProFile.getParent() + "/" + videoName;
        videoName = videoName.substring(videoName.indexOf(FileUtil.PROGRAM) + FileUtil.PROGRAM.length(), videoName.length());
        if (videoName.endsWith("_1")) {
            videoName = videoName.substring(0, videoName.length() - 2);
        }
        try {
            //url = URLConstant.urlProgramVideo +  URLEncoder.encode("/video/abc","utf-8") + ".mp4";
            //url = URLConstant.urlProgramVideo + URLEncoder.encode("/video/ab c", "utf-8") + ".mp4";
            url = URLConstant.urlProgramVideo + URLEncoder.encode(videoName, "utf-8") + ".mp4";
            url = url.replace("%2f", "/");
            url = url.replace("%2F", "/");
            url = url.replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        proxyCacheServer = mActivity.getProxy(mActivity);
        proxyCacheServer.registerCacheListener(this, url);
        //String urlStr = url;

        /*try {
            urlStr = URLDecoder.decode(urlStr, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
*/
        pathUrl = url;
        //pathUrl = proxyCacheServer.getProxyUrl(urlStr);
        /*try {
            pathUrl = URLDecoder.decode(pathUrl, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }*/
        fullyCached = proxyCacheServer.isCached(url);
        cachedSize = proxyCacheServer.getCacheFileSize(url);

        if (fullyCached) {
            onlineVideoBT.setVisibility(View.VISIBLE);
        }
    }

    //视频播放
    private void startVideo() {
        onConfigurationChangedLayout();
        onlineVideoBT.setVisibility(View.GONE);
        video_err.setVisibility(View.GONE);
        videoView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        video_img.setVisibility(View.VISIBLE);

        mediaController = new MyMediaController(mActivity);
        mediaController.setClickIsFullScreenListener(this);
        videoView.setMediaController(mediaController);
        Uri uri;
        if (pathUrl != null) {
            // pathUrl = pathUrl.replace("%2F","/");
            pathUrl = pathUrl.replace(" ", "%20");
            uri = Uri.parse(pathUrl);
        } else {
            uri = Uri.parse(url);
        }

        videoView.setVideoURI(uri);
        mediaController.setMediaPlayer(videoView);
        mediaController.setAnchorView(videoView);

        videoView.requestFocus();
        videoView.start();

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Toast.makeText(mActivity, mActivity.getResources().getString(R.string.toast_online), Toast.LENGTH_LONG);
        mScreenViewLayout.setVisibility(View.VISIBLE);
        mKeyboardLayout.setVisibility(View.VISIBLE);
        mToolBar.setVisibility(View.VISIBLE);
        onlineVideoBT.setVisibility(View.VISIBLE);
        videoView.setVisibility(View.GONE);
        video_img.setVisibility(View.GONE);
        video_err.setVisibility(View.GONE);
        //横屏
        if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }

        /**
         * 视频播放完后，将系统的状态栏显示出来
         */
        WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
        params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mActivity.getWindow().setAttributes(params);
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        mActivity.showStatusBar();
        isFull = false;
        isVideoClick = false;
        onConfigurationChangedBack();

    }


    /**
     * 视频准备好后的运行
     *
     * @param mp
     */
    @Override
    public void onPrepared(MediaPlayer mp) {
        progressBar.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            videoView.setBackgroundColor(mActivity.getColor(android.R.color.transparent));
        } else {
            videoView.setBackgroundColor(mActivity.getResources().getColor(android.R.color.transparent));
        }
        videoView.requestFocus();
        videoView.start();
    }


    //视频错误信息的监听
    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        //服务器没有连接
        if (what == MediaPlayer.MEDIA_ERROR_SERVER_DIED) {
            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.toast_service_err), Toast.LENGTH_LONG).show();
        } else if (what == MediaPlayer.MEDIA_ERROR_UNKNOWN) { //服务端文件有错
            if (extra == MediaPlayer.MEDIA_ERROR_IO) {
                Toast.makeText(mActivity, mActivity.getResources().getString(R.string.toast_net_file_err), Toast.LENGTH_LONG).show();
            }
        } else if (extra == MediaPlayer.MEDIA_ERROR_TIMED_OUT) {  //连接服务端超时
            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.toast_netconn_moreTime), Toast.LENGTH_LONG).show();
        }
        progressBar.setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            videoView.setBackgroundColor(mActivity.getColor(R.color.black));
        } else {
            videoView.setBackgroundColor(mActivity.getResources().getColor(R.color.black));
        }

        video_err.setVisibility(View.VISIBLE);
        video_err.setText(mActivity.getResources().getString(R.string.video_err));

        WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
        params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mActivity.getWindow().setAttributes(params);
        mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        return true;
    }


    @Override
    public void onPageError(int page, Throwable t) {

    }

    @Override
    public void loadComplete(int nbPages) {

    }

    @Override
    public void onPageChanged(int page, int pageCount) {
        pageNumber = pageCount;

    }

    @Override
    public void onCacheAvailable(File cacheFile, String url, int percentsAvailable) {
        //Log.d("videoCache", String.format("onCacheAvailable. percents: %d, file: %s, url: %s", percentsAvailable, cacheFile, url));
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.onlineVideo:
                isVideoClick = true;
                //如果fullyCached为真，表明视频已经缓存在了
                checkVideoCache();
                if (fullyCached) {
                    if (isVideoSize && !netVideoSize.equals(cachedSize)) {
                        //网络连接时才可播放，否则提示网络没有连接
                        if (WifiUtil.isSupportNetwork(mActivity)) {
                            startVideo();
                        } else {
                            Toast.makeText(mActivity, mActivity.getResources().getString(R.string.toast_inspect_netconn), Toast.LENGTH_LONG).show();

                        }
                    } else {
                        startVideo();
                    }
                } else {
                    //网络连接时才可播放，否则提示网络没有连接
                    if (WifiUtil.isSupportNetwork(mActivity)) {
                        startVideo();
                    } else {
                        Toast.makeText(mActivity, mActivity.getResources().getString(R.string.toast_inspect_netconn), Toast.LENGTH_LONG).show();
                    }
                }
                break;
            case R.id.video_back:
                mScreenViewLayout.setVisibility(View.VISIBLE);
                mKeyboardLayout.setVisibility(View.VISIBLE);
                mToolBar.setVisibility(View.VISIBLE);

                //横屏
                if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && isFull) {
                    mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                    onConfigurationChangedFullBack();
                } else {
                    onlineVideoBT.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                    videoView.setVisibility(View.GONE);
                    video_img.setVisibility(View.GONE);
                    video_err.setVisibility(View.GONE);
                    isVideoClick = false;
                    onConfigurationChangedBack();
                }

                WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
                params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
                mActivity.getWindow().setAttributes(params);
                mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                mediaController.showFullButton();
                isFull = false;
                mActivity.showStatusBar();
                break;
            case R.id.error_tip:
                onlineVideoBT.performClick();
                break;
            case R.id.helpfile:
                isHelpClick = true;
                onConfigurationChangedLayout();
                help.setVisibility(View.GONE);
                File file = new File(helpPath);
                if (file.exists()) {
                    try {
                        helpFrameLayout.setVisibility(View.VISIBLE);
                        pdfView.setVisibility(View.VISIBLE);
                        helpdelete.setVisibility(View.VISIBLE);
                        pdfView.fromFile(file)
                                .defaultPage(pageNumber)
                                .onPageChange(this)
                                .enableAnnotationRendering(true)
                                .onLoad(this)
                                .scrollHandle(new DefaultScrollHandle(mActivity))
                                .spacing(10) // in dp
                                .onPageError(this)
                                .pageFitPolicy(FitPolicy.WIDTH)
                                .load();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                break;
            case R.id.help_delete:
                isHelpClick = false;
                onConfigurationChangedBack();
                help.setVisibility(View.VISIBLE);
                pdfView.setVisibility(View.GONE);
                helpdelete.setVisibility(View.GONE);
                helpFrameLayout.setVisibility(View.GONE);
                break;
            default:
                break;
        }
    }

    /**
     * 视频全屏的操作
     */
    WindowManager.LayoutParams params;

    @Override
    public void setOnClickIsFullScreen() {
        //横屏
        if (mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && isFull) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);

            WindowManager.LayoutParams params = mActivity.getWindow().getAttributes();
            params.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            mActivity.getWindow().setAttributes(params);
            mActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            mScreenViewLayout.setVisibility(View.VISIBLE);
            mKeyboardLayout.setVisibility(View.VISIBLE);
            mToolBar.setVisibility(View.VISIBLE);
            mActivity.showStatusBar();
            onConfigurationChangedFullBack();
            isFull = false;

        } else if ((mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ||
                mActivity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) && !isFull) {
            mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

            /**
             * 全屏时候隐藏系统状态栏
             */
            params = mActivity.getWindow().getAttributes();
            params.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            mActivity.getWindow().setAttributes(params);
            mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

            mScreenViewLayout.setVisibility(View.GONE);
            mKeyboardLayout.setVisibility(View.GONE);
            mToolBar.setVisibility(View.GONE);

            mActivity.hideStatusBar();
            isFull = true;
            onConfigurationChangedFull();
        }

    }


    public void onConfigurationChanged(Configuration newConfig) {
        //super.onConfigurationChanged(newConfig);
        videoView.refreshDrawableState();

        //切换为竖屏
        if (newConfig.orientation == mActivity.getResources().getConfiguration().ORIENTATION_PORTRAIT) {
            Log.e("orientation", "Portrait");
            ViewGroup.LayoutParams lp_Key = mKeyboardLayout.getLayoutParams();
            ViewGroup.LayoutParams lp_Screen_ll = mScreenViewLayout.getLayoutParams();
            ViewGroup.LayoutParams lp_Screen = mScreenView.getLayoutParams();
            ViewGroup.LayoutParams lp_Others = mOthersArea.getLayoutParams();
            if (lp_Key instanceof RelativeLayout.LayoutParams) {
                Log.e("Relative", "Portrait");
                RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Key;
                rLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                rLp.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rLp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                rLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                rLp.setMarginEnd((int) (14 * scale));
                rLp.setMarginStart((int) (14 * scale));
                mKeyboardLayout.setLayoutParams(rLp);
            }

            if (lp_Screen_ll instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Screen_ll;
                rLp.addRule(RelativeLayout.CENTER_HORIZONTAL);
                rLp.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                mScreenViewLayout.setLayoutParams(rLp);
                mScreenViewLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            }

            if (lp_Screen instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Screen;
                rLp.setMarginStart(0);
                rLp.setMarginEnd(0);
                mScreenView.setLayoutParams(rLp);
            }

            if (lp_Others instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Others;
                rLp.removeRule(RelativeLayout.RIGHT_OF);
                rLp.addRule(RelativeLayout.ABOVE, mKeyboardLayout.getId());
                rLp.addRule(RelativeLayout.BELOW, mScreenViewLayout.getId());
                rLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                if (mKeyboardLayout.getVisibility() == View.VISIBLE) {
                    rLp.setMargins(0, 0, 0, 0);
                } else {
                    rLp.setMargins(0, 0, 0, (int) (5 * scale));
                }
                mOthersArea.setLayoutParams(rLp);
                mOthersArea.setGravity(Gravity.RIGHT | Gravity.BOTTOM);
            }

        }

        //切换为横屏
        else if (newConfig.orientation == mActivity.getResources().getConfiguration().ORIENTATION_LANDSCAPE) {
            if (isVideoClick && onlineVideoBT.getVisibility() == View.GONE && !isFull) { //不是全屏的时候
                ViewGroup.LayoutParams lp_Key = mKeyboardLayout.getLayoutParams();
                ViewGroup.LayoutParams lp_Screen_ll = mScreenViewLayout.getLayoutParams();
                ViewGroup.LayoutParams lp_Screen = mScreenView.getLayoutParams();
                ViewGroup.LayoutParams lp_Others = mOthersArea.getLayoutParams();
                if (lp_Key instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Key;
                    rLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    rLp.removeRule(RelativeLayout.CENTER_HORIZONTAL);
                    rLp.width = (int) (530 * scale);
                    rLp.setMarginStart((int) (14 * scale));
                    mKeyboardLayout.setLayoutParams(rLp);
                }

                if (lp_Screen_ll instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Screen_ll;
                    rLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    rLp.width = (int) (550 * scale);
                    mScreenViewLayout.setLayoutParams(rLp);
                    mScreenViewLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                }

                if (lp_Screen instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Screen;
                    rLp.setMarginEnd(0);
                    rLp.setMarginStart(0);
                    mScreenView.setLayoutParams(rLp);
                }

                if (lp_Others instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Others;
                    rLp.addRule(RelativeLayout.RIGHT_OF, mScreenViewLayout.getId());
                    rLp.removeRule(RelativeLayout.ABOVE);
                    rLp.removeRule(RelativeLayout.BELOW);
                    rLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    rLp.setMargins(0, 0, 0, (int) (5 * scale));
                    mOthersArea.setLayoutParams(rLp);
                    mOthersArea.setGravity(Gravity.LEFT | Gravity.BOTTOM);
                }

            } else if (onlineVideoBT.getVisibility() == View.GONE && isFull) {
                return;
            } else {
                ViewGroup.LayoutParams lp_Key = mKeyboardLayout.getLayoutParams();
                ViewGroup.LayoutParams lp_Screen_ll = mScreenViewLayout.getLayoutParams();
                ViewGroup.LayoutParams lp_Screen = mScreenView.getLayoutParams();
                ViewGroup.LayoutParams lp_Others = mOthersArea.getLayoutParams();
                if (lp_Key instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Key;
                    rLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    rLp.removeRule(RelativeLayout.CENTER_HORIZONTAL);
                    if (isHelpClick) {
                        rLp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        rLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                        rLp.width = (int) (530 * scale);
                        rLp.setMarginEnd((int) (14 * scale));
                    } else {
                        rLp.width = (int) (560 * scale);
                        rLp.setMarginEnd((int) (210 * scale));
                    }
                    mKeyboardLayout.setLayoutParams(rLp);
                }

                if (lp_Screen_ll instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Screen_ll;
                    rLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    rLp.removeRule(RelativeLayout.CENTER_HORIZONTAL);
                    if (isHelpClick) {
                        rLp.width = (int) (550 * scale);
                    } else {
                        rLp.width = (int) (765 * scale);
                    }
                    mScreenViewLayout.setLayoutParams(rLp);
                    mScreenViewLayout.setGravity(Gravity.RIGHT);
                }

                if (lp_Screen instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Screen;
                    rLp.setMarginEnd((int) (100 * scale));
                    mScreenView.setLayoutParams(rLp);
                }

                if (lp_Others instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Others;
                    rLp.addRule(RelativeLayout.RIGHT_OF, mScreenViewLayout.getId());
                    rLp.removeRule(RelativeLayout.ABOVE);
                    rLp.removeRule(RelativeLayout.BELOW);
                    if (isHelpClick) {
                        rLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    } else {
                        rLp.width = (int) (200 * scale);
                    }
                    rLp.setMargins(0, 0, 0, (int) (5 * scale));
                    mOthersArea.setLayoutParams(rLp);
                    mOthersArea.setGravity(Gravity.LEFT | Gravity.BOTTOM);
                }
            }
        }
    }

    //点击视频按钮时的布局分布
    public void onConfigurationChangedLayout() {
        if (mActivity.getResources().getConfiguration().orientation == mActivity.getResources().getConfiguration().ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams lp_Key = mKeyboardLayout.getLayoutParams();
            ViewGroup.LayoutParams lp_Screen_ll = mScreenViewLayout.getLayoutParams();
            ViewGroup.LayoutParams lp_Screen = mScreenView.getLayoutParams();
            ViewGroup.LayoutParams lp_Others = mOthersArea.getLayoutParams();
            if (lp_Key instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Key;
                rLp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                rLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rLp.width = (int) (530 * scale);
                rLp.setMarginStart((int) (14 * scale));
                mKeyboardLayout.setLayoutParams(rLp);
            }

            if (lp_Screen_ll instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Screen_ll;
                rLp.width = (int) (550 * scale);
                mScreenViewLayout.setLayoutParams(rLp);
                mScreenViewLayout.setGravity(Gravity.CENTER_HORIZONTAL);
            }

            if (lp_Screen instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Screen;
                rLp.setMarginEnd(0);
                rLp.setMarginStart(0);
                mScreenView.setLayoutParams(rLp);
            }

            if (lp_Others instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Others;
                rLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                rLp.setMargins(0, 0, 0, (int) (5 * scale));
                mOthersArea.setLayoutParams(rLp);
            }
        }
    }

    //点击视频返回和视频播发完的时候布局分布
    private void onConfigurationChangedBack() {
        if (mActivity.getResources().getConfiguration().orientation == mActivity.getResources().getConfiguration().ORIENTATION_LANDSCAPE) {
            ViewGroup.LayoutParams lp_Key = mKeyboardLayout.getLayoutParams();
            ViewGroup.LayoutParams lp_Screen_ll = mScreenViewLayout.getLayoutParams();
            ViewGroup.LayoutParams lp_Screen = mScreenView.getLayoutParams();
            ViewGroup.LayoutParams lp_Others = mOthersArea.getLayoutParams();
            if (lp_Key instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Key;
                rLp.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rLp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                rLp.removeRule(RelativeLayout.CENTER_HORIZONTAL);
                rLp.width = (int) (560 * scale);
                rLp.setMarginEnd((int) (210 * scale));
                mKeyboardLayout.setLayoutParams(rLp);
            }

            if (lp_Screen_ll instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Screen_ll;
                rLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                rLp.width = (int) (765 * scale);
                mScreenViewLayout.setLayoutParams(rLp);
                mScreenViewLayout.setGravity(Gravity.RIGHT);
            }

            if (lp_Screen instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Screen;
                rLp.setMarginEnd((int) (100 * scale));
                mScreenView.setLayoutParams(rLp);
            }

            if (lp_Others instanceof RelativeLayout.LayoutParams) {
                RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Others;
                rLp.addRule(RelativeLayout.RIGHT_OF, mScreenViewLayout.getId());
                rLp.removeRule(RelativeLayout.ABOVE);
                rLp.removeRule(RelativeLayout.BELOW);
                rLp.width = (int) (200 * scale);
                rLp.setMargins(0, 0, 0, (int) (5 * scale));
                mOthersArea.setLayoutParams(rLp);
                mOthersArea.setGravity(Gravity.LEFT | Gravity.BOTTOM);
            }
        }


    }

    //点击显示全屏的布局分布
    private void onConfigurationChangedFull() {
        if (mActivity.getResources().getConfiguration().orientation == mActivity.getResources().getConfiguration().ORIENTATION_LANDSCAPE) {
            if (onlineVideoBT.getVisibility() == View.GONE && isFull) { //全屏的时候
                ViewGroup.LayoutParams lp_Others = mOthersArea.getLayoutParams();
                if (lp_Others instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Others;
                    rLp.addRule(RelativeLayout.RIGHT_OF, mScreenViewLayout.getId());
                    rLp.removeRule(RelativeLayout.ABOVE);
                    rLp.removeRule(RelativeLayout.BELOW);
                    rLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    rLp.setMargins(0, 0, 0, 0);
                    mOthersArea.setLayoutParams(rLp);
                    mOthersArea.setGravity(Gravity.LEFT | Gravity.BOTTOM);
                }
            }
        }

    }

    //全屏状态下点击视屏返回的布局分布
    private void onConfigurationChangedFullBack() {
        if (mActivity.getResources().getConfiguration().orientation == mActivity.getResources().getConfiguration().ORIENTATION_LANDSCAPE) {
            if (onlineVideoBT.getVisibility() == View.GONE && isFull) { //全屏的时候点返回
                ViewGroup.LayoutParams lp_Key = mKeyboardLayout.getLayoutParams();
                ViewGroup.LayoutParams lp_Screen_ll = mScreenViewLayout.getLayoutParams();
                ViewGroup.LayoutParams lp_Screen = mScreenView.getLayoutParams();
                ViewGroup.LayoutParams lp_Others = mOthersArea.getLayoutParams();
                if (lp_Key instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Key;
                    rLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    rLp.removeRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    rLp.removeRule(RelativeLayout.CENTER_HORIZONTAL);
                    rLp.width = (int) (530 * scale);
                    rLp.setMarginStart((int) (14 * scale));
                    mKeyboardLayout.setLayoutParams(rLp);
                }

                if (lp_Screen_ll instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Screen_ll;
                    rLp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    rLp.width = (int) (550 * scale);
                    mScreenViewLayout.setLayoutParams(rLp);
                    mScreenViewLayout.setGravity(Gravity.CENTER_HORIZONTAL);
                }

                if (lp_Screen instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Screen;
                    rLp.setMarginEnd(0);
                    rLp.setMarginStart(0);
                    mScreenView.setLayoutParams(rLp);
                }
                if (lp_Others instanceof RelativeLayout.LayoutParams) {
                    RelativeLayout.LayoutParams rLp = (RelativeLayout.LayoutParams) lp_Others;
                    rLp.addRule(RelativeLayout.RIGHT_OF, mScreenViewLayout.getId());
                    rLp.removeRule(RelativeLayout.ABOVE);
                    rLp.removeRule(RelativeLayout.BELOW);
                    rLp.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    rLp.setMargins(0, 0, 0, (int) (5 * scale));
                    mOthersArea.setLayoutParams(rLp);
                    mOthersArea.setGravity(Gravity.LEFT | Gravity.BOTTOM);
                }
            }
        }
    }


    public void unRegisterReceiver() {

        videoView.stopPlayback();
        mActivity.getProxy(mActivity).unregisterCacheListener(this);
    }

    VolleyUtil.IVolleyCallback callback = new VolleyUtil.IVolleyCallback() {

        @Override
        public void getResponse(JSONObject jsonObject) {
            Log.e("Volley CheckVideo", jsonObject.toString());
            if ("".equals(jsonObject) || jsonObject == null) {
                return;
            }
            try {
                JSONObject json = jsonObject.getJSONObject("data");
                netVideoSize = json.getLong("videoSize");
                if (netVideoSize > 0) { //大于0说明有视频存在
                    onlineVideoBT.setVisibility(View.VISIBLE);
                    isVideoSize = true;
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorResponse(VolleyError error) {
            if (error.networkResponse != null) {
                String errorStr = new String(error.networkResponse.data);
                Log.e("erro", errorStr);
            }
        }
    };
}

