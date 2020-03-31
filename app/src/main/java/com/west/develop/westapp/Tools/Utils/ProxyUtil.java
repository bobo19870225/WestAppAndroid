package com.west.develop.westapp.Tools.Utils;

import com.west.develop.westapp.videocache.file.FileNameGenerator;

/**
 * Created by Develop12 on 2018/1/26.
 * 自定义视屏文件的名称
 */


public class ProxyUtil implements FileNameGenerator {
    @Override
    public String generate(String url) {
        return url.substring(url.indexOf(FileUtil.PROGRAM_ROOT )+ FileUtil.PROGRAM_ROOT.length());
    }
}
