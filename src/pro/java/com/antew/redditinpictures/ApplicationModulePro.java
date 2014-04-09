package com.antew.redditinpictures;

import com.antew.redditinpictures.library.service.RedditService;
import com.antew.redditinpictures.library.ui.ImageDetailActivity;
import com.antew.redditinpictures.library.ui.ImageDetailFragment;
import com.antew.redditinpictures.library.ui.ImgurAlbumActivity;
import com.antew.redditinpictures.library.ui.ImgurAlbumFragment;
import com.antew.redditinpictures.library.ui.RedditFragmentActivity;
import com.antew.redditinpictures.library.ui.RedditImageGridFragment;
import com.antew.redditinpictures.library.ui.RedditImageListFragment;
import dagger.Module;

@Module(
    complete = false,
    overrides = true,
    injects = {
        ImageDetailActivity.class, ImageDetailFragment.class, ImgurAlbumActivity.class, RedditFragmentActivity.class,
        RedditImageListFragment.class, RedditImageGridFragment.class, ImgurAlbumFragment.class, RedditService.GetNewPostsIfNeededTask.class
    }, library = true
)
public class ApplicationModulePro {

}