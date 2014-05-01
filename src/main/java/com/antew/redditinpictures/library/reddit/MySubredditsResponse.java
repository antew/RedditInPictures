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
package com.antew.redditinpictures.library.reddit;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.database.RedditContract;
import com.antew.redditinpictures.library.json.JsonDeserializer;
import com.antew.redditinpictures.library.model.reddit.MySubreddits;
import com.antew.redditinpictures.library.model.reddit.SubredditChildren;
import com.antew.redditinpictures.library.model.reddit.SubredditData;
import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.Strings;
import java.util.ArrayList;
import java.util.List;

public class MySubredditsResponse extends RedditResponseHandler {
    private RedditResult result;

    public MySubredditsResponse(RedditResult result) {
        this.result = result;
    }

    @Override
    public void processHttpResponse(Context context) {
        ContentResolver resolver = context.getContentResolver();

        Ln.i("MySubreddits complete! = %s", result.getJson());
        MySubreddits mySubreddits = JsonDeserializer.deserialize(result.getJson(), MySubreddits.class);

        if (mySubreddits == null) {
            Ln.e("Something went wrong on mySubreddits! status = %d, json = %s", result.getHttpStatusCode(),
                 Strings.toString(result.getJson()));
            return;
        }

        String after = mySubreddits.getData().getAfter();

        if (Strings.notEmpty(after)) {
            RedditService.getMySubreddits(context, after);
        }

        DefaultSubreddit[] defaultSubreddits = DefaultSubreddit.values();
        int capacity = mySubreddits.getCount() + defaultSubreddits.length;
        List<ContentValues> operations = new ArrayList<ContentValues>(capacity);

        String where = null;
        List<String> subredditNames = new ArrayList<String>();
        // Add in the default subreddits ('Frontpage' and 'All')
        for (DefaultSubreddit subreddit : defaultSubreddits) {
            if (where == null) {
                where = RedditContract.SubredditColumns.DISPLAY_NAME + " in (?";
            } else {
                where += ",?";
            }
            subredditNames.add(subreddit.getDisplayName());
            operations.add(mySubreddits.getContentValues(new SubredditData(subreddit.getDisplayName(), subreddit.getPriority())));
        }

        // Add all of the subreddits so we can remove them and merge the records.
        for (SubredditChildren child : mySubreddits.getData().getChildren()) {
            if (child != null) {
                if (where == null) {
                    where = RedditContract.SubredditColumns.DISPLAY_NAME + " in (?";
                } else {
                    where += ",?";
                }
                subredditNames.add(child.getData().getDisplay_name());
            }
        }
        // Close the in statement.
        where += ")";

        // Get the subreddits in an array
        operations.addAll(mySubreddits.getContentValuesArray());

        // Remove all current rows, so we can merge the records.
        resolver.delete(RedditContract.Subreddits.CONTENT_URI, where, subredditNames.toArray(new String[] { }));
        int rowsInserted = resolver.bulkInsert(RedditContract.Subreddits.CONTENT_URI,
                                               operations.toArray(new ContentValues[operations.size()]));

        Ln.i("Inserted %d rows", rowsInserted);
    }

    public enum DefaultSubreddit {
        FRONTPAGE(Constants.Reddit.REDDIT_FRONTPAGE_DISPLAY_NAME, 99999),
        ALL(Constants.Reddit.REDDIT_ALL_DISPLAY_NAME, 99998);

        private final String displayName;
        private final int    priority;

        DefaultSubreddit(String displayName, int priority) {
            this.displayName = displayName;
            this.priority = priority;
        }

        public String getDisplayName() {
            return displayName;
        }

        public int getPriority() { return priority; }

    }
}
