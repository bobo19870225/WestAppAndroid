package com.west.develop.westapp.Bean.Upgrade;

/**
 * Created by Develop0 on 2017/9/12.
 */

public class DownloadDB {
    public static final int STATUS_WAIT = 1;
    public static final int STATUS_DOWNLOAD = 2;
    public static final int STATUS_PAUSE = 3;
    public static final int STATUS_ALL = 0;


    private String url;
    private String fileName;
    private int status;
    private long contentSize;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getContentSize() {
        return contentSize;
    }

    public void setContentSize(long contentSize) {
        this.contentSize = contentSize;
    }
}
