package com.office.photoedittoolapp.operations;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;

import com.office.photoedittoolapp.tools.BitmapState;
import com.office.photoedittoolapp.tools.BitmapUtils;
import com.office.photoedittoolapp.tools.OperationResultContainer;

import java.util.ArrayList;

public class EraseOperation implements EditOperation {

    public EraseOperation(Paint paint) {
        this.paint = paint;
        this.paths = new ArrayList<>();
    }

    public Paint paint;
    public ArrayList<Path> paths;

    @Override
    public OperationResultContainer doOperation(Bitmap bitmap, BitmapState state) {
        Bitmap copyOriginBitmap = bitmap.copy(bitmap.getConfig(), true);
        Bitmap tempBitmap = BitmapUtils.mapStateIntoBitmapWithoutErase(copyOriginBitmap, state);
        Log.d("MATRIX", "doOperation: " + state.matrix.toShortString());
        Canvas canvas = new Canvas(tempBitmap);
        Paint tempPaint = new Paint(paint);
        tempPaint.setStrokeWidth(paint.getStrokeWidth());
        for (Path path : paths) {
            canvas.drawPath(path, tempPaint);
        }
        state.paths = paths;
        state.paint = paint;
        return new OperationResultContainer(state, tempBitmap);
    }

    @Override
    public OperationResultContainer undoOperation(Bitmap bitmap, BitmapState state) {
        Bitmap copyOriginBitmap = bitmap.copy(bitmap.getConfig(), true);
        Bitmap tempBitmap = BitmapUtils.mapStateIntoBitmapWithoutErase(copyOriginBitmap, state);
        Canvas canvas = new Canvas(tempBitmap);
        ArrayList<Path> tempPaths = paths;
        if (tempPaths.size() == 0){
            return new OperationResultContainer(state, tempBitmap);
        }
        tempPaths.remove(tempPaths.size() - 1);
        for (Path path : tempPaths) {
            canvas.drawPath(path, paint);
        }
        state.paths = tempPaths;
        Bitmap finalBitmap = BitmapUtils.mapStateIntoBitmap(bitmap, state);
        return new OperationResultContainer(state, finalBitmap);
    }
}
