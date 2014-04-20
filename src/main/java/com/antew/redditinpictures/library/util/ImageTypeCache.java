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
package com.antew.redditinpictures.library.util;

import com.antew.redditinpictures.library.model.ImageType;
import java.util.HashMap;

public class ImageTypeCache {
    private ImageTypeCache() {};

    private static class ImageCacheHolder {
        public static final HashMap<String, ImageType> INSTANCE = new HashMap<String, ImageType>();
    }

    public static HashMap<String, ImageType> getInstance() {
        return ImageCacheHolder.INSTANCE;
    }

    public static boolean containsKey(String url) {
        return getInstance().containsKey(url);
    }

    public static ImageType getType(String url) {
        if (getInstance().containsKey(url)) {
            return getInstance().get(url);
        }

        return null;
    }

    public static void addImage(String url, ImageType imageType) {
        getInstance().put(url, imageType);
    }
}
