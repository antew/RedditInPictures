/*
 * Copyright (C) 2014 Antew
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
package com.antew.redditinpictures.library.image;

import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.Injector;
import com.antew.redditinpictures.library.imgur.ImgurApiCache;
import com.antew.redditinpictures.library.imgur.ImgurImageApi;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.model.ImageSize;
import com.antew.redditinpictures.library.model.ImageType;
import com.antew.redditinpictures.library.network.SynchronousNetworkApi;
import com.antew.redditinpictures.library.service.ImgurAuthenticationInterceptor;
import com.antew.redditinpictures.library.service.ImgurServiceRetrofit;
import com.antew.redditinpictures.library.util.Ln;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import javax.inject.Inject;

import retrofit.RestAdapter;

public class ImgurImageType extends Image {
    private static final String     URL_REGEX           = "imgur.com/(?:gallery/)?([A-Za-z0-9]+)";
    private              ImgurImage mImgurImage         = null;

    @Inject
    ImgurServiceRetrofit imgurService;

    public ImgurImageType(String url) {
        super(url);
        Injector.inject(this);
        mImgurImage = resolve();
    }

    /**
     * Resolve the imgur image for the input URL
     *
     * @return An {@link ImgurImage}
     */
    private ImgurImage resolve() {
        ImgurImage imgurImage = resolveImgurImageFromHash(getHash());

        return imgurImage;
    }

    /**
     * Resolve an {@link ImgurImage} from an Imgur hash
     *
     * @param hash
     *     The hash to resolve an image from (e.g. u9PWV)
     *
     * @return An {@link ImgurImage} representing the image
     */
    public ImgurImage resolveImgurImageFromHash(String hash) {
        if (hash == null) {
            Ln.e("Received null hash in resolveImgurImageFromHash");
            return null;
        }

        ImgurImageApi imgurImageApi = null;
        ImgurImage imgurImage = null;

        // For one reason or another Imgur sends API responses with no-cache headers
        // so we have our own cache here
        if (ImgurApiCache.getInstance().containsImgurImage(hash)) {
            Ln.i("cache - resolveImgurImageFromHash - %s found in cache", hash);
            imgurImageApi = ImgurApiCache.getInstance().getImgurImage(hash);
        } else {
            Ln.i("cache - resolveImgurImageFromHash - %s NOT found in cache", hash);
            try {
                ImgurImage image = imgurService.getImage(hash);

                if (image == null) {
                    Ln.e("Image was null for hash: " + hash);
                }

                imgurImageApi = new ImgurImageApi();
                imgurImageApi.setImage(image);

                ImgurApiCache.getInstance().addImgurImage(hash, imgurImageApi);
            } catch (JsonSyntaxException e) {
                Ln.e(e, "resolveImgurImageFromHash");
            }
        }

        if (imgurImageApi != null) {
            imgurImage = imgurImageApi.getImage();
        }

        return imgurImage;
    }

    @Override
    public String getSize(ImageSize size) {
        assert size != null : "ImageSize must not be null";

        if (mImgurImage == null) {
            mImgurImage = resolve();
        }

        String decodedUrl = null;

        if (mImgurImage != null) {
            decodedUrl = mImgurImage.getSize(size);
        }

        return decodedUrl;
    }

    @Override
    public ImageType getImageType() {
        return ImageType.IMGUR_IMAGE;
    }

    @Override
    public String getRegexForUrlMatching() {
        return URL_REGEX;
    }
}
