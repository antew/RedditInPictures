package com.antew.redditinpictures.library.interfaces;

import com.antew.redditinpictures.library.model.ImageSize;
import com.antew.redditinpictures.library.model.ImageType;

public interface ImageInformation {
    public String getHash();

    public String getSize(ImageSize size);

    public ImageType getImageType();
}
