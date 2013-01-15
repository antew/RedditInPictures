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
    public static final String EXTRA_AGE                      = "age";
    public static final String EXTRA_CATEGORY                 = "category";
    public static final String EXTRA_ENTRIES                  = "Entries";
    public static final String EXTRA_ERROR_MESSAGE            = "errorMessage";
    public static final String EXTRA_FILENAME                 = "filenameExtra";
    public static final String EXTRA_IMAGE                    = "image";
    public static final String EXTRA_IMAGE_HASH               = "imageHash";
    public static final String EXTRA_IS_SYSTEM_UI_VISIBLE     = "isSystemUiVisible";
    public static final String EXTRA_MY_SUBREDDITS            = "mySubreddits";
    public static final String EXTRA_NAV_POSITION             = "navPosition";
    public static final String EXTRA_NEWLY_SELECTED_SUBREDDIT = "newSelectedSubreddit";
    public static final String EXTRA_PERMALINK                = "permalink";
    public static final String EXTRA_REDDIT_API               = "redditApi";
    public static final String EXTRA_REDDIT_URL               = "redditUrl";
    public static final String EXTRA_SCORE                    = "score";
    public static final String EXTRA_SUCCESS                  = "success";
    public static final String EXTRA_SELECTED_SUBREDDIT       = "selectedSubreddit";
    public static final String EXTRA_SHOW_NSFW_IMAGES         = "showNsfwImages";
    public static final String EXTRA_SHOW_NSFW_IMAGES_CHANGED = "showNsfwImagesChanged";
    public static final String EXTRA_USERNAME                 = "username";

    // Constants for dialog tags
    public static final String DIALOG_GET_FILENAME            = "getFilename";
    public static final String DIALOG_LOGIN                   = "login";
    public static final String DIALOG_LOGOUT                  = "logout";

    // Constants for broadcast messages
    public static final String BROADCAST_DOWNLOAD_IMAGE       = "download-image";
    public static final String BROADCAST_ABOUT_SUBREDDIT      = "about-subreddit";
    public static final String BROADCAST_HTTP_FINISHED        = "http-finished";
    public static final String BROADCAST_LOGIN_COMPLETE       = "login-complete";
    public static final String BROADCAST_MY_SUBREDDITS        = "my-subreddits";
    public static final String BROADCAST_SUBREDDIT_SELECTED   = "subreddit-selected";
    public static final String BROADCAST_SUBSCRIBE            = "subscribe";
    public static final String BROADCAST_REMOVE_NSFW_IMAGES   = "remove-nsfw-images";
    public static final String BROADCAST_TOGGLE_FULLSCREEN    = "fullscreen-toggle";
    public static final String BROADCAST_UPDATE_SCORE         = "update-score";

    public static final int    LOADER_REDDIT                  = 10;
    public static final int    LOADER_POSTS                   = 20;
    public static final int    LOADER_LOGIN                   = 30;

    public static final String COMPACT_URL                    = "/.compact";
    public static final String REDDIT_BASE_URL                = "http://www.reddit.com";

    public static final int    POSTS_TO_FETCH                 = 50;
}
