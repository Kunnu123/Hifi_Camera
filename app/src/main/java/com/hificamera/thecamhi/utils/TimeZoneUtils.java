package com.hificamera.thecamhi.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimeZoneUtils {
    public static String getCurrentTimeZone(boolean ext) {
        TimeZone tz = TimeZone.getDefault();
        float tim = (float) tz.getRawOffset() / (3600000.0f);
        String gmt = null;

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Date datad = cal.getTime();
        boolean daylightT = tz.inDaylightTime(datad);
        if (daylightT)
            tim += 1;
        if (ext) {
            if(tim>=0){
                gmt = "GMT+" + tim;
            }else {
                gmt = "GMT" + tim;
            }
            if(gmt.contains(".")){
                gmt=gmt.replace(".",":");
            }
            if(gmt.endsWith(":5")){//5.5-->5:30
                gmt=gmt.replace(":5",":30");
            }
        } else {
            gmt = tim + "";
        }
        return gmt;
    }
}
