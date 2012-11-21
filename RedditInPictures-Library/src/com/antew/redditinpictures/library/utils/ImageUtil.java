package com.antew.redditinpictures.library.utils;

import java.util.HashMap;
import java.util.Locale;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.EditText;

import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.logging.Log;

public class ImageUtil {
    /**
     * Types of images supported by the app
     */
    public enum ImageType {
        IMGUR_ALBUM, IMGUR_IMAGE, IMGUR_GALLERY, OTHER_SUPPORTED_IMAGE, UNSUPPORTED_IMAGE
    }

    /**
     * Returns true if the input URL links directly to an image
     * @param url The URL to evaluate
     * @return True if the input URL links directly to an image, otherwise false
     */
    private static boolean isSupportedImage(String url) {
        return (url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".gif") || url.endsWith(".bmp") || url.endsWith(".jpeg") || url.endsWith(".webp"));
    }

    /**
     * Returns true if the input URL is an Imgur gallery
     * @param url The URL to evaluate
     * @return True if the input URL is an Imgur gallery, otherwise false
     */
    private static boolean isImgurGallery(String url) {
        return url.contains("imgur.com/gallery");
    }
    
    /**
     * Returns true if the input URL is an Imgur album
     * 
     * @param url The URL to evaluate
     * @return True if the input URL is an Imgur album, otherwise false
     */
    private static boolean isImgurAlbum(String url) {
        return url.contains("imgur.com/a/");
    }

    /**
     * Returns true if the input URL is an unresolved Imgur image
     * 
     * @param url The URL to evaluate
     * @return True if the input URL is an unresolved Imgur image, otherwise false
     */
    private static boolean isUnresolvedImgurImage(String url) {
        return url.contains("imgur.com/") && !isImgurAlbum(url) && !isImgurGallery(url);
    }

    /**
     * Returns true if the input URL is supported.
     * 
     * @param url The URL to evaluate
     * @return True if the input URL is supported, otherwise false
     */
    public static boolean isSupportedUrl(String url) {
        url = url.toLowerCase(Locale.US);
        return isSupportedImage(url) || isUnresolvedImgurImage(url) || isImgurAlbum(url) || isImgurGallery(url);
    }
    
    /**
     * Returns true if the image is a gif
     * @param url The URL of the image to evaluate
     * @return True if the image is a gif, otherwise false
     */
    public static boolean isGif(String url) {
        url = url.toLowerCase(Locale.US);
        return url.endsWith(".gif");
    }

    /**
     * Returns the {@link ImageType} of the input URL. 
     * @param url The URL to evaluate
     * @return The {@link ImageType} of the input URL
     */
    public static ImageType getImageType(String url) {
        url = url.toLowerCase(Locale.US);
        ImageType type = ImageCache.getType(url);

        if (type == null) {
            if (isUnresolvedImgurImage(url)) {
                type = ImageType.IMGUR_IMAGE;
            } else if (isImgurAlbum(url)) {
                type = ImageType.IMGUR_ALBUM;
            } else if (isImgurGallery(url))  {
                type = ImageType.IMGUR_GALLERY;
            } else if (isSupportedImage(url)) {
                type = ImageType.OTHER_SUPPORTED_IMAGE;
            } else {
                type = ImageType.UNSUPPORTED_IMAGE;
            }
            
            ImageCache.addImage(url, type);
        }

        return type;
    }
    
    /**
     * Returns an error icon for use with {@link EditText#setError(CharSequence)}
     * @param context Context to use for retrieving the resource
     * @return A {@link Drawable} of the error icon
     */
    public static Drawable getErrorDrawable(Context context) {
        Drawable dr = context.getResources().getDrawable(R.drawable.custom_indicator_input_error);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());
        
        return dr;
    }

    private static class ImageCache {
        private static HashMap<String, ImageType> mImageTypeCache;
        
        private ImageCache() {};
        
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
}
