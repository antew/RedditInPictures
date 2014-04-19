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
package com.antew.redditinpictures.library.ui.base;

import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.android.debug.hv.ViewServer;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.Injector;
import com.antew.redditinpictures.library.interfaces.ActionBarTitleChanger;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.library.util.Strings;
import com.antew.redditinpictures.pro.BuildConfig;
import com.antew.redditinpictures.pro.R;
import com.google.analytics.tracking.android.EasyTracker;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import javax.inject.Inject;

/**
 * Base activity for an activity which does not use fragments.
 */
public abstract class BaseFragmentActivity extends SherlockFragmentActivity implements ActionBarTitleChanger {
    @Inject
    protected Bus mBus;

    @Override
    protected void onStart() {
        super.onStart();
        EasyTracker.getInstance(this).activityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EasyTracker.getInstance(this).activityStop(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            ViewServer.get(this).addWindow(this);
            AndroidUtil.enableStrictMode();
            Picasso.with(this).setDebugging(true);
        }

        Injector.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            ViewServer.get(this).setFocusedWindow(this);
        }
        mBus.register(this);
    }

    @Override
    protected void onPause() {
        mBus.unregister(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (BuildConfig.DEBUG) {
            ViewServer.get(this).removeWindow(this);
        }

        super.onDestroy();
    }

    @Override
    public void setActionBarTitle(String title, String subtitle) {
        ActionBar actionBar = getSupportActionBar();

        if (Strings.isEmpty(title)) {
            actionBar.setTitle(getString(R.string.app_name));
        } else if (title.equals(Constants.Reddit.REDDIT_FRONTPAGE)) {
            actionBar.setTitle(getString(R.string.frontpage));
            actionBar.setSubtitle(subtitle);
        } else {
            actionBar.setTitle(title);
            actionBar.setSubtitle(subtitle);
        }
    }
}