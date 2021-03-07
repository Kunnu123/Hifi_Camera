package com.hificamera.thecamhi.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharePreUtils {
	public static Boolean putString(String preName, Context context, String key, String values) {
		SharedPreferences shared = context.getSharedPreferences(preName, context.MODE_PRIVATE);
		Editor editor = shared.edit();
		editor.putString(key, values);
		return editor.commit();
	}

	public static Boolean putInt(String preName, Context context, String key, int values) {
		SharedPreferences shared = context.getSharedPreferences(preName, context.MODE_PRIVATE);
		Editor editor = shared.edit();
		editor.putInt(key, values);
		return editor.commit();
	}

	public static String getString(String preName, Context context, String key) {
		SharedPreferences shared = context.getSharedPreferences(preName, context.MODE_PRIVATE);
		return shared.getString(key, "");
	}

	public static float getFloat(String preName, Context context, String key) {
		SharedPreferences shared = context.getSharedPreferences(preName, context.MODE_PRIVATE);
		return shared.getFloat(key,0);
	}


	public static boolean putFloat(String preName, Context context, String key,float value) {
		SharedPreferences shared = context.getSharedPreferences(preName, context.MODE_PRIVATE);
		Editor editor = shared.edit();
		editor.putFloat(key, value);
		return editor.commit();
	}


	public static int getInt(String preName, Context context, String key) {
		SharedPreferences shared = context.getSharedPreferences(preName, context.MODE_PRIVATE);
		return shared.getInt(key, -1);

	}

	public static boolean putBoolean(String preName, Context context, String key, boolean value) {
		SharedPreferences pre = context.getSharedPreferences(preName, Context.MODE_PRIVATE);
		Editor editor = pre.edit();
		editor.putBoolean(key, value);
		return editor.commit();
	}

	public static boolean getBoolean(String preName, Context context, String key) {
		SharedPreferences pre = context.getSharedPreferences(preName, Context.MODE_PRIVATE);
		return pre.getBoolean(key, false);
	}


	public static void removeKey(String preName, Context context, String key) {
		SharedPreferences shared = context.getSharedPreferences(preName, context.MODE_PRIVATE);
		Editor editor = shared.edit();
		editor.remove(key);
		editor.commit();
	}


	public static boolean isHaveKey(String preName, Context context, String key){
		SharedPreferences shared = context.getSharedPreferences(preName, context.MODE_PRIVATE);
		return shared.contains(key);
	}

}
