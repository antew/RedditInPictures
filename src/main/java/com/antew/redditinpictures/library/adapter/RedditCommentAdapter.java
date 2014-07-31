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
import com.antew.redditinpictures.library.interfaces.Item;
import com.antew.redditinpictures.library.model.reddit.Child;
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
    private List<Child> mPostData;
    private final LayoutInflater mInflater;


    public RedditCommentAdapter(Context context, List<Child> postData) {
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
    public int getViewTypeCount() {
        return Child.Type.values().length;
    }

    @Override
    public int getItemViewType(int position) {
        return mPostData.get(position).getType().ordinal();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return mPostData.get(position).getView(mInflater, convertView);
    }

    public void addAll(List<Child> postData) {
        this.mPostData.addAll(postData);
        Ln.i("After adding posts, adapter size = " + mPostData.size());
        notifyDataSetChanged();
    }

    public void swap(List<Child> postData) {
        this.mPostData.clear();
        addAll(postData);
    }

}
