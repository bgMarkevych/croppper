package com.office.photoedittoolapp.tools;


import android.graphics.Path;

public class EraseDrawContainer {
    public Path path;
    public Integer rotate;

    public EraseDrawContainer(Path path, Integer rotate) {
        this.path = path;
        this.rotate = rotate;
    }
}
