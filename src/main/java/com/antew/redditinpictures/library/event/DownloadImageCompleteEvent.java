package com.antew.redditinpictures.library.event;

public class DownloadImageCompleteEvent {
    private final String filename;

    public DownloadImageCompleteEvent(String filename) {
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

}
