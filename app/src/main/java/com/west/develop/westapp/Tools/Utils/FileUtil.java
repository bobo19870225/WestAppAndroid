package com.west.develop.westapp.Tools.Utils;

import android.content.Context;
import android.os.Environment;

import com.west.develop.westapp.Bean.AppBean.DocumentVersion;
import com.west.develop.westapp.Bean.Upgrade.UpdateDB;
import com.west.develop.westapp.Bean.Upgrade.VersionBean;
import com.west.develop.westapp.R;
import com.west.develop.westapp.Tools.constant.URLConstant;
import com.west.develop.westapp.usb.HexDump;
import com.west.develop.westapp.videocache.HttpProxyCacheServer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Develop0 on 2017/9/7.
 */

public class FileUtil {


    public static final String PROGRAM_ROOT = "/Program/";
    public static final String DEBUG_ROOT = "/Debug/";
    public static final String PRO_DOWNLOAD_ROOT = "/ProgramDownload";
    public static final String PROGRAM = "/Program";
    public static final String PROGRAM_ICON = "/ProgramIcon/";
    public static final String PROGRAM_REPORT = "/ProgramReport/";
    public static final String PROGRAM_DOCUMENT = "/ProgramDocument/";
    public static final String BACKUP_ROOT = "/BackUP";

    public static final String AssetsDocument = "document";
    public static final String DOWNLOAD_CRC_NAME = "CheckCRC";

    public static String getAppRoot(Context context){
        String root = Environment.getExternalStorageDirectory().getPath() + "/";
        String packageName = context.getPackageName();

        return root  + packageName + "/";
    }

    /**
     * 获取程序存放根路径
     * @param context
     * @return
     */
    public static String getProgramRoot(Context context){
        String root = Environment.getExternalStorageDirectory().getPath() + "/";
        String packageName = context.getPackageName();

        return root  + packageName + PROGRAM_ROOT;
    }

    public static String getProgram(Context context){
        String root = Environment.getExternalStorageDirectory().getPath() + "/";
        String packageName = context.getPackageName();

        return root  + packageName + PROGRAM;
    }

    public static String getProgramIcon(Context context){
        String root = Environment.getExternalStorageDirectory().getPath() + "/";
        String packageName = context.getPackageName();

        return root  + packageName + PROGRAM_ICON;
    }

    public static String getProgramReport(Context context){
        String root = Environment.getExternalStorageDirectory().getPath() + "/";
        String packageName = context.getPackageName();
        return root + packageName + PROGRAM_REPORT;
    }



    /**
     * 获取调试程序 存放 根路径
     * @param context
     * @return
     */
    public static String getDebugRoot(Context context){
        String root = Environment.getExternalStorageDirectory().getPath() + "/";
        String packageName = context.getPackageName();

        return root  + packageName + DEBUG_ROOT;
    }

    /**
     * 获取诊断程序下载存放 跟路径
     * @param context
     * @return
     */
    public static String getProramDownloadRoot(Context context){
        String root = Environment.getExternalStorageDirectory().getPath() + "/";
        String packageName = context.getPackageName();

        return root + packageName + PRO_DOWNLOAD_ROOT + "/";
    }

    public static String getProgramDocument(Context context) {
        String root = Environment.getExternalStorageDirectory().getPath() + "/";

        String packageName = context.getPackageName();

        return root  + packageName + PROGRAM_DOCUMENT;
    }

    public static String getBackUPRoot(Context context){
        String root = Environment.getExternalStorageDirectory().getPath() + "/";

        String packageName = context.getPackageName();
        return root  + packageName + BACKUP_ROOT + "/";
    }

