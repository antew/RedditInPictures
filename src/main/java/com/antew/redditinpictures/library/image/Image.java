package com.antew.redditinpictures.library.image;

import com.antew.redditinpictures.library.interfaces.ImageInformation;
import com.antew.redditinpictures.library.utils.RegexUtil;


public abstract class Image implements ImageInformation {
    private String url;
    
    public Image(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
    
    public String getHash() {
        return RegexUtil.getMatch(getRegexForUrlMatching(), getUrl());
    }
    
    public abstract String getRegexForUrlMatching();
    
}
