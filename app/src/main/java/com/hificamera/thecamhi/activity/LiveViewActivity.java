package com.hificamera.thecamhi.activity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.hificamera.R;
import com.hichip.base.HiLog;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.callback.ICameraPlayStateCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.content.HiChipDefines.ABSOLUTE_LIGHT_TYPE;
import com.hichip.content.HiChipDefines.HI_P2P_WHITE_LIGHT_INFO;
import com.hichip.content.HiChipDefines.HI_P2P_WHITE_LIGHT_INFO_EXT;
import com.hichip.control.HiCamera;
import com.hificamera.hichip.widget.HumanRectView;
import com.hificamera.hichip.widget.SwitchButton;
import com.hichip.sdk.HiChipP2P;
import com.hificamera.thecamhi.base.HiToast;
import com.hificamera.thecamhi.base.HiTools;
import com.hificamera.thecamhi.base.MyLiveViewGLMonitor;
import com.hificamera.thecamhi.bean.CamHiDefines;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.HumanRect;
import com.hificamera.thecamhi.bean.MyCamera;
import com.hificamera.thecamhi.main.HiActivity;
import com.hificamera.thecamhi.model.LiveViewModel;
import com.hificamera.thecamhi.utils.SystemUtils;
import com.hichip.tools.Packet;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class LiveViewActivity extends HiActivity implements ICameraIOSessionCallback, ICameraPlayStateCallback, OnClickListener, OnTouchListener {

    private MyCamera mCamera;
    private MyLiveViewGLMonitor mMonitor;
    private HI_P2P_WHITE_LIGHT_INFO_EXT light_info_ext, audible_alarm;
    private HI_P2P_WHITE_LIGHT_INFO light_info;
    private ABSOLUTE_LIGHT_TYPE abs_light;
    private int video_width;
    private int video_height;
    private ProgressBar prs_loading;
    private ImageView img_shade;
    private ImageView btn_live_listen;
    private ImageView btn_live_exit;
    private ImageView btn_live_mirror_flip;
    private ImageView btn_live_preset;
    private ImageView btn_live_record;
    private ImageView btn_live_snapshot;
    private ImageView resolution_ratio;
    private ImageView btn_live_zoom_focus;
    private ImageView btn_live_light;
    private ImageView btn_microphone, iv_alarm, iv_alarm_mark;
    private ImageView iv_zoomin, iv_zoomout, iv_focusin, iv_focusout;
    private LinearLayout linearLayout1, lay_live_tools_bottom, btn_live_light_layout, ll_alarm;
    private TextView txt_recording;
    private ImageView mIvRecording;
    private PopupWindow mPopupWindow;
    private boolean isListening = false;
    private boolean isTalking = false;
    private final static int RECORDING_STATUS_NONE = 0;
    private final static int RECORDING_STATUS_LOADING = 1;
    private final static int RECORDING_STATUS_ING = 2;
    private int mRecordingState = RECORDING_STATUS_NONE;
    private boolean visible = false;
    FrameLayout live_view_screen = null;
    private long oldClickTime;
    private int lightModel = 0;// 0 non ,1 HI_P2P_WHITE_LIGHT_GET_EXT ,2

    private boolean mVoiceIsTran = false;
    private int action = 0;
    private Timer timer;
    private TimerTask timerTask;
    private int mCameraVideoQuality;
    private ImageView mIvLoading2;
    protected String recordFile;
    private boolean isMF = false;
    private boolean isRing = false;
    private boolean mIsSwitchResolution = false;

    private List<Toast> toastList1 = new ArrayList<>();
    private List<Toast> toastList2 = new ArrayList<>();
    private List<Toast> toastList3 = new ArrayList<>();
    public HumanRectView humanRectView;
    private int monitor_width;
    private int monitor_height;

    private List<HumanRect> humanRects = new ArrayList<>();
    private boolean receiveHumanParams = false;
    private long preHumanTime;
    private Timer mTimer;
    private TimerTask mTask;
    public static final int RECEIVE_HUMAN_DATA = 100;
    HumanReceiver humanReceiver;
    private String HUMANTAG = "human.receiver";
    private LinearLayout mLlTrack;
    private ImageView iv_live_track;
    protected HiChipDefines.HI_P2P_SMART_MOTO trackParam;
    private SwitchButton swiBtnTrack;
    private LinearLayout ll_4g_signal, ll_signal;
    private TextView tv_operator, tv_signal;
    private ImageView iv_signal;
    private boolean reGetSignal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HiTools.hideVirtualKey(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null) {
            String uid = bundle.getString(HiDataValue.EXTRAS_KEY_UID);
            for (MyCamera camera : HiDataValue.CameraList) {
                if (camera.getUid().equals(uid)) {
                    mCamera = camera;
                    mCamera.registerIOSessionListener(this);
                    mCamera.registerPlayStateListener(this);
                    mCameraVideoQuality = mCamera.getVideoQuality();
                    break;
                }
            }
        }
        HiTools.cameraWhetherNull(this, mCamera);
        getLightModel();
        initView();
        // getAlarmMode();
        getTrack();
        showLoadingView();
        getData();

    }

    private void getG4Info() {
        if (mCamera != null) {
            if (mCamera.commandFunction.getAppCmdFunction(CamHiDefines.HI_P2P_SUPPORT_4G)||mCamera.commandFunction.getAppCmdFunction(CamHiDefines.HI_P2P_SUPPORT_5G)) {
                if (SystemUtils.isZh(this)) {
                    mCamera.sendIOCtrl(CamHiDefines.HI_P2P_GET_SIGNAL_OPERATOR, new byte[0]);
                } else {
                    mCamera.sendIOCtrl(CamHiDefines.HI_P2P_GET_SIGNAL_OPERATOR_EXT, new byte[0]);
                }
            }
        }
    }

    private void getTrack() {
        mLlTrack = (LinearLayout) findViewById(R.id.btn_live_track);
        if (mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_SMART_MOTO)) {
            mLlTrack.setVisibility(View.VISIBLE);
        } else {
            mLlTrack.setVisibility(View.GONE);
        }

    }

    private void getData() {
        humanReceiver = new HumanReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(HUMANTAG);
        registerReceiver(humanReceiver, intentFilter);
        mTimer = new Timer();
        mTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long currentTime = System.currentTimeMillis();
                        if ((currentTime - preHumanTime) > 1000) {
                            humanRectView.cleanRect();
                        }
                    }
                });
            }
        };
    }

    private void startFrameAnimation(ImageView iv) {
        AnimationDrawable aw = new AnimationDrawable();
        aw.addFrame(getResources().getDrawable(R.drawable.alarm_1), 100);
        aw.addFrame(getResources().getDrawable(R.drawable.alarm_2), 100);
        aw.addFrame(getResources().getDrawable(R.drawable.alarm_3), 100);
        aw.addFrame(getResources().getDrawable(R.drawable.alarm_4), 100);
        aw.addFrame(getResources().getDrawable(R.drawable.alarm_5), 100);

        iv.setBackground(aw);
        aw.setOneShot(false);
        aw.start();
    }

    private void getLightModel() {
        if (mCamera == null) {
            return;
        }
        boolean b = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_AUDIBLE_VISUAL_ALARM_TYPE);
        if (b) {
            lightModel = 4;
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_AUDIBLE_VISUAL_ALARM_TYPE, null);
            return;
        }
        b = mCamera.getCommandFunction(HiChipDefines.HI_P2P_GET_ABSOLUTE_LIGHT_TYPE);
        if (b) {
            lightModel = 3;
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_ABSOLUTE_LIGHT_TYPE, null);
            return;
        }
        b = mCamera.getCommandFunction(HiChipDefines.HI_P2P_WHITE_LIGHT_GET_EXT);
        if (b) {
            lightModel = 1;
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_GET_EXT, null);
            return;
        }
        b = mCamera.getCommandFunction(HiChipDefines.HI_P2P_WHITE_LIGHT_GET);
        if (b) {
            lightModel = 2;
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_GET, null);
        }
    }

    private void setViewVisible(boolean visible) {
        linearLayout1.setVisibility(visible ? View.VISIBLE : View.GONE);
        lay_live_tools_bottom.setVisibility(visible ? View.VISIBLE : View.GONE);
        this.visible = !visible;
    }

    private void initView() {
        setContentView(R.layout.activity_live_view_landscape);
        mMonitor = (MyLiveViewGLMonitor) findViewById(R.id.monitor_live_view);
        mMonitor.setOnTouchListener(this);
        mMonitor.setCamera(mCamera);
        mCamera.setLiveShowMonitor(mMonitor);
        btn_live_exit = (ImageView) findViewById(R.id.btn_live_exit);
        btn_live_exit.setOnClickListener(this);

        linearLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);
        lay_live_tools_bottom = (LinearLayout) findViewById(R.id.lay_live_tools_bottom);

        btn_live_snapshot = (ImageView) findViewById(R.id.btn_live_snapshot);
        btn_live_snapshot.setOnClickListener(this);

        btn_live_record = (ImageView) findViewById(R.id.btn_live_record);
        btn_live_record.setOnClickListener(this);

        txt_recording = (TextView) findViewById(R.id.txt_recording);
        mIvRecording = (ImageView) findViewById(R.id.iv_recording);

        btn_live_listen = (ImageView) findViewById(R.id.btn_live_listen);
        btn_live_listen.setOnClickListener(this);

        btn_microphone = (ImageView) findViewById(R.id.btn_microphone);
        btn_microphone.setOnTouchListener(this);
        btn_microphone.setVisibility(View.GONE);

        resolution_ratio = (ImageView) findViewById(R.id.resolution_ratio);
        resolution_ratio.setOnClickListener(this);

        btn_live_zoom_focus = (ImageView) findViewById(R.id.btn_live_zoom_focus);
        btn_live_zoom_focus.setOnClickListener(this);

        btn_live_preset = (ImageView) findViewById(R.id.btn_live_preset);
        btn_live_preset.setOnClickListener(this);

        btn_live_mirror_flip = (ImageView) findViewById(R.id.btn_live_mirror_flip);
        btn_live_mirror_flip.setOnClickListener(this);

        btn_live_light = (ImageView) findViewById(R.id.btn_live_light);
        btn_live_light.setOnClickListener(this);
        btn_live_light_layout = (LinearLayout) findViewById(R.id.btn_live_light_layout);
        btn_live_light_layout.setVisibility(lightModel == 0 ? View.GONE : View.VISIBLE);

        img_shade = (ImageView) findViewById(R.id.img_shade);
        mIvLoading2 = (ImageView) findViewById(R.id.iv_loading2);

        ll_alarm = (LinearLayout) findViewById(R.id.ll_alarm);
        iv_alarm = (ImageView) findViewById(R.id.iv_alarm);
        iv_alarm.setOnClickListener(this);
        iv_alarm_mark = (ImageView) findViewById(R.id.iv_alarm_mark);

        humanRectView = findViewById(R.id.humanRectView);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight());
        //mMonitor.setLayoutParams(params);
        humanRectView.setLayoutParams(params);

        iv_live_track = findViewById(R.id.iv_live_track);
        iv_live_track.setOnClickListener(this);

        ll_4g_signal = findViewById(R.id.ll_4g_signal);
        ll_signal = findViewById(R.id.ll_signal);
        tv_operator = findViewById(R.id.tv_operator);
        tv_signal = findViewById(R.id.tv_signal);
        iv_signal = findViewById(R.id.iv_signal);

        iv_zoomin = findViewById(R.id.iv_zoomin);
        iv_zoomout = findViewById(R.id.iv_zoomout);
        iv_focusin = findViewById(R.id.iv_focusin);
        iv_focusout = findViewById(R.id.iv_focusout);

        iv_zoomin.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_ZOOMIN, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_STOP, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                }
                return false;
            }
        });
        iv_zoomout.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_ZOOMOUT, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_STOP, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                }
                return false;
            }
        });
        iv_focusin.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_FOCUSIN, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_STOP, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                }
                return false;
            }
        });
        iv_focusout.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_FOCUSOUT, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_STOP, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                }
                return false;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mCamera != null) {
            new Thread() {
                public void run() {
                    mCamera.startLiveShow(mCamera.getVideoQuality(), mMonitor);
                }

                ;
            }.start();

            mCamera.registerIOSessionListener(this);
            mCamera.registerPlayStateListener(this);
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    public void onPause() {
        cancelToast(toastList1);
        cancelToast(toastList2);
        cancelToast(toastList3);

        super.onPause();
        receiveHumanParams = false;
        mVoiceIsTran = false;
        if (isRing) {
            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_RING, HiChipDefines.HI_P2P_RING_Fun.parseContent(0));
        }
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }

        if (mCamera != null) {
            mCamera.stopLiveShow();
            mCamera.unregisterPlayStateListener(this);
            mCamera.unregisterIOSessionListener(this);
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (action == 0) {
            finish();
        }

    }

    private void cancelToast(List<Toast> list) {
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i) != null) {
                    list.get(i).cancel();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.stopListening();
        mVoiceIsTran = false;
        handler.removeCallbacksAndMessages(null);
        mHandler.removeCallbacksAndMessages(null);
        HiLog.e("----------------------LiveViewActivity onDestroy--------------------------");
        if (mPopupWindow != null && mPopupWindow.isShowing()) {// change by donint
            mPopupWindow.dismiss();
        }

        mTimer.cancel();
        mTimer = null;
        mTask = null;
        unregisterReceiver(humanReceiver);
    }

    @Override
    public void onBackPressed() {
        action = 1;
        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        finish();

    }

    private boolean isSaveSnapshot;

    @SuppressLint("HandlerLeak") private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HiDataValue.HANDLE_MESSAGE_SESSION_STATE: {
                    switch (msg.arg1) {
                        case HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED:
                            if (!mIsSwitchResolution) {
                                finish();
                            }

                            if (mCamera != null) {
                                mCamera.stopLiveShow();
                                if (isListening) {
                                    isListening = false;
                                    btn_live_listen.setImageResource(R.drawable.camhi_live_normal_speaker);
                                    btn_microphone.setVisibility(View.GONE);
                                }
                            }
                            break;
                        case HiCamera.CAMERA_CONNECTION_STATE_LOGIN:
                            mIsSwitchResolution = false;
                            if (mCameraVideoQuality != mCamera.getVideoQuality()) {
                                mCamera.stopRecording();
                                btn_live_record.setImageResource(R.drawable.camhi_live_normal_recording);
                                mRecordingState = RECORDING_STATUS_NONE;

                                if (timer != null) {
                                    timer.cancel();
                                    timer = null;
                                }
                                if (timerTask != null) {
                                    timerTask.cancel();
                                    timerTask = null;
                                }
                                mCameraVideoQuality = mCamera.getVideoQuality();
                            }
                            new Thread() {
                                public void run() {
                                    mCamera.startLiveShow(mCamera.getVideoQuality(), mMonitor);
                                }

                                ;
                            }.start();
                            break;
                        case HiCamera.CAMERA_CONNECTION_STATE_WRONG_PASSWORD:
                            break;
                        case HiCamera.CAMERA_CONNECTION_STATE_CONNECTING:
                            break;
                        case HiCamera.CAMERA_CHANNEL_STREAM_ERROR:
                            if (mCamera != null) {
                                mCamera.stopLiveShow();
                                if (isListening) {
                                    isListening = false;
                                    btn_live_listen.setImageResource(R.drawable.camhi_live_normal_speaker);
                                    btn_microphone.setVisibility(View.GONE);
                                }
                            }
                            break;
                    }
                }
                break;
                case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL: {
                    if (msg.arg2 == 0) {
                        handReceiveIoCtrlSuccess(msg);
                    } else if (msg.arg2 == -1) {
                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_SET_PTZ_PRESET:
                                if (LiveViewModel.getInstance().mFlagPreset == 1) {
                                    HiToast.showToast(LiveViewActivity.this, getString(R.string.tip_preset_fail));
                                }
                                break;
                            case HiChipDefines.HI_P2P_GET_ABSOLUTE_LIGHT_TYPE:
                                break;
                        }
                    }
                }
                break;
                case HiDataValue.HANDLE_MESSAGE_PLAY_STATE:
                    setViewWhetherClick(true);
                    Bundle bundle = msg.getData();
                    int command = bundle.getInt("command");
                    switch (command) {
                        case ICameraPlayStateCallback.PLAY_STATE_START:
                            dismissLoadingView();
                            video_width = bundle.getInt("width");
                            video_height = bundle.getInt("height");
                            Bitmap frame = null;

                            if (!isSaveSnapshot) {
                                try {
                                    frame = mCamera != null ? mCamera.getSnapshot_EXT(1, 640, 352) : null;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                if (frame != null) {
                                    saveSnapshot(frame);
                                    isSaveSnapshot = true;
                                }
                            }


                            break;

                        case ICameraPlayStateCallback.PLAY_STATE_RECORDING_START:
                            //					mRecordingState = RECORDING_STATUS_ING;
                            //					txt_recording.setVisibility(View.VISIBLE);
                            //					mIvRecording.setVisibility(View.VISIBLE);
                            //					mHandler.sendEmptyMessage(110);
                            break;
                        case ICameraPlayStateCallback.PLAY_STATE_RECORDING_END:
                            //					mRecordingState = RECORDING_STATUS_NONE;
                            //					txt_recording.setVisibility(View.GONE);
                            //					mIvRecording.setVisibility(View.GONE);
                            //					mHandler.removeCallbacksAndMessages(null);
                            //					if (!TextUtils.isEmpty(recordFile)) {
                            //						File file = new File(recordFile);
                            //						if (file.length() <= 1024 && file.isFile() && file.exists()) {
                            //							file.delete();
                            //						}
                            //					}
                            break;
                        case ICameraPlayStateCallback.PLAY_STATE_RECORD_ERROR:
                            mRecordingState = RECORDING_STATUS_NONE;
                            txt_recording.setVisibility(View.GONE);
                            mIvRecording.setVisibility(View.GONE);
                            mHandler.removeCallbacksAndMessages(null);
                            if (!TextUtils.isEmpty(recordFile)) {
                                File file = new File(recordFile);
                                if (file.length() <= 1024 && file.isFile() && file.exists()) {
                                    file.delete();
                                }
                            }
                            break;
                    }
                    break;

            }

        }

        private void handReceiveIoCtrlSuccess(Message msg) {
            MyCamera camera = (MyCamera) msg.obj;
            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
            switch (msg.arg1) {
                case HiChipDefines.HI_P2P_START_LIVE:
                    getG4Info();
                    receiveHumanParams = true;
                    int width = Packet.byteArrayToInt_Little(data, 4);
                    int heigth = Packet.byteArrayToInt_Little(data, 8);
                    monitor_width = Packet.byteArrayToInt_Little(data, 4);
                    monitor_height = Packet.byteArrayToInt_Little(data, 8);
                    if (width <= 0 || heigth <= 0 || width > 5000 || heigth > 5000) {
                        LiveViewActivity.this.finish();
                        HiToast.showToast(LiveViewActivity.this, getString(R.string.tips_open_video_fail));
                    }
                    break;
                case HiChipDefines.HI_P2P_SET_PTZ_PRESET:
                    int state = LiveViewModel.getInstance().mFlagPreset;
                    if (state == 1) {
                        HiToast.showToast(LiveViewActivity.this, getString(R.string.tips_preset_set_btn));
                    } else if (state == 2) {
                    }
                    break;
                case HiChipDefines.HI_P2P_GET_DISPLAY_PARAM:
                    if (isMF) {
                        if (display_param != null) {
                            dismissLoadingView();
                            display_param = new HiChipDefines.HI_P2P_S_DISPLAY(data);
                            display_param.u32Mirror = mirrorChecked;
                            display_param.u32Flip = filpChecked;
                            mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_DISPLAY_PARAM, display_param.parseContent());
                        }
                    } else {
                        if (data != null) {
                            display_param = new HiChipDefines.HI_P2P_S_DISPLAY(data);
                        }
                        mirrorChecked = display_param.u32Mirror;
                        filpChecked = display_param.u32Flip;
                        clickMirrorFlip(btn_live_mirror_flip);
                    }
                    isMF = false;
                    break;
                case HiChipDefines.HI_P2P_GET_AUDIBLE_VISUAL_ALARM_TYPE:
                    if (lightModel != 4) {
                        return;
                    }
                    audible_alarm = new HI_P2P_WHITE_LIGHT_INFO_EXT(data);
                    break;
                case HiChipDefines.HI_P2P_WHITE_LIGHT_GET:
                    if (lightModel != 2) {
                        return;
                    }
                    light_info = new HI_P2P_WHITE_LIGHT_INFO(data);

                    break;
                case HiChipDefines.HI_P2P_WHITE_LIGHT_GET_EXT:
                    if (lightModel != 1) {
                        return;
                    }
                    light_info_ext = new HI_P2P_WHITE_LIGHT_INFO_EXT(data);
                    break;
                case HiChipDefines.HI_P2P_GET_ABSOLUTE_LIGHT_TYPE:
                    if (lightModel != 3)
                        return;
                    abs_light = new ABSOLUTE_LIGHT_TYPE(data);
                    break;
                // +++
                case HiChipDefines.HI_P2P_RING:
                    isRing = !isRing;
                    if (isRing) {
                        iv_alarm_mark.setVisibility(View.VISIBLE);
                        startFrameAnimation(iv_alarm_mark);
                        iv_alarm.setSelected(true);
                    } else {
                        iv_alarm_mark.setVisibility(View.GONE);
                        iv_alarm.setSelected(false);
                    }
                    break;
                case HiChipDefines.HI_P2P_GET_RING:
                    HiChipDefines.HI_P2P_RING_Fun fun = new HiChipDefines.HI_P2P_RING_Fun(data);
                    if (fun.enable == 1) {
                        isRing = !isRing;
                        iv_alarm_mark.setVisibility(View.VISIBLE);
                        startFrameAnimation(iv_alarm_mark);
                        iv_alarm.setSelected(true);
                    } else {
                        iv_alarm_mark.setVisibility(View.GONE);
                        iv_alarm.setSelected(false);
                    }
                    break;
                case CamHiDefines.HI_P2P_ALARM_HSR:
                    if (receiveHumanParams) {
                        Intent intent = new Intent(HUMANTAG);
                        intent.putExtra("DATA", data);
                        sendBroadcast(intent);
                    }
                    break;
                case HiChipDefines.HI_P2P_GET_SMART_MOTO:
                    trackParam = new HiChipDefines.HI_P2P_SMART_MOTO(data);
                    clickTrack(iv_live_track);
                    break;
                case HiChipDefines.HI_P2P_SET_SMART_MOTO:
                    dismissjuHuaDialog(); //dismissLoadingProgress();
                    break;
                case CamHiDefines.HI_P2P_GET_SIGNAL_OPERATOR://4g
                    CamHiDefines.HI_P2P_SIGNAL_OPERATOR operator = new CamHiDefines.HI_P2P_SIGNAL_OPERATOR(data);
                    Log.e("4g", "cn:" + operator.toString());
                    if (operator.s32ShowName == 1 || operator.s32ShowSignal == 1) {
                        ll_4g_signal.setVisibility(View.VISIBLE);
                    } else {
                        ll_4g_signal.setVisibility(View.GONE);
                    }
                    if (operator.s32ShowSignal == 1 && operator.s32SignalQuality > 0) {
                        ll_signal.setVisibility(View.VISIBLE);
                        if (operator.s32SignalQuality >= 0 && operator.s32SignalQuality <= 40) {
                            iv_signal.setImageResource(R.drawable.signal_2);
                        } else if (operator.s32SignalQuality > 40 && operator.s32SignalQuality <= 60) {
                            iv_signal.setImageResource(R.drawable.signal_3);
                        } else if (operator.s32SignalQuality > 60 && operator.s32SignalQuality <= 80) {
                            iv_signal.setImageResource(R.drawable.signal_4);
                        } else {
                            iv_signal.setImageResource(R.drawable.signal_5);
                        }
                    }
                    if (operator.s32ShowName == 1) {
                        tv_operator.setVisibility(View.VISIBLE);
                        String brand = "";
                        switch (operator.s32OperatorName) {
                            case 0:
                                brand = getString(R.string.cmcc);
                                break;
                            case 1:
                                brand = getString(R.string.cucc);
                                break;
                            case 2:
                                brand = getString(R.string.ctcc);
                                break;
                            default:
                                brand = getString(R.string.none);
                                getOneMoreTime();
                                break;
                        }
                        tv_operator.setText(brand);
                    }
                    break;
                case CamHiDefines.HI_P2P_GET_SIGNAL_OPERATOR_EXT:
                    CamHiDefines.HI_P2P_GET_SIGNAL_OPERATOR_EXT operator_ext = new CamHiDefines.HI_P2P_GET_SIGNAL_OPERATOR_EXT(data);
                    Log.e("4g", "cn:" + operator_ext.toString());


                    if (operator_ext.s32ShowName == 1 || operator_ext.s32ShowSignal == 1) {
                        ll_4g_signal.setVisibility(View.VISIBLE);
                    } else {
                        ll_4g_signal.setVisibility(View.GONE);
                    }
                    if (operator_ext.s32ShowSignal == 1 && operator_ext.s32SignalQuality > 0) {
                        ll_signal.setVisibility(View.VISIBLE);
                        if (operator_ext.s32SignalQuality >= 0 && operator_ext.s32SignalQuality <= 40) {
                            iv_signal.setImageResource(R.drawable.signal_2);
                        } else if (operator_ext.s32SignalQuality > 40 && operator_ext.s32SignalQuality <= 60) {
                            iv_signal.setImageResource(R.drawable.signal_3);
                        } else if (operator_ext.s32SignalQuality > 60 && operator_ext.s32SignalQuality <= 80) {
                            iv_signal.setImageResource(R.drawable.signal_4);
                        } else {
                            iv_signal.setImageResource(R.drawable.signal_5);
                        }
                    }
                    if (operator_ext.s32ShowName == 1) {
                        tv_operator.setVisibility(View.VISIBLE);
                        String str = new String(operator_ext.s32OperatorName).trim();
                        if (str.equals("") || TextUtils.isEmpty(str))
                            str = "unknown";
                        tv_operator.setVisibility(View.VISIBLE);
                        tv_operator.setText(str);

                        if (str.equals("unknown")) {
                            getOneMoreTime();
                        }
                    }
                    break;
            }
        }
    };

    private void getOneMoreTime() {
        if (!reGetSignal) {
            reGetSignal = true;
            if (mHandler != null)
                mHandler.postDelayed(this::getG4Info, 5000);

        }
    }

    @SuppressLint("StaticFieldLeak")
    private void saveSnapshot(final Bitmap frame) {

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... arg0) {
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

                    File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
                    File sargetFolder = new File(rootFolder.getAbsolutePath() + "/android/data/" + getResources().getString(R.string.app_name));

                    if (!rootFolder.exists()) {
                        rootFolder.mkdirs();
                    }
                    if (!sargetFolder.exists()) {
                        sargetFolder.mkdirs();
                    }

                    HiTools.saveBitmap(frame, sargetFolder.getAbsolutePath() + "/" + mCamera.getUid());
                    HiLog.v(sargetFolder.getAbsolutePath() + "/" + mCamera.getUid());
                    mCamera.snapshot = frame;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void result) {
                Intent intent = new Intent();
                intent.setAction(HiDataValue.ACTION_CAMERA_INIT_END);
                sendBroadcast(intent);
                super.onPostExecute(result);
            }
        }.execute();

    }

    int moveX;
    int moveY;

    private void resetMonitorSize(boolean large, double move) {

        if (mMonitor.height == 0 && mMonitor.width == 0) {

            initMatrix((int) mMonitor.screen_width, (int) mMonitor.screen_height);
        }

        moveX = (int) (move / 2);
        moveY = (int) ((move * mMonitor.screen_height / mMonitor.screen_width) / 2);

        if (large) {
            HiLog.e(" larger and larger ");
            if (mMonitor.width <= 2 * mMonitor.screen_width && mMonitor.height <= 2 * mMonitor.screen_height) {

                mMonitor.left -= (moveX / 2);
                mMonitor.bottom -= (moveY / 2);
                mMonitor.width += (moveX);
                mMonitor.height += (moveY);
            }
        } else {
            HiLog.e(" smaller and smaller ");

            mMonitor.left += (moveX / 2);
            mMonitor.bottom += (moveY / 2);
            mMonitor.width -= (moveX);
            mMonitor.height -= (moveY);
        }

        if (mMonitor.left > 0 || mMonitor.width < (int) mMonitor.screen_width || mMonitor.height < (int) mMonitor.screen_height || mMonitor.bottom > 0) {
            initMatrix((int) mMonitor.screen_width, (int) mMonitor.screen_height);
        }

        HiLog.e("mMonitor.left=" + mMonitor.left + " mMonitor.bottom=" + mMonitor.bottom + "\n mMonitor.width=" + mMonitor.width + " mMonitor.height=" + mMonitor.height);

        if (mMonitor.width > (int) mMonitor.screen_width) {
            mMonitor.setState(1);
        } else {
            mMonitor.setState(0);
        }

        mMonitor.setMatrix(mMonitor.left, mMonitor.bottom, mMonitor.width, mMonitor.height);

    }

    private void initMatrix(int screen_width, int screen_height) {
        mMonitor.left = 0;
        mMonitor.bottom = 0;

        mMonitor.width = screen_width;
        mMonitor.height = screen_height;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // +++
            case R.id.iv_alarm:
                int enable = isRing ? 0 : 1;
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_RING, HiChipDefines.HI_P2P_RING_Fun.parseContent(enable));
                break;
            case R.id.btn_live_listen:
                if (HiDataValue.ANDROID_VERSION >= 23 && (!checkPermission(Manifest.permission.RECORD_AUDIO) || !checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                    showAlertDialog();
                    return;
                }
                clickListen((ImageView) v);
                break;
            case R.id.btn_live_exit:
                action = 1;
                finish();
                break;
            case R.id.btn_live_mirror_flip:
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_DISPLAY_PARAM, null);
                // clickMirrorFlip((ImageView) v);
                break;
            case R.id.btn_live_preset:
                setUpPreset((ImageView) v);
                break;
            case R.id.resolution_ratio:
                if (!isListening) {
                    if (mCamera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                        clickRatio((ImageView) v);
                    }
                } else {
                    HiToast.showToast(LiveViewActivity.this, getString(R.string.tip_not_switch_quality));
                }
                break;
            case R.id.btn_live_light:
                showLight((ImageView) v);

                break;

            case R.id.btn_live_record:
                if (HiDataValue.ANDROID_VERSION >= 23 && (!checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                    showAlertDialog();
                    return;
                }
                clickRecording((ImageView) v);
                break;
            case R.id.btn_live_zoom_focus:
                clickZoomFocus((ImageView) v);
                break;

            case R.id.btn_live_snapshot:
                if (HiDataValue.ANDROID_VERSION >= 23 && (!checkPermission(Manifest.permission.CAMERA) || !checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE))) {
                    // Toast.makeText(LiveViewActivity.this,
                    // getString(R.string.tips_no_permission), Toast.LENGTH_SHORT)
                    // .show();
                    showAlertDialog();
                    return;
                }
                clickSnapshot();
                break;
            case R.id.iv_live_track:
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_SMART_MOTO, new byte[0]);
                break;
        }
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(LiveViewActivity.this);
        builder.setMessage(getString(R.string.tips_no_permission));
        builder.setPositiveButton(getString(R.string.setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.MAIN");
                intent.setClassName("com.android.settings", "com.android.settings.ManageApplications");
                startActivity(intent);

            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.show();

    }

    private void setUpPreset(ImageView v) {
        View numKeyBoard = getLayoutInflater().inflate(R.layout.popup_preset_key, null);
        mPopupWindow = new PopupWindow(LiveViewActivity.this);
        mPopupWindow.setContentView(numKeyBoard);
        ColorDrawable cd = new ColorDrawable(-0000);
        mPopupWindow.setBackgroundDrawable(cd);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setWidth(HiTools.dip2px(LiveViewActivity.this, 200));
        mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        int offsetx = HiTools.dip2px(this, 20);
        int location[] = new int[2];
        v.getLocationOnScreen(location);
        int offsety = HiTools.dip2px(this, 90);
        mPopupWindow.showAtLocation(v, 0, location[0] - offsetx, offsety - location[1]);
        LiveViewModel.getInstance().handKeyBoard(LiveViewActivity.this, numKeyBoard, mCamera);

    }

    private boolean checkPermission(String permission) {
        int checkCallPhonePermission = ContextCompat.checkSelfPermission(LiveViewActivity.this, permission);
        if (checkCallPhonePermission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }

    // private int isTouchMoved; //not move=0, move=1, two point=2
    // private int state=0; //normal=0, larger=1.arrow=2;
    private float action_down_x;
    private float action_down_y;

    float lastX;
    float lastY;

    int xlenOld;
    int ylenOld;

    float move_x;
    float move_y;

    public float left;
    public float width;
    public float height;
    public float bottom;

    double nLenStart = 0;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v.getId() == R.id.monitor_live_view) {
            int nCnt = event.getPointerCount();
            if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_POINTER_DOWN && 2 == nCnt) {
                mMonitor.setTouchMove(2);
                for (int i = 0; i < nCnt; i++) {
                    float x = event.getX(i);
                    float y = event.getY(i);
                    Point pt = new Point((int) x, (int) y);
                }

                xlenOld = Math.abs((int) event.getX(0) - (int) event.getX(1));
                ylenOld = Math.abs((int) event.getY(0) - (int) event.getY(1));
                nLenStart = Math.sqrt((double) xlenOld * xlenOld + (double) ylenOld * ylenOld);

            } else if ((event.getAction() & MotionEvent.ACTION_MASK) == MotionEvent.ACTION_MOVE && 2 == nCnt) {
                mMonitor.setTouchMove(2);
                // mMonitor.setState(3);
                for (int i = 0; i < nCnt; i++) {
                    float x = event.getX(i);
                    float y = event.getY(i);

                    Point pt = new Point((int) x, (int) y);

                }

                int xlen = Math.abs((int) event.getX(0) - (int) event.getX(1));
                int ylen = Math.abs((int) event.getY(0) - (int) event.getY(1));

                int moveX = Math.abs(xlen - xlenOld);
                int moveY = Math.abs(ylen - ylenOld);

                double nLenEnd = Math.sqrt((double) xlen * xlen + (double) ylen * ylen);
                if (moveX < 20 && moveY < 20) {

                    return false;
                }

                if (nLenEnd > nLenStart) {
                    resetMonitorSize(true, nLenEnd);
                } else {
                    resetMonitorSize(false, nLenEnd);
                }

                xlenOld = xlen;
                ylenOld = ylen;
                nLenStart = nLenEnd;

                return true;
            } else if (nCnt == 1) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        action_down_x = event.getRawX();
                        action_down_y = event.getRawY();
                        lastX = action_down_x;
                        lastY = action_down_y;
                        mMonitor.setTouchMove(0);
                        break;
                    case MotionEvent.ACTION_MOVE:

                        if (mMonitor.getTouchMove() != 0)
                            break;
                        move_x = event.getRawX();
                        move_y = event.getRawY();

                        if (Math.abs(move_x - action_down_x) > 40 || Math.abs(move_y - action_down_y) > 40) {
                            mMonitor.setTouchMove(1);
                        }
                        break;
                    case MotionEvent.ACTION_UP: {
                        if (mMonitor.getTouchMove() != 0) {
                            break;
                        }
                        setViewVisible(visible);
                        HiTools.hideVirtualKey(this);

                        break;
                    }
                }
            }
        } else if (v.getId() == R.id.btn_microphone) {

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {

                    btn_live_light.setClickable(false);
                    btn_live_zoom_focus.setClickable(false);
                    btn_live_preset.setClickable(false);
                    btn_live_mirror_flip.setClickable(false);
                    btn_live_listen.setClickable(false);
                    btn_live_snapshot.setClickable(false);
                    btn_live_record.setClickable(false);
                    resolution_ratio.setClickable(false);
                    btn_live_exit.setClickable(false);

                    if (isRing) {
                        iv_alarm_mark.setVisibility(View.GONE);
                        iv_alarm.setSelected(false);
                        isRing = !isRing;
                    }

                    btn_live_listen.setClickable(false);
                    btn_live_listen.setImageResource(R.drawable.camhi_live_normal_speaker);
                    if (System.currentTimeMillis() - oldClickTime < 1000) {
                        break;
                    }
                    oldClickTime = System.currentTimeMillis();
                    if (mRecordingState == RECORDING_STATUS_ING) {
                        mCamera.PausePlayAudio();
                    } else {
                        mCamera.stopListening();
                        mVoiceIsTran = false;
                    }

                    mCamera.startTalk();

                    isTalking = true;
                    break;
                }
                case MotionEvent.ACTION_MOVE: {
                    break;
                }
                case MotionEvent.ACTION_UP: {

                    btn_live_light.setClickable(true);
                    btn_live_zoom_focus.setClickable(true);
                    btn_live_preset.setClickable(true);
                    btn_live_mirror_flip.setClickable(true);
                    btn_live_listen.setClickable(true);
                    btn_live_snapshot.setClickable(true);
                    btn_live_record.setClickable(true);
                    resolution_ratio.setClickable(true);
                    btn_live_exit.setClickable(true);
                    btn_live_listen.setImageResource(R.drawable.camhi_live_select_speaker);
                    mCamera.stopTalk();
                    if (mRecordingState == RECORDING_STATUS_ING) {
                        mCamera.ResumePlayAudio();
                    } else {
                        mCamera.startListening();
                        mVoiceIsTran = true;
                    }
                    isTalking = false;
                    break;
                }
            }
        }
        return false;
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

    private int select_preset = 0;

    private void clickPreset(ImageView iv) {
        @SuppressLint("InflateParams") View customView = getLayoutInflater().inflate(R.layout.popview_preset, null, false);

        mPopupWindow = new PopupWindow(customView);
        ColorDrawable cd = new ColorDrawable(-0000);
        mPopupWindow.setBackgroundDrawable(cd);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setWidth(LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);

        // w:210 h:40+5*3+35*2 = 125

        /*
         * if (getResources().getConfiguration().orientation ==
         * Configuration.ORIENTATION_PORTRAIT) { int offsetx = -HiTools.dip2px(this,
         * 80); int offsety = HiTools.dip2px(this, 40);
         * mPopupWindow.showAsDropDown(iv,offsetx,offsety); } else
         * if(getResources().getConfiguration().orientation ==
         * Configuration.ORIENTATION_LANDSCAPE){
         */
        int offsetx = HiTools.dip2px(this, 20);
        int location[] = new int[2];
        iv.getLocationOnScreen(location);
        int offsety = HiTools.dip2px(this, 90);
        mPopupWindow.showAtLocation(iv, 0, location[0] - offsetx, offsety - location[1]);
        // }

        RadioGroup radio_group_preset = (RadioGroup) customView.findViewById(R.id.radio_group_preset);
        radio_group_preset.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.radio_quality_0) {
                    select_preset = 0;
                }
                if (checkedId == R.id.radio_quality_1) {
                    select_preset = 1;
                }
                if (checkedId == R.id.radio_quality_2) {
                    select_preset = 2;
                }
                if (checkedId == R.id.radio_quality_3) {
                    select_preset = 3;
                }
                if (checkedId == R.id.radio_quality_4) {
                    select_preset = 4;
                }
                if (checkedId == R.id.radio_quality_5) {
                    select_preset = 5;
                }
                if (checkedId == R.id.radio_quality_6) {
                    select_preset = 6;
                }
                if (checkedId == R.id.radio_quality_7) {
                    select_preset = 7;
                }

                HiLog.v("onCheckedChanged:" + select_preset);
            }
        });

        Button btn_set = (Button) customView.findViewById(R.id.btn_preset_set);
        btn_set.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // isSetPTZReset = true;
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_PRESET, HiChipDefines.HI_P2P_S_PTZ_PRESET.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_PRESET_ACT_SET, select_preset));
                HiToast.showToast(LiveViewActivity.this, getString(R.string.tips_preset_set_btn));
            }
        });

        Button btn_call = (Button) customView.findViewById(R.id.btn_preset_call);
        btn_call.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_PRESET, HiChipDefines.HI_P2P_S_PTZ_PRESET.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_PRESET_ACT_CALL, select_preset));
            }
        });

    }

    HiChipDefines.HI_P2P_S_DISPLAY display_param = null;
    protected int filpChecked;
    protected int mirrorChecked;

    private void clickMirrorFlip(ImageView iv) {
        if (display_param == null) {
            return;
        }
        View customView = getLayoutInflater().inflate(R.layout.popview_mirror_flip, null, false);
        mPopupWindow = new PopupWindow(customView);
        ColorDrawable cd = new ColorDrawable(-0000);
        mPopupWindow.setBackgroundDrawable(cd);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setWidth(LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        int offsetx = HiTools.dip2px(this, 0);
        int location[] = new int[2];
        iv.getLocationOnScreen(location);
        int btnh = HiTools.dip2px(this, 50 + 80 / 2);

        mPopupWindow.showAtLocation(iv, 0, location[0] - offsetx, btnh - location[1]);
        SwitchButton toggle_flip = (SwitchButton) customView.findViewById(R.id.toggle_flip);
        toggle_flip.setChecked(display_param.u32Flip == 1 ? true : false);
        toggle_flip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                filpChecked = isChecked ? 1 : 0;
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_DISPLAY_PARAM, null);
                showLoadingView();
                isMF = true;
            }
        });
        // 镜像
        SwitchButton toggle_mirror = (SwitchButton) customView.findViewById(R.id.toggle_mirror);
        toggle_mirror.setChecked(display_param.u32Mirror == 1 ? true : false);
        toggle_mirror.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton view, boolean isChecked) {
                mirrorChecked = isChecked ? 1 : 0;
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_DISPLAY_PARAM, null);
                showLoadingView();
                isMF = true;
            }
        });

    }

    @Override
    public void receiveSessionState(HiCamera arg0, int arg1) {
        if (mCamera != arg0)
            return;
        Message msg = handler.obtainMessage();
        msg.what = HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
        msg.arg1 = arg1;
        msg.obj = arg0;
        handler.sendMessage(msg);

    }

    /*
     * @Override public void callbackState(int arg0, int arg1, int arg2) {
     *
     * Bundle bundle = new Bundle(); bundle.putInt("command", arg0);
     * bundle.putInt("width", arg1); bundle.putInt("height", arg2); Message msg =
     * handler.obtainMessage(); msg.what = HANDLE_MESSAGE_PLAY_STATE;
     * msg.setData(bundle); handler.sendMessage(msg);
     *
     * }
     */

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // initView();
        // resetMonitorSize();

    }

    /*
     * private class ProgressThread extends HiThread { public void run() {
     * while(isRunning) { sleep(100);
     *
     * Message msg = handler.obtainMessage(); msg.what =
     * HiDataValue.HANDLE_MESSAGE_PROGRESSBAR_RUN; handler.sendMessage(msg); } } }
     */

    private PopupWindow.OnDismissListener mOnDismissListener = new PopupWindow.OnDismissListener() {

        @Override
        public void onDismiss() {
            // setToolsButtonSelected(0);
        }
    };


    private void clickSnapshot() {
        if (mCamera != null) {

            if (HiTools.isSDCardValid()) {

                File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
                File sargetFolder = new File(rootFolder.getAbsolutePath() + "/Snapshot/");
                File yargetFolder = new File(sargetFolder.getAbsolutePath() + "/" + mCamera.getUid() + "/");
                // File targetFolder=new
                // File(yargetFolder.getAbsolutePath()+"/"+getTimeForNow()+"/");
                if (!rootFolder.exists()) {
                    rootFolder.mkdirs();
                }
                if (!sargetFolder.exists()) {
                    sargetFolder.mkdirs();
                }
                if (!yargetFolder.exists()) {
                    yargetFolder.mkdirs();
                }
                // if (!targetFolder.exists()) {
                // targetFolder.mkdir();
                // }

                String filename = HiTools.getFileNameWithTime(0);
                final String file = yargetFolder.getAbsoluteFile() + "/" + filename;
                Bitmap frame = mCamera != null ? mCamera.getSnapshot() : null;
                if (frame != null && HiTools.saveImage(file, frame)) {
                    SaveToPhone(file, filename);
                    Toast toast1 = Toast.makeText(LiveViewActivity.this, getText(R.string.tips_snapshot_success), Toast.LENGTH_SHORT);
                    toast1.show();
                    toastList1.add(toast1);
                } else {

                    Toast toast2 = Toast.makeText(LiveViewActivity.this, getText(R.string.tips_snapshot_failed), Toast.LENGTH_SHORT);
                    toast2.show();
                    toastList2.add(toast2);
                }
            } else {

                Toast toast3 = Toast.makeText(LiveViewActivity.this, getText(R.string.tips_no_sdcard).toString(), Toast.LENGTH_SHORT);
                toast3.show();
                toastList3.add(toast3);
            }

        }

    }

    private void SaveToPhone(final String path, final String fileName) {

        new Thread(new Runnable() {

            @Override
            public void run() {

                try {
                    MediaStore.Images.Media.insertImage(getContentResolver(), path, fileName, null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + path)));

            }
        }).start();

    }

    private void clickRatio(ImageView iv) {

        @SuppressLint("InflateParams") View customView = getLayoutInflater().inflate(R.layout.popview_resolution_ratio, null, false);

        mPopupWindow = new PopupWindow(customView);
        mPopupWindow.setOnDismissListener(mOnDismissListener);
        ColorDrawable cd = new ColorDrawable(-000);
        mPopupWindow.setBackgroundDrawable(cd);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setWidth(LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        int offsetx = HiTools.dip2px(this, 10 - 2);
        int location[] = new int[2];
        iv.getLocationOnScreen(location);
        int offsety = HiTools.dip2px(this, 90 + 10);
        mPopupWindow.showAtLocation(iv, 0, location[0] - offsetx, location[1] - offsety);
        final TextView ratio_high = (TextView) customView.findViewById(R.id.ratio_high);
        final TextView ratio_fluent = (TextView) customView.findViewById(R.id.ratio_fluent);
        int videoQuality = mCamera.getVideoQuality();
        if (videoQuality == 0) {
            ratio_high.setSelected(true);
            ratio_fluent.setSelected(false);
        } else if (videoQuality == 1) {
            ratio_fluent.setSelected(true);
            ratio_high.setSelected(false);
        }
        ratio_high.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                receiveHumanParams = false;
                humanRectHandler.removeMessages(10010);
                mPopupWindow.dismiss();
                ratio_high.setSelected(true);
                ratio_fluent.setSelected(false);
                switchVideoQuality(0);
            }
        });

        ratio_fluent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                receiveHumanParams = false;
                humanRectHandler.removeMessages(10010);
                mPopupWindow.dismiss();
                ratio_fluent.setSelected(true);
                ratio_high.setSelected(false);
                switchVideoQuality(1);
            }
        });
    }

    private void switchVideoQuality(int quality) {
        if (mCamera == null) {
            return;
        }
        int videoQuality = mCamera.getVideoQuality();
        videoQuality = quality;
        if (videoQuality == mCamera.getVideoQuality()) {
            return;
        }
        setViewWhetherClick(false);
        showLoadingView();


        btn_live_record.setImageResource(R.drawable.camhi_live_normal_recording);

        mRecordingState = RECORDING_STATUS_NONE;

        txt_recording.setVisibility(View.GONE);
        mIvRecording.setVisibility(View.GONE);
        mHandler.removeCallbacksAndMessages(null);
        if (!TextUtils.isEmpty(recordFile)) {
            File file = new File(recordFile);
            if (file.length() <= 1024 && file.isFile() && file.exists()) {
                file.delete();
            }
        }

        mCamera.setVideoQuality(videoQuality);
        mCamera.updateInDatabase(LiveViewActivity.this);

        mIsSwitchResolution = true;
        mCamera.stopLiveShow();
        mCamera.stopListening();
        mVoiceIsTran = false;
        mCamera.disconnect(1);

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCamera.connect();
            }
        }, 500);

    }

    private void showLight(ImageView iv) {
        if (lightModel == 0) {
            return;
        }
        View customView = getLayoutInflater().inflate(R.layout.popview_light_set, null, false);
        mPopupWindow = new PopupWindow(customView);
        ColorDrawable cd = new ColorDrawable(-000);
        mPopupWindow.setBackgroundDrawable(cd);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setWidth(LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);

        RadioButton[] lightRadioBtnsRES = new RadioButton[3];
        RadioButton[] lightRadioBtnsNOR = new RadioButton[2];
        RadioButton[] lightRadioBtnsAlarm = new RadioButton[3];//声光报警

        RadioGroup live_view_ext_layout = (RadioGroup) customView.findViewById(R.id.live_view_ext_layout);
        RadioGroup live_view_nor_layout = (RadioGroup) customView.findViewById(R.id.live_view_nor_layout);
        RadioGroup live_view_abs_light_layout = (RadioGroup) customView.findViewById(R.id.live_view_abs_light_layout);
        RadioGroup live_view_audible_alarmt_layout = (RadioGroup) customView.findViewById(R.id.live_view_audible_alarm_layout);
        lightRadioBtnsRES[0] = (RadioButton) customView.findViewById(R.id.live_view_ext_btn_normal);
        lightRadioBtnsRES[1] = (RadioButton) customView.findViewById(R.id.live_view_ext_btn_color);
        lightRadioBtnsRES[2] = (RadioButton) customView.findViewById(R.id.live_view_ext_btn_auto);
        lightRadioBtnsNOR[0] = (RadioButton) customView.findViewById(R.id.live_view_nor_btn_open);
        lightRadioBtnsNOR[1] = (RadioButton) customView.findViewById(R.id.live_view_nor_btn_close);
        lightRadioBtnsAlarm[0] = (RadioButton) customView.findViewById(R.id.audible_alarm_close);
        lightRadioBtnsAlarm[1] = (RadioButton) customView.findViewById(R.id.audible_alarm_open);
        lightRadioBtnsAlarm[2] = (RadioButton) customView.findViewById(R.id.audible_alarm_auto);
        if (lightModel == 2) {
            live_view_ext_layout.setVisibility(View.GONE);
            live_view_abs_light_layout.setVisibility(View.GONE);
            live_view_audible_alarmt_layout.setVisibility(View.GONE);
            live_view_nor_layout.setVisibility(View.VISIBLE);
            if (light_info != null && light_info.u32State < 2) {
                lightRadioBtnsNOR[light_info.u32State].setChecked(true);
            }
        } else if (lightModel == 1) {
            live_view_ext_layout.setVisibility(View.VISIBLE);
            live_view_nor_layout.setVisibility(View.GONE);
            live_view_abs_light_layout.setVisibility(View.GONE);
            live_view_audible_alarmt_layout.setVisibility(View.GONE);
            if (light_info_ext != null && light_info_ext.u32State < 3) {
                lightRadioBtnsRES[light_info_ext.u32State].setChecked(true);
            }
        } else if (lightModel == 3) {
            live_view_abs_light_layout.setVisibility(View.VISIBLE);
            live_view_ext_layout.setVisibility(View.GONE);
            live_view_audible_alarmt_layout.setVisibility(View.GONE);
            live_view_nor_layout.setVisibility(View.GONE);
            if (abs_light != null && abs_light.s32State < 3) {
                RadioButton rBtn = (RadioButton) live_view_abs_light_layout.getChildAt(abs_light.s32State);
                rBtn.setChecked(true);
            }
        } else if (lightModel == 4) {
            live_view_audible_alarmt_layout.setVisibility(View.VISIBLE);
            live_view_abs_light_layout.setVisibility(View.GONE);
            live_view_ext_layout.setVisibility(View.GONE);
            live_view_nor_layout.setVisibility(View.GONE);
            if (audible_alarm != null && audible_alarm.u32State < 3) {
                lightRadioBtnsAlarm[audible_alarm.u32State].setChecked(true);
            }
            //           HiLog.e("TAG", "u32==" + audible_alarm.u32State);
        }
        live_view_audible_alarmt_layout.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int id) {
                if (audible_alarm == null)
                    return;
                switch (id) {
                    case R.id.audible_alarm_close:
                        audible_alarm.u32State = 0;
                        break;
                    case R.id.audible_alarm_open:
                        audible_alarm.u32State = 1;
                        break;
                    case R.id.audible_alarm_auto:
                        audible_alarm.u32State = 2;
                        break;
                }
                HiLog.e("onCheckedChanged==" + audible_alarm.u32State);
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_AUDIBLE_VISUAL_ALARM_TYPE, HI_P2P_WHITE_LIGHT_INFO_EXT.parseContent(audible_alarm.u32Chn, audible_alarm.u32State));
            }
        });
        live_view_ext_layout.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup rg, int id) {
                if (light_info_ext == null)
                    return;
                switch (id) {
                    case R.id.live_view_ext_btn_normal:
                        light_info_ext.u32State = 0;
                        break;
                    case R.id.live_view_ext_btn_color:
                        light_info_ext.u32State = 1;
                        break;
                    case R.id.live_view_ext_btn_auto:
                        light_info_ext.u32State = 2;
                        break;
                }
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_SET_EXT, HI_P2P_WHITE_LIGHT_INFO_EXT.parseContent(light_info_ext.u32Chn, light_info_ext.u32State));
            }
        });

        live_view_nor_layout.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup rg, int id) {
                if (light_info == null)
                    return;
                switch (id) {
                    case R.id.live_view_nor_btn_open:
                        light_info.u32State = 0;
                        break;
                    case R.id.live_view_nor_btn_close:
                        light_info.u32State = 1;
                        break;
                }
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_WHITE_LIGHT_SET, HI_P2P_WHITE_LIGHT_INFO.parseContent(light_info.u32Chn, light_info.u32State));

            }
        });

        live_view_abs_light_layout.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (abs_light == null)
                    return;
                switch (checkedId) {
                    case R.id.abs_light_auto:
                        abs_light.s32State = 0;
                        break;
                    case R.id.abs_light_open:
                        abs_light.s32State = 1;
                        break;
                    case R.id.abs_light_close:
                        abs_light.s32State = 2;
                        break;
                }
                mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_ABSOLUTE_LIGHT_TYPE, ABSOLUTE_LIGHT_TYPE.parseContent(abs_light.s32State));

            }
        });

        // width = 210 height = 90
        int offsetx = HiTools.dip2px(this, 20);
        int location[] = new int[2];
        iv.getLocationOnScreen(location);
        int offsety = HiTools.dip2px(this, 20 + 125 / 2);

        mPopupWindow.showAtLocation(iv, 0, location[0] - offsetx, offsety - location[1]);

    }

    private void clickZoomFocus(ImageView iv) {
        @SuppressLint("InflateParams") View customView = getLayoutInflater().inflate(R.layout.popview_zoom_focus, null, false);

        mPopupWindow = new PopupWindow(customView);
        ColorDrawable cd = new ColorDrawable(-0000);
        mPopupWindow.setBackgroundDrawable(cd);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setWidth(LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);

        // width = 210 height = 90
        int offsetx = HiTools.dip2px(this, 10);
        int location[] = new int[2];
        iv.getLocationOnScreen(location);
        int offsety = HiTools.dip2px(this, 90);

        mPopupWindow.showAtLocation(iv, 0, location[0] - offsetx, offsety - location[1]);

        Button btnZoomin = (Button) customView.findViewById(R.id.btn_zoomin);
        btnZoomin.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_ZOOMIN, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_STOP, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                }
                return false;
            }
        });
        Button btnZoomout = (Button) customView.findViewById(R.id.btn_zoomout);
        btnZoomout.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_ZOOMOUT, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_STOP, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                }
                return false;
            }
        });
        Button btnFocusin = (Button) customView.findViewById(R.id.btn_focusin);
        btnFocusin.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_FOCUSIN, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_STOP, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                }
                return false;
            }
        });
        Button btnFocusout = (Button) customView.findViewById(R.id.btn_focusout);
        btnFocusout.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View arg0, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_FOCUSOUT, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_PTZ_CTRL, HiChipDefines.HI_P2P_S_PTZ_CTRL.parseContent(HiChipP2P.HI_P2P_SE_CMD_CHN, HiChipDefines.HI_P2P_PTZ_CTRL_STOP, HiChipDefines.HI_P2P_PTZ_MODE_RUN, (short) MyLiveViewGLMonitor.PTZ_STEP, (short) 10));
                }
                return false;
            }
        });
    }

    private void clickRecording(ImageView v) {
        if (mRecordingState == RECORDING_STATUS_NONE) {
            //			mRecordingState = RECORDING_STATUS_LOADING;
            TimerRecording();
            btn_live_record.setImageResource(R.drawable.camhi_live_select_recording);

            mRecordingState = RECORDING_STATUS_ING;
            txt_recording.setVisibility(View.VISIBLE);
            mIvRecording.setVisibility(View.VISIBLE);
            mHandler.sendEmptyMessage(110);

        } else if (mRecordingState == RECORDING_STATUS_ING) {
            //			mRecordingState = RECORDING_STATUS_LOADING;
            if (mVoiceIsTran && btn_microphone.getVisibility() == View.GONE) {
                mCamera.stopListening();
                mVoiceIsTran = false;
            }
            mCamera.stopRecording();
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            if (timerTask != null) {
                timerTask.cancel();
                timerTask = null;
            }
            btn_live_record.setImageResource(R.drawable.camhi_live_normal_recording);

            mRecordingState = RECORDING_STATUS_NONE;
            txt_recording.setVisibility(View.GONE);
            mIvRecording.setVisibility(View.GONE);
            mHandler.removeCallbacksAndMessages(null);
            if (!TextUtils.isEmpty(recordFile)) {
                File file = new File(recordFile);
                if (file.length() <= 1024 && file.isFile() && file.exists()) {
                    file.delete();
                }
            }
        }
    }

    private void TimerRecording() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        final File cameraFolder = new File(HiDataValue.LOCAL_VIDEO_PATH + "/" + mCamera.getUid());
        if (!cameraFolder.exists()) {
            cameraFolder.mkdirs();
        }
        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (mRecordingState == RECORDING_STATUS_ING) {
                    mCamera.stopRecording();
                }
                recordFile = cameraFolder.getAbsoluteFile() + "/" + HiTools.getFileNameWithTime(1);
                long available = HiTools.getAvailableSize();
                if (available < 100 && available > 0) {// 设备内存小于100M
                    mHandler.sendEmptyMessage(0X999);
                    return;
                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCamera.startRecording(recordFile);
                    }
                }, 1000);
            }
        };
        timer.schedule(timerTask, 0, 10 * 60 * 1000);
    }

    private void clickListen(ImageView iv) {
        // Toast.makeText(LiveViewActivity.this, mRecordingState+"--"+isListening,
        // Toast.LENGTH_LONG).show();
        if (mRecordingState == RECORDING_STATUS_ING) {
            if (mVoiceIsTran) {
                mCamera.PausePlayAudio();
                // mCamera.ResumePlayAudio();
            } else {
                mCamera.stopRecording();
                mCamera.startListening();
                mVoiceIsTran = true;

                TimerRecording();
            }

            if (isListening) {
                iv.setImageResource(R.drawable.camhi_live_normal_speaker);
                btn_microphone.setVisibility(View.GONE);
                //				mCamera.stopListening();
                if (mVoiceIsTran) {
                    mCamera.PausePlayAudio();
                }

            } else {
                iv.setImageResource(R.drawable.camhi_live_select_speaker);
                btn_microphone.setVisibility(View.VISIBLE);
                //				mCamera.startListening();
                if (mVoiceIsTran) {
                    mCamera.ResumePlayAudio();
                }
            }
        } else {
            if (isListening) {
                iv.setImageResource(R.drawable.camhi_live_normal_speaker);
                mCamera.stopListening();
                mVoiceIsTran = false;
                btn_microphone.setVisibility(View.GONE);
            } else {
                iv.setImageResource(R.drawable.camhi_live_select_speaker);
                btn_microphone.setVisibility(View.VISIBLE);
                mCamera.startListening();
                mVoiceIsTran = true;
            }
        }
        isListening = !isListening;

    }

    public String getTimeForNow() {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        return sf.format(System.currentTimeMillis());
    }

    @Override
    public void callbackPlayUTC(HiCamera arg0, int arg1) {

    }

    @Override
    public void callbackState(HiCamera camera, int arg1, int arg2, int arg3) {
        if (mCamera != camera)
            return;
        Bundle bundle = new Bundle();
        bundle.putInt("command", arg1);
        bundle.putInt("width", arg2);
        bundle.putInt("height", arg3);
        Message msg = handler.obtainMessage();
        msg.what = HiDataValue.HANDLE_MESSAGE_PLAY_STATE;
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @SuppressLint("HandlerLeak") private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 110:
                    if (mIvRecording.getVisibility() == View.GONE) {
                        mIvRecording.setVisibility(View.VISIBLE);
                    } else {
                        mIvRecording.setVisibility(View.GONE);
                    }
                    mHandler.sendEmptyMessageDelayed(110, 1000);
                    break;
                case 0X999:
                    HiToast.showToast(LiveViewActivity.this, getString(R.string.failed_recording));
                    break;
            }

        }

    };

    private void setViewWhetherClick(boolean whetherClick) {
        btn_live_light.setClickable(whetherClick);
        btn_live_zoom_focus.setClickable(whetherClick);
        btn_live_preset.setClickable(whetherClick);
        btn_live_mirror_flip.setClickable(whetherClick);
        btn_live_listen.setClickable(whetherClick);
        btn_live_snapshot.setClickable(whetherClick);
        btn_live_record.setClickable(whetherClick);
        resolution_ratio.setClickable(whetherClick);
    }

    private void showLoadingView() {
        Animation rotateAnim = AnimationUtils.loadAnimation(LiveViewActivity.this, R.anim.rotate);
        mIvLoading2.setVisibility(View.VISIBLE);
        mIvLoading2.startAnimation(rotateAnim);
    }

    private void dismissLoadingView() {
        mIvLoading2.clearAnimation();
        mIvLoading2.setVisibility(View.GONE);
    }

    @SuppressLint("HandlerLeak") private Handler humanRectHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 10010:
                    byte[] data = (byte[]) msg.obj;
                    int u32Num = msg.arg1;
                    if (u32Num > 3)
                        u32Num = 3;
                    humanRects.clear();
                    for (int i = 0; i < u32Num; i++) {
                        byte[] bytes = new byte[20];
                        if (data.length - (8 + 20 * i) >= 20) {
                            System.arraycopy(data, 8 + 20 * i, bytes, 0, 20);
                            CamHiDefines.HI_P2P_ALARM_MD md = new CamHiDefines.HI_P2P_ALARM_MD(bytes);
                            HumanRect humanRect = new HumanRect(md.u32X, md.u32Y, md.u32Width, md.u32Height, monitor_width, monitor_height);
                            if (humanRect.getRect_height() != 0 && humanRect.getRect_width() != 0) {
                                humanRects.add(humanRect);
                            }
                        }
                    }
                    humanRectView.refreshRect(humanRects, mCamera.getVideoQuality());
                    break;
            }
        }
    };

    public class HumanReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(HUMANTAG)) {
                byte[] data = intent.getByteArrayExtra("DATA");

                if (preHumanTime == 0)
                    mTimer.schedule(mTask, 0, 1000);
                preHumanTime = System.currentTimeMillis();

                if (mMonitor == null)
                    return;
                if (mMonitor.getState() == 1)
                    return;
                CamHiDefines.HI_P2P_SMART_HSR_AREA params = new CamHiDefines.HI_P2P_SMART_HSR_AREA(data);
                if (params.u32Num == 0)
                    return;
                Message message = Message.obtain();
                message.what = 10010;
                message.obj = data;
                message.arg1 = params.u32Num;
                humanRectHandler.sendMessage(message);

            }
        }
    }

    private void clickTrack(ImageView v) {
        View constom = View.inflate(LiveViewActivity.this, R.layout.popup_track, null);
        mPopupWindow = new PopupWindow(constom);
        ColorDrawable cd = new ColorDrawable(-000);
        mPopupWindow.setBackgroundDrawable(cd);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setWidth(LayoutParams.WRAP_CONTENT);
        mPopupWindow.setHeight(LayoutParams.WRAP_CONTENT);
        int location[] = new int[2];
        v.getLocationOnScreen(location);
        int btnh = HiTools.dip2px(this, 50 + 80 / 2);
        swiBtnTrack = (SwitchButton) constom.findViewById(R.id.toggle_track);
        swiBtnTrack.setChecked(trackParam.bSmartTrack == 0 ? false : true);
        swiBtnTrack.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                showjuHuaDialog();//showLoadingProgress();
                if (trackParam != null) {
                    trackParam.bSmartTrack = isChecked ? 1 : 0;
                    mCamera.sendIOCtrl(HiChipDefines.HI_P2P_SET_SMART_MOTO, trackParam.parseContent(isChecked ? 1 : 0));
                }
            }
        });
        mPopupWindow.showAtLocation(v, 0, location[0], btnh - location[1]);

    }
}
