package com.antew.redditinpictures.library.event;

public class LoadSubredditEvent {
    private String mSubreddit;

    public LoadSubredditEvent(String mSubreddit) {
        this.mSubreddit = mSubreddit;
    }

    public String getSubreddit() {
        return mSubreddit;
    }
}
