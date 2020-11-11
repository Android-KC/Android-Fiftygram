package com.example.fiftygram;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.Transformation;
import com.bumptech.glide.request.RequestOptions;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import jp.wasabeef.glide.transformations.gpu.SepiaFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SketchFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.ToonFilterTransformation;

public class MainActivity extends AppCompatActivity {
    private ImageView imageView;
    private Bitmap image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.image_view);
    }

    public void choosePhoto(View v) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");   // set file type as any image type
        startActivityForResult(intent, 1);   // start up activity, requestCode identifies where request came from for user return point
    }

    public void apply(Transformation<Bitmap> filter) {
        Glide
            .with(this)
            .load(image)
            .apply(RequestOptions.bitmapTransform(filter))
            .into(imageView);
    }

    public void applySepia(View v) {
//        Glide
//            .with(this)
//            .load(image)
//            .apply(RequestOptions.bitmapTransform(new SepiaFilterTransformation()))
//            .into(imageView);

        apply(new SepiaFilterTransformation());
    }

    public void applyToon(View v) {
        apply(new ToonFilterTransformation());
    }
    public void applySketch(View v) {
        apply(new SketchFilterTransformation());
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
            } catch (IOException e) {
                Log.e("cs50", "Image not found", e);
            }
        }
    }
}