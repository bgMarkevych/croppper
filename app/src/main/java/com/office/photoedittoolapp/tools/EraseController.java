package com.office.photoedittoolapp.tools;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;

import com.office.photoedittoolapp.data.BitmapState;
import com.office.photoedittoolapp.data.EraseDrawContainer;
import com.office.photoedittoolapp.data.PathS;

import java.util.ArrayList;
import java.util.Stack;

public class EraseController {
    private static final String TAG = EraseController.class.getSimpleName();

    public EraseController(EraseStateChangeListener eraseStateChangeListener) {
        this.eraseStateChangeListener = eraseStateChangeListener;
        initEraseTools();
    }

    private Paint pathPaint;
    private PathS path;
    private int strokeWidth = 10;
    private BitmapState state;
    private ArrayList<EraseDrawContainer> paths;
    private boolean isMultiTouch;
    private Rect canvasRect;
    private EraseStateChangeListener eraseStateChangeListener;

    public void setPaths(ArrayList<EraseDrawContainer> paths) {
        this.paths = new ArrayList<>(paths);
    }

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
        path = new PathS();
        paths = new ArrayList<>();
    }

    public void onDraw(Canvas canvas, BitmapState state, int width, int height) {
        this.state = state;
        canvasRect = canvas.getClipBounds();
        float cx = width / 2f;
        float cy = height / 2f;
        for (int i = paths.size() - 1; i >= 0; i--) {
            canvas.save();
            int rotate = paths.get(i).rotate;
            canvas.rotate(rotate, cx, cy);
            canvas.drawPath(paths.get(i).path, pathPaint);
            canvas.restore();
        }
    }

    public boolean onTouchEvent(MotionEvent event, Matrix matrix) {
        if (eraseStateChangeListener.isCropModeEnabled()) {
            return false;
        }
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
                path.moveTo(X, Y);
                paths.add(new EraseDrawContainer(path, 0, strokeWidth));
                return true;
            case MotionEvent.ACTION_UP:
                path = new PathS();
                eraseStateChangeListener.eraseStateChanged(paths);
                break;
            default:
                return false;
        }

        return true;
    }

    public interface EraseStateChangeListener {
        void eraseStateChanged(ArrayList<EraseDrawContainer> paths);

        boolean isCropModeEnabled();
    }

}
