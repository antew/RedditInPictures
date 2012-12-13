package com.antew.redditinpictures.library.service;

import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.provider.RedditProvider;
import com.antew.redditinpictures.library.reddit.MySubreddits;
import com.antew.redditinpictures.library.reddit.RedditApi;
import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.library.reddit.RedditApiManager;
import com.antew.redditinpictures.library.reddit.RedditLoginResponse;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.reddit.RedditUrl.Age;
import com.antew.redditinpictures.library.reddit.RedditUrl.Category;
import com.antew.redditinpictures.library.reddit.SubscribeAction;
import com.antew.redditinpictures.library.reddit.Vote;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.sqlite.RedditContract;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

public class RedditService extends RESTService {
    private static final String TAG                      = RedditService.class.getName();
    public static final String  USER_AGENT               = "Reddit In Pictures Android by /u/antew";
    private static final String REDDIT_LOGIN_URL         = "https://ssl.reddit.com/api/login/";
    private static final String REDDIT_SUBSCRIBE_URL     = "http://www.reddit.com/api/subscribe";
    private static final String REDDIT_VOTE_URL          = "http://www.reddit.com/api/vote";
    private static final String REDDIT_ABOUT_URL         = "http://www.reddit.com/r/%s/about.json";
    public static final String  REDDIT_SESSION           = "reddit_session";
    private static final String REDDIT_MY_SUBREDDITS_URL = "http://www.reddit.com/reddits/mine/subscriber.json";
    public static final String  COMPACT_URL              = "/.compact";
    public static final String  REDDIT_BASE_URL          = "http://www.reddit.com";

    public interface RequestTypes {
        int LOGIN           = 100;
        int POSTS           = 110;
        int LOGOUT          = 120;
        int VOTE            = 130;
        int MY_SUBREDDITS   = 140;
        int SUBSCRIBE       = 150;
        int ABOUT_SUBREDDIT = 160;
    }

    public RedditService() {
        super();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        super.onHandleIntent(intent);
    }

