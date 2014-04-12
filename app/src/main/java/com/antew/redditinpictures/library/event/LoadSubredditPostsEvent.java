package com.antew.redditinpictures.library.event;

import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;

public class LoadSubredditPostsEvent {
    private String   mSubreddit;
    private Category mCategory;
    private Age      mAge;

    public LoadSubredditPostsEvent(String mSubreddit) {
        this.mSubreddit = mSubreddit;
    }

    public LoadSubredditPostsEvent(String mSubreddit, Category mCategory, Age mAge) {
        this.mSubreddit = mSubreddit;
        this.mCategory = mCategory;
        this.mAge = mAge;
    }

    public String getSubreddit() {
        return mSubreddit;
    }

    public Category getCategory() {
        return mCategory;
    }

    public Age getAge() {
        return mAge;
    }
}
