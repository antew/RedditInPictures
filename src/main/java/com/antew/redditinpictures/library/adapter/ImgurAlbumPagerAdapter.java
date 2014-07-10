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

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.antew.redditinpictures.library.image.Image;
import com.antew.redditinpictures.library.imgur.ImgurImageApi;
import com.antew.redditinpictures.library.imgur.ImgurImageApi.ImgurImage;
import com.antew.redditinpictures.library.ui.ImgurAlbumFragment;
import java.util.List;

/**
 * The main adapter that backs the ViewPager. A subclass of FragmentStatePagerAdapter as there
 * could be a large number of items in the ViewPager and we don't want to retain them all in
 * memory at once but create/destroy them on the fly.
 */
public class ImgurAlbumPagerAdapter extends FragmentStatePagerAdapter {
    private List<ImgurImageApi.Image> mImages;

    public ImgurAlbumPagerAdapter(FragmentManager fm, List<ImgurImageApi.Image> images) {
        super(fm);
        mImages = images;
    }

    @Override
    public int getCount() {
        if (mImages == null) {
            return 0;
        }

        return mImages.size();
    }

    /**
     * The {@link ImgurImage} for the input position
     *
     * @param position
     *     The position
     *
     * @return The {@link ImgurImage} at the input position
     */
    public ImgurImageApi.Image getImage(int position) {
        if (mImages == null) {
            return null;
        } else if (position < 0 || position > mImages.size()) {
            throw new IndexOutOfBoundsException();
        }

        return mImages.get(position);
    }

    @Override
    public Fragment getItem(int position) {
        return getImgurAlbumFragment(mImages.get(position));
    }

    /**
     * A new {@link ImgurAlbumFragment} for the input {@link ImgurImage}
     *
     * @param i
     *     The {@link ImgurImage}
     *
     * @return A new {@link ImgurAlbumFragment} for the input {@link ImgurImage}
     */
    public Fragment getImgurAlbumFragment(ImgurImageApi.Image i) {
        return ImgurAlbumFragment.newInstance(i);
    }
}
