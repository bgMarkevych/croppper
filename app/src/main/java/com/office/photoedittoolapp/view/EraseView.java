package com.office.photoedittoolapp.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class EraseView extends View {
    private static final String TAG = EraseView.class.getSimpleName();

    public EraseView(@NonNull Context context) {
        super(context);
        initEraseTools();
    }

    public EraseView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initEraseTools();
    }

    public EraseView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initEraseTools();
    }

    private Paint pathPaint;
    private Path path;
    private int strokeWidth = 10;
    private ArrayList<Path> paths;
    private ArrayList<Path> undoPaths;

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
        undoPaths = new ArrayList<>();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        ViewGroup parent = (ViewGroup) getParent();
        Log.d(TAG, "onMeasure: width " + parent.getWidth() + " height " + parent.getHeight());
        int width = MeasureSpec.makeMeasureSpec(parent.getWidth(), MeasureSpec.EXACTLY);
        int height = MeasureSpec.makeMeasureSpec(parent.getHeight(), MeasureSpec.EXACTLY);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        for (Path path : paths) {
            canvas.drawPath(path, pathPaint);
        }
        canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("TOUCH", "onTouchEvent: " + event.getAction() + " fingers count: " + event.getPointerCount());
        float X = event.getX();
        float Y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:
                path.lineTo(X, Y);
                break;
            case MotionEvent.ACTION_DOWN:
                path.reset();
                path.moveTo(X, Y);
                paths.add(path);
                return true;
            case MotionEvent.ACTION_UP:
                path = new Path();
                break;
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void resetAll() {
        if (paths != null) {
            paths.clear();
            undoPaths.clear();
        }
    }

    public void undoTask() {
        if (paths != null && paths.size() > 0) {
            undoPaths.add(paths.remove(paths.size() - 1));
        }
        invalidate();
    }

    public void reUndoTask() {
        if (undoPaths != null && undoPaths.size() > 0) {
            paths.add(undoPaths.remove(undoPaths.size() - 1));
        }
        invalidate();
    }

    public ArrayList<Path> getPathArray() {
        return paths;
    }

    public Paint getPathPaint() {
        return pathPaint;
    }

}
