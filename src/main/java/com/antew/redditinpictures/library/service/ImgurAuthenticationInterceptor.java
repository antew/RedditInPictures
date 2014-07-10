package com.antew.redditinpictures.library.service;

import com.antew.redditinpictures.pro.BuildConfig;

import retrofit.RequestInterceptor;

public class ImgurAuthenticationInterceptor implements RequestInterceptor {

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Authorization", "Client-ID " + BuildConfig.IMGUR_CLIENT_ID);
    }
}
