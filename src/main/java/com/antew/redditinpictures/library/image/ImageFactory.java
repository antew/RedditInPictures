package com.antew.redditinpictures.library.image;

import com.antew.redditinpictures.library.enums.ImageType;
import com.antew.redditinpictures.library.utils.ImageUtil;

public class ImageFactory {

    public static Image getImage(String url) {
        Image image = null;

        ImageType imageType = ImageUtil.getImageType(url);
        switch (imageType) {
            case IMGUR_IMAGE:
                image = new ImgurImageType(url);
                break;
            case IMGUR_ALBUM:
                image = new ImgurAlbumType(url);
                break;
            case IMGUR_GALLERY:
                image = new ImgurGalleryType(url);
                break;
            case OTHER_SUPPORTED_IMAGE:
                image = new BasicImageType(url);
                break;
            case FLICKR_IMAGE:
                image = new FlickrImageType(url);
                break;
            case DEVIANTART_IMAGE:
                break;
            case EHOST_IMAGE:
                break;
            case LIVEMEME_IMAGE:
                break;
            case MEMECRUNCH_IMAGE:
                break;
            case MEMEFIVE_IMAGE:
                break;
            case MINUS_IMAGE:
                break;
            case PICASARUS_IMAGE:
                break;
            case PICSHD_IMAGE:
                break;
            case QUICKMEME_IMAGE:
                image = new QuickMemeImageType(url);
                break;
            case SNAGGY_IMAGE:
                break;
            case STEAM_IMAGE:
                break;
            case TUMBLR_IMAGE:
                break;
            case UNSUPPORTED_IMAGE:
                break;
            default:
                break;
        }

        return image;
    }
}
