package com.hificamera.thecamhi.widget.toast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.view.View;
import android.widget.Toast;

import java.lang.reflect.Field;

public class ToastCompat extends Toast {

    private final @NonNull Toast toast;

    private ToastCompat(Context context, @NonNull Toast base) {
        super(context);
        this.toast = base;
    }


    public static ToastCompat makeText(Context context, CharSequence text, int duration) {
        // We cannot pass the SafeToastContext to Toast.makeText() because
        // the View will unwrap the base context and we are in vain.
        @SuppressLint("ShowToast")
        Toast toast = Toast.makeText(context, text, duration);
        setContextCompat(toast.getView(), new SafeToastContext(context, toast));
        return new ToastCompat(context, toast);
    }


    public static Toast makeText(Context context, @StringRes int resId, int duration)
            throws Resources.NotFoundException {
        return makeText(context, context.getResources().getText(resId), duration);
    }


    public @NonNull ToastCompat setBadTokenListener(@NonNull BadTokenListener listener) {
        final Context context = getView().getContext();
        if (context instanceof SafeToastContext) {
            ((SafeToastContext) context).setBadTokenListener(listener);
        }
        return this;
    }


    @Override
    public void show() {
        toast.show();
    }


    @Override
    public void setDuration(int duration) {
        toast.setDuration(duration);
    }


    @Override
    public void setGravity(int gravity, int xOffset, int yOffset) {
        toast.setGravity(gravity, xOffset, yOffset);
    }


    @Override
    public void setMargin(float horizontalMargin, float verticalMargin) {
        toast.setMargin(horizontalMargin, verticalMargin);
    }


    @Override
    public void setText(int resId) {
        toast.setText(resId);
    }


    @Override
    public void setText(CharSequence s) {
        toast.setText(s);
    }


    @Override
    public void setView(View view) {
        toast.setView(view);
        setContextCompat(view, new SafeToastContext(view.getContext(), this));
    }


    @Override
    public float getHorizontalMargin() {
        return toast.getHorizontalMargin();
    }


    @Override
    public float getVerticalMargin() {
        return toast.getVerticalMargin();
    }


    @Override
    public int getDuration() {
        return toast.getDuration();
    }


    @Override
    public int getGravity() {
        return toast.getGravity();
    }


    @Override
    public int getXOffset() {
        return toast.getXOffset();
    }


    @Override
    public int getYOffset() {
        return toast.getYOffset();
    }


    @Override
    public View getView() {
        return toast.getView();
    }


    public @NonNull Toast getBaseToast() {
        return toast;
    }


    private static void setContextCompat(@NonNull View view, @NonNull Context context) {
        if (Build.VERSION.SDK_INT == 25) {
            try {
                Field field = View.class.getDeclaredField("mContext");
                field.setAccessible(true);
                field.set(view, context);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }
}
