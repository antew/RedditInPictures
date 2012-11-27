/*
 * Copyright (C) 2012 Antew | antewcode@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.antew.redditinpictures.library.imgur;

import java.io.OutputStream;

import android.content.Context;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.antew.redditinpictures.library.imgur.ImgurResolver.ImageSize;
import com.antew.redditinpictures.library.utils.ImageContainer;
import com.antew.redditinpictures.library.utils.ImageFetcher;
import com.antew.redditinpictures.library.utils.ImageUtil;
import com.antew.redditinpictures.library.utils.ImageUtil.ImageType;

public class ImgurThumbnailFetcher extends ImageFetcher {
    private static final String ORIGINAL = "ORIGINAL_";
    private static final String THUMBNAIL = "THUMBNAIL_";
    
    /**
     * Initialize providing a target image width and height for the processing images.
     * 
     * @param context
     * @param imageWidth
     * @param imageHeight
     */
    public ImgurThumbnailFetcher(Context context, int imageWidth, int imageHeight) {
        super(context, imageWidth, imageHeight);
    }

    /**
     * Initialize providing a single target image size (used for both width and height);
     * 
     * @param context
     * @param imageSize
     */
    public ImgurThumbnailFetcher(Context context, int imageSize) {
        super(context, imageSize);
    }

    /**
     * @param urlString
     *            The URL to decode
     * @return The decoded URL for a preview image
     */
    public String decodeUrl(String urlString) {
        ImageContainer container = decode(urlString);
        return ImgurResolver.getSize(container, ImageSize.SMALL_SQUARE);
    }
    
    /**
     * @param urlString
     *            The URL to decode
     * @return An ImageContainer representing the image.
     */
    public ImageContainer decode(String urlString) {
        ImageContainer container = ImgurResolver.resolve(removeImageSizeFromUrl(urlString));
        return container;
    }

    @Override
    public boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        String decodedUrl = decodeUrl(urlString);
        return super.downloadUrlToStream(decodedUrl, outputStream);
    }
    
    @Override
    public void loadImage(Object data, ImageView imageView, ProgressBar progressBar, TextView errorMessage) {
        String url = String.valueOf(data);
        String imageSize = ImageUtil.getImageType(url) == ImageType.OTHER_SUPPORTED_IMAGE ? ORIGINAL : THUMBNAIL;
        super.loadImage(imageSize + data, imageView, progressBar, errorMessage);
    }
    
    private String removeImageSizeFromUrl(String url) {
        
        if (url.startsWith(THUMBNAIL))
            url = url.replace(THUMBNAIL, "");
        else if (url.startsWith(ORIGINAL))
            url = url.replace(ORIGINAL, "");
        
        return url;
    }

}
