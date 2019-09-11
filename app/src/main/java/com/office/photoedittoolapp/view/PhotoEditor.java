package com.office.photoedittoolapp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;

import com.office.photoedittoolapp.tools.BitmapState;
import com.office.photoedittoolapp.tools.CropController;
import com.office.photoedittoolapp.tools.EraseController;
import com.office.photoedittoolapp.tools.ScaleAndRotationController;

import java.util.ArrayList;

public class PhotoEditor extends View implements EraseController.EraseStateChangeListener {

    private static final String TAG = PhotoEditor.class.getSimpleName();

    public PhotoEditor(Context context) {
        super(context);
        init();
    }

    public PhotoEditor(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PhotoEditor(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private Bitmap originBitmap;
    private Bitmap scaledBitmap;
    private Bitmap tempBitmap;
    private ScaleAndRotationController scaleAndRotationController;
    private CropController cropController;
    private EraseController eraseController;
    private AdjustUndoReundoListener adjustUndoReundoListener;
    private ArrayList<BitmapState> states = new ArrayList<>();
    private BitmapState currentState;

    private Paint adjustPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF bitmapDst = new RectF();
    private Matrix matrix = new Matrix();
    private Matrix zeroMatrix = new Matrix();
    private ColorMatrix colorMatrix = new ColorMatrix();
    private ColorMatrixColorFilter colorMatrixColorFilter;

    private boolean isEraseMode;
    private boolean isCroppingMode;
    private float sidesAspect;

    public void setOriginBitmap(Bitmap bitmap) {
        this.originBitmap = bitmap;
        tempBitmap = originBitmap;
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    Log.d(TAG, "init: " + getMeasuredHeight() + " " + getMeasuredWidth());
                    Log.d(TAG, "init: " + getHeight() + " " + getWidth());
                    scaleAndRotationController.setParentSize(getHeight(), getWidth());
                    cropController.setParentSize(getHeight(), getWidth());
                    tempBitmap = scaledBitmap = Bitmap.createScaledBitmap(originBitmap, getWidth(), getHeight(), true);
                }
            });
        }
    }

    public void setAdjustUndoReundoListener(AdjustUndoReundoListener adjustUndoReundoListener) {
        this.adjustUndoReundoListener = adjustUndoReundoListener;
    }

    private void init() {
        scaleAndRotationController = new ScaleAndRotationController();
        cropController = new CropController();
        eraseController = new EraseController();
        eraseController.setEraseStateChangeListener(this);
        currentState = new BitmapState();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        bitmapDst.right = getWidth();
        bitmapDst.bottom = getHeight();
        sidesAspect = (float) originBitmap.getHeight() / getHeight();
        canvas.setMatrix(matrix);
        canvas.rotate(currentState.getRotate(), getWidth() / 2f, getHeight() / 2f);
        if (colorMatrixColorFilter != null) {
            adjustPaint.setColorFilter(colorMatrixColorFilter);
        }
        canvas.drawBitmap(tempBitmap, null, bitmapDst, adjustPaint);
        if (isCroppingMode) {
            canvas.setMatrix(zeroMatrix);
            cropController.onDraw(canvas);
        }
        if (isEraseMode) {
            eraseController.onDraw(canvas, currentState, getWidth(), getHeight());
        }
        canvas.restore();
    }

    public void setCroppingMode(boolean croppingMode) {
        isCroppingMode = croppingMode;
        invalidate();
    }

    public void setEraseMode(boolean eraseMode) {
        isEraseMode = eraseMode;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (originBitmap != null) {
            if (heightSize == 0) {
                heightSize = originBitmap.getHeight();
            }
            int desiredWidth;
            int desiredHeight;
            double viewToBitmapWidthRatio = Double.POSITIVE_INFINITY;
            double viewToBitmapHeightRatio = Double.POSITIVE_INFINITY;
            if (widthSize < originBitmap.getWidth()) {
                viewToBitmapWidthRatio = (double) widthSize / (double) originBitmap.getWidth();
            }
            if (heightSize < originBitmap.getHeight()) {
                viewToBitmapHeightRatio = (double) heightSize / (double) originBitmap.getHeight();
            }
            if (viewToBitmapWidthRatio != Double.POSITIVE_INFINITY
                    || viewToBitmapHeightRatio != Double.POSITIVE_INFINITY) {
                if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
                    desiredWidth = widthSize;
                    desiredHeight = (int) (originBitmap.getHeight() * viewToBitmapWidthRatio);
                } else {
                    desiredHeight = heightSize;
                    desiredWidth = (int) (originBitmap.getWidth() * viewToBitmapHeightRatio);
                }
            } else {
                desiredWidth = originBitmap.getWidth();
                desiredHeight = originBitmap.getHeight();
            }
            int width = getOnMeasureSpec(widthMode, widthSize, desiredWidth);
            int height = getOnMeasureSpec(heightMode, heightSize, desiredHeight);
            setMeasuredDimension(width, height);
        } else {
            setMeasuredDimension(widthSize, heightSize);
        }
    }

    private int getOnMeasureSpec(int measureSpecMode, int measureSpecSize, int desiredSize) {
        int spec;
        if (measureSpecMode == MeasureSpec.EXACTLY) {
            spec = measureSpecSize;
        } else if (measureSpecMode == MeasureSpec.AT_MOST) {
            spec = Math.min(desiredSize, measureSpecSize);
        } else {
            spec = desiredSize;
        }
        return spec;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isCroppingMode) {
            boolean flag = cropController.onTouchEvent(event);
            matrix = scaleAndRotationController.onTouchEvent(event, flag, false);
        } else if (isEraseMode) {
            matrix = scaleAndRotationController.onTouchEvent(event, false, true);
            eraseController.onTouchEvent(event, scaleAndRotationController.isScaled() ? matrix : zeroMatrix);
            Log.d(TAG, "onTouchEvent: erase");
        } else {
            matrix = scaleAndRotationController.onTouchEvent(event, false, false);
        }
        invalidate();
        return true;
    }

    /**
     * @param brightness must be in range between 0 and 510
     * @param contrast   must be in range 0 ... 1000
     **/
    public void changeAdjust(int brightness, float contrast) {
        currentState.brightness = brightness - 255;
        currentState.contrast = contrast / 100;
        updateColorMatrix();
        invalidate();
    }

    public void saveBitmapState(int brightness, float contrast) {
        states.add(currentState);
        BitmapState bitmapState = new BitmapState(currentState);
        bitmapState.brightness = brightness - 255;
        bitmapState.contrast = contrast / 100;
        currentState = bitmapState;
    }

    private void updateColorMatrix(){
        float[] values = new float[]
                {
                        currentState.contrast, 0, 0, 0, currentState.brightness,
                        0, currentState.contrast, 0, 0, currentState.brightness,
                        0, 0, currentState.contrast, 0, currentState.brightness,
                        0, 0, 0, 1, 0
                };
        colorMatrix.set(values);
        colorMatrixColorFilter = new ColorMatrixColorFilter(colorMatrix);
    }

    public void rotateRight() {
        BitmapState state = new BitmapState(currentState);
        state.setRotate(-90);
        states.add(currentState);
        currentState = state;
        invalidate();
    }

    public void rotateLeft() {
        BitmapState state = new BitmapState(currentState);
        state.setRotate(90);
        states.add(currentState);
        currentState = state;
        invalidate();
    }

    public void flipVertical() {
        Matrix matrix = new Matrix();
        states.add(currentState);
        BitmapState state = new BitmapState(currentState);
        matrix.preScale(1, -1, getWidth() / 2f, getHeight() / 2f);
        state.flipMatrix = matrix;
        currentState = state;
        tempBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), matrix, true);
        invalidate();
    }

    public void flipHorizontal() {
        Matrix matrix = new Matrix();
        BitmapState state = new BitmapState(currentState);
        states.add(currentState);
        matrix.preScale(-1, 1, getWidth() / 2f, getHeight() / 2f);
        state.flipMatrix = matrix;
        currentState = state;
        tempBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), matrix, true);
        invalidate();
    }


    public void undo() {
        if (states.size() != 0) {
            currentState = states.remove(states.size() - 1);
            eraseController.setPaths(currentState.getPaths());
//            tempBitmap = Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), currentState.flipMatrix, true);
            updateColorMatrix();
            if (adjustUndoReundoListener != null){
                adjustUndoReundoListener.undo(currentState.brightness, currentState.contrast);
            }
        }
        invalidate();
    }

    public Bitmap getImage() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    @Override
    public void eraseStateChanged(ArrayList<Pair<Path, Integer>> paths) {
        BitmapState state = new BitmapState(currentState);
        state.setPaths(paths);
        states.add(currentState);
        currentState = state;
        invalidate();
    }

    public interface AdjustUndoReundoListener {
        void undo(int brightness, float contrast);

        void reundo(int brightness, float contrast);
    }


}
