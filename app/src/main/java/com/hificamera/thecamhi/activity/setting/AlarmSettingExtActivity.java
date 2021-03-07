package com.hificamera.thecamhi.activity.setting;

import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hificamera.customview.CropImageView;
import com.hificamera.R;
import com.hificamera.hichip.widget.SwitchButton;
import com.hificamera.thecamhi.base.HiToast;
import com.hificamera.thecamhi.base.TitleView;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.MyCamera;
import com.hificamera.thecamhi.main.HiActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class AlarmSettingExtActivity extends HiActivity implements OnClickListener, ICameraIOSessionCallback {

    private MyCamera mCamera;
    HiChipDefines.HI_P2P_S_MD_PARAM md_param = null;
    HiChipDefines.HI_P2P_S_MD_PARAM md_param2 = null;
    HiChipDefines.HI_P2P_S_MD_PARAM md_param3 = null;
    HiChipDefines.HI_P2P_S_MD_PARAM md_param4 = null;

    private SwitchButton togbtn_motion_detection;
    private RadioGroup rg_area;
    private CropImageView cropImageView;
    private LinearLayout ll_motion_area;
    private RelativeLayout rl_move_detaction_sensitivity;
    private SeekBar sb_sensitivity;
    private TextView tv_sensitivity_rate;
    private Button but_application;
    private boolean isFirstSet = true;
    private boolean isFirstGetParams = true;
    private boolean isNeedToast;
    private RadioButton rbtn_left_area;
    private RadioButton rbtn_all_area;
    private RadioButton rbtn_right_area;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_setting_ext);
        initViewAndData();

    }

    private void initViewAndData() {
        String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
        for (MyCamera camera : HiDataValue.CameraList) {
            if (uid.equals(camera.getUid())) {
                mCamera = camera;
                HiChipDefines.HI_P2P_S_MD_PARAM mdparam = new HiChipDefines.HI_P2P_S_MD_PARAM(0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_1, 0, 0, 0, 0, 0, 0));
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam.parseContent());
                HiChipDefines.HI_P2P_S_MD_PARAM mdparam2 = new HiChipDefines.HI_P2P_S_MD_PARAM(0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_2, 0, 0, 0, 0, 0, 0));
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam2.parseContent());
                HiChipDefines.HI_P2P_S_MD_PARAM mdparam3 = new HiChipDefines.HI_P2P_S_MD_PARAM(0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_3, 0, 0, 0, 0, 0, 0));
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam3.parseContent());
                HiChipDefines.HI_P2P_S_MD_PARAM mdparam4 = new HiChipDefines.HI_P2P_S_MD_PARAM(0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_4, 0, 0, 0, 0, 0, 0));
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam4.parseContent());
                break;
            }
        }
        initView();
        setListeners();

    }

    private void setListeners() {
        rg_area.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbtn_left_area:
                        Log.e("cropImageView", "is 1=" + rbtn_left_area.isChecked());
                        cropImageView.setArea(1);
                        break;
                    case R.id.rbtn_all_area:
                        Log.e("cropImageView", "is 2=" + rbtn_all_area.isChecked());
                        cropImageView.setArea(2);
                        break;
                    case R.id.rbtn_right_area:
                        Log.e("cropImageView", "is 3=" + rbtn_right_area.isChecked());
                        cropImageView.setArea(3);
                        break;
                }
            }
        });
        sb_sensitivity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress >= 1 && progress <= 100) {
                    tv_sensitivity_rate.setText(progress + "");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        but_application.setOnClickListener(this);

    }

    private void initView() {
        TitleView title = (TitleView) findViewById(R.id.title_top);
        title.setTitle(getResources().getString(R.string.title_alarm_motion_detection));
        title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
        title.setNavigationBarButtonListener(new TitleView.NavigationBarButtonListener() {
            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        AlarmSettingExtActivity.this.finish();
                        break;
                    case TitleView.NAVIGATION_BUTTON_RIGHT:
                        break;

                }

            }
        });
        togbtn_motion_detection = (SwitchButton) findViewById(R.id.togbtn_motion_detection);
        rl_move_detaction_sensitivity = (RelativeLayout) findViewById(R.id.rl_move_detaction_sensitivity);
        rg_area = (RadioGroup) findViewById(R.id.rg_area);
        cropImageView = (CropImageView) findViewById(R.id.cropimageview);
        ll_motion_area = (LinearLayout) findViewById(R.id.ll_motion_area);
        sb_sensitivity = (SeekBar) findViewById(R.id.sb_sensitivity);
        tv_sensitivity_rate = (TextView) findViewById(R.id.tv_sensitivity_rate);
        but_application = (Button) findViewById(R.id.but_application);

        rbtn_left_area = (RadioButton) findViewById(R.id.rbtn_left_area);
        rbtn_all_area = (RadioButton) findViewById(R.id.rbtn_all_area);
        rbtn_right_area = (RadioButton) findViewById(R.id.rbtn_right_area);
        if (mCamera != null) {
            cropImageView.getMainSteam(mCamera.u32Resolution);
        }
        togbtn_motion_detection = (SwitchButton) findViewById(R.id.togbtn_motion_detection);
        sb_sensitivity.setMax(100);

        togbtn_motion_detection.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked && !isFirstGetParams) {

                    if (isFirstSet) {
                        ll_motion_area.postDelayed(new Runnable() {

                            public void run() {
                                sendMotionDetection();
                            }
                        }, 1000);
                        isFirstSet = false;
                    }

                    showjuHuaDialog();
                    HiChipDefines.HI_P2P_S_MD_PARAM mdparam = new HiChipDefines.HI_P2P_S_MD_PARAM(0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_1, 0, 0, 0, 0, 0, 0));
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam.parseContent());
                    HiChipDefines.HI_P2P_S_MD_PARAM mdparam2 = new HiChipDefines.HI_P2P_S_MD_PARAM(0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_2, 0, 0, 0, 0, 0, 0));
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam2.parseContent());
                    HiChipDefines.HI_P2P_S_MD_PARAM mdparam3 = new HiChipDefines.HI_P2P_S_MD_PARAM(0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_3, 0, 0, 0, 0, 0, 0));
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam3.parseContent());
                    HiChipDefines.HI_P2P_S_MD_PARAM mdparam4 = new HiChipDefines.HI_P2P_S_MD_PARAM(0, new HiChipDefines.HI_P2P_S_MD_AREA(HiChipDefines.HI_P2P_MOTION_AREA_4, 0, 0, 0, 0, 0, 0));
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_MD_PARAM, mdparam4.parseContent());

                    ll_motion_area.setVisibility(View.VISIBLE);
                    rl_move_detaction_sensitivity.setVisibility(View.VISIBLE);
                    but_application.setVisibility(View.VISIBLE);

                } else {

                    ll_motion_area.setVisibility(View.GONE);
                    rl_move_detaction_sensitivity.setVisibility(View.GONE);
                    but_application.setVisibility(View.GONE);
                    //   rbtn_left_area.setChecked(false);
                    //   rbtn_all_area.setChecked(false);
                    //  rbtn_right_area.setChecked(false);
                    rg_area.clearCheck();
                    ll_motion_area.postDelayed(new Runnable() {

                        public void run() {
                            sendMotionDetection();
                        }
                    }, 1000);

                }

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.but_application:
                showjuHuaDialog();
                isNeedToast = true;
                sendMotionDetection();
                break;
        }
    }

    protected void sendMotionDetection() {
        if (md_param == null && md_param2 == null && md_param3 == null && md_param4 == null) {
            return;
        }
        int guard_switch = togbtn_motion_detection.isChecked() ? 1 : 0;
        md_param.struArea.u32Enable = guard_switch;
        int md = Integer.parseInt(tv_sensitivity_rate.getText().toString());
        md_param.struArea.u32Sensi = md;
        md_param.struArea.u32Width = cropImageView.getmDrawableFloatWidth();
        md_param.struArea.u32Height = cropImageView.getmDrawableFloatHeight();
        md_param.struArea.u32Y = cropImageView.getmDrawableFloatY();
        md_param.struArea.u32X = cropImageView.getmDrawableFloatX();


        mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_MD_PARAM_NEW, md_param.parseContent());
        if (!togbtn_motion_detection.isChecked() && md_param2 != null) {
            md_param2.struArea.u32Enable = 0;
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_MD_PARAM_NEW, md_param2.parseContent());
        }
        if (!togbtn_motion_detection.isChecked() && md_param3 != null) {
            md_param3.struArea.u32Enable = 0;
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_MD_PARAM_NEW, md_param3.parseContent());
        }
        if (!togbtn_motion_detection.isChecked() && md_param4 != null) {
            md_param4.struArea.u32Enable = 0;
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_MD_PARAM_NEW, md_param4.parseContent());
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

    @Override
    public void receiveSessionState(HiCamera arg0, int arg1) {

    }

    @SuppressLint("HandlerLeak") private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL: {
                    if (msg.arg2 == 0) {
                        // MyCamera camera = (MyCamera)msg.obj;
                        Bundle bundle = msg.getData();
                        byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_GET_MD_PARAM:
                                dismissjuHuaDialog();
                                HiChipDefines.HI_P2P_S_MD_PARAM md_param_temp = new HiChipDefines.HI_P2P_S_MD_PARAM(data);
                                if (md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_1) {
                                    md_param = md_param_temp;
                                    // togbtn_motion_detection.setChecked(md_param.struArea.u32Enable == 1 ? true :
                                    // false);

                                    int sensitivity = md_param.struArea.u32Sensi;
                                    if (sensitivity >= 1 && sensitivity <= 100) {
                                        sb_sensitivity.setProgress(sensitivity);
                                        tv_sensitivity_rate.setText(sensitivity + "");
                                    }
                                    handSensiArea();

                                } else if (md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_2) {
                                    md_param2 = md_param_temp;
                                } else if (md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_3) {
                                    md_param3 = md_param_temp;
                                } else if (md_param_temp.struArea.u32Area == HiChipDefines.HI_P2P_MOTION_AREA_4) {
                                    md_param4 = md_param_temp;
                                }

                                if (md_param.struArea.u32Enable == 1) {
                                    togbtn_motion_detection.setChecked(true);
                                    ll_motion_area.setVisibility(View.VISIBLE);
                                    rl_move_detaction_sensitivity.setVisibility(View.VISIBLE);
                                    but_application.setVisibility(View.VISIBLE);
                                }

                                isFirstGetParams = false;
                                break;
                            case HiChipDefines.HI_P2P_SET_MD_PARAM_NEW:
                                if (isNeedToast && togbtn_motion_detection.isChecked()) {
                                    HiToast.showToast(AlarmSettingExtActivity.this, getString(R.string.application_success));
                                    isNeedToast = false;
                                }
                                dismissjuHuaDialog();

                                break;

                        }
                    } else {
                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_SET_MD_PARAM_NEW:
                                HiToast.showToast(AlarmSettingExtActivity.this, getString(R.string.application_fail));
                                dismissjuHuaDialog();
                                break;
                        }
                    }
                }
            }
        }
    };

    private void handSensiArea() {
        if (md_param != null) {
            int X = md_param.struArea.u32X;
            int Y = md_param.struArea.u32Y;
            int width = md_param.struArea.u32Width;
            int height = md_param.struArea.u32Height;

            if (width < cropImageView.mainSteamWidth / 5) {
                width = (cropImageView.mainSteamWidth / 5);
            }
            if (height < cropImageView.mainSteamHeight / 5) {
                height = (cropImageView.mainSteamHeight / 5);
            }
            cropImageView.mMinFloatHeight = cropImageView.getHeight() / 5;
            cropImageView.mMinFloatWidth = cropImageView.getWidth() / 5;

            if (mCamera.snapshot != null) {
                cropImageView.setDrawable(mCamera.snapshot, width, height, X, Y);
            } else {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.videoclip);
                cropImageView.setDrawable(bitmap, width, height, X, Y);
            }

        }
    }

}
