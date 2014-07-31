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
package com.antew.redditinpictures.ui;

import android.app.DialogFragment;
import android.content.Intent;
import android.net.Uri;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.view.MenuItem;
import com.antew.redditinpictures.adapter.CursorPagerAdapterFree;
import com.antew.redditinpictures.dialog.UpdateToFullVersionDialogFragment;
import com.antew.redditinpictures.dialog.UpdateToFullVersionDialogFragment.UpdateToFullVersionDialogListener;
import com.antew.redditinpictures.library.event.DownloadImageCompleteEvent;
import com.antew.redditinpictures.library.event.RequestCompletedEvent;
import com.antew.redditinpictures.library.event.RequestInProgressEvent;
import com.antew.redditinpictures.library.ui.ImageDetailActivity;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.util.ConstsFree;
import com.squareup.otto.Subscribe;

public class ImageDetailActivityFree extends ImageDetailActivity {
    public static final String TAG = ImageDetailActivityFree.class.getSimpleName();

    @Override
    public FragmentStatePagerAdapter getPagerAdapter() {
        return new CursorPagerAdapterFree(getFragmentManager(), null);
    }

    @Subscribe
    @Override
    public void onDownloadImageComplete(DownloadImageCompleteEvent event) {
        super.onDownloadImageComplete(event);
    }

    @Subscribe
    @Override
    public void requestInProgress(RequestInProgressEvent event) {
        super.requestInProgress(event);
    }

    @Subscribe
    @Override
    public void requestCompleted(RequestCompletedEvent event) {
        super.requestCompleted(event);
    }
}
