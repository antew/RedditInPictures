package com.antew.redditinpictures.library.model;

public enum SubscribeAction {
    SUBSCRIBE("sub"), UNSUBSCRIBE("unsub");

    private String action;

    SubscribeAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

}