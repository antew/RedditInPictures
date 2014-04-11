package com.antew.redditinpictures;

import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.ui.ImageDetailFragment;
import com.antew.redditinpictures.ui.ImageDetailActivityFree;
import com.antew.redditinpictures.ui.ImageDetailFragmentFree;
import com.antew.redditinpictures.ui.ImgurAlbumActivityFree;
import com.antew.redditinpictures.ui.ImgurAlbumFragmentFree;
import com.antew.redditinpictures.ui.RedditFragmentActivityFree;
import com.antew.redditinpictures.ui.RedditImageGridFragmentFree;
import com.antew.redditinpictures.ui.RedditImageListFragmentFree;
import dagger.Module;

/**
 * Dagger module for setting up provides statements.
 * Register all of your entry points below.
 */
@Module(
    complete = false,
    overrides = true,
    injects = {
        RedditImageGridFragmentFree.class, RedditImageListFragmentFree.class, RedditFragmentActivityFree.class,
        ImageDetailActivityFree.class, ImageDetailFragmentFree.class, ImgurAlbumActivityFree.class, ImgurAlbumFragmentFree.class,
        ImageDetailFragment.class,
        RedditService.GetNewPostsIfNeededTask.class
    }, library = true)
// TODO: Figure out why this isn't using ImageDetailFragmentFree, but instead only using ImageDetailFragment.
public class ApplicationModuleFree {

}