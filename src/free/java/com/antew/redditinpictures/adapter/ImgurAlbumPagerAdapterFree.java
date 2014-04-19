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
package com.antew.redditinpictures.adapter;

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.antew.redditinpictures.library.adapter.ImgurAlbumPagerAdapter;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.ui.ImgurAlbumFragmentFree;

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
