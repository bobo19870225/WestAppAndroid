package com.west.develop.westapp.Bean.Upgrade;

/**
 * Created by Develop12 on 2017/9/12.
 */
public class VersionBean {
    private String programName;
    private String parentVersion;
    private String childVersion;
    private String url;

    public String getProgramName() {
        try {
            this.programName = this.programName.replace("\\", "/");
            this.programName = this.programName.replace("//", "/");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return programName;
    }

    public void setProgramName(String programName) {
        try {
            this.programName = programName;
            this.programName = this.programName.replace("\\", "/");
            this.programName = this.programName.replace("//", "/");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String getParentVersion() {
        return parentVersion;
    }

    public void setParentVersion(String parentVersion) {
        this.parentVersion = parentVersion;
    }

    public String getChildVersion() {
        return childVersion;
    }

    public void setChildVersion(String childVersion) {
        this.childVersion = childVersion;
    }

    public String getUrl() {
        url = url.replace("\\", "/");
        url = url.replace("//", "/");
        return url;
    }

    public void setUrl(String url) {
        url = url.replace("\\", "/");
        url = url.replace("//", "/");
        this.url = url;
    }
}
