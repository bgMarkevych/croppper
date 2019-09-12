package com.office.photoedittoolapp.tools;


import android.graphics.Path;

public class EraseDrawContainer {
    public Path path;
    public Integer rotate;
    public boolean isFlipHorizontal;
    public boolean isFlipVertical;

    public EraseDrawContainer(Path path, Integer rotate, boolean isFlipHorizontal, boolean isFlipVertical) {
        this.path = path;
        this.rotate = rotate;
        this.isFlipHorizontal = isFlipHorizontal;
        this.isFlipVertical = isFlipVertical;
    }
}
