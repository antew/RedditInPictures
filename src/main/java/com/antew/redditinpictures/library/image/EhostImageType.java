package com.antew.redditinpictures.library.image;

import com.antew.redditinpictures.library.enums.ImageSize;
import com.antew.redditinpictures.library.enums.ImageType;

public class EhostImageType extends Image {
    private static final String URL_REGEX = "^http://(?:i\\.)?(?:\\d+.)?eho.st/(\\w+)/?";
    private static final String EHOST_URL = "http://i.eho.st/%s.jpg";

    public EhostImageType(java.lang.String url) {
        super(url);
    }

    @Override
    public java.lang.String getSize(ImageSize size) {
        String hash = getHash();
        String decodedUrl = null;

        if (hash != null) {
            decodedUrl = String.format(EHOST_URL, hash);
        }

        return decodedUrl;
    }

    @Override
    public ImageType getImageType() {
        return ImageType.EHOST_IMAGE;
    }

    @Override
    public String getRegexForUrlMatching() {
        return URL_REGEX;
    }
}
