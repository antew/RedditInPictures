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
package com.antew.redditinpictures.library;

public class Constants {
    public static final String PACKAGE_PREFIX = "com.antew.redditinpictures";

    public static class Extra {
        private static final String EXTRA_PREFIX                   = PACKAGE_PREFIX + ".extra.";
        public static final  String EXTRA_TITLE                    = EXTRA_PREFIX + "title";
        public static final  String EXTRA_SUBREDDIT_DATA           = EXTRA_PREFIX + "subredditData";
        public static final  String EXTRA_ACTIVE_VIEW              = EXTRA_PREFIX + "activeView";
        public static final  String EXTRA_AGE                      = EXTRA_PREFIX + "age";
        public static final  String EXTRA_CATEGORY                 = EXTRA_PREFIX + "category";
        public static final  String EXTRA_ENTRIES                  = EXTRA_PREFIX + "Entries";
        public static final  String EXTRA_ERROR_MESSAGE            = EXTRA_PREFIX + "errorMessage";
        public static final  String EXTRA_FILENAME                 = EXTRA_PREFIX + "filenameExtra";
        public static final  String EXTRA_IMAGE                    = EXTRA_PREFIX + "image";
        public static final  String EXTRA_IMAGE_HASH               = EXTRA_PREFIX + "imageHash";
        public static final  String EXTRA_IS_SYSTEM_UI_VISIBLE     = EXTRA_PREFIX + "isSystemUiVisible";
        public static final  String EXTRA_MY_SUBREDDITS            = EXTRA_PREFIX + "mySubreddits";
        public static final  String EXTRA_SUBREDDIT_NAMES          = EXTRA_PREFIX + "subredditNames";
        public static final  String EXTRA_NAV_POSITION             = EXTRA_PREFIX + "navPosition";
        public static final  String EXTRA_NEWLY_SELECTED_SUBREDDIT = EXTRA_PREFIX + "newSelectedSubreddit";
        public static final  String EXTRA_PERMALINK                = EXTRA_PREFIX + "permalink";
        public static final  String EXTRA_REDDIT_API               = EXTRA_PREFIX + "redditApi";
        public static final  String EXTRA_REDDIT_URL               = EXTRA_PREFIX + "redditUrl";
        public static final  String EXTRA_SCORE                    = EXTRA_PREFIX + "score";
        public static final  String EXTRA_SUCCESS                  = EXTRA_PREFIX + "success";
        public static final  String EXTRA_SUBREDDIT                = EXTRA_PREFIX + "subreddit";
        public static final  String EXTRA_SHOW_NSFW_IMAGES         = EXTRA_PREFIX + "showNsfwImages";
        public static final  String EXTRA_SHOW_NSFW_IMAGES_CHANGED = EXTRA_PREFIX + "showNsfwImagesChanged";
        public static final  String EXTRA_USERNAME                 = EXTRA_PREFIX + "username";
        public static final  String EXTRA_QUERY                    = EXTRA_PREFIX + "query";
    }

    public static class Dialog {
        private static final String DIALOG_PREFIX             = PACKAGE_PREFIX + ".dialog.";
        public static final  String DIALOG_DEFAULT_SUBREDDITS = DIALOG_PREFIX + "defaultSubreddits";
        public static final  String DIALOG_ADD_SUBREDDIT      = DIALOG_PREFIX + "addSubreddit";
        public static final  String DIALOG_ABOUT_SUBREDDIT    = DIALOG_PREFIX + "aboutSubreddit";
        public static final  String DIALOG_GET_FILENAME       = DIALOG_PREFIX + "getFilename";
        public static final  String DIALOG_LOGIN              = DIALOG_PREFIX + "login";
        public static final  String DIALOG_LOGOUT             = DIALOG_PREFIX + "logout";
    }

