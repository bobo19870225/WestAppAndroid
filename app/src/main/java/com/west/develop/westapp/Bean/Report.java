package com.west.develop.westapp.Bean;

/**
 * 我的报告的列表
 * Created by Develop12 on 2017/5/15.
 */
public class Report {

   /* private boolean ischeck; //复选框*/
    private int reportId;
    private  String file;
    private  String dataTime;

    /**
     * 文件名以： _2  结束，表示已上传
     * 文件名以：_1   结束，表示必须上传，在平板启动时，如果连接网络会自动上传
     */

    //是否需要上传
    private boolean post = false;

    private boolean isChecked = false;

    public int getReportId() {
        return reportId;
    }

    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    public String getFile() {
        return file;
    }

    public String getReportName(){
        String reportName = file.replace("$","/");
        if(reportName.endsWith("_1") || reportName.endsWith("_2")){
            return reportName.substring(0,reportName.length() - 2);
        }
        return reportName;
    }

    public void setFile(String file) {
        this.file = file;
        if(this.file.toLowerCase().endsWith(".txt")){
            this.file = this.file.substring(0,this.file.length() - 4);
        }

        /**
         * 已经上传
         */
        if(this.file.endsWith("_1")){
            post = true;
        }
    }

    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public boolean isPost(){
        return post;
    }
}
