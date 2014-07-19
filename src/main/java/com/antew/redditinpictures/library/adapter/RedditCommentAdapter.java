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
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.antew.redditinpictures.library.Injector;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.Strings;
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
    private List<PostData> mPostData;
    private final LayoutInflater mInflater;

    @Inject
    AndDown andDown;

    public RedditCommentAdapter(Context context, List<PostData> postData) {
        mInflater = LayoutInflater.from(context);
        this.mPostData = postData;

        Injector.inject(this);
    }

    @Override
    public int getCount() {
        return mPostData.size();
    }

    @Override
    public Object getItem(int position) {
        return mPostData.get(position);
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

        PostData post = mPostData.get(position);
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
        if (Strings.notEmpty(post.getBody())) {
            holder.comment.setText(Strings.trimTrailingWhitespace(Html.fromHtml(andDown.markdownToHtml(post.getBody()))));
        }

        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.commentWrapper.getLayoutParams();
        params.setMargins(50 * post.depth, params.topMargin, params.rightMargin, params.bottomMargin);

        return v;
    }

    public void addAll(List<PostData> postData) {
        this.mPostData.addAll(postData);
        Ln.i("After adding posts, adapter size = " + mPostData.size());
        notifyDataSetChanged();
    }

    public void swap(List<PostData> postData) {
        this.mPostData.clear();
        addAll(postData);
    }

    static class ViewHolder {
        @InjectView(R.id.tv_username)        TextView  username;
        @InjectView(R.id.tv_date)            TextView  date;
        @InjectView(R.id.tv_comment_votes)   TextView  votes;
        @InjectView(R.id.tv_comment)         TextView  comment;
        @InjectView(R.id.rl_comment_wrapper)
        RelativeLayout commentWrapper;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
