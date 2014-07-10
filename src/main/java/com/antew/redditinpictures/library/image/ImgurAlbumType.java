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
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
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

import retrofit.RestAdapter;

public class ImgurAlbumType extends Image {
    private static final String URL_IMGUR_ALBUM_API = "http://api.imgur.com/2/album/";
    private static final String URL_REGEX           = "imgur.com/a/([A-Za-z0-9]+)";
    private              Album  mAlbum              = null;

    public ImgurAlbumType(String url) {
        super(url);
        mAlbum = resolve();
    }

    /**
     * Resolve an Imgur album
     *
     * @return An {@link Album} containing the individual {@link ImgurImage}s
     */
    private Album resolve() {
        ImgurAlbumApi albumApi = null;
        Album album = null;
        String hash = getHash();
        if (hash != null) {
            albumApi = resolveImgurAlbumFromHash(hash);
        }

        if (albumApi != null && albumApi.getAlbum() != null) {
            album = albumApi.getAlbum();
        }

        return album;
    }

    /**
     * Resolve an {@link ImgurAlbumApi} from an imgur hash
     *
     * @param hash
     *     The hash to resolve an Album from
     *
     * @return An {@link ImgurAlbumApi} representing the image
     */
    public static ImgurAlbumApi resolveImgurAlbumFromHash(String hash) {
        if (hash == null) {
            Ln.e("resolveImgurAlbumFromHash - hash was null");
        }

        Ln.i("resolveImgurAlbumFromHash, hash = %s", hash);
        ImgurAlbumApi album = null;
        Gson gson = new Gson();
        String json = null;
        String newUrl = URL_IMGUR_ALBUM_API + hash + Constants.JSON;

        if (ImgurApiCache.getInstance().containsImgurAlbum(hash)) {
            Ln.i("cache - resolveImgurAlbumFromHash - %s found in cache", hash);
            album = ImgurApiCache.getInstance().getImgurAlbum(hash);
        } else {
            Ln.i("cache - resolveImgurAlbumFromHash - %s NOT found in cache", hash);
            try {
                final RestAdapter adapter = new RestAdapter.Builder().setEndpoint("https://api.imgur.com/3").setLogLevel(RestAdapter.LogLevel.FULL).setRequestInterceptor(new ImgurAuthenticationInterceptor()).build();
                ImgurServiceRetrofit service = adapter.create(ImgurServiceRetrofit.class);

                album = service.getAlbum(hash);


                if (album == null) {
                    Ln.e("Album was null for hash: " + hash);
                    return null;
                }

                ImgurApiCache.getInstance().addImgurAlbum(hash, album);
            } catch (JsonSyntaxException e) {
                Ln.e(e, "resolveImgurAlbumFromHash");
            }
        }

        return album;
    }

    @Override
    public String getSize(ImageSize size) {
        assert size != null : "ImageSize must not be null";

        if (mAlbum == null) {
            mAlbum = resolve();
        }

        ImgurImageApi.Image imgurImage = null;
        String decodedUrl = null;

        boolean isValidImage = mAlbum != null && mAlbum.getImages() != null && mAlbum.getImages().get(0) != null;
        if (isValidImage) {
            imgurImage = mAlbum.getImages().get(0);
            decodedUrl = imgurImage.getSize(size);
        }

        return decodedUrl;
    }

    @Override
    public ImageType getImageType() {
        return ImageType.IMGUR_ALBUM;
    }

    public Album getAlbum() {
        if (mAlbum != null) {
            return mAlbum;
        }

        return resolve();
    }

    public Album getAlbumFromCache() {
        return mAlbum;
    }


    @Override
    public String getRegexForUrlMatching() {
        return URL_REGEX;
    }
}
