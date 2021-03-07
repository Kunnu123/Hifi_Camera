package com.hificamera.thecamhi.utils;

import android.content.Context;

import com.hificamera.thecamhi.bean.HiDataValue;
import com.meizu.cloud.pushsdk.PushManager;
import com.vivo.push.PushClient;
import com.xiaomi.mipush.sdk.MiPushClient;


public class TokenUtils {
    public static String getPushName(Context context) {

        if (SystemUtils.isZh(context)) {
            if (SystemUtils.isOPPOMoblie(context) && com.coloros.mcssdk.PushManager.isSupportPush(context)) {
                return "xinge";//oppo
            } else if (SystemUtils.isVIVOMoblie(context)) {
                return "xinge";//vivo
            } else if (SystemUtils.isXiaomiMoblie(context)) {
                return "xiaomi";
            } else if (SystemUtils.isHuaweiMoblie(context)) {
                return "huawei";
            } else if (SystemUtils.isMEIZUMoblie(context)) {
                return "meizu";
            } else {
                return "jiguang";
            }

        } else {
            return "fcm";
        }
    }

    public static String getToken(Context context) {
        String token = "";
        if (SystemUtils.isZh(context)) {
            if (SystemUtils.isOPPOMoblie(context) && com.coloros.mcssdk.PushManager.isSupportPush(context)) {
                token =com.coloros.mcssdk.PushManager.getInstance().getRegisterID();
            } else if (SystemUtils.isVIVOMoblie(context)) {
                token = PushClient.getInstance(context).getRegId();
            } else if (SystemUtils.isXiaomiMoblie(context)) {
                token = MiPushClient.getRegId(context);
            } else if (SystemUtils.isHuaweiMoblie(context)) {
                token = HiDataValue.HuaWeiToken;
            } else if (SystemUtils.isMEIZUMoblie(context)) {
                token = PushManager.getPushId(context);
            } else {
                token = HiDataValue.XGToken;
            }
        } else {
            token = HiDataValue.FcmToken;
        }
        return token;
    }
    public static String getPhoneName(Context context) {
        if (SystemUtils.isOPPOMoblie(context) && com.coloros.mcssdk.PushManager.isSupportPush(context)) {
            return "oppo";
        } else if (SystemUtils.isVIVOMoblie(context)) {
            return "vivo";
        } else if (SystemUtils.isXiaomiMoblie(context)) {
            return "xiaomi";
        } else if (SystemUtils.isHuaweiMoblie(context)) {
            return "huawei";
        } else if (SystemUtils.isMEIZUMoblie(context)) {
            return "meizu";
        } else {
            return "";
        }
    }
}
