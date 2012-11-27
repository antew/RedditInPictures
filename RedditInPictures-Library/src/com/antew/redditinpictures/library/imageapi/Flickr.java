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
package com.antew.redditinpictures.library.imageapi;

import java.util.List;

public class Flickr {
    private String      stat;
    private FlickrSizes sizes;

    public enum FlickrSize { 
        //@formatter:off
        SQUARE("Square"), 
        LARGE_SQUARE("Large Square"),
        THUMBNAIL("Thumbnail"),
        SMALL("Small"),
        SMALL_320("Small 320"),
        MEDIUM("Medium"),
        MEDIUM_640("Medium 640"),
        MEDIUM_800("Medium 800"),
        LARGE("Large"),
        ORIGINAL("Original");
        
        private String key;
        FlickrSize(String key) {
            this.key = key;
        }
        
        public String getKey() {
            return key;
        }
        //@formatter:off
    }
    
    public String getStat() {
        return stat;
    }

    public FlickrSizes getSizes() {
        return sizes;
    }
    
    public FlickrImage getSize(FlickrSize imageSize) {
        String searchKey = imageSize.getKey();
        
        for (FlickrImage f : sizes.getSize()) {
            if (f.getLabel().equalsIgnoreCase(searchKey))
                return f;
        }
        
        return null;
    }

    public static final class FlickrSizes {
        private Boolean           canblog;
        private Boolean           canprint;
        private Boolean           candownload;
        private List<FlickrImage> size;
        
        public boolean canBlog() {
            return canblog;
        }
        public boolean canPrint() {
            return canprint;
        }
        public boolean canDownload() {
            return candownload;
        }
        public List<FlickrImage> getSize() {
            return size;
        }
        
    }

    public static final class FlickrImage {
        private String label;
        private int    width;
        private int    height;
        private String source;
        private String url;
        private String media;
        
        public String getLabel() {
            return label;
        }
        public int getWidth() {
            return width;
        }
        public int getHeight() {
            return height;
        }
        public String getSource() {
            return source;
        }
        public String getUrl() {
            return url;
        }
        public String getMedia() {
            return media;
        }
    }

}
