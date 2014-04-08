package com.antew.redditinpictures.library.image;

import com.antew.redditinpictures.library.model.ImageSize;
import com.antew.redditinpictures.library.model.ImageType;

public class BasicImageType extends Image {

    public BasicImageType(java.lang.String url) {
        super(url);
    }

    @Override
    public java.lang.String getHash() {
        throw new UnsupportedOperationException("Standard URLs like \"www.test.com/image.png\" do not contain a hash");
    }

    @Override
    public String getRegexForUrlMatching() {
        throw new UnsupportedOperationException("Standard URLs like \"www.test.com/image.png\" do not require a RegEx for finding a hash");
    }

    @Override
    public java.lang.String getSize(ImageSize size) {
        return getUrl();
    }

    @Override
    public ImageType getImageType() {
        return ImageType.OTHER_SUPPORTED_IMAGE;
    }
}
