package com.office.photoedittoolapp.tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

public class BitmapUtils {

    public static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        Matrix matrix = new Matrix();
        matrix.setRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap cropBitmap(Bitmap bitmap, int x, int y, int width, int height) {
        return Bitmap.createBitmap(bitmap, x, y, width, height);
    }

    public static Bitmap changeBrightnessAndContrast(Bitmap bitmap, Matrix matrix, int brightness, float contrast) {
        ColorMatrix colorMatrix = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        Canvas canvas = new Canvas(tempBitmap);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return tempBitmap;
    }

    public static Bitmap mapStateIntoBitmap(Bitmap bitmap, BitmapState state) {
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), state.matrix, true);
        ColorMatrix colorMatrix = new ColorMatrix(new float[]
                {
                        state.contrast, 0, 0, 0, state.brightness,
                        0, state.contrast, 0, 0, state.brightness,
                        0, 0, state.contrast, 0, state.brightness,
                        0, 0, 0, 1, 0
                });
        Canvas canvas = new Canvas(tempBitmap);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(tempBitmap, 0, 0, paint);
        for (Path path : state.paths) {
            canvas.drawPath(path, state.paint);
        }
        return tempBitmap;
    }

    public static Bitmap mapStateIntoBitmapWithoutErase(Bitmap bitmap, BitmapState state) {
        Log.d("MATRIX", "doEraseOperation: " + state.matrix.toShortString());
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), state.matrix, true);
        ColorMatrix colorMatrix = new ColorMatrix(new float[]
                {
                        state.contrast, 0, 0, 0, state.brightness,
                        0, state.contrast, 0, 0, state.brightness,
                        0, 0, state.contrast, 0, state.brightness,
                        0, 0, 0, 1, 0
                });
        Canvas canvas = new Canvas(tempBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(tempBitmap, 0, 0, paint);
        return tempBitmap;
    }

    public static Bitmap mapStateIntoBitmapWithoutAdjust(Bitmap bitmap, BitmapState state) {
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), state.matrix, true);
        Canvas canvas = new Canvas(tempBitmap);
        for (Path path : state.paths) {
            canvas.drawPath(path, state.paint);
        }
        return tempBitmap;
    }

    public static Bitmap mapStateIntoBitmapWithoutMatrix(Bitmap bitmap, BitmapState state) {
        Matrix matrix = new Matrix();
        matrix.postScale(state.isFlipHorizontal ? -1 : 1, 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        matrix.postScale(1, state.isFlipVertical ? -1 : 1, bitmap.getWidth() / 2f, bitmap.getHeight() / 2f);
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        ColorMatrix colorMatrix = new ColorMatrix(new float[]
                {
                        state.contrast, 0, 0, 0, state.brightness,
                        0, state.contrast, 0, 0, state.brightness,
                        0, 0, state.contrast, 0, state.brightness,
                        0, 0, 0, 1, 0
                });
        Canvas canvas = new Canvas(tempBitmap);
        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(tempBitmap, 0, 0, paint);
        for (Path path : state.paths) {
            path.transform(matrix);
            canvas.drawPath(path, state.paint);
        }
        return tempBitmap;
    }

}
