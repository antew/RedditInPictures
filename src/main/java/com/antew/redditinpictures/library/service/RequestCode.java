/*
 * Copyright (C) 2014 Antew
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
package com.antew.redditinpictures.library.service;

public enum RequestCode {
    LOGIN(100), POSTS(110), LOGOUT(120), VOTE(130), MY_SUBREDDITS(140), SUBSCRIBE(150), ABOUT_SUBREDDIT(160), SEARCH_SUBREDDITS(170), REPORT_IMAGE(180);

    private int requestCode;

    RequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

    public int getRequestCode() {
        return requestCode;
    }
}