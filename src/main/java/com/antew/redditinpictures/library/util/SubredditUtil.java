package com.antew.redditinpictures.library.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.model.reddit.MySubreddits;
import com.antew.redditinpictures.library.model.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.model.reddit.SubredditData;
import com.antew.redditinpictures.library.reddit.MySubredditsResponse;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.library.database.RedditContract;
import com.antew.redditinpictures.library.database.RedditDatabase;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class SubredditUtil {
    public static void setDefaultSubreddits(Context context) {
        if (context == null) {
            Ln.e("Got a Null Context");
            return;
        }

        Ln.d("Setting Default Subreddits");
        // Get the defaults subreddit response (static JSON).
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.default_subreddits)));

        ContentResolver resolver = context.getContentResolver();

        int userRowsDeleted = resolver.delete(RedditContract.Subreddits.CONTENT_URI, null, null);
        Ln.d("Deleted %d Subreddits", userRowsDeleted);

        MySubreddits mySubreddits = JsonDeserializer.deserialize(reader, MySubreddits.class);

        if (mySubreddits == null) {
            Ln.e("Failed to Desearialize Default Subreddits");
            return;
        }

        MySubredditsResponse.DefaultSubreddit[] defaultSubreddits = MySubredditsResponse.DefaultSubreddit.values();

        int capacity = defaultSubreddits.length + mySubreddits.getCount();
        List<ContentValues> defaultSubredditOperations = new ArrayList<ContentValues>(capacity);

        for (MySubredditsResponse.DefaultSubreddit subreddit : defaultSubreddits) {
            defaultSubredditOperations.add(
                mySubreddits.getContentValues(new SubredditData(subreddit.getDisplayName(), subreddit.getPriority())));
        }

        defaultSubredditOperations.addAll(mySubreddits.getContentValuesArray());

        int defaultRowsInserted = resolver.bulkInsert(RedditContract.Subreddits.CONTENT_URI,
                                                      defaultSubredditOperations.toArray(new ContentValues[] { }));
        Ln.d("Inserted %d default subreddits", defaultRowsInserted);
    }

    public static boolean isAggregateSubreddit(String subreddit) {
        if (subreddit.equals(Constants.Reddit.REDDIT_FRONTPAGE) || subreddit.equals(Constants.Reddit.REDDIT_ALL_DISPLAY_NAME) || subreddit.equals(
            Constants.Reddit.REDDIT_FRONTPAGE_DISPLAY_NAME)) {
            return true;
        }

        return false;
    }

    public static void deletePostsForSubreddit(Context context, String subreddit) {
        ContentResolver resolver = context.getContentResolver();

        // If we have an aggregate subreddit we need to clear out everything.
        if (isAggregateSubreddit(subreddit)) {
            Ln.d("%s is an Aggregate Subreddit, Cleaning Out Everything", subreddit);
            // Remove all of the post rows.
            resolver.delete(RedditContract.Posts.CONTENT_URI, null, null);
            resolver.delete(RedditContract.RedditData.CONTENT_URI, null, null);
        } else if (subreddit.contains("+")) {
            Ln.d("%s is a Mutli, Clearing Out Contained Subreddits", subreddit);
            // Poor mans checking for multis. If we have a multi, we want to handle all of them appropriately.
            String[] subredditArray = subreddit.split("\\+");

            String where = null;
            List<String> selectionArgsList = new ArrayList<String>();

            for (String item : subredditArray) {
                if (where == null) {
                    where = RedditContract.PostColumns.SUBREDDIT + " in (?";
                } else {
                    where += ",?";
                }
                selectionArgsList.add(item);
            }
            // Close the in statement.
            where += ")";

            // Only delete records for the subreddits contained in the multi.
            resolver.delete(RedditContract.Posts.CONTENT_URI, where, selectionArgsList.toArray(new String[] { }));
            resolver.delete(RedditContract.RedditData.CONTENT_URI, "subreddit = ?", new String[] { subreddit });
        } else {
            Ln.d("%s is Neither an Aggregate or Multi, Clearing Out It Only", subreddit);
            String where = RedditContract.PostColumns.SUBREDDIT + " = ?";
            String[] selectionArgs = new String[] { subreddit };

            // Otherwise we have a single subreddit, so we want to remove only posts for that subreddit.
            resolver.delete(RedditContract.Posts.CONTENT_URI, where, selectionArgs);
            resolver.delete(RedditContract.RedditData.CONTENT_URI, where, selectionArgs);
        }
    }

    public static class SetDefaultSubredditsTask extends SafeAsyncTask<Void> {
        boolean forceDefaults = false;
        Context mContext;

        public SetDefaultSubredditsTask(Context context) {
            mContext = context;
        }

        public SetDefaultSubredditsTask(Context context, boolean forceDefaults) {
            mContext = context;
            this.forceDefaults = forceDefaults;
        }

        @Override
        public Void call() throws Exception {
            // If the user is logged in, we just want to update to what they have set.
            if (RedditLoginInformation.isLoggedIn()) {
                RedditService.getMySubreddits(mContext);
            } else {
                RedditDatabase mDatabaseHelper = new RedditDatabase(mContext);
                SQLiteDatabase mDatabase = mDatabaseHelper.getReadableDatabase();

                // Using a separate variable here since I want to consolidate operations and not overwrite the control variable possibly causing more problems.
                boolean terminateSubreddits = forceDefaults;

                // If we aren't terminating them by default, check to see if they have none. If so we want to set it to the defaults.
                if (!terminateSubreddits) {
                    // See how many Subreddits are in the database. Only needed if not forcing defaults.
                    long numSubreddits = DatabaseUtils.queryNumEntries(mDatabase, RedditDatabase.Tables.SUBREDDITS);
                    Ln.d("Number of Subreddits is: %d", numSubreddits);
                    mDatabase.close();

                    // Set the indicator to cause the subreddits to be overwritten if we have no records.
                    if (numSubreddits == 0) {
                        terminateSubreddits = true;
                    }
                }

                // If we either don't have any subreddits or we want to force them to defaults.
                if (terminateSubreddits) {
                    SubredditUtil.setDefaultSubreddits(mContext);
                }
            }
            return null;
        }
    }
}
