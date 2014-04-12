package com.antew.redditinpictures.library.reddit;

import android.content.Context;

abstract class RedditResponseHandler {

    public static RedditResponseHandler newInstance(RedditResult result) {
        switch (result.getRequestCode()) {
            case ABOUT_SUBREDDIT:
                return new AboutResponse(result);
            case LOGIN:
                return new LoginResponse(result);
            case LOGOUT:
                return new LogoutResponse(result);
            case MY_SUBREDDITS:
                return new MySubredditsResponse(result);
            case POSTS:
                return new PostResponse(result);
            case SUBSCRIBE:
                return new SubscribeResponse(result);
            case VOTE:
                return new VoteResponse(result);
            case SEARCH_SUBREDDITS:
                return new SubredditsSearchResponse(result);
        }

        return null;
    }

    public abstract void processHttpResponse(Context context);
}