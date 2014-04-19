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
import com.antew.redditinpictures.library.flickr.Flickr;
import com.antew.redditinpictures.library.flickr.Flickr.FlickrImage;
import com.antew.redditinpictures.library.flickr.Flickr.FlickrSize;
import com.antew.redditinpictures.library.gson.BooleanDeserializer;
import com.antew.redditinpictures.library.model.ImageSize;
import com.antew.redditinpictures.library.model.ImageType;
import com.antew.redditinpictures.library.network.SynchronousNetworkApi;
import com.antew.redditinpictures.library.util.Ln;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class FlickrImageType extends Image {
    private static final String FLICKR_URL = "http://api.flickr.com/services/rest/?method=flickr.photos.getSizes&api_key="
                                             + Constants.Flickr.API_KEY + "&photo_id=%s&format=json&nojsoncallback=1";
    private static final String URL_REGEX  = "^http://(?:\\w+).?flickr.com/(?:.*)/([\\d]{10})/?(?:.*)?$";
    private              Flickr mFlickr    = null;

    public FlickrImageType(String url) {
        super(url);
    }

    @Override
    public String getSize(ImageSize size) {
        assert size != null : "ImageSize must not be null";

        if (mFlickr == null) {
            mFlickr = resolve();
        }

        if (mFlickr == null) {
            Ln.i("Received null Flickr object");
            return null;
        }

        FlickrSize flickrSize = FlickrSize.ORIGINAL;
        if (ImageSize.SMALL_SQUARE.equals(size)) {
            flickrSize = FlickrSize.THUMBNAIL;
        }

        FlickrImage fi = mFlickr.getSize(flickrSize);
        if (fi != null) {
            return fi.getSource();
        }

        return null;
    }

    /**
     * Resolve the flickr image for the input URL
     *
     * @return An {@link Flickr} instance representing the image
     */
    private Flickr resolve() {
        mFlickr = resolveFlickrImageFromHash(getHash());

        return mFlickr;
    }

    /**
     * Resolve an image from Flickr
     *
     * @param hash
     *     The hash to resolve
     *
     * @return A {@link Flickr} instance
     */
    public static Flickr resolveFlickrImageFromHash(String hash) {
        Flickr flickr = null;

        if (hash != null) {
            Gson gson = new GsonBuilder().registerTypeAdapter(Boolean.class, new BooleanDeserializer()).create();
            try {
                String json = SynchronousNetworkApi.downloadUrl(String.format(FLICKR_URL, hash));
                flickr = gson.fromJson(json, Flickr.class);
            } catch (JsonSyntaxException e) {
                Ln.e(e, "Error parsing JSON in resolveFlickrImage");
            }
        }

        return flickr;
    }

    @Override
    public ImageType getImageType() {
        return ImageType.FLICKR_IMAGE;
    }

    @Override
    public String getRegexForUrlMatching() {
        return URL_REGEX;
    }
}
