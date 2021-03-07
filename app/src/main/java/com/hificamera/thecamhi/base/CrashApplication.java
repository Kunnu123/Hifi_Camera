package com.hificamera.thecamhi.base;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.coloros.mcssdk.callback.PushCallback;
import com.coloros.mcssdk.mode.SubscribeResult;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import com.hichip.base.HiLog;
import com.hichip.sdk.HiManageLib;
import com.hificamera.thecamhi.bean.HiDataValue;

import com.hificamera.thecamhi.utils.SharePreUtils;


import com.hificamera.thecamhi.utils.SystemUtils;
import com.meizu.cloud.pushsdk.PushManager;


import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;

import com.xiaomi.mipush.sdk.MiPushClient;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.text.TextUtils;


public class CrashApplication extends Application {
    private static CrashApplication app;
    private static final String meizuAppKey = "4bf36fb6bb3a4f9a92c48aad87d44392";
    private static final String meizuAppId = "116039";
    private static final String xiaomiAppKey = "5241733513228";
    private static final String xiaomiAppId = "2882303761517335228";
    private static final String oppoAppKey = "9xh1qpv9nuO00ksGkWCg8w8s4";
    private static final String oppoAppSecret = "cC6Dc8AB28355ae41C9D939c84da19dB";

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        if (getPackageName().equals(getCurrentProcessName())) {

            initSDK();
            initPush();
        }
    }

    private void initPush() {
        initXGPushSDK();
        if (SystemUtils.isZh(this)) {
            if (SystemUtils.isVIVOMoblie(this)) {
               // initJiGuangSDK();
                //                PushClient.getInstance(this).initialize();
                //                PushClient.getInstance(this).turnOnPush(new IPushActionListener() {
                //                    @Override
                //                    public void onStateChanged(int i) {
                //                    }
                //                });
            } else if (SystemUtils.isMEIZUMoblie(this)) {//魅族
                PushManager.register(this, meizuAppId, meizuAppKey);
            } else if (SystemUtils.isXiaomiMoblie(this)) {//小米
                MiPushClient.registerPush(this, xiaomiAppId, xiaomiAppKey);
            }
            else if (SystemUtils.isOPPOMoblie(this)
                //  && com.coloros.mcssdk.PushManager.isSupportPush(this)
                    ) {
                //initOppoPush();
               // initJiGuangSDK();
            }

        } else {
            FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                @Override
                public void onComplete(@NonNull Task<InstanceIdResult> task) {
                    if (!task.isSuccessful()) {
                        HiLog.e("getInstanceId failed" + task.getException());
                        return;
                    }
                    // Get new Instance ID token
                    String token = task.getResult().getToken();
                    HiDataValue.FcmToken = token;
                    SharePreUtils.putString("FcmToken", getApplicationContext(), "fcmtoken", token);
                }
            });
        }
    }

    private void initOppoPush() {
        com.coloros.mcssdk.PushManager.getInstance().register(this, oppoAppKey, oppoAppSecret, new PushCallback() {
            @Override
            public void onRegister(int i, String s) {
                HiDataValue.OppoToken = s;
                SharePreUtils.putString("NewPushToken", getApplicationContext(), "pushtoken", s);
            }

            @Override
            public void onUnRegister(int i) {

            }

            @Override
            public void onGetAliases(int i, List<SubscribeResult> list) {

            }

            @Override
            public void onSetAliases(int i, List<SubscribeResult> list) {

            }

            @Override
            public void onUnsetAliases(int i, List<SubscribeResult> list) {

            }

            @Override
            public void onSetUserAccounts(int i, List<SubscribeResult> list) {

            }

            @Override
            public void onUnsetUserAccounts(int i, List<SubscribeResult> list) {

            }

            @Override
            public void onGetUserAccounts(int i, List<SubscribeResult> list) {

            }

            @Override
            public void onSetTags(int i, List<SubscribeResult> list) {

            }

            @Override
            public void onUnsetTags(int i, List<SubscribeResult> list) {

            }

            @Override
            public void onGetTags(int i, List<SubscribeResult> list) {

            }

            @Override
            public void onGetPushStatus(int i, int i1) {

            }

            @Override
            public void onSetPushTime(int i, String s) {

            }

            @Override
            public void onGetNotificationStatus(int i, int i1) {

            }
        });
    }

    private void initSDK() {
        HiManageLib hiManageLib = new HiManageLib();
        //        HiChipSDK.init(new HiChipSDK.HiChipInitCallback() {
        //            @Override
        //            public void onSuccess(int syhandle, int xqhandle) {
        //                if (syhandle >= 0 && xqhandle >= 0) {
        //                }
        //            }
        //
        //            @Override
        //            public void onFali(int i, int i1) {
        //            }
        //        });

    }

    private void initJiGuangSDK() {
        //JPushInterface.setDebugMode(false);
        //JPushInterface.init(getApplicationContext());
    }

    private String getCurrentProcessName() {
        String currentProcName = "";
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                currentProcName = processInfo.processName;
                break;
            }
        }
        return currentProcName;
    }

    public static synchronized CrashApplication getInstance() {
        return app;
    }


    private static String getProcessName(int pid) {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/" + pid + "/cmdline"));
            String processName = reader.readLine();
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim();
            }
            return processName;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void initXGPushSDK() {
        XGPushManager.registerPush(this, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                String token = (String) data;
                String localToken = SharePreUtils.getString("XGToken", getApplicationContext(), "xgtoken");
                if (!TextUtils.isEmpty(token) && TextUtils.isEmpty(localToken) && !token.equalsIgnoreCase(localToken)) {
                    SharePreUtils.putString("XGToken", getApplicationContext(), "xgtoken", token);
                }
                if (!TextUtils.isEmpty(token)) {
                    HiDataValue.XGToken = token;
                }
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
            }
        });
        HiDataValue.XGToken = SharePreUtils.getString("XGToken", this, "xgtoken");
        if (TextUtils.isEmpty(HiDataValue.XGToken)) {
            HiDataValue.XGToken = XGPushConfig.getToken(this);
        }
    }
}









