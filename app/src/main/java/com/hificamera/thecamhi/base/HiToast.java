package com.hificamera.thecamhi.base;

import android.content.Context;
import android.widget.Toast;

import com.hificamera.thecamhi.widget.toast.ToastCompat;

public class HiToast {
	private static ToastCompat toast;
	public static void showToast(Context context,String str){
		if(toast==null){
			toast=ToastCompat.makeText(context, str, Toast.LENGTH_SHORT);
		}else {
			toast.setText(str);
			toast.setDuration(Toast.LENGTH_SHORT);
		}
		toast.show();
	}
}
