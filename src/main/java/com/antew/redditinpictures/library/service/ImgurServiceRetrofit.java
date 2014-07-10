package com.antew.redditinpictures.library.service;

import com.antew.redditinpictures.library.imgur.ImgurAlbumApi;
import com.antew.redditinpictures.library.imgur.ImgurImageApi;

import retrofit.http.GET;
import retrofit.http.Path;

public interface ImgurServiceRetrofit {

    @GET("/image/{id}")
    ImgurImageApi.ImgurImage getImage(@Path("id") String imageId);

    @GET("/album/{id}")
    ImgurAlbumApi getAlbum(@Path("id") String imageId);

}
