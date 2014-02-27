package com.antew.redditinpictures.library.reddit;

public class SubredditChildren {
    private String        kind;
    private SubredditData data;

    public String getKind() {
        return kind;
    }

    public SubredditData getData() {
        return data;
    }

    public SubredditChildren(String kind, String subredditDisplayName) {
        this.kind = kind;
        SubredditData subreddit = new SubredditData(subredditDisplayName);
        this.data = subreddit;
    }
}