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

import com.antew.redditinpictures.library.model.ImageType;
import com.antew.redditinpictures.library.util.ImageUtil;

public class ImageFactory {

    public static Image getImage(String url) {
        Image image = null;

        ImageType imageType = ImageUtil.getImageType(url);
        switch (imageType) {
            case IMGUR_IMAGE:
                image = new ImgurImageType(url);
                break;
            case IMGUR_ALBUM:
                image = new ImgurAlbumType(url);
                break;
            case IMGUR_GALLERY:
                image = new ImgurGalleryType(url);
                break;
            case OTHER_SUPPORTED_IMAGE:
                image = new BasicImageType(url);
                break;
            case FLICKR_IMAGE:
                image = new FlickrImageType(url);
                break;
            case DEVIANTART_IMAGE:
                break;
            case EHOST_IMAGE:
                break;
            case LIVEMEME_IMAGE:
                break;
            case MEMECRUNCH_IMAGE:
                break;
            case MEMEFIVE_IMAGE:
                break;
            case MINUS_IMAGE:
                break;
            case PICASARUS_IMAGE:
                break;
            case PICSHD_IMAGE:
                break;
            case QUICKMEME_IMAGE:
                image = new QuickMemeImageType(url);
                break;
            case SNAGGY_IMAGE:
                break;
            case STEAM_IMAGE:
                break;
            case TUMBLR_IMAGE:
                break;
            case UNSUPPORTED_IMAGE:
                break;
            default:
                break;
        }

        return image;
    }
}
