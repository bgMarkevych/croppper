package com.office.photoedittoolapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Picture;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.office.photoedittoolapp.tools.BitmapUtils;
import com.office.photoedittoolapp.view.EditPhotoView;
import com.office.photoedittoolapp.view.EditView;
import com.theartofdev.edmodo.cropper.CropImageView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditPhotoView editPhotoView = findViewById(R.id.editView);
        final Bitmap finalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        editPhotoView.setBitmap(finalBitmap);
        ImageView imageView = findViewById(R.id.temp);
        Button crop = findViewById(R.id.crop);
        crop.setOnClickListener(v -> imageView.setImageBitmap(editPhotoView.getCroppedImage()));
//        SeekBar brightness = findViewById(R.id.brightness);
//        SeekBar contrast = findViewById(R.id.contrast);
//        Button button = findViewById(R.id.undo);
//        Button save = findViewById(R.id.save);
//        CropImageView cropImageView;
//        Button rotatePlus = findViewById(R.id.rotatePlus);
//        Button rotateMinus = findViewById(R.id.rotateMinus);
//        rotatePlus.setOnClickListener(v -> editView.rotateImage(90));
//        rotateMinus.setOnClickListener(v -> editView.rotateImage(-90));
//        button.setOnClickListener(v -> editView.undoTask());
//        save.setOnClickListener(v -> editView.saveChanges());
//        brightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                editView.changeBrightnessAndContrast(progress - 255, contrast.getProgress());
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//
//        contrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                editView.changeBrightnessAndContrast(brightness.getProgress() - 255, progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
    }
}
