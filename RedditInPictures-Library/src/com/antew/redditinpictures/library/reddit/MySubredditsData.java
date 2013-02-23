package com.antew.redditinpictures.library.reddit;

import java.util.ArrayList;
import java.util.List;

public class MySubredditsData {
    private String                  modhash;
    private List<SubredditChildren> children;
    private String                  after;
    private String                  before;

    public void addChildren(List<SubredditChildren> children) {
        if (this.children == null)
            this.children = new ArrayList<SubredditChildren>();

        this.children.addAll(children);
    }

    public String getModhash() {
        return modhash;
    }

    public List<SubredditChildren> getChildren() {
        return children;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public void setBefore(String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public String getBefore() {
        return before;
    }

}