package com.office.photoedittoolapp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;

import com.office.photoedittoolapp.tools.BitmapState;
import com.office.photoedittoolapp.tools.CropController;
import com.office.photoedittoolapp.tools.EraseController;
import com.office.photoedittoolapp.tools.EraseDrawContainer;
import com.office.photoedittoolapp.tools.OperationController;
import com.office.photoedittoolapp.tools.ScaleAndRotationController;

import java.util.ArrayList;

public class PhotoEditor extends View implements EraseController.EraseStateChangeListener, OperationController.OperationCallback {

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


    private BitmapState currentState;
    private Bitmap originBitmap;
    private Bitmap scaledBitmap;
    private Bitmap tempBitmap;
    private ScaleAndRotationController scaleAndRotationController;
    private CropController cropController;
    private EraseController eraseController;
    private OperationController operationController;
    private AdjustUndoReundoListener adjustUndoReundoListener;

    private Paint adjustPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF bitmapDst = new RectF();
    private Rect bitmapSrc;
    private Matrix matrix = new Matrix();
    private Matrix zeroMatrix = new Matrix();
    private ColorMatrix colorMatrix = new ColorMatrix();
    private ColorMatrixColorFilter colorMatrixColorFilter;

    private boolean isEraseMode = true;
    private boolean isCroppingMode;

    public void setOriginBitmap(Bitmap bitmap) {
        this.originBitmap = bitmap;
        tempBitmap = originBitmap;
        ViewTreeObserver viewTreeObserver = getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
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
        eraseController = new EraseController(this);
        operationController = new OperationController(this);
        currentState = operationController.getCurrentState();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        bitmapDst.right = getWidth();
        bitmapDst.bottom = getHeight();
        canvas.setMatrix(matrix);
        canvas.rotate(currentState.getRotate(), getWidth() / 2f, getHeight() / 2f);
        if (colorMatrixColorFilter != null) {
            adjustPaint.setColorFilter(colorMatrixColorFilter);
        }
        canvas.save();
        canvas.scale(!currentState.isFlipHorizontal ? 1 : -1, !currentState.isFlipVertical ? 1 : -1, getWidth() / 2f, getHeight() / 2f);
        canvas.scale(currentState.scales[0], currentState.scales[1], getWidth() / 2f, getHeight() / 2f);
        canvas.drawBitmap(tempBitmap, null, bitmapDst, adjustPaint);
        eraseController.onDraw(canvas, currentState, getWidth(), getHeight());
        canvas.save();
        canvas.restore();
        if (isCroppingMode) {
            canvas.setMatrix(zeroMatrix);
            cropController.onDraw(canvas);
        }
        canvas.restore();
    }

