package com.antew.redditinpictures.library.image;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.antew.redditinpictures.library.enums.ImageSize;
import com.antew.redditinpictures.library.enums.ImageType;

public class PicsarusImageType extends Image {
    private static final String PICSARUS_URL          = "http://www.picsarus.com/%s.jpg";
    
    public PicsarusImageType(java.lang.String url) {
        super(url);
    }
    
    @Override
    public java.lang.String getHash() {
        String hash = null;
        Pattern pattern = Pattern.compile("^https?://(?:[i.]|[edge.]|[www.])*picsarus.com/(?:r/[\\w]+/)?([\\w]{6,})(\\..+)?$", Pattern.CASE_INSENSITIVE);

        if (pattern != null) {
            Matcher m = pattern.matcher(getUrl());
            while (m.find())
                hash = m.group(1);
        }

        return hash;
    }

    @Override
    public java.lang.String getSize(ImageSize size) {
        return String.format(PICSARUS_URL, getUrl());
    }

    @Override
    public ImageType getImageType() {
        return ImageType.PICASARUS_IMAGE;
    }

}
