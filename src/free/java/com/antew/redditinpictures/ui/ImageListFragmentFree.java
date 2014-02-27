package com.antew.redditinpictures.ui;

import com.antew.redditinpictures.library.ui.ImageDetailActivity;
import com.antew.redditinpictures.library.ui.ImageListFragment;

public class ImageListFragmentFree extends ImageListFragment {
    @Override
    public Class<? extends ImageDetailActivity> getImageDetailActivityClass() {
        return ImageDetailActivityFree.class;
    }
}
