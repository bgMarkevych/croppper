package com.office.photoedittoolapp.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.office.photoedittoolapp.R;
import com.office.photoedittoolapp.tools.BitmapUtils;
import com.office.photoedittoolapp.tools.OnCropScaleListener;
import com.office.photoedittoolapp.tools.ScaleController;


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
    private RelativeLayout parent;
    private ScaleController scaleController;

    private void init(Context context) {
        parent = (RelativeLayout) LayoutInflater.from(context).inflate(R.layout.crop_view, null);
        imageView = parent.findViewById(R.id.image_container);
        cropView = parent.findViewById(R.id.crop_shape);
        addView(parent);
    }

    public void setBitmap(Bitmap bitmap) {
        this.originBitmap = bitmap;
        scaleController = new ScaleController();
        cropView.setOnCropScaleListener(this);
    }

    public Bitmap getCroppedImage() {
        float[] scales = scaleController.getScaleFactor();
        Bitmap scaledBitmap = bitmap;
        if (scales[0] > 0 && scales[1] > 0){
            PointF scaledPoints = scaleController.getScaledImageCoordinates();
            Bitmap bigBitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * scales[0]), (int) (bitmap.getHeight() * scales[1]), true);
            scaledBitmap = BitmapUtils.cropBitmap(bigBitmap, (int) scaledPoints.x, (int) scaledPoints.y, imageView.getWidth(), imageView.getHeight());
        }
        RectF cropShapeRect = cropView.getCropShapeRect();
        Log.d(TAG, "getCroppedImage: cropShape" + cropShapeRect.toShortString());
        Log.d(TAG, "getCroppedImage: scaled bitmap height" + scaledBitmap.getHeight() + " width " + scaledBitmap.getWidth());
//        return BitmapUtils.cropBitmap(scaledBitmap, (int) cropShapeRect.left, (int) cropShapeRect.top, (int) cropShapeRect.right, (int) cropShapeRect.bottom);
        return scaledBitmap;
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
        measureChild(parent, MeasureSpec.makeMeasureSpec(widthFinal, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightFinal, MeasureSpec.EXACTLY));
        cropView.setParentSize(heightFinal, widthFinal);
        scaleController.setParentSize(heightFinal, widthFinal);
        bitmap = Bitmap.createScaledBitmap(originBitmap, widthFinal, heightFinal, true);
        imageView.setImageBitmap(bitmap);
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
