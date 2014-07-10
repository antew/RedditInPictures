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

import com.antew.redditinpictures.library.model.ImageSize;
import com.antew.redditinpictures.library.util.Ln;

/**
 * Class used to parse the Imgur image JSON into POJOs
 *
 * @author Antew
 * @see <a href="http://api.imgur.com/2/image/u9PWV.json">Example</a>
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
        Image  data;
        String success;
        int status;

        public ImgurImage() {
        }

        private ImgurImage(Parcel in) {
            this.data = in.readParcelable(Image.class.getClassLoader());
            this.success = in.readString();
            this.status = in.readInt();
        }

        /**
         * This method simply adds a null check on the {@link Image#getSize(com.antew.redditinpictures.library.model.ImageSize)},
         * method, use it instead.
         *
         * @param size The image size to get
         * @return The URL to the image of the specified size.
         */
        @Deprecated
        public String getSize(ImageSize size) {
            if (data == null) {
                return null;
            }

            return data.getSize(size);
        }

        public Image getImage() {
            return data;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeParcelable(this.data, 0);
            dest.writeString(this.success);
            dest.writeInt(this.status);
        }

        public static final Parcelable.Creator<ImgurImage> CREATOR = new Parcelable.Creator<ImgurImage>() {
            public ImgurImage createFromParcel(Parcel source) {
                return new ImgurImage(source);
            }

            public ImgurImage[] newArray(int size) {
                return new ImgurImage[size];
            }
        };
    }

    public static class Image implements Parcelable {
        String  id;
        String  title;
        String  description;
        String  datetime;
        String  type;
        boolean animated;
        int     width;
        int     height;
        int     size;
        long    views;
        long    bandwidth;
        String  deleteHash;
        String  section;
        String  link;

        public Image() {
        }

        private Image(Parcel in) {
            this.id = in.readString();
            this.title = in.readString();
            this.description = in.readString();
            this.datetime = in.readString();
            this.type = in.readString();
            this.animated = in.readByte() != 0;
            this.width = in.readInt();
            this.height = in.readInt();
            this.size = in.readInt();
            this.views = in.readLong();
            this.bandwidth = in.readLong();
            this.deleteHash = in.readString();
            this.section = in.readString();
            this.link = in.readString();
        }

        public String getSize(ImageSize size) {
            String decoded = null;
            if (link == null) {
                return null;
            }

            String linkWithoutExtension = link.substring(0, link.lastIndexOf('.') - 1);
            String extension = link.substring(link.lastIndexOf('.'));

            switch (size) {
                case SMALL_SQUARE:
                    decoded = linkWithoutExtension + ImgurImageSize.SMALL_SQURE + extension;
                case LARGE_THUMBNAIL:
                    decoded = linkWithoutExtension + ImgurImageSize.LARGE_THUMBNAIL + extension;
                case ORIGINAL:
                    decoded = link;
            }

            return decoded;
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

        public String getDatetime() {
            return datetime;
        }

        public void setDatetime(String datetime) {
            this.datetime = datetime;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public boolean isAnimated() {
            return animated;
        }

        public void setAnimated(boolean animated) {
            this.animated = animated;
        }

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public long getViews() {
            return views;
        }

        public void setViews(long views) {
            this.views = views;
        }

        public long getBandwidth() {
            return bandwidth;
        }

        public void setBandwidth(long bandwidth) {
            this.bandwidth = bandwidth;
        }

        public String getDeleteHash() {
            return deleteHash;
        }

        public void setDeleteHash(String deleteHash) {
            this.deleteHash = deleteHash;
        }

        public String getSection() {
            return section;
        }

        public void setSection(String section) {
            this.section = section;
        }

        public String getLink() {
            return link;
        }

        public void setLink(String link) {
            this.link = link;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Image image = (Image) o;

            if (animated != image.animated) return false;
            if (bandwidth != image.bandwidth) return false;
            if (height != image.height) return false;
            if (size != image.size) return false;
            if (views != image.views) return false;
            if (width != image.width) return false;
            if (datetime != null ? !datetime.equals(image.datetime) : image.datetime != null)
                return false;
            if (deleteHash != null ? !deleteHash.equals(image.deleteHash) : image.deleteHash != null)
                return false;
            if (description != null ? !description.equals(image.description) : image.description != null)
                return false;
            if (id != null ? !id.equals(image.id) : image.id != null) return false;
            if (link != null ? !link.equals(image.link) : image.link != null) return false;
            if (section != null ? !section.equals(image.section) : image.section != null)
                return false;
            if (title != null ? !title.equals(image.title) : image.title != null) return false;
            if (type != null ? !type.equals(image.type) : image.type != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = id != null ? id.hashCode() : 0;
            result = 31 * result + (title != null ? title.hashCode() : 0);
            result = 31 * result + (description != null ? description.hashCode() : 0);
            result = 31 * result + (datetime != null ? datetime.hashCode() : 0);
            result = 31 * result + (type != null ? type.hashCode() : 0);
            result = 31 * result + (animated ? 1 : 0);
            result = 31 * result + width;
            result = 31 * result + height;
            result = 31 * result + size;
            result = 31 * result + (int) (views ^ (views >>> 32));
            result = 31 * result + (int) (bandwidth ^ (bandwidth >>> 32));
            result = 31 * result + (deleteHash != null ? deleteHash.hashCode() : 0);
            result = 31 * result + (section != null ? section.hashCode() : 0);
            result = 31 * result + (link != null ? link.hashCode() : 0);
            return result;
        }

        @Override
        public String toString() {
            return "Image{" +
                    "id='" + id + '\'' +
                    ", title='" + title + '\'' +
                    ", description='" + description + '\'' +
                    ", datetime='" + datetime + '\'' +
                    ", type='" + type + '\'' +
                    ", animated=" + animated +
                    ", width=" + width +
                    ", height=" + height +
                    ", size=" + size +
                    ", views=" + views +
                    ", bandwidth=" + bandwidth +
                    ", deleteHash='" + deleteHash + '\'' +
                    ", section='" + section + '\'' +
                    ", link='" + link + '\'' +
                    '}';
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.id);
            dest.writeString(this.title);
            dest.writeString(this.description);
            dest.writeString(this.datetime);
            dest.writeString(this.type);
            dest.writeByte(animated ? (byte) 1 : (byte) 0);
            dest.writeInt(this.width);
            dest.writeInt(this.height);
            dest.writeInt(this.size);
            dest.writeLong(this.views);
            dest.writeLong(this.bandwidth);
            dest.writeString(this.deleteHash);
            dest.writeString(this.section);
            dest.writeString(this.link);
        }

        public static final Parcelable.Creator<Image> CREATOR = new Parcelable.Creator<Image>() {
            public Image createFromParcel(Parcel source) {
                return new Image(source);
            }

            public Image[] newArray(int size) {
                return new Image[size];
            }
        };
    }
}
