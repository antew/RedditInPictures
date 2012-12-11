package com.antew.redditinpictures.library.service;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.antew.redditinpictures.library.reddit.RedditApiManager;
import com.antew.redditinpictures.library.reddit.RedditLoginResponse;
import com.antew.redditinpictures.library.reddit.Vote;

public class RedditService extends RESTService {
    private static final String        TAG                      = RedditService.class.getName();
    public static final String         USER_AGENT               = "Reddit In Pictures Android by /u/antew";
    private static final String        REDDIT_LOGIN_URL         = "https://ssl.reddit.com/api/login/";
    private static final String        REDDIT_SUBSCRIBE_URL     = "http://www.reddit.com/api/subscribe";
    private static final String        REDDIT_VOTE_URL          = "http://www.reddit.com/api/vote";
    private static final String        REDDIT_ABOUT_URL         = "http://www.reddit.com/r/%s/about.json";
    public static final String         REDDIT_SESSION           = "reddit_session";
    private static final String        REDDIT_MY_SUBREDDITS_URL = "http://www.reddit.com/reddits/mine/subscriber.json";
    public static final String         COMPACT_URL              = "/.compact";
    public static final String         REDDIT_BASE_URL          = "http://www.reddit.com";
    private static RedditLoginResponse mRedditLoginResponse;

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

    public static Intent getIntentBasics(Intent intent) {
        intent.putExtra(EXTRA_USER_AGENT, USER_AGENT);

        if (RedditApiManager.isLoggedIn())
            intent.putExtra(EXTRA_COOKIE, REDDIT_SESSION + "=" + RedditApiManager.getLoginCookie());

        return intent;
    }

    public static Intent getPostIntent(Context context, String url) {
        Intent intent = new Intent(context, RedditService.class);
        intent = getIntentBasics(intent);
        intent.putExtra(RedditService.EXTRA_REQUEST_CODE, RequestTypes.POSTS);
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
        bundle.putString("uh", mRedditLoginResponse.getLoginResponse().getData().getModhash());

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

}
