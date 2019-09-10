package com.office.photoedittoolapp.operations;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import com.office.photoedittoolapp.tools.BitmapState;
import com.office.photoedittoolapp.tools.BitmapUtils;
import com.office.photoedittoolapp.tools.OperationResultContainer;


public class RightRotationOperation implements EditOperation {
    @Override
    public OperationResultContainer doOperation(Bitmap bitmap, BitmapState state) {
        Bitmap tempBitmap = BitmapUtils.mapStateIntoBitmapWithoutMatrix(bitmap, state);
        Matrix tempMatrix = new Matrix();
        state.setRotate(state.getRotate() - 90);
        tempMatrix.postRotate(state.getRotate(), tempBitmap.getWidth() / 2f, tempBitmap.getHeight() / 2f);
        state.matrix = tempMatrix;
        return new OperationResultContainer(state, Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), tempMatrix, true));
    }

    @Override
    public OperationResultContainer undoOperation(Bitmap bitmap, BitmapState state) {
        Bitmap tempBitmap = BitmapUtils.mapStateIntoBitmapWithoutMatrix(bitmap, state);
        Matrix tempMatrix = new Matrix();
        state.setRotate(state.getRotate() + 90);
        tempMatrix.postRotate(state.getRotate(), tempBitmap.getWidth() / 2f, tempBitmap.getHeight() / 2f);
        state.matrix = tempMatrix;
        return new OperationResultContainer(state, Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), tempMatrix, true));

    }

}
