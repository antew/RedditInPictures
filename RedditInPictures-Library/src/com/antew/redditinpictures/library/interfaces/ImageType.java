package com.antew.redditinpictures.library.interfaces;

import com.antew.redditinpictures.library.imgur.ImageResolver.ImageSize;

public interface ImageType<T> {
    public T resolve();
    public String getHash();
    public String getSize(T image, ImageSize size);
}
