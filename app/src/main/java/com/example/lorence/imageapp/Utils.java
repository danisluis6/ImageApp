package com.example.lorence.imageapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Calendar;

/**
 * Created by lorence on 25/09/2017.
 */

public class Utils {

    private static long lastClickTime = 0;

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    public static boolean checkPermission(Activity activity) {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==  PackageManager.PERMISSION_GRANTED;
    }

    public static void settingPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
            new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, Constants.PERMISSION
        );
    }

    public static boolean isDoubleClick() {
        long clickTime = System.currentTimeMillis();
        if (clickTime - lastClickTime < Constants.DOUBLE_CLICK_TIME_DELTA) {
            lastClickTime = clickTime;
            return true;
        }
        lastClickTime = clickTime;
        return false;
    }

    public static void scanFileInStorage(Uri fileUri) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        mediaScanIntent.setData(fileUri);
    }


    /**
     * Create a new folder in storage and save image into storage.
     */
    public static String storageImageLocalDevice(String encodeImage, Context context) {
        String path = Constants.EMPTY_STRING;
        if (encodeImage == null) {
            path = Constants.EMPTY_STRING;
        } else {
            if (context instanceof Activity) {
                if (encodeImage.equals(Constants.EMPTY_STRING)) {
                    path = Constants.EMPTY_STRING;
                } else {
                    File directoryFolder = new File(Environment.getExternalStorageDirectory(), Constants.IMAGE_FOLDER_PATH);
                    directoryFolder.mkdirs();
                    Calendar calendar = Calendar.getInstance();
                    try {
                        OutputStream fOut;
                        File file = new File(directoryFolder.getAbsolutePath(), "temp_" + calendar.getTimeInMillis() + "_.jpg");
                        file.createNewFile();
                        fOut = new FileOutputStream(file);
                        fOut.flush();
                        fOut.close();
                        path = file.getAbsolutePath();
                    } catch (Exception e) {
                        path = Constants.EMPTY_STRING;
                    }
                }
            }
        }
        return path;
    }

    public static void resetChildInDirectory() {
        File directoryFolder = new File(Environment.getExternalStorageDirectory(), Constants.IMAGE_FOLDER_PATH);
        if (directoryFolder.exists()) {
            if (directoryFolder.isDirectory()) {
                if (directoryFolder.list() != null) {
                    String[] children = directoryFolder.list();
                    for (String aChildren : children) {
                        new File(directoryFolder, aChildren).delete();
                    }
                }
            }
        }
    }
}
