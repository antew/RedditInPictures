package com.antew.redditinpictures.library.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.library.ui.ImageDetailFragment;

/**
 * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there
 * could be a large number of items in the ViewPager and we don't want to retain them all in
 * memory at once but create/destroy them on the fly.
 */
public class ImagePagerAdapter extends FragmentStatePagerAdapter {
    protected List<PostData> mImages;
    
    public ImagePagerAdapter(FragmentManager fm, List<PostData> images) {
        super(fm);
        this.mImages = images; 
    }

    @Override
    public int getCount() {
        if (mImages == null)
            return 0;
        
        return mImages.size();
    }

    public PostData getPost(int position) {
        if (mImages == null)
            return null;
        else if (position < 0 || position > mImages.size())
            throw new IndexOutOfBoundsException();

        return mImages.get(position);
    }
    
    @Override
    public Fragment getItem(int position) {
        return getImageDetailFragment(mImages.get(position));
    }
    
    public Fragment getImageDetailFragment(PostData p) {
        return ImageDetailFragment.newInstance(p);
        
    }
    
}
