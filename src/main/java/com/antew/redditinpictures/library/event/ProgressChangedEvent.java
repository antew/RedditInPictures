package com.antew.redditinpictures.library.event;

public class ProgressChangedEvent {
    private final boolean inProgress;

    public ProgressChangedEvent(boolean inProgress) {
        this.inProgress = inProgress;
    }

    public boolean isInProgress() {
        return inProgress;
    }
}
