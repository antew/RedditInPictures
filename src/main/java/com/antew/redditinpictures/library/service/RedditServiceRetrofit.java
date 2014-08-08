package com.antew.redditinpictures.library.service;

import com.antew.redditinpictures.library.model.reddit.Child;
import com.antew.redditinpictures.library.model.reddit.Comment;
import com.antew.redditinpictures.library.model.reddit.MoreComments;
import com.antew.redditinpictures.library.model.reddit.RedditApi;
import com.antew.redditinpictures.library.model.reddit.RedditUrl;

import java.util.List;

import retrofit.client.Response;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Streaming;
import rx.Observable;

public interface RedditServiceRetrofit {

    @GET("/r/{subreddit}/comments/{id}.json?limit=200")
    Observable<List<RedditApi>> getComments(@Path("subreddit") String subreddit, @Path("id") String postId);

    /**
     * Fetch comments hidden under a 'load more comments (X replies)' link
     *
     * You may think that removing the '.json' at the end of the URL would give the same result, since we
     * include the header: 'Accept: application/json', but you would be sorely mistaken!  Don't do it!!
     *
     * @param apiType The string 'JSON" (need to refactor this out...)
     * @param children The ids of child comments to fetch, must be a comma separated string
     * @param id The 'name' field of the comment that has hidden child comments
     * @param linkId The 'name' field of the post that the comments belong to
     * @return
     */
    @POST("/api/morechildren.json")
    @FormUrlEncoded
    Observable<MoreComments> getMoreComments(@Field("api_type") String apiType, @Field("children") String children, @Field("id") String id, @Field("link_id") String linkId);

}
