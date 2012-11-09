package com.antew.redditinpictures.library.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.EditText;

import com.antew.redditinpictures.library.R;

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
        return url.contains("imgur.com/") && !isImgurAlbum(url) && !isSupportedImage(url) && !isImgurGallery(url);
    }

    /**
     * Returns true if the input URL is supported.
     * 
     * @param url The URL to evaluate
     * @return True if the input URL is supported, otherwise false
     */
    public static boolean isSupportedUrl(String url) {
        url = url.toLowerCase();
        return isSupportedImage(url) || isUnresolvedImgurImage(url) || isImgurAlbum(url) || isImgurGallery(url);
    }
    
    /**
     * Returns true if the image is a gif
     * @param url The URL of the image to evaluate
     * @return True if the image is a gif, otherwise false
     */
    public static boolean isGif(String url) {
        url = url.toLowerCase();
        return url.endsWith(".gif");
    }

    /**
     * Returns the {@link ImageType} of the input URL. 
     * @param url The URL to evaluate
     * @return The {@link ImageType} of the input URL
     */
    public static ImageType getImageType(String url) {
        url = url.toLowerCase();
        if (isUnresolvedImgurImage(url)) {
            return ImageType.IMGUR_IMAGE;
        } else if (isImgurAlbum(url)) {
            return ImageType.IMGUR_ALBUM;
        } else if (isImgurGallery(url))  {
           return ImageType.IMGUR_GALLERY;
        } else if (isSupportedImage(url)) {
            return ImageType.OTHER_SUPPORTED_IMAGE;
        }

        return ImageType.UNSUPPORTED_IMAGE;
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

}
