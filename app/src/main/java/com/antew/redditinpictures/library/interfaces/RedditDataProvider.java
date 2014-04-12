package com.antew.redditinpictures.library.interfaces;

import com.antew.redditinpictures.library.model.Age;
import com.antew.redditinpictures.library.model.Category;

public interface RedditDataProvider {
    public Age getAge();

    public void setAge(Age age);

    public Category getCategory();

    public void setCategory(Category category);

    public String getSubreddit();

    public void setSubreddit(String subreddit);
}
