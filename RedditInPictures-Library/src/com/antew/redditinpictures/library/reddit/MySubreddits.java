/*
 * Copyright (C) 2012 Antew | antewcode@gmail.com
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
package com.antew.redditinpictures.library.reddit;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;

import com.antew.redditinpictures.library.interfaces.ContentValuesArrayOperation;
import com.antew.redditinpictures.sqlite.RedditContract;

public class MySubreddits implements ContentValuesArrayOperation {
    String kind;
    Data   data;

    public String getKind() {
        return kind;
    }

    public Data getData() {
        return data;
    }

    public static class Data {
        String         modhash;
        List<Children> children;
        String         after;
        String         before;

        public void addChildren(List<Children> children) {
            if (this.children == null)
                this.children = new ArrayList<MySubreddits.Children>();
            
            this.children.addAll(children);
        }

        public String getModhash() {
            return modhash;
        }

        public List<Children> getChildren() {
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

    public static class Children {
        String        kind;
        SubredditData data;

        public String getKind() {
            return kind;
        }

        public SubredditData getData() {
            return data;
        }

    }

    public static class SubredditData {
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

    public ContentValues getContentValues(SubredditData data) {
        ContentValues values = new ContentValues();
        
        values.put(RedditContract.Subreddits.DISPLAY_NAME      , data.getDisplay_name() );
        values.put(RedditContract.Subreddits.HEADER_IMAGE      , data.getHeader_img());
        values.put(RedditContract.Subreddits.TITLE             , data.getTitle());
        values.put(RedditContract.Subreddits.URL               , data.getUrl());
        values.put(RedditContract.Subreddits.DESCRIPTION       , data.getDescription());
        values.put(RedditContract.Subreddits.CREATED           , data.getCreated());
        values.put(RedditContract.Subreddits.CREATED_UTC       , data.getCreated_utc());
        
        if (data.getHeader_size() != null)
            values.put(RedditContract.Subreddits.HEADER_SIZE       , data.getHeader_size()[0] + ", " + data.getHeader_size()[1]);
        
        values.put(RedditContract.Subreddits.OVER_18           , data.isOver18());
        values.put(RedditContract.Subreddits.SUBSCRIBERS       , data.getSubscribers());
        values.put(RedditContract.Subreddits.ACCOUNTS_ACTIVE   , data.getAccountsActive());
        values.put(RedditContract.Subreddits.PUBLIC_DESCRIPTION, data.getPublic_description());
        values.put(RedditContract.Subreddits.HEADER_TITLE      , data.getHeader_title());
        values.put(RedditContract.Subreddits.SUBREDDIT_ID      , data.getSubscribers());
        values.put(RedditContract.Subreddits.NAME              , data.getName());
        return values;
    }

    @Override
    public ContentValues[] getContentValuesArray() {
        ContentValues[] operations = new ContentValues[getData().getChildren().size()];
        
        List<Children> c = getData().getChildren();
        for (int i = 0; i < getData().getChildren().size(); i++) {
            operations[i] = getContentValues(c.get(i).getData());
        }
        
        return operations;
    }

}
