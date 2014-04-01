package com.antew.redditinpictures.library.imgur;

import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;

public class SimpleImgurGallery {
    private ImgurImage imgurImage;
    private Album      imgurAlbum;
    private boolean    isImgurImage;

    public SimpleImgurGallery(ImgurImage image) {
        this.imgurImage = image;
        isImgurImage = true;
    }

    public SimpleImgurGallery(Album album) {
        this.imgurAlbum = album;
        isImgurImage = false;
    }

    public boolean isImgurImage() {
        return isImgurImage;
    }

    /**
     * This is just a second method for clarity in the code, if it isn't
     * an {@link ImgurImage} the only other valid value is an {@link Album}
     *
     * @return True if this is an {@link Album}
     */
    public boolean isImgurAlbum() {
        return !isImgurImage;
    }

    public ImgurImage getImgurImage() {
        return imgurImage;
    }

    public Album getImgurAlbum() {
        return imgurAlbum;
    }
}
