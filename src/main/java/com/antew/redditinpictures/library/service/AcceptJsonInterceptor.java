package com.antew.redditinpictures.library.service;

import retrofit.RequestInterceptor;

public class AcceptJsonInterceptor implements RequestInterceptor {
    @Override
    public void intercept(RequestFacade request) {
        request.addHeader("Accept", "application/json");
    }
}
