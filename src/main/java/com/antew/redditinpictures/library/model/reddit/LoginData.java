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
package com.antew.redditinpictures.library.model.reddit;

import android.text.TextUtils;

public class LoginData {
    private String username;
    private String modhash;
    private String cookie;

    public LoginData(String username, String modhash, String cookie) {
        this.username = username;
        this.modhash = modhash;
        this.cookie = cookie;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getModhash() {
        return modhash;
    }

    public String getCookie() {
        return cookie;
    }

    public boolean isLoggedIn() {
        return !TextUtils.isEmpty(username) && !TextUtils.isEmpty(modhash) && !TextUtils.isEmpty(cookie);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LoginData loginData = (LoginData) o;

        if (cookie != null ? !cookie.equals(loginData.cookie) : loginData.cookie != null) {
            return false;
        }
        if (modhash != null ? !modhash.equals(loginData.modhash) : loginData.modhash != null) {
            return false;
        }
        if (username != null ? !username.equals(loginData.username) : loginData.username != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = username != null ? username.hashCode() : 0;
        result = 31 * result + (modhash != null ? modhash.hashCode() : 0);
        result = 31 * result + (cookie != null ? cookie.hashCode() : 0);
        return result;
    }
}