    @Override
    public void onRequestComplete(Intent result) {
        super.onRequestComplete(result);

        Bundle args = result.getBundleExtra(EXTRA_BUNDLE);
        int requestCode = args.getInt(EXTRA_REQUEST_CODE);
        int statusCode = args.getInt(EXTRA_STATUS_CODE);
        boolean replaceAll = args.getBoolean(EXTRA_REPLACE_ALL);
        String json = args.getString(REST_RESULT);

        if (statusCode != 200 || json == null) {
            return;
        }

        Gson gson = new Gson();

        switch (requestCode) {
            case RequestTypes.POSTS:

                try {
                    // Each time we want to remove the old before/after/modhash rows from the Reddit data
                    int redditRowsDeleted = getContentResolver().delete(RedditContract.RedditData.CONTENT_URI, null, null);
                    Log.i(TAG, "Deleted " + redditRowsDeleted + " reddit rows");
                    
                    RedditApi redditApi = RedditApi.getGson().fromJson(json, RedditApi.class);
                    List<PostData> mEntries = RedditApiManager.filterPosts(redditApi, true);

                    ContentValues[] operations = RedditProvider.contentValuesFromPostData(mEntries);
                    ContentValues redditValues = RedditProvider.contentValuesFromRedditApi(redditApi);

                    // Add the new Reddit data
                    Uri newData = getContentResolver().insert(RedditContract.RedditData.CONTENT_URI, redditValues);
                    Log.i(TAG, "Inserted reddit data " + newData.toString());
                    
                    // If we're loading a new subreddit remove all existing rows first
                    if (replaceAll) {
                        int rowsDeleted = getContentResolver().delete(RedditContract.Posts.CONTENT_URI, null, null);
                        Log.i(TAG, "Deleted " + rowsDeleted + " posts");
                    }
                    
                    int rowsInserted = getContentResolver().bulkInsert(RedditContract.Posts.CONTENT_URI, operations);
                    Log.i(TAG, "Inserted " + rowsInserted + " rows");

                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "onReceiveResult - JsonSyntaxException while parsing json!", e);
                    return;
                } catch (IllegalStateException e) {
                    Log.e(TAG, "onReceiveResult - IllegalStateException while parsing json!", e);
                    return;
                }

                break;

            case RequestTypes.LOGIN:
                Log.i(TAG, "login request finished! = " + json);
                RedditLoginResponse mRedditLoginResponse = new Gson().fromJson(json, RedditLoginResponse.class);
                break;

            case RequestTypes.MY_SUBREDDITS:
                Log.i(TAG, "MySubreddits complete! = " + json);
                MySubreddits mySubreddits = gson.fromJson(json, MySubreddits.class);
                break;

            case RequestTypes.VOTE:
                Log.i(TAG, "Got back from vote! = " + json);
                break;

            case RequestTypes.ABOUT_SUBREDDIT:
                Log.i(TAG, "Got back subreddit about! = " + json);
                break;

            case RequestTypes.SUBSCRIBE:
                Log.i(TAG, "Got back from subscribe! = " + json);

                break;

        }

    }

    private static Intent getIntentBasics(Intent intent) {
        intent.putExtra(EXTRA_USER_AGENT, USER_AGENT);

        if (RedditApiManager.isLoggedIn())
            intent.putExtra(EXTRA_COOKIE, REDDIT_SESSION + "=" + RedditApiManager.getLoginCookie());

        return intent;
    }
    
    public static Intent getPostIntent(Context context, String subreddit, Age age, Category category, String after, boolean replaceAll) {
        if (subreddit == null)
            subreddit = RedditUrl.REDDIT_FRONTPAGE;
        
        if (age == null)
            age = Age.TODAY;
        
        if (category == null)
            category = Category.HOT;
        
        //@formatter:off
        RedditUrl url = new RedditUrl.Builder(subreddit)
                                     .age(age)
                                     .category(category)
                                     .count(Consts.POSTS_TO_FETCH)
                                     .after(after)
                                     .build();
        
        //@formatter:on
        return getPostIntent(context, url.getUrl(), replaceAll);

    }

    public static Intent getPostIntent(Context context, String url, boolean replaceAll) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestTypes.POSTS);
        intent.putExtra(RedditService.EXTRA_REPLACE_ALL, replaceAll);
        intent.setData(Uri.parse(url));

        return intent;
    }

    public static Intent getVoteIntent(Context context, String id, String subreddit, Vote vote) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.setData(Uri.parse(REDDIT_VOTE_URL));
        intent.putExtra(EXTRA_HTTP_VERB, POST);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestTypes.VOTE);

        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putInt("dir", vote.getVote());
        bundle.putString("r", subreddit);
        bundle.putString("uh", RedditApiManager.getModHash());

        intent.putExtra(EXTRA_PARAMS, bundle);

        return intent;
    }

    public static Intent getLoginIntent(Context context, String username, String password) {
        String url = REDDIT_LOGIN_URL + username;

        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.setData(Uri.parse(url));
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestTypes.LOGIN);
        intent.putExtra(EXTRA_HTTP_VERB, POST);

        Bundle params = new Bundle();
        params.putString("api_type", "json");
        params.putString("user", username);
        params.putString("passwd", password);
        intent.putExtra(EXTRA_PARAMS, params);

        return intent;
    }

    public static Intent getMySubredditsIntent(Context context) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.setData(Uri.parse(REDDIT_MY_SUBREDDITS_URL));
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestTypes.MY_SUBREDDITS);
        intent.putExtra(EXTRA_HTTP_VERB, GET);

        return intent;
    }

    public static Intent getSubscribeIntent(Context context, String name, SubscribeAction action) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.setData(Uri.parse(REDDIT_SUBSCRIBE_URL));
        intent.putExtra("action", action.getAction());
        intent.putExtra("sr", name);
        intent.putExtra("uh", RedditApiManager.getModHash());

        return intent;
    }

    public static Intent getAboutIntent(Context context, String subreddit) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.setData(Uri.parse(String.format(REDDIT_ABOUT_URL, subreddit)));

        return intent;
    }

}
