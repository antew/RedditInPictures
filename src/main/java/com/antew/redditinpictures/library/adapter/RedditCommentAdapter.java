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
import com.antew.redditinpictures.library.model.reddit.Comment;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.library.util.Ln;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.pro.R;
import com.commonsware.cwac.anddown.AndDown;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * When we fetch more comments from Reddit the
     * only indicator of their depth is by the 'parent_id'.
     * This version of notifyDataSetChanged() figures out
     * the correct depth for each item based on its parent.
     *
     */
    @Override
    public void notifyDataSetChanged() {
        calculateDepthForAllChilden();
        super.notifyDataSetChanged();
    }

    /**
     * On Reddit child comments are linked to their parent through a 'parent_id',
     * this method loops through all posts and sets each child's depth
     * to its parent's depth + 1.  When displaying the comments we use
     * the depth to set a left margin on the ListView row and give the appearance
     * of threading.
     *
     * This work is done in the Adapter rather than at the caller because when
     * you use Reddit's "load more comments (X replies)" links they aren't nested
     * like the regular post JSON response, it's just a flat list, so it is necessary
     * to calculate the depth for them.
     *
     */
    private void calculateDepthForAllChilden() {
        Map<String, Integer> depthMap = new HashMap<>();
        for (Child c : mPostData) {
            String parent = c.getParent();
            if (depthMap.containsKey(parent)) {
                c.setDepth(depthMap.get(parent) + 1);
            }

            depthMap.put(c.getName(), c.getDepth());
        }
    }

    public Integer getNextTopLevelComment(int firstVisiblePosition) {
        if (firstVisiblePosition >= 0 && mPostData != null && mPostData.size() > firstVisiblePosition) {
            for (int i = firstVisiblePosition + 1; i < mPostData.size(); i++) {
                if (mPostData.get(i).getDepth() == 0) {
                    return i;
                }
            }
        }

        // no further top-level comments
        return null;
    }

    public Integer getPreviousTopLevelComment(int firstVisiblePosition) {
        if (firstVisiblePosition > 0 && mPostData != null && mPostData.size() > firstVisiblePosition) {
            for (int i = firstVisiblePosition - 1; i >= 0; i--) {
                if (mPostData.get(i).getDepth() == 0) {
                    return i;
                }
            }
        }

        // no further top-level comments
        return null;
    }

    public void replaceAtPosition(int position, List<Child> things) {
        int currentDepth = mPostData.get(position).getDepth();
        for (Child c : things) {
            c.setDepth(c.getDepth() + currentDepth);
        }

        mPostData.remove(position);
        mPostData.addAll(position, things);
        notifyDataSetChanged();
    }
}
