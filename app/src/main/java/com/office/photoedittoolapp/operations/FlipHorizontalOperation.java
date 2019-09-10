package com.office.photoedittoolapp.operations;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import com.office.photoedittoolapp.tools.BitmapState;
import com.office.photoedittoolapp.tools.BitmapUtils;
import com.office.photoedittoolapp.tools.OperationResultContainer;

public class FlipHorizontalOperation implements EditOperation {

    @Override
    public OperationResultContainer doOperation(Bitmap bitmap, BitmapState state) {
        Bitmap tempBitmap = BitmapUtils.mapStateIntoBitmap(bitmap, state);
        Matrix matrix = new Matrix(state.matrix);
        if (state.isFlipHorizontal) {
            matrix.postScale(1, 1, tempBitmap.getWidth() / 2f, tempBitmap.getHeight() / 2f);
            state.isFlipHorizontal = false;
        } else {
            matrix.postScale(-1, 1, tempBitmap.getWidth() / 2f, tempBitmap.getHeight() / 2f);
            state.isFlipHorizontal = true;
        }
        state.matrix = matrix;
        return new OperationResultContainer(state, Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), matrix, true));
    }

    @Override
    public OperationResultContainer undoOperation(Bitmap bitmap, BitmapState state) {
        return doOperation(bitmap, state);
    }
}
