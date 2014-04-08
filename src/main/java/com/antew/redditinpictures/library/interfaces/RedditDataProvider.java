package com.antew.redditinpictures.library.interfaces;

import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;

public interface RedditDataProvider {
    public Age getAge();

    public void setAge(Age age);

    public Category getCategory();

    public void setCategory(Category category);

    public String getSubreddit();

    public void setSubreddit(String subreddit);
}
