package com.antew.redditinpictures.library.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.LocalBroadcastManager;
import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.MySubreddits;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.reddit.SubredditChildren;
import com.antew.redditinpictures.library.reddit.SubredditData;
import com.antew.redditinpictures.library.reddit.json.MySubredditsResponse;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.subredditmanager.SubredditManager;
import com.antew.redditinpictures.pro.R;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.antew.redditinpictures.sqlite.RedditDatabase;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

public class SubredditUtils {
    public static void setDefaultSubreddits(Context context) {
        if (context == null) {
            Ln.e("Got a Null Context");
            return;
        }

        Ln.d("Setting Default Subreddits");
        // Get the defaults subreddit response (static JSON).
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.default_subreddits)));

        String fakeJson = "";
        String line;
        try
        {
            line = reader.readLine();
            while (line != null)
            {
                fakeJson = fakeJson + line;
                line = reader.readLine();
            }
        }
        catch (IOException e)
        {
            Ln.e(e, "Failed to Load Default Subreddits");
            return;
        }

        ContentResolver resolver = context.getContentResolver();

        int userRowsDeleted = resolver.delete(RedditContract.Subreddits.CONTENT_URI, null, null);
        Ln.d("Deleted %d Subreddits", userRowsDeleted);

        MySubreddits mySubreddits = JsonDeserializer.deserialize(fakeJson, MySubreddits.class);

        if (mySubreddits == null) {
            Ln.e("Failed to Desearialize Default Subreddits");
            return;
        }

        ContentValues[] operations = mySubreddits.getContentValuesArray();

        int rowsInserted = resolver.bulkInsert(RedditContract.Subreddits.CONTENT_URI, operations);
        Ln.d("Inserted %d subreddits", rowsInserted);

        ArrayList<String> subReddits = new ArrayList<String>();

        for (SubredditChildren c : mySubreddits.getData().getChildren()) {
            SubredditData data = c.getData();
            subReddits.add(data.getDisplay_name());
        }

        Collections.sort(subReddits, StringUtil.getCaseInsensitiveComparator());
        SharedPreferencesHelper.saveArray(subReddits, SubredditManager.PREFS_NAME, SubredditManager.ARRAY_NAME, context);

        Intent intent = new Intent(Constants.BROADCAST_MY_SUBREDDITS);
        intent.putStringArrayListExtra(Constants.EXTRA_MY_SUBREDDITS, subReddits);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static void mergeDefaultSubreddits(Context context) {
        if (context == null) {
            Ln.e("Got a Null Context");
            return;
        }

        Ln.d("Merging Default Subreddits");
        // Get the defaults subreddit response (static JSON).
        BufferedReader reader = new BufferedReader(new InputStreamReader(context.getResources().openRawResource(R.raw.default_subreddits)));

        String fakeJson = "";
        String line;
        try
        {
            line = reader.readLine();
            while (line != null)
            {
                fakeJson = fakeJson + line;
                line = reader.readLine();
            }
        }
        catch (IOException e)
        {
            Ln.e(e, "Failed to Load Default Subreddits");
            return;
        }

        ContentResolver resolver = context.getContentResolver();

        MySubreddits mySubreddits = JsonDeserializer.deserialize(fakeJson, MySubreddits.class);

        if (mySubreddits == null) {
            Ln.e("Failed to Desearialize Default Subreddits");
            return;
        }


        ContentValues[] operations = mySubreddits.getContentValuesArray();

        // Delete any currently existing, to allow us to batch merge...limitations of SQLLite on Android make me sad.
        for (ContentValues contentValue : operations) {
            //TODO: Once API-11 is sunset, replace with a conditional update instead of delete/insert.
            // Updates with parameters aren't supported prior to API-11 (Honeycomb). So instead we are just deleting the record if it exists and recreating it.
            resolver.delete(RedditContract.Subreddits.CONTENT_URI, "subredditId = ?", new String[] { contentValue.getAsString(RedditContract.Subreddits.SUBREDDIT_ID)});
        }

        int rowsInserted = resolver.bulkInsert(RedditContract.Subreddits.CONTENT_URI, operations);
        Ln.d("Inserted %d subreddits", rowsInserted);

        ArrayList<String> subReddits = new ArrayList<String>();

        for (SubredditChildren c : mySubreddits.getData().getChildren()) {
            SubredditData data = c.getData();
            subReddits.add(data.getDisplay_name());
        }

        Collections.sort(subReddits, StringUtil.getCaseInsensitiveComparator());
        SharedPreferencesHelper.saveArray(subReddits, SubredditManager.PREFS_NAME, SubredditManager.ARRAY_NAME, context);

        Intent intent = new Intent(Constants.BROADCAST_MY_SUBREDDITS);
        intent.putStringArrayListExtra(Constants.EXTRA_MY_SUBREDDITS, subReddits);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static boolean isDefaultSubreddit(String subreddit) {
        if (subreddit.equals(Constants.REDDIT_FRONTPAGE)) {
            return true;
        }

        for (MySubredditsResponse.DefaultSubreddit defaultSubreddit : MySubredditsResponse.DefaultSubreddit.values()) {
            if (defaultSubreddit.getDisplayName().equals(subreddit)) {
                return true;
            }
        }
        return false;
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
                SQLiteDatabase mDatabase = mDatabaseHelper.getWritableDatabase();

                // Using a separate variable here since I want to consolidate operations and not overwrite the control variable possibly causing more problems.
                boolean terminateSubreddits = forceDefaults;

                // If we aren't terminating them by default, check to see if they have none. If so we want to set it to the defaults.
                if (!terminateSubreddits) {
                    // See how many Subreddits are in the database. Only needed if not forcing defaults.
                    long numSubreddits =
                        DatabaseUtils.queryNumEntries(mDatabase, RedditDatabase.Tables.SUBREDDITS);
                    Ln.d("Number of Subreddits is: %d", numSubreddits);
                    mDatabase.close();

                    // Set the indicator to cause the subreddits to be overwritten if we have no records.
                    if (numSubreddits == 0) {
                        terminateSubreddits = true;
                    }
                }

                // If we either don't have any subreddits or we want to force them to defaults.
                if (terminateSubreddits) {
                    SubredditUtils.setDefaultSubreddits(mContext);
                }
            }
            return null;
        }
    }
}
