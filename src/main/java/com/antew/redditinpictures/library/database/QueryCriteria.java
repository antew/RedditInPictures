package com.antew.redditinpictures.library.database;

public class QueryCriteria {

    private final String   sort;
    private final String[] projection;

    public QueryCriteria(String[] projection, String sort) {
        this.projection = projection;
        this.sort = sort;
    }

    public String[] getProjection() {
        return projection;
    }

    public String getSort() {
        return sort;
    }
}
