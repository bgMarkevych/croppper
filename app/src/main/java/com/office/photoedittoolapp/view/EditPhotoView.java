package com.office.photoedittoolapp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.photoedittoolapp.tools.BitmapUtils;
import com.office.photoedittoolapp.tools.OnCropScaleListener;
import com.office.photoedittoolapp.tools.ScaleController;

import java.util.ArrayList;


public class EditPhotoView extends FrameLayout implements OnCropScaleListener {
    private static final String TAG = EditPhotoView.class.getSimpleName();
    public EditPhotoView(@NonNull Context context) {
        super(context);
        init(context);
    }

    public EditPhotoView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public EditPhotoView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private ImageView imageView;
    private Bitmap originBitmap;
    private Bitmap bitmap;
    private CropOverlayView cropView;
    private EraseView eraseView;
    private ScaleController scaleController;

    private boolean isEraseMode;
    private boolean isCroppingMode;

    private void init(Context context) {
        imageView = new ImageView(context);
        addView(imageView);
        scaleController = new ScaleController();
        ViewTreeObserver viewTreeObserver = imageView.getViewTreeObserver();
        if (viewTreeObserver.isAlive()) {
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    imageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = imageView.getWidth();
                    int height = imageView.getHeight();
                    scaleController.setParentSize(height, width);
                    bitmap = Bitmap.createScaledBitmap(originBitmap, width, height, true);
                    imageView.setImageBitmap(bitmap);
                }
            });
        }
    }

    public void setOriginBitmap(Bitmap bitmap) {
        this.originBitmap = bitmap;
    }

    public void enableCropMode() {
        isCroppingMode = true;
        cropView = new CropOverlayView(getContext());
        cropView.setOnCropScaleListener(this);
        cropView.setParentSize(getHeight(), getWidth());
        addView(cropView);
        requestLayout();
    }

    public void disableCropMode() {
        isCroppingMode = false;
        if (cropView != null) {
            removeView(cropView);
            cropView = null;
        }
    }

    public boolean isCroppingMode() {
        return isCroppingMode;
    }

    public void enableEraseMode() {
        isEraseMode = true;
        eraseView = new EraseView(getContext());
        addView(eraseView);
    }

    public void disableEraseMode(){
        isEraseMode = false;
        if (eraseView != null){
            removeView(eraseView);
            eraseView = null;
        }
    }

    public boolean isEraseMode(){
        return isEraseMode;
    }

    public Bitmap getEraseResult(){
        Bitmap eraseBitmap = Bitmap.createBitmap(bitmap);
        Canvas canvas = new Canvas(eraseBitmap);
        ArrayList<Path> paths = eraseView.getPathArray();
        for (Path path: paths){
            canvas.drawPath(path, eraseView.getPathPaint());
        }
        return eraseBitmap;
    }

    public Bitmap getCroppedImage() {
        float[] scales = scaleController.getScaleFactor();
        Bitmap scaledBitmap = bitmap;
        if (scales[0] > 0 && scales[1] > 0) {
            PointF scaledPoints = scaleController.getScaledImageCoordinates();
            Bitmap bigBitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scales[0]), (int) (bitmap.getHeight() * scales[1]), true);
            scaledBitmap = BitmapUtils.cropBitmap(bigBitmap, (int) scaledPoints.x, (int) scaledPoints.y, imageView.getWidth(), imageView.getHeight());
        }
        RectF cropShapeRect = cropView.getCropShapeRect();
        return BitmapUtils.cropBitmap(scaledBitmap, (int) cropShapeRect.left, (int) cropShapeRect.top, (int) (cropShapeRect.right - cropShapeRect.left), (int) (cropShapeRect.bottom - cropShapeRect.top));
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthFinal;
        int heightFinal;
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
            widthFinal = width;
            heightFinal = height;
            setMeasuredDimension(width, height);
        } else {
            widthFinal = widthSize;
            heightFinal = heightSize;
            setMeasuredDimension(widthSize, heightSize);
        }
        measureChild(imageView, MeasureSpec.makeMeasureSpec(widthFinal, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightFinal, MeasureSpec.EXACTLY));
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

    @Override
    public void onActionDown(MotionEvent event) {
        imageView.setImageMatrix(scaleController.onActionDown(event));
    }

    @Override
    public void onPointerDown(MotionEvent event) {
        imageView.setImageMatrix(scaleController.onPointerDown(event));
    }

    @Override
    public void onScale(MotionEvent event) {
        imageView.setImageMatrix(scaleController.onScale(event));
    }

    @Override
    public void onPointerUp(MotionEvent event) {
        scaleController.onPointerUp();
    }

    @Override
    public void onMove(MotionEvent event) {
        imageView.setImageMatrix(scaleController.onMove(event));
    }

}
