package com.west.develop.westapp.Bean.Upgrade;

import com.google.gson.Gson;
import com.west.develop.westapp.Tools.Utils.LanguageUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by Develop12 on 2017/9/14.
 */
public class UpdateDB {
    public static final int TYPE_PROGRAM = 1;
    public static final int TYPE_CARBEAN = 2;
    private char mFirstLetter;

    private boolean isChecked = false;
    private String programName;
    String url;
    String authCode;
    boolean authen = false;

    boolean isDelete = false;

    private int mUpdateType = TYPE_PROGRAM;



    private ArrayList<VersionBean> versionList;

    public char getFirstLetter() {
        if(mFirstLetter == '\u0000'){
            char first = programName.charAt(0);
            if((first >= 'a' && first <= 'z') || (first >= 'A' && first <= 'Z')){
                if (mUpdateType == TYPE_PROGRAM) {
                    mFirstLetter = programName.toUpperCase().charAt(0);
                }
            }
            else {
                mFirstLetter = LanguageUtil.getFirstLetter(programName).charAt(0);
            }
        }
        return mFirstLetter;
    }

    public void setFirstLetter(char mFirstLetter) {
        this.mFirstLetter = mFirstLetter;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        programName.replace("\\\\", "/");
        programName.replace("//", "/");
        if (programName.startsWith("/")) {
            programName = programName.substring(1);
        }
        this.programName = programName;


        String firtChar = LanguageUtil.getFirstLetter(programName);

        if (mUpdateType == TYPE_PROGRAM) {
            mFirstLetter = firtChar.toUpperCase().charAt(0);
        }

    }

    public String getUrl() {
        url.replace("\\","/");
        url.replace("//","/");
        return url;
    }

    public void setUrl(String url) {
        url.replace("\\","/");
        url.replace("//","/");
        this.url = url;
    }

    public ArrayList<VersionBean> getVersionList() {
        return versionList;
    }

    public void setVersionList(ArrayList<VersionBean> versionList) {
        this.versionList = versionList;
    }

    public void setUpdateVersionStr(String versionJson){
        versionJson = versionJson.replace("\\","/");
        Gson gson = new Gson();

        try {
            JSONArray array = new JSONArray(versionJson);
            if(array != null) {
                for (int i = 0; i < array.length(); i++) {
                    JSONObject json = array.getJSONObject(i);
                    VersionBean bean = gson.fromJson(json.toString(), VersionBean.class);
                    addUpdateVersion(bean);
                }
            }
        }
        catch (Exception ex0){
            ex0.printStackTrace();
        }
    }

    public void addUpdateVersion(VersionBean version){

        if(this.versionList == null){
            this.versionList = new ArrayList<>();
        }

        if(versionList.size() <= 0){
            versionList.add(version);
        }
        else {
            boolean contain = false;
            for (VersionBean bean : versionList) {
                if (bean.getParentVersion().equals(version.getParentVersion()) && bean.getChildVersion().equals(version.getChildVersion())) {
                    contain = true;
                    break;
                }
            }
            if(!contain){
                versionList.add(version);
            }
        }
    }

    public boolean isAuthen() {
        return authen;
    }

    public void setAuthen(boolean authen) {
        this.authen = authen;
    }

    public String getAuthCode() {
        return authCode;
    }

    public void setAuthCode(String authCode) {
        this.authCode = authCode;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }
}
