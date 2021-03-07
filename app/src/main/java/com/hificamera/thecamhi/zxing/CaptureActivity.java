package com.hificamera.thecamhi.zxing;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.hificamera.R;

import com.hichip.callback.ICameraIOSessionCallback;
import com.hichip.content.HiChipDefines;
import com.hichip.control.HiCamera;
import com.hificamera.customview.dialog.NiftyDialogBuilder;
import com.hificamera.hichip.activity.RF.SetUpAndAddRFActivity;
import com.hichip.sdk.HiChipSDK;
import com.hificamera.thecamhi.base.HiToast;
import com.hificamera.thecamhi.base.HiTools;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.MyCamera;
import com.hificamera.thecamhi.bean.RFDevice;
import com.hificamera.thecamhi.main.MainActivity;
import com.hificamera.thecamhi.zxing.utils.UriUtils;

import static com.hificamera.thecamhi.utils.UidConfigUtil.blackUidMap;
public class CaptureActivity extends Activity implements Callback, ICameraIOSessionCallback{

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private Button cancelScanButton;

	int ifOpenLight = 0;

	private ArrayList<MyCamera> mAnalyCameraList = new ArrayList<>();
	private int category = 0;  //category==3
	private ArrayList<RFDevice> list_rf_info = new ArrayList<>();
	private ArrayList<RFDevice> list_rf_device_key = new ArrayList<>();
	private List<HiChipDefines.HI_P2P_IPCRF_INFO> list_IPCRF = new ArrayList<HiChipDefines.HI_P2P_IPCRF_INFO>();

