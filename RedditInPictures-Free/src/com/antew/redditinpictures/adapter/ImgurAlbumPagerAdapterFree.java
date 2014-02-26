package com.antew.redditinpictures.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.antew.redditinpictures.library.adapter.ImgurAlbumPagerAdapter;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.ui.ImgurAlbumFragmentFree;

import java.util.List;

/**
 * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there
 * could be a large number of items in the ViewPager and we don't want to retain them all in
 * memory at once but create/destroy them on the fly.
 */
public class ImgurAlbumPagerAdapterFree extends ImgurAlbumPagerAdapter {
    
    public ImgurAlbumPagerAdapterFree(FragmentManager fm, List<ImgurImage> images) {
        super(fm, images);
    }

    @Override
    public Fragment getImgurAlbumFragment(ImgurImage i) {
        return ImgurAlbumFragmentFree.newInstance(i);
    }
}
