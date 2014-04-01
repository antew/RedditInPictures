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

import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.widget.BaseAdapter;
import com.antew.redditinpictures.library.reddit.PostData;
import com.antew.redditinpictures.library.ui.ImageDetailFragment;
import java.util.List;

/**
 * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there
 * could be a large number of items in the ViewPager and we don't want to retain them all in
 * memory at once but create/destroy them on the fly.
 */
public class ImagePagerAdapter extends FixedFragmentStatePagerAdapter {
    protected List<PostData> mImages;

    public ImagePagerAdapter(FragmentManager fm, List<PostData> images) {
        super(fm);
        this.mImages = images;
    }

    @Override
    public int getCount() {
        if (mImages == null) {
            return 0;
        }

        return mImages.size();
    }

    /**
     * The PostData at the input position
     *
     * @param position
     *     The position
     *
     * @return PostData at the input position
     */
    public PostData getPost(int position) {
        if (mImages == null) {
            return null;
        } else if (position < 0 || position > mImages.size()) {
            throw new IndexOutOfBoundsException();
        }

        return mImages.get(position);
    }

    /**
     * Adds PostData objects to the Adapter, also calls {@link BaseAdapter#notifyDataSetChanged()}
     *
     * @param posts
     *     The posts to add
     */
    public void addPosts(List<PostData> posts) {
        if (mImages != null) {
            mImages.addAll(posts);
            notifyDataSetChanged();
        }
    }

    @Override
    public Fragment getItem(int position) {
        return getImageDetailFragment(mImages.get(position));
    }

    /**
     * Returns an {@link ImageDetailFragment} for the input {@link PostData} object
     *
     * @param p
     *     The {@link PostData} object to pass to the new {@link ImageDetailFragment}
     *
     * @return A new {@link ImageDetailFragment} for the input {@link PostData}
     */
    public Fragment getImageDetailFragment(PostData p) {
        return ImageDetailFragment.newInstance(p);
    }
}
