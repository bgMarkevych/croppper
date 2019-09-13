package com.office.photoedittoolapp.data;

import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class BitmapState implements Parcelable {
    private ArrayList<EraseDrawContainer> paths = new ArrayList<>();
    public int brightness = 0;
    public float contrast = 1f;
    private int rotate = 0;
    public boolean isFlipVertical;
    public boolean isFlipHorizontal;
    public RectF cropShape;
    public Bitmap croppedBitmap;

    public BitmapState() {
    }

    public BitmapState(BitmapState state) {
        setPaths(state.paths);
        brightness = state.brightness;
        contrast = state.contrast;
        rotate = state.rotate;
        isFlipVertical = state.isFlipVertical;
        isFlipHorizontal = state.isFlipHorizontal;
        cropShape = state.cropShape;
        croppedBitmap = state.croppedBitmap;
    }

    protected BitmapState(Parcel in) {
        paths = in.createTypedArrayList(EraseDrawContainer.CREATOR);
        brightness = in.readInt();
        contrast = in.readFloat();
        rotate = in.readInt();
        isFlipVertical = in.readByte() != 0;
        isFlipHorizontal = in.readByte() != 0;
        cropShape = in.readParcelable(RectF.class.getClassLoader());
        croppedBitmap = in.readParcelable(Bitmap.class.getClassLoader());
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
    }

    public int getRotate() {
        return rotate;
    }

    public ArrayList<EraseDrawContainer> getPaths() {
        return paths;
    }

    public void setPaths(ArrayList<EraseDrawContainer> paths) {
        this.paths.clear();
        this.paths.addAll(paths);
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
        dest.writeParcelable(cropShape, flags);
        dest.writeParcelable(croppedBitmap, flags);
    }
}
