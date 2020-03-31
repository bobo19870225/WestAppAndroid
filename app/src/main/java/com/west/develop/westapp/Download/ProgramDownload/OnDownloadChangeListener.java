package com.west.develop.westapp.Download.ProgramDownload;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Develop0 on 2017/9/12.
 */

public interface OnDownloadChangeListener {
   abstract void onChange(CopyOnWriteArrayList<ProgramDownLoadThread> list);
}
