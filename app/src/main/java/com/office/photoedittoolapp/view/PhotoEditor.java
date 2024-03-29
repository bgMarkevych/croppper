package com.office.photoedittoolapp.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.Nullable;

import com.office.photoedittoolapp.data.BitmapState;
import com.office.photoedittoolapp.tools.CropController;
import com.office.photoedittoolapp.tools.EraseController;
import com.office.photoedittoolapp.data.EraseDrawContainer;
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
    private Bitmap tempBitmap;
    private ScaleAndRotationController scaleAndRotationController;
    private CropController cropController;
    private EraseController eraseController;
    private OperationController operationController;
    private AdjustUndoReundoListener adjustUndoReundoListener;

    private Paint adjustPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private RectF bitmapDst = new RectF();
    private Matrix matrix = new Matrix();
    private Matrix zeroMatrix = new Matrix();
    private ColorMatrix colorMatrix = new ColorMatrix();
    private ColorMatrixColorFilter colorMatrixColorFilter;

    private boolean isEraseMode = true;
    private boolean isCroppingMode;
    private boolean isImageCropped;

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
                }
            });
        }
    }

    public void setAdjustUndoReundoListener(AdjustUndoReundoListener adjustUndoReundoListener) {
        this.adjustUndoReundoListener = adjustUndoReundoListener;
    }

    private void init() {
        Log.d(TAG, "init: ");
        scaleAndRotationController = new ScaleAndRotationController();
        cropController = new CropController();
        eraseController = new EraseController(this);
        operationController = new OperationController(this);
        currentState = operationController.getCurrentState();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG, "onDraw: ");
        canvas.save();
        canvas.drawColor(Color.WHITE);
        canvas.setMatrix(matrix);
        canvas.save();
        canvas.rotate(currentState.getRotate(), getWidth() / 2f, getHeight() / 2f);
        if (colorMatrixColorFilter != null) {
            adjustPaint.setColorFilter(colorMatrixColorFilter);
        }
        canvas.scale(!currentState.isFlipHorizontal() ? 1 : -1, !currentState.isFlipVertical() ? 1 : -1, getWidth() / 2f, getHeight() / 2f);
        canvas.drawBitmap(tempBitmap, null, bitmapDst, adjustPaint);
        canvas.restore();
        eraseController.onDraw(canvas, getWidth(), getHeight());
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
            widthSize = getOnMeasureSpec(widthMode, widthSize, desiredWidth);
            heightSize = getOnMeasureSpec(heightMode, heightSize, desiredHeight);
        }
        setMeasuredDimension(widthSize, heightSize);
        if (currentState != null && tempBitmap != null && isImageCropped) {
            float ratio = (float) heightSize / (float) widthSize;
            float height = (tempBitmap.getHeight() * ratio);
            bitmapDst.top = heightSize / 2f - height / 2f;
            bitmapDst.right = widthSize;
            bitmapDst.bottom = bitmapDst.top + height;
        } else {
            bitmapDst.top = 0;
            bitmapDst.right = widthSize;
            bitmapDst.bottom = heightSize;
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        scaleAndRotationController.setParentSize(getHeight(), getWidth());
        cropController.setParentSize(getHeight(), getWidth());
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
        if (!isCroppingMode) {
            return;
        }
        isCroppingMode = false;
        invalidate();
        Bitmap bitmap = getImage(true);
        isCroppingMode = true;
        invalidate();
        RectF crop = cropController.getCropShapeRect();
        tempBitmap = Bitmap.createBitmap(bitmap, (int) crop.left, (int) crop.top, (int) (crop.right - crop.left), (int) (crop.bottom - crop.top));
        matrix = scaleAndRotationController.dropToDefault();
        isImageCropped = true;
        requestLayout();
        operationController.applyCrop();
    }

    private Bitmap getImage(boolean isCrop) {
        Bitmap bitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        if (isCrop) {
            int brightness = currentState.brightness;
            float contrast = currentState.contrast;
            currentState.brightness = 0;
            currentState.contrast = 1f;
            updateColorMatrix();
            invalidate();
            draw(canvas);
            currentState.brightness = brightness;
            currentState.contrast = contrast;
            updateColorMatrix();
            invalidate();
        } else {
            draw(canvas);
        }
        return bitmap;
    }

    public Bitmap getImage() {
        return getImage(false);
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
    public void onBitmapStateChanged(BitmapState state, boolean isUndo, boolean isReundo) {
        currentState = state;
        eraseController.setPaths(new ArrayList<>(currentState.getPaths()));
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
        requestLayout();
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

    @Nullable
    @Override
    protected Parcelable onSaveInstanceState() {
        MyState myState = new MyState(super.onSaveInstanceState());
        operationController.onSaveInstanceState(myState);
        float[] values = new float[9];
        matrix.getValues(values);
        myState.values = values;
        myState.isCroppingMode = isCroppingMode;
        Log.d(TAG, "onSaveInstanceState: ");
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!(state instanceof MyState)) {
            super.onRestoreInstanceState(state);
            return;
        }
        MyState myState = ((MyState) state);
        super.onRestoreInstanceState(myState.getSuperState());
        operationController.onRestoreInstanceState(myState);
        matrix.setValues(myState.values);
        scaleAndRotationController.setMatrix(matrix);
        isCroppingMode = myState.isCroppingMode;
        invalidate();
        Log.d(TAG, "onRestoreInstanceState: ");
    }

    public static class MyState extends BaseSavedState {

        public ArrayList<BitmapState> states;
        public ArrayList<BitmapState> undoStates;
        public BitmapState currentState;
        public boolean isCroppingMode;
        public boolean isImageCropped;
        public float[] values;

        public MyState(Parcelable superState) {
            super(superState);
        }

        @TargetApi(24)
        public MyState(Parcel source, ClassLoader loader) {
            super(source, loader);
            states = new ArrayList<>();
            source.readList(states, BitmapState.class.getClassLoader());
            undoStates = new ArrayList<>();
            source.readList(undoStates, BitmapState.class.getClassLoader());
            currentState = source.readParcelable(BitmapState.class.getClassLoader());
            isCroppingMode = source.readInt() == 1;
            isImageCropped = source.readInt() == 1;
            values = new float[9];
            source.readFloatArray(values);
        }

        public MyState(Parcel source) {
            super(source);
            states = new ArrayList<>();
            source.readList(states, BitmapState.class.getClassLoader());
            undoStates = new ArrayList<>();
            source.readList(undoStates, BitmapState.class.getClassLoader());
            currentState = source.readParcelable(BitmapState.class.getClassLoader());
            isCroppingMode = source.readInt() == 1;
            isImageCropped = source.readInt() == 1;
            values = new float[9];
            source.readFloatArray(values);
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeList(states);
            dest.writeList(undoStates);
            dest.writeParcelable(currentState, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
            dest.writeInt(isCroppingMode ? 1 : 0);
            dest.writeInt(isImageCropped ? 1 : 0);
            dest.writeFloatArray(values);
        }

        public static final Parcelable.Creator<MyState> CREATOR =
                new Parcelable.Creator<MyState>() {
                    public MyState createFromParcel(Parcel in) {
                        return new MyState(in);
                    }

                    public MyState[] newArray(int size) {
                        return new MyState[size];
                    }
                };
    }

    public interface AdjustUndoReundoListener {
        void undo(int brightness, float contrast);

        void reundo(int brightness, float contrast);
    }


}
