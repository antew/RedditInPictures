package com.antew.redditinpictures.library.utils;

import java.util.HashMap;

import com.antew.redditinpictures.library.utils.ImageUtil.ImageType;

public class ImageTypeCache {
    private ImageTypeCache() {};
    
    private static class ImageCacheHolder {
        public static final HashMap<String, ImageUtil.ImageType> INSTANCE = new HashMap<String, ImageUtil.ImageType>();
    }
    
    public static HashMap<String, ImageType> getInstance() {
        return ImageCacheHolder.INSTANCE;
    }
    
    public static boolean containsKey(String url) {
        return getInstance().containsKey(url);
    }
    
    public static ImageType getType(String url) {
        if (getInstance().containsKey(url))
            return getInstance().get(url);
        
        return null;
    }
    
    public static void addImage(String url, ImageType imageType) {
        getInstance().put(url, imageType);
    }
}
