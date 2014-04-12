package com.antew.redditinpictures.library.model.reddit;

public class SubredditChildren {
    private String        kind;
    private SubredditData data;

    public SubredditChildren(String kind, String subredditDisplayName) {
        this.kind = kind;
        SubredditData subreddit = new SubredditData(subredditDisplayName);
        this.data = subreddit;
    }

    public String getKind() {
        return kind;
    }

    public SubredditData getData() {
        return data;
    }
}