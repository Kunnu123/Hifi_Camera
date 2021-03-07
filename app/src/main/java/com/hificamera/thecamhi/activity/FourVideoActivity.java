package com.hificamera.thecamhi.activity;

import android.content.Intent;
import android.os.Bundle;;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.hificamera.R;
import com.hichip.base.HiThread;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.callback.ICameraPlayStateCallback;
import com.hichip.control.HiCamera;
import com.hificamera.hichip.activity.FishEye.FishEyeActivity;
import com.hificamera.hichip.activity.WallMounted.WallMountedActivity;
import com.hificamera.thecamhi.base.MyLiveViewGLMonitor;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.MyCamera;
import com.hificamera.thecamhi.main.HiActivity;
import com.hificamera.thecamhi.utils.SharePreUtils;

import java.util.ArrayList;
import java.util.List;

public class FourVideoActivity extends HiActivity implements View.OnTouchListener, ICameraIOSessionCallback, ICameraPlayStateCallback, View.OnClickListener {
    private MyLiveViewGLMonitor mMonitor1, mMonitor2, mMonitor3, mMonitor4;
    private MyCamera camera1, camera2, camera3, camera4;
    List<MyCamera> list = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_four_video);
        initView();
    }

    private void initView() {
        mMonitor1 = findViewById(R.id.monitor1);
        mMonitor2 = findViewById(R.id.monitor2);
        mMonitor3 = findViewById(R.id.monitor3);
        mMonitor4 = findViewById(R.id.monitor4);
        mMonitor1.setOnTouchListener(this);
        mMonitor2.setOnTouchListener(this);
        mMonitor3.setOnTouchListener(this);
        mMonitor4.setOnTouchListener(this);
        mMonitor1.setOnClickListener(this);
        mMonitor2.setOnClickListener(this);
        mMonitor3.setOnClickListener(this);
        mMonitor4.setOnClickListener(this);
        if (HiDataValue.CameraList.size() >= 1) {
            camera1 = HiDataValue.CameraList.get(0);
            if (camera1.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN && camera1.mIsReceived_4179) {
                mMonitor1.setVisibility(View.VISIBLE);
                mMonitor1.setCamera(camera1);
                list.add(camera1);
            }
        }

        if (HiDataValue.CameraList.size() >= 2) {
            camera2 = HiDataValue.CameraList.get(1);
            if (camera2.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN && camera2.mIsReceived_4179) {
                mMonitor2.setVisibility(View.VISIBLE);
                mMonitor2.setCamera(camera2);
                list.add(camera2);
            }
        }
        if (HiDataValue.CameraList.size() >= 3) {
            mMonitor3.setVisibility(View.VISIBLE);
            camera3 = HiDataValue.CameraList.get(2);
            if (camera3.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN && camera3.mIsReceived_4179) {

                mMonitor3.setCamera(camera3);
                list.add(camera3);
            }
        }
        if (HiDataValue.CameraList.size() >= 4) {
            camera4 = HiDataValue.CameraList.get(3);
            if (camera4.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN && camera4.mIsReceived_4179) {
                mMonitor4.setVisibility(View.VISIBLE);
                mMonitor4.setCamera(camera4);
                list.add(camera4);
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        new Thread() {
            public void run() {
                if (camera1 != null) {
//                    camera1.SetDecodeVidtoType(1);
                    camera1.startLiveShow(camera1.getVideoQuality(), mMonitor1);
                    camera1.registerIOSessionListener(FourVideoActivity.this);
                    camera1.registerPlayStateListener(FourVideoActivity.this);
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (camera2 != null) {
//                    camera1.SetDecodeVidtoType(1);
                    camera2.startLiveShow(camera2.getVideoQuality(), mMonitor2);
                    camera2.registerIOSessionListener(FourVideoActivity.this);
                    camera2.registerPlayStateListener(FourVideoActivity.this);
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (camera3 != null) {
//                    camera1.SetDecodeVidtoType(1);
                    camera3.startLiveShow(camera3.getVideoQuality(), mMonitor3);
                    camera3.registerIOSessionListener(FourVideoActivity.this);
                    camera3.registerPlayStateListener(FourVideoActivity.this);
                }
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (camera4 != null) {
//                    camera1.SetDecodeVidtoType(1);
                    camera4.startLiveShow(camera4.getVideoQuality(), mMonitor4);
                    camera4.registerIOSessionListener(FourVideoActivity.this);
                    camera4.registerPlayStateListener(FourVideoActivity.this);
                }

            }
        }.start();


    }

    @Override
    protected void onPause() {
        super.onPause();
        for(MyCamera myCamera:list){
            if(myCamera!=null){
                myCamera.stopLiveShow();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.monitor1:
                JumpLiveActivity(camera1);
                break;
            case R.id.monitor2:
                JumpLiveActivity(camera2);
                break;
            case R.id.monitor3:
                JumpLiveActivity(camera3);
                break;
            case R.id.monitor4:
                JumpLiveActivity(camera4);
                break;
        }
    }


    private void JumpLiveActivity(MyCamera camera) {
        if (camera == null) {
            return;
        }
        if (camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN && camera.mIsReceived_4179) {
            Bundle extras = new Bundle();
            extras.putString(HiDataValue.EXTRAS_KEY_UID, camera.getUid());
            Intent intent = new Intent();
            intent.putExtras(extras);
            if (camera.isFishEye() && camera.isWallMounted) {
                intent.setClass(this, WallMountedActivity.class);
            } else if (camera.isFishEye()) {
                int num = SharePreUtils.getInt("mInstallMode", this, camera.getUid());
                camera.mInstallMode = num == -1 ? 0 : num;
                boolean bl = SharePreUtils.getBoolean("cache", this, camera.getUid());
                camera.isFirst = bl;
                intent.setClass(this, FishEyeActivity.class);
            } else {
                intent.setClass(this, LiveViewActivity.class);
            }
            startActivity(intent);
        }
    }

    private class DeThread extends HiThread {
        List<MyCamera> myCamera;

        private void setMyCamera(List<MyCamera> camera) {
            this.myCamera = camera;
        }

        @Override
        public void run() {
            Looper.prepare();
            if (myCamera.isEmpty()) {
                return;
            }
            for (MyCamera camera : myCamera) {
                Log.e("TAG", "休眠100ms");
                sleep(100);
                camera.stopListening();
                camera.stopLiveShow();
                camera.unregisterPlayStateListener(FourVideoActivity.this);
                camera.unregisterIOSessionListener(FourVideoActivity.this);
                Log.e("TAG", "UID::" + camera.getUid() + "退出");

            }
            Looper.loop();

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();


        DeThread deThread = new DeThread();
        deThread.setMyCamera(list);
        deThread.startThread();
        finish();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }

    @Override
    public void receiveSessionState(HiCamera hiCamera, int i) {

    }

    @Override
    public void receiveIOCtrlData(HiCamera hiCamera, int i, byte[] bytes, int i1) {

    }

    @Override
    public void callbackState(HiCamera hiCamera, int i, int i1, int i2) {

    }

    @Override
    public void callbackPlayUTC(HiCamera hiCamera, int i) {

    }
}
