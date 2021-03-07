package com.hificamera.thecamhi.bean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.hichip.control.HiCamera;
import com.hichip.push.HiPushSDK;
import com.hificamera.thecamhi.base.HiToast;
import com.hificamera.thecamhi.base.HiTools;
import com.hificamera.thecamhi.utils.SystemUtils;
import com.hificamera.thecamhi.utils.TokenUtils;
import com.hichip.tools.Packet;
import com.hificamera.thecamhi.base.CommandFunction;
import com.hificamera.thecamhi.base.DatabaseManager;
import com.hificamera.thecamhi.utils.SharePreUtils;


public class MyCamera extends HiCamera {
    private String nikeName = "";
    private int videoQuality = HiDataValue.DEFAULT_VIDEO_QUALITY;
    private int alarmState = HiDataValue.DEFAULT_ALARM_STATE;
    private int pushState = HiDataValue.DEFAULT_PUSH_STATE;
    private boolean hasSummerTimer;
    private boolean isFirstLogin = true;
    private byte[] bmpBuffer = null;
    public Bitmap snapshot = null;
    private long lastAlarmTime;
    private boolean isSetValueWithoutSave = false;
    private int style;
    private String serverData;
    public int isSystemState = 0;
    public boolean alarmLog = false;
    public int mInstallMode = 0;
    public boolean isFirst = false;
    public boolean isChecked;
    public boolean isWallMounted = false;
    public String uid;
    private Context mContext;
    public int u32Resolution = 0;
    public boolean mIsReceived_4179 = false;
    public CommandFunction commandFunction;
    public boolean isIngenic = false;
    public boolean isFirstAdd = false;
    public boolean isHsEV = false;

    private boolean isErrorUID = false;


    private boolean updateing = false;

    public MyCamera(Context context, String nikename, String uid, String username, String password) {
        super(context, uid, username, password);
        this.nikeName = nikename;
        this.uid = uid;
        this.mContext = context;
        commandFunction = new CommandFunction();
    }

    public boolean isAlarmLog() {
        return alarmLog;
    }

    public void setAlarmLog(boolean alarmLog) {
        this.alarmLog = alarmLog;
    }

    public void saveInDatabase(Context context) {
        DatabaseManager db = new DatabaseManager(context);
        db.addDevice(nikeName, getUid(), getUsername(), getPassword(), videoQuality, alarmState, pushState);
    }

    public void setSummerTimer(boolean hasSummerTimer) {
        this.hasSummerTimer = hasSummerTimer;
    }

    public boolean getSummerTimer() {
        return this.hasSummerTimer;
    }

    public void setServerData(String serverData) {
        this.serverData = serverData;
    }

    public String getServerData() {
        return this.serverData;
    }

    public void saveInCameraList() {
        if (!HiDataValue.CameraList.contains(this)) {
            HiDataValue.CameraList.add(this);
        }
    }

    public void deleteInCameraList() {
        HiDataValue.CameraList.remove(this);
        this.unregisterIOSessionListener();
        this.unregisterDownloadListener();
        this.unregisterPlayStateListener();
        this.unregisterYUVDataListener();
        snapshot = null;
    }

    public long getLastAlarmTime() {
        return lastAlarmTime;
    }

    public void setLastAlarmTime(long lastAlarmTime) {
        this.lastAlarmTime = lastAlarmTime;
    }

    public void updateInDatabase(Context context) {
        DatabaseManager db = new DatabaseManager(context);
        db.updateDeviceByDBID(nikeName, getUid(), getUsername(), getPassword(), videoQuality, HiDataValue.DEFAULT_ALARM_STATE, pushState, getServerData());
        isSetValueWithoutSave = false;
    }

    public void updateServerInDatabase(Context context) {
        DatabaseManager db = new DatabaseManager(context);
        db.updateServerByUID(getUid(), getServerData());

        isSetValueWithoutSave = false;
    }

    public void deleteInDatabase(Context context) {
        DatabaseManager db = new DatabaseManager(context);
        db.removeDeviceByUID(this.getUid());
        db.removeDeviceAlartEvent(this.getUid());
    }

    public int getAlarmState() {
        return alarmState;
    }

    public void setAlarmState(int alarmState) {
        this.alarmState = alarmState;
    }

    public int getPushState() {
        return pushState;
    }

    public void setPushState(int pushState) {
        this.pushState = pushState;
    }

    public void setStyle(int style) {
        this.style = style;
    }

    public int getStyle() {

        return style;
    }

    public int getVideoQuality() {
        return videoQuality;
    }

