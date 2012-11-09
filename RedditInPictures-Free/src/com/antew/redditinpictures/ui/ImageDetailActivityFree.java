/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.antew.redditinpictures.ui;

import android.support.v4.app.FragmentStatePagerAdapter;

import com.actionbarsherlock.view.Menu;
import com.antew.redditinpictures.adapter.ImagePagerAdapterFree;
import com.antew.redditinpictures.library.R;
import com.antew.redditinpictures.library.ui.ImageDetailActivity;

public class ImageDetailActivityFree extends ImageDetailActivity {
    public static final String TAG = ImageDetailActivityFree.class.getSimpleName();

    @Override
    public FragmentStatePagerAdapter getPagerAdapter() {
        return new ImagePagerAdapterFree(getSupportFragmentManager(), getImages());
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.removeItem(R.id.upvote);
        menu.removeItem(R.id.downvote);
        menu.removeItem(R.id.save_post);
        
        return true;
    }
}
