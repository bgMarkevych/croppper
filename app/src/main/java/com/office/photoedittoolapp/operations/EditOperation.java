package com.office.photoedittoolapp.operations;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Path;

import com.office.photoedittoolapp.tools.BitmapState;
import com.office.photoedittoolapp.tools.OperationResultContainer;

import java.util.ArrayList;

public interface EditOperation {
    OperationResultContainer doOperation(Bitmap bitmap, BitmapState state);

    OperationResultContainer undoOperation(Bitmap bitmap, BitmapState state);
}
