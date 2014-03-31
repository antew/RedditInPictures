package com.antew.redditinpictures;

import com.antew.redditinpictures.library.ui.ImageDetailActivity;
import com.antew.redditinpictures.library.ui.ImageDetailFragment;
import com.antew.redditinpictures.library.ui.ImageGridActivity;
import com.antew.redditinpictures.library.ui.ImageGridFragment;
import com.antew.redditinpictures.library.ui.ImageListFragment;
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
        ImageGridFragment.class, ImageListFragment.class, ImageGridActivity.class,
        ImageDetailActivity.class, ImageDetailFragment.class, ImgurAlbumActivity.class,
        RedditFragmentActivity.class, RedditImageListFragment.class, RedditImageGridFragment.class,
        ImgurAlbumFragment.class,
    }, library = true
)
public class ApplicationModulePro {

}