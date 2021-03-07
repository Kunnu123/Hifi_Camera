package com.hificamera.thecamhi.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

public class SsidUtils {
    public static String getSSID(Context context) {
        String currentSsid = "";
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || Build.VERSION.SDK_INT >= 28) {
            WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            assert mWifiManager != null;
            WifiInfo info = mWifiManager.getConnectionInfo();
            if (info != null) {
                String systemSsid = info.getSSID();
                if (!TextUtils.isEmpty(systemSsid)) {
                    if (systemSsid.startsWith("\"") && systemSsid.endsWith("\"") && systemSsid.length() >= 3) {
                        currentSsid = systemSsid.substring(1, systemSsid.length() - 1);
                    } else {
                        currentSsid = info.getSSID().replace("\"", "");
                    }
                }
            }

        } else
        // if (Build.VERSION.SDK_INT == Build.VERSION_CODES.O_MR1)
        {
            ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connManager != null;
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo != null) {
                if (networkInfo.isConnected()) {
                    if (networkInfo.getExtraInfo() != null) {
                        String systemSsid = networkInfo.getExtraInfo();
                        if (!TextUtils.isEmpty(systemSsid)) {
                            if (systemSsid.startsWith("\"") && systemSsid.endsWith("\"") && systemSsid.length() >= 3) {
                                currentSsid = systemSsid.substring(1, systemSsid.length() - 1);
                            } else {
                                currentSsid = networkInfo.getExtraInfo().replace("\"", "");
                            }
                        }
                    }
                }
            }
        }
        return currentSsid;
    }
}
