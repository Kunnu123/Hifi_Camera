package com.hificamera.thecamhi.activity.setting;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.RadioGroup.OnCheckedChangeListener;

import java.util.regex.Pattern;

import com.hificamera.R;
import com.hificamera.hichip.activity.AlarmSoundTimeActivity;
import com.hificamera.hichip.activity.AlarmVoiceTypeActivity;
import com.hichip.base.HiLog;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.data.HiDeviceInfo;
import com.hificamera.hichip.widget.SwitchButton;
import com.hichip.push.HiPushSDK;
import com.hificamera.thecamhi.utils.NotificationsUtils;
import com.hificamera.thecamhi.utils.SystemUtils;
import com.hificamera.thecamhi.utils.TokenUtils;
import com.hificamera.thecamhi.utils.UpnameUtils;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

import com.hificamera.thecamhi.base.HiToast;
import com.hificamera.thecamhi.base.HiTools;
import com.hificamera.thecamhi.base.TitleView;
import com.hificamera.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hificamera.thecamhi.bean.CamHiDefines;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.MyCamera;
import com.hificamera.thecamhi.bean.CamHiDefines.HI_P2P_ALARM_ADDRESS;
import com.hificamera.thecamhi.bean.MyCamera.OnBindPushResult;
import com.hificamera.thecamhi.main.HiActivity;

import com.hificamera.thecamhi.utils.SharePreUtils;

public class AlarmActionActivity extends HiActivity implements ICameraIOSessionCallback, SwitchButton.OnCheckedChangeListener, OnCheckedChangeListener, OnClickListener {
    private MyCamera mCamera;

    private HiChipDefines.HI_P2P_S_ALARM_PARAM param;
    private HiChipDefines.HI_P2P_SNAP_ALARM snapParam;
    private SwitchButton alarm_push_push_tgbtn, alarm_push_sd_video_tgbtn, alarm_push_email_alarm_tgbtn, alarm_push_save_picture_tgbtn, alarm_push_video_tgbtn;
    private RadioGroup mRgPictureNum;
    private int mPictureNum = 1;
    private final static int HANDLE_MESSAGE_BIND_SUCCESS = 0x80000001;
    private final static int HANDLE_MESSAGE_BIND_FAIL = 0x80000002;
    private final static int HANDLE_MESSAGE_UNBIND_SUCCESS = 0x80000003;
    private final static int HANDLE_MESSAGE_UNBIND_FAIL = 0x80000004;
    private LinearLayout ll_alarm_antion;
    private LinearLayout ll_alarm_voice_type;
    private LinearLayout ll_alarm_time;
    private SwitchButton switch_alarm_action;
    private TextView tv_voice_type;
    private TextView tv_alarm_time;
    private HiChipDefines.ALARMTOSOUND_TYPE mAlarmToSoundType;
    private int mVoiceType = 0;
    private int mTime = 0;
    private int mEnable = 0;

    private LinearLayout ll_alarm_action_setting;

