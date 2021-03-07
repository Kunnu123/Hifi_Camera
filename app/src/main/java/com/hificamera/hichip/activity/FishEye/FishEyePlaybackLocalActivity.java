package com.hificamera.hichip.activity.FishEye;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.hificamera.R;
import com.hichip.callback.PlayLocalFileCallback;
import com.hichip.control.HiGLMonitor;
import com.hichip.sdk.PlayLocal;
import com.hificamera.thecamhi.activity.VideoLocalActivity;
import com.hificamera.thecamhi.base.HiToast;
import com.hificamera.thecamhi.base.HiTools;
import com.hificamera.thecamhi.base.MyPlaybackGLMonitor;
import com.hificamera.thecamhi.bean.HiDataValue;
import com.hificamera.thecamhi.bean.MyCamera;
import com.hificamera.thecamhi.main.HiActivity;
import com.hificamera.thecamhi.utils.SharePreUtils;

import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class FishEyePlaybackLocalActivity extends HiActivity implements PlayLocalFileCallback, OnClickListener, OnTouchListener, OnCheckedChangeListener {
	private String filePath;
	private MyPlaybackGLMonitor mMonitor;
	private PlayLocal playLocal;
	private ImageView mIvPausePlay, mIvFastforward, btn_return;
	private TextView mTvSpeed, tv_install;
	private int mSpeed = 0;
	private boolean mIsEnd = false;
	private LinearLayout mLlCurrPro;
	private TextView mTvCurrPro, mTvDuraTime, play_view_model;
	public LinearLayout mLlPlay, ll_top;
	private MyCamera mMyCamera;
	private int screen_width;
	private int screen_height;
	private String title;
	private RelativeLayout rl_view_model;
	private RadioGroup rg_view_model;
	public RadioButton rbtn_wall_overallview, rbtn_circle;
	private String strat_time;
	private TextView tv_timezone;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_fisheye_playback_local);
		getIntentData();
		initViewAndData();
		setListeners();
	}

	private void setListeners() {
		mMonitor.setOnTouchListener(this);
		mIvPausePlay.setOnClickListener(this);
		mIvFastforward.setOnClickListener(this);
		play_view_model.setOnClickListener(this);
		rl_view_model.setOnClickListener(this);
		rg_view_model.setOnCheckedChangeListener(this);
		btn_return.setOnClickListener(this);
		mSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				int count = seekBar.getProgress();
				final int pre = count / 1000;
				if (playLocal != null) {
					playLocal.PlayLocal_Speed(0, 0);
					if (mIsEnd) {
						startVideoPath();
						playLocal.setLiveShowMonitor(mMonitor);
					}else {
						mIsPalying = true;
						mIvPausePlay.setSelected(false);
						playLocal.PlayLocal_Resume();
					}
					playLocal.PlayLocal_Seek(pre, false);
				}
				mIsDrag = false;
				mTvSpeed.setText(" ");
				mSpeed = 0;
				mLlCurrPro.setVisibility(View.GONE);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				mIsDrag = true;
			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				if (mIsDrag) {
					if (mProgressTime > 0) {
						int count = seekBar.getProgress();
						int pre = count / 1000;
						if (playLocal != null) {
							playLocal.PlayLocal_Speed(0, 0);
							playLocal.PlayLocal_Seek(pre, true);
						}
					}
					mLlCurrPro.setVisibility(View.VISIBLE);
					mTvCurrPro.setText(sdf.format(new Date(progress)));
					mTvDuraTime.setText(sdf.format(new Date(seekBar.getMax())));
				} else {
					mLlCurrPro.setVisibility(View.GONE);
				}
			}
		});

	}

	private void initViewAndData() {
		mMonitor = (MyPlaybackGLMonitor) findViewById(R.id.monitor_playback_local);
		mMonitor.setCamera(mMyCamera);
        
		float cirx=SharePreUtils.getFloat("chche", this, mMyCamera.getUid() + "xcircle");
		float ciry=SharePreUtils.getFloat("chche", this, mMyCamera.getUid() + "ycircle");
		float cirr=SharePreUtils.getFloat("chche", this, mMyCamera.getUid() + "rcircle");
		mMonitor.SetCirInfo(cirx, ciry, cirr);
		mMonitor.SetViewType(mMonitor.TOP_ALL_VIEW);
		mMonitor.setFlensType(mMyCamera.getFishModType());
		mMonitor.SetScreenSize(getWindowManager().getDefaultDisplay().getWidth(), getWindowManager().getDefaultDisplay().getHeight());
		rbtn_wall_overallview = (RadioButton) findViewById(R.id.rbtn_wall_overallview);
		if (mMyCamera.mInstallMode == 0) {
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CIRCLE, 1);
		} else {
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_NEWSIDE, 1);
		}

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		screen_width = dm.widthPixels;
		screen_height = dm.heightPixels;
		mTvTotalTime = (TextView) findViewById(R.id.tv_total);
		mTvCurrent = (TextView) findViewById(R.id.tv_current);
		mSeekBar = (SeekBar) findViewById(R.id.sb_playing);
		mIvPausePlay = (ImageView) findViewById(R.id.iv_pause_play);
		mIvFastforward = (ImageView) findViewById(R.id.iv_fastforward);
		mTvSpeed = (TextView) findViewById(R.id.tv_speed);
		mLlCurrPro = (LinearLayout) findViewById(R.id.ll_play_local_pro);
		mTvCurrPro = (TextView) findViewById(R.id.tv_play_loca_current_pro);
		mTvDuraTime = (TextView) findViewById(R.id.tv_play_loca_druation_tim);
		mLlPlay = (LinearLayout) findViewById(R.id.ll_playlocal);
		ll_top = (LinearLayout) findViewById(R.id.ll_top);
		play_view_model = (TextView) findViewById(R.id.play_view_model);
		rl_view_model = (RelativeLayout) findViewById(R.id.rl_view_model);
		tv_install = (TextView) findViewById(R.id.tv_install);
		tv_install.setText(mMyCamera.mInstallMode == 0 ? getString(R.string.fish_top):getString(R.string.fish_wall));
		rg_view_model = (RadioGroup) findViewById(R.id.rg_view_model);
		hideAndShowView();
		btn_return = (ImageView) findViewById(R.id.btn_return);
		rbtn_circle = (RadioButton) findViewById(R.id.rbtn_circle);
		tv_timezone=(TextView) findViewById(R.id.tv_timezone);
		rbtn_circle.setChecked(true);

		playLocal = new PlayLocal();
	}

	private void hideAndShowView() {
		if (mMyCamera.mInstallMode == 0) {
			for (int i = 0; i < rg_view_model.getChildCount(); i++) {
				if (i == 5) {
					rg_view_model.getChildAt(i).setVisibility(View.GONE);
				} else {
					rg_view_model.getChildAt(i).setVisibility(View.VISIBLE);
				}
			}
		} else {
			for (int i = 0; i < rg_view_model.getChildCount(); i++) {
				if (i == 0 || i == 5) {
					rg_view_model.getChildAt(i).setVisibility(View.VISIBLE);
				} else {
					rg_view_model.getChildAt(i).setVisibility(View.GONE);
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (playLocal != null) {
			playLocal.registerPlayLocalStateListener(this);
			playLocal.setLiveShowMonitor(mMonitor);
			if (!filePath.isEmpty()) {
				startVideoPath();
			}

		}
	}

	private void startVideoPath() {
		if (HiTools.isSDCardExist()) {
			int playstate = playLocal.StartPlayLocal(filePath);
			if (playstate != 0) {
				HiToast.showToast(FishEyePlaybackLocalActivity.this, getString(R.string.tips_open_video_fail));
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {					
						FishEyePlaybackLocalActivity.this.finish();
					}
				}, 1000);
			}
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (playLocal != null) {
			playLocal.unregisterPlayLocalStateListener(this);
			int a=playLocal.StopPlayLocal();
		}
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		finish();
		
	}
	
	@Override
	public void onBackPressed() {
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		finish();
		
	}

	private void getIntentData() {
		Bundle bundle = getIntent().getExtras();

		String uid = bundle.getString(HiDataValue.EXTRAS_KEY_UID);
		strat_time=bundle.getString("strat_time");
		if (TextUtils.isEmpty(uid))		
			finish();
		for (MyCamera camera : HiDataValue.CameraList) {
			if (camera.getUid().equalsIgnoreCase(uid)) {
				mMyCamera = camera;
				break;
			}
		}

		filePath = bundle.getString(VideoLocalActivity.FILE_PATH);
		title = filePath.substring(filePath.lastIndexOf("/") + 1);
	}

	@Override
	public void callbackplaylocal(int width, int height, int filetime, long cursec, int audiotype, int state) {
		Message msg = Message.obtain();
		if (cursec != 0) {
			if (mFirstTime == 0) {
				mFirstTime = cursec;
			}
			long sub = cursec - mFirstTime;
			if (sub > 1000) {
				msg.what = HANDLE_MESSAGE_SEEKBAR_RUN;
				msg.arg1 = (int) sub;
				mHandler.sendMessage(msg);
			}
		}
		switch (state) {
		case PlayLocalFileCallback.PLAYLOCAL_STATE_OPEN:
			msg.what = HANDLE_MESSAGE_SEEKBAR_START;
			msg.arg1 = filetime;
			mHandler.sendMessage(msg);
			break;
		case PlayLocalFileCallback.PLAY_STATE_END:
			mHandler.sendEmptyMessage(HANDLE_MESSAGE_SEEKBAR_END);
			break;
		case PlayLocalFileCallback.PLAYLOCAL_STATE_ING:
			break;
		case PlayLocalFileCallback.PLAYLOCAL_STATE_ERROR:
			HiToast.showToast(FishEyePlaybackLocalActivity.this, getString(R.string.data_parsing_error));
			break;
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_MESSAGE_SEEKBAR_START:
				tv_timezone.setText(strat_time);
				resetViewMonitor();
				mIsEnd = false;
				mIsPalying = true;
				mIvPausePlay.setSelected(false);
				mTvTotalTime.setText(sdf.format(new Date(msg.arg1 * 1000)));
				mSeekBar.setMax(msg.arg1 * 1000);
				mProgressTime = msg.arg1 * 1000;
				break;
			case HANDLE_MESSAGE_SEEKBAR_RUN:
				if (!mIsDrag) {

					mSeekBar.setProgress(msg.arg1);

				}
				mTvCurrent.setText(sdf.format(new Date(msg.arg1)));
				
				Date date;
				try {
					date = sdf_time.parse(strat_time);
					long time=date.getTime()+msg.arg1;
					tv_timezone.setText(sdf_time.format(new Date(time)));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				break;
			case HANDLE_MESSAGE_SEEKBAR_END:
				mIsEnd = true;
				mIsPalying = false;
				mSeekBar.setProgress(mProgressTime);
				mTvCurrent.setText(sdf.format(new Date(mProgressTime)));
				mIvPausePlay.setSelected(true);
				playLocal.StopPlayLocal();// ***
				mTvSpeed.setText(" ");
				mSpeed = 0;
				HiToast.showToast(FishEyePlaybackLocalActivity.this, getString(R.string.tips_stop_video));
				break;
			}
		};
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.pb_local_exit:			
			finish();
			break;
		case R.id.iv_pause_play:
			if (mIsEnd) {
				playLocal.setLiveShowMonitor(mMonitor);
				int playstate = playLocal.StartPlayLocal(filePath);
				if (playstate != 0) {
					HiToast.showToast(FishEyePlaybackLocalActivity.this, getString(R.string.tips_open_video_fail));					
					finish();
				} else {
					handSpeed();
				}
			} else {
				if (mIsPalying) {
					playLocal.PlayLocal_pause();
				} else {
					playLocal.PlayLocal_Resume();
				}
				mIvPausePlay.setSelected(mIsPalying);
				mIsPalying = !mIsPalying;
			}
			break;
		case R.id.iv_fastforward:
			handleFast();
			break;
		case R.id.play_view_model:
			rl_view_model.setVisibility(View.VISIBLE);
			ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
			scaleAnimation.setDuration(400);
			rl_view_model.startAnimation(scaleAnimation);
			break;
		case R.id.rl_view_model:
			rl_view_model.setVisibility(View.GONE);
			scaleAnimation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
			scaleAnimation.setDuration(200);
			rl_view_model.startAnimation(scaleAnimation);
			break;
		case R.id.btn_return:
			FishEyePlaybackLocalActivity.this.finish();
			break;
		}

	}

	protected void resetViewMonitor() {
//		RelativeLayout.LayoutParams lp = null;
//		if (screen_width * 0.667 > screen_height) {
//			lp = new RelativeLayout.LayoutParams((int) (screen_width * 0.9),screen_height);
//		} else {
//			lp = new RelativeLayout.LayoutParams(screen_width, screen_height);
//		}
		RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(screen_width, screen_height);
		lp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
		lp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
		mMonitor.setLayoutParams(lp);

	}

	private void handSpeed() {
		switch (mSpeed) {
		case 2:
			playLocal.PlayLocal_Speed(3, 30);
			break;
		case 4:
			playLocal.PlayLocal_Speed(4, 15);
			break;
		case 8:
			playLocal.PlayLocal_Speed(12, 15);
			break;
		case 16:
			playLocal.PlayLocal_Speed(15, 15);
			break;
		case 0:
			playLocal.PlayLocal_Speed(0, 0);
			break;
		}
	}

	private void handleFast() {
		switch (mSpeed) {
		case 0:
			mSpeed = 2;
			mTvSpeed.setText("X 2");
			playLocal.PlayLocal_Speed(3, 30);
			break;
		case 2:
			mSpeed = 4;
			mTvSpeed.setText("X 4");
			playLocal.PlayLocal_Speed(4, 15);
			break;
		case 4:
			mSpeed = 8;
			mTvSpeed.setText("X 8");
			playLocal.PlayLocal_Speed(12, 15);
			break;
		case 8:
			mSpeed = 16;
			mTvSpeed.setText("X 16");
			playLocal.PlayLocal_Speed(15, 15);
			break;
		case 16:
			mSpeed = 0;
			mTvSpeed.setText(" ");
			playLocal.PlayLocal_Speed(0, 0);
			break;
		}
	}

	private long mFirstTime;
	private final static int HANDLE_MESSAGE_SEEKBAR_START = 0x90000002;
	private final static int HANDLE_MESSAGE_SEEKBAR_RUN = 0x90000003;
	private final static int HANDLE_MESSAGE_SEEKBAR_END = 0x90000004;
	private boolean mIsPalying = false;
	private TextView mTvTotalTime, mTvCurrent;
	private SimpleDateFormat sdf = new SimpleDateFormat("mm:ss");
	private SimpleDateFormat sdf_time=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private boolean mIsDrag = false;
	private SeekBar mSeekBar;
	private int mProgressTime;

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
		if (v.getId() == R.id.monitor_playback_local) {
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
					mMonitor.SetZoom(true);
					mMonitor.SetZoom(true);
					mMonitor.SetZoom(true);
					mMonitor.SetZoom(true);
				} else {
					mMonitor.SetZoom(false);
					mMonitor.SetZoom(false);
					mMonitor.SetZoom(false);
					mMonitor.SetZoom(false);
				}
				xlenOld = xlen;
				ylenOld = ylen;
				nLenStart = nLenEnd;
				return true;
			} else if (nCnt == 1) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					mMonitor.setTouchMove(0);
					break;
				case MotionEvent.ACTION_MOVE:
					if (mMonitor.getTouchMove() != 0)
						break;
					if (Math.abs(move_x - action_down_x) > 40 || Math.abs(move_y - action_down_y) > 40) {
						mMonitor.setTouchMove(1);
					}
					break;
				}
			}
		}
		return true;
	}

	int moveX;
	int moveY;

	@Override
	public void onCheckedChanged(RadioGroup group, int checkedId) {
		switch (checkedId) {
		case R.id.rbtn_wall_overallview:
			rl_view_model.setVisibility(View.GONE);
			mMonitor.mWallMode = 1;
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_SIDE, 0);
			break;
		case R.id.rbtn_circle:
			rl_view_model.setVisibility(View.GONE);
			if (mMyCamera.mInstallMode == 0) {
				mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CIRCLE, 1);
				mMonitor.setmFrameMode(1);
			} else {
				mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_NEWSIDE, 1);
				mMonitor.mWallMode = 0;
			}
			break;
		case R.id.rbtn_bowl:
			rl_view_model.setVisibility(View.GONE);
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CARTOON, 0);
			mMonitor.setmFrameMode(5);
			break;
		case R.id.rbtn_two:
			rl_view_model.setVisibility(View.GONE);
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_COLUMN, 2);
			mMonitor.setmFrameMode(3);
			break;
		case R.id.rbtn_four:
			rl_view_model.setVisibility(View.GONE);
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CIRCLE, 4);
			mMonitor.setmFrameMode(4);
			break;
		case R.id.rbtn_cylinder:
			rl_view_model.setVisibility(View.GONE);
			mMonitor.SetShowScreenMode(HiGLMonitor.VIEW_MODE_CARTOON, 1);
			mMonitor.setmFrameMode(2);
			break;
		}
	}

}
