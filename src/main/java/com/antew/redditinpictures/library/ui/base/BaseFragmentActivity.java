package com.antew.redditinpictures.library.ui.base;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.android.debug.hv.ViewServer;
import com.antew.redditinpictures.library.RedditInPicturesApplication;
import com.antew.redditinpictures.library.interfaces.ActionBarTitleChanger;
import com.antew.redditinpictures.library.modules.ActivityModule;
import com.antew.redditinpictures.library.utils.Constants;
import com.antew.redditinpictures.library.utils.Strings;
import com.antew.redditinpictures.library.utils.Util;
import com.antew.redditinpictures.pro.BuildConfig;
import com.antew.redditinpictures.pro.R;
import com.squareup.otto.Bus;
import com.squareup.picasso.Picasso;
import dagger.ObjectGraph;
import java.util.Arrays;
import java.util.List;
import javax.inject.Inject;

/**
 * Base activity for an activity which does not use fragments.
 */
public abstract class BaseFragmentActivity extends SherlockFragmentActivity implements
    ActionBarTitleChanger {
    private ObjectGraph activityGraph;

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
        activityGraph = application.getApplicationGraph().plus(getModules().toArray());

        // Inject ourselves so subclasses will have dependencies fulfilled when this method returns.
        activityGraph.inject(this);
    }

    @Override
    protected void onPause() {
        mBus.unregister(this);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        activityGraph = null;

        if (BuildConfig.DEBUG) {
            ViewServer.get(this).removeWindow(this);
        }

        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (BuildConfig.DEBUG) {
            ViewServer.get(this).setFocusedWindow(this);
        }
        mBus.register(this);
    }

    /**
     * A list of modules to use for the individual activity graph. Subclasses can override this
     * method to provide additional modules provided they call and include the modules returned by
     * calling {@code super.getModules()}.
     */
    protected List<Object> getModules() {
        return Arrays.<Object>asList(new ActivityModule(this));
    }

    /** Inject the supplied {@code object} using the activity-specific graph. */
    public void inject(Object object) {
        activityGraph.inject(object);
    }

    @Override public void setActionBarTitle(String title) {
        if (Strings.isEmpty(title)) {
            getSupportActionBar().setTitle(getString(R.string.app_name));
        } else if (title.equals(Constants.REDDIT_FRONTPAGE)) {
            getSupportActionBar().setTitle(getString(R.string.frontpage));
        } else {
            getSupportActionBar().setTitle(title);
        }
    }

}