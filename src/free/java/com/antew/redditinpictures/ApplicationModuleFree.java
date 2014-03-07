package com.antew.redditinpictures;

import com.antew.redditinpictures.ui.ImageDetailActivityFree;
import com.antew.redditinpictures.ui.ImageGridActivityFree;
import com.antew.redditinpictures.ui.ImageGridFragmentFree;
import com.antew.redditinpictures.ui.ImageListFragmentFree;
import dagger.Module;

/**
 * Dagger module for setting up provides statements.
 * Register all of your entry points below.
 */
@Module
    (
        complete = false,

        injects = {
            ImageGridFragmentFree.class,
            ImageListFragmentFree.class,
            ImageGridActivityFree.class,
            ImageDetailActivityFree.class
        }, library = true
    )
public class ApplicationModuleFree {

}