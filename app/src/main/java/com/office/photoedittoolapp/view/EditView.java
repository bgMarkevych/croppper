package com.office.photoedittoolapp.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.photoedittoolapp.tools.BitmapUtils;

import java.util.ArrayList;

/**
 * View for editing photos
 * Mods: erasing, changing brightness, contrast, orientation
 * TODO need to improve onMeasure method
 */

public class EditView extends View {
    public EditView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public EditView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EditView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private Bitmap originBitmap;
    private Bitmap tempBitmap;
    private Bitmap currentBitmap;
    private Canvas bitmapCanvas;
    private int height = 0;
    private int width = 0;
    private int rotation = 0;

    private boolean cropFlag = true;
    private Bitmap cropBorderBitmap;
    private Paint cropRectanglePaint;
    private Paint darkBgPaint;
    private RectF cropRectangleRect;
    private Canvas cropBorderCanvas;

    private boolean eraseFlag;
    private Paint pathPaint;
    private Path path;
    private int strokeWidth = 10;
    private ArrayList<Path> paths;
    private ArrayList<Path> undoPaths;

    private ScaleGestureDetector scaleGestureDetector;
    private float scaleFactor = 1.f;
    private float scaleTouchX;
    private float scaleTouchY;

    private void init(Context context) {
        scaleGestureDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleTouchX = detector.getFocusX();
            scaleTouchY = detector.getFocusY();
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 10.0f));
            Log.d("TOUCH", "span x: " + detector.getCurrentSpanX() + " x: " + detector.getFocusX() + " scale " + scaleFactor);
            invalidate();
            return true;
        }
    }

    /**
     * @param bitmapImage origin bitmap for editing
     *                    need to use only once or for changing origin bitmap
     */
    public void setBitmapImage(@NonNull Bitmap bitmapImage) {
        countSize(bitmapImage.getWidth() / bitmapImage.getHeight());
        originBitmap = currentBitmap = tempBitmap = Bitmap.createScaledBitmap(bitmapImage, width, height, true);
        bitmapCanvas = new Canvas(tempBitmap);
        initCropTools();
    }

    public void setStrokeWidth(int width) {
        strokeWidth = width;
    }

    public void enableEraseMode() {
        eraseFlag = true;
        initEraseTools();
        invalidate();
    }

    public void disableEraseMode() {
        eraseFlag = false;
        path = null;
        paths.clear();
        paths = null;
        undoPaths.clear();
        undoPaths = null;
        pathPaint = null;
        invalidate();
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

    private void initCropTools() {
        cropBorderBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        cropBorderCanvas = new Canvas(cropBorderBitmap);
        cropRectanglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cropRectanglePaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        cropRectanglePaint.setStyle(Paint.Style.FILL);
        darkBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        darkBgPaint.setAlpha(70);
        cropBorderCanvas.drawColor(Color.BLACK);
        cropRectangleRect = new RectF(width / 2f - width / 4f, height / 2f - height / 4f, width / 2f + width / 4f, height / 2f + height / 4f);
        cropBorderCanvas.drawRect(cropRectangleRect, cropRectanglePaint);
    }

    /**
     * count width and height according to screen size
     */

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.makeMeasureSpec(this.width, MeasureSpec.EXACTLY);
        int height = MeasureSpec.makeMeasureSpec(this.height, MeasureSpec.EXACTLY);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        float scaledWidth = width * scaleFactor;
        float scaledHeight = height * scaleFactor;
        if (!cropFlag) {
            if (scaledHeight > height || scaledWidth > width) {
                canvas.scale(scaleFactor, scaleFactor, scaleTouchX, scaleTouchY);
            }
            canvas.drawBitmap(tempBitmap != null && !tempBitmap.isRecycled() ? tempBitmap : currentBitmap, 0, 0, null);
            if (eraseFlag) {
                for (Path path : paths) {
                    canvas.drawPath(path, pathPaint);
                }
            }
            canvas.drawBitmap(cropBorderBitmap, 0, 0, darkBgPaint);
        } else {
            canvas.drawBitmap(tempBitmap != null && !tempBitmap.isRecycled() ? tempBitmap : currentBitmap, 0, 0, null);
            canvas.scale(scaleFactor, scaleFactor, scaleTouchX, scaleTouchY);
            canvas.drawBitmap(cropBorderBitmap, 0, 0, darkBgPaint);
        }
        canvas.restore();
    }

    @Override
    public boolean onFilterTouchEventForSecurity(MotionEvent event) {
        Log.d("TOUCH", "onFilterTouchEvent: " + event.getAction() + " fingers count: " + event.getPointerCount());
        return super.onFilterTouchEventForSecurity(event);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("TOUCH", "onTouchEvent: " + event.getAction() + " fingers count: " + event.getPointerCount());
        float X = event.getX();
        float Y = event.getY();

        if (eraseFlag) {
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
        }

        scaleGestureDetector.onTouchEvent(event);
        invalidate();
        return true;
    }

    private int getStatusBarHeight() {
        int result = 0;
        int resourceId = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getContext().getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private int getAppBarHeight() {
        TypedValue tv = new TypedValue();
        if (getContext().getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            return TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }
        return 0;
    }

    /**
     * Method for counting bitmap and view size
     *
     * @param bitmapRatio param of ratio of bitmap size
     */

    private void countSize(float bitmapRatio) {
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        if (metrics.widthPixels > metrics.heightPixels) {
            width = metrics.heightPixels - getStatusBarHeight() - getAppBarHeight();
            height = (int) ((metrics.heightPixels - getStatusBarHeight() - getAppBarHeight()) / bitmapRatio);
        } else {
            width = metrics.widthPixels;
            height = (int) (metrics.widthPixels / bitmapRatio);
        }
    }

    public void saveChanges() {
        if (eraseFlag) {
            if (bitmapCanvas != null) {
                for (Path path : paths) {
                    bitmapCanvas.drawPath(path, pathPaint);
                }
                bitmapCanvas.save();
            }
            disableEraseMode();
        }
        currentBitmap = tempBitmap;
        invalidate();
    }

    public void resetAll() {
        if (eraseFlag) {
            if (paths != null) {
                paths.clear();
                undoPaths.clear();
            }
        }
        currentBitmap = tempBitmap = originBitmap;
        invalidate();
    }

    public void undoTask() {
        if (eraseFlag) {
            if (paths != null && paths.size() > 0) {
                undoPaths.add(paths.remove(paths.size() - 1));
            }
        }
        invalidate();
    }

    public void reUndoTask() {
        if (eraseFlag) {
            if (undoPaths != null && undoPaths.size() > 0) {
                paths.add(undoPaths.remove(undoPaths.size() - 1));
            }
        }
        invalidate();
    }

    public void rotateImage(int rotate) {
        tempBitmap = BitmapUtils.rotateBitmap(tempBitmap, rotate);
        invalidate();
    }

    /**
     * @param brightness must be in range between 0 and 510
     * @param contrast   must be in range 0 ... 1000
     **/
    public void changeBrightnessAndContrast(int brightness, float contrast) {
        tempBitmap = BitmapUtils.changeBrightnessAndContrast(currentBitmap, brightness, contrast / 100f);
        invalidate();
    }

}
