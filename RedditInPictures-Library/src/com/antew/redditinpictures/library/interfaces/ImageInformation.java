package com.antew.redditinpictures.library.interfaces;

import com.antew.redditinpictures.library.enums.ImageSize;
import com.antew.redditinpictures.library.enums.ImageType;

public interface ImageInformation {
    public String getHash();
    public String getSize(ImageSize size);
    public ImageType getImageType();
}
