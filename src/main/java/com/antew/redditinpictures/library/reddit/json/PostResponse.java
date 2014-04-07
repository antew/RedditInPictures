package com.antew.redditinpictures.library.reddit.json;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.reddit.RedditApi;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.SubredditUtils;
import com.antew.redditinpictures.sqlite.RedditContract;
import java.util.Date;

class PostResponse extends RedditResponseHandler {
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
        Ln.i("Deleted %d reddit rows", redditRowsDeleted);

        RedditApi redditApi = JsonDeserializer.deserialize(result.getJson(), RedditApi.class);
        if (redditApi == null) {
            Ln.e("Error parsing Reddit api response");
            return;
        }

        if (redditApi.getData() != null) {
            redditApi.getData().setRetrievedDate(new Date());
        }

        // If we are replacing all, go ahead and clear out the old posts.
        if (result.isReplaceAll()) {
            Bundle arguments = result.getArguments();
            String subreddit = Constants.REDDIT_FRONTPAGE;
            if (arguments.containsKey(RedditService.EXTRA_SUBREDDIT)) {
                subreddit = arguments.getString(RedditService.EXTRA_SUBREDDIT);
            }
            SubredditUtils.deletePostsForSubreddit(context, subreddit);
        }

        ContentValues[] operations = redditApi.getPostDataContentValues(true);
        ContentValues redditValues = redditApi.getContentValues();

        // Add the new Reddit data
        Uri newData = resolver.insert(RedditContract.RedditData.CONTENT_URI, redditValues);
        Ln.i("Inserted reddit data %s", newData.toString());

        // If we're loading a new subreddit remove all existing rows first
        if (result.isReplaceAll()) {
            int rowsDeleted = resolver.delete(RedditContract.Posts.CONTENT_URI, null, null);
            Ln.i( "Deleted %d posts", rowsDeleted);
        }

        int rowsInserted = resolver.bulkInsert(RedditContract.Posts.CONTENT_URI, operations);
        Ln.i("Inserted %d rows", rowsInserted);
    }
}