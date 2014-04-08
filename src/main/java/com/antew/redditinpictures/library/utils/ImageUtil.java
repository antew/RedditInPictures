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
package com.antew.redditinpictures.library.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.EditText;
import com.antew.redditinpictures.library.model.ImageType;
import com.antew.redditinpictures.pro.R;
import java.util.Locale;

public class ImageUtil {
    public static final String TAG = ImageUtil.class.getSimpleName();

    /**
     * Returns true if the input URL is a Snaggy image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an Snaggy image
     */
    private static boolean isSnaggyImage(String url) {
        return url.contains("snag.gy/");
    }

    /**
     * Returns true if the input URL is a PicsHD image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an PicsHD image
     */
    private static boolean isPicsHDImage(String url) {
        return url.contains("picshd.com/");
    }

    /**
     * Returns true if the input URL is a Picsarus image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an Picsarus image
     */
    private static boolean isPicsarusImage(String url) {
        return url.contains("picsarus.com");
    }

    /**
     * Returns true if the input URL is a min.us image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an min.us image
     */
    private static boolean isMinusImage(String url) {
        return url.contains("min.us") && !url.contains("blog.");
    }

    /**
     * Returns true if the input URL is a Steam image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an Steam image
     */
    private static boolean isSteamImage(String url) {
        return url.contains("cloud.steampowered.com");
    }

    /**
     * Returns true if the input URL is a DeviantArt image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an DeviantArt image
     */
    private static boolean isDeviantArtImage(String url) {
        return url.matches("^http://(?:fav.me/.*|(?:.+\\.)?deviantart.com/(?:art/.*|[^#]*#/d.*)$");
    }

    /**
     * Returns true if the input URL is a Tumblr image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an Tumblr image
     */
    private static boolean isTumblrImage(String url) {
        return url.matches("^https?://([a-z0-9-]+\\.tumblr\\.com)/post/(\\d+)(?:/.*)?$");
    }

    /**
     * Returns true if the input URL is a MemeCrunch image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an MemeCrunch image
     */
    private static boolean isMemeCrunchImage(String url) {
        return url.contains("memecrunch.com");
    }

    /**
     * Returns true if the input URL is a LiveMeme image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an LiveMeme image
     */
    private static boolean isLiveMemeImage(String url) {
        return url.contains("livememe.com");
    }

    /**
     * Returns true if the input URL is a MemeFive image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an MemeFive image
     */
    private static boolean isMemeFiveImage(String url) {
        return url.contains("memefive.com");
    }

    /**
     * Returns true if the input URL is supported.
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is supported, otherwise false
     */
    public static boolean isSupportedUrl(String url) {
        return !ImageType.UNSUPPORTED_IMAGE.equals(getImageType(url));
    }

    /**
     * Returns the {@link ImageType} of the input URL.
     *
     * @param url
     *     The URL to evaluate
     *
     * @return The {@link ImageType} of the input URL
     */
    public static ImageType getImageType(String url) {
        url = url.toLowerCase(Locale.US);
        ImageType type = ImageTypeCache.getType(url);

        if (type == null) {
            if (isImgurAlbum(url)) {
                type = ImageType.IMGUR_ALBUM;
            } else if (isUnresolvedImgurImage(url)) {
                type = ImageType.IMGUR_IMAGE;
            } else if (isImgurGallery(url)) {
                type = ImageType.IMGUR_GALLERY;
            } else if (isSupportedImage(url)) {
                type = ImageType.OTHER_SUPPORTED_IMAGE;
            } else if (isFlickrImage(url)) {
                type = ImageType.FLICKR_IMAGE;
            } else if (isEHostImage(url)) {
                type = ImageType.EHOST_IMAGE;
            } else if (isQuickMemeImage(url)) {
                type = ImageType.QUICKMEME_IMAGE;
            }

            /*
            else if (isSnaggyImage(url))     { type = ImageType.SNAGGY_IMAGE; }
            else if (isPicsHDImage(url))     { type = ImageType.PICSHD_IMAGE; }
            else if (isPicsarusImage(url))   { type = ImageType.PICASARUS_IMAGE; }
            else if (isMinusImage(url))      { type = ImageType.MINUS_IMAGE; }
            else if (isSteamImage(url))      { type = ImageType.STEAM_IMAGE; }
            else if (isDeviantArtImage(url)) { type = ImageType.DEVIANTART_IMAGE; }
            else if (isTumblrImage(url))     { type = ImageType.TUMBLR_IMAGE; }
            else if (isMemeCrunchImage(url)) { type = ImageType.MEMECRUNCH_IMAGE; }
            else if (isLiveMemeImage(url))   { type = ImageType.LIVEMEME_IMAGE; }
            else if (isMemeFiveImage(url))   { type = ImageType.MEMEFIVE_IMAGE; }
            */

            else {
                type = ImageType.UNSUPPORTED_IMAGE;
            }

            ImageTypeCache.addImage(url, type);
        }

        return type;
    }

    /**
     * Returns true if the input URL is an Imgur album
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an Imgur album, otherwise false
     */
    private static boolean isImgurAlbum(String url) {
        return url.contains("imgur.com/a/");
    }

    /**
     * Returns true if the input URL is an unresolved Imgur image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an unresolved Imgur image, otherwise false
     */
    private static boolean isUnresolvedImgurImage(String url) {
        return url.contains("imgur.com/") && !isImgurAlbum(url) && !isImgurGallery(url);
    }

    /**
     * Returns true if the input URL is an Imgur gallery
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an Imgur gallery, otherwise false
     */
    private static boolean isImgurGallery(String url) {
        return url.contains("imgur.com/gallery");
    }

    /**
     * Returns true if the input URL links directly to an image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL links directly to an image, otherwise false
     */
    private static boolean isSupportedImage(String url) {
        return (url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".gif") || url.endsWith(".bmp") || url.endsWith(".jpeg") || url
            .endsWith(".webp"));
    }

    /**
     * Returns true if the input URL is a min.us image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an min.us image
     */
    private static boolean isFlickrImage(String url) {
        return url.matches("^http://(?:\\w+).?flickr.com/(?:.*)/([\\d]{10})/?(?:.*)?$");
    }

    /**
     * Returns true if the input URL is an eho.st image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an eho.st image
     */
    private static boolean isEHostImage(String url) {
        return url.contains("eho.st");
    }

    /**
     * Returns true if the input URL is a QuickMeme image
     *
     * @param url
     *     The URL to evaluate
     *
     * @return True if the input URL is an QuickMeme image
     */
    private static boolean isQuickMemeImage(String url) {
        return url.contains("qkme.me") || url.contains("quickmeme.com");
    }

    /**
     * Returns true if the image is a gif
     *
     * @param url
     *     The URL of the image to evaluate
     *
     * @return True if the image is a gif, otherwise false
     */
    public static boolean isGif(String url) {
        if (Strings.notEmpty(url)) {
            url = url.toLowerCase(Locale.US);
            return url.endsWith(".gif");
        }
        return false;
    }

    /**
     * Returns an error icon for use with {@link EditText#setError(CharSequence)}
     *
     * @param context
     *     Context to use for retrieving the resource
     *
     * @return A {@link Drawable} of the error icon
     */
    public static Drawable getErrorDrawable(Context context) {
        Drawable dr = context.getResources().getDrawable(R.drawable.custom_indicator_input_error);
        dr.setBounds(0, 0, dr.getIntrinsicWidth(), dr.getIntrinsicHeight());

        return dr;
    }
}
