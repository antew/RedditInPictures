package com.antew.redditinpictures.library.reddit;

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