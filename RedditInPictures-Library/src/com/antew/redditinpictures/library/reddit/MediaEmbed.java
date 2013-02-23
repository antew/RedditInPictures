package com.antew.redditinpictures.library.reddit;

import android.os.Parcel;
import android.os.Parcelable;

import com.antew.redditinpictures.library.utils.Util;

public class MediaEmbed implements Parcelable {
    private String  content;
    private int     width;
    private int     height;
    private boolean scrolling;

    public MediaEmbed(Parcel in) {
        content = in.readString();
        width = in.readInt();
        height = in.readInt();
        scrolling = in.readByte() == 1;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(content);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeByte(Util.parcelBoolean(scrolling));
    }

    //@formatter:off
    public static final Parcelable.Creator<MediaEmbed> CREATOR 
        = new Parcelable.Creator<MediaEmbed>() {
                 public MediaEmbed createFromParcel(Parcel in) {
                     return new MediaEmbed(in);
                 }

                 public MediaEmbed[] newArray(int size) {
                     return new MediaEmbed[size];
                 }
             };
    //@formatter:on
}