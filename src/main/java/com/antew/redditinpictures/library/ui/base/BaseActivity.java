package com.antew.redditinpictures.library.ui.base;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockActivity;
import com.android.debug.hv.ViewServer;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.BuildConfig;
import com.squareup.picasso.Picasso;

/**
 * Base activity for an activity which does not use fragments.
 */
public abstract class BaseActivity extends SherlockActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            ViewServer.get(this).addWindow(this);
            Util.enableStrictMode();
            Picasso.with(this).setDebugging(true);
        }
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