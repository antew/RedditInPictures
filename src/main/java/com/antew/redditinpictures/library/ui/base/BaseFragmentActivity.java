package com.antew.redditinpictures.library.ui.base;

import android.os.Bundle;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.android.debug.hv.ViewServer;
import com.antew.redditinpictures.library.Injector;
import com.antew.redditinpictures.pro.BuildConfig;
import com.squareup.otto.Bus;;import javax.inject.Inject;

/**
 * Base activity for an activity which does not use fragments.
 */
public abstract class BaseFragmentActivity extends SherlockFragmentActivity {
    @Inject
    protected Bus mBus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            ViewServer.get(this).addWindow(this);
        }

        Injector.inject(this);
        mBus.register(this);
    }

    @Override
    protected void onPause() {
        mBus.unregister(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) {
            ViewServer.get(this).removeWindow(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            ViewServer.get(this).setFocusedWindow(this);
        }
    }
}