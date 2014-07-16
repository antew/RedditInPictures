package com.antew.redditinpictures.library.modules;

import com.antew.redditinpictures.library.adapter.RedditCommentAdapter;
import com.antew.redditinpictures.library.ui.ImageDetailFragment;
import com.commonsware.cwac.anddown.AndDown;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module(library = true, complete = false)
public class CommonModule {

    @Provides
    @Singleton
    AndDown provideAndDown() {
        return new AndDown();
    }
}
