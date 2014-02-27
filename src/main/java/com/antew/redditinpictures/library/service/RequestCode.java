package com.antew.redditinpictures.library.service;

public enum RequestCode {
    LOGIN(100), POSTS(110), LOGOUT(120), VOTE(130), MY_SUBREDDITS(140), SUBSCRIBE(150), ABOUT_SUBREDDIT(160), SEARCH_SUBREDDITS(170);

    private int requestCode;

    RequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public int getRequestCode() {
        return requestCode;
    }
}