package com.antew.redditinpictures.library.image;

import com.antew.redditinpictures.library.enums.ImageSize;
import com.antew.redditinpictures.library.enums.ImageType;

public class PicsarusImageType extends Image {
    private static final String PICSARUS_URL          = "http://www.picsarus.com/%s.jpg";
    private static final String URL_REGEX = "^https?://(?:[i.]|[edge.]|[www.])*picsarus.com/(?:r/[\\w]+/)?([\\w]{6,})(\\..+)?$";
    
    public PicsarusImageType(java.lang.String url) {
        super(url);
    }
    
    @Override
    public String getSize(ImageSize size) {
        return String.format(PICSARUS_URL, getUrl());
    }

    @Override
    public ImageType getImageType() {
        return ImageType.PICASARUS_IMAGE;
    }

    @Override
    public String getRegexForUrlMatching() {
        return URL_REGEX;
    }

}
