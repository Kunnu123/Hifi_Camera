package com.hificamera.thecamhi.zxing.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

public class UriUtils {
	public static String getRealPathFromUri(Context context, Uri uri) {
		int sdkVersion = Build.VERSION.SDK_INT;
		if (sdkVersion < 11)
			return getRealPathFromUri_BelowApi11(context, uri);
		if (sdkVersion < 19)
			return getRealPathFromUri_Api11To18(context, uri);
		else
			return getRealPathFromUriAboveApi19(context, uri);
	}

	@SuppressLint("NewApi")
	private static String getRealPathFromUriAboveApi19(Context context, Uri uri) {
		String filePath = null;
		if (DocumentsContract.isDocumentUri(context, uri)) {
			String documentId = DocumentsContract.getDocumentId(uri);
			if (isMediaDocument(uri)) { // MediaProvider
				String id = documentId.split(":")[1];
				String selection = MediaStore.Images.Media._ID + "=?";
				String[] selectionArgs = { id };
				filePath = getDataColumn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection, selectionArgs);
			} else if (isDownloadsDocument(uri)) { // DownloadsProvider
				Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(documentId));
				filePath = getDataColumn(context, contentUri, null, null);
			}
		} else if ("content".equalsIgnoreCase(uri.getScheme())) {
			filePath = getDataColumn(context, uri, null, null);
		} else if ("file".equals(uri.getScheme())) {
			filePath = uri.getPath();
		}
		return filePath;
	}

	private static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	private static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
		String path = null;

		String[] projection = new String[] { MediaStore.Images.Media.DATA };
		Cursor cursor = null;
		try {
			cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {
				int columnIndex = cursor.getColumnIndexOrThrow(projection[0]);
				path = cursor.getString(columnIndex);
			}
		} catch (Exception e) {
			if (cursor != null) {
				cursor.close();
			}
		}
		return path;
	}

	private static String getRealPathFromUri_Api11To18(Context context, Uri uri) {
		String filePath = null;
		String[] projection = { MediaStore.Images.Media.DATA };
		CursorLoader loader = new CursorLoader(context, uri, projection, null, null, null);
		Cursor cursor = loader.loadInBackground();

		if (cursor != null) {
			cursor.moveToFirst();
			filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
			cursor.close();
		}
		return filePath;
	}

	private static String getRealPathFromUri_BelowApi11(Context context, Uri uri) {
		String filePath = null;
		String[] projection = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
			filePath = cursor.getString(cursor.getColumnIndex(projection[0]));
			cursor.close();
		}
		return filePath;
	}

}
