package com.office.photoedittoolapp.data;


import android.os.Parcel;
import android.os.Parcelable;

public class EraseDrawContainer implements Parcelable {
    public PathS path;
    public Integer rotate;
    public int strokeWidth;
    public boolean isFlipHorizontal;
    public boolean isFlipVertical;

    public EraseDrawContainer(PathS path, Integer rotate) {
        this.path = path;
        this.rotate = rotate;
    }

    protected EraseDrawContainer(Parcel in) {
        if (in.readByte() == 0) {
            rotate = null;
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
        if (rotate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(rotate);
            dest.writeSerializable(path);
            dest.writeInt(strokeWidth);
            dest.writeInt(isFlipHorizontal ? 1 : 0);
            dest.writeInt(isFlipVertical ? 1 : 0);
        }
    }
}
