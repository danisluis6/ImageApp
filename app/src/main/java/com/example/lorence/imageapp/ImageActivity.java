package com.example.lorence.imageapp;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

public class ImageActivity extends AppCompatActivity implements View.OnClickListener, IImageView {

    private LinearLayout lnCamera;
    private CircleImageView imvCamera;
    private File file;
    private String path = Constants.EMPTY_STRING;
    private final static String TAG = ImageActivity.class.getSimpleName();
    private Uri imageToUploadUri;

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
            Intent chooserIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File f = new File(Environment.getExternalStorageDirectory(), "POST_IMAGE.jpg");
            chooserIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
            imageToUploadUri = Uri.fromFile(f);
            startActivityForResult(chooserIntent, Constants.CAMERA_REQUEST);
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
                            if (imageToUploadUri != null) {
                                Uri selectedImage = imageToUploadUri;
                                getContentResolver().notifyChange(selectedImage, null);
                                Bitmap reducedSizeBitmap = getBitmap(imageToUploadUri.getPath());
                                if (reducedSizeBitmap != null) {
                                    imvCamera.setImageBitmap(reducedSizeBitmap);
                                } else {
                                    Toast.makeText(this, "Error while capturing Image", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Toast.makeText(this, "Error while capturing Image", Toast.LENGTH_LONG).show();
                            }
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

    private Bitmap getBitmap(String path) {

        Uri uri = Uri.fromFile(new File(path));
        InputStream in = null;
        try {
            final int IMAGE_MAX_SIZE = 20 * 1024; // 1.2MP
            in = getContentResolver().openInputStream(uri);

            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(in, null, o);
            in.close();


            int scale = 1;
            while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) >
                    IMAGE_MAX_SIZE) {
                scale++;
            }
            Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight);

            Bitmap b = null;
            in = getContentResolver().openInputStream(uri);
            if (scale > 1) {
                scale--;
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                b = BitmapFactory.decodeStream(in, null, o);

                // resize to desired dimensions
                int height = b.getHeight();
                int width = b.getWidth();
                Log.d("", "1th scale operation dimenions - width: " + width + ", height: " + height);

                double y = Math.sqrt(IMAGE_MAX_SIZE
                        / (((double) width) / height));
                double x = (y / height) * width;

                Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x,
                        (int) y, true);
                b.recycle();
                b = scaledBitmap;

                System.gc();
            } else {
                b = BitmapFactory.decodeStream(in);
            }
            in.close();

            Log.d("", "bitmap size - width: " + b.getWidth() + ", height: " +
                    b.getHeight());
            return b;
        } catch (IOException e) {
            Log.e("", e.getMessage(), e);
            return null;
        }
    }
}
