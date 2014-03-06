package com.antew.redditinpictures.library;

import com.antew.redditinpictures.library.utils.MainThreadBus;
import com.squareup.otto.Bus;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * Dagger module for setting up provides statements.
 * Register all of your entry points below.
 */
@Module
    (
        complete = false,

        injects = {
            RedditInPicturesApplication.class,
        }, library = true

    )
public class ApplicationModule {

    @Singleton
    @Provides Bus provideOttoBus() {
        return new MainThreadBus(new Bus());
    }

}