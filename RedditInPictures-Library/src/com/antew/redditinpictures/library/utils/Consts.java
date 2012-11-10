package com.antew.redditinpictures.library.utils;

public class Consts {
    public static final int    POSITION_FRONTPAGE             = 0;

    // Constants for extras
    public static final String EXTRA_FILENAME                 = "filenameExtra";
    public static final String EXTRA_IMAGE_HASH               = "imageHash";
    public static final String EXTRA_IS_SYSTEM_UI_VISIBLE     = "isSystemUiVisible";
    public static final String EXTRA_NAV_POSITION             = "navPosition";
    public static final String EXTRA_NEWLY_SELECTED_SUBREDDIT = "newSelectedSubreddit";
    public static final String EXTRA_PERMALINK                = "permalink";
    public static final String EXTRA_REDDIT_API               = "redditApi";
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
