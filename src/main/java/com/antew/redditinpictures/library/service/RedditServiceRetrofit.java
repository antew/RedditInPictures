package com.antew.redditinpictures.library.service;

import com.antew.redditinpictures.library.model.reddit.RedditApi;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Path;
import rx.Observable;

public interface RedditServiceRetrofit {

    @GET("/{subreddit}/comments/{id}.json?limit=200")
    Observable<List<RedditApi>> getComments(@Path("subreddit") String subreddit, @Path("id") String postId);
}
