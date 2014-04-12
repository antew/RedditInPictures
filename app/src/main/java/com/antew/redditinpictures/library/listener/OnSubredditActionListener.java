package com.antew.redditinpictures.library.listener;

import com.antew.redditinpictures.library.model.reddit.SubredditData;

public interface OnSubredditActionListener {
    public void onAction(SubredditData subredditData, SubredditAction action);

    enum SubredditAction {
        View, Subscribe, Unsubscribe, Info, Delete;
    }
}
