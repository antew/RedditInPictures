package com.antew.redditinpictures.library.ui.base;

import android.os.Bundle;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.android.debug.hv.ViewServer;
import com.antew.redditinpictures.Injector;
import com.antew.redditinpictures.library.Constants;
import com.antew.redditinpictures.library.RedditInPicturesApplication;
import com.antew.redditinpictures.library.interfaces.ActionBarTitleChanger;
import com.antew.redditinpictures.library.modules.ActivityModule;
import com.antew.redditinpictures.library.utils.Strings;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.BuildConfig;
import com.antew.redditinpictures.pro.R;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (BuildConfig.DEBUG) {
            ViewServer.get(this).addWindow(this);
            Util.enableStrictMode();
            Picasso.with(this).setDebugging(true);
        }

        RedditInPicturesApplication application = (RedditInPicturesApplication) getApplication();
        Injector.init(new ActivityModule(this), this);
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

    @Override public void setActionBarTitle(String title, String subtitle) {
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