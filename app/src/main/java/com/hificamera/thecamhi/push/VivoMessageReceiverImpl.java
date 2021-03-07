package com.hificamera.thecamhi.push;

import android.content.Context;

import com.hichip.base.HiLog;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.utils.SharePreUtils;
import com.vivo.push.model.UPSNotificationMessage;
import com.vivo.push.sdk.OpenClientPushMessageReceiver;

public class VivoMessageReceiverImpl extends OpenClientPushMessageReceiver {
    @Override
    public void onNotificationMessageClicked(Context context, UPSNotificationMessage upsNotificationMessage) {
       HiLog.e("==push"+"vivo:"+upsNotificationMessage.toString());
    }

    @Override
    public void onReceiveRegId(Context context, String s) {
        HiDataValue.VivoToken = s;
       HiLog.e("==push"+ "onReceiveRegId: " + s);
        SharePreUtils.putString("NewPushToken", context, "pushtoken", s);

    }

}
