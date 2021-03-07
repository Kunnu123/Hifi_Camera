package com.hificamera.thecamhi.base;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.hichip.base.HiLog;
import com.hichip.push.HiPushSDK;
import com.hificamera.R;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.utils.SharePreUtils;
import com.tencent.android.tpush.XGLocalMessage;
import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushConfig;
import com.tencent.android.tpush.XGPushManager;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

import org.json.JSONException;
import org.json.JSONObject;

import static com.hificamera.thecamhi.base.HiTools.checkAddressReNew;

@SuppressWarnings("unused")
public class PushMessageReceiver extends XGPushBaseReceiver {

    @Override
    public void onDeleteTagResult(Context arg0, int arg1, String arg2) {
    }

    @Override
    public void onNotifactionClickedResult(Context arg0, XGPushClickedResult arg1) {
    }

    @Override
    public void onNotifactionShowedResult(Context arg0, XGPushShowedResult arg1) {
    }

    @Override
    public void onRegisterResult(Context arg0, int arg1, XGPushRegisterResult arg2) {
    }

    @Override
    public void onSetTagResult(Context arg0, int arg1, String arg2) {
    }

    @Override
    public void onTextMessage(Context arg0, XGPushTextMessage arg1) {
        String key = arg1.getCustomContent();
        String uid = null;
        int type = 0;
        int time = 0;
        String rfType = null;
        String pushName = "";
        if (key != null) {
            try {
                JSONObject arrJson = new JSONObject(key);
                Log.e("onTextMessage==", arrJson.toString());
                String jsonc = arrJson.getString("content");
                if (arrJson.has("title")) {
                    pushName = arrJson.getString("title");
                }

                Log.e("onTextMessage==", "json=" + jsonc);
                Log.e("onTextMessage==", "pushName=" + pushName);
                JSONObject conJson = new JSONObject(jsonc);
                uid = conJson.getString("uid");
                type = conJson.getInt("type");
                if (type == 6) {
                    rfType = conJson.getString("rftype");
                }
                //time = conJson.getInt("time");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (uid == null)
                return;
            if (HiDataValue.CameraList.size() > 0) {
                return;
            }
            DatabaseManager db = new DatabaseManager(arg0);
            HiLog.e("==queryDeviceByUid==" + db.queryDeviceByUid(uid) + "");
            if (!db.queryDeviceByUid(uid)) {
                mContext = arg0;
                mUid = uid;
                int subId = SharePreUtils.getInt("subId", arg0, uid);
                String server = " ";

                if (handSubXYZ(uid)) {
                    server = HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173;
                } else if (handSubWTU(uid)) {
                    server = HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122;
                } else if (handSubAACC(uid)) {
                    server = HiDataValue.CAMERA_ALARM_ADDRESS_AACC_148;
                } else if (handSubSSAA(uid)) {
                    server = HiDataValue.CAMERA_ALARM_ADDRESS_SSAA_161;
                } else {
                    server = HiDataValue.CAMERA_ALARM_ADDRESS_233;
                }

                //                if (isDEAA(uid)) {
                //                   server = HiDataValue.CAMERA_ALARM_ADDRESS_DERICAM_148;
                //                } else if (isFDTAA(uid)) {
                //                    server = HiDataValue.CAMERA_ALARM_ADDRESS_FDT_221;
                //                } else if (handSubXYZ(uid)) {
                //                    server = HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173;
                //                } else if (handSubWTU(uid)) {
                //                    server = HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122;
                //                } else {
                //                    server = HiDataValue.CAMERA_ALARM_ADDRESS_233;
                //                }

                pushSDK = new HiPushSDK(XGPushConfig.getToken(arg0), uid, HiDataValue.company, pushResult, server);
                if ((handSubXYZ(uid) || handSubWTU(uid) || handSubP(uid))) {
                    checkAddressReNew(pushSDK, server, pushSDK.getPushServer());
                }
                HiLog.e("server==" + server + "  :::pushSdk address=" + pushSDK.getPushServer());
                HiLog.e(subId + "--" + XGPushConfig.getToken(arg0));
                if (subId > 0) {
                    pushSDK.unbind(subId);
                } else {
                    pushSDK.bind();
                }
                return;
            } else {
                int pushState = db.queryPushStateByUid(uid);
                if (pushState < 1) {
                    return;
                }

            }
            String[] strAlarmType = arg0.getResources().getStringArray(R.array.tips_alarm_list_array);
            XGLocalMessage local_msg = new XGLocalMessage();
            local_msg.setType(1);
            String msg = handType(arg0, type, rfType, strAlarmType);
            if (TextUtils.isEmpty(msg)) {
                return;
            }
            if (TextUtils.isEmpty(pushName)) {
                local_msg.setTitle(uid);
                // if (type >= 0) local_msg.setContent(type==6?strAlarmType[4]:strAlarmType[type]);
                if (!TextUtils.isEmpty(msg)) {
                    local_msg.setContent(msg);
                }
            } else {
                local_msg.setTitle(pushName);
                local_msg.setContent(msg + "  " + uid);

            }
            if (db != null) {
                db.updateAlarmStateByUID(uid, 1);
            }
            XGPushManager.addLocalNotification(arg0, local_msg);
        }

    }

    private String handType(Context context, int type, String rfType, String[] strAlarmType) {
        String msg = null;
        switch (type) {
            case 0:
                msg = strAlarmType[0];
                break;
            case 1:
                msg = strAlarmType[1];
                break;
            case 2:
                msg = strAlarmType[2];
                break;
            case 3:
                msg = strAlarmType[3];
                break;
            case 6:
                if ("key2".equals(rfType)) {
                    msg = context.getResources().getString(R.string.alarm_sos);
                } else if ("key3".equals(rfType)) {
                    msg = context.getResources().getString(R.string.alarm_ring);
                } else if ("door".equals(rfType)) {
                    msg = context.getResources().getString(R.string.alarm_door);
                } else if ("infra".equals(rfType)) {
                    msg = context.getResources().getString(R.string.alarm_infra);
                } else if ("beep".equals(rfType)) {
                    msg = context.getResources().getString(R.string.alarm_doorbell);
                } else if ("fire".equals(rfType)) {
                    msg = context.getResources().getString(R.string.alarm_smoke);
                } else if ("gas".equals(rfType)) {
                    msg = context.getResources().getString(R.string.alarm_gas);
                } else if ("socket".equals(rfType)) {
                    msg = context.getResources().getString(R.string.alarm_socket);
                } else if ("temp".equals(rfType)) {
                    msg = context.getResources().getString(R.string.alarm_temp);
                } else if ("humi".equals(rfType)) {
                    msg = context.getResources().getString(R.string.alarm_humi);
                }
                break;
            case 12:
                msg = strAlarmType[4];
                break;
        }
        return msg;
    }


    private HiPushSDK pushSDK;
    private Context mContext;
    private String mUid;

    private HiPushSDK.OnPushResult pushResult = new HiPushSDK.OnPushResult() {

        @Override
        public void pushBindResult(int subID, int type, int result) {
            if (type == HiPushSDK.PUSH_TYPE_BIND) {
                HiLog.e("==bindResult1==" + subID + "");
                if (HiPushSDK.PUSH_RESULT_SUCESS == result) {
                    if (pushSDK != null)
                        pushSDK.unbind(subID);
                    SharePreUtils.removeKey("subId", mContext, mUid);
                } else if (HiPushSDK.PUSH_RESULT_FAIL == result || HiPushSDK.PUSH_RESULT_NULL_TOKEN == result) {
                }
            } else if (type == HiPushSDK.PUSH_TYPE_UNBIND) {
                HiLog.e("==bindResult2==" + subID + "");
                if (HiPushSDK.PUSH_RESULT_SUCESS == result) {
                    SharePreUtils.removeKey("subId", mContext, mUid);
                } else if (HiPushSDK.PUSH_RESULT_FAIL == result) {
                }
            }

        }
    };

    @Override
    public void onUnregisterResult(Context arg0, int arg1) {
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

    public boolean isFDTAA(String uid) {
        if (!TextUtils.isEmpty(uid) && uid.length() > 4) {
            String subUid = uid.substring(0, 4);
            if ("FDTAA".equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDEAA(String uid) {
        if (!TextUtils.isEmpty(uid) && uid.length() > 4) {
            String subUid = uid.substring(0, 4);
            if ("DEAA".equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    public boolean handSubAACC(String uid) {
        String subUid = uid.substring(0, 4);
        for (String str : HiDataValue.SUBUID_AACC) {
            if (str.equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    public boolean handSubSSAA(String uid) {
        String subUid = uid.substring(0, 4);
        for (String str : HiDataValue.SUBUID_SSAA) {
            if (str.equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }
}