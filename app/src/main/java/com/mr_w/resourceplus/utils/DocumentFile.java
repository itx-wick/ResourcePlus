package com.mr_w.resourceplus.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class DocumentFile extends BaseFile implements Parcelable {
    private String mimeType;

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(getId());
        dest.writeString(getName());
        dest.writeString(getPath());
        dest.writeLong(getSize());
        dest.writeString(getBucketId());
        dest.writeString(getBucketName());
        dest.writeLong(getDate());
        dest.writeByte((byte) (isSelected() ? 1 : 0));
        dest.writeString(getMimeType());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<DocumentFile> CREATOR = new Creator<DocumentFile>() {
        @Override
        public DocumentFile[] newArray(int size) {
            return new DocumentFile[size];
        }

        @Override
        public DocumentFile createFromParcel(Parcel in) {
            DocumentFile file = new DocumentFile();
            file.setId(in.readLong());
            file.setName(in.readString());
            file.setPath(in.readString());
            file.setSize(in.readLong());
            file.setBucketId(in.readString());
            file.setBucketName(in.readString());
            file.setDate(in.readLong());
            file.setSelected(in.readByte() != 0);
            file.setMimeType(in.readString());
            return file;
        }
    };
}