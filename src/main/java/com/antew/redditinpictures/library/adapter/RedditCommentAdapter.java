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
package com.antew.redditinpictures.library.adapter;

import android.content.Context;
import android.text.Html;
import android.text.Layout;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.antew.redditinpictures.library.Injector;
import com.antew.redditinpictures.library.model.reddit.Children;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.pro.R;
import com.commonsware.cwac.anddown.AndDown;

import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * This is used as the backing adapter for a {@link android.widget.GridView}
 *
 * @author Antew
 */
public class RedditCommentAdapter extends BaseAdapter {
    private List<Children> mComments;
    private final LayoutInflater mInflater;

    @Inject
    AndDown andDown;

    public RedditCommentAdapter(Context context, List<Children> comments) {
        mInflater = LayoutInflater.from(context);
        mComments = comments;

        Injector.inject(this);
    }

    @Override
    public int getCount() {
        return mComments.size();
    }

    @Override
    public Object getItem(int position) {
        return mComments.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ViewHolder holder = null;
        if (v == null) {
            v = mInflater.inflate(R.layout.reddit_comment, parent, false);
            holder = new ViewHolder(v);
            v.setTag(holder);
        } else {
            holder = (ViewHolder) v.getTag();
        }

        PostData post = mComments.get(position).getData();
        holder.username.setText(post.getAuthor());
        holder.votes.setText(Integer.toString(post.getScore()) + " points");
        holder.date.setText(
                DateUtils.getRelativeTimeSpanString(
                        (long) post.getCreated_utc() * 1000, // startTime
                        System.currentTimeMillis(),          // endTime
                        0L,                                  // minResolution
                        DateUtils.FORMAT_ABBREV_RELATIVE     // format
                )
        );
        holder.comment.setText(Html.fromHtml(andDown.markdownToHtml(post.getBody())));

        return v;
    }

    public void swap(List<Children> comments) {
        mComments.clear();
        mComments.addAll(comments);
        notifyDataSetChanged();
    }

    static class ViewHolder {
        @InjectView(R.id.tv_username)      TextView username;
        @InjectView(R.id.tv_date)          TextView date;
        @InjectView(R.id.tv_comment_votes) TextView votes;
        @InjectView(R.id.tv_comment)       TextView comment;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
