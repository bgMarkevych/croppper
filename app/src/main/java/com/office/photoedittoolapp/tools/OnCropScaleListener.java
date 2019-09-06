package com.office.photoedittoolapp.tools;

import android.view.MotionEvent;

public interface OnCropScaleListener {
    void onActionDown(MotionEvent event);
    void onPointerDown(MotionEvent event);
    void onScale(MotionEvent event);
    void onMove(MotionEvent event);
    void onPointerUp(MotionEvent event);
}
