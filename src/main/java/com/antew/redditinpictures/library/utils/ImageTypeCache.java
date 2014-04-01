package com.antew.redditinpictures.library.utils;

import com.antew.redditinpictures.library.enums.ImageType;
import java.util.HashMap;

public class ImageTypeCache {
    private ImageTypeCache() {}

    ;



    public static HashMap<String, ImageType> getInstance() {
        return ImageCacheHolder.INSTANCE;
    }

    public static ImageType getType(String url) {
        if (getInstance().containsKey(url)) {
            return getInstance().get(url);
        }

        return null;
    }    public static boolean containsKey(String url) {
        return getInstance().containsKey(url);
    }

    public static void addImage(String url, ImageType imageType) {
        getInstance().put(url, imageType);
    }    public static ImageType getType(String url) {
        if (getInstance().containsKey(url)) {
            return getInstance().get(url);
        }

        return null;
    }

    private static class ImageCacheHolder {
        public static final HashMap<String, ImageType> INSTANCE = new HashMap<String, ImageType>();
    }
}
