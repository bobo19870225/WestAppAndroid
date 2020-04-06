package com.west.develop.westapp.UI.Activity.Upgrade;

import android.app.Dialog;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.west.develop.westapp.Dialog.TipDialog;
import com.west.develop.westapp.Bean.Upgrade.DownloadDB;
import com.west.develop.westapp.R;
import com.west.develop.westapp.UI.Adapter.Upgrade.DownloadTaskAdapter;
import com.west.develop.westapp.UI.base.BaseActivity;
import com.west.develop.westapp.Download.ProgramDownload.DownloadManager;
import com.west.develop.westapp.Download.ProgramDownload.OnDownloadChangeListener;
import com.west.develop.westapp.Download.ProgramDownload.ProgramDownLoadThread;

import org.json.JSONArray;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DownloadTaskActivity extends BaseActivity {
    public static final String kURLList = "kURLList";

    private TextView back_tv;
    private TextView title;
    private ListView listView;
    private CheckBox downloadBTN;
    private List<ProgramDownLoadThread> mDownloadThreads;
    private DownloadTaskAdapter adapter;
    private Handler mHandler = new Handler();


    @Override
    protected View getContentView() {
        return this.getLayoutInflater().inflate(R.layout.activity_download, null);
    }

    @Override
    protected void initView() {
        back_tv = findViewById(R.id.car_back);
        back_tv.setVisibility(View.VISIBLE);
        title = findViewById(R.id.car_title);
        downloadBTN = findViewById(R.id.title_Download_BTN);
        downloadBTN.setVisibility(View.VISIBLE);


        /*
         * 下载按钮的监听
         */
        downloadBTN.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //点击按钮暂停正在下载的程序，全部暂停
                if (!downloadBTN.isChecked() && DownloadManager.getInstance(DownloadTaskActivity.this).isStarted()) {
                    adapter.setItemStart(false);
                    downloadBTN.setText(R.string.startdownload);
                    DownloadManager.getInstance(DownloadTaskActivity.this).pauseAllDownload();


                }//如果所有的下载列表都是暂停状态时，将按钮的文字改为开始下载程序
                else if (!downloadBTN.isChecked() && !DownloadManager.getInstance(DownloadTaskActivity.this).isStarted()) {
                    downloadBTN.setText(R.string.startdownload);

                }//按钮开始下载程序，文字变为全部暂停
                else if (downloadBTN.isChecked()) {
                    //当isItemStart为真时，表示点击了列表中的其中一项，只需改变按钮的状态文字
                    if (adapter.isItemStart()) {
                        downloadBTN.setText(R.string.stopdownload);
                        adapter.setItemStart(false);
                    }
                    //点击按钮使列表处于下载状态
                    else {
                        downloadBTN.setText(R.string.stopdownload);
                        DownloadManager.getInstance(DownloadTaskActivity.this).startDownload();
                    }
                }
            }
        });

        listView = findViewById(R.id.onekeylistview);
    }

    @Override
    protected void initData() {
        if (mDownloadThreads == null) {
            mDownloadThreads = new ArrayList<>();
        }


        DownloadManager.getInstance(this).setOnChangeListener(new OnDownloadChangeListener() {
            @Override
            public void onChange(final CopyOnWriteArrayList<ProgramDownLoadThread> list) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (mDownloadThreads == null) {
                                    mDownloadThreads = new ArrayList<>();
                                }
                                mDownloadThreads.clear();
                                mDownloadThreads.addAll(list);
                                int count = 0;
                                if (list.size() > 0) {
                                    for (int i = 0; i < list.size(); i++) {
                                        if (list.get(i).getStatus() == DownloadDB.STATUS_PAUSE) {
                                            count++;
                                        }//当列表中的item都是暂停状态时，将按钮的状态改成开始下载
                                        if (count == list.size()) {
                                            downloadBTN.setChecked(false);
                                        }
                                    }
                                }

                                notifyDataSetChanged();
                            }
                        });

                    }
                }).start();

            }
        });

        adapter = new DownloadTaskAdapter(mDownloadThreads, DownloadTaskActivity.this, downloadBTN);
        listView.setAdapter(adapter);

        DownloadManager.getInstance(this).requestThreads();
        DownloadManager.getInstance(this).queryProgressPerSecond();


        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String listStr = getIntent().getStringExtra(kURLList);
                    JSONArray array = new JSONArray(listStr);

                    ArrayList<String> urls = new ArrayList<>();
                    for (int i = 0; i < array.length(); i++) {
                        urls.add(URLEncoder.encode(array.getString(i), "utf-8"));
                    }
                    DownloadManager.getInstance(DownloadTaskActivity.this).addUrls(urls);

                    DownloadManager.getInstance(DownloadTaskActivity.this).refreshDowndload();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }).start();


        //如果一进来就是下载状态，就直接启动到下载模式
        if (DownloadManager.getInstance(this).isStarted()) {
            downloadBTN.setChecked(true);
        } else {
            downloadBTN.setText(R.string.startdownload);
        }
        title.setText(R.string.upgrade_update);
    }

    public void notifyDataSetChanged() {
        adapter.notifyDataSetChanged();
    }

    @Override
    protected void initListener() {
        back_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //listview长按监听
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, final View view, final int position, long id) {
                new TipDialog.Builder(DownloadTaskActivity.this)
                        .setTitle(getResources().getString(R.string.report_delete))
                        .setMessage(mDownloadThreads.get(position).getFileName() + ", " + getResources().getString(R.string.tip_message_delete))
                        .setPositiveClickListener(getResources().getString(R.string.tip_yes), new TipDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialogInterface, int index, String label) {
                                DownloadManager.getInstance(DownloadTaskActivity.this).deleteUrl(mDownloadThreads.get(position).getUrl());
                                DownloadManager.getInstance(DownloadTaskActivity.this).requestThreads();
                                dialogInterface.dismiss();
                            }
                        })
                        .setNegativeClickListener(getResources().getString(R.string.tip_no), new TipDialog.OnClickListener() {
                            @Override
                            public void onClick(Dialog dialogInterface, int index, String label) {
                                dialogInterface.dismiss();
                            }
                        })
                        .requestSystemAlert(true)
                        .build().show();

                return true;
            }
        });

        //listview的单击事件
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.findViewById(R.id.item_Status_Layout).performClick();

            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        //如果一进来就是下载状态，就直接启动到下载模式
        if (DownloadManager.getInstance(this).isStarted()) {
            downloadBTN.setText(R.string.stopdownload);
        } else {
            downloadBTN.setText(R.string.startdownload);
        }

    }
}