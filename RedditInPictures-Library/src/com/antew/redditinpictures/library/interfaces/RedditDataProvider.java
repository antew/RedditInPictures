package com.antew.redditinpictures.library.interfaces;

import com.antew.redditinpictures.library.reddit.RedditUrl.Age;
import com.antew.redditinpictures.library.reddit.RedditUrl.Category;

public interface RedditDataProvider {
    public Age getAge();
    public Category getCategory();
    public String getSubreddit();
}
