/*
 * Copyright (C) 2012 Antew | antewcode@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.antew.redditinpictures.library.imgur;

import android.os.Parcel;
import android.os.Parcelable;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is used by Gson to parse the Imgur Album API into POJOs
 *
 * @author Antew
 * @see <a href="http://api.imgur.com/2/album/T2GDa.json">Example</a>
 */
public class ImgurAlbumApi {
    private Album album;

    public Album getAlbum() {
        return album;
    }

    public static class Album implements Parcelable {

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
        List<ImgurImage> images;
        private String title;
        private String description;
        private String cover;
        private String layout;

        public Album(Parcel source) {
            title = source.readString();
            description = source.readString();
            cover = source.readString();
            layout = source.readString();
            images = new ArrayList<ImgurImage>();
            source.readList(images, ImgurImage.class.getClassLoader());
        }

        //@formatter:off
        public String getTitle()            { return title; }

        public String getDescription()      { return description; }

        public String getCover()            { return cover; }

        public String getLayout()           { return layout; }
        //@formatter:on

        public List<ImgurImage> getImages() { return images; }

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
        //@formatter:on
    }
}
