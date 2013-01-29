package com.antew.redditinpictures.library.interfaces;

import java.util.List;

public interface RedditPostFilter<PostData> {
    public List<PostData> filterPosts(boolean includeNsfwImages);
}
