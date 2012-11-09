package com.antew.redditinpictures.library.imgur;

import java.io.OutputStream;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.antew.redditinpictures.library.imgur.ImgurResolver.ImageSize;
import com.antew.redditinpictures.library.utils.ImageContainer;
import com.antew.redditinpictures.library.utils.ImageFetcher;

public class ImgurOriginalFetcher extends ImageFetcher {
    public static final String  TAG       = "ImgurOriginalFetcher";
    private static final String ORIGINAL  = "ORIGINAL_";
    private static final String THUMBNAIL = "THUMBNAIL_";

    /**
     * Initialize providing a target image width and height for the processing images.
     * 
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImgurOriginalFetcher(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     * 
     * @param context
     * @param imageSize
     */
    public ImgurOriginalFetcher(Context context, int imageSize) {
        super(context, imageSize);
    }

    /**
     * @param urlString
     *            The URL to decode
     * @return The decoded URL for a preview image
     */
    public String decodeUrl(String urlString) {
        ImageContainer container = decode(urlString); 
        return ImgurResolver.getSize(container, ImageSize.ORIGINAL);
    }
    
    /**
     * @param urlString
     *            The URL to decode
     * @return The decoded URL for a preview image
     */
    public ImageContainer decode(String urlString) {
        Log.i(TAG, "decode - URL = " + urlString);
        ImageContainer container = ImgurResolver.resolve(removeImageSizeFromUrl(urlString));
        return container;
    }

    @Override
    public boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        return super.downloadUrlToStream(decodeUrl(urlString), outputStream);
    }

    @Override
    public void loadImage(Object data, ImageView imageView, ProgressBar progressBar) {
        super.loadImage(ORIGINAL + data, imageView, progressBar);
    }

    private String removeImageSizeFromUrl(String url) {

        if (url.startsWith(THUMBNAIL))
            url = url.replace(THUMBNAIL, "");
        else if (url.startsWith(ORIGINAL))
            url = url.replace(ORIGINAL, "");

        return url;
    }
}
