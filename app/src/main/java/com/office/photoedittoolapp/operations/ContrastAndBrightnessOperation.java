package com.office.photoedittoolapp.operations;

import android.graphics.Bitmap;

import com.office.photoedittoolapp.tools.BitmapUtils;

public class ContrastAndBrightnessOperation implements EditOperation {

    public ContrastAndBrightnessOperation(float prevContrast, int prevBrightness) {
        this.prevContrast = prevContrast;
        this.prevBrightness = prevBrightness;
    }

    public float contrast = 1f;
    public int brightness = 255;
    public float prevContrast = 1f;
    public int prevBrightness = 255;

    @Override
    public Bitmap doOperation(Bitmap bitmap) {
        return  BitmapUtils.changeBrightnessAndContrast(bitmap, brightness, contrast);
    }

    @Override
    public Bitmap undoOperation(Bitmap bitmap) {
        return  BitmapUtils.changeBrightnessAndContrast(bitmap, prevBrightness, prevContrast);
    }
}
