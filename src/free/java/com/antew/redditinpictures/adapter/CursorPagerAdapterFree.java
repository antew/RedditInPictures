package com.antew.redditinpictures.adapter;

import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import com.antew.redditinpictures.library.adapter.CursorPagerAdapter;
import com.antew.redditinpictures.library.model.reddit.PostData;
import com.antew.redditinpictures.ui.ImageDetailFragmentFree;

public class CursorPagerAdapterFree extends CursorPagerAdapter {

    public CursorPagerAdapterFree(FragmentManager fm, Cursor cursor) {
        super(fm, cursor);
    }

    /**
     * Returns an {@link com.antew.redditinpictures.library.ui.ImageDetailFragment} for the input {@link com.antew.redditinpictures.library.model.reddit.PostData} object
     *
     * @param p
     *            The {@link com.antew.redditinpictures.library.model.reddit.PostData} object to pass to the new {@link com.antew.redditinpictures.ui.ImageDetailFragmentFree}
     * @return A new {@link com.antew.redditinpictures.ui.ImageDetailFragmentFree} for the input {@link com.antew.redditinpictures.library.model.reddit.PostData}
     */
    public Fragment getImageDetailFragment(PostData p) {
        return ImageDetailFragmentFree.newInstance(p);
    }
}
