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

import com.antew.redditinpictures.library.utils.Util;

import java.util.List;

/**
 * Class used by Gson to parse Imgur gallery JSON
 * @see <a href="http://imgur.com/gallery/T2GDa.json">example</a>
 * @author Antew
 *
 */
public class ImgurGallery implements Parcelable {
    private Data data;
    private boolean success;
    private int status;
    
    //@formatter:off
    public Data getData()      { return data; }
    public boolean isSuccess() { return success; }
    public int getStatus()     { return status; }
    //@formatter:on

    
    public ImgurGallery(Parcel source) {
        data = source.readParcelable(Data.class.getClassLoader());
        success = source.readByte() == 1;
        status = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(data, 0);
        dest.writeByte(Util.parcelBoolean(success));
        dest.writeInt(status);
    }
    
    //@formatter:off
    public static final Parcelable.Creator<ImgurGallery> CREATOR
        = new Parcelable.Creator<ImgurGallery>() {
        
        @Override
        public ImgurGallery createFromParcel(Parcel source) {
            return new ImgurGallery(source);
        }
        
        public ImgurGallery[] newArray(int size) {
            return new ImgurGallery[size];
        };
        
    };
    
    
    public static class Data implements Parcelable {
        private GalleryImage image;
        private List<Caption> captions;
        
        //@formatter:off
        public GalleryImage getImage()          { return image; }
        public List<Caption> getCaptions()      { return captions; }
        public Caption getCaption(int position) { return captions.get(position); }
        //@formatter:on

        
        public Data(Parcel source) {
            image = source.readParcelable(GalleryImage.class.getClassLoader());
            source.readList(captions, Caption.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(image, 0);
            dest.writeList(captions);
        }
        
        //@formatter:off
        public static final Parcelable.Creator<Data> CREATOR
            = new Parcelable.Creator<Data>() {
            
            @Override
            public Data createFromParcel(Parcel source) {
                return new Data(source);
            }
            
            public Data[] newArray(int size) {
                return new Data[size];
            };
            
        };
    }
    
    public static class GalleryImage implements Parcelable {
        private String hash;
        private String account_url;
        private String title;
        private int score;
        private int starting_score;
        private float virality;
        private int size;
        private long views;
        private boolean is_hot;
        private boolean is_album;
        private String album_cover;
        private String mimetype;
        private String ext;
        private int width;
        private int height;
        private int ups;
        private int downs;
        private int points;
        private String reddit;
        private String bandwidth;
        private String timestamp;
        private String hot_datetime;
        private List<AlbumImages> images;
        
        
        //@formatter:off
        public String getHash()              { return hash; }
        public String getAccountUrl()        { return account_url; }
        public String getTitle()             { return title; }
        public int getScore()                { return score; }
        public int getStartingScore()        { return starting_score; }
        public float getVirality()           { return virality; }
        public int getSize()                 { return size; }
        public long getViews()               { return views; }
        public boolean isHot()               { return is_hot; }
        public boolean isAlbum()             { return is_album; }
        public String getAlbumCover()        { return album_cover; }
        public String getMimetype()          { return mimetype; }
        public String getExt()               { return ext; }
        public int getWidth()                { return width; }
        public int getHeight()               { return height; }
        public int getUps()                  { return ups; }
        public int getDowns()                { return downs; }
        public int getPoints()               { return points; }
        public String getReddit()            { return reddit; }
        public String getBandwidth()         { return bandwidth; }
        public String getTimestamp()         { return timestamp; }
        public String getHotDatetime()       { return hot_datetime; }
        public List<AlbumImages> getImages() { return images; }
        //@formatter:off

        public GalleryImage(Parcel source) {
            hash = source.readString();
            account_url = source.readString();
            title = source.readString();
            score = source.readInt();
            starting_score = source.readInt();
            virality = source.readFloat();
            size = source.readInt();
            views = source.readLong();
            is_hot = source.readByte() == 1;
            is_album = source.readByte() == 1;
            album_cover = source.readString();
            mimetype = source.readString();
            ext = source.readString();
            width = source.readInt();
            height = source.readInt();
            ups = source.readInt();
            downs = source.readInt();
            points = source.readInt();
            reddit = source.readString();
            bandwidth = source.readString();
            timestamp = source.readString();
            hot_datetime = source.readString();
            source.readList(images, AlbumImages.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(hash);
            dest.writeString(account_url);
            dest.writeString(title);
            dest.writeInt(score);
            dest.writeInt(starting_score);
            dest.writeFloat(virality);
            dest.writeInt(size);
            dest.writeLong(views);
            dest.writeByte(Util.parcelBoolean(is_hot));
            dest.writeByte(Util.parcelBoolean(is_album));
            dest.writeString(album_cover);
            dest.writeString(mimetype);
            dest.writeString(ext);
            dest.writeInt(width);
            dest.writeInt(height);
            dest.writeInt(ups);
            dest.writeInt(downs);
            dest.writeInt(points);
            dest.writeString(reddit);
            dest.writeString(bandwidth);
            dest.writeString(timestamp);
            dest.writeString(hot_datetime);
            dest.writeList(images);
        }
        
        //@formatter:off
        public static final Parcelable.Creator<GalleryImage> CREATOR
            = new Parcelable.Creator<GalleryImage>() {
            
            @Override
            public GalleryImage createFromParcel(Parcel source) {
                return new GalleryImage(source);
            }
            
            public GalleryImage[] newArray(int size) {
                return new GalleryImage[size];
            };
            
        };
        //@formatter:on
    }
    
