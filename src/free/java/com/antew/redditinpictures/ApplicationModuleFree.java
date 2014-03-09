package com.antew.redditinpictures;

import com.antew.redditinpictures.library.RedditInPicturesApplication;
import com.antew.redditinpictures.ui.ImageDetailActivityFree;
import com.antew.redditinpictures.ui.ImageGridActivityFree;
import com.antew.redditinpictures.ui.ImageGridFragmentFree;
import com.antew.redditinpictures.ui.ImageListFragmentFree;
import com.antew.redditinpictures.ui.ImgurAlbumActivityFree;
import dagger.Module;

/**
 * Dagger module for setting up provides statements.
 * Register all of your entry points below.
 */
@Module
    (
        complete = false,
        overrides = true,
        injects = {
            ImageGridFragmentFree.class,
            ImageListFragmentFree.class,
            ImageGridActivityFree.class,
            ImageDetailActivityFree.class,
            ImgurAlbumActivityFree.class
        }, library = true
    )
public class ApplicationModuleFree {

}