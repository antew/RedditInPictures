package com.antew.redditinpictures.library.image;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.antew.redditinpictures.library.enums.ImageSize;
import com.antew.redditinpictures.library.enums.ImageType;

public class EhostImageType extends Image {
    private static final String EHOST_URL             = "http://i.eho.st/%s.jpg";
    
    public EhostImageType(java.lang.String url) {
        super(url);
    }

    @Override
    public java.lang.String getHash() {
        Pattern pattern = Pattern.compile("^http://(?:i\\.)?(?:\\d+.)?eho.st/(\\w+)/?", Pattern.CASE_INSENSITIVE);
        String hash = null;

        if (pattern != null) {
            Matcher m = pattern.matcher(getUrl());
            while (m.find())
                hash = m.group(1);
        }

        return hash;
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

}
