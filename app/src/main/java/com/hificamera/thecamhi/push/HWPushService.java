package com.hificamera.thecamhi.push;


import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.utils.SharePreUtils;
import com.huawei.hms.push.HmsMessageService;
import com.huawei.hms.push.RemoteMessage;

public class HWPushService extends HmsMessageService {
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        HiDataValue.HuaWeiToken = s;
        SharePreUtils.putString("NewPushToken", getApplicationContext(), "pushtoken", s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
//
//        if (remoteMessage.getData().length() > 0) {
//           HiLog.e("==push", "Message data payload: " + remoteMessage.getData());
//        }
//        if (remoteMessage.getNotification() != null) {
//           HiLog.e("==push", "Message Notification title: " + remoteMessage.getNotification().getTitle());
//           HiLog.e("==push", "Message Notification Body: " + remoteMessage.getNotification().getBody());
//        }
    }

}
