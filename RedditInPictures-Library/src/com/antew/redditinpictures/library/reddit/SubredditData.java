package com.antew.redditinpictures.library.reddit;

public class SubredditData {
    String  display_name;
    String  description;
    String  name;
    long    created;
    String  url;
    String  title;
    long    created_utc;
    String  public_description;
    int[]   header_size;
    int     accounts_active;
    boolean over18;
    int     subscribers;
    String  header_title;
    String  id;
    String  header_img;

    public String getDisplay_name() {
        return display_name;
    }

    public String getDescription() {
        return description;
    }

    public String getName() {
        return name;
    }

    public long getCreated() {
        return created;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public long getCreated_utc() {
        return created_utc;
    }

    public String getPublic_description() {
        return public_description;
    }

    public int[] getHeader_size() {
        return header_size;
    }

    public boolean isOver18() {
        return over18;
    }

    public int getSubscribers() {
        return subscribers;
    }

    public String getHeader_title() {
        return header_title;
    }

    public String getId() {
        return id;
    }

    public String getHeader_img() {
        return header_img;
    }
    
    public int getAccountsActive() {
        return accounts_active;
    }

}