package com.office.photoedittoolapp.tools;

import android.graphics.Matrix;
import android.graphics.Path;
import android.util.Pair;

import java.util.ArrayList;

public class BitmapState {
    private ArrayList<Pair<Path, Integer>> paths = new ArrayList<>();
    public int brightness = 0;
    public float contrast = 1f;
    private int rotate = 0;
    public Matrix flipMatrix;

    public BitmapState(){}

    public BitmapState(BitmapState state){
        setPaths(state.paths);
        brightness = state.brightness;
        contrast = state.contrast;
        rotate = state.rotate;
        flipMatrix = state.flipMatrix;
    }

    public void setRotate(int rotate) {
        this.rotate += rotate;
        this.rotate = this.rotate == 360 || this.rotate == -360 ? 0 : this.rotate;
    }

    public int getRotate() {
        return rotate;
    }

    public ArrayList<Pair<Path, Integer>> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<Pair<Path, Integer>> paths) {
        this.paths.clear();
        this.paths.addAll(paths);
    }
}
