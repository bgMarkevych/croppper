package com.office.photoedittoolapp.tools;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.Pair;
import android.view.MotionEvent;

import java.util.ArrayList;

public class EraseController {
    private static final String TAG = EraseController.class.getSimpleName();

    public EraseController() {
        initEraseTools();
    }

    private Paint pathPaint;
    private Path path;
    private int strokeWidth = 10;
    private BitmapState state;
    private ArrayList<Pair<Path, Integer>> paths;
    private boolean isMultiTouch;
    private Rect canvasRect;
    private EraseStateChangeListener eraseStateChangeListener;

    public void setEraseStateChangeListener(EraseStateChangeListener eraseStateChangeListener) {
        this.eraseStateChangeListener = eraseStateChangeListener;
    }

    public void setPaths(ArrayList<Pair<Path, Integer>> paths) {
        this.paths.clear();
        this.paths.addAll(paths);
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
        path = new Path();
        paths = new ArrayList<>();
    }

    public void onDraw(Canvas canvas, BitmapState state, int width, int height) {
        this.state = state;
        canvasRect = canvas.getClipBounds();
        float cx = width / 2f;
        float cy = height / 2f;
        canvas.save();
        for (int i = 0; i < paths.size(); i++) {
            int rotate = paths.get(i).second;
            canvas.rotate(-rotate, cx, cy);
            canvas.drawPath(paths.get(i).first, pathPaint);
            canvas.rotate(rotate, cx, cy);
        }
        canvas.restore();
    }

    public boolean onTouchEvent(MotionEvent event, Matrix matrix) {
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
                paths.add(new Pair<>(path, state.getRotate()));
                return true;
            case MotionEvent.ACTION_UP:
                path = new Path();
                eraseStateChangeListener.eraseStateChanged(paths);
                break;
            default:
                return false;
        }

        return true;
    }

    public interface EraseStateChangeListener{
        void eraseStateChanged(ArrayList<Pair<Path, Integer>> paths);
    }

}