    public void setVideoQuality(int videoQuality) {
        this.videoQuality = videoQuality;
    }

    public String getNikeName() {
        return nikeName;
    }
    public String getBindNikeName() {
        if(nikeName.length()>31){
            return nikeName.substring(0,31);
        }
        return nikeName;
    }

    public void setNikeName(String nikeName) {
        this.nikeName = nikeName;
    }

    private int curbmpPos = 0;

    public boolean reciveBmpBuffer(byte[] byt) {
        if (byt.length < 10) {
            return false;
        }
        if (bmpBuffer == null) {
            curbmpPos = 0;
            int buflen = Packet.byteArrayToInt_Little(byt, 0);
            if (buflen <= 0) {
                return false;
            }
            bmpBuffer = new byte[buflen];
        }
        int datalen = Packet.byteArrayToInt_Little(byt, 4);
        if (curbmpPos + datalen <= bmpBuffer.length)
            System.arraycopy(byt, 10, bmpBuffer, curbmpPos, datalen);
        curbmpPos += (datalen);
        short flag = Packet.byteArrayToShort_Little(byt, 8);
        if (flag == 1) {
            createSnapshot();
            return true;
        }
        return false;
    }

    private void createSnapshot() {
        Bitmap snapshot_temp = BitmapFactory.decodeByteArray(bmpBuffer, 0, bmpBuffer.length);
        if (snapshot_temp != null)
            snapshot = snapshot_temp;

        bmpBuffer = null;
        curbmpPos = 0;

    }

    public boolean isFirstLogin() {
        return isFirstLogin;
    }

    public void setFirstLogin(boolean isFirstLogin) {
        this.isFirstLogin = isFirstLogin;
    }

    public boolean isSetValueWithoutSave() {
        return isSetValueWithoutSave;
    }

    @Override
    public void connect() {
        if (getUid() != null && getUid().length() > 4) {
            // String temp = getUid().substring(0, 5);
            String str = getUid().substring(0, 4);
            // if (temp.equalsIgnoreCase("FDTAA") || str.equalsIgnoreCase("DEAA") ||
            // str.equalsIgnoreCase("AAES")) {
            if (str.equalsIgnoreCase("AAES")) {
                return;
            } else {
                super.connect();
                return;
            }
        } else {
            return;
        }
    }

    public interface OnBindPushResult {
        public void onBindSuccess(MyCamera camera);

        public void onBindFail(MyCamera camera);

        public void onUnBindSuccess(MyCamera camera);

        public void onUnBindFail(MyCamera camera);
        public void onUpNameFail(MyCamera camera);
        public void onUpNameSuccess(MyCamera camera);
    }

    private OnBindPushResult onBindPushResult;

    public HiPushSDK push;
    public HiPushSDK.OnPushResult pushResult = new HiPushSDK.OnPushResult() {
        @Override
        public void pushBindResult(int subID, int type, int result) {
            isSetValueWithoutSave = true;
            //Log.e("final_Bind_address==", push.getPushServer());
            if (type == HiPushSDK.PUSH_TYPE_BIND) {
                if (HiPushSDK.PUSH_RESULT_SUCESS == result) {
                    pushState = subID;
                    if (onBindPushResult != null)
                        onBindPushResult.onBindSuccess(MyCamera.this);
                } else if (HiPushSDK.PUSH_RESULT_FAIL == result || HiPushSDK.PUSH_RESULT_NULL_TOKEN == result) {
                    if (onBindPushResult != null)
                        onBindPushResult.onBindFail(MyCamera.this);
                }
            } else if (type == HiPushSDK.PUSH_TYPE_UNBIND) {
                if (HiPushSDK.PUSH_RESULT_SUCESS == result) {
                    if (onBindPushResult != null)
                        onBindPushResult.onUnBindSuccess(MyCamera.this);
                } else if (HiPushSDK.PUSH_RESULT_FAIL == result) {
                    if (onBindPushResult != null)
                        onBindPushResult.onUnBindFail(MyCamera.this);
                }
            } else if (type == HiPushSDK.PUSH_TYPE_UPNAME) {
                if (HiPushSDK.PUSH_RESULT_SUCESS == result) {
                    if(onBindPushResult != null)
                        onBindPushResult.onUpNameSuccess(MyCamera.this);
                } else if (HiPushSDK.PUSH_RESULT_FAIL == result) {
                    if(onBindPushResult != null)
                        onBindPushResult.onUpNameFail(MyCamera.this);
                }
            }
        }
    };

