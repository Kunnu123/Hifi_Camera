package com.hificamera.thecamhi.zxing;
import java.io.FileNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.google.zxing.LuminanceSource;
import com.hificamera.thecamhi.zxing.utils.CreateQRCode_Utils;


public final class RGBLuminanceSource extends LuminanceSource {
	private final byte[] luminances;

	public RGBLuminanceSource(String path) throws FileNotFoundException {
		this(loadBitmap(path));
	}

	public RGBLuminanceSource(Bitmap bitmap) {
		super(bitmap.getWidth(), bitmap.getHeight());
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] pixels = new int[width * height];
		//
		bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		luminances = new byte[width * height];
		for (int y = 0; y < height; y++) {
			int offset = y * width;
			for (int x = 0; x < width; x++) {
				int pixel = pixels[offset + x];
				int r = (pixel >> 16) & 0xff;
				int g = (pixel >> 8) & 0xff;
				int b = pixel & 0xff;
				if (r == g && g == b) {
					// Image is already greyscale, so pick any channel.
					luminances[offset + x] = (byte) r;
				} else {
					// Calculate luminance cheaply, favoring green.
					luminances[offset + x] = (byte) ((r + g + g + b) >> 2);
//					luminances[offset + x] = (byte) ((0.299*r + 0.578*g + 0.114*b));
				}
			}
		}
	}

	@Override
	public byte[] getRow(int y, byte[] row) {
		if (y < 0 || y >= getHeight()) {
			throw new IllegalArgumentException(
					"Requested row is outside the image: " + y);
		}
		int width = getWidth();
		if (row == null || row.length < width) {
			row = new byte[width];
		}
		System.arraycopy(luminances, y * width, row, 0, width);
		return row;
	}

	// Since this class does not support cropping, the underlying byte array
	// already contains
	// exactly what the caller is asking for, so give it to them without a copy.
	@Override
	public byte[] getMatrix() {
		return luminances;
	}

	private static Bitmap loadBitmap(String path) throws FileNotFoundException {
		Bitmap bitmap = BitmapFactory.decodeFile(path);
		if (bitmap == null) {
			throw new FileNotFoundException("Couldn't open " + path);
		}
		return bitmap;
	}
	
    public static final Long SCAN_DEFAULT_SIZE=(long)4000000;
	private static Bitmap loadBitmap(String path, boolean isOkSize) throws FileNotFoundException {
        Bitmap bitmap = null;
        if (isOkSize) {
            bitmap = BitmapFactory.decodeFile(path);
        } else {
            bitmap = CreateQRCode_Utils.compress(path);
        }
        if (bitmap == null) {
            throw new FileNotFoundException("Couldn't open " + path);
        }
        return bitmap;
    }
	
	
}

