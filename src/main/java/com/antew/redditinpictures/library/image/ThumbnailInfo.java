package com.antew.redditinpictures.library.image;

import android.content.res.Resources;

import com.antew.redditinpictures.pro.R;

public class ThumbnailInfo {
    private int thumbnailSize    = 0;
    private int thumbnailSpacing = 0;

    private ThumbnailInfo(Resources resources) {
        thumbnailSize = resources.getDimensionPixelSize(R.dimen.image_thumbnail_size);
        thumbnailSpacing = resources.getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
    }
    
    public static ThumbnailInfo getThumbnailInfo(Resources resources) {
        return new ThumbnailInfo(resources);
    }

    public int getSize() {
        return thumbnailSize;
    }

    public void setSize(int thumbnailSize) {
        this.thumbnailSize = thumbnailSize;
    }

    public int getSpacing() {
        return thumbnailSpacing;
    }

    public void setSpacing(int thumbnailSpacing) {
        this.thumbnailSpacing = thumbnailSpacing;
    }

}