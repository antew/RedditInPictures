package com.antew.redditinpictures.library;

import com.antew.redditinpictures.library.ui.ImageDetailActivity;
import com.antew.redditinpictures.library.ui.ImageGridActivity;
import com.antew.redditinpictures.library.ui.ImageGridFragment;
import com.antew.redditinpictures.library.ui.ImageListFragment;
import com.antew.redditinpictures.library.utils.MainThreadBus;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for setting up provides statements.
 * Register all of your entry points below.
 */
@Module
    (
        complete = false,

        injects = {
            RedditInPicturesApplication.class,
            ImageGridFragment.class,
            ImageListFragment.class,
            ImageGridActivity.class,
            ImageDetailActivity.class
        }, library = true
    )
public class ApplicationModule {

    @Singleton
    @Provides Bus provideOttoBus() {
        return new MainThreadBus(new Bus());
    }

}