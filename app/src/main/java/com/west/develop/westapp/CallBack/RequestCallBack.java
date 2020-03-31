package com.west.develop.westapp.CallBack;

import java.io.File;

/**
 * Created by Develop12 on 2017/12/14.
 */

public interface RequestCallBack {
    void onResult(boolean success, File file);
    void onLoading(long total, long current);
}