    public void bindPushState(boolean isBind, OnBindPushResult bindPushResult) {
        String bindtoken = "";
        // String jgToken = JPushInterface.getRegistrationID(mContext);
        if (SystemUtils.isZh(mContext)) {
            if (SystemUtils.isOPPOMoblie(mContext) || SystemUtils.isVIVOMoblie(mContext)) {
                bindtoken = HiDataValue.XGToken;
            } else {
                bindtoken = HiDataValue.NewPushToken;
            }

        } else {
            bindtoken = HiDataValue.FcmToken;
        }
        if (TextUtils.isEmpty(bindtoken) && TextUtils.isEmpty(HiDataValue.XGToken)) {
            return;
        }

        if (!isBind && this.getServerData() != null && !this.getServerData().equals(HiDataValue.CAMERA_ALARM_ADDRESS_233)) {

            String token = "";
            if (SystemUtils.isZh(mContext)) {
                if (SystemUtils.isOPPOMoblie(mContext) || SystemUtils.isVIVOMoblie(mContext)) {
                    token = HiDataValue.XGToken;
                } else {
                    token = HiDataValue.NewPushToken;
                }
            } else {
                token = HiDataValue.FcmToken;
            }
            if (TextUtils.isEmpty(token)) {
                if (!TextUtils.isEmpty(HiDataValue.XGToken)) {
                    push = new HiPushSDK(mContext,HiDataValue.XGToken + "&notify=1", getUid(), HiDataValue.company,"xinge", getBindNikeName(),pushResult, getPushAddressByUID());
                } else {
                    HiToast.showToast(mContext, "all token is null");
                    return;
                }
            } else {
                if (SystemUtils.isOPPOMoblie(mContext) || SystemUtils.isVIVOMoblie(mContext)) {
                    push = new HiPushSDK(mContext,HiDataValue.XGToken+ "&notify=1", getUid(), HiDataValue.company,"xinge",getBindNikeName(), pushResult, getPushAddressByUID());
                } else {
                    push = new HiPushSDK(mContext, token + "&notify=1", getUid(), HiDataValue.company, TokenUtils.getPushName(mContext), getBindNikeName(),pushResult, getPushAddressByUID());
                }
            }
            push.setPushServer(this.getServerData(), 1, 1);
        } else if (this.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET) && !handSubXYZ()) {
            if (handSubWTU()) {

                newPushWay(HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122);
            } else if (handSubAACC()) {
                newPushWay(HiDataValue.CAMERA_ALARM_ADDRESS_AACC_148);
            } else if (handSubSSAA()) {
                newPushWay(HiDataValue.CAMERA_ALARM_ADDRESS_SSAA_161);
            } else {
                newPushWay(HiDataValue.CAMERA_ALARM_ADDRESS_233);
            }
        } else if (this.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET) && handSubXYZ()) {
            newPushWay(HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173);
        } else if (this.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET) && handSubAACC()) {
            newPushWay(HiDataValue.CAMERA_ALARM_ADDRESS_AACC_148);
        } else {// old device
            if (handSubWTU()) {
                newPushWay(HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122);
            } else {
                newPushWay(HiDataValue.CAMERA_ALARM_ADDRESS_233);
            }
        }

        onBindPushResult = bindPushResult;
        if (isBind) {
            push.bind();
        } else {
            push.unbind(getPushState());
        }

    }

    private void newPushWay(String pushAddress) {

        String token = "";
        if (SystemUtils.isZh(mContext)) {
            if (SystemUtils.isOPPOMoblie(mContext) || SystemUtils.isVIVOMoblie(mContext)) {
                token = HiDataValue.XGToken;
            } else {
                token = HiDataValue.NewPushToken;
            }

        } else {
            token = HiDataValue.FcmToken;
        }


        if (TextUtils.isEmpty(token)) {
            if (!TextUtils.isEmpty(HiDataValue.XGToken)) {
                push = new HiPushSDK(mContext,HiDataValue.XGToken+ "&notify=1", getUid(), HiDataValue.company, "xinge",getBindNikeName(),pushResult, pushAddress);
                SharePreUtils.putBoolean("cache", mContext, getUid() + "isReXg", true);
            } else {
                HiToast.showToast(mContext, "all token is null");
                return;
            }
        } else {
            if (SystemUtils.isOPPOMoblie(mContext) || SystemUtils.isVIVOMoblie(mContext)) {
                push = new HiPushSDK(mContext,HiDataValue.XGToken+"&notify=1", getUid(), HiDataValue.company,"xinge",getBindNikeName(), pushResult, pushAddress);
                SharePreUtils.putBoolean("cache", mContext, getUid() + "isReXg", true);
            } else {
                push = new HiPushSDK(mContext, token + "&notify=1", getUid(), HiDataValue.company, TokenUtils.getPushName(mContext),getBindNikeName() ,pushResult, pushAddress);
                if ("fcm".equalsIgnoreCase(TokenUtils.getPushName(mContext))) {
                    SharePreUtils.putBoolean("cache", mContext, getUid() + "isReFCM", true);
                } else {
                    SharePreUtils.putBoolean("cache", mContext, getUid() + "isReFives", true);
                }
            }
        }
        HiTools.checkAddressReNew(push, pushAddress, push.getPushServer());

    }

    public boolean handSubXYZ() {
        String subUid = this.getUid().substring(0, 4);
        for (String str : HiDataValue.SUBUID) {
            if (str.equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    public boolean handSubWTU() {
        String subUid = this.getUid().substring(0, 4);
        for (String str : HiDataValue.SUBUID_WTU) {
            if (str.equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }
    public boolean isFDTAA() {
        if (!TextUtils.isEmpty(this.getUid()) && this.getUid().length() > 4) {
            String subUid = this.getUid().substring(0, 4);
            if ("FDTAA".equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    public boolean isDEAA() {
        if (!TextUtils.isEmpty(this.getUid()) && this.getUid().length() > 4) {
            String subUid = this.getUid().substring(0, 4);
            if ("DEAA".equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    public boolean handSubAACC() {
        String subUid = this.getUid().substring(0, 4);
        for (String str : HiDataValue.SUBUID_AACC) {
            if (str.equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    public boolean handSubSSAA() {
        String subUid = this.getUid().substring(0, 4);
        for (String str : HiDataValue.SUBUID_SSAA) {
            if (str.equalsIgnoreCase(subUid)) {
                return true;
            }
        }
        return false;
    }

    public boolean isFishEye() {
        if (mContext == null)
            return false;
        int isFishEye = 0;
        if (this.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
            isFishEye = this.getmold();
            SharePreUtils.putInt("cache", mContext, this.getUid() + "isFishEye", this.getmold());
        } else {
            isFishEye = SharePreUtils.getInt("cache", mContext, this.getUid() + "isFishEye");
        }
        if (isFishEye == 1) {
            return true;
        } else {
            return false;
        }
    }

    public void putFishModType(int fishModetype) {
        if (mContext == null)
            return;
        SharePreUtils.putInt("cache", mContext, this.getUid() + "fishmodtype", fishModetype);
    }

    public int getFishModType() {
        if (mContext == null)
            return 0;
        int type = SharePreUtils.getInt("cache", mContext, this.getUid() + "fishmodtype");
        if (type == -1) {
            return 0;
        } else {
            return type;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((uid == null) ? 0 : uid.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        MyCamera other = (MyCamera) obj;
        if (uid == null) {
            if (other.uid != null)
                return false;
        } else if (!uid.equals(other.uid))
            return false;
        return true;
    }

    public boolean isErrorUID() {
        return isErrorUID;
    }

    public void setErrorUID(boolean errorUID) {
        isErrorUID = errorUID;
    }

    public boolean isUpdateing() {
        return updateing;
    }

    public void setUpdateing(boolean updateing) {
        this.updateing = updateing;
    }


    public boolean isErrorUID(String uid) {
        if (TextUtils.isEmpty(uid)) {
            return false;
        }
        String[] uidArray = uid.split("-");
        if (uidArray.length == 3) {
            String uidBegin = uidArray[0];
            String uidStage = uidArray[1];
            int stage = Integer.parseInt(uidStage);
            boolean isStage = stage >= 357000 && stage <= 362000;
            if ("SSSS".equalsIgnoreCase(uidBegin) && isStage) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public String getPushAddressByUID() {
        String pushAddress;
        if (this.commandFunction.getAppCmdFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_GET)) {
            if (handSubWTU()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122;
            } else if (handSubXYZ()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173;
            } else if (handSubAACC()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_AACC_148;
            } else if (handSubSSAA()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_SSAA_161;
            } else {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_233;
            }
        } else {
            if (handSubWTU()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122;
            } else if (handSubAACC()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_AACC_148;
            } else if (handSubSSAA()) {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_SSAA_161;
            } else {
                pushAddress = HiDataValue.CAMERA_ALARM_ADDRESS_233;
            }
        }
        return pushAddress;
    }

}
