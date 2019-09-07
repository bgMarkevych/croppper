package com.office.photoedittoolapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import com.office.photoedittoolapp.view.EditPhotoView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditPhotoView editPhotoView = findViewById(R.id.editView);
        final Bitmap finalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        editPhotoView.setOriginBitmap(finalBitmap);
        ImageView imageView = findViewById(R.id.temp);
        Button erase = findViewById(R.id.erase);
        erase.setOnClickListener(v -> {
            if (editPhotoView.isEraseMode()){
                editPhotoView.disableEraseMode();
            } else {
                editPhotoView.enableEraseMode();
            }
        });
        Button crop = findViewById(R.id.crop);
        Button getImage = findViewById(R.id.get_image);
        crop.setOnClickListener(v -> {
            if (editPhotoView.isCroppingMode()){
                editPhotoView.disableCropMode();
            } else {
                editPhotoView.enableCropMode();
            }
        });
        getImage.setOnClickListener(v -> {
            if (editPhotoView.isCroppingMode()){
                imageView.setImageBitmap(editPhotoView.getCroppedImage());
            } else {
                imageView.setImageBitmap(editPhotoView.getEraseResult());
            }
        });
    }
}
