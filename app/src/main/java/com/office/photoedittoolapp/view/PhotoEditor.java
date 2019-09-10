package com.office.photoedittoolapp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;

import com.office.photoedittoolapp.operations.ContrastAndBrightnessOperation;
import com.office.photoedittoolapp.operations.EditOperation;
import com.office.photoedittoolapp.operations.EraseOperation;
import com.office.photoedittoolapp.operations.FlipHorizontalOperation;
import com.office.photoedittoolapp.operations.FlipVerticalOperation;
import com.office.photoedittoolapp.operations.LeftRotationOperation;
import com.office.photoedittoolapp.operations.RightRotationOperation;
import com.office.photoedittoolapp.tools.BitmapState;
import com.office.photoedittoolapp.tools.BitmapUtils;
import com.office.photoedittoolapp.tools.CropController;
import com.office.photoedittoolapp.tools.EraseController;
import com.office.photoedittoolapp.tools.OperationResultContainer;
import com.office.photoedittoolapp.tools.ScaleAndRotationController;

import java.util.ArrayList;
import java.util.Stack;

public class PhotoEditor extends View implements EraseController.OnSaveEraseResultListener {

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

    private RectF bitmapDst = new RectF();
    private Matrix matrix = new Matrix();
    private Matrix zeroMatrix = new Matrix();
    private BitmapState state = new BitmapState();

    private Stack<EditOperation> operations;
    private Stack<EditOperation> undoOperations;

    private boolean isEraseMode;
    private boolean isCroppingMode;
    private float sidesAspect;

    public void setOriginBitmap(Bitmap bitmap) {
        this.originBitmap = bitmap;
        tempBitmap = originBitmap;
        operations = new Stack<>();
        undoOperations = new Stack<>();
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
        eraseController = new EraseController(this);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        bitmapDst.right = getWidth();
        bitmapDst.bottom = getHeight();
        sidesAspect = (float) originBitmap.getHeight() / getHeight();
        canvas.setMatrix(matrix);
        canvas.drawBitmap(tempBitmap, null, bitmapDst, null);
        if (isCroppingMode) {
            canvas.setMatrix(zeroMatrix);
            cropController.onDraw(canvas);
        }
        if (isEraseMode || eraseController.getPathArray().size() != 0) {
            eraseController.onDraw(canvas);
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

    public Bitmap saveCropResult() {
        Canvas canvas = new Canvas(tempBitmap);
        ArrayList<Path> paths = eraseController.getPathArray();
        Paint paint = eraseController.getPathPaint();
        for (Path path : paths) {
            canvas.drawPath(path, paint);
        }
        return tempBitmap;
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
    public void changeBrightnessAndContrast(int brightness, float contrast) {
        ContrastAndBrightnessOperation operation = (ContrastAndBrightnessOperation) operations.peek();
        operation.contrast = contrast / 100;
        operation.brightness = brightness - 255;
        OperationResultContainer container = operation.doOperation(scaledBitmap, state);
        tempBitmap = container.bitmap;
        state = container.bitmapState;
        invalidate();
    }

    public void saveStartBrightnessAndContrast(int prevBrightness, float prevContrast) {
        ContrastAndBrightnessOperation operation = new ContrastAndBrightnessOperation(prevContrast / 100, prevBrightness - 255);
        operations.add(operation);
    }

    public void rotateRight() {
        RightRotationOperation operation = new RightRotationOperation();
        operations.add(operation);
        OperationResultContainer container = operation.doOperation(scaledBitmap, state);
        tempBitmap = container.bitmap;
        state = container.bitmapState;
        invalidate();
    }

    public void rotateLeft() {
        LeftRotationOperation operation = new LeftRotationOperation();
        operations.add(operation);
        OperationResultContainer container = operation.doOperation(scaledBitmap, state);
        tempBitmap = container.bitmap;
        state = container.bitmapState;
        invalidate();
    }

    public void flipVertical() {
        FlipVerticalOperation operation = new FlipVerticalOperation();
        operations.add(operation);
        OperationResultContainer container = operation.doOperation(scaledBitmap, state);
        tempBitmap = container.bitmap;
        state = container.bitmapState;
        invalidate();
    }

    public void flipHorizontal() {
        FlipHorizontalOperation operation = new FlipHorizontalOperation();
        operations.add(operation);
        OperationResultContainer container = operation.doOperation(scaledBitmap, state);
        tempBitmap = container.bitmap;
        state = container.bitmapState;
        invalidate();
    }


    @Override
    public void saveEraseResult() {
        EraseOperation operation = new EraseOperation(eraseController.getPathPaint());
        for (int i = operations.size() - 1; i >= 0; i--) {
            EditOperation editOperation = operations.get(i);
            if (editOperation instanceof EraseOperation) {
                operation.paths.addAll(((EraseOperation) editOperation).paths);
                break;
            }
        }
        operation.paths.addAll(eraseController.getPathArray());
        operations.add(operation);
        OperationResultContainer container = operation.doOperation(scaledBitmap, state);
        state = container.bitmapState;
        tempBitmap = container.bitmap;
        invalidate();
    }

    public void undo() {
        if (operations.size() == 0) {
            return;
        }
        EditOperation editOperation = operations.pop();
        undoOperations.add(editOperation);
        if (editOperation instanceof ContrastAndBrightnessOperation) {
            if (adjustUndoReundoListener != null){
                adjustUndoReundoListener.undo(((ContrastAndBrightnessOperation) editOperation).prevBrightness, ((ContrastAndBrightnessOperation) editOperation).prevContrast);
            }
        }
        OperationResultContainer container = editOperation.undoOperation(scaledBitmap, state);
        state = container.bitmapState;
        tempBitmap = container.bitmap;
        invalidate();
    }

    public interface AdjustUndoReundoListener {
        void undo(int brightness, float contrast);

        void reundo(int brightness, float contrast);
    }


}
