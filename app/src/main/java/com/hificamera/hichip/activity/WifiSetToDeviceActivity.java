package com.hificamera.hichip.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.InputFilter;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.hificamera.R;
import com.hichip.base.HiLog;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.sdk.HiChipP2P;
import com.hificamera.thecamhi.activity.setting.WifiSettingActivity;
import com.hificamera.thecamhi.base.HiToast;
import com.hificamera.thecamhi.base.HiTools;
import com.hificamera.thecamhi.base.TitleView;
import com.hificamera.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hificamera.thecamhi.bean.CamHiDefines;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.MyCamera;
import com.hificamera.thecamhi.main.HiActivity;
import com.hificamera.thecamhi.main.MainActivity;
import com.hificamera.thecamhi.utils.EmojiFilter;
import com.hificamera.thecamhi.utils.FullCharFilter;
import com.hificamera.thecamhi.utils.SpcialCharFilterWIFISET;
import com.hificamera.thecamhi.utils.SsidUtils;

public class WifiSetToDeviceActivity extends HiActivity implements ICameraIOSessionCallback {
    private TitleView mTitleView;
    private EditText mEtPw;
    private Button mBtnApplication;
    private TextView mTvSsid;
    private String mSsid, moldSsid;
    private String mUid;
    private MyCamera mCamera;
    protected byte wifiSelectMode;
    protected byte wifiSelectEncType;
    protected boolean send;
    private boolean isSupportLenExt = false;
    private boolean isSupportFullChar = false;
    private byte[] mPassWord;
    private boolean isNEWPWD255CheckWay;
    private String ssid, psw, currentSsid, type;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_set_to_device);
        getIntentData();
        initData();
        initView();
        setListerners();


    }

    private void getIntentData() {
        getWifiName();
        mSsid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_DATA);
        moldSsid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_DATA_OLD);
        mUid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
        wifiSelectMode = getIntent().getByteExtra(WifiSettingActivity.WIFISELECTMODE, (byte) 0);
        wifiSelectEncType = getIntent().getByteExtra(WifiSettingActivity.WIFISELECTENCTYPE, (byte) 0);
        mPassWord = getIntent().getByteArrayExtra("password");
        type = getIntent().getStringExtra("type");


    }

    private void setListerners() {
        mBtnApplication.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCamera == null) {
                    return;
                }
                ssid = mSsid.trim();
                String oldSsid = moldSsid.trim();
                psw = mEtPw.getText().toString().trim();
                if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_SET_WIFI_PARAM_NEWPWD255_EXT1) && isSupportFullChar) {
                    if (HiTools.isMaxLength(ssid, 63)) {
                        HiToast.showToast(WifiSetToDeviceActivity.this, getString(R.string.toast_ssid_tolong));
                        return;
                    }
                    boolean isMax = HiTools.isMaxLength(psw, 63);
                    if (isMax) {
                        HiToast.showToast(WifiSetToDeviceActivity.this, getString(R.string.tips_input_tolong));
                        return;
                    }

                    if (mPassWord != null && psw != null && new String(mPassWord).trim().equals(psw)) {
                        startToMain();
                        return;
                    }
                    if (currentWifiIsNotIPCAM()) {
                        //       HiLog.e("==1==", "NewCheck--->" + "type=HiChipDefines.HI_P2P_SET_WIFI_PARAM_NEWPWD255_EXT1" + "  u32Channel=" + HiChipP2P.HI_P2P_SE_CMD_CHN + "  u32Enable=0" + "  Mode=" + wifiSelectMode + "  ssid=" + ssid + "  psw=" + psw);
                        isNEWPWD255CheckWay = true;
                        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_WIFI_PARAM_NEWPWD255_EXT1, HiChipDefines.HI_P2P_S_WIFI_CHECK_NEWPWD255_EXT1.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, 0, wifiSelectMode, wifiSelectEncType, ssid.getBytes(), psw.getBytes(), 1));
                    } else {
                        //       HiLog.e("==2==", "NewSave--->" + "type=HiChipDefines.HI_P2P_SET_WIFI_PARAM_NEWPWD255_EXT1" + "  u32Channel=" + HiChipP2P.HI_P2P_SE_CMD_CHN + "  u32Enable=0" + "  Mode=" + wifiSelectMode + "  ssid=" + ssid + "  psw=" + psw);
                        isNEWPWD255CheckWay = false;
                        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_WIFI_PARAM_NEWPWD255_EXT1, HiChipDefines.HI_P2P_S_WIFI_CHECK_NEWPWD255_EXT1.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, 1, wifiSelectMode, wifiSelectEncType, ssid.getBytes(), psw.getBytes(), 0));
                    }

                } else {
                    if (psw.getBytes().length > 31) {
                        HiToast.showToast(WifiSetToDeviceActivity.this, getString(R.string.tips_input_tolong));
                        return;
                    }

                    if (ssid.getBytes().length > 31) {

                        HiToast.showToast(WifiSetToDeviceActivity.this, getString(R.string.toast_ssid_tolong));
                        return;
                    }

                    if (mPassWord != null && psw != null && new String(mPassWord).trim().equals(psw)) {
                        startToMain();
                        return;
                    }

                    if (currentWifiIsNotIPCAM()) {
                        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_WIFI_CHECK, HiChipDefines.HI_P2P_S_WIFI_CHECK.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, 1, wifiSelectMode, wifiSelectEncType, ssid.getBytes(), psw.getBytes(), 1));
                    } else {
                        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_WIFI_PARAM, HiChipDefines.HI_P2P_S_WIFI_PARAM.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, 1, wifiSelectMode, wifiSelectEncType, ssid.getBytes(), psw.getBytes()));
                    }

                }
                showLoadingProgress();
                send = true;
                if (currentWifiIsNotIPCAM()) {
                    mHandler.sendEmptyMessageDelayed(WifiSettingActivity.SET_WIFI_END, 10000 * 4);
                } else {
                    mHandler.sendEmptyMessageDelayed(WifiSettingActivity.SET_WIFI_END, 10000);
                }

            }
        });

    }

    private boolean currentWifiIsNotIPCAM() {
        if (mCamera.commandFunction.getAppCmdFunction(CamHiDefines.HI_P2P_SUPPORT_WIFICHECK)) {
           HiLog.e("currentSsid= wired  check...");
            return true;
        }
        return false;
    }

    private void initData() {
        for (MyCamera camera : HiDataValue.CameraList) {
            if (mUid.equals(camera.getUid())) {
                mCamera = camera;
                isSupportFullChar = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_CHAR);
                isSupportLenExt = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_WIFI_PARAM_NEWPWD255_EXT1);
                break;
            }
        }
    }

    private void initView() {

        mTitleView = (TitleView) findViewById(R.id.wifitode_title_top);
        mTitleView.setTitle(getString(R.string.title_wifi_setting));
        mTitleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        mTitleView.setNavigationBarButtonListener(new NavigationBarButtonListener() {

            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        WifiSetToDeviceActivity.this.finish();
                        break;
                }
            }
        });
        mEtPw = (EditText) findViewById(R.id.wifi_to_device_pw);


        mBtnApplication = (Button) findViewById(R.id.wifi_to_devide_application);
        mTvSsid = (TextView) findViewById(R.id.wifi_to_device_tvSsid);
        mTvSsid.setText(mSsid);
        if (mPassWord != null && mPassWord.length > 0) {
            if (type == null) {
                mEtPw.setTransformationMethod(PasswordTransformationMethod.getInstance());
                String string = new String(mPassWord).trim();
                mEtPw.setText(string);
            } else {
                mEtPw.setText("");
            }

        }
        if (isSupportLenExt && isSupportFullChar) {
            mEtPw.setFilters(new InputFilter[]{new InputFilter.LengthFilter(63), new FullCharFilter(this), new EmojiFilter()});
        } else {
            mEtPw.setFilters(new InputFilter[]{new InputFilter.LengthFilter(31), new SpcialCharFilterWIFISET(this), new EmojiFilter()});
        }

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
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        send = false;
    }

    @Override
    public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
        if (arg0 != mCamera)
            return;

        Bundle bundle = new Bundle();
        bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
        Message msg = mHandler.obtainMessage();
        msg.what = HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL;
        msg.obj = arg0;
        msg.arg1 = arg1;
        msg.arg2 = arg3;
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    }

    @Override
    public void receiveSessionState(HiCamera arg0, int arg1) {

    }

    @SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WifiSettingActivity.SET_WIFI_END: {
                   HiLog.e("wifi end go to main");
                    if (!send) {
                        return;
                    }
                    dismissLoadingProgress();
                    Intent intentBroadcast = new Intent();
                    intentBroadcast.setAction(HiDataValue.ACTION_CAMERA_INIT_END);
                    sendBroadcast(intentBroadcast);
                    Intent intent = new Intent(WifiSetToDeviceActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
                break;
                case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:

                    if (msg.arg2 == 0) {
                        Bundle bundle = msg.getData();
                        byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_SET_WIFI_PARAM_NEWPWD255_EXT1:
                                if (isNEWPWD255CheckWay) {
                                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_WIFI_PARAM_NEWPWD255_EXT1, HiChipDefines.HI_P2P_S_WIFI_CHECK_NEWPWD255_EXT1.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, 1, wifiSelectMode, wifiSelectEncType, ssid.getBytes(), psw.getBytes(), 0));
                                    isNEWPWD255CheckWay = false;
                                } else {
                                    startToMain();
                                    isNEWPWD255CheckWay = false;
                                }

                                break;
                            case HiChipDefines.HI_P2P_SET_WIFI_PARAM:
                                startToMain();
                                break;

                            case HiChipDefines.HI_P2P_SET_WIFI_CHECK:
                                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_WIFI_PARAM, HiChipDefines.HI_P2P_S_WIFI_PARAM.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, 1, wifiSelectMode, wifiSelectEncType, ssid.getBytes(), psw.getBytes()));
                                break;
                        }
                    } else {
                        mHandler.removeMessages(WifiSettingActivity.SET_WIFI_END);
                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_SET_WIFI_PARAM_NEWPWD255_EXT1:
                                if (isNEWPWD255CheckWay) {
                                    dismissLoadingProgress();
                                    HiToast.showToast(WifiSetToDeviceActivity.this, getString(R.string.tips_wifi_check_fail));
                                } else {
                                    dismissLoadingProgress();
                                    HiToast.showToast(WifiSetToDeviceActivity.this, getString(R.string.tips_wifi_setting_fail));
                                }

                                isNEWPWD255CheckWay = false;

                                break;
                            case HiChipDefines.HI_P2P_SET_WIFI_PARAM:
                                dismissLoadingProgress();
                                HiToast.showToast(WifiSetToDeviceActivity.this, getString(R.string.tips_wifi_setting_fail));
                                break;
                            case HiChipDefines.HI_P2P_SET_WIFI_CHECK:
                                dismissLoadingProgress();
                                HiToast.showToast(WifiSetToDeviceActivity.this, getString(R.string.tips_wifi_check_fail));
                                break;

                        }
                    }
            }
        }
    };

    private void startToMain() {
        dismissLoadingProgress();
        HiToast.showToast(WifiSetToDeviceActivity.this, getString(R.string.tips_wifi_setting));
        Intent intentBroadcast = new Intent();
        intentBroadcast.setAction(HiDataValue.ACTION_CAMERA_INIT_END);
        sendBroadcast(intentBroadcast);
        Intent intent = new Intent(WifiSetToDeviceActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void getWifiName() {
        if (!HiTools.checkPermission(WifiSetToDeviceActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(WifiSetToDeviceActivity.this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, 0);
        } else {
            getSsid();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getSsid();
            } else {
                finish();
            }
        }
    }

    private void getSsid() {
        currentSsid = SsidUtils.getSSID(this);
    }
}
