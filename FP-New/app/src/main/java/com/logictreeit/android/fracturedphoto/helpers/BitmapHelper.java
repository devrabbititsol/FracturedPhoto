package com.logictreeit.android.fracturedphoto.helpers;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Build;

/**
 * A helper class for all the operations related to bitmap
 * @author Santhosh Annam
 *
 */
public class BitmapHelper {

	/**
	 * It does not give the OOM Exception, we can use this to show the image on
	 * a particular imageview. But It have certain limitation, After the decode
	 * we can not make the scale of this bitmap. And Its size will be same as a
	 * large, so it makes slow to show in the list view.
	 * 
	 * @param ImagePath
	 * 
	 * @return Returns a bitmap with its original size. But you cannot make the
	 *         scale of this bitmap.
	 */
	public static Bitmap decodeBitmapFromPath(String path) {

		Bitmap bitmap = null;
		BitmapFactory.Options bfOptions = new BitmapFactory.Options();
		bfOptions.inDither = false; // Disable Dithering mode
		bfOptions.inPurgeable = true; // Tell to gc that whether it needs free
		// memory, the Bitmap can be cleared
		bfOptions.inInputShareable = true; // Which kind of reference will be
		// used to recover the Bitmap data
		// after being clear, when it will
		// be used in the future
		bfOptions.inTempStorage = new byte[32 * 1024];

		File file = new File(path);
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			if (fs != null) {
				bitmap = BitmapFactory.decodeFileDescriptor(fs.getFD(), null,
						bfOptions);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fs != null) {
				try {
					fs.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return bitmap;
	}

	/**
	 * IT WILL NEVER GIVE OOM EXCEPTION. This one is the best and optimized
	 * solution. Use this method If you want to make a small image from large
	 * image with height and width like 640 and 480.
	 * 
	 * @param imagePath
	 * @param reqWidth
	 * @param reqHeight
	 * @param orientation
	 * @return returns a bitmap with desired width, height.
	 */
	public static Bitmap decodeBitmapFromPath(String imagePath, int reqWidth, int reqHeight, int orientation) {

		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;

		BitmapFactory.decodeFile(imagePath, options);

		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

		System.err.println(imagePath);
		System.err.println("Calculated inSampleSize = "+options.inSampleSize);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;
		Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);
		if (orientation != 0) {
			return getRotatedBitmap(bmp, orientation);
		}
		if(bmp == null){
			System.err.println("Returning NULL");
		}
		return bmp;
	}

	private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {
			if (width > height) {
				inSampleSize = Math.round((float) height / (float) reqHeight);
			} else {
				inSampleSize = Math.round((float) width / (float) reqWidth);
			}
		}
		return inSampleSize;
	}

	public static int getBitmapSizeInBytes(Bitmap bitmap) {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
			return bitmap.getAllocationByteCount();
		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
			return bitmap.getByteCount();
		} else {
			return bitmap.getRowBytes() * bitmap.getHeight();
		}
	}

	public static Bitmap getRotatedBitmap(Bitmap source, float angle) {
		Matrix matrix = new Matrix();
		matrix.setRotate(angle, 0, 0);
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}

	public static Bitmap getResizedBitmap(Bitmap source, int newWidth, int newHeight) throws NullPointerException {
		if (source != null) {
			int width = source.getWidth();
			int height = source.getHeight();
			// create a matrix for the manipulation
			Matrix matrix = new Matrix();
			// resize the bitmap
			matrix.postScale(((float) newWidth) / width, ((float) newHeight) / height);
			// recreate the new Bitmap
			return Bitmap.createBitmap(source, 0, 0, width, height, matrix, true);
		} else {
			throw new NullPointerException("Inputted Bitmap is NULL");
		}
	}

	public static byte[] getByteArrayOfBitmap(Bitmap bitmap) {
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
		return stream.toByteArray();
	}

	public static Bitmap decodeFile(String filePath) {
		try {
			// decode image size
			BitmapFactory.Options op = new BitmapFactory.Options();
			op.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(new FileInputStream(filePath), null, op);

			// Find the correct scale value. It should be the power of 2.

			final int REQUIRED_SIZE = 70;
			int width_tmp = op.outWidth, height_tmp = op.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp / 2 < REQUIRED_SIZE
						|| height_tmp / 2 < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale++;
			}

			// decode with inSampleSize
			BitmapFactory.Options op2 = new BitmapFactory.Options();
			op2.inSampleSize = scale;
			return BitmapFactory.decodeStream(new FileInputStream(filePath),
					null, op2);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getBitmapFromByteArray(byte[] imageArray) {
		return BitmapFactory.decodeByteArray(imageArray, 0, imageArray.length);
	}

	public static void writeBitmapIntoFile(Context c, Bitmap bitmap, String filePath) {
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(filePath);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try{
				out.close();
			} catch(Throwable ignore) {}
		}
	}
}
