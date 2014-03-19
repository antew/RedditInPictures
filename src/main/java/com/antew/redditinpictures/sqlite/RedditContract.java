package com.antew.redditinpictures.sqlite;

import android.net.Uri;
import android.provider.BaseColumns;
import com.antew.redditinpictures.pro.BuildConfig;

public class RedditContract {

    private RedditContract() {
    }

    ;

    public static final String CONTENT_AUTHORITY =
        "com.antew.redditinpictures." + BuildConfig.FLAVOR;
    public static final int BASE = 1;
    public static final int REDDIT = 100;
    public static final int REDDIT_ID = 101;

    public static final int POSTS = 200;
    public static final int POSTS_ID = 201;

    public static final int LOGIN = 300;
    public static final int LOGIN_ID = 301;

    public static final int SUBREDDIT = 400;
    public static final int SUBREDDIT_ID = 401;

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_POSTS = "posts";
    public static final String PATH_REDDIT_DATA = "reddit_data";
    public static final String PATH_SUBREDDITS = "subreddits";
    public static final String PATH_LOGIN = "login";

    public interface RedditDataColumns {
        String MODHASH = "modhash";
        String AFTER = "after";
        String BEFORE = "before";
    }

    public interface LoginColumns {
        String USERNAME = "username";
        String MODHASH = "modhash";
        String COOKIE = "cookie";
        String ERROR_MESSAGE = "errorMessage";
        String SUCCESS = "success";
    }

    /**
     * Container for Subreddit data
     */
    public interface SubredditColumns {
        String DISPLAY_NAME = "displayName";
        String HEADER_IMAGE = "headerImage";
        String TITLE = "title";
        String URL = "url";
        String DESCRIPTION = "description";
        String CREATED = "created";
        String CREATED_UTC = "createdUtc";
        String HEADER_SIZE = "headerSize";
        String OVER_18 = "over18";
        String SUBSCRIBERS = "subscribers";
        String ACCOUNTS_ACTIVE = "accountsActive";
        String PUBLIC_DESCRIPTION = "publicDescription";
        String HEADER_TITLE = "headerTitle";
        String SUBREDDIT_ID = "subredditId";
        String NAME = "name";
        String PRIORITY = "priority";
        String USER_IS_SUBSCRIBER = "userIsSubscriber";
        String DEFAULT_SUBREDDIT = "isDefaultSubreddit";
    }

    public interface PostColumns {
        // String MODHASH = "modhash";
        String DOMAIN = "domain";
        String BANNED_BY = "bannedby";
        String SUBREDDIT = "subreddit";
        String SELFTEXT_HTML = "selfTextHtml";
        String SELFTEXT = "selfText";
        String VOTE = "vote";
        String SAVED = "saved";
        String POST_ID = "postId";
        String CLICKED = "clicked";
        String TITLE = "title";
        String COMMENTS = "numComments";
        String SCORE = "score";
        String APPROVED_BY = "approvedBy";
        String OVER_18 = "over18";
        String HIDDEN = "hidden";
        String THUMBNAIL = "thumbnail";
        String SUBREDDIT_ID = "subredditId";
        String AUTHOR_FLAIR_CSS_CLASS = "authorFlairCssClass";
        String DOWNS = "downs";
        String IS_SELF = "isSelf";
        String PERMALINK = "permalink";
        String NAME = "name";
        String CREATED = "created";
        String URL = "url";
        String AUTHOR_FLAIR_TEXT = "authorFlairText";
        String AUTHOR = "author";
        String CREATED_UTC = "createdUtc";
        String LINK_FLAIR_TEXT = "linkFlairText";
        String DECODED_URL = "decodedUrl";
        String LOADED_AT = "loadedAt";
    }

    public static class Posts implements PostColumns, BaseColumns {
        public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_POSTS).build();
        public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/vnd.redditinpictures.postdata";
        public static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd.redditinpictures.postdata";

        public static final String DEFAULT_SORT = BaseColumns._ID + " ASC";

        public static final String[] GRIDVIEW_PROJECTION = new String[] { _ID, URL, THUMBNAIL };
        public static final String[] LISTVIEW_PROJECTION = new String[] {
            _ID, URL, THUMBNAIL, TITLE, SCORE, SELFTEXT, COMMENTS, SUBREDDIT, DOMAIN, AUTHOR, VOTE,
            NAME, OVER_18
        };

        public static Uri buildPostDataUri(long postNumber) {
            return CONTENT_URI.buildUpon().appendPath(String.valueOf(postNumber)).build();
        }
    }

    public static class Login implements LoginColumns, BaseColumns {
        public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOGIN).build();
        public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/vnd.redditinpictures.login";
        public static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd.redditinpictures.login";

        public static final String DEFAULT_SORT = BaseColumns._ID + " ASC";

        public static Uri buildLoginUri(String username) {
            return CONTENT_URI.buildUpon().appendPath(username).build();
        }
    }

    public static class RedditData implements RedditDataColumns, BaseColumns {
        public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_REDDIT_DATA).build();
        public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/vnd.redditinpictures.redditdata";
        public static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd.redditinpictures.redditdata";

        public static final String DEFAULT_SORT = BaseColumns._ID + " ASC";

        public static Uri buildPostDataUri(String modhash) {
            return CONTENT_URI.buildUpon().appendPath(modhash).build();
        }
    }

    public static class Subreddits implements SubredditColumns, BaseColumns {
        public static final Uri CONTENT_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBREDDITS).build();
        public static final String CONTENT_TYPE =
            "vnd.android.cursor.dir/vnd.redditinpictures.subreddits";
        public static final String CONTENT_ITEM_TYPE =
            "vnd.android.cursor.item/vnd.redditinpictures.subreddits";

        public static final String[] SUBREDDITS_PROJECTION =
            new String[] { _ID, DISPLAY_NAME, PRIORITY, NAME, USER_IS_SUBSCRIBER };

        public static final String DEFAULT_SORT =
            PRIORITY + " DESC, " + DISPLAY_NAME + " COLLATE NOCASE ASC";

        public static Uri buildSubredditUri(String displayName) {
            return CONTENT_URI.buildUpon().appendPath(displayName).build();
        }
    }
}
