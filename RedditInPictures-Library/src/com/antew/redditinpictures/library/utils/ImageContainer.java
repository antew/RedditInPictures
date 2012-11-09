package com.antew.redditinpictures.library.utils;

import android.os.Parcel;
import android.os.Parcelable;

import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.utils.ImageUtil.ImageType;

public class ImageContainer implements Parcelable {
    private ImageType  imageType;
    private ImgurImage imgurImage;
    private Album      imgurAlbum;
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
