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

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.reddit.RedditUrl;

public class SubredditMenuAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<String>   data;
    private RedditUrl.Age age;
    private RedditUrl.Category category;

    public SubredditMenuAdapter(Context context, List<String> listdata, RedditUrl.Age age, RedditUrl.Category category) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.age = age;
        this.category = category;
        data = listdata;
    }

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

        holder.subreddit.setText(data.get(position));
        holder.category.setText(RedditUrl.getCategorySimpleName(category, age));

        return v;
    }

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

        holder.subreddit.setText(data.get(position));

        return v;
    }
    
    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    public void notifyDataSetChanged(RedditUrl.Category category, RedditUrl.Age age) {
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


}