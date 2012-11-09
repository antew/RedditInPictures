package com.antew.redditinpictures.library.reddit;

import com.antew.redditinpictures.library.reddit.MySubreddits.SubredditData;

public class About {
    private String        kind;
    private SubredditData data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public SubredditData getData() {
        return data;
    }

    public void setData(SubredditData data) {
        this.data = data;
    }
}
