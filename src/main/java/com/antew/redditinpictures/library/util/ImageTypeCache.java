package com.antew.redditinpictures.library.util;

import com.antew.redditinpictures.library.model.ImageType;
import java.util.HashMap;

public class ImageTypeCache {
    private ImageTypeCache() {};

    private static class ImageCacheHolder {
        public static final HashMap<String, ImageType> INSTANCE = new HashMap<String, ImageType>();
    }

    public static HashMap<String, ImageType> getInstance() {
        return ImageCacheHolder.INSTANCE;
    }

    public static boolean containsKey(String url) {
        return getInstance().containsKey(url);
    }

    public static ImageType getType(String url) {
        if (getInstance().containsKey(url)) {
            return getInstance().get(url);
        }

        return null;
    }

    public static void addImage(String url, ImageType imageType) {
        getInstance().put(url, imageType);
    }
}