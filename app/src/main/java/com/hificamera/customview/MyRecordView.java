package com.hificamera.customview;

import com.hificamera.R;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class MyRecordView extends View {
	private static final int WHAT_LONG_CLICK = 1;
	private static final int WHAT_SINGLE_CLICK = 2;
	private Paint mCirclePaint;
	private Paint mTextPaint;
	private Paint mTextMillsPaint;
	private Paint mProgressCirclePaint;
	private int mHeight;
	private int mWidth;
	private float mInitBitRadius;
	private float mInitSmallRadius;
	private float mBigRadius;
	private float mSmallRadius;
	private long mStartTime;
	private long mEndTime;
	private Context mContext;
	private boolean isRecording;
	private boolean isMaxTime;
	private float mCurrentProgress;

	private long mLongClickTime = 500;
	private int mTime = 5;
	private int mMinTime = 2;
	private int mProgressColor;
	private float mProgressW = 18f;

	private boolean isPressed;
	private ValueAnimator mProgressAni;
	private ValueAnimator playAni;
	private String centerText;
	private boolean isPlayRecording;
	private boolean isPlayType;
	private int mCurrentProgressTime;

	private boolean isCanTouch = true;

	private String playCenterText;

	public MyRecordView(Context context) {
		super(context);
		init(context, null);
	}

	public MyRecordView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	public MyRecordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		this.mContext = context;
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.MyRecordView);
		mMinTime = a.getInt(R.styleable.MyRecordView_minTime, 2);
		mTime = a.getInt(R.styleable.MyRecordView_maxTime, 15);
		centerText = a.getString(R.styleable.MyRecordView_centerText);
		playCenterText = a.getString(R.styleable.MyRecordView_playCenterText);
		isPlayRecording = a.getBoolean(R.styleable.MyRecordView_isPlayRecording, false);
		mProgressW = a.getDimension(R.styleable.MyRecordView_progressWidth, 25f);
		mProgressColor = a.getColor(R.styleable.MyRecordView_progressColor, Color.parseColor("#6ABF66"));
		a.recycle();
		mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mCirclePaint.setColor(Color.parseColor("#3A82CD"));
		mCirclePaint.setStrokeWidth(2);
		mCirclePaint.setStyle(Paint.Style.STROKE);

		mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextPaint.setColor(Color.parseColor("#3A82CD"));
		mTextPaint.setTextSize(35);
		mTextPaint.setTextAlign(Paint.Align.CENTER);

		mTextMillsPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mTextMillsPaint.setColor(Color.parseColor("#6ABF66"));
		mTextMillsPaint.setTextSize(35);
		mTextMillsPaint.setTextAlign(Paint.Align.CENTER);

		mProgressCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mProgressCirclePaint.setColor(mProgressColor);

		mProgressAni = ValueAnimator.ofFloat(0, 360f);
		mProgressAni.setDuration(mTime * 1000);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mWidth = MeasureSpec.getSize(widthMeasureSpec);
		mHeight = MeasureSpec.getSize(heightMeasureSpec);
		mInitBitRadius = mBigRadius = mWidth / 2 * 0.75f;
		mInitSmallRadius = mSmallRadius = mBigRadius * 0.75f;
	}

	@Override
	protected void onDraw(final Canvas canvas) {
		super.onDraw(canvas);
		canvas.drawCircle(mWidth / 2, mHeight / 2, mBigRadius, mCirclePaint);

		if (isRecording || isPlayType) {
			drawProgress(canvas);
		} else {
			Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
			float top = fontMetrics.top;
			float bottom = fontMetrics.bottom;
			canvas.drawText(centerText, mWidth / 2, mHeight / 2 - top / 2 - bottom / 2, mTextPaint);
		}
	}

	private void drawProgress(Canvas canvas) {
		mProgressCirclePaint.setStrokeWidth(mProgressW);
		mProgressCirclePaint.setStyle(Paint.Style.STROKE);
		RectF oval = new RectF(mWidth / 2 - (mBigRadius - mProgressW / 2), mHeight / 2 - (mBigRadius - mProgressW / 2),
				mWidth / 2 + (mBigRadius - mProgressW / 2), mHeight / 2 + (mBigRadius - mProgressW / 2));
		canvas.drawArc(oval, -90, mCurrentProgress, false, mProgressCirclePaint);
		Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
		float top = fontMetrics.top;
		float bottom = fontMetrics.bottom;
		if (isPlayRecording) {
			canvas.drawText(playCenterText, mWidth / 2, mHeight / 2 - top / 2 - bottom / 2, mTextMillsPaint);
		}else {
			canvas.drawText(mCurrentProgressTime + " S", mWidth / 2, mHeight / 2 - top / 2 - bottom / 2, mTextMillsPaint);
		}

	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case WHAT_LONG_CLICK:

					if (!isCanTouch) {
						return ;
					}
					if (onLongClickListener != null) {
						onLongClickListener.onLongClick();
					}
					startAnimation(mBigRadius, mBigRadius * 1.33f, mSmallRadius, mSmallRadius * 0.7f);
					break;

			}
		}
	};

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (isCanTouch) {
			switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					if (!isPlayRecording) {
						isPressed = true;
						mStartTime = System.currentTimeMillis();
						Message mMessage = Message.obtain();
						mMessage.what = WHAT_LONG_CLICK;
						mHandler.sendMessageDelayed(mMessage, mLongClickTime);
					} else {
						if (onClickListener != null)
							onClickListener.onClick();
					}

					break;
				case MotionEvent.ACTION_UP:

					if (!isPlayRecording) {
						isPressed = false;
						isRecording = false;
						mEndTime = System.currentTimeMillis();

						if (mEndTime - mStartTime < mLongClickTime&&mEndTime - mStartTime>0) {
							mHandler.removeMessages(WHAT_LONG_CLICK);
							if (onClickListener != null)
								onClickListener.onClick();
						} else {
							resetAnimation();
							if (mProgressAni != null && mProgressAni.getCurrentPlayTime() / 1000 < mMinTime && !isMaxTime) {
								if (onLongClickListener != null) {
									onLongClickListener.onNoMinRecord(mMinTime);
								}
								cancelProgressAni();
							} else {
								if (onLongClickListener != null && !isMaxTime) {
									onLongClickListener.onRecordFinishedListener();
								}
							}
						}

					}

					break;
			}
		}
		return true;

	}

	public void resetAnimation() {
		startAnimation(mBigRadius, mInitBitRadius, mSmallRadius, mInitSmallRadius);
	}

	public void setTouchAble(boolean isCanTouch) {
		this.isCanTouch = isCanTouch;
	}

	public boolean getTouchAble() {
		return this.isCanTouch;
	}

	public void cancelProgressAni() {
		mProgressAni.cancel();
	}

	public void cancelPlayProgressAni() {
		if (playAni != null) {
			playAni.cancel();
		}
	}

	public void startPlay(int mTime, float mProgressW) {
		this.mTime = mTime;
		this.mProgressW = mProgressW;
		this.isPlayType = true;

		if (playAni!=null) {
			playAni.cancel();
			playAni = null;
			isPlayType = true;
		}

		mCurrentProgress = 0;
		mCurrentProgressTime = 0;
		playAni = ValueAnimator.ofFloat(0, 360f);
		playAni.setDuration(mTime * 1000);
		Log.e("==mAudioFile==", mTime + "----" + mProgressW + "----" + isPlayType);
		startPlayAnimation();
	}

	private void startAnimation(float bigStart, float bigEnd, float smallStart, float smallEnd) {
		ValueAnimator bigObjAni = ValueAnimator.ofFloat(bigStart, bigEnd);
		bigObjAni.setDuration(150);
		bigObjAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mBigRadius = (float) animation.getAnimatedValue();
				invalidate();
			}
		});

		ValueAnimator smallObjAni = ValueAnimator.ofFloat(smallStart, smallEnd);
		smallObjAni.setDuration(150);
		smallObjAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mSmallRadius = (float) animation.getAnimatedValue();
				invalidate();
			}
		});

		bigObjAni.start();
		smallObjAni.start();

		smallObjAni.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {
				if (!isPlayRecording) {
					isRecording = false;
				}

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (isPressed) {
					isRecording = true;
					isMaxTime = false;
					startProgressAnimation();
				}
			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});

	}

	private void startProgressAnimation() {
		mProgressAni.start();
		mProgressAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mCurrentProgress = (float) animation.getAnimatedValue();
				mCurrentProgressTime = (int) animation.getCurrentPlayTime() / 1000;
				invalidate();
			}
		});

		mProgressAni.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				if (onLongClickListener != null && isPressed) {
					isPressed = false;
					isMaxTime = true;
					onLongClickListener.onRecordFinishedListener();
					startAnimation(mBigRadius, mInitBitRadius, mSmallRadius, mInitSmallRadius);
					mCurrentProgress = 0;
					mCurrentProgressTime = 0;
					isRecording = false;
					invalidate();
				}

			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
	}
	private void startPlayAnimation() {
		Log.e("==mAudioFile==", mTime + "----" + mProgressW + "----" + isPlayType);
		playAni.start();
		playAni.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

			@Override
			public void onAnimationUpdate(ValueAnimator animation) {
				mCurrentProgress = (float) animation.getAnimatedValue();
				mCurrentProgressTime = (int) animation.getCurrentPlayTime() / 1000;
				invalidate();
			}
		});

		playAni.addListener(new Animator.AnimatorListener() {
			@Override
			public void onAnimationStart(Animator animation) {

			}

			@Override
			public void onAnimationEnd(Animator animation) {
				isPlayType = false;
				invalidate();

			}

			@Override
			public void onAnimationCancel(Animator animation) {

			}

			@Override
			public void onAnimationRepeat(Animator animation) {

			}
		});
	}

	public interface OnLongClickListener {
		void onLongClick();

		void onNoMinRecord(int currentTime);

		void onRecordFinishedListener();
	}

	public OnLongClickListener onLongClickListener;

	public void setOnLongClickListener(OnLongClickListener onLongClickListener) {
		this.onLongClickListener = onLongClickListener;
	}

	public interface MyOnClickListener {
		void onClick();
	}

	public MyOnClickListener onClickListener;

	public void setOnClickListener(MyOnClickListener onClickListener) {
		this.onClickListener = onClickListener;
	}

}
