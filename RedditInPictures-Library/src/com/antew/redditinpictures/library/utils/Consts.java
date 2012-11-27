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

public class Consts {
    public static final int    POSITION_FRONTPAGE             = 0;

    // Constants for extras
    public static final String EXTRA_ENTRIES                  = "Entries";
    public static final String EXTRA_FILENAME                 = "filenameExtra";
    public static final String EXTRA_IMAGE                    = "image";
    public static final String EXTRA_IMAGE_HASH               = "imageHash";
    public static final String EXTRA_IS_SYSTEM_UI_VISIBLE     = "isSystemUiVisible";
    public static final String EXTRA_NAV_POSITION             = "navPosition";
    public static final String EXTRA_NEWLY_SELECTED_SUBREDDIT = "newSelectedSubreddit";
    public static final String EXTRA_PERMALINK                = "permalink";
    public static final String EXTRA_REDDIT_API               = "redditApi";
    public static final String EXTRA_REDDIT_URL               = "redditUrl";
    public static final String EXTRA_SCORE                    = "score";
    public static final String EXTRA_SELECTED_SUBREDDIT       = "selectedSubreddit";
    public static final String EXTRA_SHOW_NSFW_IMAGES         = "showNsfwImages";
    public static final String EXTRA_SHOW_NSFW_IMAGES_CHANGED = "showNsfwImagesChanged";

    // Constants for dialog tags
    public static final String DIALOG_GET_FILENAME            = "getFilename";
    public static final String DIALOG_LOGIN                   = "login";
    public static final String DIALOG_LOGOUT                  = "logout";

    // Constants for broadcast messages
    public static final String BROADCAST_DOWNLOAD_IMAGE       = "downloadImage";
    public static final String BROADCAST_REMOVE_NSFW_IMAGES   = "removeNsfwImages";
    public static final String BROADCAST_TOGGLE_FULLSCREEN    = "fullscreenToggle";
    public static final String BROADCAST_UPDATE_SCORE         = "update-score";
}
