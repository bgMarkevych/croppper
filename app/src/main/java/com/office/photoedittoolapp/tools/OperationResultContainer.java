package com.office.photoedittoolapp.tools;

import android.graphics.Bitmap;

public class OperationResultContainer {
    public BitmapState bitmapState;
    public Bitmap bitmap;

    public OperationResultContainer(BitmapState bitmapState, Bitmap bitmap) {
        this.bitmapState = bitmapState;
        this.bitmap = bitmap;
    }
}
