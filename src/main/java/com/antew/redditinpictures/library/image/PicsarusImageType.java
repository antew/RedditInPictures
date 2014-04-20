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

public class PicsarusImageType extends Image {
    private static final String PICSARUS_URL = "http://www.picsarus.com/%s.jpg";
    private static final String URL_REGEX    = "^https?://(?:[i.]|[edge.]|[www.])*picsarus.com/(?:r/[\\w]+/)?([\\w]{6,})(\\..+)?$";

    public PicsarusImageType(java.lang.String url) {
        super(url);
    }

    @Override
    public String getSize(ImageSize size) {
        return String.format(PICSARUS_URL, getUrl());
    }

    @Override
    public ImageType getImageType() {
        return ImageType.PICASARUS_IMAGE;
    }

    @Override
    public String getRegexForUrlMatching() {
        return URL_REGEX;
    }
}
