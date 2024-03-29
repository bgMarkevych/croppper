package com.office.photoedittoolapp.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class BitmapState implements Parcelable {
    private ArrayList<EraseDrawContainer> paths = new ArrayList<>();
    public int brightness = 0;
    public float contrast = 1f;
    private int rotate = 0;
    private boolean isFlipVertical;
    private boolean isFlipHorizontal;

    public BitmapState() {
    }

    public BitmapState(BitmapState state) {
        setPaths(state.paths);
        brightness = state.brightness;
        contrast = state.contrast;
        rotate = state.rotate;
        isFlipVertical = state.isFlipVertical;
        isFlipHorizontal = state.isFlipHorizontal;
    }

    protected BitmapState(Parcel in) {
        paths = in.createTypedArrayList(EraseDrawContainer.CREATOR);
        brightness = in.readInt();
        contrast = in.readFloat();
        rotate = in.readInt();
        isFlipVertical = in.readByte() != 0;
        isFlipHorizontal = in.readByte() != 0;
    }

    public static final Creator<BitmapState> CREATOR = new Creator<BitmapState>() {
        @Override
        public BitmapState createFromParcel(Parcel in) {
            return new BitmapState(in);
        }

        @Override
        public BitmapState[] newArray(int size) {
            return new BitmapState[size];
        }
    };

    public void setRotate(int rotate) {
        this.rotate += rotate;
        this.rotate = this.rotate == 360 || this.rotate == -360 ? 0 : this.rotate;
        updatePathsRotations(rotate);
    }

    private void updatePathsRotations(int rotate) {
        for (EraseDrawContainer drawContainer : paths) {
            drawContainer.rotate += rotate;
            drawContainer.rotate = drawContainer.rotate == 360 || drawContainer.rotate == -360 ? 0 : drawContainer.rotate;
        }
    }

    private void updatePathsFlipVertical() {
        for (EraseDrawContainer drawContainer : paths) {
            drawContainer.isFlipVertical = !drawContainer.isFlipVertical;
        }
    }

    private void updatePathsFlipHorizontal() {
        for (EraseDrawContainer drawContainer : paths) {
            drawContainer.isFlipHorizontal = !drawContainer.isFlipHorizontal;
        }
    }

    public boolean isFlipVertical() {
        return isFlipVertical;
    }

    public void setFlipVertical(boolean flipVertical) {
        isFlipVertical = flipVertical;
        updatePathsFlipVertical();
    }

    public boolean isFlipHorizontal() {
        return isFlipHorizontal;
    }

    public void setFlipHorizontal(boolean flipHorizontal) {
        isFlipHorizontal = flipHorizontal;
        updatePathsFlipHorizontal();
    }

    public void dropRotate() {
        rotate = 0;
    }

    public int getRotate() {
        return rotate;
    }

    public ArrayList<EraseDrawContainer> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<EraseDrawContainer> paths) {
        this.paths = new ArrayList<>();
        for (EraseDrawContainer eraseDrawContainer : paths) {
            EraseDrawContainer drawContainer = new EraseDrawContainer(eraseDrawContainer.path, eraseDrawContainer.rotate,
                    eraseDrawContainer.strokeWidth, eraseDrawContainer.isFlipHorizontal, eraseDrawContainer.isFlipVertical);
            this.paths.add(drawContainer);
        }
    }

    public void clearPaths() {
        this.paths.clear();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(paths);
        dest.writeInt(brightness);
        dest.writeFloat(contrast);
        dest.writeInt(rotate);
        dest.writeByte((byte) (isFlipVertical ? 1 : 0));
        dest.writeByte((byte) (isFlipHorizontal ? 1 : 0));
    }
}
