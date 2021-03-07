package com.hificamera.thecamhi.zxing.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.text.TextUtils;
import android.util.Log;

public class EllpsizeUtils_DingChang {

	public static int ascii(String c) {
		byte x[] = new byte[2];
		x = c.getBytes();

		// x=c.getBytes("utf-8");
		// x=c.getBytes(srcBegin, srcEnd, dst, dstBegin);

		if (x == null || x.length > 2 || x.length <= 0) {
			return -1;
		}
		if (x.length == 1) {// 英文字符
			return 1;
		}
		Pattern p=Pattern.compile("[\u4e00-\u9fa5]");
	    Matcher m=p.matcher(c);
	     if(m.matches()){
	    	 return -1;
	     }

		return 0;
	}

	public static String goodStr(String string) {

		string = string.trim();
		return string;
	}
	public static int letterSum(String string) {
		if (null != string) {
			string = goodStr(string);
			if (string.length() <= 0) {
				return 0;
			} else {
				String str;
				double len = 0;
				for (int i = 0; i < string.length(); i++) {
					str = string.substring(i, i + 1);
					if (ascii(str) < 0) {
						len++;
					} else {
						len += 0.5;
					}
					
				}
				Log.e("num", (int) Math.round(len)+";  len="+ len);
				return (int) Math.round(len);
			}
		}
		return 0;

	}
	
	public static int chineseSum(String string) {
		if (!TextUtils.isEmpty(string)) {
			string = goodStr(string);
			if (string.length() <= 0) {
				return 0;
			} else {
				String str;
				double len = 0;
				for (int i = 0; i < string.length(); i++) {
					str = string.substring(i, i + 1);
					if (ascii(str) < 0) {
						len++;
					}
				}
				Log.e("num", (int) Math.round(len)+";  len="+ len);
				return (int) Math.round(len);
			}
		}
		return 0;
	}
	public static String limitStr(String string, int size) {
		if (null != string) {
			string = goodStr(string);
			if (string.length() <= size) {
				return string;
			} else {
				StringBuffer buffer = new StringBuffer();
				String str;
				double len = 0;
				for (int i = 0; i < string.length(); i++) {
					str = string.substring(i, i + 1);
					if (ascii(str) < 0) {
						buffer.append(str);
						len++;
					} else {
						buffer.append(str);
						len += 0.5;
					}
					if (len >= size)
						break;
				}
				return buffer.toString();
			}
		}
		return "";

	}


	public static String limitStr_Ending(String strData, int size, String endStr) {
		strData = goodStr(strData);
		if (size < endStr.length() || strData.length() < endStr.length()) {

			Log.e("endStr is too long","endStr is too long! Please cut it.");
		}
		String  cutStr;
		cutStr = limitStr(strData, size);
		if (cutStr.length()!=strData.length()) {
			cutStr = cutStr.substring(0, cutStr.length() - letterSum(endStr))+ endStr;
		}

		return cutStr;
	}
}