    /**
     * 获取文档 版本
     * @param context
     * @param docType
     * @return
     */
    public static DocumentVersion getDocumentVersion(Context context,int docType){

        File documentRoot = new File(getProgramDocument(context));

        File[] fileList = documentRoot.listFiles();

        if(fileList == null){
            return null;
        }

        for(int i = 0;i < fileList.length;i++){
            String fileName = fileList[i].getPath().substring(documentRoot.getPath().length());

            if(docType == URLConstant.DOC_GUIDE){
                if(fileName.contains(context.getString(R.string.help_guide))){
                    DocumentVersion documentVersion = new DocumentVersion();
                    int index_v = fileName.lastIndexOf("_v");
                    if(index_v <= 0){
                        return null;
                    }

                    int index_Main = index_v + 2;
                    int index_Slave = fileName.indexOf(".",index_Main) + 1;
                    int index_Code = fileName.indexOf(".",index_Slave) + 1;

                    String vMain = fileName.substring(index_Main,fileName.indexOf(".", index_Main));
                    String vSlave = fileName.substring(index_Slave,fileName.indexOf(".", index_Slave));
                    String vCode = fileName.substring(index_Code,fileName.indexOf(".", index_Code));

                    documentVersion.setMain(vMain);
                    documentVersion.setSlave(vSlave);
                    documentVersion.setCode(vCode);

                    return documentVersion;
                }
            }
            if(docType == URLConstant.DOC_MANUAL){
                if(fileName.contains(context.getString(R.string.help_manual))){
                    DocumentVersion documentVersion = new DocumentVersion();
                    int index_v = fileName.lastIndexOf("_v");
                    if(index_v <= 0){
                        return null;
                    }

                    int index_Main = index_v + 2;
                    int index_Slave = fileName.indexOf(".",index_Main) + 1;
                    int index_Code = fileName.indexOf(".",index_Slave) + 1;

                    String vMain = fileName.substring(index_Main,fileName.indexOf(".", index_Main));
                    String vSlave = fileName.substring(index_Slave,fileName.indexOf(".", index_Slave));
                    String vCode = fileName.substring(index_Code,fileName.indexOf(".", index_Code));
                    documentVersion.setMain(vMain);
                    documentVersion.setSlave(vSlave);
                    documentVersion.setCode(vCode);

                    return documentVersion;
                }
            }

        }

        return null;

    }

    public static void copyDefaulDocument(Context context){
        String AssetPath = AssetsDocument;
        String programRoot  = getProgramDocument(context);
        copyFiles(context,AssetPath,programRoot);

    }

    /**
     * @param path
     * @return
     */
    public static  void  getPathBinVer(Map<String,UpdateDB> versions, final String path){
        File file = new File(path);
        if (file.exists()) {
            File[] children = file.listFiles();

            ArrayList mBeans = new ArrayList<>();
            for (int i = 0; i < children.length; i++) {
                File child = children[i];

                File[] childFiles = child.listFiles();

                if (childFiles != null) {
                    for (int cIndex = 0; cIndex < childFiles.length; cIndex++) {
                        File superChild = childFiles[cIndex];

                        /**
                         * 获取版本
                         */
                        if (superChild.getName().toLowerCase().endsWith(".bin")) {
                            try {
                                String url =  superChild.getParentFile().getParent() + "/" + superChild.getParentFile().getName();

                                /**
                                 * 获取授权码
                                 */
                                String authCode = superChild.getName().substring(0,4);
                                url = url.substring(url.indexOf(PROGRAM) + PROGRAM.length());

                                int index_V = url.lastIndexOf("_v");

                                int index_P_Start = index_V + "_v".length();
                                int index_P_End = url.indexOf(".", index_P_Start);

                                int index_C_Start = index_P_End + ".".length();

                                String parentVStr = url.substring(index_P_Start, index_P_End);
                                String childVStr = url.substring(index_C_Start);

                                //程序名称
                                String fileName = url.substring(0, index_V);

                                fileName = fileName.replace("\\", "/");
                                if(fileName.startsWith("/")){
                                    fileName = fileName.substring(1);
                                }
                                //版本
                                VersionBean versionBean = new VersionBean();
                                versionBean.setProgramName(fileName);
                                versionBean.setParentVersion(parentVStr);
                                versionBean.setChildVersion(childVStr);

                                UpdateDB updateDB;

                                if(versions.containsKey(fileName)){
                                    updateDB = versions.get(fileName);
                                }
                                else{
                                    updateDB = new UpdateDB();
                                }

                                updateDB.setAuthCode(authCode);
                                updateDB.setProgramName(fileName);
                                updateDB.addUpdateVersion(versionBean);

                                //将版本添加到相同的程序列表中
                                versions.put(fileName,updateDB);
                            } catch (Exception e) {
                                // TODO: handle exception
                            }
                        }
                    }
                }
                if (child.isDirectory()) {
                    /**
                     * 遍历 目录中存在的版本
                     */
                    getPathBinVer(versions,child.getAbsolutePath());
                }

            }
        }
    }


