package com.hificamera.hichip.activity.RF;

import java.util.ArrayList;
import java.util.List;

import com.hificamera.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.content.HiChipDefines.HI_P2P_IPCRF_INFO;
import com.hichip.control.HiCamera;
import com.hificamera.hichip.widget.SwitchButton;
import com.hificamera.thecamhi.base.HiToast;
import com.hificamera.thecamhi.base.TitleView;
import com.hificamera.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.MyCamera;
import com.hificamera.thecamhi.bean.RFDevice;
import com.hificamera.thecamhi.main.HiActivity;
import com.hificamera.thecamhi.main.MainActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SetUpAndAddRFActivity extends HiActivity implements OnClickListener, ICameraIOSessionCallback {
	private TitleView mTitleView;
	private TextView mTvCodeContent, tv_rf_type_con;
	private String mRFType;
	private byte[] mCode;
	private EditText mEditName, edit_preset_con;
	private SwitchButton swibut_enable, swibut_alarm;
	private Button mBtnAdd;
	private String mKey;
	private TextView tv_rf_alarm, tv_rf_pz_preset;
	private MyCamera mMyCamera;
	private String mRfName;
	private boolean mIsEdit = false;
	private List<HI_P2P_IPCRF_INFO> list_rf_info = new ArrayList<HI_P2P_IPCRF_INFO>();
	private RFDevice rfDevice;
	private TextView tv_tips;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setup_and_add);
		getIntentData();
		getData();
		initView();
		setListeners();
	}

	private void getData() {
		if (mIsEdit) {
			int enable = 0;
			int index = rfDevice.getU32Index();
			String code = " ";
			String type = " ";
			String name = " ";
			byte voiceLink = (byte) 0;
			byte ptzLink = (byte) 0;
			mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_GET, HI_P2P_IPCRF_INFO.parseContent(index, enable, code, type, name, voiceLink, ptzLink));
		}
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

	private void setListeners() {
		mBtnAdd.setOnClickListener(this);

	}

	private void getIntentData() {
		mRFType = getIntent().getStringExtra(HiDataValue.EXTRAS_RF_TYPE);
		mCode = getIntent().getByteArrayExtra(HiDataValue.EXTRAS_KEY_DATA);
		mKey = getIntent().getStringExtra(RemoteControlActivity.KEY);
		mIsEdit = getIntent().getBooleanExtra("edit", false);
		rfDevice = (RFDevice) getIntent().getSerializableExtra("device");
		String uid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
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

	private void initView() {
		mTitleView = (TitleView) findViewById(R.id.top_setup_and_add);
		mTitleView.setTitle(mIsEdit ? "Modify the sensor" : "Set up and add RF");
		mTitleView.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		mTitleView.setNavigationBarButtonListener(new NavigationBarButtonListener() {
			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					SetUpAndAddRFActivity.this.finish();
					break;
				}
			}
		});
		mTvCodeContent = (TextView) findViewById(R.id.tv_rf_code_con);
		tv_rf_type_con = (TextView) findViewById(R.id.tv_rf_type_con);
		mEditName = (EditText) findViewById(R.id.tv_rf_name_con);
		tv_tips = (TextView) findViewById(R.id.tv_tips);
		if (mIsEdit) {
			mTvCodeContent.setText(rfDevice.getCode());
			mRFType = rfDevice.getType();
			tv_tips.setText("Sensor information");
			//mEditName.setText(rfDevice.getName());
		} else {
			mTvCodeContent.setText(new String(mCode).trim());
		}
		swibut_enable = (SwitchButton) findViewById(R.id.swibut_enable);
		swibut_alarm = (SwitchButton) findViewById(R.id.swibut_alarm);
		edit_preset_con = (EditText) findViewById(R.id.edit_preset_con);
		mBtnAdd = (Button) findViewById(R.id.btn_add);
		tv_rf_alarm = (TextView) findViewById(R.id.tv_rf_alarm);
		tv_rf_pz_preset = (TextView) findViewById(R.id.tv_rf_pz_preset);
		if (mKey != null) {
			hideAndVisView(false);
			if (mKey.equals(RemoteControlActivity.KEY_0)) {
				tv_rf_type_con.setText("Alarm: Off");
				mEditName.setText("Alarm: Off");
			} else if (mKey.equals(RemoteControlActivity.KEY_1)) {
				tv_rf_type_con.setText("Alarm: On");
				mEditName.setText("Alarm: On");
			} else if (mKey.equals(RemoteControlActivity.KEY_2)) {
				tv_rf_type_con.setText("SOS");
				mEditName.setText("SOS");
			} else if (mKey.equals(RemoteControlActivity.KEY_3)) {
				tv_rf_type_con.setText("ring");
				mEditName.setText("ring");
			}

		} else {
			hideAndVisView(true);
			if (mRFType.equals("door")) {//
				tv_rf_type_con.setText("Door sensor");
				mEditName.setText("Door sensor");
			} else if (mRFType.equals("infra")) {
				tv_rf_type_con.setText("Infrared");
				mEditName.setText("Infrared");
			} else if (mRFType.equals("fire")) {
				tv_rf_type_con.setText("smoke");
				mEditName.setText("smoke");
			} else if (mRFType.equals("gas")) {
				tv_rf_type_con.setText("Gas");
				mEditName.setText("Gas");
			} else if (mRFType.equals("beep")) {
				tv_rf_type_con.setText("Other types");
				mEditName.setText("Other types");
			}
		}
		if(mIsEdit){
			mEditName.setText(rfDevice.getName());
		}
		mEditName.setSelection(mEditName.getText().toString().length());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_add:
				String strName=mEditName.getText().toString().trim();
				if (TextUtils.isEmpty(strName)) {
					HiToast.showToast(SetUpAndAddRFActivity.this, "Name cannot be empty");
					return;
				}else {
					int length=strName.getBytes().length;
					if(length>64){
						HiToast.showToast(SetUpAndAddRFActivity.this, "The name you entered is too long???");
						return;
					}
				}
				if (mKey == null) {
					String preset = edit_preset_con.getText().toString().trim();
					if (TextUtils.isEmpty(preset) || Integer.parseInt(preset) > 8 || Integer.parseInt(preset) < 0) {
						HiToast.showToast(SetUpAndAddRFActivity.this, "The range of PTZ preset position is 0-8");
						edit_preset_con.setText("");
						return;
					}
				}
				if (mIsEdit) {
					int index = rfDevice.getU32Index();
					int enable = swibut_enable.isChecked() ? 1 : 0;
					String code = rfDevice.getCode();
					String type = rfDevice.getType();
					String name =mEditName.getText().toString();
					byte voiceLink = swibut_alarm.isChecked() ? (byte) 1 : (byte) 0;

					byte b = 0;
					if (edit_preset_con.getVisibility() == View.VISIBLE) {
						b = (byte) Integer.parseInt(edit_preset_con.getText().toString());
					}
					mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_SET, HiChipDefines.HI_P2P_IPCRF_INFO.parseContent(index, enable, code, type, name, voiceLink, b));
				} else {
					mRfName = mEditName.getText().toString().trim();
					mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET, null);
				}
				break;
		}
	}

	private void hideAndVisView(boolean vis) {
		tv_rf_alarm.setVisibility(vis ? View.VISIBLE : View.GONE);
		swibut_alarm.setVisibility(vis ? View.VISIBLE : View.GONE);
		tv_rf_pz_preset.setVisibility(vis ? View.VISIBLE : View.GONE);
		edit_preset_con.setVisibility(vis ? View.VISIBLE : View.GONE);

	}

	@Override
	public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
		if (arg0 != mMyCamera)  return;
		Message msg = Message.obtain();
		msg.arg1 = arg1;
		msg.arg2 = arg3;
		msg.what = HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL;
		Bundle bundle = new Bundle();
		bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
		msg.setData(bundle);
		mHandler.sendMessage(msg);

	}

	@Override
	public void receiveSessionState(HiCamera arg0, int arg1) {
		if(arg0!=mMyCamera) return;
		Message msg=Message.obtain();
		msg.what=HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
		msg.arg1=arg1;
		mHandler.sendMessage(msg);
	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
					if(msg.arg1==HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED){
						Intent intent=new Intent(SetUpAndAddRFActivity.this,MainActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(intent);
					}
					break;
				case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
					if (msg.arg2 == 0) {// success
						byte[] data = msg.getData().getByteArray(HiDataValue.EXTRAS_KEY_DATA);
						switch (msg.arg1) {
							case HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_GET:
								if (mIsEdit) {
									HI_P2P_IPCRF_INFO info = new HI_P2P_IPCRF_INFO(data);
									swibut_enable.setChecked(info.u32Enable == 1 ? true : false);
									swibut_alarm.setChecked(info.s8AlarmVoiceLink == (byte) 0 ? false : true);
									edit_preset_con.setText(info.s8PtzLink + " ");
								}
								break;
							case HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET:
								HiChipDefines.HI_P2P_IPCRF_ALL_INFO allRfInfo = new HiChipDefines.HI_P2P_IPCRF_ALL_INFO(data);
								for (int i = 0; i < allRfInfo.sRfInfo.length; i++) {
									HiChipDefines.HI_P2P_IPCRF_INFO info = allRfInfo.sRfInfo[i];
									list_rf_info.add(info);
								}
								if (allRfInfo.u32Flag == 1) {
									handAdd(allRfInfo);
								}
								break;
							case HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_SET:
								if (mIsEdit) {
									HiToast.showToast(SetUpAndAddRFActivity.this, "Successfully modified");
								}
								if(mKey==null){
									Intent intent=new Intent(SetUpAndAddRFActivity.this,RFActivity.class);
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
									startActivity(intent);
								}else {
									Intent intent = new Intent();
									if(strings.size()==3){
										intent.setClass(SetUpAndAddRFActivity.this, RFActivity.class);
									}else {
										intent.setClass(SetUpAndAddRFActivity.this, RemoteControlActivity.class);
									}
									intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
									intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mMyCamera.getUid());
									startActivity(intent);
								}
								break;
						}

					} else {// fail
						switch (msg.arg1) {
							case HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_SET:
								HiToast.showToast(SetUpAndAddRFActivity.this, "Failed to add (device returns failed callback)");
								break;
						}
					}
					break;
			}
		}
	};

	private List<String> strings=new ArrayList<>();
	protected void handAdd(HiChipDefines.HI_P2P_IPCRF_ALL_INFO allRfInfo) {
		for (int i = 0; i < list_rf_info.size(); i++) {
			HiChipDefines.HI_P2P_IPCRF_INFO info = list_rf_info.get(i);
			String code = new String(info.sRfCode).trim();
			if (!TextUtils.isEmpty(code) && code.length() > 10) {
				String str = new String(info.sType).trim();
				str = str.substring(0, 3);
				if ("key".equals(str)) {
					strings.add(str);
				}
			}
			if (new String(mCode).trim().equals(new String(info.sRfCode).trim())) {
				HiToast.showToast(SetUpAndAddRFActivity.this, "RF equipment has been added");
				return;
			}
		}
		byte b = 0;
		if (edit_preset_con.getVisibility() == View.VISIBLE) {
			b = (byte) Integer.parseInt(edit_preset_con.getText().toString());
		}
		if (mKey == null) {
			handIndexAndAdd(mRFType, b);
		} else {
			handIndexAndAdd(mKey, b);
		}
	}

	private void handIndexAndAdd(String type, byte ptzLink) {
		int index = -1;
		for (int j = 0; j < list_rf_info.size(); j++) {
			String strCode = new String(list_rf_info.get(j).sRfCode).trim();
			if (TextUtils.isEmpty(strCode) || strCode.length() < 10) {
				index = list_rf_info.get(j).u32Index;
				break;
			}
		}
		if (index == -1) {
			HiToast.showToast(SetUpAndAddRFActivity.this, "The upper limit of RF device addition has been reached, if you want to continue adding, please delete the previously added device???");
			return;
		} else {
			int inde = index;
			int enable = swibut_enable.isChecked() ? 1 : 0;
			String code = new String(mCode).trim();
			String typeu = type;
			String name = mRfName;
			byte voiceLink = swibut_alarm.isChecked() ? (byte) 1 : (byte) 0;
			byte ptzLinkf = ptzLink;
			mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_SET, HiChipDefines.HI_P2P_IPCRF_INFO.parseContent(inde, enable, code, typeu, name, voiceLink, ptzLinkf));
		}
	}
}






