/*
 * Copyright (C) 2014 Antew
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
package com.antew.redditinpictures.library.model.reddit;

import android.content.ContentValues;
import com.antew.redditinpictures.library.database.RedditContract;

public class About {
    private String        kind;
    private SubredditData data;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public SubredditData getData() {
        return data;
    }

    public void setData(SubredditData data) {
        this.data = data;
    }

    public ContentValues getContentValues() {
        ContentValues values = new ContentValues();

        values.put(RedditContract.Subreddits.DISPLAY_NAME, data.getDisplay_name());
        values.put(RedditContract.Subreddits.HEADER_IMAGE, data.getHeader_img());
        values.put(RedditContract.Subreddits.TITLE, data.getTitle());
        values.put(RedditContract.Subreddits.URL, data.getUrl());
        values.put(RedditContract.Subreddits.DESCRIPTION, data.getDescription());
        values.put(RedditContract.Subreddits.CREATED, data.getCreated());
        values.put(RedditContract.Subreddits.CREATED_UTC, data.getCreated_utc());

        if (data.getHeader_size() != null) {
            values.put(RedditContract.Subreddits.HEADER_SIZE, data.getHeader_size()[0] + ", " + data.getHeader_size()[1]);
        }

        values.put(RedditContract.Subreddits.OVER_18, data.isOver18());
        values.put(RedditContract.Subreddits.SUBSCRIBERS, data.getSubscribers());
        values.put(RedditContract.Subreddits.ACCOUNTS_ACTIVE, data.getAccountsActive());
        values.put(RedditContract.Subreddits.PUBLIC_DESCRIPTION, data.getPublic_description());
        values.put(RedditContract.Subreddits.HEADER_TITLE, data.getHeader_title());
        values.put(RedditContract.Subreddits.SUBREDDIT_ID, data.getSubscribers());
        values.put(RedditContract.Subreddits.NAME, data.getName());

        return values;
    }
}