    public static class Caption implements Parcelable {
        private long id;
        private String hash;
        private String caption;
        private String author;
        private long author_id;
        private int ups;
        private int downs;
        private float best_score;
        private int points;
        private String datetime;
        private long parent_id;
        private boolean deleted;
        private boolean on_album;
        private String album_cover;

        //@formatter:off
        public long getId()           { return id; }
        public String getHash()       { return hash; }
        public String getCaption()    { return caption; }
        public String getAuthor()     { return author; }
        public long getAuthorId()     { return author_id; }
        public int getUps()           { return ups; }
        public int getDowns()         { return downs; }
        public float getBest_score()  { return best_score; }
        public int getPoints()        { return points; }
        public String getDatetime()   { return datetime; }
        public long getParentId()     { return parent_id; }
        public boolean isDeleted()    { return deleted; }
        public boolean isOnAlbum()    { return on_album; }
        public String getAlbumCover() { return album_cover; }
        //@formatter:off
        
        public Caption(Parcel source) {
            id = source.readLong();
            hash = source.readString();
            caption = source.readString();
            author = source.readString();
            author_id = source.readLong();
            ups = source.readInt();
            downs = source.readInt();
            best_score = source.readFloat();
            points = source.readInt();
            datetime = source.readString();
            parent_id = source.readLong();
            deleted = source.readByte() == 1;
            on_album = source.readByte() == 1;
            album_cover = source.readString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(id);
            dest.writeString(hash);
            dest.writeString(caption);
            dest.writeString(author);
            dest.writeLong(author_id);
            dest.writeInt(ups);
            dest.writeInt(downs);
            dest.writeFloat(best_score);
            dest.writeInt(points);
            dest.writeString(datetime);
            dest.writeLong(parent_id);
            dest.writeByte(Util.parcelBoolean(deleted));
            dest.writeByte(Util.parcelBoolean(on_album));
            dest.writeString(album_cover);
        }
        
        //@formatter:off
        public static final Parcelable.Creator<Caption> CREATOR
            = new Parcelable.Creator<Caption>() {
            
            @Override
            public Caption createFromParcel(Parcel source) {
                return new Caption(source);
            }
            
            public Caption[] newArray(int size) {
                return new Caption[size];
            };
            
        };
        //@formatter:on
    }
    
    public static class AlbumImages implements Parcelable {
        private int count;
        private List<ImgurGalleryImage> galleryImages;
        
        public int getCount() {
            return count;
        }

        public List<ImgurGalleryImage> getGalleryImages() {
            return galleryImages;
        }

        public AlbumImages(Parcel source) {
            count = source.readInt();
            source.readList(galleryImages, GalleryImage.class.getClassLoader());
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(count);
            dest.writeList(galleryImages);
        }
        
        //@formatter:off
        public static final Parcelable.Creator<AlbumImages> CREATOR
            = new Parcelable.Creator<AlbumImages>() {
            
            @Override
            public AlbumImages createFromParcel(Parcel source) {
                return new AlbumImages(source);
            }
            
            public AlbumImages[] newArray(int size) {
                return new AlbumImages[size];
            };
            
        };
        //@formatter:on
    }
    
    public static class ImgurGalleryImage implements Parcelable {
        private String  hash;
        private String  title;
        private String  description;
        private int     width;
        private int     height;
        private int     size;
        private String  ext;
        private boolean animated;
        private String  datetime;

        public ImgurGalleryImage(Parcel source) {
            hash = source.readString();
            title = source.readString();
            description = source.readString();
            width = source.readInt();
            height = source.readInt();
            size = source.readInt();
            ext = source.readString();
            animated = source.readByte() == 1;
            datetime = source.readString();
        }

        //@formatter:off
        public String getHash()         { return hash; }
        public String getTitle()        { return title; }
        public String getDescription()  { return description; }
        public int getWidth()           { return width; }
        public int getHeight()          { return height; }
        public int getSize()            { return size; }
        public String getExtension()    { return ext; }
        public boolean isAnimated()     { return animated; }
        public String getDatetime()     { return datetime; }
        //@formatter:on

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(hash);
            dest.writeString(title);
            dest.writeString(description);
            dest.writeInt(width);
            dest.writeInt(height);
            dest.writeInt(size);
            dest.writeString(ext);
            dest.writeByte(Util.parcelBoolean(animated));
            dest.writeString(datetime);
        }
        
        //@formatter:off
        public static final Parcelable.Creator<ImgurGalleryImage> CREATOR
            = new Parcelable.Creator<ImgurGalleryImage>() {
            
            @Override
            public ImgurGalleryImage createFromParcel(Parcel source) {
                return new ImgurGalleryImage(source);
            }
            
            public ImgurGalleryImage[] newArray(int size) {
                return new ImgurGalleryImage[size];
            };
            
        };
        //@formatter:on
    }
}
