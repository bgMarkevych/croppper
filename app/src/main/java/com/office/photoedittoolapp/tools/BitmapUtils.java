package com.office.photoedittoolapp.tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;

public class BitmapUtils {

    public static Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        Matrix matrix = new Matrix();
        matrix.setRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

    public static Bitmap cropBitmap(Bitmap bitmap, int x, int y, int width, int height) {
        return Bitmap.createBitmap(bitmap, x, y, width, height);
    }

    public static Bitmap changeBrightnessAndContrast(Bitmap bitmap, int brightness, float contrast) {
        ColorMatrix colorMatrix = new ColorMatrix(new float[]
                {
                        contrast, 0, 0, 0, brightness,
                        0, contrast, 0, 0, brightness,
                        0, 0, contrast, 0, brightness,
                        0, 0, 0, 1, 0
                });
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), bitmap.getConfig());
        Canvas canvas = new Canvas(tempBitmap);

        Paint paint = new Paint();
        paint.setColorFilter(new ColorMatrixColorFilter(colorMatrix));
        canvas.drawBitmap(bitmap, 0, 0, paint);
        return tempBitmap;
    }

    public static Bitmap flipImage(Bitmap bitmap, float sx, float sy) {
        Matrix matrix = new Matrix();
        matrix.postScale(sx, sy, bitmap.getWidth()/2f, bitmap.getHeight()/2f);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}
