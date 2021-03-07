package com.hificamera.thecamhi.zxing.utils;

import java.io.File;

import android.content.Context;
import android.os.Environment;

public class FileUtils {
	public static String getCacheDir(Context context) {

		File cacheDir = context.getCacheDir();
		String cachePath = cacheDir.getPath();
		return cachePath;
	}
	public static String getFileRoot(Context context) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {  
            File external = context.getExternalFilesDir(null);  
            if (external != null) {  
                return external.getAbsolutePath();  
            }  
        }  
        return context.getFilesDir().getAbsolutePath();  
    } 
	 
    public static String getFileAllPath(String fileName){
    	String filePath = Environment.getExternalStorageDirectory().getPath() + File.separator + fileName;
    	return filePath;
    }
	
}
