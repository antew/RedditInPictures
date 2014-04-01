package com.antew.redditinpictures.library.reddit.json;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.reddit.RedditApi;
import com.antew.redditinpictures.sqlite.RedditContract;

class PostResponse extends RedditResponseHandler {
    public static String TAG = PostResponse.class.getSimpleName();
    private RedditResult result;

    public PostResponse(RedditResult result) {
        super();
        this.result = result;
    }

    @Override
    public void processHttpResponse(Context context) {
        ContentResolver resolver = context.getContentResolver();
        // Each time we want to remove the old before/after/modhash rows from the Reddit data
        int redditRowsDeleted = resolver.delete(RedditContract.RedditData.CONTENT_URI, null, null);
        Log.i(TAG, "Deleted " + redditRowsDeleted + " reddit rows");

        RedditApi redditApi = JsonDeserializer.deserialize(result.getJson(), RedditApi.class);
        if (redditApi == null) {
            Log.e(TAG, "Error parsing Reddit api response");
            return;
        }

        ContentValues[] operations = redditApi.getPostDataContentValues(true);
        ContentValues redditValues = redditApi.getContentValues();

        // Add the new Reddit data
        Uri newData = resolver.insert(RedditContract.RedditData.CONTENT_URI, redditValues);
        Log.i(TAG, "Inserted reddit data " + newData.toString());

        // If we're loading a new subreddit remove all existing rows first
        if (result.isReplaceAll()) {
            int rowsDeleted = resolver.delete(RedditContract.Posts.CONTENT_URI, null, null);
            Log.i(TAG, "Deleted " + rowsDeleted + " posts");
        }

        int rowsInserted = resolver.bulkInsert(RedditContract.Posts.CONTENT_URI, operations);
        Log.i(TAG, "Inserted " + rowsInserted + " rows");
    }
}