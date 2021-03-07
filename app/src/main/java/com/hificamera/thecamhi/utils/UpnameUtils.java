package com.hificamera.thecamhi.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hichip.push.HiPushSDK;
import com.hificamera.thecamhi.bean.CamHiDefines;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.MyCamera;

public  class UpnameUtils {
    private static Context c;
    private static String upname;
    private static MyCamera camera;
    public static void upName(String strNikename, Context context, MyCamera mCamera) {
        c = context;
        upname = strNikename;
        camera=mCamera;
        String bindtoken = "";
        // String jgToken = JPushInterface.getRegistrationID(mContext);
        if (SystemUtils.isZh(context)) {
            if (SystemUtils.isOPPOMoblie(context) || SystemUtils.isVIVOMoblie(context)) {
                bindtoken = HiDataValue.XGToken;
            } else {
                bindtoken = HiDataValue.NewPushToken;
            }

        } else {
            bindtoken = HiDataValue.FcmToken;
        }
        if (!TextUtils.isEmpty(bindtoken)) {
            HiPushSDK push = new HiPushSDK(context, bindtoken + "&notify=1", mCamera.getUid(), HiDataValue.company, TokenUtils.getPushName(context), mCamera.getBindNikeName(), pushResult, getPushAddressByUID(mCamera));
            push.UpdateName(mCamera.getPushState(), strNikename);
        } else {
            Log.e("upname", "id==" + mCamera.getPushState());
            if (!TextUtils.isEmpty(HiDataValue.XGToken)) {
                HiPushSDK push = new HiPushSDK(context, HiDataValue.XGToken + "&notify=1", mCamera.getUid(), HiDataValue.company, TokenUtils.getPushName(context), mCamera.getBindNikeName(), pushResult, getPushAddressByUID(mCamera));
                push.UpdateName(mCamera.getPushState(), strNikename);
            } else {
                Log.e("upname", "all null");
            }

        }
    }

    private static HiPushSDK.OnPushResult pushResult = (subID, type, result) -> {
        if (type == HiPushSDK.PUSH_TYPE_UPNAME) {
            if (HiPushSDK.PUSH_RESULT_SUCESS == result) {
                Log.e("upname", "name up su");
                SharePreUtils.putString("upName",c,camera.getUid()+"upName",upname);

            } else if (HiPushSDK.PUSH_RESULT_FAIL == result) {
                Log.e("upname", "name up fail");

            }
        }
    };


    private static String getPushAddressByUID(MyCamera mCamera) {
        String pushAddress;
        if (mCamera.commandFunction.getAppCmdFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_GET)) {
            if (mCamera.handSubWTU()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122;
            } else if (mCamera.handSubXYZ()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173;
            } else if (mCamera.handSubAACC()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_AACC_148;
            } else if (mCamera.handSubSSAA()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_SSAA_161;
            } else {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_233;
            }
        } else {
            if (mCamera.handSubWTU()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122;
            } else if (mCamera.handSubAACC()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_AACC_148;
            } else if (mCamera.handSubSSAA()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_SSAA_161;
            } else {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_233;
            }
        }
        return pushAddress;
    }
}
