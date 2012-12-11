package com.antew.redditinpictures.library.service;

public class RedditService extends RESTService {
    private static final String TAG = RedditService.class.getName();

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

}
