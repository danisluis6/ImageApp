package com.example.lorence.imageapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * Created by lorence on 25/09/2017.
 */

public class MethodScaleImage {

    public static Bitmap getScaledBitmap(File temp, int size) {
        try {
            Bitmap scaled = decodeSampledBitmapFromResourceMemOpt(new FileInputStream(temp), size);
            if (scaled != null) {
                Matrix mat = new Matrix();
                mat.postRotate(getOrientationFromExif(temp.getPath()));
                return Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), mat, true);
            }
            return scaled;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Bitmap decodeSampledBitmapFromResourceMemOpt(
            InputStream inputStream/*, int reqWidth, int reqHeight*/, int size) {
        byte[] byteArr = new byte[0];
        byte[] buffer = new byte[1024];
        int len;
        int count = 0;
        try {
            while ((len = inputStream.read(buffer)) > -1) {
                if (len != 0) {
                    if (count + len > byteArr.length) {
                        byte[] newbuf = new byte[(count + len) * 2];
                        System.arraycopy(byteArr, 0, newbuf, 0, count);
                        byteArr = newbuf;
                    }
                    System.arraycopy(buffer, 0, byteArr, count, len);
                    count += len;
                }
            }
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(byteArr, 0, count, options);
            options.inSampleSize = calculateInSampleSize(options/*, reqWidth,
                    reqHeight*/, size);
            Log.i("TAG", "options.inSampleSize: " +options.inSampleSize);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeByteArray(byteArr, 0, count, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int getOrientationFromExif(String imagePath) {
        int orientation = 0;
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int exifOrientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);

            switch (exifOrientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    orientation = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    orientation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    orientation = 90;
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                    orientation = 0;
                    break;
                default:
                    break;

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return orientation;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options/*, int reqWidth, int reqHeight*/, int size) {
        // Raw height and width of image
        int height = options.outHeight;
        int width = options.outWidth;
        int inSampleSize = 1;
        // The new size we want to scale to
        if (height >= width) {
            if (height > size) {
                inSampleSize = Math.round((float) (height / size));
            }
        } else {
            if (width > size) {
                inSampleSize = Math.round((float) (width / size));
            }
        }
        return inSampleSize;
    }
}