    public void setCroppingMode(boolean croppingMode) {
        isCroppingMode = croppingMode;
        isEraseMode = !croppingMode;
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        if (tempBitmap != null) {
            if (heightSize == 0) {
                heightSize = tempBitmap.getHeight();
            }
            int desiredWidth;
            int desiredHeight;
            double viewToBitmapWidthRatio = Double.POSITIVE_INFINITY;
            double viewToBitmapHeightRatio = Double.POSITIVE_INFINITY;
            if (widthSize < tempBitmap.getWidth()) {
                viewToBitmapWidthRatio = (double) widthSize / (double) tempBitmap.getWidth();
            }
            if (heightSize < tempBitmap.getHeight()) {
                viewToBitmapHeightRatio = (double) heightSize / (double) tempBitmap.getHeight();
            }
            if (viewToBitmapWidthRatio != Double.POSITIVE_INFINITY
                    || viewToBitmapHeightRatio != Double.POSITIVE_INFINITY) {
                if (viewToBitmapWidthRatio <= viewToBitmapHeightRatio) {
                    desiredWidth = widthSize;
                    desiredHeight = (int) (tempBitmap.getHeight() * viewToBitmapWidthRatio);
                } else {
                    desiredHeight = heightSize;
                    desiredWidth = (int) (tempBitmap.getWidth() * viewToBitmapHeightRatio);
                }
            } else {
                desiredWidth = tempBitmap.getWidth();
                desiredHeight = tempBitmap.getHeight();
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
        operationController.changeAdjust(brightness, contrast);
    }

    public void saveAdjustBitmapState(int brightness, float contrast) {
        operationController.saveAdjustBitmapState(brightness, contrast);
    }

    public void rotateRight() {
        operationController.rotateRight();
    }

    public void rotateLeft() {
        operationController.rotateLeft();
    }

    public void flipVertical() {
        operationController.flipVertical();
    }

    public void flipHorizontal() {
        operationController.flipHorizontal();
    }

    public void undo() {
        operationController.undo();
    }

    public void reundo() {
        operationController.reundo();
    }

    public void applyCrop() {
        RectF crop = cropController.getCropShapeRect();
        PointF pointF = scaleAndRotationController.getScaledImageCoordinates();
        float[] scales = scaleAndRotationController.getScaleFactor();
        bitmapSrc = new Rect();
        float scaledLeft = pointF.x / scales[0];
        float scaledTop = pointF.y / scales[1];
        float scaledRight = (pointF.x + getWidth()) / scales[0];
        float scaledBottom = (pointF.y + getHeight()) / scales[1];
        bitmapSrc.left = (int) scaledLeft;
        bitmapSrc.top = (int) scaledTop;
        bitmapSrc.right = (int) scaledRight;
        bitmapSrc.bottom = (int) scaledBottom;
        operationController.applyCrop(scaleAndRotationController.getScaleFactor(), crop, bitmapSrc);
        matrix = new Matrix();
        scaleAndRotationController.dropToDefault(getHeight(), getWidth());
    }

    public Bitmap getImage() {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        draw(canvas);
        return bitmap;
    }

    @Override
    public void eraseStateChanged(ArrayList<EraseDrawContainer> paths) {
        operationController.eraseStateChanged(paths);
    }

    @Override
    public boolean isCropModeEnabled() {
        return isCroppingMode;
    }

    @Override
    public void onOperationDone(BitmapState state, boolean isUndo, boolean isReundo) {
//        if (isUndo || isReundo) {
//            if (state.dstPoint != null && state.cropShape != null) {
//                Bitmap bitmap = Bitmap.createBitmap(scaledBitmap, (int) (state.dstPoint.x / state.scales[0]), (int) (state.dstPoint.y / state.scales[1]),
//                        (int) (getWidth() / state.scales[0]), (int) (getHeight() / state.scales[1]));
//                Bitmap bitmap2 = Bitmap.createBitmap(bitmap, (int) (state.cropShape.left / state.scales[0]), (int) (state.cropShape.top / state.scales[1]),
//                        (int) ((state.cropShape.right - state.cropShape.left)/ state.scales[0]), (int) ((state.cropShape.bottom - state.cropShape.top) / state.scales[1]));
//                tempBitmap = bitmap;
//            } else {
//                tempBitmap = scaledBitmap;
//            }
//        }
        currentState = state;
        bitmapSrc = state.src;
        if (bitmapDst.bottom == 0 || bitmapDst.right == 0) {
            bitmapDst.bottom = getHeight();
            bitmapDst.right = getWidth();
        }
        eraseController.setPaths(currentState.getPaths());
        updateColorMatrix();
        if (adjustUndoReundoListener != null) {
            if (isUndo) {
                adjustUndoReundoListener.undo(currentState.brightness, currentState.contrast);
            }
            if (isReundo) {
                adjustUndoReundoListener.reundo(currentState.brightness, currentState.contrast);
            }
        }
        invalidate();
    }

    private void updateColorMatrix() {
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

    public interface AdjustUndoReundoListener {
        void undo(int brightness, float contrast);

        void reundo(int brightness, float contrast);
    }


}
