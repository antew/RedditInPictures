package com.antew.redditinpictures.library.ui.base;

import android.os.Bundle;
import com.actionbarsherlock.app.SherlockFragment;
import com.antew.redditinpictures.library.Injector;
import com.squareup.otto.Bus;
import javax.inject.Inject;

public class BaseFragment extends SherlockFragment {
    @Inject
    protected Bus mBus;

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Injector.inject(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);
    }

    @Override
    public void onPause() {
        mBus.unregister(this);
        super.onPause();
    }
}
