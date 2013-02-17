package com.antew.redditinpictures.library.image;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.antew.redditinpictures.library.enums.ImageSize;
import com.antew.redditinpictures.library.enums.ImageType;
import com.antew.redditinpictures.library.logging.Log;

public class QuickMemeImageType extends Image {
    private static final String QUICKMEME_URL = "http://i.qkme.me/%s.jpg";
    private static final String TAG = QuickMemeImageType.class.getSimpleName();
    
    public QuickMemeImageType(java.lang.String url) {
        super(url);
    }

    @Override
    public java.lang.String getHash() {
        
        Pattern pattern = Pattern.compile("^http://(?:(?:www.)?quickmeme.com/meme|qkme.me|i.qkme.me)/([\\w]+)/?", Pattern.CASE_INSENSITIVE);
        String hash = null;

        if (pattern != null) {
            Matcher m = pattern.matcher(getUrl());
            while (m.find())
                hash = m.group(1);
        }

        Log.i(TAG, "Hash = " + hash);
        return hash;
    }

    @Override
    public java.lang.String getSize(ImageSize size) {
        String hash = getHash();
        String decodedUrl = null;
        
        if (hash != null) {
            decodedUrl = String.format(QUICKMEME_URL, hash);
        }
        
        return decodedUrl;
    }

    @Override
    public ImageType getImageType() {
        return ImageType.QUICKMEME_IMAGE;
    }

}
