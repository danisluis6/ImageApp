package com.example.lorence.imageapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener, IImageView {

    private LinearLayout lnCamera;
    private CircleImageView imvCamera;
    private File file;
    private String path = Constants.EMPTY_STRING;
    private final static String TAG = ImageActivity.class.getSimpleName();

    @Override
    public void initView() {
        imvCamera = (CircleImageView) this.findViewById(R.id.imvCamera);
        lnCamera = (LinearLayout) this.findViewById(R.id.lnCamera);
    }

    @Override
    public void initAttributes() {

    }

    @Override
    public void initOnListener() {
        imvCamera.setOnClickListener(this);
        lnCamera.setOnClickListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        initView();
        initOnListener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.PERMISSION:
                for (int permissionId : grantResults) {
                    if (permissionId != PackageManager.PERMISSION_GRANTED) {
                        Utils.showToast(this, getString(R.string.error_permission));
                        return;
                    }
                }
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, Constants.CAMERA_REQUEST);
                break;
        }
    }

    @Override
    public void onClick(View view) {
        if (Utils.isDoubleClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.imvCamera:
                break;
            case R.id.lnCamera:
                takePhotoByCamera();
                break;
        }
    }

    private void takePhotoByCamera() {
        if (Utils.checkPermission(this)) {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, Constants.CAMERA_REQUEST);
        } else {
            Utils.settingPermission(this);
            return;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.CAMERA_REQUEST:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        try {
//                            if (data.getData() != null) {
//                                final Uri uri = data.getData();
//                                // Get path of image from Gallery
//                                Utils.scanFileInStorage(uri);
//                                String selectedImagePath = getRealPathFromURI(uri);
//                                path = new File(selectedImagePath).getAbsolutePath();
//                                Log.i("TAG", "path: "+path);
//                            } else {
//                                Bitmap photo = (Bitmap) data.getExtras().get("data");
//                                Uri tempUri = getImageUri(this, photo);
//                                Utils.scanFileInStorage(tempUri);
//                                File temp = new File(getRealPathFromURI(tempUri));
//                                path = temp.getAbsolutePath();
//                            }

                            Bitmap photo = (Bitmap) data.getExtras().get("data");
                            Uri tempUri = getImageUri(this, photo);
                            Utils.scanFileInStorage(tempUri);
                            File temp = new File(getRealPathFromURI(tempUri));
                            path = temp.getAbsolutePath();
                            Bitmap demo = BitmapFactory.decodeFile(path);
                            Log.i("TAG", "original.width: "+demo.getWidth());
                            Log.i("TAG", "original.height: "+demo.getHeight());
                            Log.i("TAG", "length: "+new File(path).length());
//                            file = new File(path);
//                            Bitmap bitmap = MethodScaleImage.getScaledBitmap(file, 250);
//                            if (imvCamera != null) {
//                                imvCamera.setImageBitmap(bitmap);
//                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = this.getContentResolver().query(contentUri, proj, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                return cursor.getString(column_index);
            }
            cursor.close();
        }
        return null;
    }

    public Uri getImageUri(Context inContext, Bitmap bitmap) {
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), bitmap, null, null);
        return Uri.parse(path);
    }
}
