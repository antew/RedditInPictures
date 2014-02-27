package com.antew.redditinpictures.library.reddit.json;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;

import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.preferences.SharedPreferencesHelper;
import com.antew.redditinpictures.library.reddit.About;
import com.antew.redditinpictures.library.reddit.MySubreddits;
import com.antew.redditinpictures.library.reddit.SubredditChildren;
import com.antew.redditinpictures.library.reddit.SubredditData;
import com.antew.redditinpictures.library.subredditmanager.SubredditManager;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.StringUtil;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.antew.redditinpictures.sqlite.RedditDatabase;

import java.util.ArrayList;
import java.util.Collections;

public class AboutResponse extends RedditResponseHandler {

    public static final String TAG = AboutResponse.class.getSimpleName();
    private RedditResult       result;

    public AboutResponse(RedditResult result) {
        this.result = result;
    }

    @Override
    public void processHttpResponse(Context context) {
        Ln.v("About Subreddit complete! = %s", result.getJson());
        About aboutSubreddit = JsonDeserializer.deserialize(result.getJson(), About.class);

        if (aboutSubreddit == null) {
            Ln.e("Something went wrong on About Subreddit status code: %d json: %s", result.getHttpStatusCode(), result.getJson());
            return;
        }

        RedditDatabase mDatabaseHelper = new RedditDatabase(context);
        SQLiteDatabase mDatabase = mDatabaseHelper.getWritableDatabase();


        ContentResolver resolver = context.getContentResolver();
        //TODO: Once API-11 is sunset, replace with an update instead of delete/insert.
        // Updates with parameters aren't supported prior to API-11 (Honeycomb). So instead we are just deleting the record if it exists and recreating it.
        resolver.delete(RedditContract.Subreddits.CONTENT_URI, "subredditId = ?", new String[] { aboutSubreddit.getData().getId()});
        Ln.v("Deleted row");
        resolver.insert(RedditContract.Subreddits.CONTENT_URI, aboutSubreddit.getContentValues());
        Ln.v("Inserted row");

        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(Consts.BROADCAST_ABOUT_SUBREDDIT));
    }

}
