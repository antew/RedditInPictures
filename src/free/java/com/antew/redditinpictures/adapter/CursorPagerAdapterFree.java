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

import android.app.Fragment;
import android.app.FragmentManager;
import android.database.Cursor;
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
