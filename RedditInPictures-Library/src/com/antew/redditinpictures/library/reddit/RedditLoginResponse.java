package com.antew.redditinpictures.library.reddit;

import java.util.List;

public class RedditLoginResponse {
    private LoginResponse json;

    public LoginResponse getLoginResponse() {
        return json;
    }

    public static class LoginResponse {
        private List<String[]> errors;
        private LoginData      data;

        public List<String[]> getErrors() {
            return errors;
        }

        public LoginData getData() {
            return data;
        }
    }

    public static class LoginData {
        private String modhash;
        private String cookie;

        public String getModhash() {
            return modhash;
        }

        public String getCookie() {
            return cookie;
        }
    }

}
