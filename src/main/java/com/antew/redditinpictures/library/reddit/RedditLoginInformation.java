/*
 * Copyright (C) 2012 Antew | antewcode@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.antew.redditinpictures.library.reddit;

/**
 * The expectation is that this class will only be used to store data for successful logins
 *
 * @author Antew
 */
public class RedditLoginInformation {
    public static final String TAG = RedditLoginInformation.class.getSimpleName();
    private static LoginData mLoginData;

    private RedditLoginInformation() {}

    ;

    public static LoginData getLoginData() {
        return mLoginData;
    }

    public static void setLoginData(LoginData loginData) {
        mLoginData = loginData;
    }

    public static boolean isLoggedIn() {
        return mLoginData != null && mLoginData.isLoggedIn();
    }

    public static String getUsername() {
        return mLoginData == null ? null : mLoginData.getUsername();
    }

    public static String getModhash() {
        return mLoginData == null ? null : mLoginData.getModhash();
    }

    public static String getCookie() {
        return mLoginData == null ? null : mLoginData.getCookie();
    }
}
