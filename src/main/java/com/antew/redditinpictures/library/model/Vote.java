package com.antew.redditinpictures.library.model;

public enum Vote {
    UP(1), DOWN(-1), NEUTRAL(0);

    private int vote;

    Vote(int vote) {
        this.vote = vote;
    }

    public int getVote() {
        return vote;
    }
}