    public static class Broadcast {
        private static final String BROADCAST_PREFIX             = PACKAGE_PREFIX + ".broadcast.";
        public static final  String BROADCAST_DOWNLOAD_IMAGE     = BROADCAST_PREFIX + "download-image";
        public static final  String BROADCAST_ABOUT_SUBREDDIT    = BROADCAST_PREFIX + "about-subreddit";
        public static final  String BROADCAST_HTTP_FINISHED      = BROADCAST_PREFIX + "http-finished";
        public static final  String BROADCAST_LOGIN_COMPLETE     = BROADCAST_PREFIX + "login-complete";
        public static final  String BROADCAST_MY_SUBREDDITS      = BROADCAST_PREFIX + "my-subreddits";
        public static final  String BROADCAST_SUBREDDIT_SELECTED = BROADCAST_PREFIX + "subreddit-selected";
        public static final  String BROADCAST_SUBSCRIBE          = BROADCAST_PREFIX + "subscribe";
        public static final  String BROADCAST_REMOVE_NSFW_IMAGES = BROADCAST_PREFIX + "remove-nsfw-images";
        public static final  String BROADCAST_TOGGLE_FULLSCREEN  = BROADCAST_PREFIX + "fullscreen-toggle";
        public static final  String BROADCAST_UPDATE_SCORE       = BROADCAST_PREFIX + "update-score";
        public static final  String BROADCAST_SUBREDDIT_SEARCH   = BROADCAST_PREFIX + "subreddit-search";
    }

    public static class Loader {
        public static final int LOADER_REDDIT     = 10;
        public static final int LOADER_POSTS      = 20;
        public static final int LOADER_LOGIN      = 30;
        public static final int LOADER_SUBREDDITS = 40;
    }

    public static class Reddit {
        public static final String USER_AGENT                    = "Reddit In Pictures Android by /u/antew";
        public static final int    POSTS_TO_FETCH                = 100;
        public static final String REDDIT_FRONTPAGE              = "REDDIT_FRONTPAGE";
        public static final String REDDIT_FRONTPAGE_DISPLAY_NAME = "Frontpage";
        public static final String REDDIT_ALL_DISPLAY_NAME       = "All";

        public static class Endpoint {
            public static final String REDDIT_BASE_URL              = "http://www.reddit.com";
            public static final String REDDIT_LOGIN_URL             = "https://ssl.reddit.com/api/login/";
            public static final String REDDIT_SUBSCRIBE_URL         = REDDIT_BASE_URL + "/api/subscribe";
            public static final String REDDIT_VOTE_URL              = REDDIT_BASE_URL + "/api/vote";
            public static final String SUBREDDIT_BASE_URL           = REDDIT_BASE_URL + "/r/";
            public static final String REDDIT_ABOUT_URL             = REDDIT_BASE_URL + "/r/%s/about.json";
            public static final String REDDIT_SEARCH_SUBREDDITS_URL = REDDIT_BASE_URL + "/api/search_reddit_names.json";
            public static final String REDDIT_MY_SUBREDDITS_URL     = REDDIT_BASE_URL + "/reddits/mine/subscriber.json";
            public static final String REDDIT_SESSION               = "reddit_session";
            public static final String COMPACT_URL                  = "/.compact";
        }
    }

    public static class Flickr {
        // TODO: Replace with Flickr API key.
        public static final String API_KEY = "REPLACE_THIS_WITH_YOUR_FLICKR_API_KEY";
    }

    public static final float  IMAGE_CACHE_SIZE = 0.25f;
    public static final String JSON             = ".json";

    public static final String WEBVIEW_IMAGE_HTML_BEGIN = "<html><head><style type=\"text/css\">* {padding:0;margin:0;}body {background:#000000;}.center-container {position:absolute;width:100%;height:100%;text-align:center;}.center-agent {display:inline-block;height:100%;vertical-align:middle;}.center-target {display:inline-block;vertical-align:middle;}#image {width: 80%;}</style></head><body><div class=\"center-container\"><span class=\"center-agent\"></span><img id=\"image\" class=\"center-target\" src=\"";
    public static final String WEBVIEW_IMAGE_HTML_END   = "\"/></div></body></html>";
}
