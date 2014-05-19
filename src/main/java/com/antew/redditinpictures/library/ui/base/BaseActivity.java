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

import android.app.Activity;
import android.os.Bundle;
import butterknife.ButterKnife;
import com.android.debug.hv.ViewServer;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.pro.BuildConfig;
import com.google.analytics.tracking.android.EasyTracker;
import com.squareup.picasso.Picasso;

/**
 * Base activity for an activity which does not use fragments.
 */
public abstract class BaseActivity extends Activity {

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            ViewServer.get(this).setFocusedWindow(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {
            ViewServer.get(this).removeWindow(this);
        }
    }

    @Override public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);
        ButterKnife.inject(this);
    }
}