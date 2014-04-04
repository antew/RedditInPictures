package com.antew.redditinpictures.library.image;

import com.antew.redditinpictures.library.enums.ImageSize;
import com.antew.redditinpictures.library.enums.ImageType;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi;
import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.imgur.ImgurApiCache;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.network.SynchronousNetworkApi;
import com.antew.redditinpictures.library.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class ImgurAlbumType extends Image {
    public static final  String TAG                 = ImgurAlbumType.class.getSimpleName();
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
     * @param url
     *     The input url to use in resolving
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
            Log.e(TAG, "resolveImgurAlbumFromHash - hash was null");
        }

        Log.i(TAG, "resolveImgurAlbumFromHash, hash = " + hash);
        ImgurAlbumApi album = null;
        Gson gson = new Gson();
        String json = null;
        String newUrl = URL_IMGUR_ALBUM_API + hash + Constants.JSON;

        if (ImgurApiCache.getInstance().containsImgurAlbum(hash)) {
            Log.i(TAG, "cache - resolveImgurAlbumFromHash - " + hash + " found in cache");
            album = ImgurApiCache.getInstance().getImgurAlbum(hash);
        } else {
            Log.i(TAG, "cache - resolveImgurAlbumFromHash - " + hash + " NOT found in cache");
            try {
                json = SynchronousNetworkApi.downloadUrl(newUrl);

                if (json == null) {
                    return null;
                }

                album = gson.fromJson(json, ImgurAlbumApi.class);
                ImgurApiCache.getInstance().addImgurAlbum(hash, album);
            } catch (JsonSyntaxException e) {
                Log.e(TAG, "resolveImgurAlbumFromHash", e);
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

        ImgurImage imgurImage = null;
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

    @Override
    public String getRegexForUrlMatching() {
        return URL_REGEX;
    }
}
