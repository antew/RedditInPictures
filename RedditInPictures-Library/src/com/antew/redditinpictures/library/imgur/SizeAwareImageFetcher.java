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

import com.antew.redditinpictures.library.enums.ImageSize;
import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.image.ImageResolver;
import com.antew.redditinpictures.library.ui.ImageViewerFragment;
import com.antew.redditinpictures.library.utils.ImageFetcher;

/**
 * Used to load original versions of images in {@link ImageViewerFragment} and subclasses
 * 
 * @author Antew
 * 
 */
public class SizeAwareImageFetcher extends ImageFetcher {
    public static final String  TAG       = "ImgurOriginalFetcher";
    private static ImageSize    mImageSize;

    /**
     * Initialize providing a single target image size (used for both width and height);
     * 
     * @param context
     * @param sizeInPxToScaleImagesTo
     */
    public SizeAwareImageFetcher(Context context, int sizeInPxToScaleImagesTo, ImageSize size) {
        super(context, sizeInPxToScaleImagesTo);
        assert size != null : "ImageSize must not be null!";
        
        mImageSize = size;
    }

    /**
     * @param urlString
     *            The URL to decode
     * @return The decoded URL for a preview image
     */
    public String decodeUrl(String urlString) {
        return ImageResolver.resolve(removeImageSizeFromUrl(urlString), mImageSize);
    }

    public Image getImage(String url) {
        return ImageResolver.resolve(url);
    }

    @Override
    public boolean downloadUrlToStream(String urlString, OutputStream outputStream) {
        return super.downloadUrlToStream(decodeUrl(urlString), outputStream);
    }

    @Override
    public void loadImage(Object data, ImageView imageView, ProgressBar progressBar, TextView errorMessage) {
        super.loadImage(getImageSizeName() + data, imageView, progressBar, errorMessage);
    }

    private String removeImageSizeFromUrl(String url) {
        if (url.startsWith(getImageSizeName()))
            url = url.replace(getImageSizeName(), "");

        return url;
    }
    
    private String getImageSizeName() {
        return mImageSize.name();
    }
}