    public static VersionBean getVersionBean(String url){
        int index = url.indexOf(PROGRAM_ROOT);
        if(url.indexOf(PROGRAM_ROOT) >= 0){
            index = index + PROGRAM_ROOT.length();
        }
        else{
            index = 0;
        }

        url = url.substring(index);
        VersionBean bean = null;
        if(url.contains("_v")) {
            int index_V = url.lastIndexOf("_v");

            int index_P_Start = index_V + "_v".length();
            int index_P_End = url.indexOf(".", index_P_Start);

            int index_C_Start = index_P_End + ".".length();
            int index_C_End = url.lastIndexOf(".bin");

            String parentVStr = url.substring(index_P_Start, index_P_End);
            String childVStr = url.substring(index_C_Start, index_C_End);

            String fileName = url.substring(0, url.lastIndexOf("/"));
            int vParent = Integer.parseInt(parentVStr);
            int vChild = Integer.parseInt(childVStr);

            bean = new VersionBean();
            bean.setParentVersion(parentVStr);
            bean.setProgramName(fileName);
            bean.setChildVersion(childVStr);
        }else {
            bean = new VersionBean();
            bean.setProgramName(url);

        }


        return bean;
    }

    public static String getFileSizeStr(long size){
        String sizeStr = size + "B";

        if(size > 1000){
            sizeStr = getFileSizeKB(size);

            if(((float)size / 1024) > 1000){
                sizeStr = getFileSizeMB((float)size / 1024);

                if((((float)size / 1024)/1024) > 1000){
                    sizeStr = getFileSizeGB(((float)size / 1024)/1024) ;
                }
            }
        }
        return sizeStr;
    }

    public static String getFileSizeKB(long sizeB){
        DecimalFormat df = new DecimalFormat("#.##");
        String sizeKB = df.format(sizeB /1024) + "KB";

        return sizeKB;
    }

    public static String getFileSizeMB(double sizeKB){
        DecimalFormat df = new DecimalFormat("#.##");
        String sizeMB = df.format(sizeKB /1024) + "MB";

        return sizeMB;
    }

    public static String getFileSizeGB(double sizeMB){
        DecimalFormat df = new DecimalFormat("#.##");
        String sizeGB = df.format(sizeMB /1024) + "GB";

        return sizeGB;
    }

    //拷贝assets文件夹到 外部存储 ProgramWest文件夹下面
    public static void copyAssetsToEnvir(Context context) {
        String AssetPath = "ProgramWest";
        String programRoot  = getProgram(context);
        copyFiles(context,AssetPath,programRoot);
    }

    public static void copyAssetsBinToDebugRoot(Context context){
        String AssetPath = "BIN";
        String debugRoot  = getDebugRoot(context);
        copyFiles(context,AssetPath,debugRoot);
    }

