package com.antew.redditinpictures.library.interfaces;

import android.content.Intent;

import com.antew.redditinpictures.library.reddit.MySubreddits;
import com.antew.redditinpictures.library.reddit.RedditApi;
import com.antew.redditinpictures.library.reddit.RedditLoginResponse;

public interface RedditUpdateProvider {
    public void addNewPosts(RedditApi api);
    public void setRequestInProgress(boolean inProgress);
    public void onError(int errorCode);
    public void createService(Intent intent);
    public void loginComplete(RedditLoginResponse response);
    public void mySubreddits(MySubreddits mySubreddits);
}
