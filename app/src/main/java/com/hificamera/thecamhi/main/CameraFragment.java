package com.hificamera.thecamhi.main;

import java.io.File;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;
import java.util.regex.Pattern;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Notification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hificamera.customview.dialog.Effectstype;
import com.hificamera.customview.dialog.NiftyDialogBuilder;
import com.hificamera.R;
import com.hificamera.hichip.activity.FishEye.FishEyeActivity;
import com.hificamera.hichip.activity.Share.SeleShaCameraListActivity;
import com.hificamera.hichip.activity.WallMounted.WallMountedActivity;
import com.hichip.base.HiLog;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hichip.push.HiPushSDK;
import com.hichip.system.HiDefaultData;

import com.hificamera.thecamhi.activity.setting.TimeSettingActivity;

import com.hificamera.thecamhi.utils.DialogUtils;
import com.hificamera.thecamhi.utils.SsidUtils;
import com.hificamera.thecamhi.utils.SystemUtils;
import com.hificamera.thecamhi.utils.TimeZoneUtils;
import com.hificamera.thecamhi.utils.TokenUtils;
import com.hichip.tools.HiWriteUIDSDK;
import com.hichip.tools.Packet;

import com.hificamera.thecamhi.activity.AddCameraActivity;
import com.hificamera.thecamhi.activity.EditCameraActivity;
import com.hificamera.thecamhi.activity.LiveViewActivity;
import com.hificamera.thecamhi.activity.setting.AliveSettingActivity;

import com.hificamera.thecamhi.base.DatabaseManager;
import com.hificamera.thecamhi.base.HiToast;
import com.hificamera.thecamhi.base.HiTools;
import com.hificamera.thecamhi.base.TitleView;
import com.hificamera.thecamhi.bean.CamHiDefines;

import com.hificamera.thecamhi.utils.SharePreUtils;
import com.hificamera.thecamhi.widget.swipe.SwipeMenu;
import com.hificamera.thecamhi.widget.swipe.SwipeMenuCreator;
import com.hificamera.thecamhi.widget.swipe.SwipeMenuItem;
import com.hificamera.thecamhi.widget.swipe.SwipeMenuListView;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.MyCamera;


import static android.content.Context.ACTIVITY_SERVICE;
import static com.hificamera.thecamhi.utils.UidConfigUtil.blackUidMap;

public class CameraFragment extends HiFragment implements ICameraIOSessionCallback, OnItemClickListener {
    private View layoutView;
    private static final int MOTION_ALARM = 0;
    private static final int IO_ALARM = 1;
    private static final int AUDIO_ALARM = 2;
    private static final int UART_ALARM = 3;


    private static final int DELETE_CAM = 0x224; //
    private static final int UPDATE_CAM = 0x225; //

    private CameraListAdapter adapter;
    private CameraBroadcastReceiver receiver;
    private SwipeMenuListView mListView;

    private String[] str_state;
    private boolean delModel = false;
    int ranNum;
    private TitleView titleView;
    private AppCompatImageView ivLogout;

    HiThreadConnect connectThread = null;
    private int saveopenswipeindex = -1;
    private NotificationManager notificationManager;
    private int lastPosition;
    private int lastY;
    private boolean isNeedScorllToOldPosition;
    private boolean isFirst;


