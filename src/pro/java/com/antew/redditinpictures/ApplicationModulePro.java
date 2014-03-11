package com.antew.redditinpictures;

import com.antew.redditinpictures.library.ui.ImageDetailActivity;
import com.antew.redditinpictures.library.ui.ImageDetailFragment;
import com.antew.redditinpictures.library.ui.ImageGridActivity;
import com.antew.redditinpictures.library.ui.ImageGridFragment;
import com.antew.redditinpictures.library.ui.ImageListFragment;
import com.antew.redditinpictures.library.ui.ImgurAlbumActivity;

import dagger.Module;

@Module
(
    complete = false,
    overrides = true,
    injects = {
            ImageGridFragment.class,
            ImageListFragment.class,
            ImageGridActivity.class,
            ImageDetailActivity.class,
            ImageDetailFragment.class,
            ImgurAlbumActivity.class
    }, library = true
)
public class ApplicationModulePro {

}