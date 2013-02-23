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

import android.database.Cursor;
import android.support.v4.app.FixedFragmentStatePagerAdapter;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.antew.redditinpictures.library.reddit.PostData;
import com.antew.redditinpictures.library.ui.ImageDetailFragment;

/**
 * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there could
 * be a large number of items in the ViewPager and we don't want to retain them all in memory at
 * once but create/destroy them on the fly.
 */
public class CursorPagerAdapter extends FixedFragmentStatePagerAdapter {
    public static final String TAG = CursorPagerAdapter.class.getSimpleName();
    private Cursor             mCursor;

    public CursorPagerAdapter(FragmentManager fm, Cursor cursor) {
        super(fm);
        this.mCursor = cursor;
    }

    @Override
    public int getCount() {
        if (mCursor == null)
            return 0;

        return mCursor.getCount();
    }

    public void swapCursor(Cursor newCursor) {
        if (mCursor == newCursor)
            return;

        mCursor = newCursor;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        if (mCursor != null && mCursor.moveToPosition(position)) {
            return getImageDetailFragment(new PostData(mCursor));
        }

        return null;
    }

    /**
     * The PostData at the input position
     * 
     * @param position
     *            The position
     * @return PostData at the input position
     */
    public PostData getPost(int position) {
        if (mCursor != null && mCursor.moveToPosition(position)) {
            return new PostData(mCursor);
        }

        return null;
    }

    /**
     * Returns an {@link ImageDetailFragment} for the input {@link PostData} object
     * 
     * @param p
     *            The {@link PostData} object to pass to the new {@link ImageDetailFragment}
     * @return A new {@link ImageDetailFragment} for the input {@link PostData}
     */
    public Fragment getImageDetailFragment(PostData p) {
        return ImageDetailFragment.newInstance(p);
    }

}
