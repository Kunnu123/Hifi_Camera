package com.hificamera.thecamhi.base;

import java.text.SimpleDateFormat;  
import java.util.Date;

public class MyDate {
    public static String getFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String date = format.format(new Date(System.currentTimeMillis()));
        return date;
    }

    public static String getDateEN() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date1 = format1.format(new Date(System.currentTimeMillis()));
        return date1;
    }

    public static String getDateEN2() {
        SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");
        String date1 = format1.format(new Date(System.currentTimeMillis()));
        return date1;
    }

}  