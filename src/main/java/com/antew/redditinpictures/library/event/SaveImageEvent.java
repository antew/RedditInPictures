package com.antew.redditinpictures.library.event;

import com.antew.redditinpictures.library.model.reddit.PostData;

public class SaveImageEvent {
    PostData postData;

    public SaveImageEvent(PostData postData) {
        this.postData = postData;
    }

    public PostData getPostData() {
        return postData;
    }

}
