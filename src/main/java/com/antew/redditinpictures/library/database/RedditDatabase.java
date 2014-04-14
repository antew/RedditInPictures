package com.antew.redditinpictures.library.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.database.RedditContract.LoginColumns;
import com.antew.redditinpictures.library.database.RedditContract.PostColumns;
import com.antew.redditinpictures.library.database.RedditContract.RedditDataColumns;
import com.antew.redditinpictures.library.database.RedditContract.SubredditColumns;

public class RedditDatabase extends SQLiteOpenHelper {

    private static final int    DATABASE_VERSION = 6;
    private static final String DATABASE_NAME    = "redditinpictures.db";

    public RedditDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static void deleteDatabase(Context context) {
        context.deleteDatabase(DATABASE_NAME);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Tables.CreateTableSql.POSTDATA);
        db.execSQL(Tables.CreateTableSql.REDDIT_DATA);
        db.execSQL(Tables.CreateTableSql.LOGIN);
        db.execSQL(Tables.CreateTableSql.SUBREDDITS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /* Dropping the tables is useful for quick development, but shouldn't
           be used in release versions.
        db.execSQL("DROP TABLE IF EXISTS " + Tables.POSTDATA);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.REDDIT_DATA);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.LOGIN);
        db.execSQL("DROP TABLE IF EXISTS " + Tables.SUBREDDITS);
        */

        Ln.d("Attempting to Upgrade To %d From %d", newVersion, oldVersion);

        switch (oldVersion) {
            case 1:
                // Database version 1 didn't have the 'priority' column
                db.execSQL("ALTER TABLE " + Tables.SUBREDDITS  + " ADD COLUMN " + SubredditColumns.PRIORITY           + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + Tables.SUBREDDITS  + " ADD COLUMN " + SubredditColumns.USER_IS_SUBSCRIBER + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + Tables.SUBREDDITS  + " ADD COLUMN " + SubredditColumns.DEFAULT_SUBREDDIT  + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + Tables.REDDIT_DATA + " ADD COLUMN " + RedditDataColumns.RETRIEVED_DATE    + " INTEGER DEFAULT 0");
                break;
            case 2:
                //Database version 2 didn't have the 'userIsSubscriber' or 'isDefaultSubreddit' column
                db.execSQL("ALTER TABLE " + Tables.SUBREDDITS  + " ADD COLUMN " + SubredditColumns.USER_IS_SUBSCRIBER + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + Tables.SUBREDDITS  + " ADD COLUMN " + SubredditColumns.DEFAULT_SUBREDDIT  + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + Tables.REDDIT_DATA + " ADD COLUMN " + RedditDataColumns.RETRIEVED_DATE    + " INTEGER DEFAULT 0");
                break;
            case 3:
                //Database version 3 didn't have the 'isDefaultSubreddit' column
                db.execSQL("ALTER TABLE " + Tables.SUBREDDITS  + " ADD COLUMN " + SubredditColumns.DEFAULT_SUBREDDIT + " INTEGER DEFAULT 0");
                db.execSQL("ALTER TABLE " + Tables.REDDIT_DATA + " ADD COLUMN " + RedditDataColumns.RETRIEVED_DATE   + " INTEGER DEFAULT 0");
                break;
            case 4:
                //Database version 4 didn't have the 'retrievedDate' column.
                db.execSQL("ALTER TABLE " + Tables.REDDIT_DATA + " ADD COLUMN " + RedditDataColumns.RETRIEVED_DATE + " INTEGER DEFAULT 0");
                break;
            case 5:
                db.execSQL("ALTER TABLE " + Tables.REDDIT_DATA + " ADD COLUMN " + RedditDataColumns.SUBREDDIT + " TEXT");
                db.execSQL("ALTER TABLE " + Tables.REDDIT_DATA + " ADD COLUMN " + RedditDataColumns.CATEGORY  + " TEXT");
                db.execSQL("ALTER TABLE " + Tables.REDDIT_DATA + " ADD COLUMN " + RedditDataColumns.AGE       + " TEXT");
                break;
        }
    }

    public interface Tables {
        String POSTDATA    = "post_data";
        String REDDIT_DATA = "reddit_data";
        String LOGIN       = "login";
        String SUBREDDITS  = "subreddits";

        interface CreateTableSql {
            String LOGIN = "CREATE TABLE "
                           + Tables.LOGIN
                           + " ("
                           + BaseColumns._ID            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                           + LoginColumns.USERNAME      + " TEXT NOT NULL, "
                           + LoginColumns.COOKIE        + " TEXT, "
                           + LoginColumns.MODHASH       + " TEXT, "
                           + LoginColumns.SUCCESS       + " INTEGER, "
                           + LoginColumns.ERROR_MESSAGE + " TEXT"
                           + " );";

