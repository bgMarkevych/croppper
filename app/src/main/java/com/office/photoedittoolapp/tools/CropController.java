package com.office.photoedittoolapp.tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.Log;
import android.view.MotionEvent;


public class CropController {
    private static final String TAG = "CROP";

    private float prevX = 0;
    private float prevY = 0;
    private int parentHeight;
    private int parentWidth;
    private int cropShapeWidth;
    private int cropShapeHeight;
    private float xRatio = 0;
    private float yRatio = 0;
    private int minHeight;
    private int minWidth;
    private int resizeTouchPadding = 30;
    private int cropRingSize = 50;

    private TouchType touchType;

    private Paint cropShapePaint;
    private Paint cropShapeBorderPaint;
    private Paint cropShapeRingPaint;
    private RectF cropShapeRect;
    private int borderColor = Color.parseColor("#248df6");
    private int rectColor = Color.parseColor("#4c248df6");

    private boolean isMultiTouch;

    public void setParentSize(int parentHeight, int parentWidth) {
        this.parentHeight = parentHeight;
        this.parentWidth = parentWidth;
        minWidth = parentWidth / 4;
        minHeight = parentHeight / 4;
        if (cropShapeHeight == 0 && cropShapeWidth == 0) {
            cropShapeHeight = parentHeight - minHeight;
            cropShapeWidth = parentWidth - minWidth;
        }
        initCropPaintTools();
    }

    public RectF getCropShapeRect() {
        return cropShapeRect;
    }

    private void initCropPaintTools() {
        cropShapePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cropShapePaint.setStyle(Paint.Style.FILL);
        cropShapePaint.setColor(rectColor);
        cropShapeRect = new RectF(parentWidth / 2f - cropShapeWidth / 2f, parentHeight / 2f - cropShapeHeight / 2f, parentWidth / 2f + cropShapeWidth / 2f, parentHeight / 2f + cropShapeHeight / 2f);
        cropShapeBorderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cropShapeBorderPaint.setStyle(Paint.Style.STROKE);
        cropShapeBorderPaint.setColor(borderColor);
        cropShapeBorderPaint.setStrokeWidth(5);
        cropShapeRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cropShapeRingPaint.setColor(borderColor);
        cropShapeRingPaint.setStyle(Paint.Style.FILL);
    }

    public void onDraw(Canvas canvas) {
        canvas.save();
        canvas.drawRect(cropShapeRect, cropShapePaint);
        canvas.drawRect(cropShapeRect, cropShapeBorderPaint);
        canvas.drawOval(cropShapeRect.left - cropRingSize / 2f, cropShapeRect.top - cropRingSize / 2f, cropShapeRect.left + cropRingSize / 2f, cropShapeRect.top + cropRingSize / 2f, cropShapeRingPaint);
        canvas.drawOval(cropShapeRect.right + cropRingSize / 2f, cropShapeRect.top - cropRingSize / 2f, cropShapeRect.right - cropRingSize / 2f, cropShapeRect.top + cropRingSize / 2f, cropShapeRingPaint);
        canvas.drawOval(cropShapeRect.right + cropRingSize / 2f, cropShapeRect.bottom - cropRingSize / 2f, cropShapeRect.right - cropRingSize / 2f, cropShapeRect.bottom + cropRingSize / 2f, cropShapeRingPaint);
        canvas.drawOval(cropShapeRect.left - cropRingSize / 2f, cropShapeRect.bottom - cropRingSize / 2f, cropShapeRect.left + cropRingSize / 2f, cropShapeRect.bottom + cropRingSize / 2f, cropShapeRingPaint);
        canvas.restore();
    }


    public boolean onTouchEvent(MotionEvent event) {

        float x = event.getX();
        float y = event.getY();
        int index = event.getActionIndex();
        if (index == 1) {
            isMultiTouch = true;
        }
        boolean flag = true;
        switch (event.getAction() & event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isMultiTouch) {
                    flag = false;
                } else {
                    if (touchType == null) {
                        flag = false;
                    } else {
                        onTouchMove(x, y);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isMultiTouch) {
                    isMultiTouch = false;
                } else {
                    onTouchUp();
                }
                break;
        }
        return flag;
    }

