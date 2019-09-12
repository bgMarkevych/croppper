package com.office.photoedittoolapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;

import com.office.photoedittoolapp.view.PhotoEditor;

public class MainActivity extends AppCompatActivity {

    boolean undo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PhotoEditor editPhotoView = findViewById(R.id.editView);
        final Bitmap finalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.image);
        editPhotoView.setOriginBitmap(finalBitmap);
//        editPhotoView.setEraseMode(true);

        SeekBar seekBrightness = findViewById(R.id.brightness);
        SeekBar seekContrast = findViewById(R.id.contrast);
        seekBrightness.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!undo) {
                    editPhotoView.changeAdjust(progress, seekContrast.getProgress());
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                editPhotoView.saveAdjustBitmapState(seekBrightness.getProgress(), seekContrast.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        seekContrast.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!undo) {
                    editPhotoView.changeAdjust(seekBrightness.getProgress(), progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                editPhotoView.saveAdjustBitmapState(seekBrightness.getProgress(), seekContrast.getProgress());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        editPhotoView.setAdjustUndoReundoListener(new PhotoEditor.AdjustUndoReundoListener() {
            @Override
            public void undo(int brightness, float contrast) {
                undo = true;
                seekBrightness.setProgress(brightness + 255);
                seekContrast.setProgress((int) (contrast * 100));
                undo = false;
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
        Button crop = findViewById(R.id.crop);
//        Button getImage = findViewById(R.id.get_image);
        crop.setOnClickListener(v -> {
            editPhotoView.setCroppingMode(!editPhotoView.isCropModeEnabled());
        });
        Button getImage = findViewById(R.id.get_image);
        ImageView imageView = findViewById(R.id.temp);
        getImage.setOnClickListener(v -> {
            imageView.setImageBitmap(editPhotoView.getImage());
        });
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
        Button reundo = findViewById(R.id.reundo);
        reundo.setOnClickListener(v -> editPhotoView.reundo());
        Button applyCrop = findViewById(R.id.apply_crop);
        applyCrop.setOnClickListener(v -> editPhotoView.applyCrop());
    }
}
