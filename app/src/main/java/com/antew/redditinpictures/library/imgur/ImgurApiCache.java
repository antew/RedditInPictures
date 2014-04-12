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

import com.antew.redditinpictures.library.model.imgur.ImgurGallery;
import java.util.HashMap;

/**
 * Cache used to avoid repeated API calls to Imgur
 *
 * @author Antew
 */
public class ImgurApiCache {
    private static HashMap<String, ImgurImageApi> imgurImages    = null;
    private static HashMap<String, ImgurAlbumApi> imgurAlbums    = null;
    private static HashMap<String, ImgurGallery> imgurGalleries = null;

    private ImgurApiCache() {}

    public static ImgurApiCache getInstance() {
        return ImgurApiCacheHolder.INSTANCE;
    }

    public boolean containsImgurImage(String url) {
        return imgurImages != null && imgurImages.containsKey(url);
    }

    public boolean containsImgurAlbum(String url) {
        return imgurAlbums != null && imgurAlbums.containsKey(url);
    }

    public boolean containsImgurGallery(String url) {
        return imgurGalleries != null && imgurGalleries.containsKey(url);
    }

    public synchronized void addImgurImage(String url, ImgurImageApi image) {
        if (imgurImages == null) {
            imgurImages = new HashMap<String, ImgurImageApi>();
        }

        imgurImages.put(url, image);
    }

    public synchronized void addImgurAlbum(String url, ImgurAlbumApi album) {
        if (imgurAlbums == null) {
            imgurAlbums = new HashMap<String, ImgurAlbumApi>();
        }

        imgurAlbums.put(url, album);
    }

    public synchronized void addImgurGallery(String url, ImgurGallery album) {
        if (imgurGalleries == null) {
            imgurGalleries = new HashMap<String, ImgurGallery>();
        }

        imgurGalleries.put(url, album);
    }

    public ImgurImageApi getImgurImage(String url) {
        ImgurImageApi retVal = null;

        if (imgurImages != null) {
            retVal = imgurImages.get(url);
        }

        return retVal;
    }

    public ImgurAlbumApi getImgurAlbum(String url) {
        ImgurAlbumApi retVal = null;

        if (imgurAlbums != null) {
            retVal = imgurAlbums.get(url);
        }

        return retVal;
    }

    public ImgurGallery getImgurGallery(String url) {
        ImgurGallery retVal = null;

        if (imgurGalleries != null) {
            retVal = imgurGalleries.get(url);
        }

        return retVal;
    }

    private static class ImgurApiCacheHolder {
        public static final ImgurApiCache INSTANCE = new ImgurApiCache();
    }
}
