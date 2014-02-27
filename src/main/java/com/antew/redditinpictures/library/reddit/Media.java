package com.antew.redditinpictures.library.reddit;

import android.os.Parcel;
import android.os.Parcelable;

public class Media implements Parcelable {
    private String type;
    private Oembed oembed;

    public Media(Parcel source) {
        type = source.readString();
        oembed = source.readParcelable(Oembed.class.getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(type);
        dest.writeParcelable(oembed, flags);
    }

    //@formatter:off
    public static final Parcelable.Creator<Media> CREATOR
        = new Parcelable.Creator<Media>() {

            @Override
            public Media createFromParcel(Parcel source) {
                return new Media(source);
            }

            @Override
            public Media[] newArray(int size) {
                return new Media[size];
            }
            
        
    };
    //@formatter:on
}