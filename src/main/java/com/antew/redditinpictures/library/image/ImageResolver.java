package com.antew.redditinpictures.library.image;

import com.antew.redditinpictures.library.enums.ImageSize;

public class ImageResolver {

    public static String resolve(String url, ImageSize size) {
        Image image = resolve(url);
        String decodedUrl = null;

        if (image != null) {
            decodedUrl = image.getSize(size);
        }

        return decodedUrl;
    }

    public static Image resolve(String url) {
        return ImageFactory.getImage(url);
    }
}
