package com.antew.redditinpictures.library.ui.base;

import android.os.Bundle;
import butterknife.ButterKnife;
import com.actionbarsherlock.app.SherlockActivity;
import com.android.debug.hv.ViewServer;
import com.antew.redditinpictures.library.util.AndroidUtil;
import com.antew.redditinpictures.pro.BuildConfig;
import com.google.analytics.tracking.android.EasyTracker;
import com.squareup.picasso.Picasso;

/**
 * Base activity for an activity which does not use fragments.
 */
public abstract class BaseActivity extends SherlockActivity {

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