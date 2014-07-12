package com.antew.redditinpictures.library.service;

import com.antew.redditinpictures.pro.BuildConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit.RequestInterceptor;

@Singleton
public class ImgurAuthenticationInterceptor implements RequestInterceptor {
    private static final String AUTHORIZATION_PREFIX = "Client-ID";

    @Inject
    public ImgurAuthenticationInterceptor() {

    }

    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Authorization", "Client-ID " + BuildConfig.IMGUR_CLIENT_ID);
    }
}
