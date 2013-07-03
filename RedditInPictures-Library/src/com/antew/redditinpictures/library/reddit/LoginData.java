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
}