package com.office.photoedittoolapp.tools;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;

public class EraseController {
    private static final String TAG = EraseController.class.getSimpleName();

    public EraseController(OnSaveEraseResultListener onSaveEraseResultListener) {
        initEraseTools();
        this.onSaveEraseResultListener = onSaveEraseResultListener;
    }


    private Paint pathPaint;
    private Path path;
    private int strokeWidth = 10;
    private ArrayList<Path> paths;
    private boolean isMultiTouch;
    private Rect canvasRect;
    private OnSaveEraseResultListener onSaveEraseResultListener;

    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }

    private void initEraseTools() {
        pathPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        pathPaint.setStrokeWidth(strokeWidth);
        pathPaint.setStyle(Paint.Style.STROKE);
        pathPaint.setColor(Color.TRANSPARENT);
        pathPaint.setStrokeJoin(Paint.Join.ROUND);
        pathPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        path = new Path();
        paths = new ArrayList<>();
    }

    public void onDraw(Canvas canvas) {
        canvasRect = canvas.getClipBounds();
        canvas.save();
        for (Path path : paths) {
            canvas.drawPath(path, pathPaint);
        }
        canvas.restore();
    }

    public boolean onTouchEvent(MotionEvent event, Matrix matrix) {
        Log.d("TOUCH", "onTouchEvent: " + event.getAction() + " fingers count: " + event.getPointerCount());
        float[] values = new float[9];
        matrix.getValues(values);
        float X = event.getX() / values[Matrix.MSCALE_X] + canvasRect.left;
        float Y = event.getY() / values[Matrix.MSCALE_X] + canvasRect.top;
        int index = event.getActionIndex();
        if (index == 1) {
            isMultiTouch = true;
        }
        if (isMultiTouch) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                isMultiTouch = false;
            }
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                path.lineTo(X, Y);
                break;
            case MotionEvent.ACTION_DOWN:
                path.reset();
                path.transform(matrix);
                path.moveTo(X, Y);
                paths.add(path);
                return true;
            case MotionEvent.ACTION_UP:
                path = new Path();
                onSaveEraseResultListener.saveEraseResult();
                paths.clear();
                break;
            default:
                return false;
        }

        return true;
    }

    public ArrayList<Path> getPathArray() {
        return paths;
    }

    public Paint getPathPaint() {
        return pathPaint;
    }

    public interface OnSaveEraseResultListener {
        void saveEraseResult();
    }

}
