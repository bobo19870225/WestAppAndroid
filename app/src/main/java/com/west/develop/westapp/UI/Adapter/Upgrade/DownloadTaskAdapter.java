package com.west.develop.westapp.UI.Adapter.Upgrade;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.west.develop.westapp.Tools.Utils.FileUtil;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Bean.Upgrade.DownloadDB;
import com.west.develop.westapp.Download.ProgramDownload.DownloadManager;
import com.west.develop.westapp.Download.ProgramDownload.ProgramDownLoadThread;

import java.util.List;

/**
 * Created by Develop12 on 2017/5/28.
 */
public class DownloadTaskAdapter extends BaseAdapter {

    private List<ProgramDownLoadThread> mThreads;
    private Context mContext;
    private CheckBox mDownloadBTN;
    private boolean isItemStart = false;

    public DownloadTaskAdapter(List<ProgramDownLoadThread> list, Context context, CheckBox downloadBTN) {
        this.mThreads = list;
        this.mContext = context;
        this.mDownloadBTN = downloadBTN;

    }

    @Override
    public int getCount() {
        if (mThreads.size() != 0)
        {
            return mThreads.size();
        }
        return 0;
    }

    @Override
    public Object getItem(int position) {
        if (mThreads.size() != 0)
        {
            return mThreads.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public boolean isItemStart() {
        return isItemStart;
    }


    public void setItemStart(boolean itemStart) {
        isItemStart = itemStart;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final HolderView holderView;
        if(convertView == null) {
            holderView = new HolderView();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_download_task,null);
            holderView.number = (TextView) convertView.findViewById(R.id.onekeytext);
            holderView.name = (TextView) convertView.findViewById(R.id.onekeyname);
            holderView.fileSize_TV = (TextView)convertView.findViewById(R.id.item_FileSize_TV);
            holderView.progressBar = (TextView) convertView.findViewById(R.id.onekeyprogressBar);
            //holderView.checkBox = (CheckBox) convertView.findViewById(R.id.onekeyRadioButton);
            holderView.statusLayout = (LinearLayout)convertView.findViewById(R.id.item_Status_Layout);
            holderView.statusIMG = (ImageView)convertView.findViewById(R.id.item_Status_IMG);
            holderView.statusLabel = (TextView)convertView.findViewById(R.id.item_Status_LABEL);
            convertView.setTag(holderView);
        }
        else {
            holderView = (HolderView) convertView.getTag();
        }


        final ProgramDownLoadThread thread = mThreads.get(position);



        holderView.number.setText(position + 1 + "");
        holderView.name.setText(mThreads.get(position).getFileName());

        String sizeStr = FileUtil.getFileSizeStr(thread.getContenetSize());
        if(thread.getContenetSize() <= 0){
            sizeStr = mContext.getResources().getString(R.string.unknown);
        }
        else {
            sizeStr = FileUtil.getFileSizeStr(thread.getContenetSize());
        }
        holderView.fileSize_TV.setText(sizeStr);


        int progress = (int)(mThreads.get(position).getProgress());
        holderView.progressBar.setText(progress + "%");

        refreshStatus(holderView,thread.getStatus());

        final HolderView tempHolder = holderView;

        holderView.statusLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(thread.getStatus() == DownloadDB.STATUS_PAUSE){
                    isItemStart = true;
                    mDownloadBTN.setChecked(true);
                    DownloadManager.getInstance(mContext).startUrl(thread.getUrl(),DownloadDB.STATUS_PAUSE);

                }
                else if (thread.getStatus() == DownloadDB.STATUS_DOWNLOAD){
                    DownloadManager.getInstance(mContext).pauseUrl(thread.getUrl());

                }else if (thread.getStatus() == DownloadDB.STATUS_WAIT){
                    isItemStart = true;
                    mDownloadBTN.setChecked(true);
                    DownloadManager.getInstance(mContext).startUrl(thread.getUrl(),DownloadDB.STATUS_WAIT);

                }
                refreshStatus(tempHolder,thread.getStatus());
            }
        });
        return convertView;
    }

    private void refreshStatus(HolderView holderView,int status){
        if(status == DownloadDB.STATUS_WAIT){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holderView.statusIMG.setImageDrawable(mContext.getDrawable(R.mipmap.icon_download_wait));
            }
            else{
                holderView.statusIMG.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.icon_download_wait));
            }
            holderView.statusLabel.setText(mContext.getResources().getString(R.string.waitdownload));
        }
        if(status == DownloadDB.STATUS_DOWNLOAD){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holderView.statusIMG.setImageDrawable(mContext.getDrawable(R.mipmap.icon_download_downloading));
            }
            else{
                holderView.statusIMG.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.icon_download_downloading));
            }
            holderView.statusLabel.setText(mContext.getResources().getString(R.string.downloading));
        }
        if(status == DownloadDB.STATUS_PAUSE){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                holderView.statusIMG.setImageDrawable(mContext.getDrawable(R.mipmap.icon_download_pause));
            }
            else{
                holderView.statusIMG.setImageDrawable(mContext.getResources().getDrawable(R.mipmap.icon_download_pause));
            }
            holderView.statusLabel.setText(mContext.getResources().getString(R.string.stop));
        }
        //holderView.statusIMG
    }

      class HolderView {
         TextView number;
         TextView name;
         TextView fileSize_TV;
         TextView progressBar;
         LinearLayout statusLayout;
         ImageView statusIMG;
         TextView statusLabel;


    }


}
