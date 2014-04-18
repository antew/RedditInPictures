package com.antew.redditinpictures.library.event;

public class DownloadImageEvent {
    private final String filename;
    private final String uniqueId;

    public DownloadImageEvent(String uniqueId, String filename) {
        this.uniqueId = uniqueId;
        this.filename = filename;
    }

    public String getFilename() {
        return filename;
    }

    public String getUniqueId() {
        return uniqueId;
    }
}
