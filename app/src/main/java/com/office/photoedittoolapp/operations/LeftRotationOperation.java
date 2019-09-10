package com.office.photoedittoolapp.operations;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.util.Log;

import com.office.photoedittoolapp.tools.BitmapState;
import com.office.photoedittoolapp.tools.BitmapUtils;
import com.office.photoedittoolapp.tools.OperationResultContainer;

import java.util.ArrayList;

public class LeftRotationOperation implements EditOperation {
    @Override
    public OperationResultContainer doOperation(Bitmap bitmap, BitmapState state) {
        Bitmap tempBitmap = BitmapUtils.mapStateIntoBitmapWithoutMatrix(bitmap, state);
        Bitmap mutable = tempBitmap.copy(tempBitmap.getConfig(), true);
        Canvas canvas = new Canvas(mutable);
        canvas.rotate(90);
        canvas.drawRect(0,0,200,200, state.paint);
//        Matrix tempMatrix = new Matrix();
//        state.setRotate(state.getRotate() + 90);
//        tempMatrix.postRotate(state.getRotate(), tempBitmap.getWidth() / 2f, tempBitmap.getHeight() / 2f);
//        state.matrix = tempMatrix;
//        return new OperationResultContainer(state, Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), tempMatrix, true));
        return new OperationResultContainer(state, mutable);
    }

    @Override
    public OperationResultContainer undoOperation(Bitmap bitmap, BitmapState state) {
        Bitmap tempBitmap = BitmapUtils.mapStateIntoBitmapWithoutMatrix(bitmap, state);
        Matrix tempMatrix = new Matrix();
        state.setRotate(state.getRotate() - 90);
        tempMatrix.postRotate(state.getRotate(), tempBitmap.getWidth() / 2f, tempBitmap.getHeight() / 2f);
        state.matrix = tempMatrix;
        return new OperationResultContainer(state, Bitmap.createBitmap(tempBitmap, 0, 0, tempBitmap.getWidth(), tempBitmap.getHeight(), tempMatrix, true));

    }

}