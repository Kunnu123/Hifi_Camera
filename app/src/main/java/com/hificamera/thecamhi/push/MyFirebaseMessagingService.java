package com.hificamera.thecamhi.push;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.utils.SharePreUtils;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);


        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
        }
    }

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        HiDataValue.FcmToken=s;
        SharePreUtils.putString("FcmToken", getApplicationContext(), "fcmtoken", s);

    }
}