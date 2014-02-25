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
package com.antew.redditinpictures.library.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.enums.Age;
import com.antew.redditinpictures.library.enums.Category;
import com.antew.redditinpictures.library.logging.Log;
import com.antew.redditinpictures.sqlite.RedditContract;

/**
 * This is the Adapter for the drop down in the Action Bar of {@link com.antew.redditinpictures.library.ui.ImageGridActivity}. It displays
 * the current Subreddit along with the selected Category and Age
 *
 * @author Antew
 *
 */
public class SubredditMenuDrawerCursorAdapter extends CursorAdapter {

    private static final String TAG = SubredditMenuDrawerCursorAdapter.class.getSimpleName();
    private LayoutInflater inflater;
    private Age            age;
    private Category       category;
    private int mActivePosition = -1;

    /**
     * Create a new Adapter for the Subreddit/Category/Age combo
     *
     * @param context
     *            The context
     * @param cursor
     *            Cursor containing the list of subreddits
     * @param age
     *            The {@link com.antew.redditinpictures.library.enums.Age}
     * @param category
     *            The {@link com.antew.redditinpictures.library.reddit.RedditUrl#category}
     */
    public SubredditMenuDrawerCursorAdapter(Context context, Cursor cursor, Age age, Category category) {
        super(context, cursor, 0);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.age = age;
        this.category = category;
        this.mCursor = cursor;
    }

    private static String getSubredditDisplayName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(RedditContract.SubredditColumns.DISPLAY_NAME));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public String getSubreddit(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor != null)
            return getSubredditDisplayName(cursor);

        return null;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView subreddit = (TextView) view.findViewById(R.id.subreddit);
        String subredditDisplayName = getSubredditDisplayName(cursor);
        subreddit.setText(subredditDisplayName);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.i(TAG, "newView cursorPosition = " + cursor.getPosition());
        return inflater.inflate(R.layout.subredditdrawer_item, parent, false);
    }

    public void setActivePosition(int activePosition) {
        this.mActivePosition = activePosition;
    }

}