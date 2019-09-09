package com.office.photoedittoolapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.office.photoedittoolapp.view.EditPhotoView;
import com.office.photoedittoolapp.view.PhotoEditor;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PhotoEditor editPhotoView = findViewById(R.id.editView);
        final Bitmap finalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.white);
        editPhotoView.setOriginBitmap(finalBitmap);
        editPhotoView.setEraseMode(true);

        SeekBar seekBrightness = findViewById(R.id.brightness);
        SeekBar seekContrast = findViewById(R.id.contrast);
        seekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editPhotoView.changeBrightnessAndContrast(progress, seekContrast.getProgress());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                editPhotoView.saveStartBrightnessAndContrast(seekBrightness.getProgress(), seekContrast.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editPhotoView.saveFinishBrightnessAndContrast(seekBrightness.getProgress(), seekContrast.getProgress());
            }
        });
        seekContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editPhotoView.changeBrightnessAndContrast(seekBrightness.getProgress(), progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                editPhotoView.saveStartBrightnessAndContrast(seekBrightness.getProgress(), seekContrast.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                editPhotoView.saveFinishBrightnessAndContrast(seekBrightness.getProgress(), seekContrast.getProgress());
            }
        });
        editPhotoView.setUndoReundoListener(new PhotoEditor.UndoReundoListener() {
            @Override
            public void undo(int brightness, float contrast) {
                seekBrightness.setProgress(brightness + 255);
                seekContrast.setProgress((int) (contrast * 100));
            }

            @Override
            public void reundo(int brightness, float contrast) {

            }
        });
//        ImageView imageView = findViewById(R.id.temp);
//        Button erase = findViewById(R.id.erase);
//        erase.setOnClickListener(v -> {
//            if (editPhotoView.isEraseMode()){
//                editPhotoView.disableEraseMode();
//            } else {
//                editPhotoView.enableEraseMode();
//            }
//        });
//        Button crop = findViewById(R.id.crop);
//        Button getImage = findViewById(R.id.get_image);
//        crop.setOnClickListener(v -> {
//            if (editPhotoView.isCroppingMode()){
//                editPhotoView.disableCropMode();
//            } else {
//                editPhotoView.enableCropMode();
//            }
//        });
//        getImage.setOnClickListener(v -> {
//            if (editPhotoView.isCroppingMode()){
//                imageView.setImageBitmap(editPhotoView.getCroppedImage());
//            } else {
//                imageView.setImageBitmap(editPhotoView.getEraseResult());
//            }
//        });
        Button right = findViewById(R.id.right);
        right.setOnClickListener(v -> {
            editPhotoView.rotateRight();
        });
        Button left = findViewById(R.id.left);
        left.setOnClickListener(v -> {
            editPhotoView.rotateLeft();
        });
        Button horizontal = findViewById(R.id.horizontal);
        horizontal.setOnClickListener(v -> {
            editPhotoView.flipHorizontal();
        });
        Button vertical = findViewById(R.id.vertical);
        vertical.setOnClickListener(v -> {
            editPhotoView.flipVertical();
        });
        Button undo = findViewById(R.id.undo);
        undo.setOnClickListener(v -> editPhotoView.undo());
    }
}
