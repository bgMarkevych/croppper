package com.office.photoedittoolapp.tools;

import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import java.util.ArrayList;

public class BitmapState {
    private ArrayList<EraseDrawContainer> paths = new ArrayList<>();
    public int brightness = 0;
    public float contrast = 1f;
    private int rotate = 0;
    public boolean isFlipVertical;
    public boolean isFlipHorizontal;
    public float[] scales = new float[]{1, 1};
    public RectF cropShape;
    public Rect src;

    public BitmapState() {
    }

    public BitmapState(BitmapState state) {
        setPaths(state.paths);
        brightness = state.brightness;
        contrast = state.contrast;
        rotate = state.rotate;
        isFlipVertical = state.isFlipVertical;
        isFlipHorizontal = state.isFlipHorizontal;
        scales = state.scales;
        cropShape = state.cropShape;
        src = state.src;
    }

    public void setRotate(int rotate) {
        this.rotate += rotate;
        this.rotate = this.rotate == 360 || this.rotate == -360 ? 0 : this.rotate;
    }

    public int getRotate() {
        return rotate;
    }

    public ArrayList<EraseDrawContainer> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<EraseDrawContainer> paths) {
        this.paths.clear();
        this.paths.addAll(paths);
    }
}
