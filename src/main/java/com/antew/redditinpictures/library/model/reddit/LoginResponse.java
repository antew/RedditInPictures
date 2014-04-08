package com.antew.redditinpictures.library.model.reddit;

import java.util.List;

public class LoginResponse {
    private List<String[]> errors;
    private LoginData      data;

    public List<String[]> getErrors() {
        return errors;
    }

    public void setErrors(List<String[]> errors) {
        this.errors = errors;
    }

    public LoginData getData() {
        return data;
    }

    public void setData(LoginData data) {
        this.data = data;
    }
}