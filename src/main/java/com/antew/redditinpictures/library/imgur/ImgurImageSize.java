package com.antew.redditinpictures.library.imgur;

public enum ImgurImageSize {
    SMALL_SQURE("s"),
    BIG_SQUARE("b"),
    SMALL_THUMBNAIL("t"),
    MEDIUM_THUMBNAIL("m"),
    LARGE_THUMBNAIL("l"),
    HUGE_THUMBNAIL("h");

    private final String suffix;

    ImgurImageSize(String suffix) {
        this.suffix = suffix;
    }


    public String getSuffix() {
        return suffix;
    }
}
