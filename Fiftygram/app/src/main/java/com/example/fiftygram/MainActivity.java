package com.example.fiftygram;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.request.RequestOptions;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import jp.wasabeef.glide.transformations.gpu.InvertFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SepiaFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SketchFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.ToonFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.VignetteFilterTransformation;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private ImageView imageView;
    private Bitmap image;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public void choosePhoto(View v) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");   // set file type as any image type
        startActivityForResult(intent, 1);   // start up activity, requestCode identifies where request came from for user return point
    }

    public void apply(Transformation<Bitmap> filter) {
        if (image != null) {
            Glide
                .with(this)
                .load(image)
                .apply(RequestOptions.bitmapTransform(filter))
                .into(imageView);
        }
    }

    public void applyVignette(View v) {
        apply(new VignetteFilterTransformation());
    }
    public void applySepia(View v) { apply(new SepiaFilterTransformation()); }
    public void applyToon(View v) {
        apply(new ToonFilterTransformation());
    }
    public void applyInvert(View v) {
        apply(new InvertFilterTransformation());
    }

//    public void savePhoto(View v) {
//        Bitmap filtered = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
//        MediaStore.Images.Media.insertImage();
//        ContentValues values = new ContentValues();
//        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DCIM);
//        values.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
//        values.put(MediaStore.MediaColumns.IS_PENDING, true);
//
//        Uri uri = context.getContentResolver().insert(externalContentUri, values);
//
//        if (uri != null) {
//            try {
//                if (WriteFileToStream(originalFile, context.getContentResolver().openOutputStream(uri))) {
//                    values.put(MediaStore.MediaColumns.IS_PENDING, false);
//                    context.getContentResolver().update(uri, values, null, null);
//                }
//            } catch (Exception e) {
//                context.getContentResolver().delete( uri, null, null );
//            }
//        }
//    }

    public void saveImageToStorage(View v) throws IOException {
        Bitmap filtered = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        OutputStream imageOutStream;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, "image_screenshot.jpg");
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
            values.put(MediaStore.MediaColumns.IS_PENDING, true);
            Uri uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            imageOutStream = getContentResolver().openOutputStream(uri);
        } else {
            String imagePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();

            int i = 0;
            boolean exists = true;
            String imageName = "filtered.jpg";
            do {
                File file = new File(imagePath, imageName);
                if (!file.exists()) {
                    exists = false;
                } else {
                    imageName = "filtered(" + i + ").jpg";
                    i++;
                }
            } while (exists == true);

            File image = new File(imagePath, imageName);
            imageOutStream = new FileOutputStream(image);
        }

        try {
            Log.e(null, "trying image save");
            filtered.compress(Bitmap.CompressFormat.JPEG, 100, imageOutStream);
        } finally {
            imageOutStream.close();
        }

    }

    @Override  // photo click triggers this function
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) { // requestCode is request code of startActivity
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null) {  // code for error or success
            try {
                Uri uri = data.getData();
                ParcelFileDescriptor parcelFileDescriptor = getContentResolver().openFileDescriptor(uri, "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
                imageView.setImageBitmap(image);
                imageView.getLayoutParams().height = image.getHeight();
            } catch (IOException e) {
                Log.e("cs50", "Image not found", e);
            }
        }
    }
}