    private String uid;
    private HiChipDefines.HI_P2P_S_REC_AUTO_PARAM rec_param;
    private TextView tvOpenNotif, tvOpenBattery;
    //推送名称
    private EditText etPushName;
    private ImageView ivUpName;
    private LinearLayout llHandEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_action_with_alarm);
        uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
        for (MyCamera camera : HiDataValue.CameraList) {
            if (uid.equals(camera.getUid())) {
                mCamera = camera;
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_ALARM_PARAM, null);
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_SNAP_ALARM_PARAM, null);


                break;
            }
        }
        HiTools.cameraWhetherNull(this, mCamera);
        initView();
        setListeners();

    }

    private void setListeners() {
        ll_alarm_voice_type.setOnClickListener(this);
        ll_alarm_time.setOnClickListener(this);
        switch_alarm_action.setOnCheckedChangeListener(this);
    }

    private void initView() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        String pushName = SharePreUtils.getString("upName", this, mCamera.getUid() + "upName");
        TitleView title = (TitleView) findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.item_action_with_alarm));
        title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        AlarmActionActivity.this.finish();
                        break;
                }

            }
        });

        alarm_push_push_tgbtn = (SwitchButton) findViewById(R.id.alarm_push_push_tgbtn);
        if (mCamera != null) {
            if (mCamera.getPushState() > 0) {
                alarm_push_push_tgbtn.setChecked(true);
            } else {
                alarm_push_push_tgbtn.setChecked(false);
            }
        }
        alarm_push_push_tgbtn.setOnCheckedChangeListener(this);

        alarm_push_sd_video_tgbtn = (SwitchButton) findViewById(R.id.alarm_push_sd_video_tgbtn);
        alarm_push_email_alarm_tgbtn = (SwitchButton) findViewById(R.id.alarm_push_email_alarm_tgbtn);
        alarm_push_save_picture_tgbtn = (SwitchButton) findViewById(R.id.alarm_push_save_picture_tgbtn);
        alarm_push_video_tgbtn = (SwitchButton) findViewById(R.id.alarm_push_video_tgbtn);
        initRGPicView();
        LinearLayout action_alarm_picture_num_ll = (LinearLayout) findViewById(R.id.action_alarm_picture_num_ll);
        if (mCamera.getChipVersion() == HiDeviceInfo.CHIP_VERSION_HISI && mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_SNAP_ALARM_PARAM)) {
            action_alarm_picture_num_ll.setVisibility(View.VISIBLE);
        }
        ll_alarm_antion = (LinearLayout) findViewById(R.id.ll_alarm_antion);
        ll_alarm_voice_type = (LinearLayout) findViewById(R.id.ll_alarm_voice_type);
        ll_alarm_time = (LinearLayout) findViewById(R.id.ll_alarm_time);
        switch_alarm_action = (SwitchButton) findViewById(R.id.switch_alarm_action);
        tv_voice_type = (TextView) findViewById(R.id.tv_voice_type);
        tv_alarm_time = (TextView) findViewById(R.id.tv_alarm_time);
        ll_alarm_action_setting = (LinearLayout) findViewById(R.id.ll_alarm_action_setting);

        if (mCamera != null && mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_ALARMTOSOUND)) {
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_ALARMTOSOUND, new byte[0]);
            ll_alarm_antion.setVisibility(View.VISIBLE);
        } else {
            ll_alarm_antion.setVisibility(View.GONE);
        }
        tvOpenBattery = findViewById(R.id.tv_open_battery);
        tvOpenNotif = findViewById(R.id.tv_open_notice);
        tvOpenBattery.setOnClickListener(this);
        tvOpenNotif.setOnClickListener(this);
        etPushName = findViewById(R.id.et_pushname);
        ivUpName = findViewById(R.id.iv_up_name);
        llHandEdit = findViewById(R.id.ll_hand_edit);
        ivUpName.setOnClickListener(this);
        llHandEdit.setOnClickListener(this);
        if (!TextUtils.isEmpty(pushName)) {
            etPushName.setText(pushName);
        } else {
            etPushName.setText(mCamera.getNikeName());
        }
    }

    private void initRGPicView() {
        mRgPictureNum = (RadioGroup) findViewById(R.id.radioGroup_alarm_action);
    }

    private void sendRegister() {
        HiLog.e("mCamera.getPushState()==" + mCamera.getPushState());

        if (mCamera.getPushState() == 1) {
            return;
        }
        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_TOKEN_REGIST)) {
            return;
        }

        byte[] info = CamHiDefines.HI_P2P_ALARM_TOKEN_INFO.parseContent(0, mCamera.getPushState(), (int) (System.currentTimeMillis() / 1000 / 3600), mCamera.getPushState() > 0 ? 1 : 0);
        mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_TOKEN_REGIST, info);
    }

    private void sendUnRegister() {
        if (mCamera.getPushState() == 0) {
            return;
        }
        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_TOKEN_UNREGIST)) {
            return;
        }

        byte[] info = CamHiDefines.HI_P2P_ALARM_TOKEN_INFO.parseContent(0, mCamera.getPushState(), (int) (System.currentTimeMillis() / 1000 / 3600), mCamera.getPushState() > 0 ? 1 : 0);
        mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_TOKEN_UNREGIST, info);
    }

    private OnBindPushResult bindPushResult = new OnBindPushResult() {

        @Override
        public void onBindSuccess(MyCamera camera) {
            Message msg = handler.obtainMessage();
            msg.what = HANDLE_MESSAGE_BIND_SUCCESS;
            msg.obj = camera;
            handler.sendMessage(msg);
        }

        @Override
        public void onBindFail(MyCamera camera) {
            Message msg = handler.obtainMessage();
            msg.what = HANDLE_MESSAGE_BIND_FAIL;
            handler.sendMessage(msg);
        }

        @Override
        public void onUnBindSuccess(MyCamera camera) {
            Message msg = handler.obtainMessage();
            msg.what = HANDLE_MESSAGE_UNBIND_SUCCESS;
            handler.sendMessage(msg);
        }

        @Override
        public void onUnBindFail(MyCamera camera) {
            Message msg = handler.obtainMessage();
            msg.what = HANDLE_MESSAGE_UNBIND_FAIL;
            handler.sendMessage(msg);
        }

        @Override
        public void onUpNameFail(MyCamera camera) {


        }

        @Override
        public void onUpNameSuccess(MyCamera camera) {

        }

    };

    public void sendFTPSetting() {
        if (param == null)
            return;
        param.u32SDRec = alarm_push_sd_video_tgbtn.isChecked() ? 1 : 0;
        param.u32EmailSnap = alarm_push_email_alarm_tgbtn.isChecked() ? 1 : 0;
        param.u32FtpSnap = alarm_push_save_picture_tgbtn.isChecked() ? 1 : 0;
        param.u32FtpRec = alarm_push_video_tgbtn.isChecked() ? 1 : 0;
        showjuHuaDialog();
        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_ALARM_PARAM, param.parseContent());
        if (snapParam == null)
            return;
        snapParam.u32Number = mPictureNum;
        snapParam.u32Interval = snapParam.u32Interval < 5 ? 5 : snapParam.u32Interval;
        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_SNAP_ALARM_PARAM, snapParam.parseContent());

    }

    @Override
    public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
        if (arg0 != mCamera)
            return;
        Bundle bundle = new Bundle();
        bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
        Message msg = handler.obtainMessage();
        msg.what = HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL;
        msg.obj = arg0;
        msg.arg1 = arg1;
        msg.arg2 = arg3;
        msg.setData(bundle);
        handler.sendMessage(msg);

    }

    @SuppressLint("HandlerLeak") private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_MESSAGE_BIND_SUCCESS:
                    MyCamera myCamera = (MyCamera) msg.obj;
                    dismissjuHuaDialog();
                    if (!myCamera.handSubXYZ()) {
                        if (myCamera.handSubWTU()) {
                            myCamera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122);
                        } else if (myCamera.handSubAACC()) {
                            myCamera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_AACC_148);
                        } else if (myCamera.handSubSSAA()) {
                            myCamera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_SSAA_161);
                        } else {
                            myCamera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_233);
                        }
                    } else {
                        myCamera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173);
                    }

                    //                    if (myCamera.isDEAA()) {
                    //                        myCamera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_DERICAM_148);
                    //                    } else if (myCamera.isFDTAA()) {
                    //                        myCamera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_FDT_221);
                    //                    } else if (myCamera.handSubXYZ()) {
                    //                        myCamera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173);
                    //                    } else if (myCamera.handSubWTU()) {
                    //                        myCamera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122);
                    //                    } else {
                    //                        myCamera.setServerData(HiDataValuCAMERA_ALARM_ADDRESS_233);
                    //                    }

                    mCamera.updateInDatabase(AlarmActionActivity.this);
                    sendServer(myCamera);
                    sendRegister();
                    break;
                case HANDLE_MESSAGE_BIND_FAIL:
                    HiToast.showToast(AlarmActionActivity.this, getString(R.string.tip_open_faild));
                    mChecked = true;
                    alarm_push_push_tgbtn.setChecked(false);
                    dismissjuHuaDialog();
                    mCamera.updateInDatabase(AlarmActionActivity.this);
                    break;
                case HANDLE_MESSAGE_UNBIND_SUCCESS:
                    sendUnRegister();
                    mCamera.setPushState(HiDataValue.DEFAULT_PUSH_STATE);
                    dismissjuHuaDialog();
                    mCamera.updateInDatabase(AlarmActionActivity.this);
                    break;
                case HANDLE_MESSAGE_UNBIND_FAIL:
                    HiToast.showToast(AlarmActionActivity.this, getString(R.string.tip_close_failed));
                    mChecked = true;
                    alarm_push_push_tgbtn.setChecked(true);
                    dismissjuHuaDialog();
                    mCamera.updateInDatabase(AlarmActionActivity.this);
                    break;
                case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL: {
                    if (msg.arg2 == 0) {
                        Bundle bundle = msg.getData();
                        byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
                        switch (msg.arg1) {

                            case HiChipDefines.HI_P2P_GET_REC_AUTO_PARAM:
                                rec_param = new HiChipDefines.HI_P2P_S_REC_AUTO_PARAM(data);
                                if (rec_param.u32Enable == 1) {
                                    closePlanRecord(rec_param);
                                }
                                break;

                            case HiChipDefines.HI_P2P_GET_ALARM_PARAM:
                                param = new HiChipDefines.HI_P2P_S_ALARM_PARAM(data);
                                alarm_push_sd_video_tgbtn.setChecked(param.u32SDRec == 1 ? true : false);
                                alarm_push_sd_video_tgbtn.setOnCheckedChangeListener(AlarmActionActivity.this);
                                alarm_push_email_alarm_tgbtn.setChecked(param.u32EmailSnap == 1 ? true : false);
                                alarm_push_email_alarm_tgbtn.setOnCheckedChangeListener(AlarmActionActivity.this);
                                alarm_push_save_picture_tgbtn.setChecked(param.u32FtpSnap == 1 ? true : false);
                                alarm_push_save_picture_tgbtn.setOnCheckedChangeListener(AlarmActionActivity.this);
                                alarm_push_video_tgbtn.setChecked(param.u32FtpRec == 1 ? true : false);
                                alarm_push_video_tgbtn.setOnCheckedChangeListener(AlarmActionActivity.this);
                                break;
                            case HiChipDefines.HI_P2P_GET_SNAP_ALARM_PARAM:
                                snapParam = new HiChipDefines.HI_P2P_SNAP_ALARM(data);
                                if (snapParam.u32Number < 1)
                                    return;
                                switch (snapParam.u32Number - 1) {
                                    case 0:
                                        mPictureNum = 1;
                                        break;
                                    case 1:
                                        mPictureNum = 2;
                                        break;
                                    case 2:
                                        mPictureNum = 3;
                                        break;
                                }
                                ((RadioButton) mRgPictureNum.getChildAt(snapParam.u32Number - 1)).setChecked(true);
                                mRgPictureNum.setOnCheckedChangeListener(AlarmActionActivity.this);
                                break;
                            case HiChipDefines.HI_P2P_SET_ALARM_PARAM:
                                mCamera.updateInDatabase(AlarmActionActivity.this);
                                dismissjuHuaDialog();
                                break;
                            case HiChipDefines.HI_P2P_SET_SNAP_ALARM_PARAM:
                                dismissjuHuaDialog();
                                break;
                            case CamHiDefines.HI_P2P_ALARM_TOKEN_UNREGIST:

                                break;
                            case HiChipDefines.HI_P2P_IPCRF_ALARM_GET:
                                break;
                            case HiChipDefines.HI_P2P_IPCRF_ALARM_SET:
                                dismissjuHuaDialog();
                                break;
                            case HiChipDefines.HI_P2P_GET_ALARMTOSOUND:
                                mAlarmToSoundType = new HiChipDefines.ALARMTOSOUND_TYPE(data);
                                mVoiceType = mAlarmToSoundType.SoundType;
                                mTime = mAlarmToSoundType.TimeLast;
                                mEnable = mAlarmToSoundType.enable;
                                Log.i("tedu", "--HI_P2P_GET_ALARMTOSOUND--" + "--mAlarmToSoundType.SoundType--:" + mAlarmToSoundType.SoundType);
                                Log.i("tedu", "--HI_P2P_GET_ALARMTOSOUND--" + "--mAlarmToSoundType.TimeLast--:" + mAlarmToSoundType.TimeLast);
                                switch_alarm_action.setChecked(mAlarmToSoundType.enable == 1 ? true : false);
                                //	tv_voice_type.setText(mAlarmToSoundType.SoundType==0?getString(R.string.alarm_alarm_sound):getString(R.string.alarm_barking));
                                switch (mVoiceType) {
                                    case 0:
                                        tv_voice_type.setText(getString(R.string.alarm_alarm_sound));
                                        break;
                                    case 1:
                                        tv_voice_type.setText(getString(R.string.alarm_barking));
                                        break;
                                    case 2:
                                        tv_voice_type.setText(getString(R.string.alarm_custom));
                                        break;
                                }
                                tv_alarm_time.setText(mAlarmToSoundType.TimeLast + getString(R.string.sends));
                                break;
                            case HiChipDefines.HI_P2P_SET_ALARMTOSOUND:
                                dismissjuHuaDialog();
                                break;

                            //                            case CamHiDefines.HI_P2P_ALARM_ADDRESS_SET:
                            //                                 mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_ADDRESS_GET, null);
                            //                                break;
                            //                            case CamHiDefines.HI_P2P_ALARM_ADDRESS_GET:
                            //                                CamHiDefines.HI_P2P_ALARM_ADDRESS aa = new CamHiDefines.HI_P2P_ALARM_ADDRESS(data);
                            //                                HiLog.e("==push-sever-address=="+Packet.getString(aa.szAlarmAddr));
                            //                                break;

                        }
                    } else {
                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_SET_ALARM_PARAM:
                                HiToast.showToast(AlarmActionActivity.this, getResources().getString(R.string.alarm_action_save_failed));
                                break;
                            case HiChipDefines.HI_P2P_SET_ALARMTOSOUND:
                                break;
                        }
                    }
                }
            }
        }
    };

    @Override
    public void receiveSessionState(HiCamera arg0, int arg1) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera != null) {
            mCamera.registerIOSessionListener(this);
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        if (mCamera != null) {
            mCamera.unregisterIOSessionListener(this);
        }

    }

    private boolean mChecked = false;


    @Override
    public void onCheckedChanged(CompoundButton view, boolean isChecked) {
        switch (view.getId()) {
            case R.id.alarm_push_push_tgbtn:
                if (mChecked) {
                    mChecked = false;
                    return;
                }
                String bindtoken = "";
                // String jgToken = JPushInterface.getRegistrationID(this);
                if (SystemUtils.isZh(this)) {
                    if (SystemUtils.isOPPOMoblie(this) || SystemUtils.isVIVOMoblie(this)) {
                        bindtoken = HiDataValue.XGToken;
                    } else {
                        bindtoken = HiDataValue.NewPushToken;
                    }

                } else {
                    bindtoken = HiDataValue.FcmToken;
                }
                if (TextUtils.isEmpty(bindtoken) && TextUtils.isEmpty(HiDataValue.XGToken)) {
                    view.setChecked(!view.isChecked());
                    HiToast.showToast(AlarmActionActivity.this, getString(R.string.tip_open_faild));
                    return;
                }
                if (!isChecked) {
                    unbindOldRegist(mCamera);
                    SharePreUtils.removeKey("upName", this, mCamera.getUid() + "upName");

                }
                showjuHuaDialog();
                mCamera.bindPushState(view.isChecked(), bindPushResult);
                break;
            case R.id.alarm_push_sd_video_tgbtn:
                sendFTPSetting();
                break;
            case R.id.alarm_push_email_alarm_tgbtn:
                sendFTPSetting();
                break;
            case R.id.alarm_push_save_picture_tgbtn:
                sendFTPSetting();
                break;
            case R.id.alarm_push_video_tgbtn:
                sendFTPSetting();
                break;
            case R.id.switch_alarm_action:
                if (mAlarmToSoundType != null) {
                    byte enable = isChecked ? (byte) 1 : (byte) 0;
                    mEnable = enable;
                    byte voiceType = (byte) mVoiceType;
                    byte time = (byte) mTime;
                    showjuHuaDialog();
                    if (isChecked) {
                        ll_alarm_action_setting.setVisibility(View.VISIBLE);
                        AnimatorSet animatorSet = new AnimatorSet();
                        ObjectAnimator animator1 = ObjectAnimator.ofFloat(ll_alarm_action_setting, "Alpha", 0f, 1f);
                        animatorSet.playTogether(animator1);
                        animatorSet.setDuration(500);
                        animatorSet.start();
                    } else {
                        ll_alarm_action_setting.setVisibility(View.GONE);
                    }
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_ALARMTOSOUND, HiChipDefines.ALARMTOSOUND_TYPE.parseContent(enable, voiceType, time));
                }
                break;
        }
    }


    public void ignoreBatteryOptimization(Activity activity) {

        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        boolean hasIgnored = false;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            assert powerManager != null;
            hasIgnored = powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
            if (!hasIgnored) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + activity.getPackageName()));
                startActivity(intent);
            }
        }
    }

    private void closePlanRecord(HiChipDefines.HI_P2P_S_REC_AUTO_PARAM rec_param) {
        rec_param.u32Enable = 0;
        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_REC_AUTO_PARAM, rec_param.parseContent());
        HiToast.showToast(AlarmActionActivity.this, getString(R.string.closed_plan_record));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radio_one:
                mPictureNum = 1;
                sendFTPSetting();
                break;
            case R.id.radio_two:
                mPictureNum = 2;
                sendFTPSetting();
                break;
            case R.id.radio_there:
                mPictureNum = 3;
                sendFTPSetting();
                break;
        }
    }

    protected void sendServer(MyCamera mCamera) {
        if (mCamera.getServerData() == null) {
            mCamera.setServerData(mCamera.getPushAddressByUID());
            mCamera.updateServerInDatabase(this);
        }
        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET)) {
            return;
        }
        if (mCamera.push != null) {
            String[] strs = mCamera.push.getPushServer().split("\\.");
            if (strs.length == 4 && isInteger(strs[0]) && isInteger(strs[1]) && isInteger(strs[2]) && isInteger(strs[3])) {
                byte[] info = HI_P2P_ALARM_ADDRESS.parseContent(mCamera.push.getPushServer());
                mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET, info);

            }

        }
    }
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    @Override
    public void onClick(View v) {
        if (etPushName.hasFocus()) {
            if (v.getId() == R.id.btn_return) {
                AlarmActionActivity.this.finish();
            } else {
                handDown();
                return;
            }
        }

        switch (v.getId()) {
            case R.id.iv_up_name:
                handIvEditName();
                break;
            case R.id.ll_alarm_voice_type:
                Intent intent = new Intent(AlarmActionActivity.this, AlarmVoiceTypeActivity.class);
                intent.putExtra("mVoiceType", mVoiceType);
                intent.putExtra("uid", uid);
                startActivityForResult(intent, 110);
                break;
            case R.id.ll_alarm_time:
                intent = new Intent(AlarmActionActivity.this, AlarmSoundTimeActivity.class);
                intent.putExtra("mTime", mTime);
                startActivityForResult(intent, 119);
                break;
            case R.id.tv_open_battery:
                startActivity(new Intent(Settings.ACTION_SETTINGS));
                break;
            case R.id.tv_open_notice:
                NotificationsUtils.OpenNotifi(AlarmActionActivity.this);
                break;
        }
    }

    private void handIvEditName() {
        etPushName.setFocusable(true);
        etPushName.setFocusableInTouchMode(true);
        etPushName.requestFocus();
        if (!TextUtils.isEmpty(etPushName.getText().toString())) {
            etPushName.setSelection(etPushName.getText().toString().length());
        }
        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.showSoftInput(etPushName, InputMethodManager.SHOW_FORCED);
    }

    private boolean handDown() {
        etPushName.clearFocus();
        etPushName.setFocusable(false);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etPushName.getWindowToken(), 0);

        String pushName = etPushName.getText().toString().trim();
        if (TextUtils.isEmpty(pushName)) {
            showAlert(getText(R.string.push_input_error));
            return true;
        }

        mCamera.setNikeName(pushName);
        mCamera.updateInDatabase(this);
        if (!TextUtils.isEmpty(pushName.trim()) && mCamera.getPushState() > 0) {
            UpnameUtils.upName(pushName, this, mCamera);
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 110:
                    mVoiceType = data.getIntExtra("mVoiceType", 0);
                    switch (mVoiceType) {
                        case 0:
                            tv_voice_type.setText(getString(R.string.alarm_alarm_sound));
                            break;
                        case 1:
                            tv_voice_type.setText(getString(R.string.alarm_barking));
                            break;
                        case 2:
                            tv_voice_type.setText(getString(R.string.alarm_custom));
                            break;
                    }
                    showjuHuaDialog();
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_ALARMTOSOUND, HiChipDefines.ALARMTOSOUND_TYPE.parseContent((byte) mEnable, (byte) mVoiceType, (byte) mTime));
                    break;
                case 119:
                    mTime = data.getIntExtra("mTime", 5);
                    tv_alarm_time.setText(mTime + getString(R.string.sends));
                    showjuHuaDialog();
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_ALARMTOSOUND, HiChipDefines.ALARMTOSOUND_TYPE.parseContent((byte) mEnable, (byte) mVoiceType, (byte) mTime));
                    break;
            }

        }

    }


    private void unbindOldRegist(MyCamera mCamera) {
        boolean isReJG = SharePreUtils.getBoolean("cache", this, mCamera.getUid() + "isReJG");
        boolean isReFCM = SharePreUtils.getBoolean("cache", this, mCamera.getUid() + "isReFCM");
        boolean isReFives = SharePreUtils.getBoolean("cache", this, mCamera.getUid() + "isReFives");
        boolean isReXg = SharePreUtils.getBoolean("cache", this, mCamera.getUid() + "isReXg");
        handUnbind(mCamera, isReJG, isReFCM, isReFives, isReXg);

    }

    private void handUnbind(MyCamera mCamera, boolean isReGJ, boolean isReFCM, boolean isReFives, boolean isReXg) {
        if (mCamera == null) {
            return;
        }

        // String jgToken = JPushInterface.getRegistrationID(this);
        if (SystemUtils.isZh(this)) {
            if (SystemUtils.isVIVOMoblie(this) || SystemUtils.isOPPOMoblie(this)) {
                if (!TextUtils.isEmpty(HiDataValue.NewPushToken) && isReFives) {
                    unBindFives(mCamera);
                }

            }

            if (!TextUtils.isEmpty(HiDataValue.FcmToken) && isReFCM) {
                unBindFCM(mCamera);
            }
            if (!TextUtils.isEmpty(HiDataValue.XGToken) && ((!isReGJ && !isReFCM && !isReFives))) {
                unBindXg(mCamera);
            }
        } else {
            if (!TextUtils.isEmpty(HiDataValue.NewPushToken) && isReFives) {
                unBindFives(mCamera);
            }
            if (!TextUtils.isEmpty(HiDataValue.XGToken) && isReXg) {
                unBindXg(mCamera);
            }

        }

    }

    private void unBindXg(MyCamera mCamera) {
        HiPushSDK push = new HiPushSDK(HiDataValue.XGToken, mCamera.getUid(), HiDataValue.company, mCamera.pushResult, mCamera.getPushAddressByUID());
        push.unbind(mCamera.getPushState());
        SharePreUtils.putBoolean("cache", this, mCamera.getUid() + "isReXg", false);
    }

    private void unBindFives(final MyCamera mCamera) {
        String pushName = TokenUtils.getPhoneName(this);
        if (TextUtils.isEmpty(pushName)) {
            return;
        }
        HiPushSDK unPush = new HiPushSDK(this, HiDataValue.NewPushToken + "&notify=1", mCamera.getUid(), HiDataValue.company, pushName, mCamera.pushResult, mCamera.getPushAddressByUID());
        unPush.unbind(mCamera.getPushState());
        SharePreUtils.putBoolean("cache", this, mCamera.getUid() + "isReFives", false);

    }

    private void unBindJiGuang(final MyCamera mCamera, String jgToken) {
        HiPushSDK unPush = new HiPushSDK(this, jgToken + "&notify=1", mCamera.getUid(), HiDataValue.company, "jiguang", mCamera.pushResult, mCamera.getPushAddressByUID());
        unPush.unbind(mCamera.getPushState());
        SharePreUtils.putBoolean("cache", this, mCamera.getUid() + "isReJG", false);

    }

    private void unBindFCM(final MyCamera mCamera) {
        HiPushSDK fcmpush = new HiPushSDK(this, HiDataValue.FcmToken + "&notify=1", mCamera.getUid(), HiDataValue.company, "fcm", mCamera.pushResult, mCamera.getPushAddressByUID());
        fcmpush.unbind(mCamera.getPushState());
        SharePreUtils.putBoolean("cache", this, mCamera.getUid() + "isReFCM", false);
    }
}





