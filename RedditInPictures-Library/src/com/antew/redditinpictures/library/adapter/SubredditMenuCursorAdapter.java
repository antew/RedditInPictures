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
import com.antew.redditinpictures.library.reddit.RedditUrl;
import com.antew.redditinpictures.library.ui.ImageGridActivity;
import com.antew.redditinpictures.sqlite.RedditContract;

/**
 * This is the Adapter for the drop down in the Action Bar of {@link ImageGridActivity}. It displays
 * the current Subreddit along with the selected Category and Age
 * 
 * @author Antew
 * 
 */
public class SubredditMenuCursorAdapter extends CursorAdapter {

    private static final String TAG = SubredditMenuCursorAdapter.class.getSimpleName();
    private LayoutInflater      inflater;
    private Age                 age;
    private Category            category;

    /**
     * Create a new Adapter for the Subreddit/Category/Age combo
     * 
     * @param context
     *            The context
     * @param listdata
     *            The list of subreddits
     * @param age
     *            The {@link RedditUrl#Age}
     * @param category
     *            The {@link RedditUrl#category}
     */
    public SubredditMenuCursorAdapter(Context context, Cursor cursor, Age age, Category category) {
        super(context, cursor, 0);
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.age = age;
        this.category = category;
    }

    /**
     * The main View contains the Subreddit name and the Category/Age combination below
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        RootItemViewHolder holder;
        if (v == null) {
            v = inflater.inflate(R.layout.spinner_row, null);
            holder = new RootItemViewHolder();
            holder.subreddit = (TextView) v.findViewById(R.id.subreddit);
            holder.category = (TextView) v.findViewById(R.id.category);
            v.setTag(holder);
        } else {
            holder = (RootItemViewHolder) v.getTag();
        }

        mCursor.moveToPosition(position);
        String subredditDisplayName = mCursor.getString(mCursor.getColumnIndex(RedditContract.SubredditColumns.DISPLAY_NAME));
        
        holder.subreddit.setText(subredditDisplayName);
        holder.category.setText(RedditUrl.getCategorySimpleName(category, age));

        return v;
    }

    /**
     * The dropdown view simply contains the Subreddit name
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ItemViewHolder holder;
        if (v == null) {
            v = inflater.inflate(R.layout.spinner_dropdown_row, parent, false);
            holder = new ItemViewHolder();
            holder.subreddit = (TextView) v.findViewById(R.id.subreddit);
            v.setTag(holder);
        } else {
            holder = (ItemViewHolder) v.getTag();
        }

        mCursor.moveToPosition(position);
        String subredditDisplayName = getSubredditDisplayName(mCursor); 
        holder.subreddit.setText(subredditDisplayName);

        return v;
    }

    @Override
    public Object getItem(int position) {
        mCursor.moveToPosition(position);
        return getSubredditDisplayName(mCursor);
    }

    private static String getSubredditDisplayName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(RedditContract.SubredditColumns.DISPLAY_NAME));
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Refreshes the Category/Age in the Adapter
     * 
     * @param category
     *            The {@link RedditUrl#category}
     * @param age
     *            The {@link RedditUrl#age}
     */
    public void notifyDataSetChanged(Category category, Age age) {
        this.category = category;
        this.age = age;
        notifyDataSetChanged();
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    public static class RootItemViewHolder {
        public TextView subreddit;
        public TextView category;
    }

    public static class ItemViewHolder {
        public TextView subreddit;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (view.getTag() instanceof ItemViewHolder) {
            Log.i(TAG, "Got an ItemViewHolder");
        } else if (view.getTag() instanceof RootItemViewHolder) {
            Log.i(TAG, "Got a RootItemViewHolder");
        }

    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        Log.i(TAG, "newView cursorPosition = " + cursor.getPosition());
        return inflater.inflate(R.layout.spinner_dropdown_row, parent, false);
    }

}