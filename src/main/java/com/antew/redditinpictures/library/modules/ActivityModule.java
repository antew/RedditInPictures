package com.antew.redditinpictures.library.modules;

import android.content.Context;
import com.antew.redditinpictures.library.annotations.ForActivity;
import com.antew.redditinpictures.library.ui.ImageDetailActivity;
import com.antew.redditinpictures.library.ui.ImageGridActivity;
import com.antew.redditinpictures.library.ui.ImgurAlbumActivity;
import com.antew.redditinpictures.library.ui.base.BaseFragmentActivity;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

/**
 * This module represents objects which exist only for the scope of a single activity. We can
 * safely create singletons using the activity instance because ths entire object graph will only
 * ever exist inside of that activity.
 */
@Module(
    injects = {
        ImageGridActivity.class, ImageDetailActivity.class, ImgurAlbumActivity.class
    },
    addsTo = RootModule.class,
    library = true
)
public class ActivityModule {
    private final BaseFragmentActivity activity;

    public ActivityModule(BaseFragmentActivity activity) {
        this.activity = activity;
    }

    /**
     * Allow the activity context to be injected but require that it be annotated with
     * {@link ForActivity @ForActivity} to explicitly differentiate it from application context.
     */
    @Provides @ForActivity @Singleton Context provideActivityContext() {
        return activity;
    }
}