package com.office.photoedittoolapp.operations;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;

import java.util.ArrayList;

public class EraseOperation implements EditOperation {

    public EraseOperation(Paint paint) {
        this.paint = paint;
        this.paths = new ArrayList<>();
    }

    public Paint paint;
    public ArrayList<Path> paths;

    @Override
    public Bitmap doOperation(Bitmap bitmap) {
        Bitmap tempBitmap = bitmap.copy(bitmap.getConfig(), true);
        Canvas canvas = new Canvas(tempBitmap);
        Paint tempPaint = new Paint(paint);
        tempPaint.setStrokeWidth(paint.getStrokeWidth());
        for (Path path : paths) {
            canvas.drawPath(path, tempPaint);
        }
        return tempBitmap;
    }

    @Override
    public Bitmap undoOperation(Bitmap bitmap) {
        Bitmap tempBitmap = bitmap.copy(bitmap.getConfig(), true);
        Canvas canvas = new Canvas(tempBitmap);
        ArrayList<Path> tempPaths = paths;
        if (tempPaths.size() == 0){
            return tempBitmap;
        }
        tempPaths.remove(tempPaths.size() - 1);
        for (Path path : tempPaths) {
            canvas.drawPath(path, paint);
        }
        return tempBitmap;
    }
}
