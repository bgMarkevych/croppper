package com.office.photoedittoolapp.tools;

import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;

public class BitmapState {
    public Matrix matrix = new Matrix();
    public ArrayList<Path> paths = new ArrayList<>();
    public Paint paint = new Paint();
    public int brightness = 0;
    public float contrast = 1f;
    private int rotate = 0;
    public boolean isFlipHorizontal;
    public boolean isFlipVertical;

    public void setRotate(int rotate) {
        this.rotate = rotate == 360 || rotate == -360 ? 0 : rotate;
    }

    public int getRotate() {
        return rotate;
    }
}
