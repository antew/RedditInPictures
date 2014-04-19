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

import com.antew.redditinpictures.library.model.ImageSize;
import com.antew.redditinpictures.library.model.ImageType;

public class QuickMemeImageType extends Image {
    private static final String TAG           = QuickMemeImageType.class.getSimpleName();
    private static final String QUICKMEME_URL = "http://i.qkme.me/%s.jpg";
    private static final String URL_REGEX     = "^http://(?:(?:www.)?quickmeme.com/meme|qkme.me|i.qkme.me)/([\\w]+)/?";

    public QuickMemeImageType(java.lang.String url) {
        super(url);
    }

    @Override
    public String getSize(ImageSize size) {
        String hash = getHash();
        String decodedUrl = null;

        if (hash != null) {
            decodedUrl = String.format(QUICKMEME_URL, hash);
        }

        return decodedUrl;
    }

    @Override
    public ImageType getImageType() {
        return ImageType.QUICKMEME_IMAGE;
    }

    @Override
    public String getRegexForUrlMatching() {
        return URL_REGEX;
    }
}