            String SUBREDDITS = "CREATE TABLE "
                                + Tables.SUBREDDITS
                                + " ("
                                + BaseColumns._ID                     + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                + SubredditColumns.DISPLAY_NAME       + " TEXT, "
                                + SubredditColumns.HEADER_IMAGE       + " TEXT, "
                                + SubredditColumns.TITLE              + " TEXT, "
                                + SubredditColumns.URL                + " TEXT, "
                                + SubredditColumns.DESCRIPTION        + " TEXT, "
                                + SubredditColumns.CREATED            + " INTEGER, "
                                + SubredditColumns.CREATED_UTC        + " INTEGER, "
                                + SubredditColumns.HEADER_SIZE        + " TEXT, "
                                + SubredditColumns.OVER_18            + " TEXT, "
                                + SubredditColumns.SUBSCRIBERS        + " INTEGER, "
                                + SubredditColumns.ACCOUNTS_ACTIVE    + " TEXT, "
                                + SubredditColumns.PUBLIC_DESCRIPTION + " TEXT, "
                                + SubredditColumns.HEADER_TITLE       + " TEXT, "
                                + SubredditColumns.SUBREDDIT_ID       + " TEXT, "
                                + SubredditColumns.NAME               + " TEXT, "
                                + SubredditColumns.PRIORITY           + " INTEGER DEFAULT 0,"
                                + SubredditColumns.USER_IS_SUBSCRIBER + " INTEGER DEFAULT 0,"
                                + SubredditColumns.DEFAULT_SUBREDDIT  + " INTEGER DEFAULT 0"
                                + " );";

            String POSTDATA = "CREATE TABLE "
                              + Tables.POSTDATA
                              + " ("
                              + BaseColumns._ID                    + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                              + PostColumns.DOMAIN                 + " TEXT NOT NULL, "
                              + PostColumns.BANNED_BY              + " TEXT, "
                              + PostColumns.SUBREDDIT              + " TEXT NOT NULL, "
                              + PostColumns.SELFTEXT_HTML          + " TEXT, "
                              + PostColumns.SELFTEXT               + " TEXT, "
                              + PostColumns.VOTE                   + " TEXT NOT NULL, "
                              + PostColumns.SAVED                  + " INTEGER, "
                              + PostColumns.POST_ID                + " TEXT NOT NULL, "
                              + PostColumns.CLICKED                + " INTEGER, "
                              + PostColumns.TITLE                  + " TEXT, "
                              + PostColumns.COMMENTS               + " INTEGER, "
                              + PostColumns.SCORE                  + " INTEGER, "
                              + PostColumns.APPROVED_BY            + " TEXT, "
                              + PostColumns.OVER_18                + " INTEGER, "
                              + PostColumns.HIDDEN                 + " INTEGER, "
                              + PostColumns.THUMBNAIL              + " TEXT, "
                              + PostColumns.SUBREDDIT_ID           + " TEXT NOT NULL, "
                              + PostColumns.AUTHOR_FLAIR_CSS_CLASS + " TEXT, "
                              + PostColumns.DOWNS                  + " INTEGER, "
                              + PostColumns.IS_SELF                + " INTEGER, "
                              + PostColumns.PERMALINK              + " TEXT NOT NULL, "
                              + PostColumns.NAME                   + " TEXT NOT NULL, "
                              + PostColumns.CREATED                + " INTEGER, "
                              + PostColumns.URL                    + " TEXT, "
                              + PostColumns.AUTHOR_FLAIR_TEXT      + " TEXT, "
                              + PostColumns.AUTHOR                 + " TEXT NOT NULL, "
                              + PostColumns.CREATED_UTC            + " INTEGER, "
                              + PostColumns.LINK_FLAIR_TEXT        + " TEXT, "
                              + PostColumns.DECODED_URL            + " TEXT, "
                              + PostColumns.LOADED_AT              + " INTEGER"
                              + " );";

            String REDDIT_DATA = "CREATE TABLE "
                                 + Tables.REDDIT_DATA
                                 + " ("
                                 + BaseColumns._ID                  + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                                 + RedditDataColumns.MODHASH        + " TEXT NOT NULL, "
                                 + RedditDataColumns.AFTER          + " TEXT, "
                                 + RedditDataColumns.BEFORE         + " TEXT, "
                                 + RedditDataColumns.RETRIEVED_DATE + " INTEGER DEFAULT 0, "
                                 + RedditDataColumns.SUBREDDIT      + " TEXT, "
                                 + RedditDataColumns.CATEGORY       + " TEXT, "
                                 + RedditDataColumns.AGE            + " TEXT "
                                 + ");";
        }
    }
}
