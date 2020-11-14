package com.bitgam.app.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import com.stfalcon.imageviewer.StfalconImageViewer;
import com.stfalcon.imageviewer.listeners.OnDismissListener;
import com.stfalcon.imageviewer.loader.ImageLoader;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import com.bitgam.app.R;

public class ImageViewer extends AppCompatActivity {

    List<String> bitmapArr = new ArrayList<String>();
    Bitmap pic;
    String picPath;
    private StfalconImageViewer.Builder<String> a;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intn = getIntent();
        picPath = intn.getStringExtra("path");

        setContentView(R.layout.activity_image_viewer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bitmapArr.add(picPath);
        a = (new StfalconImageViewer.Builder<String>((Context) this, bitmapArr, new ImageLoaderClass())).withDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        a.show();
    }

    private class ImageLoaderClass implements ImageLoader {

        @Override
        public void loadImage(ImageView imageView, Object image) {
            imageView.setImageBitmap(BitmapFactory.decodeFile((String) image));
        }
    }

}