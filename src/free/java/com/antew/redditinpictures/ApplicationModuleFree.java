package com.antew.redditinpictures;

import com.antew.redditinpictures.library.RedditInPicturesApplication;
import com.antew.redditinpictures.ui.ImageDetailActivityFree;
import com.antew.redditinpictures.ui.ImageGridActivityFree;
import com.antew.redditinpictures.ui.ImageGridFragmentFree;
import com.antew.redditinpictures.ui.ImageListFragmentFree;
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
            ImageGridFragmentFree.class,
            ImageListFragmentFree.class,
            ImageGridActivityFree.class,
            ImageDetailActivityFree.class
        }, library = true
    )
public class ApplicationModuleFree {

    @Singleton
    @Provides Bus provideOttoBus() {
        return new MainThreadBus(new Bus());
    }

}