    private static void copyFiles(Context context, String assetPath, String programRoot) {
        String[] filenames = null;
        FileOutputStream fos = null;
        InputStream fis = null;
        try {
            filenames = context.getAssets().list(assetPath);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        if (filenames.length > 0 ) //目录
        {
            File file = new File(programRoot);
            if(!file.exists()){
                file.getParentFile().mkdirs();
            }
            for (String fileName : filenames){ //递归调用复制文件
                String inDir = assetPath ;
                String outDir = programRoot +File.separator;
                if (!assetPath.equals("")){ //空目录
                    inDir = inDir + File.separator;
                }
                copyFiles(context,inDir + fileName,outDir + fileName);
            }

        }else {//文件
            File fileOut = new File(programRoot);
            if (!fileOut.exists()){
                fileOut.getParentFile().mkdirs();
            }
            try {
                fos = new FileOutputStream(fileOut);
                fis = context.getAssets().open(assetPath);
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = fis.read(buffer)) != -1){
                    fos.write(buffer,0,len);
                }
                fos.flush();
                fis.close();
                fos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                try {
                if (fis != null){
                    fis.close();
                }
                if (fos != null){
                    fos.close();
                }
                }catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    //读取文件中的功能呢数据
    public static String readIniData(File fileIni) {
        String lineData = "";
        InputStreamReader isr = null;
        if (fileIni.isFile() && fileIni.exists()){
            try {
                if (fileIni.getPath().toLowerCase().contains(".ini")){
                    isr = new InputStreamReader(new FileInputStream(fileIni),"GBK");
                }else {
                    isr = new InputStreamReader(new FileInputStream(fileIni), "utf-8");
                }
                BufferedReader buffer = new BufferedReader(isr);
                String line = null;
                while ( (line = buffer.readLine()) != null){
                    lineData += line + System.lineSeparator();
                }
                buffer.close();
                isr.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lineData;
    }

    //读取文件中的对bin文件描述的文件数据
    public static String readBinFileDecsData(File fileIni) {
        String lineData = "";
        InputStreamReader isr = null;
        if (fileIni.isFile() && fileIni.exists()){
            try {
                if (fileIni.getPath().toLowerCase().contains(".txt") ){
                    isr = new InputStreamReader(new FileInputStream(fileIni),"GBK");
                }
                BufferedReader buffer = new BufferedReader(isr);
                String line = null;
                while ( (line = buffer.readLine()) != null){
                    lineData += line + System.lineSeparator(); ;
                }
                buffer.close();
                isr.close();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return lineData;
    }



    /**
     * 将升级程序的bin文件的16进制和存放在一个文件中
     * @param context
     * @param filePath
     * @param hex
     */
    public static void writeHexData(Context context,String filePath,String hex){
        String path = getProramDownloadRoot(context);
        filePath = filePath.substring(0,filePath.lastIndexOf("/"));
        File file = new File(path+filePath,DOWNLOAD_CRC_NAME);
        if (!file.exists()){
            file.getParentFile().mkdirs();
        }else {
            file.delete();
        }

        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(file, true);
            byte[] bytes = HexDump.hexStringToByteArray(hex);
            fout.write(bytes);
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] readHexData(Context context,String filePath){
        String path = getProramDownloadRoot(context);
        filePath = filePath.substring(0,filePath.lastIndexOf("/"));
        File file ;
        if (filePath.contains(path)){
             file = new File(filePath,DOWNLOAD_CRC_NAME);
        }else {
            file = new File(path + filePath, DOWNLOAD_CRC_NAME);
        }
        byte[] data = new byte[2];
        if (file.exists()){
            try {
                FileInputStream isr = new FileInputStream(file);
                //BufferedReader buffer = new BufferedReader(isr);

                isr.read(data);
                String line = "";
               /* while ( (line = buffer.readLine()) != null){
                    //data = line;
                }*/
                //buffer.close();
                isr.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return data;
    }

    /**
     * 读取bin文件的数据和的十六进制
     * @param
     * @param mContext
     *@param file  @return
     */
    public static String readBinFile(Context mContext, File file){
        String hexString = "";
        if (file.exists()){
            try {
                FileInputStream isr = new FileInputStream(file);
                byte[] buff = new byte[256];
                int offset;
                long sum = 0;
                while ( (offset = isr.read(buff)) != -1){
                    for (int i = 0; i < offset ; i++) {
                        int index = buff[i];
                        if (index < 0){
                            index += 256;
                        }
                        sum += index;
                    }
                    if (offset < 256) {
                        for (int k = offset; k < 256; k++) {
                            sum += 0xFF;
                        }
                    }
                }
                byte[] b = new byte[2];
                byte last = (byte) sum;
                byte first = (byte) (sum >> 8);
                b[0] =  first;
                b[1] = last;
                hexString = HexUtil.toHexString(b);
                hexString = hexString.replace(" ","");
                isr.close();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return hexString;
    }

    //删除app安装包
    public static void removeAppFile(Context mContext){
        String path = getProgramDocument(mContext) + mContext.getResources().getString(R.string.app_Name)+".apk";
        File file = new File(path);
        if (file.exists()){
            file.delete();
        }
    }

    /**
     * 删除app中的部分文件
     * @param path
     */
    public static  void deleteApp(String path,Context mContext){
        File fileRoot = new File(path);
        if (fileRoot.exists()){
            File[] fileRoots = fileRoot.listFiles();

            if (fileRoots.length > 0 ) {
                for (int i = 0; i < fileRoots.length; i++) {
                    String str = fileRoots[i].getPath();
                    str = str.substring(str.indexOf(mContext.getPackageName())+mContext.getPackageName().length()+1);
                    if (str.contains("/")) {
                        str = str.substring(0, str.indexOf("/"));
                    }
                    if (str.equals("Program") || str.equals("ProgramDownload") || str.equals("ProgramReport")) {
                        File child = fileRoots[i];
                        if (child.isDirectory()) {
                            deleteApp(child.getAbsolutePath(),mContext);
                        }
                        if (child.exists()){
                            child.delete();
                            File[] parentFiles = child.getParentFile().listFiles();
                            if (parentFiles.length == 0 ){
                                child.getParentFile().delete();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 删除程序
     * @param context
     * @param program
     */
    public static void deleteProgram(Context context,String program){
        String path = getProgramRoot(context) + program;

        File dir = new File(path);
        String programName = dir.getName();

        File parent = dir.getParentFile();

        File[] fileList = parent.listFiles();
        for(int i = 0;i < fileList.length;i++){
            String name = fileList[i].getName();
            int index_V = name.toLowerCase().lastIndexOf("_v");
            if(index_V > 0) {
                name = name.substring(0, index_V);
                if(name.equals(programName)){
                    File[] files = fileList[i].listFiles();
                    if(files != null){
                        for(int index = 0;index < files.length;index++){
                            files[index].delete();
                        }
                    }
                    fileList[i].delete();
                }
            }
        }
        fileList = parent.listFiles();
        while(fileList == null || fileList.length == 0){
            parent = parent.getParentFile();
            parent.delete();
        }

    }


    /**
     * 删除app中的视屏
     * @param mContext
     */
    public static void deleteAppVideo(Context mContext) {
        HttpProxyCacheServer proxyCacheServer = new HttpProxyCacheServer.Builder(mContext).build();
        File file = proxyCacheServer.getCacheFilePath(mContext);
        if (file.exists()) {
            deleteAppVideoFile(file.getPath());
        }

    }

    private static void deleteAppVideoFile(String path) {
        File file = new File(path);
        File[] files = file.listFiles();
        if (files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                File child = files[i];
                if (child.isDirectory()) {
                    deleteAppVideoFile(child.getAbsolutePath());
                }
                if (child.exists()) {
                    child.delete();
                    File[] parentFiles = child.getParentFile().listFiles();
                    if (parentFiles.length == 0) {
                        child.getParentFile().delete();
                    }
                }
            }
        }


    }

}