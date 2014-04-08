package com.antew.redditinpictures.library.reddit;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;
import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.model.reddit.RedditApi;
import com.antew.redditinpictures.library.model.reddit.RedditApiData;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.utils.BundleUtil;
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

        RedditApi redditApi = JsonDeserializer.deserialize(result.getJson(), RedditApi.class);
        if (redditApi == null) {
            Ln.e("Error parsing Reddit api response");
            return;
        }

        Bundle arguments = result.getExtraData();
        boolean replaceAll = BundleUtil.getBoolean(arguments, RedditService.EXTRA_REPLACE_ALL, false);
        String subreddit = BundleUtil.getString(arguments, Constants.REDDIT_FRONTPAGE, Constants.REDDIT_FRONTPAGE);
        Category category = Category.fromString(BundleUtil.getString(arguments, RedditService.EXTRA_CATEGORY, Category.HOT.getName()));
        Age age = Age.fromString(BundleUtil.getString(arguments, RedditService.EXTRA_AGE, Age.TODAY.getAge()));

        // If we are replacing all, go ahead and clear out the old posts.
        if (replaceAll) {
            SubredditUtils.deletePostsForSubreddit(context, subreddit);
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
        int redditRowsDeleted = resolver.delete(RedditContract.RedditData.CONTENT_URI, "subreddit = ?", new String[] { subreddit });
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