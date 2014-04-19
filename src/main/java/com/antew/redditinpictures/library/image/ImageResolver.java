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

public class ImageResolver {

    public static String resolve(String url, ImageSize size) {
        Image image = resolve(url);
        String decodedUrl = null;

        if (image != null) {
            decodedUrl = image.getSize(size);
        }

        return decodedUrl;
    }

    public static Image resolve(String url) {
        return ImageFactory.getImage(url);
    }
}