    private void onTouchDown(float x, float y) {
        if (y >= cropShapeRect.top && y <= cropShapeRect.top + resizeTouchPadding * 2
                && x >= cropShapeRect.left && x <= cropShapeRect.left + cropShapeWidth) {
            touchType = TouchType.TOP;
        }
        if (y <= cropShapeRect.top + cropShapeHeight && y >= cropShapeRect.top + cropShapeHeight - resizeTouchPadding * 2
                && x >= cropShapeRect.left && x <= cropShapeRect.left + cropShapeWidth) {
            touchType = TouchType.BOTTOM;
        }
        if (y >= cropShapeRect.top && y <= cropShapeRect.top + cropShapeHeight
                && x <= cropShapeRect.right && x >= cropShapeRect.right - resizeTouchPadding * 2) {
            touchType = TouchType.RIGHT;
        }
        if (y >= cropShapeRect.top && y <= cropShapeRect.top + cropShapeHeight
                && x >= cropShapeRect.left && x <= cropShapeRect.left + resizeTouchPadding * 2) {
            touchType = TouchType.LEFT;
        }
        if (y <= cropShapeRect.bottom && y >= cropShapeRect.bottom - resizeTouchPadding * 2
                && x <= cropShapeRect.right && x >= cropShapeRect.right - resizeTouchPadding * 2) {
            touchType = TouchType.BOTTOM_RIGHT;
        }
        if (y >= cropShapeRect.top && y <= cropShapeRect.top + resizeTouchPadding * 2
                && x <= cropShapeRect.right && x >= cropShapeRect.right - resizeTouchPadding * 2) {
            touchType = TouchType.TOP_RIGHT;
        }
        if (y >= cropShapeRect.top && y <= cropShapeRect.top + resizeTouchPadding * 2
                && x >= cropShapeRect.left && x <= cropShapeRect.left + resizeTouchPadding * 2) {
            touchType = TouchType.TOP_LEFT;
        }
        if (y <= cropShapeRect.bottom && y >= cropShapeRect.bottom - resizeTouchPadding * 2
                && x >= cropShapeRect.left && x <= cropShapeRect.left + resizeTouchPadding * 2) {
            touchType = TouchType.BOTTOM_LEFT;
        }
        if (x > cropShapeRect.left + resizeTouchPadding * 2 && x < cropShapeRect.left + cropShapeWidth - resizeTouchPadding * 2
                && y > cropShapeRect.top + resizeTouchPadding * 2 && y < cropShapeRect.top + cropShapeHeight - resizeTouchPadding * 2) {
            xRatio = x - cropShapeRect.left;
            yRatio = y - cropShapeRect.top;
            touchType = TouchType.MOVE;
        }
    }

    private void onTouchMove(float x, float y) {
        if (touchType == null) {
            return;
        }
        switch (touchType) {
            case MOVE:
                moveShape(x, y);
                break;
            case TOP:
                resizeFromTop(y);
                break;
            case BOTTOM:
                resizeFromBottom(y);
                break;
            case LEFT:
                resizeFromLeft(x);
                break;
            case RIGHT:
                resizeFromRight(x);
                break;
            case TOP_LEFT:
                resizeFromLeft(x);
                resizeFromTop(y);
                break;
            case TOP_RIGHT:
                resizeFromTop(y);
                resizeFromRight(x);
                break;
            case BOTTOM_LEFT:
                resizeFromBottom(y);
                resizeFromLeft(x);
                break;
            case BOTTOM_RIGHT:
                resizeFromBottom(y);
                resizeFromRight(x);
                break;
        }
    }

    private void onTouchUp() {
        touchType = null;
        prevX = 0;
        prevY = 0;
        isMultiTouch = false;
    }

    private void moveShape(float x, float y) {
        float cropViewNewX = x - xRatio;
        float cropViewNewY = y - yRatio;
        float correctX = Math.max(cropViewNewX, 0);
        float correctY = Math.max(cropViewNewY, 0);
        float X2 = correctX + cropShapeWidth;
        float Y2 = correctY + cropShapeHeight;
        correctX = Math.min(parentWidth - cropShapeWidth, X2 - cropShapeWidth);
        correctY = Math.min(parentHeight - cropShapeHeight, Y2 - cropShapeHeight);
        cropShapeRect.left = correctX;
        cropShapeRect.top = correctY;
        cropShapeRect.right = correctX + cropShapeWidth;
        cropShapeRect.bottom = correctY + cropShapeHeight;
    }

    private void resizeFromLeft(float x) {
        if (prevX != 0) {
            float pointX = x - prevX;
            int newLeft = (int) Math.max(0, cropShapeRect.left + pointX);
            newLeft = (int) Math.min(newLeft, cropShapeRect.right - minWidth);
            cropShapeRect.left = newLeft;
            cropShapeWidth = (int) (cropShapeRect.right - cropShapeRect.left);
        }
        prevX = x;
    }

    private void resizeFromRight(float x) {
        if (prevX != 0) {
            float pointX = x - prevX;
            int newWidth = (int) Math.min(parentWidth - cropShapeRect.left, cropShapeWidth + pointX);
            cropShapeWidth = Math.max(newWidth, minWidth);
            cropShapeRect.right = cropShapeRect.left + cropShapeWidth;
        }
        prevX = x;
    }

    private void resizeFromBottom(float y) {
        if (prevY != 0) {
            float pointY = y - prevY;
            int newHeight = (int) Math.min(parentHeight - cropShapeRect.top, cropShapeHeight + pointY);
            cropShapeHeight = Math.max(newHeight, minHeight);
            cropShapeRect.bottom = cropShapeRect.top + cropShapeHeight;
        }
        prevY = y;
    }

    private void resizeFromTop(float y) {
        if (prevY != 0) {
            float pointY = y - prevY;
            int newTop = (int) Math.max(0, cropShapeRect.top + pointY);
            newTop = (int) Math.min(newTop, cropShapeRect.bottom - minHeight);
            cropShapeRect.top = newTop;
            cropShapeHeight = (int) (cropShapeRect.bottom - cropShapeRect.top);
        }
        prevY = y;
    }

    public int getCropShapeWidth() {
        return cropShapeWidth;
    }

    public int getCropShapeHeight() {
        return cropShapeHeight;
    }
}
