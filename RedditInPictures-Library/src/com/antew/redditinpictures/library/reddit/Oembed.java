package com.antew.redditinpictures.library.reddit;

import android.os.Parcel;
import android.os.Parcelable;

public class Oembed implements Parcelable {
    private String provider_url;
    private String description;
    private String title;
    private String url;
    private String author_name;
    private int    height;
    private int    width;
    private String html;
    private int    thumbnail_width;
    private String version;
    private String provider_name;
    private String thumbnail_url;
    private String type;
    private int    thumbnail_height;
    private String author_url;
    
    
    public Oembed(Parcel source) {
        provider_url = source.readString();
        description = source.readString();
        title = source.readString();
        url = source.readString();
        author_name = source.readString();
        height = source.readInt();
        width = source.readInt();
        html = source.readString();
        thumbnail_width = source.readInt();
        version = source.readString();
        provider_name = source.readString();
        thumbnail_url = source.readString();
        type = source.readString();
        thumbnail_height = source.readInt();
        author_url = source.readString();
    }
    
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(provider_url);
        dest.writeString(description);
        dest.writeString(title);
        dest.writeString(url);
        dest.writeString(author_name);
        dest.writeInt(height);
        dest.writeInt(width);
        dest.writeString(html);
        dest.writeInt(thumbnail_width);
        dest.writeString(version);
        dest.writeString(provider_name);
        dest.writeString(thumbnail_url);
        dest.writeString(type);
        dest.writeInt(thumbnail_height);
        dest.writeString(url);
    }
    
  //@formatter:off
    public static final Parcelable.Creator<Oembed> CREATOR
        = new Parcelable.Creator<Oembed>() {

            @Override
            public Oembed createFromParcel(Parcel source) {
                return new Oembed(source);
            }

            @Override
            public Oembed[] newArray(int size) {
                return new Oembed[size];
            }
            
        
    };
    //@formatter:on
}
