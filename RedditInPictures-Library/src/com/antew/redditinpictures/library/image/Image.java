package com.antew.redditinpictures.library.image;

import com.antew.redditinpictures.library.interfaces.ImageInformation;


public abstract class Image implements ImageInformation {
    private String url;
    
    public Image(String url) {
        this.url = url;
    }
    
    public String getUrl() {
        return url;
    }
    
}
