package com.antew.redditinpictures.library.model.reddit;

import android.os.Parcel;
import android.os.Parcelable;

public class Children implements Parcelable {
    public static final Parcelable.Creator<Children> CREATOR = new Parcelable.Creator<Children>() {

        @Override
        public Children createFromParcel(Parcel source) {
            return new Children(source);
        }

        @Override
        public Children[] newArray(int size) {
            return new Children[size];
        }
    };
    private String   kind;
    private PostData data;

    public Children(Parcel source) {
        kind = source.readString();
        data = source.readParcelable(PostData.class.getClassLoader());
    }

    public String getKind() {
        return kind;
    }

    public PostData getData() {
        return data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(kind);
        dest.writeParcelable(data, flags);
    }
}