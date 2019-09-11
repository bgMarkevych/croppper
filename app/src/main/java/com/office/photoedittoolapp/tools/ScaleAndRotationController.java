package com.office.photoedittoolapp.tools;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;

public class ScaleAndRotationController {

    private static final String TAG = ScaleAndRotationController.class.getSimpleName();
    private Matrix matrix;
    private Matrix savedMatrix;
    private RectF src;
    private RectF dst;
    private PointF start;
    private PointF mid;
    private double oldDist;
    private float scaleWidth;
    private float scaleHeight;
    private boolean isScaling;
    private float[] values = new float[9];
    private boolean isMultiTouch;

    public ScaleAndRotationController() {
        matrix = new Matrix();
        savedMatrix = new Matrix();
        src = new RectF();
        dst = new RectF();
        start = new PointF();
        mid = new PointF();
    }

    public void setParentSize(int bitmapHeight, int bitmapWidth) {
        src.bottom = bitmapHeight;
        src.right = bitmapWidth;
        dst.bottom = bitmapHeight;
        dst.right = bitmapWidth;
        scaleHeight = bitmapHeight;
        scaleWidth = bitmapWidth;
    }

    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    public Matrix onTouchEvent(MotionEvent event, boolean cropFlag, boolean eraseFlag) {
        int index = event.getActionIndex();
        Log.d(TAG, "onTouchEvent: index " + index);
        if (index == 1) {
            isMultiTouch = true;
            Log.d(TAG, "onTouchEvent: multitouch");
        }
        switch (event.getAction() & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                onActionDown(event);
                Log.d(TAG, "onTouchEvent: down");
                break;
            case MotionEvent.ACTION_MOVE:
                if (cropFlag) {
                    break;
                }
                if (isMultiTouch) {
                    Log.d(TAG, "onTouchEvent: scale");
                    onScale(event);
                } else {
                    Log.d(TAG, "onTouchEvent: move");
                    if (!eraseFlag) {
                        onMove(event);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isMultiTouch) {
                    isMultiTouch = false;
                }
                Log.d(TAG, "onTouchEvent: up");
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                onPointerDown(event);
                Log.d(TAG, "onTouchEvent: pointer down");
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(TAG, "onTouchEvent: pointer up");
                onPointerUp();
                break;

        }
        return matrix;
    }

    private void onActionDown(MotionEvent event) {
        savedMatrix.set(matrix);
        start.set(event.getX(), event.getY());
    }

    private void onPointerDown(MotionEvent event) {
        oldDist = spacing(event);
        if (oldDist > 10f) {
            savedMatrix.set(matrix);
            isScaling = true;
            midPoint(mid, event);
        }
    }

    private void onPointerUp() {
        isScaling = false;
    }

    private void onScale(MotionEvent event) {
        if (isScaling) {
            double newDist = spacing(event);
            if (newDist > 10f) {
                float scale = (float) (newDist / oldDist);
                matrix.set(savedMatrix);
                matrix.postScale(scale, scale, mid.x, mid.y);
                correctScaleCoordinates();
            }
        }
    }

    private void onMove(MotionEvent event) {
        if (scaleHeight != src.bottom && scaleWidth != src.right) {
            float dx = event.getX() - start.x;
            float dy = event.getY() - start.y;
            matrix.set(savedMatrix);
            matrix.postTranslate(dx, dy);
            correctMoveCoordinates();
        }
    }

    private void correctScaleCoordinates() {
        matrix.getValues(values);
        float scaleX = values[Matrix.MSCALE_X];
        float scaleY = values[Matrix.MSCALE_Y];
        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];
        scaleX = Math.max(scaleX, 1);
        scaleY = Math.max(scaleY, 1);
        scaleWidth = src.right * (scaleX - 1);
        scaleHeight = src.bottom * (scaleY - 1);
        transX = Math.max(Math.min(transX, 0), -scaleWidth);
        transY = Math.max(Math.min(transY, 0), -scaleHeight);
        values[Matrix.MSCALE_X] = scaleX;
        values[Matrix.MSCALE_Y] = scaleY;
        values[Matrix.MTRANS_X] = transX;
        values[Matrix.MTRANS_Y] = transY;
        matrix.setValues(values);
        matrix.mapRect(dst, src);
        Log.d(TAG, "correctScaleCoordinates: " + dst.toShortString());
        Log.d(TAG, "correctScaleCoordinates: " + scaleHeight + " " + scaleWidth);
    }

    private void correctMoveCoordinates() {
        matrix.getValues(values);
        float transX = values[Matrix.MTRANS_X];
        float transY = values[Matrix.MTRANS_Y];
        transX = Math.min(Math.max(transX, -scaleWidth), 0);
        transY = Math.min(Math.max(transY, -scaleHeight), 0);
        values[Matrix.MTRANS_X] = transX;
        values[Matrix.MTRANS_Y] = transY;
        matrix.setValues(values);
        matrix.mapRect(dst, src);
        Log.d(TAG, "correctMoveCoordinates: " + dst.toShortString());
        Log.d(TAG, "correctScaleCoordinates: float " + scaleHeight + " int" + (int) scaleWidth);
    }

    private double spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    public PointF getScaledImageCoordinates() {
        PointF pointF = new PointF();
        pointF.x = dst.left == 0.0 ? dst.left : -dst.left;
        pointF.y = dst.top == 0.0 ? dst.top : -dst.top;
        return pointF;
    }

    public float[] getScaleFactor() {
        float[] scales = new float[2];
        scales[0] = values[Matrix.MSCALE_X];
        scales[1] = values[Matrix.MSCALE_Y];
        return scales;
    }

    private void rotateImage(int degrees) {
        matrix.set(savedMatrix);
        matrix.postRotate(degrees, src.right/2, src.bottom/2);
        savedMatrix.set(matrix);
    }

    public Matrix rotateRight() {
        rotateImage(90);
        return matrix;
    }

    public Matrix rotateLeft() {
        rotateImage(-90);
        return matrix;
    }

    private void flipImage(float sx, float sy) {
        matrix.set(savedMatrix);
        matrix.postScale(sx, sy, src.right/2, src.bottom/2);
        savedMatrix.set(matrix);
    }

    public Matrix flipVertical() {
        flipImage(1, -1);
        return matrix;
    }

    public Matrix flipHorizontal() {
        flipImage(-1, 1);
        return matrix;
    }

    public boolean isScaled() {
        float[] scales = getScaleFactor();
        return scales[0] > 0 && scales[1] > 0;
    }
}
