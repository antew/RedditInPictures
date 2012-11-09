package com.antew.redditinpictures.library.imgur;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;

public class ImgurAlbumApi {
    public Album getAlbum() {
        return album;
    }

    private Album album;

    public static class Album implements Parcelable {
        
        private String   title;
        private String   description;
        private String   cover;
        private String   layout;
        List<ImgurImage> images;
        
        public Album(Parcel source) {
            title = source.readString();
            description = source.readString();
            cover = source.readString();
            layout = source.readString();
            images = new ArrayList<ImgurImage>();
            source.readList(images,ImgurImage.class.getClassLoader());
                    
        }

        //@formatter:off
        public String getTitle()            { return title; }
        public String getDescription()      { return description; }
        public String getCover()            { return cover; }
        public String getLayout()           { return layout; }
        public List<ImgurImage> getImages() { return images; }
        //@formatter:on

        
        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(title);
            dest.writeString(description);
            dest.writeString(cover);
            dest.writeString(layout);
            dest.writeList(images);
        }
        
        //@formatter:off
        public static final Parcelable.Creator<Album> CREATOR
            = new Parcelable.Creator<Album>() {
            
            @Override
            public Album createFromParcel(Parcel source) {
                return new Album(source);
            }
            
            public Album[] newArray(int size) {
                return new Album[size];
            };
            
        };
        //@formatter:on
    }
}
