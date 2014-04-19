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
package com.antew.redditinpictures.library.model.imgur;

import com.antew.redditinpictures.library.imgur.ImgurAlbumApi.Album;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;

public class SimpleImgurGallery {
    private ImgurImage imgurImage;
    private Album      imgurAlbum;
    private boolean    isImgurImage;

    public SimpleImgurGallery(ImgurImage image) {
        this.imgurImage = image;
        isImgurImage = true;
    }

    public SimpleImgurGallery(Album album) {
        this.imgurAlbum = album;
        isImgurImage = false;
    }

    public boolean isImgurImage() {
        return isImgurImage;
    }

    /**
     * This is just a second method for clarity in the code, if it isn't
     * an {@link ImgurImage} the only other valid value is an {@link Album}
     *
     * @return True if this is an {@link Album}
     */
    public boolean isImgurAlbum() {
        return !isImgurImage;
    }

    public ImgurImage getImgurImage() {
        return imgurImage;
    }

    public Album getImgurAlbum() {
        return imgurAlbum;
    }
}
