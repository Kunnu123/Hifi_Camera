package com.hificamera.hichip.activity.RF;

import java.util.ArrayList;
import java.util.List;

import com.hificamera.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hificamera.thecamhi.base.TitleView;
import com.hificamera.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.MyCamera;
import com.hificamera.thecamhi.bean.RFDevice;
import com.hificamera.thecamhi.main.HiActivity;
import com.hificamera.thecamhi.main.MainActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class RemoteControlActivity extends HiActivity implements OnClickListener, ICameraIOSessionCallback {
	private TitleView mTitleView;
	private Button mBtnOpen, but_sos, but_ring, but_close;
	private MyCamera mMyCamera;
	private int mRFType;
	protected List<RFDevice> list_rf_device_key = new ArrayList<>();
	public static String KEY_0 = "key0";
	public static String KEY_1 = "key1";
	public static String KEY_2 = "key2";
	public static String KEY_3 = "key3";
	public static String KEY = "KEY";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_remote_control);
		getIntentData();
		initView();
		setListerners();
		getData();
	}

	private void getData() {
		mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET, null);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mMyCamera != null) {
			mMyCamera.registerIOSessionListener(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mMyCamera != null) {
			mMyCamera.unregisterIOSessionListener(this);
		}
	}

	private void getIntentData() {
		String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
		mRFType = getIntent().getIntExtra(HiDataValue.EXTRAS_RF_TYPE, 0);
		if (TextUtils.isEmpty(uid)) {
			finish();
			return;
		}
		for (MyCamera camera : HiDataValue.CameraList) {
			if (uid.equals(camera.getUid())) {
				this.mMyCamera = camera;
				break;
			}
		}
	}

	private void setListerners() {
		mBtnOpen.setOnClickListener(this);
		but_sos.setOnClickListener(this);
		but_ring.setOnClickListener(this);
		but_close.setOnClickListener(this);

	}

	private void initView() {
		mTitleView = (TitleView) findViewById(R.id.remote_control_top);
		mTitleView.setTitle("function button");
		mTitleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		mTitleView.setNavigationBarButtonListener(new NavigationBarButtonListener() {
			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
					case TitleView.NAVIGATION_BUTTON_LEFT:
						RemoteControlActivity.this.finish();
						break;
				}
			}
		});
		mBtnOpen = (Button) findViewById(R.id.but_open);
		but_sos = (Button) findViewById(R.id.but_sos);
		but_ring = (Button) findViewById(R.id.but_ring);
		but_close = (Button) findViewById(R.id.but_close);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.but_open:
				Intent intent = new Intent(RemoteControlActivity.this, CheckRFCodeActivity.class);
				intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
				intent.putExtra(HiDataValue.EXTRAS_RF_TYPE, mRFType);
				intent.putExtra(KEY, KEY_1);
				startActivity(intent);
				break;
			case R.id.but_close:
				intent = new Intent(RemoteControlActivity.this, CheckRFCodeActivity.class);
				intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
				intent.putExtra(HiDataValue.EXTRAS_RF_TYPE, mRFType);
				intent.putExtra(KEY, KEY_0);
				startActivity(intent);
				break;
			case R.id.but_sos:
				intent = new Intent(RemoteControlActivity.this, CheckRFCodeActivity.class);
				intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
				intent.putExtra(HiDataValue.EXTRAS_RF_TYPE, mRFType);
				intent.putExtra(KEY, KEY_2);
				startActivity(intent);
				break;
			case R.id.but_ring:
				intent = new Intent(RemoteControlActivity.this, CheckRFCodeActivity.class);
				intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
				intent.putExtra(HiDataValue.EXTRAS_RF_TYPE, mRFType);
				intent.putExtra(KEY, KEY_3);
				startActivity(intent);
				break;
		}

	}

	@Override
	public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
		if (arg0 != mMyCamera) {
			return;
		}
		Message message = Message.obtain();
		message.arg1 = arg1;
		message.arg2 = arg3;
		message.what = HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL;
		Bundle bundle = new Bundle();
		bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
		message.setData(bundle);
		mHandler.sendMessage(message);

	}

	@Override
	public void receiveSessionState(HiCamera arg0, int arg1) {
		if(arg0!=mMyCamera) return;
		Message msg=Message.obtain();
		msg.what=HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
		msg.arg1=arg1;
		mHandler.sendMessage(msg);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
					if(msg.arg1==HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED){
						Intent intent=new Intent(RemoteControlActivity.this,MainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					break;
				case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
					if (msg.arg2 == 0) {
						byte[] data = msg.getData().getByteArray(HiDataValue.EXTRAS_KEY_DATA);
						switch (msg.arg1) {
							case HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET:
								HiChipDefines.HI_P2P_IPCRF_ALL_INFO allRfInfo = new HiChipDefines.HI_P2P_IPCRF_ALL_INFO(data);
								for (int i = 0; i < allRfInfo.sRfInfo.length; i++) {
									HiChipDefines.HI_P2P_IPCRF_INFO info = allRfInfo.sRfInfo[i];
									String code = new String(info.sRfCode).trim();
									if (!TextUtils.isEmpty(code) && code.length() > 10) {
										String str = new String(info.sType).trim();
										str = str.substring(0, 3);
										if ("key".equals(str)) {
											RFDevice device = new RFDevice(new String(info.sName).trim(), new String(info.sType).trim(), new String(info.sRfCode).trim(), info.u32Index, info.u32Enable);
											list_rf_device_key.add(device);
										}
									}
								}
								for (RFDevice device : list_rf_device_key) {
									if (device.getType().equals(RemoteControlActivity.KEY_0)) {
										but_close.setEnabled(false);
										but_close.setText("RF alarm: off (added)");
									} else if (device.getType().equals(RemoteControlActivity.KEY_1)) {
										mBtnOpen.setEnabled(false);
										mBtnOpen.setText("RF alarm: On (added)");
									} else if (device.getType().equals(RemoteControlActivity.KEY_2)) {
										but_sos.setEnabled(false);
										but_sos.setText("SOS (added)");
									} else if (device.getType().equals(RemoteControlActivity.KEY_3)) {
										but_ring.setEnabled(false);
										but_ring.setText("Alarm bell (added)");
									}
								}

								break;
						}

					} else {

					}

					break;

			}

		};
	};

}
