package com.hificamera.customview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

public class CropImageView extends View {
    private float mX_1 = 0;
    private float mY_1 = 0;
    private final int STATUS_SINGLE = 1;
    private final int STATUS_MULTI_START = 2;
    private final int STATUS_MULTI_TOUCHING = 3;
    private int mStatus = STATUS_SINGLE;
    private int cropWidth;
    private int cropHeight;
    private final int EDGE_LT = 1;
    private final int EDGE_RT = 2;
    private final int EDGE_LB = 3;
    private final int EDGE_RB = 4;
    private final int EDGE_MOVE_IN = 5;
    private final int EDGE_MOVE_OUT = 6;
    private final int EDGE_NONE = 7;

    public int currentEdge = EDGE_NONE;

    protected float oriRationWH = 0;

    protected Drawable mDrawable;
    protected FloatDrawable mFloatDrawable;

    protected Rect mDrawableSrc = new Rect();
    protected Rect mDrawableDst = new Rect();
    protected Rect mDrawableFloat = new Rect();
    protected boolean isFirst = true;
    private boolean isTouchInSquare = true;
    protected Context mContext;
    private int X;
    private int Y;

    public int mMinFloatWidth;
    public int mMinFloatHeight;

    public CropImageView(Context context) {
        super(context);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CropImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    @SuppressLint("NewApi")
    private void init(Context context) {
        this.mContext = context;
        try {
            if (android.os.Build.VERSION.SDK_INT >= 11) {
                this.setLayerType(LAYER_TYPE_SOFTWARE, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        mFloatDrawable = new FloatDrawable(context);
    }

    public void setDrawable(Bitmap bitmap, int cropWidth, int cropHeight,int X,int Y) {
        this.mDrawable = new BitmapDrawable(bitmap);
        this.cropWidth = cropWidth;
        this.cropHeight = cropHeight;
        this.isFirst = true;
        this.X=X;
        this.Y=Y;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        setMeasuredDimension(widthSize, heightSize);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if (event.getPointerCount() > 1) {
            if (mStatus == STATUS_SINGLE) {
                mStatus = STATUS_MULTI_START;
            } else if (mStatus == STATUS_MULTI_START) {
                mStatus = STATUS_MULTI_TOUCHING;
            }
        } else {
            if (mStatus == STATUS_MULTI_START
                    || mStatus == STATUS_MULTI_TOUCHING) {
                mX_1 = event.getX();
                mY_1 = event.getY();
            }

            mStatus = STATUS_SINGLE;
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mX_1 = event.getX();
                mY_1 = event.getY();
                currentEdge = getTouch((int) mX_1, (int) mY_1);
                break;

            case MotionEvent.ACTION_UP:
                break;

            case MotionEvent.ACTION_POINTER_UP:
                currentEdge = EDGE_NONE;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mStatus == STATUS_MULTI_TOUCHING) {

                } else if (mStatus == STATUS_SINGLE) {
                    int dx = (int) (event.getX() - mX_1);
                    int dy = (int) (event.getY() - mY_1);
                    mX_1 = event.getX();
                    mY_1 = event.getY();
                    if (dx != 0 || dy != 0) {
                        switch (currentEdge) {
                            case EDGE_LT:
                                if (mDrawableFloat.right - (mDrawableFloat.left + dx) <=mMinFloatWidth) {
                                    dx=0;
                                }
                                if (mDrawableFloat.bottom - (mDrawableFloat.top + dy) <= mMinFloatHeight) {
                                    dy=0;
                                }
                                mDrawableFloat.set(mDrawableFloat.left + dx,
                                        mDrawableFloat.top + dy,
                                        mDrawableFloat.right,
                                        mDrawableFloat.bottom);
                                break;

                            case EDGE_RT:
                                if ((mDrawableFloat.right + dx) - mDrawableFloat.left <= mMinFloatWidth) {
                                    dx=0;
                                }
                                if (mDrawableFloat.bottom - (mDrawableFloat.top + dy) <= mMinFloatHeight) {
                                    dy=0;
                                }
                                mDrawableFloat.set(mDrawableFloat.left,
                                        mDrawableFloat.top + dy,
                                        mDrawableFloat.right + dx,
                                        mDrawableFloat.bottom);
                                break;

                            case EDGE_LB:
                                if (mDrawableFloat.right - (mDrawableFloat.left + dx) <= mMinFloatWidth) {
                                    dx=0;
                                }
                                if (mDrawableFloat.bottom + dy - mDrawableFloat.top <= mMinFloatHeight) {
                                    dy=0;
                                }
                                mDrawableFloat.set(mDrawableFloat.left + dx,
                                        mDrawableFloat.top,
                                        mDrawableFloat.right,
                                        mDrawableFloat.bottom + dy);
                                break;

                            case EDGE_RB:
                                int r_l=(mDrawableFloat.right + dx) - mDrawableFloat.left;
                                if (r_l <= mMinFloatWidth) {
                                    dx=0;
                                }
                                if (mDrawableFloat.bottom + dy - mDrawableFloat.top <= mMinFloatHeight) {
                                    dy=0;
                                }
                                mDrawableFloat.set(mDrawableFloat.left, mDrawableFloat.top, mDrawableFloat.right + dx, mDrawableFloat.bottom + dy);
                                break;

                            case EDGE_MOVE_IN:
                                isTouchInSquare = mDrawableFloat.contains((int) event.getX(), (int) event.getY());
                                if (isTouchInSquare) {
                                    if(mDrawableFloat.right+dx>=getWidth()) dx=0;
                                    if(mDrawableFloat.left+dx<=0) dx=0;
                                    if(mDrawableFloat.top+dy<=0) dy=0;
                                    if(mDrawableFloat.bottom+dy>=getHeight()) dy=0;
                                    mDrawableFloat.offset(dx, dy);
                                }
                                break;

                            case EDGE_MOVE_OUT:
                                break;
                        }
                        mDrawableFloat.sort();
                        invalidate();
                    }
                }
                break;
        }
        return true;
    }

    public int getTouch(int eventX, int eventY) {
        Rect mFloatDrawableRect = mFloatDrawable.getBounds();
        int mFloatDrawableWidth = mFloatDrawable.getBorderWidth();
        int mFloatDrawableHeight = mFloatDrawable.getBorderHeight();
        if (mFloatDrawableRect.left <= eventX && eventX < (mFloatDrawableRect.left + mFloatDrawableWidth) && mFloatDrawableRect.top <= eventY && eventY < (mFloatDrawableRect.top + mFloatDrawableHeight)) {
            return EDGE_LT;
        } else if ((mFloatDrawableRect.right - mFloatDrawableWidth) <= eventX
                && eventX < mFloatDrawableRect.right
                && mFloatDrawableRect.top <= eventY
                && eventY < (mFloatDrawableRect.top + mFloatDrawableHeight)) {
            return EDGE_RT;
        } else if (mFloatDrawableRect.left <= eventX
                && eventX < (mFloatDrawableRect.left + mFloatDrawableWidth)
                && (mFloatDrawableRect.bottom - mFloatDrawableHeight) <= eventY
                && eventY < mFloatDrawableRect.bottom) {
            return EDGE_LB;
        } else if ((mFloatDrawableRect.right - mFloatDrawableWidth) <= eventX
                && eventX < mFloatDrawableRect.right
                && (mFloatDrawableRect.bottom - mFloatDrawableHeight) <= eventY
                && eventY < mFloatDrawableRect.bottom) {
            return EDGE_RB;
        } else if (mFloatDrawableRect.contains(eventX, eventY)) {
            return EDGE_MOVE_IN;
        }
        return EDGE_MOVE_OUT;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mDrawable == null) return;
        if (mDrawable.getIntrinsicWidth() == 0 || mDrawable.getIntrinsicHeight() == 0) return;
        configureBounds();
        mDrawable.draw(canvas);
        canvas.save();
        canvas.clipRect(mDrawableFloat, Region.Op.DIFFERENCE);
        canvas.drawColor(Color.parseColor("#44000000"));
        canvas.restore();
        mFloatDrawable.draw(canvas);
    }

    protected void configureBounds() {
        if (isFirst) {
            int left = 0;
            int top = 0;
            int right = getWidth();
            int bottom = getHeight();
            mDrawableSrc.set(left, top, right, bottom);
            mDrawableDst.set(mDrawableSrc);
            int floatLeft = (X*getWidth())/mainSteamWidth;
            int floatTop = (Y*getHeight())/mainSteamHeight;
            int floatRight= (getWidth()*cropWidth)/mainSteamWidth+floatLeft;
            int floatBottoom=(getHeight()*cropHeight)/mainSteamHeight+floatTop;
            mDrawableFloat.set(floatLeft, floatTop, floatRight, floatBottoom);
            isFirst = false;
        } else if (getTouch((int) mX_1, (int) mY_1) == EDGE_MOVE_IN) {
            if (mDrawableFloat.left < 0) {
                mDrawableFloat.right = mDrawableFloat.width();
                mDrawableFloat.left = 0;
            }
            if (mDrawableFloat.top < 0) {
                mDrawableFloat.bottom = mDrawableFloat.height();
                mDrawableFloat.top = 0;
            }
            if (mDrawableFloat.right > getWidth()) {
                mDrawableFloat.left = getWidth() - mDrawableFloat.width();
                mDrawableFloat.right = getWidth();
            }
            if (mDrawableFloat.bottom > getHeight()) {
                mDrawableFloat.top = getHeight() - mDrawableFloat.height();
                mDrawableFloat.bottom = getHeight();
            }
            mDrawableFloat.set(mDrawableFloat.left, mDrawableFloat.top, mDrawableFloat.right,
                    mDrawableFloat.bottom);
        } else {
            if (mDrawableFloat.left < 0) {
                mDrawableFloat.left = 0;
            }
            if (mDrawableFloat.top < 0) {
                mDrawableFloat.top = 0;
            }
            if (mDrawableFloat.right > getWidth()) {
                mDrawableFloat.right = getWidth();
                mDrawableFloat.left = getWidth() - mDrawableFloat.width();
            }
            if (mDrawableFloat.bottom > getHeight()) {
                mDrawableFloat.bottom = getHeight();
                mDrawableFloat.top = getHeight() - mDrawableFloat.height();
            }
            mDrawableFloat.set(mDrawableFloat.left, mDrawableFloat.top, mDrawableFloat.right,
                    mDrawableFloat.bottom);
        }

        mDrawable.setBounds(mDrawableDst);
        mFloatDrawable.setBounds(mDrawableFloat);
    }

    public Bitmap getCropImage() {
        Bitmap tmpBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(tmpBitmap);
        mDrawable.draw(canvas);

        Matrix matrix = new Matrix();
        float scale = (float) (mDrawableSrc.width())
                / (float) (mDrawableDst.width());
        matrix.postScale(scale, scale);

        Bitmap ret = Bitmap.createBitmap(tmpBitmap, mDrawableFloat.left,
                mDrawableFloat.top, mDrawableFloat.width(),
                mDrawableFloat.height(), matrix, true);
        tmpBitmap.recycle();
        return ret;
    }

    public int dipToPx(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }


    public int mainSteamWidth;
    public int mainSteamHeight;

    public void getMainSteam(int u32Resolution) {
        switch (u32Resolution) {
            case 0:
                mainSteamWidth = 640;
                mainSteamHeight = 480;
                break;
            case 1:
                mainSteamWidth = 320;
                mainSteamHeight = 240;
                break;
            case 2:
                mainSteamWidth = 160;
                mainSteamHeight = 112;
                break;
            case 4:
                mainSteamWidth = 352;
                mainSteamHeight = 288;
                break;
            case 3:
                mainSteamWidth = 704;
                mainSteamHeight = 576;
                break;
            case 6:
                mainSteamWidth = 1280;
                mainSteamHeight = 720;
                break;
            case 7:
                mainSteamWidth = 640;
                mainSteamHeight = 352;
                break;
            case 8:
                mainSteamWidth = 320;
                mainSteamHeight = 176;
                break;
            case 14:
                mainSteamWidth = 1280;
                mainSteamHeight = 960;
                break;
            case 13:
                mainSteamWidth = 1920;
                mainSteamHeight = 1080;
                break;
            case 15:
                mainSteamWidth = 1536;
                mainSteamHeight = 1536;
                break;
            case 16:
                mainSteamWidth = 2560;
                mainSteamHeight = 1440;
                break;
            case 17:
                mainSteamWidth = 800;
                mainSteamHeight = 448;
                break;
            case 18:
                mainSteamWidth = 800;
                mainSteamHeight= 600;
                break;
            case 19:
                mainSteamWidth = 2304;
                mainSteamHeight = 1296;
                break;
            case 20:
                mainSteamWidth = 2560;
                mainSteamHeight = 1920;
                break;
              default:
                  mainSteamWidth = (int) (getScreenWidth()*0.9);
                  mainSteamHeight = (int) (mainSteamWidth/1.5);
                  break;

        }
        invalidate();
    }

    private int getScreenWidth() {
        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }


    public void setArea(int area) {
        switch (area) {
            case 1:
                mDrawableFloat.set(0, 0, getWidth()/2, getHeight());
                break;
            case 2:
                mDrawableFloat.set(0, 0, getWidth(), getHeight());
                break;
            case 3:
                mDrawableFloat.set(getWidth()/2, 0, getWidth(), getHeight());
                break;
        }
        invalidate();
    }

    public int getmDrawableFloatX() {
        return (mainSteamWidth*mDrawableFloat.left)/getWidth();
    }

    public int getmDrawableFloatY() {
        return (mainSteamHeight*mDrawableFloat.top)/getHeight();
    }

    public int getmDrawableFloatWidth() {
        return (mainSteamWidth*mDrawableFloat.width())/getWidth();
    }

    public int getmDrawableFloatHeight() {
        return (mainSteamHeight*mDrawableFloat.height())/getHeight();
    }
}
