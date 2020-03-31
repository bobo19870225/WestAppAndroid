package com.west.develop.westapp.Bean;

/**
 * Created by Develop0 on 2017/8/31.
 */

public class NCarBean {


    private String carID;

    private String carName_CN;

    private String carName_EN;

    private String binRoot;

    private int number;

    private String logoPath = " ";

    public String getCarID() {
        return carID;
    }

    public void setCarID(String carID) {
        this.carID = carID;
    }

    public String getCarName_CN() {
        return carName_CN;
    }

    public void setCarName_CN(String carName) {
        this.carName_CN = carName;
    }

    public String getCarName_EN() {
        return carName_EN;
    }

    public void setCarName_EN(String carname_EN) {
        this.carName_EN = carname_EN;
    }

    public String getBinRoot() {
        return binRoot;
    }

    public void setBinRoot(String binRoot) {
        this.binRoot = binRoot;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }


    public String getCarChineseName(){
        return  getCarName_CN();
    }
    public String getCarEnglishName(){
       return getCarName_EN();
    }
}
