package com.office.photoedittoolapp.operations;

import android.graphics.Bitmap;

public interface EditOperation {
    Bitmap doOperation(Bitmap bitmap);

    Bitmap undoOperation(Bitmap bitmap);
}
