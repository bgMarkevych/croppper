package com.office.photoedittoolapp.data;


import android.os.Parcel;
import android.os.Parcelable;

public class EraseDrawContainer implements Parcelable {
    public PathS path;
    public int rotate;
    public int strokeWidth;
    public boolean isFlipHorizontal;
    public boolean isFlipVertical;

    public EraseDrawContainer(PathS path, int rotate, int strokeWidth) {
        this.path = path;
        this.rotate = rotate;
        this.strokeWidth = strokeWidth;
    }

    public EraseDrawContainer(PathS path, int rotate, int strokeWidth, boolean isFlipHorizontal, boolean isFlipVertical) {
        this.path = path;
        this.rotate = rotate;
        this.strokeWidth = strokeWidth;
        this.isFlipHorizontal = isFlipHorizontal;
        this.isFlipVertical = isFlipVertical;
    }

    protected EraseDrawContainer(Parcel in) {
        if (in.readByte() == 0) {
            rotate = 0;
            path = null;
        } else {
            rotate = in.readInt();
            path = (PathS) in.readSerializable();
            isFlipHorizontal = in.readInt() == 1;
            isFlipVertical = in.readInt() == 1;
        }
    }

    public static final Creator<EraseDrawContainer> CREATOR = new Creator<EraseDrawContainer>() {
        @Override
        public EraseDrawContainer createFromParcel(Parcel in) {
            return new EraseDrawContainer(in);
        }

        @Override
        public EraseDrawContainer[] newArray(int size) {
            return new EraseDrawContainer[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
            dest.writeByte((byte) 1);
            dest.writeInt(rotate);
            dest.writeSerializable(path);
            dest.writeInt(strokeWidth);
            dest.writeInt(isFlipHorizontal ? 1 : 0);
            dest.writeInt(isFlipVertical ? 1 : 0);
    }
}
