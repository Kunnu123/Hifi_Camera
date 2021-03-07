package com.hificamera.thecamhi.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;


import com.hificamera.thecamhi.bean.HiDataValue;

public class JPushEventReceiver extends BroadcastReceiver {
    private static final String TAG = "==push";




    @Override
    public void onReceive(Context context, Intent intent) {
        //  this.mContext = context;
        try {
            Bundle bundle = intent.getExtras();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    public boolean handSubXYZ(String uid) {
        String subUid = uid.substring(0, 4);
        for (String str : HiDataValue.SUBUID) {
            if (str.equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    public boolean handSubP(String uid) {
        String subUid = uid.substring(0, 4);
        for (String str : HiDataValue.SUBUID_P) {
            if (str.equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    public boolean handSubWTU(String uid) {
        String subUid = uid.substring(0, 4);
        for (String str : HiDataValue.SUBUID_WTU) {
            if (str.equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }


}
