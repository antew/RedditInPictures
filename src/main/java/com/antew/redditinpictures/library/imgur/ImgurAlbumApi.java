/*
 * Copyright (C) 2014 Antew
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
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is used by Gson to parse the Imgur Album API into POJOs
 *
 * @author Antew
 * @see <a href="http://api.imgur.com/2/album/T2GDa.json">Example</a>
 */
public class ImgurAlbumApi {
    @SerializedName("data")
    private Album album;

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public static class Album implements Parcelable {
        private String id;
        private String title;
        private String description;
        private int datetime;
        private String cover;
        private int coverWidth;
        private int coverHeight;
        private String accountUrl;
        private String privacy;
        private String layout;
        private int views;
        private String link;
        private String deletehash;
        private int imagesCount;
        List<ImgurImageApi.Image> images = new ArrayList<ImgurImageApi.Image>();

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.title);
            dest.writeString(this.description);
            dest.writeInt(this.datetime);
            dest.writeString(this.cover);
            dest.writeInt(this.coverWidth);
            dest.writeInt(this.coverHeight);
            dest.writeString(this.accountUrl);
            dest.writeString(this.privacy);
            dest.writeString(this.layout);
            dest.writeInt(this.views);
            dest.writeString(this.link);
            dest.writeString(this.deletehash);
            dest.writeInt(this.imagesCount);
            dest.writeTypedList(images);
        }

        public Album() {
        }

        private Album(Parcel in) {
            this.id = in.readString();
            this.title = in.readString();
            this.description = in.readString();
            this.datetime = in.readInt();
            this.cover = in.readString();
            this.coverWidth = in.readInt();
            this.coverHeight = in.readInt();
            this.accountUrl = in.readString();
            this.privacy = in.readString();
            this.layout = in.readString();
            this.views = in.readInt();
            this.link = in.readString();
            this.deletehash = in.readString();
            this.imagesCount = in.readInt();
            in.readTypedList(images, ImgurImageApi.Image.CREATOR);
        }

        public static final Parcelable.Creator<Album> CREATOR = new Parcelable.Creator<Album>() {
            public Album createFromParcel(Parcel source) {
                return new Album(source);
            }

            public Album[] newArray(int size) {
                return new Album[size];
            }
        };

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Album album = (Album) o;

            if (coverHeight != album.coverHeight) return false;
            if (coverWidth != album.coverWidth) return false;
            if (datetime != album.datetime) return false;
            if (imagesCount != album.imagesCount) return false;
            if (views != album.views) return false;
            if (accountUrl != null ? !accountUrl.equals(album.accountUrl) : album.accountUrl != null)
                return false;
            if (cover != null ? !cover.equals(album.cover) : album.cover != null) return false;
            if (deletehash != null ? !deletehash.equals(album.deletehash) : album.deletehash != null)
                return false;
            if (description != null ? !description.equals(album.description) : album.description != null)
                return false;
            if (id != null ? !id.equals(album.id) : album.id != null) return false;
            if (images != null ? !images.equals(album.images) : album.images != null) return false;
            if (layout != null ? !layout.equals(album.layout) : album.layout != null) return false;
            if (link != null ? !link.equals(album.link) : album.link != null) return false;
            if (privacy != null ? !privacy.equals(album.privacy) : album.privacy != null)
                return false;
            if (title != null ? !title.equals(album.title) : album.title != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (title != null ? title.hashCode() : 0);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + datetime;
            result = 31 * result + (cover != null ? cover.hashCode() : 0);
            result = 31 * result + coverWidth;
            result = 31 * result + coverHeight;
            result = 31 * result + (accountUrl != null ? accountUrl.hashCode() : 0);
            result = 31 * result + (privacy != null ? privacy.hashCode() : 0);
            result = 31 * result + (layout != null ? layout.hashCode() : 0);
            result = 31 * result + views;
            result = 31 * result + (link != null ? link.hashCode() : 0);
            result = 31 * result + (deletehash != null ? deletehash.hashCode() : 0);
            result = 31 * result + imagesCount;
            result = 31 * result + (images != null ? images.hashCode() : 0);
            return result;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getDatetime() {
            return datetime;
        }

        public void setDatetime(int datetime) {
            this.datetime = datetime;
        }

        public String getCover() {
            return cover;
        }

        public void setCover(String cover) {
            this.cover = cover;
        }

        public int getCoverWidth() {
            return coverWidth;
        }

        public void setCoverWidth(int coverWidth) {
            this.coverWidth = coverWidth;
        }

        public int getCoverHeight() {
            return coverHeight;
        }

        public void setCoverHeight(int coverHeight) {
            this.coverHeight = coverHeight;
        }

        public String getAccountUrl() {
            return accountUrl;
        }

        public void setAccountUrl(String accountUrl) {
            this.accountUrl = accountUrl;
        }

        public String getPrivacy() {
            return privacy;
        }

        public void setPrivacy(String privacy) {
            this.privacy = privacy;
        }

        public String getLayout() {
            return layout;
        }

        public void setLayout(String layout) {
            this.layout = layout;
        }

        public int getViews() {
            return views;
        }

        public void setViews(int views) {
            this.views = views;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        public String getDeletehash() {
            return deletehash;
        }

        public void setDeletehash(String deletehash) {
            this.deletehash = deletehash;
        }

        public int getImagesCount() {
            return imagesCount;
        }

        public void setImagesCount(int imagesCount) {
            this.imagesCount = imagesCount;
        }

        public List<ImgurImageApi.Image> getImages() {
            return images;
        }

        public void setImages(List<ImgurImageApi.Image> images) {
            this.images = images;
        }

        @Override
        public String toString() {
            return "Album{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", datetime=" + datetime +
                    ", cover='" + cover + '\'' +
                    ", coverWidth=" + coverWidth +
                    ", coverHeight=" + coverHeight +
                    ", accountUrl='" + accountUrl + '\'' +
                    ", privacy='" + privacy + '\'' +
                    ", layout='" + layout + '\'' +
                    ", views=" + views +
                    ", link='" + link + '\'' +
                    ", deletehash='" + deletehash + '\'' +
                    ", imagesCount=" + imagesCount +
                    ", images=" + images +
                    '}';
        }
    }
}
