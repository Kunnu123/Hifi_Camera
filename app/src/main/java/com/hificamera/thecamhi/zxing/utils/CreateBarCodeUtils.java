package com.hificamera.thecamhi.zxing.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.text.Html;
import android.view.Gravity;
import android.view.View.MeasureSpec;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CreateBarCodeUtils {
	public static Bitmap creatBarcode(Context context, String dataStr,
									  int barCodedWidth, int barCodeHeight, boolean displayCode) {
		Bitmap ruseltBitmap = null;
		int marginW = 10;
		BarcodeFormat barcodeFormat = BarcodeFormat.CODE_128;

		if (displayCode) {
			Bitmap barcodeBitmap = encodeAsBitmap(dataStr,
					barcodeFormat,
					barCodedWidth + 2 * marginW,
					barCodeHeight);
			Bitmap codeBitmap = creatCodeBitmap(dataStr,
					barCodedWidth + 2* marginW,
					barCodeHeight, context);
			ruseltBitmap = mixtureBitmap(barcodeBitmap, codeBitmap, new PointF(
					0, barCodeHeight));
		} else {
			ruseltBitmap = encodeAsBitmap(dataStr, barcodeFormat,
					barCodedWidth, barCodeHeight);
		}

		return ruseltBitmap;
	}

	public static Bitmap encodeAsBitmap(String contents, BarcodeFormat format,
										int desiredWidth, int desiredHeight) {
		final int WHITE = 0x000000FF;
		final int BLACK = 0xFF000000;

		MultiFormatWriter writer = new MultiFormatWriter();
		BitMatrix result = null;
		try {
			result = writer.encode(contents, format, desiredWidth,
					desiredHeight, null);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		int width = result.getWidth();
		int height = result.getHeight();
		int[] pixels = new int[width * height];
		// All are 0, or black, by default
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				pixels[offset + x] = result.get(x, y) ? BLACK : WHITE;
			}
		}

		Bitmap bitmap = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;
	}

	public static Bitmap creatCodeBitmap(String contents, int width,
										 int height, Context context) {
		TextView tv = new TextView(context);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		tv.setLayoutParams(layoutParams);
		tv.setText(contents);
		// tv.setTextSize(Utils.px2sp(context, height/3));
		// tv.setText(Html.fromHtml(contents));
		// tv.setHeight(height);
		tv.setWidth(width);//
		tv.setGravity(Gravity.CENTER_HORIZONTAL);
		tv.setDrawingCacheEnabled(true);
		tv.setTextColor(Color.BLACK);
		tv.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
				MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
		tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());

		tv.buildDrawingCache();
		Bitmap bitmapCode = tv.getDrawingCache();
		return bitmapCode;
	}

	public static Bitmap mixtureBitmap(Bitmap first, Bitmap second,
									   PointF fromPoint) {
		if (first == null || second == null || fromPoint == null) {
			return null;
		}
		int marginW = 10;
		Bitmap newBitmap = Bitmap.createBitmap(
				// first.getWidth() + second.getWidth() +
				// marginW,
				first.getWidth() + marginW * 2,
				first.getHeight() + second.getHeight(), Config.ARGB_4444);
		Canvas cv = new Canvas(newBitmap);
		cv.drawBitmap(first, marginW, 0, null);
		cv.drawBitmap(second, fromPoint.x, fromPoint.y, null);
		cv.save();
		cv.restore();

		return newBitmap;
	}
}