	private String mUid;
	private MyCamera mMyCamera;
	private String[] code_arr;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		getIntentData();
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
		cancelScanButton = (Button) this.findViewById(R.id.btn_cancel_scan);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
	}

	@SuppressWarnings("unchecked")
	private void getIntentData() {
		category = getIntent().getIntExtra("category", 0);
		list_rf_info = (ArrayList<RFDevice>) getIntent().getSerializableExtra("list_rf_info");
		list_rf_device_key = (ArrayList<RFDevice>) getIntent().getSerializableExtra("list_rf_device_key");
		mUid = getIntent().getStringExtra(HiDataValue.EXTRAS_KEY_UID);
		for(MyCamera camera:HiDataValue.CameraList){
			if(!TextUtils.isEmpty(mUid)){
				if(mUid.equalsIgnoreCase(camera.getUid())){
					this.mMyCamera=camera;
					break;
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = null;

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;

		// quit the scan view
		cancelScanButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				CaptureActivity.this.finish();
			}
		});
		if (mMyCamera != null) {
			mMyCamera.registerIOSessionListener(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
		if (mMyCamera != null) {
			mMyCamera.unregisterIOSessionListener(this);
		}
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}


	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();Log.e("ZXing","zixng result="+result);
		String resultString = result.getText();
		if (TextUtils.isEmpty(resultString)) {
			HiToast.showToast(CaptureActivity.this, getString(R.string.toast_scan_fail));

		} else {
			if (!TextUtils.isEmpty(resultString) && resultString.length() > 8) {
				String sub = resultString.substring(0, 8);
				if (sub.equalsIgnoreCase(getString(R.string.app_name) + "_AC")) {
					handData(resultString);
				} else if (category == 1 && resultString.substring(0, 1).equalsIgnoreCase("0")) {
					handRFData(resultString);
				} else {
					Intent resultIntent = new Intent();
					Bundle bundle = new Bundle();
					bundle.putString(HiDataValue.EXTRAS_KEY_UID, resultString);
					resultIntent.putExtras(bundle);
					CaptureActivity.this.setResult(RESULT_OK, resultIntent);
					CaptureActivity.this.finish();
				}
			}
		}
	}

	private void handRFData(final String resultString) {
		// resultString: 02bbba2a000000
		// resultString: 08f47c82000000-07f47c84000000-09f47c81000000-0Af47c88000000
		code_arr = resultString.split("-");
		if (code_arr.length == 1) {
			handLen_1(resultString);
		}
		if (code_arr.length == 4) {
			if(list_rf_device_key!=null&&list_rf_device_key.size()>0){
				HiToast.showToast(this, "Please delete the added remote control and scan the code to add！");
				CaptureActivity.this.finish();
				return;
			}
			StringBuffer sb = new StringBuffer();
			for (String str : code_arr) {
				sb.append(handCate(str) + "\n");
			}

			final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(this);
			dialog.withMessageLayoutWrap();
			dialog.withTitle("Suppose it succeeded").withMessage("Sensor detected:\n\n" + sb.toString() + "\nConfirm whether to add?\n");
			dialog.withButton1Text("cancel").withButton2Text("Confirm add");
			dialog.setButton1Click(new OnClickListener() {
				@Override
				public void onClick(View v) {
					dialog.dismiss();
					CaptureActivity.this.finish();
				}
			});
			dialog.setButton2Click(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET, null);
				}
			});
			dialog.isCancelable(false);
			dialog.show();

		}


	}




	private void handLen_1(final String resultString) {
		final String resultCode = resultString.substring(2);
		if (list_rf_info != null && list_rf_info.size() > 0) {
			for (RFDevice device : list_rf_info) {
				if (resultCode.equalsIgnoreCase(device.code)) {
					HiToast.showToast(this, "The device has already been added！");
					CaptureActivity.this.finish();
					return;
				}
			}
		}
		String cate = handCate(resultString);
		final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(this);
		dialog.withTitle("Suppose it succeeded").withMessage("Sensor detected:  " + cate + "\n" + "Confirm whether to add?");
		dialog.withButton1Text("cancel").withButton2Text("Confirm add");
		dialog.setButton1Click(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dialog.dismiss();
				CaptureActivity.this.finish();
				return;
			}
		});
		dialog.setButton2Click(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(CaptureActivity.this, SetUpAndAddRFActivity.class);
				intent.putExtra(HiDataValue.EXTRAS_RF_TYPE, handCateType(resultString));
				intent.putExtra(HiDataValue.EXTRAS_KEY_DATA, resultCode.getBytes());
				intent.putExtra(HiDataValue.EXTRAS_KEY_UID, mUid);
				startActivity(intent);
				return;
			}
		});
		dialog.setCancelable(false);
		dialog.show();
	}

	private String handCateType(String resultString) {
		String sub = resultString.substring(0, 2);
		switch (sub) {
			case "01":
				sub = "infra";
				break;
			case "02":
				sub = "door";
				break;
			case "03":
				sub = "fire";
				break;
			case "04":
				sub = "gas";
				break;
			case "05":
				sub = "beep";
				break;
			case "06":
				sub = "beep";
				break;
			case "07":
				sub = "key1";
				break;
			case "08":
				sub = "key0";
				break;
			case "09":
				sub = "key2";
				break;
			case "0A":
				sub = "key3";
				break;
		}
		return sub;
	}

	private String handCate(String resultString) {
		String sub = resultString.substring(0, 2);
		switch (sub) {
			case "01":
				sub = "Infrared";
				break;
			case "02":
				sub = "Door sensor";
				break;
			case "03":
				sub = "smoke";
				break;
			case "04":
				sub = "Gas";
				break;
			case "05":
				sub = "doorbell";
				break;
			case "06":
				sub = "socket";
				break;
			case "07":
				sub = "RF alarm: On";
				break;
			case "08":
				sub = "RF alarm: off";
				break;
			case "09":
				sub = "SOS";
				break;
			case "0A":
				sub = "Alarm bell";
				break;
		}
		return sub;
	}

	private void handData(String resultString) {
		String string = resultString.substring(8, resultString.length());
		byte[] buff = new byte[resultString.getBytes().length];
		byte[] datas = string.getBytes();
		System.arraycopy(datas, 0, buff, 0, datas.length);
		HiChipSDK.Aes_Decrypt(buff, datas.length);
		String decryptStr = new String(buff).trim();
		analyData(decryptStr);
	}

	private StringBuffer sbAddCamerUid = new StringBuffer();

	private void analyData(String string) {
		try {
			JSONArray jsonArray = new JSONArray(string);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				String uid = jsonObject.getString("U").substring(0, jsonObject.getString("U").length() - 2);
				String username = jsonObject.getString("A").substring(0, jsonObject.getString("A").length() - 2);
				String password = jsonObject.getString("P").substring(0, jsonObject.getString("P").length() - 2);
				MyCamera camera = new MyCamera(CaptureActivity.this, getString(R.string.title_camera_fragment), uid, username, password);
				if(camera.isErrorUID(uid))
				camera.setErrorUID(blackUidMap.containsKey(uid));
				mAnalyCameraList.add(camera);
			}
			if (mAnalyCameraList != null && mAnalyCameraList.size() > 0) {
				for (MyCamera camera : HiDataValue.CameraList) {
					for (int i = 0; i < mAnalyCameraList.size(); i++) {
						if (camera.getUid().equalsIgnoreCase(mAnalyCameraList.get(i).getUid())) {
							mAnalyCameraList.remove(i);
						}
					}
				}
				if (mAnalyCameraList.size() < 1) {
					HiToast.showToast(CaptureActivity.this, getString(R.string.toast_device_added));
					CaptureActivity.this.finish();
				} else {
					for (int i = 0; i < mAnalyCameraList.size(); i++) {
						MyCamera camera = mAnalyCameraList.get(i);
						if (i < mAnalyCameraList.size() - 1) {
							sbAddCamerUid.append(camera.getUid() + "\n");
						} else {
							sbAddCamerUid.append(camera.getUid());
						}
					}
					final NiftyDialogBuilder dialog = NiftyDialogBuilder.getInstance(CaptureActivity.this);
					if (mAnalyCameraList.size() > 3) {
						dialog.withMessageLayoutWrap();
					}
					dialog.withTitle(getString(R.string.add_camera)).withMessage(sbAddCamerUid.toString()).withButton1Text(getString(R.string.cancel)).withButton2Text(getString(R.string.toast_confirm_add));
					dialog.setButton1Click(new OnClickListener() {
						@Override
						public void onClick(View v) {
							dialog.dismiss();
							CaptureActivity.this.finish();
						}
					});
					dialog.setButton2Click(new OnClickListener() {
						@Override
						public void onClick(View v) {
							for (MyCamera camera : mAnalyCameraList) {
								camera.saveInDatabase(CaptureActivity.this);
								camera.saveInCameraList();
							}
							dialog.dismiss();
							Intent intent = new Intent();
							intent.setAction(HiDataValue.ACTION_CAMERA_INIT_END);
							sendBroadcast(intent);

							intent = new Intent(CaptureActivity.this, MainActivity.class);
							intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
							startActivity(intent);
						}
					});
					dialog.isCancelable(false);
					dialog.show();
				}
			}
		} catch (JSONException e) {
			HiToast.showToast(CaptureActivity.this, getString(R.string.toast_scan_fail));
			e.printStackTrace();
		}

	}

	@SuppressLint("InlinedApi")
	public void pickPictureFromAblum(View v) {
		Intent innerIntent = new Intent(Intent.ACTION_GET_CONTENT);
		innerIntent.setType("image/*");
		startActivityForResult(innerIntent, 0X22);
	}

	String photo_path;
	ProgressDialog mProgress;
	Bitmap scanBitmap;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case 0X22:
					handleAlbumPic(data);
					break;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void handleAlbumPic(Intent data) {
		photo_path = UriUtils.getRealPathFromUri(CaptureActivity.this, data.getData());
		mProgress = new ProgressDialog(CaptureActivity.this);
		mProgress.setMessage(getString(R.string.toast_scanning));
		mProgress.setCancelable(false);
		mProgress.show();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				mProgress.dismiss();
				Result result = scanningImage(photo_path);
				if (result != null) {
					mProgress.dismiss();
					String resultString = result.getText();
					if (resultString.equals("")) {
						HiToast.showToast(CaptureActivity.this, getString(R.string.toast_scan_fail));
						CaptureActivity.this.finish();
					} else if(category==3) {
//						handData(resultString);
						if(HiTools.checkIsUid(resultString)){
							Intent resultIntent=new Intent();
							Bundle bundle=new Bundle();
							bundle.putString(HiDataValue.EXTRAS_KEY_UID, resultString);
							resultIntent.putExtras(bundle);
							CaptureActivity.this.setResult(RESULT_OK,resultIntent);
							CaptureActivity.this.finish();
						}else{
							HiToast.showToast(CaptureActivity.this, "The data format is wrong");
						}
					}else {
						handData(resultString);
					}
				} else {
					mProgress.dismiss();
					HiToast.showToast(CaptureActivity.this,getString(R.string.toast_scan_fail) );
					CaptureActivity.this.finish();
				}
			}
		});
	}


	protected Result scanningImage(String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		Hashtable<DecodeHintType, String> hints = new Hashtable<DecodeHintType, String>();
		hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		scanBitmap = BitmapFactory.decodeFile(path, options);
		options.inJustDecodeBounds = false;
		int sampleSize = (int) (options.outHeight / (float) 200);
		if (sampleSize <= 0)
			sampleSize = 1;
		options.inSampleSize = sampleSize;
		scanBitmap = BitmapFactory.decodeFile(path, options);
		RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap);
		BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
		QRCodeReader reader = new QRCodeReader();
		try {
			return reader.decode(bitmap1, hints);
		} catch (NotFoundException e) {
			Log.i("tedu","--NotFoundException--");
			e.printStackTrace();
		} catch (ChecksumException e) {
			Log.i("tedu","--ChecksumException--");
			e.printStackTrace();
		} catch (FormatException e) {
			Log.i("tedu","--FormatException--");
			e.printStackTrace();
		}
		return null;
	}




	private String recode(String str) {
		String formart = "";
		try {
			boolean ISO = Charset.forName("ISO-8859-1").newEncoder().canEncode(str);
			if (ISO) {
				formart = new String(str.getBytes("ISO-8859-1"), "GB2312");
				Log.i("1234      ISO8859-1", formart);
			} else {
				formart = str;
				Log.i("1234      stringExtra", str);
			}
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return formart;
	}

	public void IfOpenLight(View v) {
		ifOpenLight++;

		switch (ifOpenLight % 2) {
			case 0:
				CameraManager.get().closeLight();
				break;

			case 1:
				CameraManager.get().openLight();
				break;
			default:
				break;
		}
	}

	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats, characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	@Override
	public void receiveIOCtrlData(HiCamera arg0, int arg1, byte[] arg2, int arg3) {
		if(mMyCamera!=arg0) return;
		Message msg=mHandler.obtainMessage();
		msg.what=HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL;
		msg.arg1=arg1;
		msg.arg2=arg3;
		Bundle bundle=new Bundle();
		bundle.putByteArray(HiDataValue.EXTRAS_KEY_DATA, arg2);
		msg.setData(bundle);
		mHandler.sendMessage(msg);

	}

	@Override
	public void receiveSessionState(HiCamera arg0, int arg1) {

	}

	private  int num=0;
	private int[] indexs=new int[4];
	private List<Integer> list_index=new ArrayList<>();

	private Handler mHandler=new Handler(){
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case HiDataValue.HANDLE_MESSAGE_RECEIVE_IOCTRL:
					if(msg.arg2==0){
						byte[] data=msg.getData().getByteArray(HiDataValue.EXTRAS_KEY_DATA);
						switch (msg.arg1) {
							case HiChipDefines. HI_P2P_IPCRF_SINGLE_INFO_SET:
								num++;
								if(num==code_arr.length){
									HiToast.showToast(CaptureActivity.this, "Added successfully！");
									CaptureActivity.this.finish();
								}
								break;
							case HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET:
								HiChipDefines.HI_P2P_IPCRF_ALL_INFO allRfInfo = new HiChipDefines.HI_P2P_IPCRF_ALL_INFO(data);
								for (int i = 0; i < allRfInfo.sRfInfo.length; i++) {
									HiChipDefines.HI_P2P_IPCRF_INFO info = allRfInfo.sRfInfo[i];
									list_IPCRF.add(info);
								}
								if (allRfInfo.u32Flag == 1) {
									if(code_arr.length>1){
										for (int j = 0; j < list_IPCRF.size(); j++) {
											String strCode = new String(list_IPCRF.get(j).sRfCode).trim();
											if (TextUtils.isEmpty(strCode) || strCode.length() < 10) {
												if(list_index.size()<5){
													list_index.add(list_IPCRF.get(j).u32Index);
												}else {
													break;
												}
											}
										}
										if(list_index.size()<4){
											HiToast.showToast(CaptureActivity.this, "The upper limit of RF device addition has been reached, if you want to continue adding, please delete the previously added device！");
											return;
										}
										for(int i=0;i<code_arr.length;i++){
											String str=code_arr[i];
											String code=str.substring(2);
											handIndexAndAdd(list_index.get(i),handCateType(str), (byte)0, handCate(str), code);
										}
									}
								}
								break;
						}
					}else {
						switch (msg.arg1) {
							case HiChipDefines. HI_P2P_IPCRF_SINGLE_INFO_SET:
							case HiChipDefines.HI_P2P_IPCRF_ALL_INFO_GET:
								HiToast.showToast(CaptureActivity.this, getString(R.string.toast_scan_fail));
								CaptureActivity.this.finish();
								break;

						}

					}

					break;

			}

		};
	};

	private void handIndexAndAdd(int index,String type, byte ptzLink,String mRfName,String mCode) {
		int inde = index;
		int enable = 1;
		String code = mCode;
		String typeu = type;
		String name = mRfName;
		byte voiceLink = (byte)1;
		byte ptzLinkf = ptzLink;
		mMyCamera.sendIOCtrl(HiChipDefines.HI_P2P_IPCRF_SINGLE_INFO_SET, HiChipDefines.HI_P2P_IPCRF_INFO.parseContent(inde, enable, code, typeu, name, voiceLink, ptzLinkf));
	}


}




