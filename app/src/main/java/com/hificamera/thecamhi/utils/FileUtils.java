package com.hificamera.thecamhi.utils;

import android.text.TextUtils;

public class FileUtils {
    public static boolean isCamHiLoadFile(String file) {
        if (TextUtils.isEmpty(file))
            return false;
        return file.endsWith(".mp4")
                || file.endsWith(".avi")
                || file.endsWith(".h264")
                || file.endsWith(".h265");
    }

}
