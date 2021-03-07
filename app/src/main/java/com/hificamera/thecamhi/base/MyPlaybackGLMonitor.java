package com.hificamera.thecamhi.base;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.hificamera.hichip.activity.PlaybackLocalActivity;
import com.hificamera.hichip.activity.FishEye.FishEyePhotoActivity;
import com.hificamera.hichip.activity.FishEye.FishEyePlaybackLocalActivity;
import com.hificamera.hichip.activity.FishEye.FishPlaybackOnlineActivity;
import com.hichip.base.HiThread;
import com.hichip.control.HiGLMonitor;
import com.hificamera.thecamhi.bean.MyCamera;

public class MyPlaybackGLMonitor extends HiGLMonitor implements OnTouchListener, OnGestureListener, GestureDetector.OnDoubleTapListener {
	public int left;
	public int width;
	public int height;
	public int bottom;
	public int screen_width;
	public int screen_height;
	private OnTouchListener mOnTouchListener;
	private int state = 0; // normal=0, larger=1,two finger touch=3
	private int touchMoved; // not move=0, move=1, two point=2
	private GestureDetector gestureDetector;
	private Context mContext;
	public boolean mVisible = true;
	private MyCamera mCamera;
	public int mFrameMode = 1;
	public static int centerPoint;
	public boolean mIsZoom = false;
	public int mSetPosition = 0;
	public int mWallMode = 0;

	public MyPlaybackGLMonitor(Context context, AttributeSet attrs) {
		super(context, attrs);
		super.setOnTouchListener(this);
		this.mContext = context;
		gestureDetector = new GestureDetector(context, this);
		setOnTouchListener(this);
		setFocusable(true);
		setClickable(true);
		setLongClickable(true);

		DisplayMetrics dm = new DisplayMetrics();
		((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(dm);
		screen_width = dm.widthPixels;
		screen_height = dm.heightPixels;

	}

	public void setOnTouchListener(OnTouchListener mOnTouchListener) {
		this.mOnTouchListener = mOnTouchListener;
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public int getTouchMove() {
		return this.touchMoved;
	}

	public void setTouchMove(int touchMoved) {
		this.touchMoved = touchMoved;
	}

	private float rawX = 0;
	private float rawY = 0;
	private float lastX = 0;
	private float lastY = 0;

	int xlenOld;
	int ylenOld;

	private int pyl = 20;
	double nLenStart = 0;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mOnTouchListener != null) {
			mOnTouchListener.onTouch(v, event);
		}
		int nCnt = event.getPointerCount();
		if (nCnt == 1)
			gestureDetector.onTouchEvent(event);
		if (state == 1) {
			if (nCnt == 2) {
				return false;
			}
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					rawX = (event.getRawX());
					rawY = (event.getRawY());
					lastX = rawX;
					lastY = rawY;
					break;
				case MotionEvent.ACTION_MOVE:
					if (touchMoved == 2) {
						break;
					}
					rawX = event.getRawX();
					rawY = event.getRawY();
					float offsetX = rawX - lastX;
					float offsetY = rawY - lastY;
					if (Math.abs(offsetX) < pyl && Math.abs(offsetY) < pyl) {
						return false;
					}
					left += offsetX;
					bottom -= offsetY;
					if (left > 0) {
						left = 0;
					}
					if (bottom > 0) {
						bottom = 0;
					}
					if ((left + width < (screen_width))) {
						left = (int) (screen_width - width);
					}
					if (bottom + height < screen_height) {
						bottom = (int) (screen_height - height);
					}
					if (left <= (-width)) {
						left = (-width);
					}
					if (bottom <= (-height)) {
						bottom = (-height);
					}
					setMatrix(left, bottom, width, height);
					lastX = rawX;
					lastY = rawY;
					break;
			}
			return false;
		}
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		down_x=e.getRawX();
		down_y=e.getRawY();
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return true;
	}

