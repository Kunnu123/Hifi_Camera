package com.hificamera.hichip.activity;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.hificamera.customview.MyRecordView;
import com.hificamera.R;
import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.callback.ICameraRPSAudioCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.hificamera.thecamhi.base.HiToast;
import com.hificamera.thecamhi.base.TitleView;
import com.hificamera.thecamhi.base.TitleView.NavigationBarButtonListener;
import com.hificamera.thecamhi.bean.CamHiDefines;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.MyCamera;
import com.hificamera.thecamhi.main.HiActivity;
import com.hificamera.thecamhi.utils.DialogUtils;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class AlarmVoiceTypeActivity extends HiActivity
		implements OnClickListener, ICameraRPSAudioCallback, ICameraIOSessionCallback {

	protected static final int REQUEST_RECORDER = 110;

	public static final int PERMISSIONS_REQUEST_FOR_AUDIO = 1;

	public static final int RECORD_TOO_SHORT = 102;

	private ExecutorService mExecutorService;
	private TitleView title;
	private RelativeLayout rl_alarm;
	private RelativeLayout rl_barking;
	private RelativeLayout rl_custom;
	private RelativeLayout rl_record_about;
	private int mVoiceType;
	private MyRecordView play, record;

	private ImageView iv_alarm, iv_barking, iv_custom;

	private String mFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/audio/";

	private long startTime;

	private long endTime;

	private boolean isPlaying;

	private MyCamera mCamera;

	private String filePath = mFilePath + "audioRecord.g711";;

	private File mAudioFile;

	private boolean isSendRecord;

	private boolean isEquipmentHasAudio;

	public static boolean isOnLine = true;

	private AudioManager audioManager;

	private boolean isRecording;
	
	private boolean isGetAudioFile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_voice_type);
		getIntentData();
		initView();
		setOnListeners();

	}

	private void getIntentData() {

		mVoiceType = getIntent().getIntExtra("mVoiceType", 0);
		String uid = getIntent().getStringExtra("uid");
		for (MyCamera camera : HiDataValue.CameraList) {
			if (uid.equals(camera.getUid())) {
				mCamera = camera;
				break;
			}
		}

		mCamera.registerRPSAudioListener(this);
		mCamera.registerIOSessionListener(this);
		// mCamera.registerPlayStateListener(this);

	}

	private void setOnListeners() {
		rl_alarm.setOnClickListener(this);
		rl_barking.setOnClickListener(this);
		rl_custom.setOnClickListener(this);

		play.setOnClickListener(new MyRecordView.MyOnClickListener() {

			@Override
			public void onClick() {

				final int time = (int) (((endTime - startTime) + 500) / 1000);
				Log.e("==play_status==", isPlaying + "---" + time);

				if (isPlaying) {
					stopPlay();
					return;
				}
				
				if (time <= 0)
					return;

				isPlaying = true;
				//record.setTouchAble(false);
				// play.setTouchAble(false);

				mExecutorService.submit(new Runnable() {
					@Override
					public void run() {
						startPlay();
					}
				});

				play.startPlay(time, 20);

			}
		});

		record.setOnClickListener(new MyRecordView.MyOnClickListener() {
			
			@Override
			public void onClick() {
				if (isPlaying) {
					stopPlay();
				}
			}
		});

		record.setOnLongClickListener(new MyRecordView.OnLongClickListener() {

			@Override
			public void onRecordFinishedListener() {
				if (isRecording) {
					endTime = System.currentTimeMillis();
					mCamera.stopRecordingAudio();
					isRecording = false;
					if (new File(filePath).exists() && startTime != 0 && endTime != 0) {
						play.setVisibility(View.VISIBLE);

					}
				}
			}

			@Override
			public void onNoMinRecord(int currentTime) {
				if (isRecording) {
					mCamera.stopRecordingAudio();
					isRecording = false;
					play.setVisibility(View.GONE);				
					deleteFile(mAudioFile);
					
					Message message = new Message();
					message.what = RECORD_TOO_SHORT;
					mHandler.sendMessage(message);
				}
			}

			@Override
			public void onLongClick() {
				Log.e("==record_status==", record.getTouchAble() + "");

				if (isPlaying) {
					stopPlay();
				}
				if (Build.VERSION.SDK_INT > 22) {
					permissionForM();
				} else {
					mExecutorService.submit(new Runnable() {
						@Override
						public void run() {
							if (record.getTouchAble() && !isPlaying) {
								startRecord();
							}

						}
					});
				}

			}
		});
	}

	protected void startPlay() {
		if (filePath == null) {
			return;
		}
		Log.e("==filePath==", filePath);

		mCamera.startPlayRecordAudio(filePath);
	}

	private void permissionForM() {
		if (ContextCompat.checkSelfPermission(this,
				Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
				|| ContextCompat.checkSelfPermission(this,
						Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

			record.resetAnimation();
			record.cancelProgressAni();

			ActivityCompat.requestPermissions(this,
					new String[] { Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE },
					PERMISSIONS_REQUEST_FOR_AUDIO);
		} else {
			startRecord();
		}

	}

	protected void startRecord() {
		isRecording = true;
		mAudioFile = null;
		mAudioFile = new File(filePath);

		mAudioFile.getParentFile().mkdirs();

		try {
			deleteFile(mAudioFile);
			mAudioFile.createNewFile();

			mCamera.startRecordingAudio(filePath);
			startTime = System.currentTimeMillis();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {

			// case HiDataValue.HANDLE_MESSAGE_SESSION_STATE:
			// switch (msg.arg1) {
			// case HiCamera.CAMERA_CONNECTION_STATE_DISCONNECTED:
			// dismissjuHuaDialog();
			// HiToast.showToast(AlarmVoiceTypeActivity.this,
			// getString(R.string.disconnect));
			// startActivity(new Intent(AlarmVoiceTypeActivity.this, MainActivity.class));
			// finish();
			// break;
			// }
			// break;

			case RPSAUDIO_STATE_RECORDERROR:
				HiToast.showToast(AlarmVoiceTypeActivity.this, getString(R.string.failed_record_audio));

				play.setVisibility(View.GONE);
				mCamera.stopRecordingAudio();
				isRecording = false;
				break;
			case RECORD_TOO_SHORT:
				HiToast.showToast(AlarmVoiceTypeActivity.this, getString(R.string.mini_time_record_audio));
				break;

			case RPSAUDIO_STATE_PLAYERROR:
				HiToast.showToast(AlarmVoiceTypeActivity.this, getString(R.string.play_audio_fail));
				mCamera.stopPlayRecordAudio();
				isPlaying = false;
				mHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						record.setTouchAble(true);
						play.setTouchAble(true);

					}
				}, 500);
				break;

			case RPSAUDIO_STATE_SENDERROR:
				dismissjuHuaDialog();
				HiToast.showToast(AlarmVoiceTypeActivity.this, getString(R.string.send_audio_fail));
				break;

			case RPSAUDIO_STATE_END:
				if (isPlaying) {
					isPlaying = false;
					mCamera.stopPlayRecordAudio();
					mHandler.postDelayed(new Runnable() {

						@Override
						public void run() {
							record.setTouchAble(true);
							play.setTouchAble(true);

						}
					}, 500);
				}
				if (isSendRecord) {
					// dismissjuHuaDialog();
					// isSendRecord = false;
					// Intent intent = new Intent();
					// intent.putExtra("mVoiceType", 2);
					// setResult(RESULT_OK, intent);
					// deleteFile(mAudioFile);
					// finish();
				}

				break;

			case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
				if (msg.arg2 == 0) {
					Bundle bundle = msg.getData();
					byte[] data = bundle.getByteArray(HiDataValue.EXTRAS_KEY_DATA);
					switch (msg.arg1) {
					case HiChipDefines.HI_P2P_TRANSFER_AUDIOFILE:
						dismissjuHuaDialog();
						HiToast.showToast(AlarmVoiceTypeActivity.this, getString(R.string.send_audio_success));
						isSendRecord = false;
						Intent intent = new Intent();
						intent.putExtra("mVoiceType", 2);
						setResult(RESULT_OK, intent);
						deleteFile(mAudioFile);
						finish();
						break;
					case HiChipDefines.HI_P2P_GET_TRANSFER_AUDIOFILE:
						isGetAudioFile = false;
						dismissjuHuaDialog();
						isEquipmentHasAudio = true;
						File file = new File(filePath);
						if (!file.exists()) {
							Intent intent1 = new Intent();
							intent1.putExtra("mVoiceType", mVoiceType);
							setResult(RESULT_OK, intent1);
							deleteFile(mAudioFile);
							finish();
						} else {

							new DialogUtils(AlarmVoiceTypeActivity.this).title("")
									.message(getString(R.string.cover_audio)).cancelText(getString(R.string.cancel))
									.sureText(getString(R.string.sure))
									.setCancelOnClickListener(new OnClickListener() {
										@Override
										public void onClick(View view) {

										}
									}).setSureOnClickListener(new OnClickListener() {

										@Override
										public void onClick(View v) {
											showjuHuaDialog();
											isSendRecord = true;
											mCamera.startSendRecordAudio(filePath);
										}
									}).build().show();

						}
						break;

					}
				} else {
					switch (msg.arg1) {
					case HiChipDefines.HI_P2P_TRANSFER_AUDIOFILE:
						dismissjuHuaDialog();
						isSendRecord = false;
						HiToast.showToast(AlarmVoiceTypeActivity.this, getString(R.string.send_audio_fail));
						break;
					case HiChipDefines.HI_P2P_GET_TRANSFER_AUDIOFILE:
						isEquipmentHasAudio = false;
						isGetAudioFile = false;

						File file = new File(filePath);
						if (!file.exists()) {
							dismissjuHuaDialog();
							HiToast.showToast(AlarmVoiceTypeActivity.this, getString(R.string.no_record_audio));
						} else {
							isSendRecord = true;
							mCamera.startSendRecordAudio(filePath);
						}

						break;
					}
				}
			}
		}
	};

	private void initView() {
		audioManager = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
		mExecutorService = Executors.newSingleThreadExecutor();

		title = (TitleView) findViewById(R.id.title);
		title.setButton(TitleView.NAVIGATION_BUTTON_LEFT);
		title.setTitle(getString(R.string.alarm_sound_type_));
		title.setRightText(R.string.application);

		title.getRightText().setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				if (mVoiceType == 1 || mVoiceType == 0) {
					Intent intent = new Intent();
					intent.putExtra("mVoiceType", mVoiceType);
					setResult(RESULT_OK, intent);
					finish();
				} else {

					if (isRecording||isGetAudioFile) {
						return ;
					}
					
					showjuHuaDialog();

					if (isPlaying) {
						stopPlay();
					}

					mHandler.postDelayed(new Runnable() {

					

						@Override
						public void run() {
							isGetAudioFile = true;
							mCamera.sendIOCtrl(HiChipDefines.HI_P2P_GET_TRANSFER_AUDIOFILE, null);

						}
					}, 500);

				}

			}
		});

		title.setNavigationBarButtonListener(new NavigationBarButtonListener() {
			@Override
			public void OnNavigationButtonClick(int which) {
				switch (which) {
				case TitleView.NAVIGATION_BUTTON_LEFT:
					File file = new File(filePath);
					if (file.exists()) {
						new DialogUtils(AlarmVoiceTypeActivity.this).title("")
								.message(getString(R.string.give_up_record)).cancelText(getString(R.string.sure))
								.sureText(getString(R.string.cancel))
								.setCancelOnClickListener(new OnClickListener() {
									@Override
									public void onClick(View view) {
										finish();
									}
								}).setSureOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {

									}
								}).build().show();
					} else {
						finish();
					}

					break;
				}

			}
		});



		rl_record_about = (RelativeLayout) findViewById(R.id.rl_record_about);

		rl_barking = (RelativeLayout) findViewById(R.id.rl_barking);
		rl_alarm = (RelativeLayout) findViewById(R.id.rl_alarm);
		rl_custom = (RelativeLayout) findViewById(R.id.rl_custom);

		play = (MyRecordView) findViewById(R.id.play);
		record = (MyRecordView) findViewById(R.id.record);

		iv_alarm = (ImageView) findViewById(R.id.iv_alarm);
		iv_barking = (ImageView) findViewById(R.id.iv_barking);
		iv_custom = (ImageView) findViewById(R.id.iv_custom);

		if (mVoiceType == 0) {
			iv_alarm.setVisibility(View.VISIBLE);
			iv_barking.setVisibility(View.GONE);
			iv_custom.setVisibility(View.GONE);
		} else if (mVoiceType == 1) {
			iv_alarm.setVisibility(View.GONE);
			iv_custom.setVisibility(View.GONE);
			iv_barking.setVisibility(View.VISIBLE);
		} else {
			iv_alarm.setVisibility(View.GONE);
			iv_custom.setVisibility(View.VISIBLE);
			iv_barking.setVisibility(View.GONE);
			record.setVisibility(View.VISIBLE);

		}

		if (mCamera.getCommandFunction(CamHiDefines.HI_P2P_CUSTOM_ALARM)) {
			rl_custom.setVisibility(View.VISIBLE);
		} else {
			rl_custom.setVisibility(View.GONE);
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			File file = new File(filePath);
			if (file.exists() && play.getVisibility() == View.VISIBLE) {
				new DialogUtils(AlarmVoiceTypeActivity.this).title("").message(getString(R.string.give_up_record))
						.cancelText(getString(R.string.sure)).sureText(getString(R.string.cancel))
						.setCancelOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View view) {
								finish();
							}
						}).setSureOnClickListener(new OnClickListener() {

							@Override
							public void onClick(View v) {

							}
						}).build().show();
			} else {
				finish();
			}
			break;
		case KeyEvent.KEYCODE_VOLUME_UP:
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_RAISE,
					AudioManager.FX_FOCUS_NAVIGATION_UP);
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC, AudioManager.ADJUST_LOWER,
					AudioManager.FX_FOCUS_NAVIGATION_UP);
			return true;
		}

		return true;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.rl_alarm:
			if (mVoiceType != 0 && !isRecording) {
				mVoiceType = 0;
				iv_barking.setVisibility(View.GONE);
				iv_custom.setVisibility(View.GONE);
				iv_alarm.setVisibility(View.VISIBLE);
				record.setVisibility(View.GONE);
				play.setVisibility(View.GONE);

				stopPlay();
			}
			break;
		case R.id.rl_barking:
			if (mVoiceType != 1 && !isRecording) {
				mVoiceType = 1;
				iv_barking.setVisibility(View.VISIBLE);
				iv_alarm.setVisibility(View.GONE);
				iv_custom.setVisibility(View.GONE);
				record.setVisibility(View.GONE);
				play.setVisibility(View.GONE);

				stopPlay();
			}
			break;

		case R.id.rl_custom:
			if (mVoiceType != 2 && !isRecording) {
				mVoiceType = 2;
				iv_barking.setVisibility(View.GONE);
				iv_alarm.setVisibility(View.GONE);
				iv_custom.setVisibility(View.VISIBLE);
				record.setVisibility(View.VISIBLE);

				if (new File(filePath).exists() && startTime != 0 && endTime != 0) {
					play.setVisibility(View.VISIBLE);
				}

				playAni(rl_record_about);
			}
			break;
		}

	}

	private void stopPlay() {
		mCamera.stopPlayRecordAudio();
		play.cancelPlayProgressAni();
		isPlaying = false;
		mHandler.postDelayed(new Runnable() {

			@Override
			public void run() {
				record.setTouchAble(true);
				play.setTouchAble(true);

			}
		}, 500);

	}

	@Override
	protected void onPause() {
		super.onPause();

		stopPlay();

		if (isSendRecord) {
			mCamera.stopSendRecordAudio();
			isSendRecord = false;
		}
		if (isRecording) {
			endTime = System.currentTimeMillis();
			mCamera.stopRecordingAudio();
			isRecording = false;
			// record.resetAnimation();
			record.cancelProgressAni();
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (null != mExecutorService) {
			mExecutorService.shutdownNow();
		}
		if (mCamera != null) {
			mCamera.unregisterRPSAudioListener(this);
			mCamera.unregisterIOSessionListener(this);
			// mCamera.unregisterPlayStateListener(this);
		}

		deleteFile(mAudioFile);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
			@NonNull int[] grantResults) {

		if (requestCode == PERMISSIONS_REQUEST_FOR_AUDIO) {
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

			}
			return;
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	@Override
	public void callbackRPSAudioState(HiCamera arg0, int arg1, int arg2, int arg3, String arg4) {
		Message msg = mHandler.obtainMessage();
		msg.what = arg3;
		msg.obj = arg0;
		msg.arg1 = arg1;
		msg.arg2 = arg2;
		mHandler.sendMessage(msg);

	}

	private void deleteFile(File file) {
		if (file == null) {
			file = new File(filePath);
		}
		if (file.isDirectory()) {
			File[] files = file.listFiles();
			for (int i = 0; i < files.length; i++) {
				File f = files[i];
				deleteFile(f);
			}

		} else if (file.exists()) {
			file.delete();
		}
	}

	public void playAni(View view) {
		AnimatorSet animatorSet = new AnimatorSet();
		ObjectAnimator animator1 = ObjectAnimator.ofFloat(view, "Alpha", 0f, 1f);
		animatorSet.playTogether(animator1);
		animatorSet.setDuration(500);
		animatorSet.start();
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
		Message msg = mHandler.obtainMessage();
		msg.what = HiDataValue.HANDLE_MESSAGE_SESSION_STATE;
		msg.arg1 = arg1;
		msg.obj = arg0;
		mHandler.sendMessage(msg);
	}

	// @Override
	// public void callbackPlayUTC(HiCamera arg0, int arg1) {
	//
	//
	// }
	//
	// @Override
	// public void callbackState(HiCamera camera, int arg1, int arg2, int arg3) {
	//
	// if (mCamera != camera)
	// return;
	// Message msg = mHandler.obtainMessage();
	// msg.what = arg1;
	// msg.arg1 = arg2;
	// msg.arg2 = arg3;
	// mHandler.sendMessage(msg);
	//
	// }

}
