package com.antew.redditinpictures.library.event;

import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;

public class LoadSubredditEvent {
    private String   mSubreddit;
    private Age      mAge;
    private Category mCategory;

    public LoadSubredditEvent(String mSubreddit, Category mCategory, Age mAge) {
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
