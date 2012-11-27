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

/**
 * Class used to parse the Imgur image JSON into POJOs
 * @see <a href="http://api.imgur.com/2/image/u9PWV.json">Example</a>
 * @author Antew
 *
 */
public class ImgurImageApi {
    public ImgurImage getImage() {
        return image;
    }

    public void setImage(ImgurImage image) {
        this.image = image;
    }

    private ImgurImage image;

    public static class ImgurImage implements Parcelable {
        private Link  links;
        private Image image;

        public ImgurImage(Parcel in) {
            links = in.readParcelable(Link.class.getClassLoader());
            image = in.readParcelable(Image.class.getClassLoader());
        }
        
        public Link getLinks() { return links; }
        public Image getImage() { return image; }
        
        @Override
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append("Links: [ ImgurPage: " + r(links.getImgur_page()) + ", LargeThumbnail: " + r(links.getLarge_thumbnail()) + ", Original: " + r(links.getOriginal()) + ", SmallSquare: " + r(links.getSmall_square()) + "]");
            return buf.toString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(links, 0);
            dest.writeParcelable(image, 0);
        }
        
        //@formatter:off
        public static final Parcelable.Creator<ImgurImage> CREATOR
            = new Parcelable.Creator<ImgurImage>() {
            
            @Override
            public ImgurImage createFromParcel(Parcel source) {
                return new ImgurImage(source);
            }
            
            public ImgurImage[] newArray(int size) {
                return new ImgurImage[size];
            };
            
        };
        //@formatter:on

    }
    
    private static String r(String res) {
        return res == null ? "null" : res;
            
    }

    public static class Image implements Parcelable {
        private String  title;
        private String  caption;
        private String  hash;
        private String  datetime;
        private String  type;
        private boolean animated;
        private int     width;
        private int     height;
        private int     size;
        private long    views;
        private long    bandwidth;

        public Image(Parcel source) {
            title = source.readString();
            caption = source.readString();
            hash = source.readString();
            datetime = source.readString();
            type = source.readString();
            animated = source.readByte() == 1;
            width = source.readInt();
            height = source.readInt();
            size = source.readInt();
            views = source.readLong();
            bandwidth = source.readLong();
        }

        //@formatter:off
        public String getTitle()    { return title; }
        public String getCaption()  { return caption; }
        public String getHash()     { return hash; }
        public String getDatetime() { return datetime; }
        public String getType()     { return type; }
        public boolean isAnimated() { return animated; }
        public int getWidth()       { return width; }
        public int getHeight()      { return height; }
        public int getSize()        { return size; }
        public long getViews()      { return views; }
        public long getBandwidth()  { return bandwidth; }
        //@formatter:on

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(title);
            dest.writeString(caption);
            dest.writeString(hash);
            dest.writeString(datetime);
            dest.writeString(type);
            dest.writeByte(Util.parcelBoolean(animated));
            dest.writeInt(width);
            dest.writeInt(height);
            dest.writeInt(size);
            dest.writeLong(views);
            dest.writeLong(bandwidth);
        }
        
        //@formatter:off
        public static final Parcelable.Creator<Image> CREATOR
            = new Parcelable.Creator<Image>() {
            
            @Override
            public Image createFromParcel(Parcel source) {
                return new Image(source);
            }
            
            public Image[] newArray(int size) {
                return new Image[size];
            };
            
        };
        //@formatter:on

    }

    public static class Link implements Parcelable {
        private String original;
        private String imgur_page;
        private String small_square;
        private String large_thumbnail;

        public Link(Parcel source) {
            original = source.readString();
            imgur_page = source.readString();
            small_square = source.readString();
            large_thumbnail = source.readString();
        }

        //@formatter:off
        public String getOriginal()        { return original; }
        public String getImgur_page()      { return imgur_page; }
        public String getSmall_square()    { return small_square; }
        public String getLarge_thumbnail() { return large_thumbnail; }
        //@formatter:on

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(original);
            dest.writeString(imgur_page);
            dest.writeString(small_square);
            dest.writeString(large_thumbnail);
        }
        
        //@formatter:off
        public static final Parcelable.Creator<Link> CREATOR
            = new Parcelable.Creator<Link>() {
            
            @Override
            public Link createFromParcel(Parcel source) {
                return new Link(source);
            }
            
            public Link[] newArray(int size) {
                return new Link[size];
            };
            
        };
        //@formatter:on

    }
}
