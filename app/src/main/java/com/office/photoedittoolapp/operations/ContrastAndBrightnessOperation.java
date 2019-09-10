package com.office.photoedittoolapp.operations;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;

import com.office.photoedittoolapp.tools.BitmapState;
import com.office.photoedittoolapp.tools.BitmapUtils;
import com.office.photoedittoolapp.tools.OperationResultContainer;


public class ContrastAndBrightnessOperation implements EditOperation {

    public ContrastAndBrightnessOperation(float prevContrast, int prevBrightness) {
        this.prevContrast = prevContrast;
        this.prevBrightness = prevBrightness;
    }

    public float contrast = 1f;
    public int brightness = 0;
    public float prevContrast = 1f;
    public int prevBrightness = 0;

    @Override
    public OperationResultContainer doOperation(Bitmap bitmap, BitmapState state) {
        Bitmap tempBitmap = BitmapUtils.mapStateIntoBitmapWithoutAdjust(bitmap, state);
        state.contrast = contrast;
        state.brightness = brightness;
        Bitmap changedBitmap = BitmapUtils.changeBrightnessAndContrast(tempBitmap, state.matrix, brightness, contrast);
        Canvas canvas = new Canvas(changedBitmap);
        for (Path path: state.paths){
            canvas.drawPath(path, state.paint);
        }
        return new OperationResultContainer(state, changedBitmap);
    }

    @Override
    public OperationResultContainer undoOperation(Bitmap bitmap, BitmapState state) {
        Bitmap tempBitmap = BitmapUtils.mapStateIntoBitmapWithoutAdjust(bitmap, state);
        state.contrast = prevContrast;
        state.brightness = prevBrightness;
        Bitmap changedBitmap = BitmapUtils.changeBrightnessAndContrast(tempBitmap, state.matrix, prevBrightness, prevContrast);
        Canvas canvas = new Canvas(changedBitmap);
        for (Path path: state.paths){
            canvas.drawPath(path, state.paint);
        }
        return new OperationResultContainer(state, changedBitmap);
    }
}
