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
import com.antew.redditinpictures.library.model.ImageSize;
import com.antew.redditinpictures.library.model.ImageType;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.imgur.ImgurApiCache;
import com.antew.redditinpictures.library.model.imgur.ImgurGallery;
import com.antew.redditinpictures.library.imgur.ImgurImageApi;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.model.imgur.SimpleImgurGallery;
import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.network.SynchronousNetworkApi;
import com.antew.redditinpictures.library.util.Ln;

public class ImgurGalleryType extends Image {
    private static final String URL_IMGUR_GALLERY_API = "http://imgur.com/gallery/";
    private static final String URL_REGEX             = "imgur.com/gallery/([A-Za-z0-9]+)";
    private SimpleImgurGallery mSimpleImgurGallery;

    public ImgurGalleryType(String url) {
        super(url);
        mSimpleImgurGallery = resolve();
    }

    /**
     * Resolve an {@link SimpleImgurGallery} from the input URL
     *
     * @return An {@link SimpleImgurGallery} containing the {@link ImgurAlbumApi.Album} or
     * {@link ImgurImageApi.ImgurImage}
     */
    private SimpleImgurGallery resolve() {
        SimpleImgurGallery gallery = null;

        String hash = getHash();
        if (hash != null) {
            gallery = getImgurGalleryFromHash(hash);
        }

        return gallery;
    }

    /**
     * Returns a {@link SimpleImgurGallery} containing either the {@link ImgurImage} or
     * {@link Album} the gallery resolves to.
     *
     * @param hash
     *     The hash to resolve an {@link SimpleImgurGallery} from
     *
     * @return A {@link SimpleImgurGallery} representing the image
     */
    private static SimpleImgurGallery getImgurGalleryFromHash(String hash) {
        ImgurGallery gallery = null;
        SimpleImgurGallery simpleGallery = null;
        ImgurAlbumApi album = null;
        ImgurImage image = null;
        ImgurApiCache cache = ImgurApiCache.getInstance();
        String newUrl = URL_IMGUR_GALLERY_API + hash + Constants.JSON;

        if (cache.containsImgurGallery(hash)) {
            Ln.d("cache - getImgurGalleryFromHash - %s found in cache", hash);
            gallery = cache.getImgurGallery(hash);

            String imageHash = gallery.getData().getImage().getHash();

            if (gallery.getData().getImage().isAlbum()) {
                album = ImgurAlbumType.resolveImgurAlbumFromHash(imageHash);
                if (album != null && album.getAlbum() != null) {
                    simpleGallery = new SimpleImgurGallery(album.getAlbum());
                }
            } else {
                image = ImgurImageType.resolveImgurImageFromHash(imageHash);
                if (image != null && image.getImage() != null) {
                    simpleGallery = new SimpleImgurGallery(image);
                }
            }

        } else {
            Ln.d("cache - getImgurGalleryFromHash - %s NOT found in cache", hash);

            String json = SynchronousNetworkApi.downloadUrl(newUrl);
            gallery = JsonDeserializer.deserialize(json, ImgurGallery.class);

            if (gallery == null) {
                return null;
            }

            // Resolve the image as a standard ImgurImage or an Album
            if (gallery.isSuccess()) {
                String imageHash = gallery.getData().getImage().getHash();

                if (gallery.getData().getImage().isAlbum()) {
                    album = ImgurAlbumType.resolveImgurAlbumFromHash(imageHash);
                    if (album != null && album.getAlbum() != null) {
                        simpleGallery = new SimpleImgurGallery(album.getAlbum());
                    }
                } else {
                    image = ImgurImageType.resolveImgurImageFromHash(imageHash);
                    if (image != null && image.getImage() != null) {
                        simpleGallery = new SimpleImgurGallery(image);
                    }
                }

                cache.addImgurGallery(hash, gallery);
            } else {
                Ln.e("getImgurGalleryFromHash, error resolving image");
            }
        }

        return simpleGallery;
    }

    public Image getSingleImage() {
        //TODO: Make this less terrible...
        if (mSimpleImgurGallery != null
            && mSimpleImgurGallery.getImgurImage() != null
            && mSimpleImgurGallery.getImgurImage().getLinks() != null) {
            return new ImgurImageType(mSimpleImgurGallery.getImgurImage().getLinks().getOriginal());
        }
        return null;
    }

    public boolean isSingleImage() {
        return mSimpleImgurGallery != null && mSimpleImgurGallery.isImgurImage();
    }

    public Album getAlbum() {
        if (mSimpleImgurGallery != null) {
            return mSimpleImgurGallery.getImgurAlbum();
        }
        return null;
    }

    @Override
    public String getSize(ImageSize size) {
        SimpleImgurGallery gallery = resolve();

        if (gallery == null) {
            Ln.e("Was passed a null SimpleImgurGallery in getSize method");
            return null;
        }

        String returnVal = null;
        if (gallery.isImgurImage()) {
            returnVal = gallery.getImgurImage().getSize(size);
        } else if (gallery.isImgurAlbum()) {
            returnVal = gallery.getImgurAlbum().getImages().get(0).getSize(size);
        }

        return returnVal;
    }

    @Override
    public ImageType getImageType() {
        return ImageType.IMGUR_GALLERY;
    }

    @Override
    public String getRegexForUrlMatching() {
        return URL_REGEX;
    }
}
