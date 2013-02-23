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

import java.util.List;

import android.content.ContentValues;

import com.antew.redditinpictures.library.interfaces.ContentValuesArrayOperation;
import com.antew.redditinpictures.sqlite.RedditContract;

public class MySubreddits implements ContentValuesArrayOperation {
    String           kind;
    MySubredditsData data;

    public String getKind() {
        return kind;
    }

    public MySubredditsData getData() {
        return data;
    }

    public ContentValues getContentValues(SubredditData data) {
        ContentValues values = new ContentValues();

        //@formatter:off
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
        //@formatter:on

        return values;
    }

    @Override
    public ContentValues[] getContentValuesArray() {
        ContentValues[] operations = new ContentValues[getData().getChildren().size()];

        List<SubredditChildren> c = getData().getChildren();
        for (int i = 0; i < getData().getChildren().size(); i++) {
            operations[i] = getContentValues(c.get(i).getData());
        }

        return operations;
    }

}
