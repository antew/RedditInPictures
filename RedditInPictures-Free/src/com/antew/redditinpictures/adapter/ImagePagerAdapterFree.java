package com.antew.redditinpictures.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.antew.redditinpictures.library.adapter.ImagePagerAdapter;
import com.antew.redditinpictures.library.reddit.RedditApi.PostData;
import com.antew.redditinpictures.library.ui.ImageDetailFragment;
import com.antew.redditinpictures.ui.ImageDetailFragmentFree;

/**
 * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there
 * could be a large number of items in the ViewPager and we don't want to retain them all in
 * memory at once but create/destroy them on the fly.
 */
public class ImagePagerAdapterFree extends ImagePagerAdapter {
    public static final String TAG = ImagePagerAdapterFree.class.getSimpleName();
    
    public ImagePagerAdapterFree(FragmentManager fm, List<PostData> images) {
        super(fm, images);
    }
    
    @Override
    public Fragment getImageDetailFragment(PostData p) {
        return ImageDetailFragmentFree.newInstance(p);
        
    }
}
