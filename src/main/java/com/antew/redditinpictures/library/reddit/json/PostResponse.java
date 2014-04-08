package com.antew.redditinpictures.library.reddit.json;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.reddit.RedditApi;
import com.antew.redditinpictures.library.reddit.RedditApiData;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.utils.Ln;
import com.antew.redditinpictures.library.utils.Strings;
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

        RedditApi redditApi = JsonDeserializer.deserialize(result.getJson(), RedditApi.class);
        if (redditApi == null) {
            Ln.e("Error parsing Reddit api response");
            return;
        }

        Bundle arguments = result.getArguments();
        String subreddit = Constants.REDDIT_FRONTPAGE;
        if (arguments.containsKey(RedditService.EXTRA_SUBREDDIT)) {
            subreddit = arguments.getString(RedditService.EXTRA_SUBREDDIT);
        }

        // If we are replacing all, go ahead and clear out the old posts.
        if (result.isReplaceAll() && Strings.notEmpty(subreddit)) {
            SubredditUtils.deletePostsForSubreddit(context, subreddit);
        }

        Category category = Category.HOT;
        if (arguments.containsKey(RedditService.EXTRA_CATEGORY)) {
            category = Category.valueOf(arguments.getString(RedditService.EXTRA_CATEGORY));
        }

        Age age = Age.TODAY;
        if (arguments.containsKey(RedditService.EXTRA_AGE)) {
            age = Age.valueOf(arguments.getString(RedditService.EXTRA_AGE));
        }

        RedditApiData data = redditApi.getData();
        if (data != null) {
            data.setRetrievedDate(new Date());
            data.setSubreddit(subreddit);
            data.setCategory(category);
            data.setAge(age);
        }

        // Each time we want to remove the old before/after/modhash rows from the Reddit data
        // TODO: Update this to support category and age in the future. This will also require changes to how we handle storing results.
        int redditRowsDeleted = resolver.delete(RedditContract.RedditData.CONTENT_URI, "subreddit = ?", new String[] {subreddit});
        Ln.i("Deleted %d reddit rows", redditRowsDeleted);

        ContentValues[] operations = redditApi.getPostDataContentValues(true);
        ContentValues redditValues = redditApi.getContentValues();

        // Add the new Reddit data
        Uri newData = resolver.insert(RedditContract.RedditData.CONTENT_URI, redditValues);
        Ln.i("Inserted reddit data %s", newData.toString());

        int rowsInserted = resolver.bulkInsert(RedditContract.Posts.CONTENT_URI, operations);
        Ln.i("Inserted %d rows", rowsInserted);
    }
}