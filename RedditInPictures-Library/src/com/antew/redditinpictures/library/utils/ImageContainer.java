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
package com.antew.redditinpictures.library.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.antew.redditinpictures.library.imageapi.Flickr;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.utils.ImageUtil.ImageType;

public class ImageContainer implements Parcelable {
    private ImageType  imageType;
    private ImgurImage imgurImage;
    private Album      imgurAlbum;
    private Flickr     flickr;
    private String     url;
    
    public ImageContainer(Album album) {
        this.imgurAlbum = album;
        this.imageType = ImageType.IMGUR_ALBUM;
    }
    
    public ImageContainer(ImgurImage image) {
        this.imgurImage = image;
        this.imageType = ImageType.IMGUR_IMAGE;
    }
    
    public ImageContainer(String url) { 
        this.url = url;
        this.imageType = ImageType.OTHER_SUPPORTED_IMAGE;
    }
    
    public ImageContainer(Flickr flickr) { 
        this.flickr = flickr;
        this.imageType = ImageType.FLICKR_IMAGE;
    }

    public ImageContainer(Parcel source) {
        imageType = ImageType.valueOf(source.readString());
        imgurImage = source.readParcelable(ImgurImage.class.getClassLoader());
        imgurAlbum = source.readParcelable(Album.class.getClassLoader());
        url = source.readString();
    }

    public ImageType getImageType() {
        return imageType;
    }

    public void setImageType(ImageType imageType) {
        this.imageType = imageType;
    }

    public ImgurImage getImgurImage() {
        return imgurImage;
    }

    public Flickr getFlickr() {
        return flickr;
    }
    
    public void setImgurImage(ImgurImage imgurImage) {
        this.imgurImage = imgurImage;
    }

    public Album getImgurAlbum() {
        return imgurAlbum;
    }

    public void setImgurAlbum(Album imgurAlbum) {
        this.imgurAlbum = imgurAlbum;
    }
    

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    //@formatter:off
    public static Parcelable.Creator<ImageContainer> CREATOR = new Parcelable.Creator<ImageContainer>() {

        @Override
        public ImageContainer createFromParcel(Parcel source) {
            return new ImageContainer(source);
        }

        @Override
        public ImageContainer[] newArray(int size) {
            return new ImageContainer[size];
        }
        
        
        
    };
    //@formatter:on

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(imageType.name());
        dest.writeParcelable(imgurImage, 0);
        dest.writeParcelable(imgurAlbum, 0);
        dest.writeString(url);
    }
}