	private float down_x = 0;
	private float down_y = 0;
	private float move_X = 0;
	private float move_Y = 0;

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		if (mCamera == null)  return false;
		if (mCamera.isFishEye()) {
			if (mFrameMode == 4) {
				if (Math.abs(distanceX) < 5) {
					return true;
				}

				handlerFrameMode_4(distanceX, e1.getX(), e1.getY());
			} else if (mFrameMode == 3) {
				handerFrameMode_3(e1, e2, distanceX);
			} else if (mFrameMode == 1 || mFrameMode == 2 || mFrameMode == 5) {

				if(mFrameMode==2||mFrameMode==5){
					int numX = (int) Math.ceil(Math.abs(distanceX) / 15);
					if(distanceX<0){
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
					}
					if(distanceX>0){
						setGesture(HiGLMonitor.GESTURE_LEFT, numX);
					}
					return true;
				}


				move_X = e2.getRawX();
				move_Y = e2.getRawY();
				float disX = move_X - down_x;
				float disY = move_Y - down_y;
				float K = disY / disX;
				float x = e2.getRawX();
				float y = e2.getRawY();
				boolean area_1 = false, area_2 = false, area_3 = false, area_4 = false, area_5 = false, area_6 = false, area_7 = false, area_8 = false;

				centerPointX = screen_width / 2;
				centerPointY = (int) (screen_height / 2);
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);

				boolean area_one = x < screen_width / 2 && y < screen_height / 2;
				if (area_one) {
					if (absY > absX) {
						area_8 = true;
					} else if (absX > absY) {
						area_7 = true;
					}
				}
				boolean area_two = x > screen_width / 2 && y < screen_height / 2;
				if (area_two) {
					if (absY > absX) {
						area_1 = true;
					} else if (absX > absY) {
						area_2 = true;
					}
				}
				boolean area_there = x < screen_width / 2 && y > screen_height / 2;
				if (area_there) {
					if (absY > absX) {
						area_5 = true;
					} else if (absX > absY) {
						area_6 = true;
					}
				}
				boolean area_four = x > screen_width / 2 && y > screen_height / 2;
				if (area_four) {
					if (absY > absX) {
						area_4 = true;
					} else if (absX > absY) {
						area_3 = true;
					}
				}

				int numX = (int) Math.ceil(Math.abs(distanceX) / 15);

				return handMyScro(e2, distanceX, distanceY, K, area_1, area_2, area_3, area_4, area_5, area_6, area_7, area_8, numX,0,screen_height);

			}

		}
		down_x = move_X;
		down_y = move_Y;

		return true;
	}

	private int mNO = 0;
	private int mScrollOri = 0;


	private boolean handMyScro(MotionEvent e1, float distanceX, float distanceY, float K, boolean area_1, boolean area_2, boolean area_3, boolean area_4, boolean area_5, boolean area_6, boolean area_7, boolean area_8, int numX, float pxTopview, int screen_width) {
		if (distanceX < 0) {
			if (K < 3 && K > 0.3) {
				if (mScrollOri != 1) {
					mNO = 0;
					mScrollOri = 1;
				}
				mNO++;
				if (mNO < 2) {
					return false;
				}
				if (this.GetFishLager() == 0.0) {
					if (area_8 || area_1 || area_2 || area_3) {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
					} else {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX);
					}
				} else {
					this.SetGesture(7);
					this.SetGesture(7);
					this.SetGesture(7);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						this.SetGesture(HiGLMonitor.GESTURE_RIGHT);
						this.SetGesture(HiGLMonitor.GESTURE_DOWN);
					}
				}
				down_x = move_X;
				down_y = move_Y;
				return true;
			} else if (K > -3 && K < -0.33) {
				if (mScrollOri != 2) {
					mNO = 0;
					mScrollOri = 2;
				}
				mNO++;
				if (mNO < 2) {
					return false;
				}
				if (this.GetFishLager() == 0.0) {
					if (area_8 || area_1 || area_6 || area_7) {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
					} else {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX);
					}
				} else {
					this.SetGesture(6);
					this.SetGesture(6);
					this.SetGesture(6);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						this.SetGesture(HiGLMonitor.GESTURE_RIGHT);
						this.SetGesture(HiGLMonitor.GESTURE_UP);
					}
				}
				down_x = move_X;
				down_y = move_Y;
				return true;
			} else if (K > 3 || K < -3) {
			} else {
				if (mScrollOri != 3) {
					mNO = 0;
					mScrollOri = 3;
				}
				mNO++;
				if (mNO < 2) {
					return false;
				}
				if (e1.getRawY() > pxTopview && e1.getRawY() < pxTopview + screen_width / 2 && mCamera.mInstallMode == 0) {// Y轴的上半轴
					if (this.GetFishLager() ==0.0) {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX /2);
					}else {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX/2);
					}
				} else if (e1.getRawY() > pxTopview + screen_width / 2 && e1.getRawY() < pxTopview + screen_width && mCamera.mInstallMode == 0) {
					if (this.GetFishLager() ==0.0) {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX/2);
					}else {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX/2);
					}
				} else {
					setGesture(HiGLMonitor.GESTURE_RIGHT, numX / 2);
				}
				down_x = move_X;
				down_y = move_Y;
				return true;
			}
		}
		if (distanceX > 0) {
			if (K < 3 && K > 0.33) {
				if (mScrollOri != 4) {
					mNO = 0;
					mScrollOri = 4;
				}
				mNO++;
				if (mNO < 2) {
					return false;
				}
				if (this.GetFishLager() == 0.0) {
					if (area_8 || area_1 || area_2 || area_3) {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX);
					} else {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
					}
				} else {
					this.SetGesture(4);
					this.SetGesture(4);
					this.SetGesture(4);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						this.SetGesture(HiGLMonitor.GESTURE_LEFT);
						this.SetGesture(HiGLMonitor.GESTURE_UP);
					}
				}
				down_x = move_X;
				down_y = move_Y;
				return true;
			} else if (K > -3 && K < -0.33) {
				if (mScrollOri != 5) {
					mNO = 0;
					mScrollOri = 5;
				}
				mNO++;
				if (mNO < 2) {
					return false;
				}
				if (this.GetFishLager() == 0.0) {
					if (area_1 || area_8 || area_7 || area_6) {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX);
					} else {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
					}
				} else {
					this.SetGesture(5);
					this.SetGesture(5);
					this.SetGesture(5);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						this.SetGesture(HiGLMonitor.GESTURE_LEFT);
						this.SetGesture(HiGLMonitor.GESTURE_DOWN);
					}
				}
				down_x = move_X;
				down_y = move_Y;
				return true;
			} else if (K > 3 || K < -3) {
			} else {
				if (mScrollOri != 6) {
					mNO = 0;
					mScrollOri = 6;
				}
				mNO++;
				if (mNO < 2) {
					return false;
				}
				if (e1.getRawY() > pxTopview && e1.getRawY() < pxTopview + screen_width / 2 && mCamera.mInstallMode == 0) {// Y轴的上半轴
					if (this.GetFishLager() ==0.0) {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX/2);
					}else {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX/2);
					}

				} else if (e1.getRawY() > pxTopview + screen_width / 2 && e1.getRawY() < pxTopview + screen_width && mCamera.mInstallMode == 0) {
					if (this.GetFishLager() ==0.0) {
						setGesture(HiGLMonitor.GESTURE_RIGHT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX/2);
					}else {
						setGesture(HiGLMonitor.GESTURE_LEFT, numX/2);
					}
				} else {
					setGesture(HiGLMonitor.GESTURE_LEFT, numX / 2);
				}

				down_x = move_X;
				down_y = move_Y;
				return true;
			}
		}
		int num = (int) Math.ceil(Math.abs(distanceY) / 40) + 1;
		if (distanceY < 0 && Math.abs(K) > 3.0) {// 5.下滑
			if (mScrollOri != 7) {
				mNO = 0;
				mScrollOri = 7;
			}
			mNO++;
			if (mNO < 2) {
				return false;
			}
			if (this.GetFishLager() != 0.0) {
				if (this.GetFishLager() < 8.0 && this.GetFishLager() >= 0) {
					setGesture(HiGLMonitor.GESTURE_DOWN, num);
				} else {
					setGesture(HiGLMonitor.GESTURE_DOWN, num - 1);
				}
			}
			if (this.GetFishLager() == 0.0) {
				if (area_1 || area_2 || area_3 || area_4) {
					setGesture(HiGLMonitor.GESTURE_RIGHT, num);
				} else {
					setGesture(HiGLMonitor.GESTURE_LEFT, num);
				}
			}
			down_x = move_X;
			down_y = move_Y;
			return true;
		}
		if (distanceY > 0 && Math.abs(K) > 3.0) {
			if (mScrollOri != 8) {
				mNO = 0;
				mScrollOri = 8;
			}
			mNO++;
			if (mNO < 2) {
				return false;
			}
			if (this.GetFishLager() != 0.0) {
				if (this.GetFishLager() < 8.0 && this.GetFishLager() >= 0.0) {
					setGesture(HiGLMonitor.GESTURE_UP, num);
				} else {
					setGesture(HiGLMonitor.GESTURE_UP, num - 1);
				}
			}
			if (this.GetFishLager() == 0.0) {
				if (area_1 || area_2 || area_3 || area_4) {
					setGesture(HiGLMonitor.GESTURE_LEFT, num);
				} else {
					setGesture(HiGLMonitor.GESTURE_RIGHT, num);
				}
			}
			down_x = move_X;
			down_y = move_Y;
			return true;
		}
		return false;
	}





	private void setGesture(int gesture, int num) {
		for (int i = 0; i < num; i++) {
			this.SetGesture(gesture);
			this.SetGesture(gesture);
		}
	}

	private void handerFrameMode_3(MotionEvent e1, MotionEvent e2, float distanceX) {
		boolean area_top = e1.getRawY() < screen_height / 2;
		boolean area_bottom = e1.getRawY() > screen_height / 2 && e1.getRawY() < screen_height;

		move_X = e2.getRawX();
		move_Y = e2.getRawY();
		float disX = move_X - down_x;
		float disY = move_Y - down_y;
		float K = disY / disX;
		if (area_top) {
			if (distanceX < 0 && Math.abs(K) < 1) {
				this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 0);
				this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 0);
			} else if (distanceX > 0 && Math.abs(K) < 1) {
				this.SetGesture(HiGLMonitor.GESTURE_LEFT, 0);
				this.SetGesture(HiGLMonitor.GESTURE_LEFT, 0);
			}
		} else if (area_bottom) {
			if (distanceX < 0 && Math.abs(K) < 1) {
				this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 1);
				this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 1);
			} else if (distanceX > 0 && Math.abs(K) < 1) {
				this.SetGesture(HiGLMonitor.GESTURE_LEFT, 1);
				this.SetGesture(HiGLMonitor.GESTURE_LEFT, 1);
			}
		}
	}

	@Override
	public void onLongPress(MotionEvent e) {

	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (mCamera == null)
			return false;
		if (mCamera.isFishEye()) {
			if (mFrameMode == 4 && Math.abs(velocityX) >= 1000 && mCamera.mInstallMode == 0) {
				float distanceX = e1.getRawX() - e2.getRawX();
				handlerFrameMode_4_Rotate(distanceX, e1.getX(), e1.getY());
				return true;
			}
			if (mFrameMode == 3 && mCamera.mInstallMode == 0) {
				boolean area_top = e1.getRawY() < screen_height / 2;
				boolean area_bottom = e1.getRawY() > screen_height / 2 && e1.getRawY() < screen_height;
				float disX = e2.getRawX() - e1.getRawX();
				if (area_top) {
					if (disX > 0 && Math.abs(velocityX) >= 1600) {
						startRotateMode4(HiGLMonitor.GESTURE_RIGHT, 100, 0);
					} else if (disX < 0 && Math.abs(velocityX) >= 1600) {
						startRotateMode4(HiGLMonitor.GESTURE_LEFT, 100, 0);
					}
				} else if (area_bottom) {
					if (disX > 0 && Math.abs(velocityX) >= 1600) {
						startRotateMode4(HiGLMonitor.GESTURE_RIGHT, 100, 1);
					} else if (disX < 0 && Math.abs(velocityX) >= 1600) {
						startRotateMode4(HiGLMonitor.GESTURE_LEFT, 100, 1);
					}
				}
				return true;
			}

			if(mFrameMode==2||mFrameMode == 5){
				int numX = (int) (Math.abs(e2.getRawX() - e1.getRawX()) / 3) / 4;
				if(velocityX>5000){
					startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
				}
				if(velocityX<-5000){
					startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
				}
				return true;
			}

			if (mFrameMode == 1 || mCamera.mInstallMode == 0 || mCamera.mInstallMode == 1) {
				int numX = (int) (Math.abs(e2.getRawX() - e1.getRawX()) / 3) / 4;
				float x = e2.getRawX();
				float y = e2.getRawY();
				centerPointX = screen_width / 2;
				centerPointY = (int) (screen_height / 2);
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				boolean area_1 = false, area_2 = false, area_3 = false, area_4 = false, area_5 = false, area_6 = false, area_7 = false, area_8 = false;

				boolean area_one =x < screen_width / 2 && y < screen_height / 2;
				if (area_one) {
					if (absY >= absX) {
						area_8 = true;
					} else if (absX > absY) {
						area_7 = true;
					}
				}

				boolean area_two = x > screen_width / 2 && y < screen_height / 2;
				if (area_two) {
					if (absY >= absX) {
						area_1 = true;
					} else if (absX > absY) {
						area_2 = true;
					}
				}

				boolean area_there = x < screen_width / 2 && y > screen_height / 2;
				if (area_there) {
					if (absY >= absX) {
						area_5 = true;
					} else if (absX > absY) {
						area_6 = true;
					}
				}

				boolean area_four = x > screen_width / 2 && y > screen_height / 2;
				if (area_four) {
					if (absY >=absX) {
						area_4 = true;
					} else if (absX > absY) {
						area_3 = true;
					}
				}
				return handMyRota(e2, velocityX, velocityY,numX, area_1, area_2, area_3, area_4,area_5,area_6,area_7,area_8,0,screen_height);
			}
		}
		return true;
	}


	private boolean handMyRota(MotionEvent e1, float velocityX, float velocityY, int numX, boolean area_1, boolean area_2, boolean area_3, boolean area_4, boolean area_5, boolean area_6, boolean area_7, boolean area_8, float pxTopview, int screen_width) {
		if (velocityX > 3000) {
			if (velocityY > 3000) {
				if (this.GetFishLager() == 0.0 || mFrameMode == 2 || mFrameMode == 5) {
					if (area_8 || area_1 || area_2 || area_3) {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					} else {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					}
				} else {
					startRotate(1, 7, numX);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						startRotate(1, GESTURE_RIGHT, numX / 2);
						startRotate(2, GESTURE_DOWN, numX / 2);
					}
				}
				return true;
			} else if (velocityY < -3000) {
				if (this.GetFishLager() == 0.0 || mFrameMode == 2 || mFrameMode == 5) {
					if (area_8 || area_1 || area_6 || area_7) {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					} else {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					}
				} else {
					startRotate(1, 6, numX);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						startRotate(1, GESTURE_RIGHT, numX / 2);
						startRotate(2, GESTURE_UP, numX / 2);
					}
				}
				return true;
			} else{
				if (e1.getRawY() > pxTopview && e1.getRawY() < pxTopview + screen_width / 2 && mCamera.mInstallMode == 0) {
					if (this.GetFishLager() ==0.0) {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					}else {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					}
				} else if (e1.getRawY() > pxTopview + screen_width / 2 && e1.getRawY() < pxTopview + screen_width && mCamera.mInstallMode == 0) {
					if (this.GetFishLager() ==0.0) {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					}else {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					}
				} else {
					startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX / 2);
				}
				return true;
			}
		}
		if (velocityX < -3000) {
			if (velocityY < -3000) {
				if (this.GetFishLager() == 0.0 || mFrameMode == 2 || mFrameMode == 5) {
					if (area_8 || area_1 || area_2 || area_3) {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					} else {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					}
				} else {
					startRotate(1, 4, numX);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						startRotate(1, GESTURE_LEFT, numX / 2);
						startRotate(2, GESTURE_UP, numX / 2);
					}
				}
				return true;
			} else if (velocityY > 3000) {
				if (this.GetFishLager() == 0.0 || mFrameMode == 2 || mFrameMode == 5) {
					if (area_1 || area_8 || area_7 || area_6) {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					} else {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					}
				} else {
					startRotate(1, 5, numX);
					if (mCamera.mInstallMode == 1 || mCamera.isWallMounted) {
						startRotate(1, GESTURE_LEFT, numX / 2);
						startRotate(2, GESTURE_DOWN, numX / 2);
					}
				}
				return true;
			} else {
				if (e1.getRawY() > pxTopview && e1.getRawY() < pxTopview + screen_width / 2 && mCamera.mInstallMode == 0) {// Y轴的上半轴
					if (this.GetFishLager() ==0.0) {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					}else {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					}

				} else if (e1.getRawY() > pxTopview + screen_width / 2 && e1.getRawY() < pxTopview + screen_width && mCamera.mInstallMode == 0) {
					if (this.GetFishLager() ==0.0) {
						startRotate(1, HiGLMonitor.GESTURE_RIGHT, numX);
					} else if(this.GetFishLager() >0.0 && this.GetFishLager() < 8.0) {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					}else {
						startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
					}
				} else {
					startRotate(1, HiGLMonitor.GESTURE_LEFT, numX);
				}
				return true;
			}
		}
		if (velocityY > 3000) {
			if (this.GetFishLager() == 0.0 || mFrameMode == 2 || mFrameMode == 5) {
				if (area_1 || area_2 || area_3 || area_4) {
					startRotate(1, HiGLMonitor.GESTURE_RIGHT, 60);
				} else {
					startRotate(1, HiGLMonitor.GESTURE_LEFT, 60);
				}
			} else {
				startRotate(1, HiGLMonitor.GESTURE_DOWN, 40);
			}
			return true;
		} else if (velocityY <- 3000) {
			if (this.GetFishLager() == 0.0 || mFrameMode == 2 || mFrameMode == 5) {
				if (area_1 || area_2 || area_3 || area_4) {
					startRotate(1, HiGLMonitor.GESTURE_LEFT, 60);
				} else {
					startRotate(1, HiGLMonitor.GESTURE_RIGHT, 60);
				}
			} else {
				startRotate(1, HiGLMonitor.GESTURE_UP, 40);
			}
			return true;
		}
		return false;
	}




	private void startRotate(int No, final int gesture, final int num) {
		if (mthreadGesture_2 != null) {
			mthreadGesture_2.stopThread();
			mthreadGesture_2 = null;
		}
		if (No == 1) {
			if (mthreadGesture != null) {
				mthreadGesture.stopThread();
				mthreadGesture = null;
			}
			mthreadGesture = new ThreadGesture();
			mthreadGesture.SetValue(num, gesture, -1);
			mthreadGesture.startThread();
		} else {
			mthreadGesture_2 = new ThreadGesture();
			mthreadGesture_2.SetValue(num, gesture, -1);
			mthreadGesture_2.startThread();
		}

	}

	private ThreadGesture mthreadGesture = null;
	private ThreadGesture mthreadGesture_2 = null;

	private class ThreadGesture extends HiThread {
		int num;
		int gesture;
		int no;
		public void SetValue(int num, int gesture, int no) {
			this.num = num;
			this.gesture = gesture;
			this.no = no;
		}
		public void run() {
			int i = 0;
			while (isRunning && i < num) {
				if (i < num / 5)
					i++;
				else if (i < num * 2 / 5)
					i += 5;
				else if (i < num * 3 / 5)
					i += 3;
				else if (i < num * 4 / 5)
					i += 1;
				else
					i++;
				try {
					Thread.sleep(20);
					int b = (num * 2 / 3 - i);
					if (b < 4)
						b = 4;
					int j = b / 4;
					for (int a = j; a >= 0; a--) {
						if (no != -1)
							MyPlaybackGLMonitor.this.SetGesture(gesture, no);
						else
							MyPlaybackGLMonitor.this.SetGesture(gesture);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} // isRunning
	}




	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {

		if(mContext instanceof PlaybackLocalActivity){
			PlaybackLocalActivity act=(PlaybackLocalActivity) mContext;
			if (mVisible) {
				act.mLlPlay.setVisibility(View.GONE);
			} else {
				act.mLlPlay.setVisibility(View.VISIBLE);
			}
		}

		if (mContext instanceof FishPlaybackOnlineActivity) {
			FishPlaybackOnlineActivity act = (FishPlaybackOnlineActivity) mContext;
			if (mVisible) {
//				act.mllPlay.animate().translationX(1.0f).translationY(act.mllPlay.getHeight()).start();
//				act.ll_top.animate().translationX(1.0f).translationY(-act.ll_top.getHeight()).start();
				act.mllPlay.setVisibility(View.GONE);
				act.ll_top.setVisibility(View.GONE);
			} else {
//				act.mllPlay.animate().translationX(1.0f).translationY(1.0f).start();
//				act.ll_top.animate().translationX(1.0f).translationY(1.0f).start();
				act.mllPlay.setVisibility(View.VISIBLE);
				act.ll_top.setVisibility(View.VISIBLE);
			}
		}
		if (mContext instanceof FishEyePlaybackLocalActivity) {
			FishEyePlaybackLocalActivity act = (FishEyePlaybackLocalActivity) mContext;
			if (mVisible) {
//				act.mLlPlay.animate().translationX(1.0f).translationY(act.mLlPlay.getHeight()).start();
//				act.ll_top.animate().translationX(1.0f).translationY(-act.ll_top.getHeight()).start();
				act.mLlPlay.setVisibility(View.GONE);
				act.ll_top.setVisibility(View.GONE);
			} else {
//				act.mLlPlay.animate().translationX(1.0f).translationY(1.0f).start();
//				act.ll_top.animate().translationX(1.0f).translationY(1.0f).start();
				act.mLlPlay.setVisibility(View.VISIBLE);
				act.ll_top.setVisibility(View.VISIBLE);
			}
		}
		if (mContext instanceof FishEyePhotoActivity) {
			FishEyePhotoActivity act = (FishEyePhotoActivity) mContext;
			if (mVisible) {
				act.ll_top.animate().translationX(1.0f).translationY(-act.ll_top.getHeight()).start();
			} else {
				act.ll_top.animate().translationX(1.0f).translationY(1.0f).start();
			}

		}


		mVisible = !mVisible;
		return true;
	}

	float resetWidth;
	float resetHeight;
	private int centerPointX;
	private int centerPointY;

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (mCamera!=null&&mCamera.isFishEye()) {
			if (mContext instanceof FishEyePhotoActivity && mCamera.mInstallMode == 1) {
				FishEyePhotoActivity act = (FishEyePhotoActivity) mContext;
				if (mWallMode == 1) {
					this.SetShowScreenMode(HiGLMonitor.VIEW_MODE_NEWSIDE, 1);
					mWallMode = 0;
					act.rbtn_circle.setChecked(true);
					return true;
				}
			}

			if (mContext instanceof FishPlaybackOnlineActivity && mCamera.mInstallMode == 1) {
				FishPlaybackOnlineActivity act = (FishPlaybackOnlineActivity) mContext;
				if (mWallMode == 1) {
					this.SetShowScreenMode(HiGLMonitor.VIEW_MODE_NEWSIDE, 1);
					mWallMode = 0;
					act.rbtn_circle.setChecked(true);
					return true;
				}
			}

			if (mContext instanceof FishEyePlaybackLocalActivity && mCamera.mInstallMode == 1) {
				FishEyePlaybackLocalActivity act = (FishEyePlaybackLocalActivity) mContext;
				if (mWallMode == 1) {
					this.SetShowScreenMode(HiGLMonitor.VIEW_MODE_NEWSIDE, 1);
					mWallMode = 0;
					act.rbtn_circle.setChecked(true);
					return true;
				}
			}

			float x = e.getRawX();
			float y = e.getRawY();
			int pxMoniter = screen_height;
			int pxTopview = 0;
			centerPoint = screen_height / 2 / 3;
			centerPointX = screen_width / 2;
			centerPointY = screen_height / 2 + pxTopview;
			boolean area_one = x < screen_width / 2 && y > pxTopview && y < pxTopview + pxMoniter / 2;
			boolean area_two = x > screen_width / 2 && x < screen_width && y < pxTopview + pxMoniter / 2 && y > pxTopview;
			boolean area_there = x < screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
			boolean area_four = x > screen_width / 2 && y > pxTopview + pxMoniter / 2 && y < pxMoniter + pxTopview;
			if (mFrameMode == 1) {
				if (mIsZoom == true && GetFishLager() == 0.0) {
					mIsZoom = false;
				}
				if (GetFishLager() > 0.0 && !mIsZoom) {
					this.SetPosition(false, 8);
					return true;
				}
				if (mIsZoom) {
					this.SetPosition(false, mSetPosition);
					mIsZoom = !mIsZoom;
					return true;
				}
				handlerFrameMode1(x, y, pxTopview, centerPoint, area_one, area_two, area_there, area_four);
			}
			return true;
		}
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	private void handlerFrameMode1(float x, float y, float pxTopview, float centerPoint, boolean area_one, boolean area_two, boolean area_there, boolean area_four) {
		if (area_one) {
			float distanceX = screen_width / 2 - x;
			float distanceY = screen_height / 2 + pxTopview - y;
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screen_height / 2 && distance > centerPoint) {
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screen_height / 2 && distance > centerPoint) {
					if (!mIsZoom) {
						this.SetPosition(true, 6);
						mIsZoom = !mIsZoom;
						mSetPosition = 6;
					}
				} else if (absX < absY && distance < screen_height / 2 && distance > centerPoint) {
					if (!mIsZoom) {
						this.SetPosition(true, 7);
						mIsZoom = !mIsZoom;
						mSetPosition = 7;
					}
				}
			} else if (distance < centerPoint) {
				if (!mIsZoom) {
					this.SetPosition(true, 8);
					mIsZoom = !mIsZoom;
					mSetPosition = 8;
				}
			}
		} else if (area_two) {
			float distanceX = x - screen_width / 2;
			float distanceY = screen_height / 2 + pxTopview - y;
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screen_height / 2 && distance > centerPoint) {
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screen_height / 2 && distance > centerPoint) {
					if (!mIsZoom) {
						this.SetPosition(true, 1);
						mIsZoom = !mIsZoom;
						mSetPosition = 1;
					}
				} else if (absX < absY && distance < screen_height / 2 && distance > centerPoint) {
					if (!mIsZoom) {
						this.SetPosition(true, 0);
						mIsZoom = !mIsZoom;
						mSetPosition = 0;
					}
				}
			} else if (distance < centerPoint) {
				if (!mIsZoom) {
					this.SetPosition(true, 8);
					mIsZoom = !mIsZoom;
					mSetPosition = 8;
				}
			}
		} else if (area_there) {
			float distanceX = screen_width / 2 - x;
			float distanceY = y - (screen_height / 2 + pxTopview);
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screen_height / 2 && distance > centerPoint) {
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screen_height / 2 && distance > centerPoint) {
					if (!mIsZoom) {
						this.SetPosition(true, 5);
						mIsZoom = !mIsZoom;
						mSetPosition = 5;
					}
				} else if (absX < absY && distance < screen_height / 2 && distance > centerPoint) {
					if (!mIsZoom) {
						this.SetPosition(true, 4);
						mIsZoom = !mIsZoom;
						mSetPosition = 4;
					}
				}
			} else if (distance < centerPoint) {
				if (!mIsZoom) {
					this.SetPosition(true, 8);
					mIsZoom = !mIsZoom;
					mSetPosition = 8;
				}
			}
		} else if (area_four) {
			float distanceX = x - screen_width / 2;
			float distanceY = y - screen_height / 2 - pxTopview;
			double distance = Math.sqrt(Math.pow(distanceX, 2) + Math.pow(distanceY, 2));
			if (distance < screen_width / 2 && distance > centerPoint) {
				float absX = Math.abs(x - centerPointX);
				float absY = Math.abs(y - centerPointY);
				if (absX > absY && distance < screen_height / 2 && distance > centerPoint) {
					if (!mIsZoom) {
						this.SetPosition(true, 2);
						mIsZoom = !mIsZoom;
						mSetPosition = 2;
					}
				} else if (absX < absY && distance < screen_height / 2 && distance > centerPoint) {
					if (!mIsZoom) {
						this.SetPosition(true, 3);
						mIsZoom = !mIsZoom;
						mSetPosition = 3;
					}
				}
			} else if (distance < centerPoint) {
				if (!mIsZoom) {
					this.SetPosition(true, 8);
					mIsZoom = !mIsZoom;
					mSetPosition = 8;
				}
			}
		}
	}

	private void handlerFrameMode_4(float distanceX, float x, float y) {
		boolean area_one = false;
		boolean area_two = false;
		boolean area_there = false;
		boolean area_four = false;
		area_one = x < screen_width / 2 && y < screen_height / 2;
		area_two = x > screen_width / 2 && y < screen_height / 2;
		area_there = x < screen_width / 2 && y > screen_height / 2;
		area_four = x > screen_width / 2 && y > screen_height / 2;
		int num = 1;
		if (area_one) {
			for (int i = 0; i <= num; i++) {
				if (distanceX > 0) {
					this.SetGesture(HiGLMonitor.GESTURE_LEFT, 2);
				} else {
					this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 2);
				}
			}
		} else if (area_two) {
			for (int i = 0; i <= num; i++) {
				if (distanceX > 0) {
					this.SetGesture(HiGLMonitor.GESTURE_LEFT, 0);
				} else {
					this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 0);
				}
			}
		} else if (area_there) {
			for (int i = 0; i <= num; i++) {
				if (distanceX > 0) {
					this.SetGesture(HiGLMonitor.GESTURE_LEFT, 3);
				} else {
					this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 3);
				}
			}
		} else if (area_four) {
			for (int i = 0; i <= num; i++) {
				if (distanceX > 0) {
					this.SetGesture(HiGLMonitor.GESTURE_LEFT, 1);
				} else {
					this.SetGesture(HiGLMonitor.GESTURE_RIGHT, 1);
				}
			}
		}
	}

	private void handlerFrameMode_4_Rotate(float distanceX, float x, float y) {
		boolean area_one = false;
		boolean area_two = false;
		boolean area_there = false;
		boolean area_four = false;
		area_one = x < screen_width / 2 && y < screen_height / 2;
		area_two = x > screen_width / 2 && y < screen_height / 2;
		area_there = x < screen_width / 2 && y > screen_height / 2;
		area_four = x > screen_width / 2 && y > screen_height / 2;
		int num = (int) Math.abs(distanceX) / 8;
		if (area_one) {
			if (distanceX > 0) {
				startRotateMode4(HiGLMonitor.GESTURE_LEFT, num, 2);
			} else {
				startRotateMode4(HiGLMonitor.GESTURE_RIGHT, num, 2);
			}
		} else if (area_two) {
			if (distanceX > 0) {
				startRotateMode4(HiGLMonitor.GESTURE_LEFT, num, 0);
			} else {
				startRotateMode4(HiGLMonitor.GESTURE_RIGHT, num, 0);
			}
		} else if (area_there) {
			if (distanceX > 0) {
				startRotateMode4(HiGLMonitor.GESTURE_LEFT, num, 3);
			} else {
				startRotateMode4(HiGLMonitor.GESTURE_RIGHT, num, 3);
			}
		} else if (area_four) {
			if (distanceX > 0) {
				startRotateMode4(HiGLMonitor.GESTURE_LEFT, num, 1);
			} else {
				startRotateMode4(HiGLMonitor.GESTURE_RIGHT, num, 1);
			}
		}

	}

	private void startRotateMode4(final int gesture, final int num, final int no) {
		if (mthreadGesture != null) {
			mthreadGesture.stopThread();
			mthreadGesture = null;
		}
		mthreadGesture = new ThreadGesture();
		mthreadGesture.SetValue(num, gesture, no);
		mthreadGesture.startThread();
	}

	public void setCamera(MyCamera mCamera) {
		this.mCamera = mCamera;
	}

	public void setmFrameMode(int frameMode) {
		this.mFrameMode = frameMode;
	}

}
