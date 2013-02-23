package com.antew.redditinpictures.library.interfaces;

import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;

public interface RedditDataProvider {
    public Age getAge();

    public Category getCategory();

    public String getSubreddit();
}
