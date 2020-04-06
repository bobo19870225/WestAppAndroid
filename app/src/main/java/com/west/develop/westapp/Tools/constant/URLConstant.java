package com.west.develop.westapp.Tools.constant;

/**
 * Created by Develop14 on 2017/6/8.
 */
public class URLConstant {

    private static final boolean isDebug = true;
    //    public static final String HOST = isDebug ? "http://zexuanlin.wicp.net" : "";
    //    public static final String HOST = isDebug?"http://oerllin.51vip.biz":"";
    private static final String HOST = isDebug ? " http://westapptest.vicp.io" : "";

    private static final String DOMAIN = HOST + "/WestProBack/";

    //获取汽车地址
    public static String urlCarList = DOMAIN + "car/getCarList";
    //获取汽车类型地址

    /**
     * 获取所有程序版本
     *
     * @params:
     * @deviceSN
     */
    public static String CAR_UPDATE_VERSION = DOMAIN + "version/getNewVersions?deviceSN=";


    /**
     * 下载
     */
    public static final String urlProgramDownload = DOMAIN + "version/download";

    /**
     * @urlProgramVedio + programPath.mp4
     */
    public static final String urlProgramVideo = DOMAIN + "../WestProSaveDIR/Program";

    /**
     * 检查程序视频
     */
    public static final String urlProgramCheckVideo = DOMAIN + "/version/checkVideo?program=";

    /**
     * 检查程序版本
     *
     * @params:
     * @deviceSN
     * @program
     */
    public static final String urlProgramCheckVersion = DOMAIN + "version/checkVersion";


    /**
     * 设备激活:
     */
    public static final String urlDeviceSign = DOMAIN + "device/signDevice";

    /**
     * 设备绑定
     */
    public static final String urlDeviceBond = DOMAIN + "device/bondDevice";

    /**
     * 下载汽车图标
     *
     * Method: GET
     * params:
     * logoPath(例：audi.png);
     * deviceSN
     */
    public static final String urlDownloadLogo = DOMAIN + "car/downloadLogo";


    public static final int DOC_MANUAL = 0;
    public static final int DOC_GUIDE = 1;
    /**
     * 文档最新版本
     * 下载文档最新版本
     * method:GET
     * params:
     * docType( {@DOC_MANUAL} :用户手册；{@DOC_GUIDE}:快速指南)
     * deviceSN
     * locale   中文：1    英文：2
     */
    public static final String urlDucumentVersion = DOMAIN + "version/ducumentVersion";


    /**
     * 下载文档
     * params:
     *
     * @docType( {@DOC_MANUAL} :用户手册；{@DOC_GUIDE}:快速指南)
     * @deviceSN locale   中文：1    英文：2
     */
    public static final String urldownloadDocumentversion = DOMAIN + "version/downloadDocument";

    /**
     * APK最新版本
     * 下载新版本APK
     *
     * @params:
     * @deviceSN
     */
    public static final String urlAPK = DOMAIN + "version/appVersion?deviceSN=";

    /**
     * 下载APK
     *
     * @params
     * @deviceSN
     */
    public static final String urldownloadAPK = DOMAIN + "version/downloadApk?deviceSN=";


    /**
     * 下载固件程序
     *
     * @params:
     * @deviceSN
     */
    public static final String urldownloadFirmware = DOMAIN + "version/downloadFW?deviceSN=";


    /**
     * 获取支持的蓝牙名称列表
     */
    public static final String urlBluetoothList = DOMAIN + "version/btList";


    /**
     * 获取汽车
     *
     * @params
     * @deviceSN
     * @targetID
     * @regCount
     */
    public static final String urlCheckDeviceMode = DOMAIN + "device/checkDeviceMode";


    /**
     * 配置设备，开始计算试用次数
     *
     * @method POST
     * @Params:
     * @deviceSN
     * @targetID
     */
    public static final String urlConfigureDevice = DOMAIN + "device/config";


    /**
     * 上传诊断记录
     *
     * @method: POST
     * @params:
     * @deviceSN
     * @time （格式：yyyy-MM-dd HH:mm:ss）
     * @content
     * @program
     * @prev 上传时未配置
     */
    public static final String urlReportPost = DOMAIN + "report/postReport";


    /**
     * 分页查询网络诊断记录
     *
     * @method: GET
     * @params:
     * @deviceSN
     * @page 起始页为：1
     * @count 单页条数
     */
    public static final String urlQueryReport = DOMAIN + "report/queryReport";
}