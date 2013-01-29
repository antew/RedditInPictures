package com.antew.redditinpictures.library.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.library.reddit.RedditLoginInformation;
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.reddit.RedditUrl.Age;
import com.antew.redditinpictures.library.reddit.RedditUrl.Category;
import com.antew.redditinpictures.library.reddit.SubscribeAction;
import com.antew.redditinpictures.library.reddit.Vote;
import com.antew.redditinpictures.library.reddit.json.RedditResult;
import com.antew.redditinpictures.library.utils.Consts;
import com.antew.redditinpictures.sqlite.RedditContract;

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

    @Override
    public void onRequestComplete(Intent result) {
        super.onRequestComplete(result);

        RedditResult redditResult = new RedditResult(result);

        if (redditResult.getHttpStatusCode() != 200 || redditResult.getJson() == null) {
            Log.i(TAG, "onRequestComplete - error retrieving data, status code was " + redditResult.getHttpStatusCode());
            return;
        }

        redditResult.handleResponse(getApplicationContext());
        
    }

    private static Intent getIntentBasics(Intent intent) {
        intent.putExtra(EXTRA_USER_AGENT, USER_AGENT);

        if (RedditLoginInformation.isLoggedIn())
            intent.putExtra(EXTRA_COOKIE, REDDIT_SESSION + "=" + RedditLoginInformation.getCookie());

        return intent;
    }

    public static void getPosts(Context context, String subreddit, Age age, Category category, String after, boolean replaceAll) {
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
        getPosts(context, url.getUrl(), replaceAll);

    }

    public static void getPosts(Context context, String url, boolean replaceAll) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.POSTS);
        intent.putExtra(RedditService.EXTRA_REPLACE_ALL, replaceAll);
        intent.setData(Uri.parse(url));

        context.startService(intent);
    }

    public static void vote(Context context, String id, String subreddit, Vote vote) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.setData(Uri.parse(REDDIT_VOTE_URL));
        intent.putExtra(EXTRA_HTTP_VERB, POST);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.VOTE);

        Bundle bundle = new Bundle();
        bundle.putString("id", id);
        bundle.putInt("dir", vote.getVote());
        bundle.putString("r", subreddit);
        bundle.putString("uh", RedditLoginInformation.getModhash());

        intent.putExtra(EXTRA_PARAMS, bundle);
        
        context.startService(intent);
    }

    public static void login(Context context, String username, String password) {
        String url = REDDIT_LOGIN_URL + username;

        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.setData(Uri.parse(url));
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.LOGIN);
        intent.putExtra(EXTRA_HTTP_VERB, POST);

        Bundle extraPassthru = new Bundle();
        extraPassthru.putString(RedditContract.Login.USERNAME, username);

        Bundle params = new Bundle();
        params.putString("api_type", "json");
        params.putString("user", username);
        params.putString("passwd", password);

        intent.putExtra(EXTRA_PASS_THROUGH, extraPassthru);
        intent.putExtra(EXTRA_PARAMS, params);

        context.startService(intent);
    }

    public static void getMySubreddits(Context context) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.setData(Uri.parse(REDDIT_MY_SUBREDDITS_URL));
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.MY_SUBREDDITS);
        intent.putExtra(EXTRA_HTTP_VERB, GET);
        
        context.startService(intent);
    }

    public static void aboutSubreddit(Context context, String subreddit) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.ABOUT_SUBREDDIT);
        intent.setData(Uri.parse(String.format(REDDIT_ABOUT_URL, subreddit)));
        
        context.startService(intent);
    }
    
    public static void subscribe(Context context, String subreddit) {
        changeSubscription(context, subreddit, SubscribeAction.SUBSCRIBE);
    }
    
    public static void unsubscribe(Context context, String subreddit) {
        changeSubscription(context, subreddit, SubscribeAction.UNSUBSCRIBE);
    }

    private static void changeSubscription(Context context, String subreddit, SubscribeAction action) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestCode.SUBSCRIBE);
        intent.setData(Uri.parse(REDDIT_SUBSCRIBE_URL));
        intent.putExtra(EXTRA_HTTP_VERB, POST);
        intent.putExtra("action", action.getAction());
        intent.putExtra("sr_name", subreddit);
        intent.putExtra("uh", RedditLoginInformation.getModhash());

        context.startService(intent);
    }
}