    public interface OnButtonClickListener {
        void onButtonClick(int btnId, MyCamera camera);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (receiver == null) {
            receiver = new CameraBroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(HiDataValue.ACTION_CAMERA_INIT_END);
            getActivity().registerReceiver(receiver, filter);
        }
        SharedPreferences pre = Objects.requireNonNull(getActivity()).getSharedPreferences("isFirst", Context.MODE_PRIVATE);
        isFirst = pre.getBoolean("first", true);
        if (isFirst) {
            SharePreUtils.putBoolean("isFirst", getActivity(), "first", false);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutView = inflater.inflate(R.layout.fragment_camera, null);
        initView();
        ranNum = (int) (Math.random() * 10000);
        return layoutView;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        SwipeMenuCreator creator = new SwipeMenuCreator() {
            @Override
            public void create(SwipeMenu menu) {

                SwipeMenuItem deleteItem = new SwipeMenuItem(getActivity().getApplicationContext());
                deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9, 0x3F, 0x25)));
                deleteItem.setWidth(HiTools.dip2px(CameraFragment.this.getActivity(), 80));
                deleteItem.setHeight(HiTools.dip2px(CameraFragment.this.getActivity(), 200));
                menu.addMenuItem(deleteItem);
            }
        };

        mListView.setMenuCreator(creator);

        mListView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
            @Override
            public void onMenuItemClick(int position, SwipeMenu menu, int index) {
                MyCamera camera = HiDataValue.CameraList.get(position);
                switch (index) {
                    case 0:
                        showDeleteCameraDialog(camera, Effectstype.Slidetop);
                        break;
                }
            }
        });
        mListView.setOnMenuItemOpenListener(new SwipeMenuListView.OnMenuItemOpenListener() {

            @Override
            public void OnSwipeOpen(int position, boolean state) {
                if (position >= 0 && position < HiDataValue.CameraList.size()) {
                    if (true == state)
                        saveopenswipeindex = position;
                    else
                        saveopenswipeindex = -1;
                }
            }
        });


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "camera_notification";
            String channelName = getContext().getResources().getString(R.string.notification_channel_name);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            createNotificationChannel(channelId, channelName, importance);
        }

    }

    @TargetApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(String channelId, String channelName, int importance) {
        NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
        notificationManager = (NotificationManager) Objects.requireNonNull(getActivity()).getSystemService(getActivity().NOTIFICATION_SERVICE);

        notificationManager.createNotificationChannel(channel);

    }

    private void showDeleteCameraDialog(final MyCamera camera, Effectstype type) {
        final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(getActivity());
        dialog.withTitle(getString(R.string.tip_reminder)).withMessage(getString(R.string.tips_msg_delete_camera)).withEffect(type).setButton1Click(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        }).setButton2Click(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                showjuHuaDialog();
                camera.bindPushState(false, bindPushResult);
                SharePreUtils.removeKey("cache", getActivity(), camera.getUid());
                SharePreUtils.removeKey("upName", getActivity(), camera.getUid() + "upName");

                SharePreUtils.putBoolean("cache", getActivity(), "isFirstPbOnline", false);
                SharePreUtils.putBoolean("cache", getActivity(), camera.getUid() + "pb", false);
                sendUnRegister(camera, 0);
                Message msg = handler.obtainMessage();
                msg.what = HiDataValue.HANDLE_MESSAGE_DELETE_FILE;
                msg.obj = camera;
                handler.sendMessageDelayed(msg, 1000);
            }
        }).show();
    }

    private void initView() {
        titleView = (TitleView) layoutView.findViewById(R.id.fg_ca_title);
        ivLogout = (AppCompatImageView) layoutView.findViewById(R.id.ivLogout);
        titleView.setTitle(getString(R.string.title_camera_fragment));
        titleView.setButton(TitleView.NAVIGATION_TEXT_RIGHT);
        if (HiDataValue.shareIsOpen) {
            titleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
            titleView.setLeftBtnTextBackround(R.drawable.share);
            titleView.setLeftBackroundPadding(2, 2, 2, 2);
        }
        titleView.setNavigationBarButtonListener(new TitleView.NavigationBarButtonListener() {
            @Override
            public void OnNavigationButtonClick(int which) {
                switch (which) {
                    case TitleView.NAVIGATION_TEXT_RIGHT:
                        if (delModel) {
                            titleView.setRightText(R.string.btn_edit);
                        } else {
                            titleView.setRightText(R.string.finish);
                        }
                        delModel = !delModel;
                        if (adapter != null) {
                            adapter.notifyDataSetChanged();
                        }
                        break;
                    case TitleView.NAVIGATION_BUTTON_LEFT:
                        if (HiDataValue.CameraList.size() > 0) {
                            Intent intent = new Intent(getActivity(), SeleShaCameraListActivity.class);
                            startActivity(intent);

                        } else {
                            HiToast.showToast(getContext(), getString(R.string.tips_goto_add_camera));
                        }
                        break;
                }

            }
        });

        ivLogout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlgBuilder = new AlertDialog.Builder(getActivity());
                dlgBuilder.setIcon(android.R.drawable.ic_lock_power_off);
                dlgBuilder.setTitle("Logout");
                dlgBuilder.setMessage("are you sure want to logout ?");
                dlgBuilder.setPositiveButton(getText(R.string.btn_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        doLogOut();
                    }
                }).show();
            }
        });

        mListView = (SwipeMenuListView) layoutView.findViewById(R.id.lv_swipemenu);
        LinearLayout add_camera_ll = (LinearLayout) layoutView.findViewById(R.id.add_camera_ll);
        add_camera_ll.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AddCameraActivity.class);
                startActivity(intent);
            }
        });
        str_state = getActivity().getResources().getStringArray(R.array.connect_state);
        adapter = new CameraListAdapter(getActivity());
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);
        adapter.setOnButtonClickListener(new OnButtonClickListener() {
            @Override
            public void onButtonClick(int btnId, final MyCamera camera) {
                switch (btnId) {
                    case R.id.setting_camera_item: {
                        if (delModel) {
                            Intent intent = new Intent();
                            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, camera.getUid());
                            intent.setClass(getActivity(), EditCameraActivity.class);
                            startActivity(intent);


                        } else {
                            Log.e("Flag", "getEncryptionFlag=" + camera.getEncryptionFlag());
                            if (camera.getEncryptionFlag() != 0) {
                                HiToast.showToast(getContext(), str_state[5]);
                                return;
                            }
                            if (camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                                Intent intent = new Intent();
                                intent.putExtra(HiDataValue.EXTRAS_KEY_UID, camera.getUid());
                                intent.setClass(getActivity(), AliveSettingActivity.class);
                                startActivity(intent);
                            } else {
                                HiToast.showToast(getActivity(), getString(R.string.click_offline_setting));
                            }
                        }
                    }
                    break;

                    case R.id.delete_icon_camera_item:
                        showDeleteCameraDialog(camera, Effectstype.Slidetop);
                        break;
                }
            }

        });
    }

    private void doLogOut() {
        try {
            Objects.requireNonNull(getActivity()).deleteDatabase(HiDataValue.DB_NAME);
            SharePreUtils.removeKey("isfshow", getActivity(), "isLogin");
            SharePreUtils.removeKey("isfshow", getActivity(), "pre_userid");
            SharePreUtils.removeKey("isfshow", getActivity(), "pre_customer_id");
            SharePreUtils.removeKey("isfshow", getActivity(), "pre_user_name");
            SharePreUtils.removeKey("isfshow", getActivity(), "pre_address");
            SharePreUtils.removeKey("isfshow", getActivity(), "pre_email");
            SharePreUtils.removeKey("isfshow", getActivity(), "pre_phone_no");
            SharePreUtils.removeKey("isfshow", getActivity(), "pre_camera_id");
            SharePreUtils.removeKey("isfshow", getActivity(), "pre_is_camera_live");
            SharePreUtils.removeKey("isfshow", getActivity(), "pre_create_date");
//
            Intent intent = new Intent(getActivity(), SplashActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendUnRegister(MyCamera mCamera, int enable) {
        if (mCamera.getPushState() == 1) {
            return;
        }

        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_TOKEN_UNREGIST)) {
            return;
        }

        byte[] info = CamHiDefines.HI_P2P_ALARM_TOKEN_INFO.parseContent(0, mCamera.getPushState(), (int) (System.currentTimeMillis() / 1000 / 3600), enable);
        mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_TOKEN_UNREGIST, info);
    }

    protected void sendRegisterToken(MyCamera mCamera) {
        if (mCamera.getPushState() == 1 || mCamera.getPushState() == 0) {
            return;
        }

        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_TOKEN_REGIST)) {
            return;
        }

        byte[] info = CamHiDefines.HI_P2P_ALARM_TOKEN_INFO.parseContent(0, mCamera.getPushState(), (int) (System.currentTimeMillis() / 1000 / 3600), 1);
        mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_TOKEN_REGIST, info);
    }

    MyCamera.OnBindPushResult bindPushResult = new MyCamera.OnBindPushResult() {
        @Override
        public void onBindSuccess(MyCamera camera) {
            HiLog.e("onBindSuccess");
            if (!camera.handSubXYZ()) {
                if (camera.handSubWTU()) {
                    camera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122);
                } else if (camera.handSubAACC()) {
                    camera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_AACC_148);
                } else if (camera.handSubSSAA()) {
                    camera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_SSAA_161);
                } else {
                    camera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_233);
                }
            } else {
                camera.setServerData(HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173);
            }
            camera.updateInDatabase(getActivity());
            sendServer(camera);
            sendRegisterToken(camera);
        }

        @Override
        public void onBindFail(MyCamera camera) {
            HiLog.e("==push" + "onBindFail");
        }

        @Override
        public void onUnBindSuccess(MyCamera camera) {
            //    camera.bindPushState(true, bindPushResult);
            HiLog.e("==push onUnBindSuccess");
            //  camera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_ADDRESS_GET, null);
        }

        @Override
        public void onUnBindFail(MyCamera camera) {
            HiLog.e("onUnBindFail");
            if (camera.getPushState() > 0) {
                SharePreUtils.putInt("subId", getActivity(), camera.getUid(), camera.getPushState());
            }

        }

        @Override
        public void onUpNameFail(MyCamera camera) {

        }

        @Override
        public void onUpNameSuccess(MyCamera camera) {

        }

    };

    private class CameraBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            HiLog.e("==ConnectState=" + "CameraBroadcastReceiver");

            if (intent.getAction().equals(HiDataValue.ACTION_CAMERA_INIT_END)) {
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }

                if (HiDataValue.ANDROID_VERSION >= 23 && !HiTools.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    return;
                }
                if (connectThread == null) {
                    connectThread = new HiThreadConnect();
                    connectThread.start();
                }
            }
        }
    }

    public class HiThreadConnect extends Thread {
        private int connnum = 0;

        public synchronized void run() {
            for (connnum = 0; connnum < HiDataValue.CameraList.size(); connnum++) {
                MyCamera camera = HiDataValue.CameraList.get(connnum);
                HiLog.e("==ConnectState=" + camera.getConnectState() + "");
                if (camera != null) {
                    if (camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED) {
                        camera.registerIOSessionListener(CameraFragment.this);
                        camera.connect();
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            if (connectThread != null) {
                connectThread = null;
            }
        }

    }


    @Override
    public void onResume() {
        super.onResume();
        delToNor();

        if (isNeedScorllToOldPosition) {
            mListView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mListView.setSelectionFromTop(lastPosition, lastY);
                    isNeedScorllToOldPosition = false;
                }
            }, 500);
        }
    }


    public void delToNor() {
        delModel = false;
        titleView.setRightText(R.string.btn_edit);
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }


    public class CameraListAdapter extends BaseAdapter {
        Context context;
        private LayoutInflater mInflater;
        OnButtonClickListener mListener;
        private String strState;

        public void setOnButtonClickListener(OnButtonClickListener listener) {
            mListener = listener;
        }

        public CameraListAdapter(Context context) {

            mInflater = LayoutInflater.from(context);
            this.context = context;
        }

        @Override
        public int getCount() {
            return HiDataValue.CameraList.size();
        }

        @Override
        public Object getItem(int position) {
            return HiDataValue.CameraList.get(position);
        }

        @Override
        public long getItemId(int arg0) {

            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final MyCamera camera = HiDataValue.CameraList.get(position);
            if (camera == null) {
                return null;
            }
            ViewHolder holder = new ViewHolder();
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.camera_main_item, null);
                holder.setting = (ImageView) convertView.findViewById(R.id.setting_camera_item);
                holder.img_snapshot = (ImageView) convertView.findViewById(R.id.snapshot_camera_item);
                holder.txt_nikename = (TextView) convertView.findViewById(R.id.nickname_camera_item);
                holder.txt_uid = (TextView) convertView.findViewById(R.id.uid_camera_item);
                holder.txt_state = (TextView) convertView.findViewById(R.id.state_camera_item);
                holder.img_alarm = (ImageView) convertView.findViewById(R.id.img_alarm);
                holder.delete_icon = (ImageView) convertView.findViewById(R.id.delete_icon_camera_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            if (holder != null) {
                if (camera.snapshot == null) {
                    holder.img_snapshot.setImageResource(R.drawable.videoclip);
                } else {
                    RoundedBitmapDrawable drawable = RoundedBitmapDrawableFactory.create(context.getResources(), camera.snapshot);
                    drawable.setCornerRadius(15);
                    holder.img_snapshot.setImageDrawable(drawable);
                }

                holder.txt_nikename.setText(camera.getNikeName());
                holder.txt_uid.setText(camera.getUid());
                int state = camera.getConnectState();
                switch (state) {
                    case 0:// DISCONNECTED
                        holder.txt_state.setTextColor(getResources().getColor(R.color.color_disconnected));
                        break;
                    case -8:
                    case 1:// CONNECTING
                        holder.txt_state.setTextColor(getResources().getColor(R.color.color_connecting));
                        break;
                    case 2:// CONNECTED
                        holder.txt_state.setTextColor(getResources().getColor(R.color.color_connected));
                        break;
                    case 3:// WRONG_PASSWORD
                        holder.txt_state.setTextColor(getResources().getColor(R.color.color_pass_word));
                        break;
                    case 4:// STATE_LOGIN
                        holder.txt_state.setTextColor(getResources().getColor(R.color.color_login));
                        break;
                }
                if (state >= 0 && state <= 4) {
                    strState = str_state[state];
                    holder.txt_state.setText(strState);
                }
                if (state == -8) {
                    holder.txt_state.setText(str_state[2]);
                }
                if (camera.isSystemState == 1 && camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                    holder.txt_state.setText(getString(R.string.tips_restart));
                }
                if (camera.isSystemState == 2 && camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                    holder.txt_state.setText(getString(R.string.tips_recovery));
                }
                if (camera.isSystemState == 3 && camera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                    holder.txt_state.setText(getString(R.string.tips_update));
                }
                holder.setting.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (camera.isErrorUID()) {
                            return;
                        }
                        if (mListener != null) {
                            mListener.onButtonClick(R.id.setting_camera_item, camera);
                        }
                    }
                });

                holder.delete_icon.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View arg0) {
                        if (mListener != null) {
                            mListener.onButtonClick(R.id.delete_icon_camera_item, camera);
                        }

                    }
                });

                if (delModel) {
                    holder.delete_icon.setVisibility(View.VISIBLE);
                } else {
                    holder.delete_icon.setVisibility(View.GONE);
                }

                if (camera.getAlarmState() == 0) {
                    holder.img_alarm.setVisibility(View.GONE);
                } else {
                    holder.img_alarm.setVisibility(View.VISIBLE);
                }
            }

            return convertView;
        }

        public void notifyItem(MyCamera CameraItem) {
            MyCamera camera = null;
            View view = null;
            int i = 0;
            for (i = 0; i < mListView.getChildCount(); i++) {
                view = mListView.getChildAt(i);
                if (view == null)
                    return;
                TextView txt_uid = (TextView) view.findViewById(R.id.uid_camera_item);
                if (!TextUtils.isEmpty(txt_uid.getText().toString().trim())) {
                    if (txt_uid.getText().toString().equals(CameraItem.getUid())) {
                        break;
                    }
                }
            }
            if (i == mListView.getChildCount()) {
                return;
            }
            ImageView img_alarm = (ImageView) view.findViewById(R.id.img_alarm);
            TextView txt_state = (TextView) view.findViewById(R.id.state_camera_item);
            int state = CameraItem.getConnectState();
            HiLog.e("notifyItem CameraF state=" + CameraItem.getUid() + "::连接状态::" + state);
            switch (state) {
                case 0:// DISCONNECTED
                    txt_state.setTextColor(getActivity().getResources().getColor(R.color.color_disconnected));
                    break;
                case -8:
                case 1:// CONNECTING
                    txt_state.setTextColor(getActivity().getResources().getColor(R.color.color_connecting));
                    break;
                case 2:// CONNECTED
                    txt_state.setTextColor(getActivity().getResources().getColor(R.color.color_connected));
                    break;
                case 3:// WRONG_PASSWORD
                    txt_state.setTextColor(getActivity().getResources().getColor(R.color.color_pass_word));
                    break;
                case 4:// STATE_LOGIN
                    txt_state.setTextColor(getActivity().getResources().getColor(R.color.color_login));
                    break;
            }
            if (state >= 0 && state <= 4) {
                strState = str_state[state];
                txt_state.setText(strState);
            }
            if (state == -8) {
                txt_state.setText(str_state[2]);
            }
            if (CameraItem.isSystemState == 1 && CameraItem.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                txt_state.setText(getString(R.string.tips_restart));
            }
            if (CameraItem.isSystemState == 2 && CameraItem.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                txt_state.setText(getString(R.string.tips_recovery));
            }
            if (CameraItem.isSystemState == 3 && CameraItem.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN) {
                txt_state.setText(getString(R.string.tips_update));
            }
            if (CameraItem.getAlarmState() == 0) {
                img_alarm.setVisibility(View.GONE);
            } else {
                img_alarm.setVisibility(View.VISIBLE);
            }
            if (saveopenswipeindex != -1 && saveopenswipeindex < HiDataValue.CameraList.size()) {
                camera = HiDataValue.CameraList.get(saveopenswipeindex);
                if (camera.getUid().equals(CameraItem.getUid())) {
                    mListView.smoothOpenMenu(saveopenswipeindex);
                }
            }

        }

        public class ViewHolder {
            public ImageView img_snapshot;
            public TextView txt_nikename;
            public TextView txt_uid;
            public TextView txt_state;
            public ImageView img_alarm;

            public ImageView setting;
            public ImageView delete_icon;

        }

    }

    @Override
    public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
        if (arg1 == HiChipDefines.HI_P2P_GET_SNAP && arg3 == 0) {
            MyCamera camera = (MyCamera) arg0;
            if (!camera.reciveBmpBuffer(arg2)) {
                return;
            }
        }
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

        if (HiDataValue.isDebug)
            HiLog.v("uid:" + arg0.getUid() + "  state:" + arg1);

        Message msg = handler.obtainMessage();
        msg.what = HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
        msg.arg1 = arg1;
        msg.obj = arg0;
        handler.sendMessage(msg);

    }

    @SuppressLint("HandlerLeak") private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            MyCamera camera = (MyCamera) msg.obj;
            switch (msg.what) {
                case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
                    if (adapter != null) {
                        camera.isSystemState = 0;
                        if (getActivity() != null) {
                            adapter.notifyItem(camera);
                        }
                    }
                    switch (msg.arg1) {
                        case HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED:
                            camera.mIsReceived_4179 = false;
                            break;
                        case HiCamera.CAMERA_CONNECTION_STATE_LOGIN:
                            if (camera.getEncryptionFlag() > 0) {
                                return;
                            }
                            if (camera.getCommandFunction(HiChipDefines.HI_P2P_SET_MD_PARAM_NEW)) {
                                camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_RESOLUTION, HiChipDefines.HI_P2P_RESOLUTION.parseContent(0, 1));
                            }
                            if (camera.isErrorUID()) {
                                camera.setErrorUID(true);
                                camera.setUpdateing(true);
                                camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_NET_PARAM, new byte[0]);
                                return;
                            }
                            if (HiTools.checkPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
                                String ssid = SsidUtils.getSSID(getActivity());
                                HiLog.e("ssid::" + ssid);
                                if (TextUtils.isEmpty(ssid)) {
                                    return;
                                }
                                if (ssid.startsWith("IPCAM-") || ssid.startsWith("WLAN PTZ-")) {
                                    setTime(camera);
                                }
                            }
                            if (camera.getPushState() > 0) {
                                if (camera.commandFunction.getAppCmdFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_GET)) {
                                    camera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_ADDRESS_GET, null);
                                } else {
                                    sendRegisterToken(camera);
                                }

                            }
                            if (!camera.getCommandFunction(HiChipDefines.HI_P2P_PB_QUERY_START_NODST) || camera.isFirstAdd) {
                                if (camera.getCommandFunction(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT)) {
                                    camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT, new byte[0]);
                                } else {
                                    camera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TIME_ZONE, new byte[0]);
                                }
                            }
                            break;
                        case HiCamera.CAMERA_CONNECTION_STATE_WRONG_PASSWORD:
                            break;
                        case HiCamera.CAMERA_CONNECTION_STATE_CONNECTING:
                            break;
                        case 7:
                            break;

                    }
                    break;
                case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
                    if (msg.arg2 == 0) {
                        handIOCTRLSucce(msg, camera);
                    } else {
                        switch (msg.arg1) {
                            case HiChipDefines.HI_P2P_GET_DEVICE_FISH_PARAM:
                                camera.mIsReceived_4179 = true;
                                camera.isWallMounted = false;
                                SharePreUtils.putBoolean("cache", getActivity(), camera.getUid() + "isWallMounted", false);
                                break;
                        }
                    }
                    break;

                case HiDataValue.HANDLE_MESSAGE_DELETE_FILE:
                    camera.disconnect(1);
                    camera.deleteInCameraList();
                    camera.deleteInDatabase(getActivity());
                    adapter.notifyDataSetChanged();
                    dismissjuHuaDialog();
                    HiToast.showToast(getActivity(), getString(R.string.tips_remove_success));
                    break;

            }
        }

        private void handIOCTRLSucce(Message msg, MyCamera camera) {
            Bundle bundle = msg.getData();
            byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);

            switch (msg.arg1) {

                case HiChipDefines.HI_P2P_GET_FUNCTION:
                    camera.commandFunction.setCmdfunction(new HiChipDefines.HI_P2P_FUNCTION(data));

                    break;

                case HiChipDefines.HI_P2P_GET_SNAP:
                    adapter.notifyDataSetChanged();
                    if (camera.snapshot != null) {
                        File rootFolder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/");
                        File sargetFolder = new File(rootFolder.getAbsolutePath() + "/android/data/" + getActivity().getResources().getString(R.string.app_name));

                        if (!rootFolder.exists()) {
                            rootFolder.mkdirs();
                        }
                        if (!sargetFolder.exists()) {
                            sargetFolder.mkdirs();
                        }
                    }
                    break;

                case HiChipDefines.HI_P2P_GET_TIME_ZONE: {

                    HiChipDefines.HI_P2P_S_TIME_ZONE timezone = new HiChipDefines.HI_P2P_S_TIME_ZONE(data);

                    if (timezone.u32DstMode == 1) {
                        camera.setSummerTimer(true);
                    } else {
                        camera.setSummerTimer(false);
                    }
                    if (camera.isFirstAdd) {
                        String locatGMT = TimeZoneUtils.getCurrentTimeZone(false);
                        int index = -1;
                        for (int i = 0; i < HiDefaultData.TimeZoneField.length; i++) {
                            if (HiDefaultData.TimeZoneField[i][0] == timezone.s32TimeZone) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            int gmt = HiDefaultData.TimeZoneField[index][0];
                            double d_locatGMT = Double.parseDouble(locatGMT);
                            if (gmt != d_locatGMT) {
                                showToTimeSetAc(camera);
                            }
                        }
                        camera.isFirstAdd = false;
                    }
                }
                break;
                case HiChipDefines.HI_P2P_GET_TIME_ZONE_EXT: {
                    HiChipDefines.HI_P2P_S_TIME_ZONE_EXT timezone = new HiChipDefines.HI_P2P_S_TIME_ZONE_EXT(data);
                    if (timezone.u32DstMode == 1) {
                        camera.setSummerTimer(true);
                    } else {
                        camera.setSummerTimer(false);
                    }
                    if (camera.isFirstAdd) {
                        String locatGMT = TimeZoneUtils.getCurrentTimeZone(true);
                        int index = -1;
                        for (int i = 0; i < HiDefaultData.TimeZoneField1.length; i++) {
                            if (isEqual(timezone.sTimeZone, HiDefaultData.TimeZoneField1[i][0])) {
                                index = i;
                                break;
                            }
                        }
                        if (index != -1) {
                            String gmt = HiDefaultData.TimeZoneField1[index][1];
                            if (!gmt.contains(locatGMT)) {
                                showToTimeSetAc(camera);
                            }
                        }
                        camera.isFirstAdd = false;
                    }
                    break;
                }
                case CamHiDefines.HI_P2P_ALARM_TOKEN_REGIST:

                    break;
                case CamHiDefines.HI_P2P_ALARM_TOKEN_UNREGIST:
                    break;
                case CamHiDefines.HI_P2P_ALARM_ADDRESS_SET:
                    //camera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_ADDRESS_GET, null);
                    break;
                case CamHiDefines.HI_P2P_ALARM_ADDRESS_GET:
                    CamHiDefines.HI_P2P_ALARM_ADDRESS ADDRESS = new CamHiDefines.HI_P2P_ALARM_ADDRESS(data);
                    String add = new String(ADDRESS.szAlarmAddr).trim();
                    if (!camera.getPushAddressByUID().equalsIgnoreCase(add)) {
                        if (camera.getPushState() > 0) {
                            sendUnRegister(camera, 0);
                            camera.bindPushState(true, bindPushResult);
                        }
                    } else {
                        sendRegisterToken(camera);
                    }
                    break;

                case HiChipDefines.HI_P2P_ALARM_EVENT: {
                    if (camera.getPushState() == 0) {
                        return;
                    }
                    camera.setLastAlarmTime(System.currentTimeMillis());
                    HiChipDefines.HI_P2P_EVENT event = new HiChipDefines.HI_P2P_EVENT(data);
                    showAlarmNotification(camera, event, System.currentTimeMillis());
                    saveAlarmData(camera, event.u32Event, (int) (System.currentTimeMillis() / 1000));
                    camera.setAlarmState(1);
                    camera.setAlarmLog(true);
                    if (getActivity() != null) {
                        if (adapter != null) {//change by #7568321 #175771 #176090
                            adapter.notifyItem(camera);
                        }
                    }
                }
                break;
                case HiChipDefines.HI_P2P_GET_DEVICE_FISH_PARAM:
                    camera.mIsReceived_4179 = true;
                    HiChipDefines.HI_P2P_DEV_FISH fishmod = new HiChipDefines.HI_P2P_DEV_FISH(data);
                    float xcircle = fishmod.xcircle;
                    float ycircle = fishmod.ycircle;
                    float rcircle = fishmod.rcircle;
                    SharePreUtils.putFloat("chche", getContext(), camera.getUid() + "xcircle", xcircle);
                    SharePreUtils.putFloat("chche", getContext(), camera.getUid() + "ycircle", ycircle);
                    SharePreUtils.putFloat("chche", getContext(), camera.getUid() + "rcircle", rcircle);
                    SharePreUtils.putInt("mInstallMode", getContext(), camera.getUid(), fishmod.mold);
                    if (fishmod.fish == 1 && (fishmod.type == 2 || fishmod.type == 4)) {//1:
                        camera.isWallMounted = true;
                        SharePreUtils.putBoolean("cache", getActivity(), camera.getUid() + "isWallMounted", true);
                    } else {
                        camera.isWallMounted = false;
                        SharePreUtils.putBoolean("cache", getActivity(), camera.getUid() + "isWallMounted", false);
                    }
                    camera.putFishModType(fishmod.type);
                    break;
                case HiChipDefines.HI_P2P_GET_RESOLUTION:
                    HiChipDefines.HI_P2P_RESOLUTION param = new HiChipDefines.HI_P2P_RESOLUTION(data);
                    camera.u32Resolution = param.u32Resolution;
                    break;
                case HiChipDefines.HI_P2P_GET_DEV_INFO_EXT:
                    HiChipDefines.HI_P2P_GET_DEV_INFO_EXT deviceInfo = new HiChipDefines.HI_P2P_GET_DEV_INFO_EXT(data);
                    String extVersion = Packet.getString(deviceInfo.aszSystemSoftVersion);
                    String extModel=Packet.getString(deviceInfo.aszSystemModel);
                    checkIsIngenicByVersion(extVersion, camera);
                    handHsAudioByVersion(extVersion,extModel,camera);
                    break;
                case HiChipDefines.HI_P2P_GET_DEV_INFO:
                    HiChipDefines.HI_P2P_S_DEV_INFO info = new HiChipDefines.HI_P2P_S_DEV_INFO(data);
                    String version = Packet.getString(info.strSoftVer);
                    checkIsIngenicByVersion(version, camera);
                    break;

                case HiChipDefines.HI_P2P_GET_NET_PARAM:

                    HiChipDefines.HI_P2P_S_NET_PARAM net_param = new HiChipDefines.HI_P2P_S_NET_PARAM(data);
                    String ip = Packet.getString(net_param.strIPAddr);
                    int port = net_param.u32Port;

                    if (camera.isErrorUID()) {
                        String ssid = SsidUtils.getSSID(getActivity());
                        String[] uidArray = camera.getUid().split("-");
                        boolean isShowApTip = false;
                        if (!TextUtils.isEmpty(ssid) && !TextUtils.isEmpty(uidArray[1])) {
                            if (ssid.startsWith("IPCAM-" + uidArray[1])) {
                                isShowApTip = true;
                            }
                        }
                        handleErrorUID(camera, ip, port, isShowApTip);

                    }
                    break;
            }
        }
    };


    private void showToTimeSetAc(final MyCamera myCamera) {
        final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(getActivity());
        dialog.withTitle(getString(R.string.tip_reminder)).withMessage(getString(R.string.time_tip_content)).
                withEffect(Effectstype.Slidetop).
                setButton1Click(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                }).setButton2Click(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                Intent intent = new Intent(getActivity(), TimeSettingActivity.class);
                intent.putExtra(HiDataValue.EXTRAS_KEY_UID, myCamera.getUid());
                startActivity(intent);
            }
        }).show();
    }

    private boolean isEqual(byte[] bys, String str) {
        String string = new String(bys);
        String temp = string.substring(0, str.length());
        if (temp.equalsIgnoreCase(str)) {
            return true;
        }
        return false;
    }

    public String getCurrentTimeZone() {
        TimeZone tz = TimeZone.getDefault();
        float tim = (float) tz.getRawOffset() / (3600000.0f);
        String gmt = null;

        Calendar cal = Calendar.getInstance(Locale.getDefault());
        Date datad = cal.getTime();
        boolean daylightT = tz.inDaylightTime(datad);
        if (daylightT)
            tim += 1;

        gmt = "GMT" + tim;
        if (tim > 0) {
            gmt = "GMT+" + tim;
        }
        return gmt;
        //TimeZone tz = TimeZone.getDefault();
        //return createGmtOffsetString(true, true, tz.getRawOffset());
    }

    public String createGmtOffsetString(boolean includeGmt, boolean includeMinuteSeparator, int offsetMillis) {
        int offsetMinutes = offsetMillis / 60000;
        char sign = '+';
        if (offsetMinutes < 0) {
            sign = '-';
            offsetMinutes = -offsetMinutes;
        }
        StringBuilder builder = new StringBuilder(9);
        if (includeGmt) {
            builder.append("GMT");
        }
        builder.append(sign);
        appendNumber(builder, 1, offsetMinutes / 60);
        if (includeMinuteSeparator) {
            builder.append(':');
        }
        appendNumber(builder, 2, offsetMinutes % 60);
        return builder.toString();
    }

    private static void appendNumber(StringBuilder builder, int count, int value) {
        String string = Integer.toString(value);
        for (int i = 0; i < count - string.length(); i++) {
            builder.append('0');
        }
        builder.append(string);
    }

    private void checkIsIngenicByVersion(String version, MyCamera camera) {
        if (TextUtils.isEmpty(version)) {
            return;
        }
        if (version.startsWith("V17") || version.startsWith("V18")) {
            camera.isIngenic = true;
        }
    }

    private void handHsAudioByVersion(String version,String extModel ,MyCamera camera){
        if (TextUtils.isEmpty(version)||TextUtils.isEmpty(extModel)) {
            return;
        }
        if ((version.startsWith("V20") || version.startsWith("V19"))&&extModel.contains("Z3")) {
            camera.isHsEV = true;
        }
    }
    @SuppressWarnings("deprecation")
    private void showAlarmNotification(MyCamera camera, HiChipDefines.HI_P2P_EVENT event, long evtTime) {
        try {

            NotificationManager manager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
            Bundle extras = new Bundle();
            extras.putString(HiDataValue.EXTRAS_KEY_UID, camera.getUid());
            extras.putInt("type", 1);
            Intent intent = new Intent(getActivity(), SplashActivity.class);
            intent.setAction(Intent.ACTION_MAIN);
            //			intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.putExtras(extras);
            PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            String[] alarmList = getResources().getStringArray(R.array.tips_alarm_list_array);
            String type = null;

            switch (event.u32Event) {
                case 0:
                    type = alarmList[0];
                    break;
                case 1:
                    type = alarmList[1];
                    break;
                case 2:
                    type = alarmList[2];
                    break;
                case 3:
                    type = alarmList[3];
                    break;
                case 6:
                    String sType = new String(event.sType).trim();
                    if ("key2".equals(sType)) {
                        type = getString(R.string.alarm_sos);
                    } else if ("key3".equals(sType)) {
                        type = getString(R.string.alarm_ring);
                    } else if ("door".equals(sType)) {
                        type = getString(R.string.alarm_door);
                    } else if ("infra".equals(sType)) {
                        type = getString(R.string.alarm_infra);
                    } else if ("beep".equals(sType)) {
                        type = getString(R.string.alarm_doorbell);
                    } else if ("fire".equals(sType)) {
                        type = getString(R.string.alarm_smoke);
                    } else if ("gas".equals(sType)) {
                        type = getString(R.string.alarm_gas);
                    } else if ("socket".equals(sType)) {
                        type = getString(R.string.alarm_socket);
                    } else if ("temp".equals(sType)) {
                        type = getString(R.string.alarm_temp);
                    } else if ("humi".equals(sType)) {
                        type = getString(R.string.alarm_humi);
                    }
                    break;
                case 12:
                    type = alarmList[4];
                    break;
            }
            HiLog.e("==CameraFragment==" + "==\n" + "u32Event==" + event.u32Event + "\n" + "u32Time==" + event.u32Time + "\n" + "u32Channel==" + event.u32Channel + "\n" + "sType==" + Arrays.toString(event.sType) + "\n" + "sReserved==" + Arrays.toString(event.sReserved) + "\n" + "type==" + type);
            if (TextUtils.isEmpty(type)) {
                return;
            }

            NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(getContext());
            boolean areNotificationsEnabled = notificationManagerCompat.areNotificationsEnabled();

            if (!areNotificationsEnabled) {
                HiToast.showToast(getActivity(), getActivity().getResources().getString(R.string.tips_open_notification));
                return;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                assert manager != null;
                NotificationChannel channel = manager.getNotificationChannel("camera_notification");
                if (channel.getImportance() == NotificationManager.IMPORTANCE_NONE) {
                    HiToast.showToast(getActivity(), getActivity().getResources().getString(R.string.tips_open_notification));
                } else {
                    NotificationManager notificationManager = (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
                    Notification notification = new NotificationCompat.Builder(getContext(), "camera_notification").setSmallIcon(R.drawable.ic_launcher).setTicker(camera.getNikeName())
                            .setContentTitle(camera.getNikeName())//camera.getUid()
                            .setContentText(type + "  " + camera.getUid())//type
                            .setContentIntent(pendingIntent).build();
                    notification.flags |= Notification.FLAG_AUTO_CANCEL;
                    notification.defaults = Notification.DEFAULT_LIGHTS;
                    ranNum++;
                    notificationManager.notify(ranNum, notification);
                }

            } else {
                //HiLog.e("==CameraFragment==2",event.u32Event+"--"+type);
                Notification notification = new Notification.Builder(getActivity()).setSmallIcon(R.drawable.ic_launcher).setTicker(camera.getNikeName())
                        .setContentTitle(camera.getNikeName())//camera.getNikeName()
                        .setContentText(type + "  " + camera.getUid())
                        .setContentIntent(pendingIntent).getNotification();
                notification.flags |= Notification.FLAG_AUTO_CANCEL;
                notification.defaults = Notification.DEFAULT_ALL;
                ranNum++;
                manager.notify(ranNum, notification);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void setServer(MyCamera mCamera) {
        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET)) {
            return;
        }

        if (HiDataValue.ANDROID_VERSION >= 23) {
            if (!HiTools.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }
        }
    }

    protected void sendServer(MyCamera mCamera) {
        if (mCamera.getServerData() == null) {
            mCamera.setServerData(mCamera.getPushAddressByUID());
            mCamera.updateServerInDatabase(getActivity());
        }
        if (!mCamera.getCommandFunction(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET)) {
            return;
        }
        if (mCamera.push != null) {
            String[] strs = mCamera.push.getPushServer().split("\\.");
            if (strs.length == 4 && isInteger(strs[0]) && isInteger(strs[1]) && isInteger(strs[2]) && isInteger(strs[3])) {
                byte[] info = CamHiDefines.HI_P2P_ALARM_ADDRESS.parseContent(mCamera.push.getPushServer());
                HiLog.e("==push.getPushServer()" + "sendServer()" + mCamera.push.getPushServer());
                mCamera.sendIOCtrl(CamHiDefines.HI_P2P_ALARM_ADDRESS_SET, info);
            }

        }
    }

    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    }

    private void saveAlarmData(MyCamera camera, int evtType, int evtTime) {

        DatabaseManager manager = new DatabaseManager(getActivity());
        manager.addAlarmEvent(camera.getUid(), evtTime, evtType);

    }

    private void setTime(MyCamera camera) {
        Calendar cal = Calendar.getInstance(TimeZone.getDefault());
        cal.setTimeInMillis(System.currentTimeMillis());

        byte[] time = HiChipDefines.HI_P2P_S_TIME_PARAM.parseContent(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND));

        camera.sendIOCtrl(HiChipDefines.HI_P2P_SET_TIME_PARAM, time);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            getActivity().unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (connectThread != null) {
            connectThread.interrupt();
        }

    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final MyCamera selectedCamera = HiDataValue.CameraList.get(position);
        if (delModel) {
            Intent intent = new Intent();
            intent.putExtra(HiDataValue.EXTRAS_KEY_UID, selectedCamera.getUid());
            intent.setClass(getActivity(), EditCameraActivity.class);
            startActivity(intent);
        } else {
            if (selectedCamera.isErrorUID()) {
                HiToast.showToast(getActivity(), getString(R.string.tip_error_uid) + getString(R.string.tips_update));
                return;
            }
            Log.e("Flag", "getEncryptionFlag=" + selectedCamera.getEncryptionFlag());
            if (selectedCamera.getEncryptionFlag() != 0) {
                HiToast.showToast(getContext(), str_state[5]);
                return;
            }
            if (selectedCamera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_LOGIN && selectedCamera.mIsReceived_4179 == true) {
                Bundle extras = new Bundle();
                extras.putString(HiDataValue.EXTRAS_KEY_UID, selectedCamera.getUid());
                Intent intent = new Intent();
                intent.putExtras(extras);
                if (selectedCamera.isFishEye() && selectedCamera.isWallMounted) {
                    intent.setClass(getActivity(), WallMountedActivity.class);
                } else if (selectedCamera.isFishEye()) {
                    int num = SharePreUtils.getInt("mInstallMode", getActivity(), selectedCamera.getUid());
                    selectedCamera.mInstallMode = num == -1 ? 0 : num;
                    boolean bl = SharePreUtils.getBoolean("cache", getActivity(), selectedCamera.getUid());
                    selectedCamera.isFirst = bl;
                    intent.setClass(getActivity(), FishEyeActivity.class);
                } else {
                    intent.setClass(getActivity(), LiveViewActivity.class);
                }
                startActivity(intent);
                RecordLastPosition();
                HiDataValue.isOnLiveView = true;
                selectedCamera.setAlarmState(0);
                adapter.notifyDataSetChanged();
            } else if (selectedCamera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED || selectedCamera.getConnectState() == HiCamera.CAMERA_CONNECTION_STATE_WRONG_PASSWORD) {
                if (HiDataValue.ANDROID_VERSION >= 23 && !HiTools.checkPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showAlertDialog();
                    return;
                }
                selectedCamera.connect();
                selectedCamera.registerIOSessionListener(CameraFragment.this);
                adapter.notifyDataSetChanged();
            } else if (selectedCamera.getConnectState() == HiCamera.CAMERA_CHANNEL_APP_VERSIONERROR) {
                HiToast.showToast(getContext(), Objects.requireNonNull(getContext()).getResources().getStringArray(R.array.connect_state)[5]);
                return;
            } else {
                if (selectedCamera.isErrorUID()) {
                    HiToast.showToast(getActivity(), getString(R.string.tip_error_uid) + getString(R.string.tips_update));
                } else {
                    HiToast.showToast(getActivity(), getString(R.string.click_offline_setting));
                    return;
                }

            }
        }

    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(getString(R.string.tips_no_permission));
        builder.setPositiveButton(getString(R.string.setting), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_SETTINGS);
                // intent.setAction("android.intent.action.MAIN");
                //intent.setClassName("com.android.settings", "com.android.settings.ManageApplications");
                startActivity(intent);

            }
        });
        builder.setNegativeButton(getString(R.string.cancel), null);
        builder.setCancelable(false);
        builder.show();

    }

    private void RecordLastPosition() {
        isNeedScorllToOldPosition = true;
        if (mListView.getChildCount() < 1)
            return;
        try {
            lastPosition = mListView.getFirstVisiblePosition();
            lastY = mListView.getChildAt(0).getTop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void unBindJGorFCM(MyCamera mCamera, boolean isReGJ, boolean isReFCM, boolean isReFives) {
        if (mCamera == null) {
            return;
        }

        //String jgToken = JPushInterface.getRegistrationID(Objects.requireNonNull(getActivity()));
        if (SystemUtils.isZh(Objects.requireNonNull(getActivity()))) {
            if (!TextUtils.isEmpty(HiDataValue.FcmToken) && isReFCM) {
                unBindFCM(mCamera);
            }
        } else {
            if (!TextUtils.isEmpty(HiDataValue.NewPushToken) && isReFives) {
                unBindFives(mCamera);
            }
        }

    }

    private void unBindFives(final MyCamera mCamera) {
        String pushName = TokenUtils.getPhoneName(getActivity());
        if (TextUtils.isEmpty(pushName)) {
            return;
        }
        HiPushSDK unPush = new HiPushSDK(getActivity(), HiDataValue.NewPushToken + "&notify=1", mCamera.getUid(), HiDataValue.company, pushName, mCamera.pushResult, getServiceAddressByUID(mCamera));
        unPush.unbind(mCamera.getPushState());
        SharePreUtils.putBoolean("cache", getActivity(), mCamera.getUid() + "isReFives", false);

    }

    private void unBindJiGuang(final MyCamera mCamera, String jgToken) {

        HiPushSDK unPush = new HiPushSDK(getActivity(), jgToken + "&notify=1", mCamera.getUid(), HiDataValue.company, "jiguang", mCamera.pushResult, getServiceAddressByUID(mCamera));
        unPush.unbind(mCamera.getPushState());
        SharePreUtils.putBoolean("cache", getActivity(), mCamera.getUid() + "isReJG", false);

    }

    private void unBindFCM(final MyCamera mCamera) {
        HiPushSDK fcmpush = new HiPushSDK(getActivity(), HiDataValue.FcmToken + "&notify=1", mCamera.getUid(), HiDataValue.company, "fcm", mCamera.pushResult, getServiceAddressByUID(mCamera));
        fcmpush.unbind(mCamera.getPushState());
        SharePreUtils.putBoolean("cache", getActivity(), mCamera.getUid() + "isReFCM", false);
    }

    private String getServiceAddressByUID(MyCamera mCamera) {
        if (mCamera.handSubXYZ()) {
            return HiDataValue.CAMERA_ALARM_ADDRESS_XYZ_173;
        } else if (mCamera.handSubWTU()) {
            return HiDataValue.CAMERA_ALARM_ADDRESS_WTU_122;
        } else {
            return HiDataValue.CAMERA_ALARM_ADDRESS_233;
        }
    }


    private void handleErrorUID(final MyCamera camera, final String ip, final int port, final boolean isShowApTip) {
        getAfType(camera, ip, port, isShowApTip);
    }

    private void getAfType(final MyCamera camera, final String ip, final int port, final boolean isShowApTip) {
        String name = camera.getUsername();
        String pwd = camera.getPassword();
        final String param = name + ":" + pwd;
        final String uid = camera.getUid();
        HiWriteUIDSDK mhiwrite = new HiWriteUIDSDK(new HiWriteUIDSDK.IWriteUIDResult() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onReceiveLitosResult(String s, String s1, String s2, int i, int i1) {
                HiLog.e(s + ":::" + s1 + ":::" + s2 + ":::" + i + ";;;;" + i1 + ";;;;");
                String uid = "";
                if (i1 == 191128) {
                    if (i == 0) {
                        int type = Integer.parseInt(s2);
                        if (type == 1) {
                            uid = Objects.requireNonNull(blackUidMap.get(s))[1];
                        } else {
                            uid = Objects.requireNonNull(blackUidMap.get(s))[0];
                        }

                        if (!TextUtils.isEmpty(uid)) {
                            writeUId(uid, camera, ip, port, isShowApTip);
                        }
                    }
                }
            }
        });
        mhiwrite.startGetAF(ip, port, Base64.encodeToString(param.getBytes(), Base64.DEFAULT), 15, 5, uid.getBytes());

    }

    private void writeUId(String uid, final MyCamera camera, final String ip, final int port, final boolean isShowApTip) {
        String name = camera.getUsername();
        String pwd = camera.getPassword();
        final String param = name + ":" + pwd;
        HiWriteUIDSDK mhiwrite = new HiWriteUIDSDK(new HiWriteUIDSDK.IWriteUIDResult() {
            @SuppressLint("StringFormatInvalid")
            @Override
            public void onReceiveLitosResult(String s, String s1, String s2, int i, int i1) {
                HiLog.e(s + ":::" + s1 + ":::" + s2 + ":::" + i + ";;;;" + i1 + ";;;;");
                if (i1 == 191129) {
                    if (i == 1) {

                        Message message = new Message();
                        message.what = DELETE_CAM;
                        message.obj = camera;
                        ipTiphandler.sendMessage(message);

                        MyCamera myCamera = new MyCamera(getContext(), camera.getNikeName(), s, camera.getUsername(), camera.getPassword());

                        Message message1 = new Message();
                        message1.what = UPDATE_CAM;
                        message1.obj = myCamera;
                        if (isShowApTip) {
                            message1.arg1 = 1;
                        } else {
                            message1.arg1 = 0;
                        }
                        ipTiphandler.sendMessage(message1);
                    }
                }
            }
        });
        mhiwrite.startWriteUIDRequest(ip, port, Base64.encodeToString(param.getBytes(), Base64.DEFAULT), 15, 5, uid.getBytes());
    }

    @SuppressLint("HandlerLeak") Handler ipTiphandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0x1111111:
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    break;
                case 0x11111112:
                    final MyCamera camera12 = (MyCamera) msg.obj;
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                    Intent broadcast = new Intent();
                    broadcast.setAction(HiDataValue.ACTION_CAMERA_INIT_END);
                    getActivity().sendBroadcast(broadcast);
                    int arg1 = msg.arg1;
                    String s = camera12.getUid();
                    if (arg1 == 1) {
                        String ipcam = "(IPCAM-" + s.split("-")[1] + ")";
                        showApDialog(ipcam);
                    }
                    break;
                case DELETE_CAM:
                    final MyCamera mcamera = (MyCamera) msg.obj;
                    if (mcamera == null) {
                        return;
                    }
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            mcamera.disconnect(1);
                            mcamera.deleteInCameraList();
                            mcamera.deleteInDatabase(getActivity());
                            Message message = new Message();
                            message.what = 0x1111111;
                            ipTiphandler.sendMessage(message);
                        }
                    }).start();
                    break;
                case UPDATE_CAM:
                    final MyCamera camera1 = (MyCamera) msg.obj;
                    final int myarg1 = msg.arg1;
                    if (camera1 == null)
                        return;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            HiLog.e(camera1.uid);
                            camera1.saveInDatabase(getActivity());
                            camera1.saveInCameraList();
                            Message message = new Message();
                            message.what = 0x11111112;
                            message.arg1 = myarg1;
                            message.obj = camera1;
                            ipTiphandler.sendMessage(message);
                        }
                    }).start();
                    break;
            }
        }
    };


    @SuppressLint("StringFormatInvalid")
    private void showApDialog(String ipcam) {
        String context = getResources().getString(R.string.tip_ap);
        String msg = String.format(context, ipcam);
        new DialogUtils(getActivity()).title(getString(R.string.tip_hint)).message(msg).cancelText(getString(R.string.cancel)).sureText(getString(R.string.sure)).setCancelOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        }).setSureOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(i);
            }
        }).build().show();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            if (HiDataValue.shareIsOpen) {
                if (titleView != null) {
                    titleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
                    titleView.setLeftBtnTextBackround(R.drawable.share);
                    titleView.setLeftBackroundPadding(2, 2, 2, 2);
                }
            }
        }
    }
}
